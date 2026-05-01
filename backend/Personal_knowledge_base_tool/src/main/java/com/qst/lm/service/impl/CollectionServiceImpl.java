package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.collection.CollectionDTO;
import com.qst.lm.dto.collection.CollectionQueryDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionMapper;
import com.qst.lm.pojo.Collection;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.service.ICollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏集服务实现类
 */
@Slf4j
@Service
public class CollectionServiceImpl implements ICollectionService {

    private final CollectionMapper collectionMapper;
    private final CollectionItemMapper collectionItemMapper;

    public CollectionServiceImpl(CollectionMapper collectionMapper, CollectionItemMapper collectionItemMapper) {
        this.collectionMapper = collectionMapper;
        this.collectionItemMapper = collectionItemMapper;
    }

    @Override
    public R createCollection(Long userId, CollectionDTO dto) {
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setName(dto.getName());
        collection.setDescription(dto.getDescription());
        collection.setCoverImage(dto.getCoverImage());
        collection.setIsPublic(dto.getIsPublic());
        collectionMapper.insert(collection);
        log.info("用户[{}]创建收藏集[{}]成功", userId, collection.getId());
        return R.success("创建收藏集成功", collection);
    }

    @Override
    public R updateCollection(Long userId, Long id, CollectionDTO dto) {
        getAndCheckOwnership(userId, id);

        LambdaUpdateWrapper<Collection> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Collection::getId, id)
                .eq(Collection::getUserId, userId)
                .eq(Collection::getDeleted, 0)
                .set(Collection::getName, dto.getName())
                .set(Collection::getDescription, dto.getDescription())
                .set(Collection::getCoverImage, dto.getCoverImage())
                .set(Collection::getIsPublic, dto.getIsPublic());
        collectionMapper.update(null, updateWrapper);
        log.info("用户[{}]更新收藏集[{}]成功", userId, id);
        return R.success("更新收藏集成功");
    }

    @Override
    public R deleteCollection(Long userId, Long id) {
        getAndCheckOwnership(userId, id);
        collectionMapper.updateDeletedById(id);
        log.info("用户[{}]删除收藏集[{}]成功", userId, id);
        return R.success("删除收藏集成功");
    }

    @Override
    public R getCollectionList(Long userId, CollectionQueryDTO query) {
        Page<Collection> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getUserId, userId)
                .eq(Collection::getDeleted, 0);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Collection::getName, query.getKeyword())
                    .or()
                    .like(Collection::getDescription, query.getKeyword()));
        }
        if (query.getIsPublic() != null) {
            wrapper.eq(Collection::getIsPublic, query.getIsPublic());
        }
        wrapper.orderByDesc(Collection::getCreatedAt);

        Page<Collection> result = collectionMapper.selectPage(page, wrapper);
        return R.success(result);
    }

    @Override
    public R getCollectionDetail(Long userId, Long id) {
        Collection collection = getAndCheckOwnership(userId, id);
        return R.success(collection);
    }

    @Override
    public R getPublicCollections(CollectionQueryDTO query) {
        Page<Collection> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getIsPublic, 1)
                .eq(Collection::getDeleted, 0);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Collection::getName, query.getKeyword())
                    .or()
                    .like(Collection::getDescription, query.getKeyword()));
        }
        wrapper.orderByDesc(Collection::getCreatedAt);

        Page<Collection> result = collectionMapper.selectPage(page, wrapper);
        return R.success(result);
    }

    /**
     * 校验数据归属并返回收藏集
     *
     * @param userId 当前用户ID
     * @param id     收藏集ID
     * @return 收藏集实体
     */
    private Collection getAndCheckOwnership(Long userId, Long id) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getId, id)
                .eq(Collection::getDeleted, 0);
        Collection collection = collectionMapper.selectOne(wrapper);
        if (collection == null) {
            throw new BusinessException("收藏集不存在");
        }
        if (!collection.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该收藏集");
        }
        return collection;
    }

    @Override
    public R getRecommended(Long userId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        // 1. 查询公开收藏集
        Page<Collection> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getIsPublic, 1)
                .eq(Collection::getDeleted, 0);
        
        // 如果userId不为null，则排除用户自己的收藏集
        if (userId != null) {
            wrapper.ne(Collection::getUserId, userId);
        }
        
        wrapper.orderByDesc(Collection::getCreatedAt);

        Page<Collection> result = collectionMapper.selectPage(pageObj, wrapper);
        
        // 2. 批量统计每个收藏集的收藏项数量（仅统计未删除的）
        if (!result.getRecords().isEmpty()) {
            // 提取所有收藏集ID
            List<Long> collectionIds = result.getRecords().stream()
                    .map(Collection::getId)
                    .collect(Collectors.toList());
            
            // 批量查询每个收藏集的收藏项数量
            LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(CollectionItem::getCollectionId, collectionIds)
                       .eq(CollectionItem::getDeleted, 0)
                       .select(CollectionItem::getCollectionId);
            
            List<CollectionItem> items = collectionItemMapper.selectList(itemWrapper);
            
            // 统计每个收藏集的数量
            Map<Long, Long> itemCountMap = items.stream()
                    .collect(Collectors.groupingBy(CollectionItem::getCollectionId, Collectors.counting()));
            
            // 填充itemCount到收藏集对象
            for (Collection collection : result.getRecords()) {
                collection.setItemCount(itemCountMap.getOrDefault(collection.getId(), 0L).intValue());
            }
        }
        
        return R.success(result);
    }

    @Override
    public R getCollectionItems(Long userId, Long collectionId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        getAndCheckOwnership(userId, collectionId);

        Page<CollectionItem> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getCollectionId, collectionId)
                .eq(CollectionItem::getDeleted, 0)
                .orderByDesc(CollectionItem::getCreatedAt);

        Page<CollectionItem> result = collectionItemMapper.selectPage(pageObj, wrapper);
        return R.success(result);
    }
}
