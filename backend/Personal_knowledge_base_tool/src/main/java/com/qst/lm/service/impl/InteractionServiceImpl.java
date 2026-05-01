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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private static final String LIKE_RATE_LIMIT_KEY = "interaction:like:limit:";
    private static final String COMMENT_RATE_LIMIT_KEY = "interaction:comment:limit:";
    private static final String COLLECT_RATE_LIMIT_KEY = "interaction:collect:limit:";
    private static final long RATE_LIMIT_SECONDS = 1;
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
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

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

        InteractionLike existing = interactionLikeMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (existing != null) {
            throw new BusinessException("已点赞");
        }

        InteractionLike like = new InteractionLike();
        like.setUserId(userId);
        like.setTargetId(targetId);
        like.setTargetType(targetType);
        interactionLikeMapper.insert(like);

        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        try {
            Long targetUserId = findTargetContentUserId(targetId, targetType);
            if (targetUserId != null && !targetUserId.equals(userId)) {
                String actorNickname = getUserNickname(userId);
                createInteractionNotification(targetUserId, userId, actorNickname, targetId, targetType, 5);
            }
        } catch (Exception e) {
            log.warn("发送点赞通知失败", e);
        }

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

        InteractionLike like = interactionLikeMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (like == null) {
            throw new BusinessException("未找到点赞记录");
        }

        LambdaUpdateWrapper<InteractionLike> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(InteractionLike::getId, like.getId())
                .set(InteractionLike::getDeleted, 1);
        interactionLikeMapper.update(null, wrapper);

        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

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

        String filteredContent = SensitiveWordFilter.filterSensitiveWord(content);

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

        InteractionComment comment = new InteractionComment();
        comment.setUserId(userId);
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        comment.setContent(filteredContent);
        comment.setParentId(parentId);
        interactionCommentMapper.insert(comment);

        redisTemplate.opsForValue().set(rateLimitKey, "1", COMMENT_RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

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

        InteractionCollect existing = interactionCollectMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (existing != null) {
            throw new BusinessException("已收藏");
        }

        InteractionCollect collect = new InteractionCollect();
        collect.setUserId(userId);
        collect.setTargetId(targetId);
        collect.setTargetType(targetType);
        interactionCollectMapper.insert(collect);

        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

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

        InteractionCollect collect = interactionCollectMapper.existsByUserAndTarget(userId, targetId, targetType);
        if (collect == null) {
            throw new BusinessException("未找到收藏记录");
        }

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

    @Override
    public R getInteractionMessages(Long userId, String type, Integer isRead, Integer pageNum, Integer pageSize) {
        Integer notifyType = resolveNotifyType(type);
        if (notifyType == null) {
            throw new BusinessException("互动类型不支持");
        }

        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (currentPage - 1) * currentSize;

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getNotifyType, notifyType)
                .orderByAsc(Notification::getIsRead)
                .orderByDesc(Notification::getCreatedAt);
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        int end = Math.min(offset + currentSize, notifications.size());
        for (int i = offset; i < end; i++) {
            list.add(convertInteractionNotification(notifications.get(i), type));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("pageNum", currentPage);
        result.put("pageSize", currentSize);
        result.put("total", notifications.size());
        return R.success(result);
    }

    @Override
    public R markInteractionMessageRead(Long userId, Long id) {
        Notification notification = notificationMapper.selectOne(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .in(Notification::getNotifyType, 5, 6, 7));
        if (notification == null) {
            throw new BusinessException("互动消息不存在或无权限");
        }
        if (notification.getIsRead() == null || notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationMapper.updateById(notification);
        }
        return R.success("标记已读成功");
    }

    @Override
    public R markAllInteractionMessagesRead(Long userId, String type) {
        Integer notifyType = resolveNotifyType(type);
        if (notifyType == null) {
            throw new BusinessException("互动类型不支持");
        }
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getNotifyType, notifyType)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        notificationMapper.update(null, wrapper);
        return R.success("全部已读成功");
    }

    @Override
    public R deleteInteractionMessage(Long userId, Long id) {
        int rows = notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .in(Notification::getNotifyType, 5, 6, 7));
        if (rows <= 0) {
            throw new BusinessException("互动消息不存在或无权限");
        }
        return R.success("删除成功");
    }

    @Override
    public R getInteractionStats(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("commentUnread", countUnread(userId, 6));
        result.put("likeUnread", countUnread(userId, 5));
        result.put("collectUnread", countUnread(userId, 7));
        result.put("commentTotal", countTotal(userId, 6));
        result.put("likeTotal", countTotal(userId, 5));
        result.put("collectTotal", countTotal(userId, 7));
        return R.success(result);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseDto(Object dto) {
        if (dto instanceof Map) {
            return (Map<String, Object>) dto;
        }
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

    private String getStringParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

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

    private String getUserNickname(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            return user != null && user.getNickname() != null ? user.getNickname() : "用户" + userId;
        } catch (Exception e) {
            log.warn("获取用户昵称失败, userId={}", userId, e);
            return "用户" + userId;
        }
    }

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

            String readableType = readableTargetType(targetType);
            String title = actorNickname + typeDesc + "您的" + readableType;
            String content = actorNickname + typeDesc + "您的" + readableType + "(ID:" + targetId + ")";

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

    private String readableTargetType(String targetType) {
        if ("note".equalsIgnoreCase(targetType)) {
            return "笔记";
        }
        if ("collection".equalsIgnoreCase(targetType)) {
            return "收藏集";
        }
        if ("collection_item".equalsIgnoreCase(targetType) || "item".equalsIgnoreCase(targetType)) {
            return "收藏项";
        }
        return targetType;
    }

    private Integer resolveNotifyType(String type) {
        if ("like".equalsIgnoreCase(type)) {
            return 5;
        }
        if ("comment".equalsIgnoreCase(type)) {
            return 6;
        }
        if ("collect".equalsIgnoreCase(type)) {
            return 7;
        }
        return null;
    }

    private Map<String, Object> convertInteractionNotification(Notification notification, String type) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", notification.getId());
        item.put("interactionType", type);
        item.put("isRead", notification.getIsRead());
        item.put("createdAt", notification.getCreatedAt());
        item.put("summary", notification.getContent());
        item.put("targetId", parseTargetId(notification.getContent()));
        item.put("targetType", parseTargetType(notification.getTitle()));
        item.put("targetUrl", buildTargetUrl(parseTargetType(notification.getTitle()), parseTargetId(notification.getContent())));
        item.put("targetTitle", loadTargetTitle(parseTargetType(notification.getTitle()), parseTargetId(notification.getContent())));
        item.put("actorName", parseActorName(notification.getTitle()));
        item.put("actorAvatar", null);
        item.put("actorId", null);
        return item;
    }

    private String parseActorName(String title) {
        if (!StringUtils.hasText(title)) {
            return "用户";
        }
        int index = title.indexOf("点赞了");
        if (index < 0) index = title.indexOf("评论了");
        if (index < 0) index = title.indexOf("收藏了");
        if (index < 0) {
            return title;
        }
        return title.substring(0, index);
    }

    private Long parseTargetId(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        int start = content.indexOf("(ID:");
        int end = content.indexOf(")", start);
        if (start < 0 || end < 0) {
            return null;
        }
        try {
            return Long.parseLong(content.substring(start + 4, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseTargetType(String title) {
        if (!StringUtils.hasText(title)) {
            return null;
        }
        if (title.contains("笔记")) {
            return "note";
        }
        if (title.contains("收藏集")) {
            return "collection";
        }
        if (title.contains("收藏项")) {
            return "collection_item";
        }
        return null;
    }

    private String buildTargetUrl(String targetType, Long targetId) {
        if (targetId == null || targetType == null) {
            return null;
        }
        if ("note".equals(targetType)) {
            return "/creation/notes/" + targetId + "#comments";
        }
        if ("collection".equals(targetType)) {
            return "/collections/" + targetId;
        }
        if ("collection_item".equals(targetType)) {
            return "/collections/" + targetId + "/workspace";
        }
        return null;
    }

    private String loadTargetTitle(String targetType, Long targetId) {
        if (targetId == null || targetType == null) {
            return null;
        }
        if ("note".equals(targetType)) {
            Note note = noteMapper.selectById(targetId);
            return note != null ? note.getTitle() : null;
        }
        if ("collection".equals(targetType)) {
            Collection collection = collectionMapper.selectById(targetId);
            return collection != null ? collection.getName() : null;
        }
        if ("collection_item".equals(targetType)) {
            CollectionItem item = collectionItemMapper.selectById(targetId);
            return item != null ? item.getTitle() : null;
        }
        return null;
    }

    private Long countUnread(Long userId, Integer notifyType) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getNotifyType, notifyType)
                .eq(Notification::getIsRead, 0));
    }

    private Long countTotal(Long userId, Integer notifyType) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getNotifyType, notifyType));
    }

    private void incrementNoteLikes(Long noteId) {
        try {
            Note note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记[{}]不存在，无法更新热度", noteId);
                return;
            }
            Integer likes = note.getLikes() == null ? 0 : note.getLikes();
            note.setLikes(likes + 1);
            noteMapper.updateById(note);
        } catch (Exception e) {
            log.warn("更新笔记点赞数失败, noteId={}", noteId, e);
        }
    }

    private void decrementNoteLikes(Long noteId) {
        try {
            Note note = noteMapper.selectById(noteId);
            if (note == null) {
                log.warn("笔记[{}]不存在，无法更新热度", noteId);
                return;
            }
            Integer likes = note.getLikes() == null ? 0 : note.getLikes();
            note.setLikes(Math.max(likes - 1, 0));
            noteMapper.updateById(note);
        } catch (Exception e) {
            log.warn("更新笔记点赞数失败, noteId={}", noteId, e);
        }
    }
}
