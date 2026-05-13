package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.note.NoteDTO;
import com.qst.lm.dto.note.NoteQueryDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.NoteCollectRelationMapper;
import com.qst.lm.mapper.NoteMapper;
import com.qst.lm.pojo.Note;
import com.qst.lm.pojo.UserNoteCollect;
import com.qst.lm.service.INoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记管理服务实现类
 */
@Slf4j
@Service
public class NoteServiceImpl implements INoteService {

    private final NoteMapper noteMapper;
    private final NoteCollectRelationMapper noteCollectRelationMapper;
    private final com.qst.lm.mapper.CollectionItemMapper collectionItemMapper;

    public NoteServiceImpl(NoteMapper noteMapper, 
                          NoteCollectRelationMapper noteCollectRelationMapper,
                          com.qst.lm.mapper.CollectionItemMapper collectionItemMapper) {
        this.noteMapper = noteMapper;
        this.noteCollectRelationMapper = noteCollectRelationMapper;
        this.collectionItemMapper = collectionItemMapper;
    }

    private void validatePublicDraftRule(String status, Integer isPublic) {
        if ("草稿".equals(status) && Integer.valueOf(1).equals(isPublic)) {
            throw new BusinessException("草稿状态仅允许私密，需先发布才可公开");
        }
    }

    private boolean isVisiblePublicNote(Note note) {
        return note != null
                && Integer.valueOf(1).equals(note.getIsPublic())
                && "完成".equals(note.getStatus());
    }

    @Override
    public R createNote(Long userId, NoteDTO dto) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setNoteType(dto.getNoteType() != null ? dto.getNoteType() : "conceptual");
        note.setDescription(dto.getDescription());
        note.setCoverImage(dto.getCoverImage());
        note.setCollectionItemId(dto.getCollectionItemId());
        note.setIsPublic(dto.getIsPublic() != null ? (dto.getIsPublic() ? 1 : 0) : 0);
        // 如果status为null，默认设为"草稿"
        note.setStatus(dto.getStatus() != null ? dto.getStatus() : "草稿");
        validatePublicDraftRule(note.getStatus(), note.getIsPublic());

        noteMapper.insert(note);

        log.info("用户[{}]创建笔记[{}]成功", userId, note.getId());
        return R.success("创建笔记成功", note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateNote(Long userId, Long id, NoteDTO dto) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException("笔记不存在");
        }

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        if (dto.getNoteType() != null) {
            note.setNoteType(dto.getNoteType());
        }
        note.setDescription(dto.getDescription());
        note.setCoverImage(dto.getCoverImage());
        note.setCollectionItemId(dto.getCollectionItemId());
        if (dto.getIsPublic() != null) {
            note.setIsPublic(dto.getIsPublic() ? 1 : 0);
        }
        if (dto.getStatus() != null) {
            note.setStatus(dto.getStatus());
        }
        validatePublicDraftRule(note.getStatus(), note.getIsPublic());

        noteMapper.updateById(note);

        if (note.getCollectionItemId() != null && "完成".equals(note.getStatus())) {
            try {
                updateCollectionItemStatistics(note.getCollectionItemId());
            } catch (Exception e) {
                log.error("更新收藏项统计失败", e);
            }
        }

        log.info("用户[{}]更新笔记[{}]成功", userId, id);
        return R.success("更新笔记成功", note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteNote(Long userId, Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException("笔记不存在");
        }

        Long collectionItemId = note.getCollectionItemId();

        noteMapper.deleteById(id);

        if (collectionItemId != null) {
            try {
                updateCollectionItemStatistics(collectionItemId);
                log.info("笔记[{}]删除成功，已触发收藏项[{}]统计更新", id, collectionItemId);
            } catch (Exception e) {
                log.error("更新收藏项统计失败", e);
            }
        }

        log.info("用户[{}]删除笔记[{}]成功", userId, id);
        return R.success("删除笔记成功");
    }

    @Override
    public R getNoteList(Long userId, NoteQueryDTO query) {
        Page<Note> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);

        if (StringUtils.hasText(query.getNoteType())) {
            wrapper.eq(Note::getNoteType, query.getNoteType());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Note::getStatus, query.getStatus());
        }
        if (query.getIsPublic() != null) {
            wrapper.eq(Note::getIsPublic, query.getIsPublic() ? 1 : 0);
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Note::getTitle, query.getKeyword())
                    .or()
                    .like(Note::getContent, query.getKeyword()));
        }

        wrapper.orderByDesc(Note::getCreateTime);
        Page<Note> result = noteMapper.selectPage(page, wrapper);
        return R.success(result);
    }

    @Override
    public R getNoteDetail(Long userId, Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        // 如果不是自己的笔记，需要是公开完成状态才能查看
        if (!note.getUserId().equals(userId) && !isVisiblePublicNote(note)) {
            throw new BusinessException("笔记不存在");
        }
        
        // 更新浏览量和热度评分
        incrementNoteViews(note);
        
        return R.success(note);
    }

    @Override
    public R getPublicNoteDetail(Long id) {
        Note note = noteMapper.selectById(id);
        if (!isVisiblePublicNote(note)) {
            throw new BusinessException("笔记不存在");
        }

        incrementNoteViews(note);
        return R.success(note);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R publishNote(Long userId, Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new BusinessException("笔记不存在");
        }

        note.setStatus("完成");
        note.setFinishedAt(LocalDateTime.now());
        
        int qualityScore = calculateNoteQuality(note);
        note.setNoteQualityScore(qualityScore);
        
        noteMapper.updateById(note);

        if (note.getCollectionItemId() != null) {
            updateCollectionItemAfterNotePublish(note.getCollectionItemId(), qualityScore);
        }

        log.info("用户[{}]发布笔记[{}]成功，质量评分[{}]", userId, id, qualityScore);
        return R.success("发布笔记成功", note);
    }

    @Override
    public R saveDraft(Long userId, NoteDTO dto) {
        Note note = null;
        boolean isUpdate = false;

        if (dto.getCollectionItemId() != null) {
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getUserId, userId)
                    .eq(Note::getCollectionItemId, dto.getCollectionItemId())
                    .eq(Note::getStatus, "草稿")
                    .orderByDesc(Note::getUpdateTime)
                    .last("LIMIT 1");
            note = noteMapper.selectOne(wrapper);
            
            if (note != null) {
                isUpdate = true;
            }
        }

        if (note == null) {
            note = new Note();
            note.setUserId(userId);
            note.setStatus("草稿");
        }

        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setNoteType(dto.getNoteType() != null ? dto.getNoteType() : "conceptual");
        note.setDescription(dto.getDescription());
        note.setCoverImage(dto.getCoverImage());
        note.setCollectionItemId(dto.getCollectionItemId());
        note.setIsPublic(dto.getIsPublic() != null ? (dto.getIsPublic() ? 1 : 0) : 0);
        
        validatePublicDraftRule(note.getStatus(), note.getIsPublic());

        if (isUpdate) {
            noteMapper.updateById(note);
            log.info("用户[{}]更新草稿[{}]成功", userId, note.getId());
        } else {
            noteMapper.insert(note);
            log.info("用户[{}]创建草稿[{}]成功", userId, note.getId());
        }

        return R.success("保存草稿成功", note);
    }

    @Override
    public R getPublicNotes(NoteQueryDTO query) {
        Page<Note> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getIsPublic, 1)
                .eq(Note::getStatus, "完成");

        if (StringUtils.hasText(query.getNoteType())) {
            wrapper.eq(Note::getNoteType, query.getNoteType());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Note::getTitle, query.getKeyword())
                    .or()
                    .like(Note::getContent, query.getKeyword()));
        }

        wrapper.orderByDesc(Note::getCreateTime);
        Page<Note> result = noteMapper.selectPage(page, wrapper);
        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R collectNote(Long userId, Long noteId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!isVisiblePublicNote(note)) {
            throw new BusinessException("该笔记未公开发布，无法收藏");
        }

        // 校验是否已收藏
        LambdaQueryWrapper<UserNoteCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteCollect::getUserId, userId)
                .eq(UserNoteCollect::getNoteId, noteId);
        Long count = noteCollectRelationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("已收藏该笔记");
        }

        UserNoteCollect collect = new UserNoteCollect();
        collect.setUserId(userId);
        collect.setNoteId(noteId);
        noteCollectRelationMapper.insert(collect);

        log.info("用户[{}]收藏笔记[{}]成功", userId, noteId);
        return R.success("收藏笔记成功");
    }

    @Override
    public R uncollectNote(Long userId, Long noteId) {
        LambdaQueryWrapper<UserNoteCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNoteCollect::getUserId, userId)
                .eq(UserNoteCollect::getNoteId, noteId);
        int deleted = noteCollectRelationMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException("未收藏该笔记");
        }

        log.info("用户[{}]取消收藏笔记[{}]成功", userId, noteId);
        return R.success("取消收藏成功");
    }

    @Override
    public R getCollectedNotes(Long userId, PageDTO page) {
        // 查询用户收藏的笔记ID
        LambdaQueryWrapper<UserNoteCollect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(UserNoteCollect::getUserId, userId)
                .orderByDesc(UserNoteCollect::getCreatedAt);
        List<UserNoteCollect> collects = noteCollectRelationMapper.selectList(collectWrapper);

        List<Long> noteIds = collects.stream()
                .map(UserNoteCollect::getNoteId)
                .collect(Collectors.toList());

        if (noteIds.isEmpty()) {
            Page<Note> emptyPage = new Page<>(page.getPageNum(), page.getPageSize());
            emptyPage.setTotal(0);
            return R.success(emptyPage);
        }

        // 分页查询笔记详情
        Page<Note> notePage = new Page<>(page.getPageNum(), page.getPageSize());
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                .eq(Note::getIsPublic, 1)
                .orderByDesc(Note::getCreateTime);
        Page<Note> result = noteMapper.selectPage(notePage, noteWrapper);
        return R.success(result);
    }

    @Override
    public R getDraftList(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        Page<Note> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getStatus, "草稿")
                .orderByDesc(Note::getUpdateTime);

        Page<Note> result = noteMapper.selectPage(pageObj, wrapper);
        return R.success(result);
    }

    @Override
    public R getNoteTemplate(String type) {
        if (!StringUtils.hasText(type)) {
            type = "conceptual";
        }

        Map<String, Object> template = new HashMap<>(8);
        template.put("type", type);

        switch (type) {
            case "conceptual":
                template.put("title", "概念笔记模板");
                template.put("content", "# 概念名称\n\n## 定义\n\n## 核心要点\n\n1. 要点一\n2. 要点二\n3. 要点三\n\n## 示例\n\n## 相关概念\n\n## 总结");
                template.put("description", "用于记录概念性知识的笔记模板");
                break;
            case "tutorial":
                template.put("title", "教程笔记模板");
                template.put("content", "# 教程标题\n\n## 前置知识\n\n## 步骤\n\n### 步骤一\n\n### 步骤二\n\n### 步骤三\n\n## 注意事项\n\n## 常见问题\n\n## 参考资料");
                template.put("description", "用于记录教程和步骤的笔记模板");
                break;
            case "review":
                template.put("title", "复习笔记模板");
                template.put("content", "# 复习主题\n\n## 知识框架\n\n## 重点回顾\n\n### 重点一\n\n### 重点二\n\n## 易错点\n\n## 练习题\n\n## 复习总结");
                template.put("description", "用于复习和总结的笔记模板");
                break;
            case "daily":
                template.put("title", "每日笔记模板");
                template.put("content", "# 日期: YYYY-MM-DD\n\n## 今日学习\n\n## 收获\n\n## 问题与思考\n\n## 明日计划");
                template.put("description", "用于每日学习记录的笔记模板");
                break;
            default:
                template.put("title", "通用笔记模板");
                template.put("content", "# 标题\n\n## 正文\n\n## 总结");
                template.put("description", "通用笔记模板");
                break;
        }

        return R.success(template);
    }

    @Override
    public R getRecommendedNotes(Integer pageNum, Integer pageSize) {
        try {
            log.info("获取推荐笔记, 页码[{}], 每页[{}]", pageNum, pageSize);
            
            // 查询推荐笔记：公开+完成状态，按热度评分排序
            // 注：note表没有digest_status字段，只使用status字段筛选
            Page<Note> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getIsPublic, 1)
                   .eq(Note::getStatus, "完成")  // 使用中文"完成"，匹配数据库enum值
                   .eq(Note::getDeleted, 0)  // 显式过滤已删除数据
                   .orderByDesc(Note::getHotScore)
                   .orderByDesc(Note::getCreateTime);
            
            Page<Note> resultPage = noteMapper.selectPage(page, wrapper);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", resultPage.getRecords());
            result.put("total", resultPage.getTotal());
            result.put("pageNum", pageNum);
            result.put("pageSize", pageSize);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("获取推荐笔记失败", e);
            return R.error("获取推荐笔记失败: " + e.getMessage());
        }
    }

    /**
     * 增加笔记浏览量并更新热度评分
     * @param note 笔记对象
     */
    private void incrementNoteViews(Note note) {
        try {
            // 增加浏览量
            Integer currentViews = note.getViews() != null ? note.getViews() : 0;
            note.setViews(currentViews + 1);
            
            // 重新计算热度评分
            recalculateHotScore(note);
            
            // 更新数据库
            noteMapper.updateById(note);
            
            log.debug("笔记[{}]浏览量更新: {} -> {}, 热度评分: {}", 
                     note.getId(), currentViews, note.getViews(), note.getHotScore());
        } catch (Exception e) {
            log.error("更新笔记[{}]浏览量失败", note.getId(), e);
            // 不抛出异常，避免影响用户查看笔记
        }
    }

    /**
     * 增加笔记点赞数并更新热度评分
     * @param note 笔记对象
     */
    private void incrementNoteLikes(Note note) {
        try {
            // 增加点赞数
            Integer currentLikes = note.getLikes() != null ? note.getLikes() : 0;
            note.setLikes(currentLikes + 1);
            
            // 重新计算热度评分
            recalculateHotScore(note);
            
            // 更新数据库
            noteMapper.updateById(note);
            
            log.debug("笔记[{}]点赞数更新: {} -> {}, 热度评分: {}", 
                     note.getId(), currentLikes, note.getLikes(), note.getHotScore());
        } catch (Exception e) {
            log.error("更新笔记[{}]点赞数失败", note.getId(), e);
            // 不抛出异常，避免影响用户操作
        }
    }

    /**
     * 重新计算笔记热度评分
     * 公式：hotScore = views * 0.6 + likes * 0.4
     * @param note 笔记对象
     */
    private void recalculateHotScore(Note note) {
        Integer views = note.getViews() != null ? note.getViews() : 0;
        Integer likes = note.getLikes() != null ? note.getLikes() : 0;
        
        double hotScore = (views * 0.6) + (likes * 0.4);
        note.setHotScore(hotScore);
    }

    private int calculateNoteQuality(Note note) {
        int score = 0;
        String content = note.getContent() != null ? note.getContent() : "";
        
        if (content.length() > 500) score += 20;
        if (content.length() > 1000) score += 20;
        
        if (content.contains("##") || content.contains("# ")) score += 15;
        
        if (note.getDescription() != null && !note.getDescription().isEmpty()) score += 15;
        
        if (note.getCoverImage() != null && !note.getCoverImage().isEmpty()) score += 10;
        
        score += 20;
        
        return Math.min(score, 100);
    }

    private void updateCollectionItemAfterNotePublish(Long collectionItemId, int qualityScore) {
        try {
            com.qst.lm.pojo.CollectionItem item = collectionItemMapper.selectById(collectionItemId);
            if (item == null) {
                log.warn("收藏项[{}]不存在，跳过闭环更新", collectionItemId);
                return;
            }
            
            Integer currentNoteCount = item.getNoteCount() != null ? item.getNoteCount() : 0;
            item.setNoteCount(currentNoteCount + 1);
            item.setLastNoteTime(LocalDateTime.now());
            item.setNoteQualityScore(qualityScore);
            
            if (qualityScore >= 60) {
                item.setDigestStatus("digested");
            } else if (qualityScore >= 30) {
                item.setDigestStatus("digesting");
            }
            
            collectionItemMapper.updateById(item);
            
            log.info("收藏项[{}]闭环更新成功: 笔记数[{}], 质量评分[{}], 消化状态[{}]", 
                    collectionItemId, item.getNoteCount(), qualityScore, item.getDigestStatus());
        } catch (Exception e) {
            log.error("更新收藏项[{}]闭环信息失败", collectionItemId, e);
        }
    }

    private void updateCollectionItemStatistics(Long collectionItemId) {
        try {
            com.qst.lm.pojo.CollectionItem item = collectionItemMapper.selectById(collectionItemId);
            if (item == null) {
                log.warn("收藏项[{}]不存在，跳过统计更新", collectionItemId);
                return;
            }
            
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getCollectionItemId, collectionItemId)
                   .eq(Note::getDeleted, 0);
            
            List<Note> notes = noteMapper.selectList(wrapper);
            
            int noteCount = notes.size();
            LocalDateTime lastNoteTime = notes.stream()
                .map(Note::getUpdateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            int avgQualityScore = (int) notes.stream()
                .mapToInt(this::calculateNoteQuality)
                .average()
                .orElse(0.0);
            
            boolean hasPublishedNote = notes.stream()
                .anyMatch(n -> "完成".equals(n.getStatus()));
            
            item.setNoteCount(noteCount);
            item.setLastNoteTime(lastNoteTime);
            item.setNoteQualityScore(avgQualityScore);
            
            if (hasPublishedNote && !"digested".equals(item.getDigestStatus())) {
                item.setDigestStatus("digested");
            } else if (noteCount == 0 && "digested".equals(item.getDigestStatus())) {
                item.setDigestStatus("undigest");
            }
            
            collectionItemMapper.updateById(item);
            
            log.info("更新收藏项[{}]统计: noteCount={}, lastNoteTime={}, qualityScore={}", 
                     collectionItemId, noteCount, lastNoteTime, avgQualityScore);
        } catch (Exception e) {
            log.error("更新收藏项[{}]统计失败", collectionItemId, e);
        }
    }
}
