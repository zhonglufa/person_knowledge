package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qst.lm.common.R;
import com.qst.lm.dto.tag.TagDTO;
import com.qst.lm.dto.tag.TagMergeDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionItemTagMapper;
import com.qst.lm.mapper.TagMapper;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.pojo.CollectionItemTag;
import com.qst.lm.pojo.Tag;
import com.qst.lm.service.ITagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签管理服务实现类
 */
@Slf4j
@Service
public class TagServiceImpl implements ITagService {

    private final TagMapper tagMapper;
    private final CollectionItemTagMapper collectionItemTagMapper;
    private final CollectionItemMapper collectionItemMapper;

    public TagServiceImpl(TagMapper tagMapper,
                          CollectionItemTagMapper collectionItemTagMapper,
                          CollectionItemMapper collectionItemMapper) {
        this.tagMapper = tagMapper;
        this.collectionItemTagMapper = collectionItemTagMapper;
        this.collectionItemMapper = collectionItemMapper;
    }

    @Override
    public R createTag(Long userId, TagDTO dto) {
        // 校验标签名唯一（用户范围内）
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId).eq(Tag::getName, dto.getName());
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
        tagMapper.insert(tag);

        log.info("用户[{}]创建标签[{}]成功", userId, tag.getId());
        return R.success("创建标签成功", tag);
    }

    @Override
    public R updateTag(Long userId, Long id, TagDTO dto) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此标签");
        }

        // 校验标签名唯一（用户范围内，排除自身）
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getName, dto.getName())
                .ne(Tag::getId, id);
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setName(dto.getName());
        tag.setColor(dto.getColor());
        tagMapper.updateById(tag);

        log.info("用户[{}]更新标签[{}]成功", userId, id);
        return R.success("更新标签成功", tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteTag(Long userId, Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此标签");
        }

        // 删除标签与收藏项的关联
        collectionItemTagMapper.deleteByTagId(id);

        // 删除标签
        tagMapper.deleteById(id);

        log.info("用户[{}]删除标签[{}]成功", userId, id);
        return R.success("删除标签成功");
    }

    @Override
    public R getTagList(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getId);
        List<Tag> tags = tagMapper.selectList(wrapper);
        return R.success(tags);
    }

    @Override
    public R getUserTags(Long userId) {
        // 通过collection_item_tag关联查询用户使用过的标签
        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(wrapper);

        List<Long> tagIds = relations.stream()
                .map(CollectionItemTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return R.success(List.of());
        }

        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return R.success(tags);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R bindTagToItem(Long userId, Long itemId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new BusinessException("标签ID列表不能为空");
        }

        CollectionItem item = getAndCheckItemOwnership(userId, itemId);

        for (Long tagId : tagIds) {
            Tag tag = tagMapper.selectById(tagId);
            if (tag == null || tag.getDeleted() != null && tag.getDeleted() == 1) {
                throw new BusinessException("标签[" + tagId + "]不存在");
            }
            if (!tag.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权操作标签[" + tagId + "]");
            }

            LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CollectionItemTag::getUserId, userId)
                    .eq(CollectionItemTag::getCollectionItemId, itemId)
                    .eq(CollectionItemTag::getTagId, tagId);
            Long count = collectionItemTagMapper.selectCount(wrapper);
            if (count > 0) {
                continue;
            }

            CollectionItemTag relation = new CollectionItemTag();
            relation.setUserId(userId);
            relation.setCollectionItemId(itemId);
            relation.setTagId(tagId);
            collectionItemTagMapper.insert(relation);
        }

        log.info("用户[{}]为收藏项[{}]绑定标签成功", userId, item.getId());
        return R.success("绑定标签成功");
    }

    @Override
    public R unbindTagFromItem(Long userId, Long itemId, Long tagId) {
        CollectionItem item = getAndCheckItemOwnership(userId, itemId);

        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || tag.getDeleted() != null && tag.getDeleted() == 1) {
            throw new BusinessException("标签不存在");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此标签");
        }

        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId)
                .eq(CollectionItemTag::getCollectionItemId, itemId)
                .eq(CollectionItemTag::getTagId, tagId);
        int deleted = collectionItemTagMapper.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException("未找到该标签绑定关系");
        }

        log.info("用户[{}]为收藏项[{}]解绑标签[{}]成功", userId, item.getId(), tagId);
        return R.success("解绑标签成功");
    }

    @Override
    public R getItemTags(Long userId, Long itemId) {
        CollectionItem item = getAndCheckItemOwnership(userId, itemId);

        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId)
                .eq(CollectionItemTag::getCollectionItemId, itemId);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(wrapper);

        List<Long> tagIds = relations.stream()
                .map(CollectionItemTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return R.success(List.of());
        }

        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getDeleted, 0)
                .in(Tag::getId, tagIds);
        List<Tag> tags = tagMapper.selectList(tagWrapper);

        log.info("用户[{}]获取收藏项[{}]标签成功", userId, item.getId());
        return R.success(tags);
    }

    private CollectionItem getAndCheckItemOwnership(Long userId, Long itemId) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, itemId)
                .eq(CollectionItem::getDeleted, 0);
        CollectionItem item = collectionItemMapper.selectOne(wrapper);
        if (item == null) {
            throw new BusinessException("收藏项不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该收藏项");
        }
        return item;
    }

    @Override
    public R getStatistics(Long userId) {
        // 获取用户使用过的标签
        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(wrapper);

        Map<String, Object> statistics = new HashMap<>(4);
        statistics.put("totalTags", relations.stream()
                .map(CollectionItemTag::getTagId)
                .distinct()
                .count());

        // 统计每个标签的使用次数
        Map<Long, Long> tagUsageCount = relations.stream()
                .collect(Collectors.groupingBy(CollectionItemTag::getTagId, Collectors.counting()));

        statistics.put("tagUsageCount", tagUsageCount);
        return R.success(statistics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchOperate(Long userId, List<Long> tagIds, String operation) {
        if (CollectionUtils.isEmpty(tagIds)) {
            throw new BusinessException("标签ID列表不能为空");
        }

        if ("delete".equalsIgnoreCase(operation)) {
            for (Long tagId : tagIds) {
                Tag tag = tagMapper.selectById(tagId);
                if (tag != null) {
                    if (!tag.getUserId().equals(userId)) {
                        throw new BusinessException(403, "无权操作标签[" + tagId + "]");
                    }
                    collectionItemTagMapper.deleteByTagId(tagId);
                    tagMapper.deleteById(tagId);
                }
            }
            log.info("用户[{}]批量删除标签,共{}个", userId, tagIds.size());
            return R.success("批量删除成功");
        } else {
            throw new BusinessException("不支持的操作类型: " + operation);
        }
    }

    @Override
    public R getPopular(Long userId, Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        // 获取用户使用过的标签及其使用次数
        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(wrapper);

        Map<Long, Long> tagUsageCount = relations.stream()
                .collect(Collectors.groupingBy(CollectionItemTag::getTagId, Collectors.counting()));

        // 按使用次数排序,取前limit个
        List<Long> topTagIds = tagUsageCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (topTagIds.isEmpty()) {
            return R.success(List.of());
        }

        List<Tag> tags = tagMapper.selectBatchIds(topTagIds);
        // 附加使用次数
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tag tag : tags) {
            Map<String, Object> item = new HashMap<>(4);
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("color", tag.getColor());
            item.put("usageCount", tagUsageCount.get(tag.getId()));
            result.add(item);
        }

        return R.success(result);
    }

    @Override
    public R getCloud(Long userId) {
        // 获取用户使用过的标签及其使用次数
        LambdaQueryWrapper<CollectionItemTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItemTag::getUserId, userId);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(wrapper);

        Map<Long, Long> tagUsageCount = relations.stream()
                .collect(Collectors.groupingBy(CollectionItemTag::getTagId, Collectors.counting()));

        if (tagUsageCount.isEmpty()) {
            return R.success(List.of());
        }

        List<Long> tagIds = new ArrayList<>(tagUsageCount.keySet());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);

        // 构建标签云数据
        List<Map<String, Object>> cloud = new ArrayList<>();
        long maxCount = tagUsageCount.values().stream().max(Long::compareTo).orElse(1L);

        for (Tag tag : tags) {
            Map<String, Object> item = new HashMap<>(8);
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            item.put("color", tag.getColor());
            Long count = tagUsageCount.get(tag.getId());
            item.put("count", count);
            // 计算权重(0-1之间)
            item.put("weight", count != null ? (double) count / maxCount : 0);
            cloud.add(item);
        }

        return R.success(cloud);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R mergeTags(Long userId, TagMergeDTO dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }

        List<Long> sourceTagIds = dto.getSourceTagIds();
        Long targetTagId = dto.getTargetTagId();

        if (CollectionUtils.isEmpty(sourceTagIds) || targetTagId == null) {
            throw new BusinessException("源标签ID和目标标签ID不能为空");
        }

        // 校验目标标签存在
        Tag targetTag = tagMapper.selectById(targetTagId);
        if (targetTag == null) {
            throw new BusinessException("目标标签不存在");
        }

        int mergedCount = 0;
        for (Long sourceTagId : sourceTagIds) {
            if (sourceTagId.equals(targetTagId)) {
                continue;
            }

            // 校验源标签存在
            Tag sourceTag = tagMapper.selectById(sourceTagId);
            if (sourceTag == null) {
                continue;
            }

            // 将源标签关联的收藏项重新关联到目标标签
            LambdaQueryWrapper<CollectionItemTag> sourceWrapper = new LambdaQueryWrapper<>();
            sourceWrapper.eq(CollectionItemTag::getUserId, userId)
                    .eq(CollectionItemTag::getTagId, sourceTagId);
            List<CollectionItemTag> sourceRelations = collectionItemTagMapper.selectList(sourceWrapper);

            for (CollectionItemTag relation : sourceRelations) {
                // 检查是否已经绑定了目标标签
                LambdaQueryWrapper<CollectionItemTag> existWrapper = new LambdaQueryWrapper<>();
                existWrapper.eq(CollectionItemTag::getUserId, userId)
                        .eq(CollectionItemTag::getCollectionItemId, relation.getCollectionItemId())
                        .eq(CollectionItemTag::getTagId, targetTagId);
                Long existCount = collectionItemTagMapper.selectCount(existWrapper);

                if (existCount == 0) {
                    // 创建新的关联记录
                    CollectionItemTag newRelation = new CollectionItemTag();
                    newRelation.setUserId(userId);
                    newRelation.setCollectionItemId(relation.getCollectionItemId());
                    newRelation.setTagId(targetTagId);
                    collectionItemTagMapper.insert(newRelation);
                }
            }

            // 删除源标签的所有关联
            collectionItemTagMapper.deleteByTagId(sourceTagId);

            // 删除源标签
            tagMapper.deleteById(sourceTagId);
            mergedCount++;
        }

        log.info("用户[{}]合并{}个标签到标签[{}]", userId, mergedCount, targetTag.getName());
        return R.success("标签合并成功,共合并" + mergedCount + "个标签");
    }
}
