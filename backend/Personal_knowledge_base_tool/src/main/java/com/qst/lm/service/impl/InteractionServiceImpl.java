package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionMapper;
import com.qst.lm.mapper.InteractionCollectMapper;
import com.qst.lm.mapper.InteractionCommentMapper;
import com.qst.lm.mapper.InteractionLikeMapper;
import com.qst.lm.mapper.NoteMapper;
import com.qst.lm.mapper.NotificationMapper;
import com.qst.lm.mapper.UserMapper;
import com.qst.lm.pojo.Collection;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.pojo.InteractionCollect;
import com.qst.lm.pojo.InteractionComment;
import com.qst.lm.pojo.InteractionLike;
import com.qst.lm.pojo.Note;
import com.qst.lm.pojo.Notification;
import com.qst.lm.pojo.User;
import com.qst.lm.service.IInteractionService;
import com.qst.lm.utils.SensitiveWordFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 互动服务实现类
 * 提供点赞、评论、收藏等互动功能
 * 包含防刷机制(使用Redis,1秒间隔限制)和敏感词过滤
 */
@Slf4j
@Service
public class InteractionServiceImpl implements IInteractionService {

    private final InteractionLikeMapper interactionLikeMapper;
    private final InteractionCommentMapper interactionCommentMapper;
    private final InteractionCollectMapper interactionCollectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationMapper notificationMapper;
    private final CollectionMapper collectionMapper;
    private final NoteMapper noteMapper;
    private final CollectionItemMapper collectionItemMapper;
    private final UserMapper userMapper;

    /**
     * Redis防刷key前缀 - 点赞
     */
    private static final String LIKE_RATE_LIMIT_KEY = "interaction:like:limit:";

    /**
     * Redis防刷key前缀 - 评论
     */
    private static final String COMMENT_RATE_LIMIT_KEY = "interaction:comment:limit:";

    /**
     * Redis防刷key前缀 - 收藏
     */
    private static final String COLLECT_RATE_LIMIT_KEY = "interaction:collect:limit:";

    /**
     * 防刷间隔时间(秒)
     */
    private static final long RATE_LIMIT_SECONDS = 1;

    /**
     * 评论防刷间隔时间(秒)
     */
    private static final long COMMENT_RATE_LIMIT_SECONDS = 5;

    public InteractionServiceImpl(InteractionLikeMapper interactionLikeMapper,
                                  InteractionCommentMapper interactionCommentMapper,
                                  InteractionCollectMapper interactionCollectMapper,
                                  RedisTemplate<String, Object> redisTemplate,
                                  NotificationMapper notificationMapper,
                                  CollectionMapper collectionMapper,
                                  NoteMapper noteMapper,
                                  CollectionItemMapper collectionItemMapper,
                                  UserMapper userMapper) {
        this.interactionLikeMapper = interactionLikeMapper;
        this.interactionCommentMapper = interactionCommentMapper;
        this.interactionCollectMapper = interactionCollectMapper;
        this.redisTemplate = redisTemplate;
        this.notificationMapper = notificationMapper;
        this.collectionMapper = collectionMapper;
        this.noteMapper = noteMapper;
        this.collectionItemMapper = collectionItemMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R like(Long userId, Object dto) {
        // 参数校验
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        // 防刷检查
        String rateLimitKey = LIKE_RATE_LIMIT_KEY + userId;
        Boolean exists = redisTemplate.hasKey(rateLimitKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException("操作过于频繁,请稍后再试");
        }

        // 从dto中解析参数(使用Map兼容不同DTO类型)
        Map<String, Object> params = parseDto(dto);
        Long targetId = getLongParam(params, "targetId");
        String targetType = getStringParam(params, "targetType");

        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        // 检查是否已点赞
        InteractionLike existing = interactionLikeMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (existing != null) {
            throw new BusinessException("已点赞");
        }

        // 执行点赞
        InteractionLike like = new InteractionLike();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType);
        interactionLikeMapper.insert(like);

        // 设置防刷标记
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 发送点赞通知
        try {
            Long targetUserId = findTargetContentUserId(targetId, targetType);
            if (targetUserId != null && !targetUserId.equals(userId)) {
                String actorNickname = getUserNickname(userId);
                createInteractionNotification(targetUserId, userId, actorNickname, targetId, targetType, 5);
            }
        } catch (Exception e) {
            log.warn("发送点赞通知失败", e);
        }

        // 如果是笔记点赞，更新笔记热度评分
        if ("note".equals(targetType)) {
            try {
                incrementNoteLikes(targetId);
            } catch (Exception e) {
                log.warn("更新笔记热度评分失败", e);
            }
        }

        log.info("用户[{}]对[{}][{}]点赞成功", userId, targetType, targetId);
        return R.success("点赞成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R unlike(Long userId, Object dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        // 防刷检查
        String rateLimitKey = LIKE_RATE_LIMIT_KEY + userId;
        Boolean exists = redisTemplate.hasKey(rateLimitKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException("操作过于频繁,请稍后再试");
        }

        Map<String, Object> params = parseDto(dto);
        Long targetId = getLongParam(params, "targetId");
        String targetType = getStringParam(params, "targetType");

        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        // 查找点赞记录
        InteractionLike like = interactionLikeMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (like == null) {
            throw new BusinessException("未找到点赞记录");
        }

        // 逻辑删除点赞记录
        LambdaUpdateWrapper<InteractionLike> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(InteractionLike::getId, like.getId())
                .set(InteractionLike::getDeleted, 1);
        interactionLikeMapper.update(null, wrapper);

        // 设置防刷标记
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 如果是笔记取消点赞，更新笔记热度评分
        if ("note".equals(targetType)) {
            try {
                decrementNoteLikes(targetId);
            } catch (Exception e) {
                log.warn("更新笔记热度评分失败", e);
            }
        }

        log.info("用户[{}]对[{}][{}]取消点赞成功", userId, targetType, targetId);
        return R.success("取消点赞成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R comment(Long userId, Object dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        // 防刷检查
        String rateLimitKey = COMMENT_RATE_LIMIT_KEY + userId;
        Boolean exists = redisTemplate.hasKey(rateLimitKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException("操作过于频繁,请稍后再试");
        }

        Map<String, Object> params = parseDto(dto);
        Long targetId = getLongParam(params, "targetId");
        String targetType = getStringParam(params, "targetType");
        String content = getStringParam(params, "content");
        Long parentId = getLongParam(params, "parentId");

        if (targetId == null || !StringUtils.hasText(targetType) || !StringUtils.hasText(content)) {
            throw new BusinessException("参数不完整");
        }

        // 敏感词过滤
        String filteredContent = SensitiveWordFilter.filterSensitiveWord(content);

        // 检查评论嵌套层级
        if (parentId != null) {
            int depth = 0;
            Long currentParentId = parentId;
            while (currentParentId != null && depth < 4) {
                InteractionComment parent = interactionCommentMapper.selectById(currentParentId);
                if (parent == null) break;
                currentParentId = parent.getParentId();
                depth++;
            }
            if (depth >= 3) {
                throw new BusinessException("评论嵌套层级不能超过3层");
            }
        }

        // 发表评论
        InteractionComment comment = new InteractionComment();
        comment.setUserId(userId);
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        comment.setContent(filteredContent);
        comment.setParentId(parentId);
        interactionCommentMapper.insert(comment);

        // 设置防刷标记
        redisTemplate.opsForValue().set(rateLimitKey, "1", COMMENT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 发送评论通知
        try {
            Long targetUserId = findTargetContentUserId(targetId, targetType);
            if (targetUserId != null && !targetUserId.equals(userId)) {
                String actorNickname = getUserNickname(userId);
                createInteractionNotification(targetUserId, userId, actorNickname, targetId, targetType, 6);
            }
        } catch (Exception e) {
            log.warn("发送评论通知失败", e);
        }

        log.info("用户[{}]对[{}][{}]发表评论成功", userId, targetType, targetId);
        return R.success("评论成功", comment);
    }

    @Override
    public R getCommentList(Long targetId, String targetType, Long parentId, Integer page, Integer size) {
        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        Page<InteractionComment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<InteractionComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InteractionComment::getTargetId, targetId)
                .eq(InteractionComment::getTargetType, targetType)
                .eq(InteractionComment::getDeleted, 0);

        // 如果指定了parentId,则查询该评论下的回复;否则查询一级评论
        if (parentId != null) {
            wrapper.eq(InteractionComment::getParentId, parentId);
        } else {
            wrapper.isNull(InteractionComment::getParentId);
        }

        wrapper.orderByAsc(InteractionComment::getCreatedAt);
        Page<InteractionComment> result = interactionCommentMapper.selectPage(pageObj, wrapper);

        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R collectContent(Long userId, Object dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        // 防刷检查
        String rateLimitKey = COLLECT_RATE_LIMIT_KEY + userId;
        Boolean exists = redisTemplate.hasKey(rateLimitKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new BusinessException("操作过于频繁,请稍后再试");
        }

        Map<String, Object> params = parseDto(dto);
        Long targetId = getLongParam(params, "targetId");
        String targetType = getStringParam(params, "targetType");

        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        // 检查是否已收藏
        InteractionCollect existing = interactionCollectMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (existing != null) {
            throw new BusinessException("已收藏");
        }

        // 执行收藏
        InteractionCollect collect = new InteractionCollect();
        collect.setUserId(userId);
        collect.setTargetId(targetId);
        collect.setTargetType(targetType);
        interactionCollectMapper.insert(collect);

        // 设置防刷标记
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 发送收藏通知
        try {
            Long targetUserId = findTargetContentUserId(targetId, targetType);
            if (targetUserId != null && !targetUserId.equals(userId)) {
                String actorNickname = getUserNickname(userId);
                createInteractionNotification(targetUserId, userId, actorNickname, targetId, targetType, 7);
            }
        } catch (Exception e) {
            log.warn("发送收藏通知失败", e);
        }

        log.info("用户[{}]收藏[{}][{}]成功", userId, targetType, targetId);
        return R.success("收藏成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R uncollectContent(Long userId, Object dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        Map<String, Object> params = parseDto(dto);
        Long targetId = getLongParam(params, "targetId");
        String targetType = getStringParam(params, "targetType");

        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        // 查找收藏记录
        InteractionCollect collect = interactionCollectMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (collect == null) {
            throw new BusinessException("未找到收藏记录");
        }

        // 逻辑删除收藏记录
        LambdaUpdateWrapper<InteractionCollect> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(InteractionCollect::getId, collect.getId())
                .set(InteractionCollect::getDeleted, 1);
        interactionCollectMapper.update(null, wrapper);

        log.info("用户[{}]取消收藏[{}][{}]成功", userId, targetType, targetId);
        return R.success("取消收藏成功");
    }

    @Override
    public R getLikeCount(Long targetId, String targetType) {
        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        Long count = interactionLikeMapper.countByTarget(targetId, targetType);
        Map<String, Object> result = new HashMap<>(2);
        result.put("targetId", targetId);
        result.put("targetType", targetType);
        result.put("likeCount", count != null ? count : 0);

        return R.success(result);
    }

    @Override
    public R getCollectCount(Long targetId, String targetType) {
        if (targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        Long count = interactionCollectMapper.countByTarget(targetId, targetType);
        Map<String, Object> result = new HashMap<>(2);
        result.put("targetId", targetId);
        result.put("targetType", targetType);
        result.put("collectCount", count != null ? count : 0);

        return R.success(result);
    }

    @Override
    public R checkLikeStatus(Long userId, Long targetId, String targetType) {
        if (userId == null || targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        InteractionLike like = interactionLikeMapper.existsByUserAndTarget(userId, targetId, targetType);
        Map<String, Object> result = new HashMap<>(3);
        result.put("targetId", targetId);
        result.put("targetType", targetType);
        result.put("isLiked", like != null);

        return R.success(result);
    }

    @Override
    public R checkCollectStatus(Long userId, Long targetId, String targetType) {
        if (userId == null || targetId == null || !StringUtils.hasText(targetType)) {
            throw new BusinessException("参数不完整");
        }

        InteractionCollect collect = interactionCollectMapper.existsByUserAndTarget(userId, targetId, targetType);
        Map<String, Object> result = new HashMap<>(3);
        result.put("targetId", targetId);
        result.put("targetType", targetType);
        result.put("isCollected", collect != null);

        return R.success(result);
    }

    /**
     * 将DTO解析为Map(兼容不同DTO类型)
     *
     * @param dto DTO对象
     * @return Map形式的参数
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDto(Object dto) {
        if (dto instanceof Map) {
            return (Map<String, Object>) dto;
        }
        // 对于非Map类型,尝试通过反射获取属性
        Map<String, Object> params = new HashMap<>(8);
        try {
            java.lang.reflect.Field[] fields = dto.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                params.put(field.getName(), field.get(dto));
            }
        } catch (IllegalAccessException e) {
            log.error("解析DTO失败", e);
            throw new BusinessException("请求参数格式错误");
        }
        return params;
    }

    /**
     * 从Map中获取Long类型参数
     */
    private Long getLongParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 从Map中获取String类型参数
     */
    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 根据目标类型查找内容所属用户ID
     */
    private Long findTargetContentUserId(Long targetId, String targetType) {
        try {
            if ("collection".equalsIgnoreCase(targetType)) {
                Collection collection = collectionMapper.selectById(targetId);
                return collection != null ? collection.getUserId() : null;
            } else if ("note".equalsIgnoreCase(targetType)) {
                Note note = noteMapper.selectById(targetId);
                return note != null ? note.getUserId() : null;
            } else if ("collection_item".equalsIgnoreCase(targetType) || "item".equalsIgnoreCase(targetType)) {
                CollectionItem item = collectionItemMapper.selectById(targetId);
                return item != null ? item.getUserId() : null;
            }
        } catch (Exception e) {
            log.warn("查找目标内容用户ID失败, targetType={}, targetId={}", targetType, targetId, e);
        }
        return null;
    }

    /**
     * 获取用户昵称
     */
    private String getUserNickname(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            return user != null && user.getNickname() != null ? user.getNickname() : "用户" + userId;
        } catch (Exception e) {
            log.warn("获取用户昵称失败, userId={}", userId, e);
            return "用户" + userId;
        }
    }

    /**
     * 创建互动通知
     *
     * @param targetUserId   目标内容所属用户ID
     * @param actorId        执行操作的用户ID
     * @param actorNickname  执行操作的用户昵称
     * @param targetId       目标内容ID
     * @param targetType     目标内容类型
     * @param notifyType     通知类型 (5=点赞, 6=评论, 7=收藏)
     */
    private void createInteractionNotification(Long targetUserId, Long actorId, String actorNickname,
                                                Long targetId, String targetType, int notifyType) {
        try {
            String typeDesc;
            switch (notifyType) {
                case 5:
                    typeDesc = "点赞了";
                    break;
                case 6:
                    typeDesc = "评论了";
                    break;
                case 7:
                    typeDesc = "收藏了";
                    break;
                default:
                    typeDesc = "互动了";
            }

            String title = actorNickname + typeDesc + "您的" + targetType;
            String content = actorNickname + typeDesc + "您的" + targetType + "(ID:" + targetId + ")";

            Notification notification = new Notification();
            notification.setUserId(targetUserId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setNotifyType(notifyType);
            notification.setIsRead(0);

            notificationMapper.insert(notification);
            log.info("创建互动通知成功: userId={}, notifyType={}, title={}", targetUserId, notifyType, title);
        } catch (Exception e) {
            log.error("创建互动通知失败: targetUserId={}, notifyType={}", targetUserId, notifyType, e);
        }
    }

    /**
     * 增加笔记点赞数并更新热度评分
     * @param noteId 笔记ID
     */
    private void incrementNoteLikes(Long noteId) {
        try {
            Note note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记[{}]不存在，无法更新热度", noteId);
                return;
            }

            // 增加点赞数
            Integer currentLikes = note.getLikes() != null ? note.getLikes() : 0;
            note.setLikes(currentLikes + 1);

            // 重新计算热度评分
            Integer views = note.getViews() != null ? note.getViews() : 0;
            Integer likes = note.getLikes();
            double hotScore = (views * 0.6) + (likes * 0.4);
            note.setHotScore(hotScore);

            // 更新数据库
            noteMapper.updateById(note);

            log.debug("笔记[{}]点赞数更新: {} -> {}, 热度评分: {}",
                    noteId, currentLikes, note.getLikes(), note.getHotScore());
        } catch (Exception e) {
            log.error("更新笔记[{}]热度评分失败", noteId, e);
        }
    }

    /**
     * 减少笔记点赞数并更新热度评分（取消点赞时调用）
     * @param noteId 笔记ID
     */
    private void decrementNoteLikes(Long noteId) {
        try {
            Note note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记[{}]不存在，无法更新热度", noteId);
                return;
            }

            // 减少点赞数（确保不小于0）
            Integer currentLikes = note.getLikes() != null ? note.getLikes() : 0;
            note.setLikes(Math.max(0, currentLikes - 1));

            // 重新计算热度评分
            Integer views = note.getViews() != null ? note.getViews() : 0;
            Integer likes = note.getLikes();
            double hotScore = (views * 0.6) + (likes * 0.4);
            note.setHotScore(hotScore);

            // 更新数据库
            noteMapper.updateById(note);

            log.debug("笔记[{}]点赞数减少: {} -> {}, 热度评分: {}",
                    noteId, currentLikes, note.getLikes(), note.getHotScore());
        } catch (Exception e) {
            log.error("更新笔记[{}]热度评分失败", noteId, e);
        }
    }
}
