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
import com.qst.lm.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectionServiceImpl implements ICollectionService {

    private final CollectionMapper collectionMapper;
    private final CollectionItemMapper collectionItemMapper;
    private final OssUtil ossUtil;

    public CollectionServiceImpl(CollectionMapper collectionMapper, CollectionItemMapper collectionItemMapper, OssUtil ossUtil) {
        this.collectionMapper = collectionMapper;
        this.collectionItemMapper = collectionItemMapper;
        this.ossUtil = ossUtil;
    }

    @Override
    public R createCollection(Long userId, CollectionDTO dto) {
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setName(dto.getName());
        collection.setDescription(dto.getDescription());
        collection.setCoverImage(dto.getCoverImage());
        collection.setIsPublic(booleanToInteger(dto.getIsPublic()));
        collectionMapper.insert(collection);
        log.info("用户[{}]创建收藏集[{}]成功", userId, collection.getId());
        return R.success("创建收藏集成功", collection);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateCollection(Long userId, Long id, CollectionDTO dto) {
        Collection collection = getAndCheckOwnership(userId, id);

        Integer isPublicInt = booleanToInteger(dto.getIsPublic());
        LambdaUpdateWrapper<Collection> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Collection::getId, id)
                .eq(Collection::getUserId, userId)
                .eq(Collection::getDeleted, 0)
                .set(Collection::getName, dto.getName())
                .set(Collection::getDescription, dto.getDescription())
                .set(Collection::getCoverImage, dto.getCoverImage())
                .set(Collection::getIsPublic, isPublicInt);
        collectionMapper.update(null, updateWrapper);

        if (!java.util.Objects.equals(collection.getIsPublic(), isPublicInt)) {
            LambdaUpdateWrapper<CollectionItem> itemUpdateWrapper = new LambdaUpdateWrapper<>();
            itemUpdateWrapper.eq(CollectionItem::getCollectionId, id)
                    .eq(CollectionItem::getDeleted, 0)
                    .set(CollectionItem::getIsPublic, isPublicInt);
            int updatedCount = collectionItemMapper.update(null, itemUpdateWrapper);
            log.info("用户[{}]更新收藏集[{}]公开状态，同步更新{}个收藏项", userId, id, updatedCount);
        } else {
            log.info("用户[{}]更新收藏集[{}]成功", userId, id);
        }

        return R.success("更新收藏集成功");
    }

    @Override
    public R deleteCollection(Long userId, Long id) {
        getAndCheckOwnership(userId, id);
        collectionItemMapper.clearCollectionReference(id);
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

        Page<Collection> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getIsPublic, 1)
                .eq(Collection::getDeleted, 0);
        
        if (userId != null) {
            wrapper.ne(Collection::getUserId, userId);
        }
        
        wrapper.orderByDesc(Collection::getCreatedAt);

        Page<Collection> result = collectionMapper.selectPage(pageObj, wrapper);
        
        if (!result.getRecords().isEmpty()) {
            List<Long> collectionIds = result.getRecords().stream()
                    .map(Collection::getId)
                    .collect(Collectors.toList());
            
            LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(CollectionItem::getCollectionId, collectionIds)
                       .eq(CollectionItem::getDeleted, 0)
                       .select(CollectionItem::getCollectionId);
            
            List<CollectionItem> items = collectionItemMapper.selectList(itemWrapper);
            
            Map<Long, Long> itemCountMap = items.stream()
                    .collect(Collectors.groupingBy(CollectionItem::getCollectionId, Collectors.counting()));
            
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

    @Override
    public R uploadCoverImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择封面图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("封面图片文件大小不能超过5MB");
        }
        try {
            String coverUrl = ossUtil.uploadFile(file, true);
            log.info("用户[{}]上传收藏集封面图片成功: {}", userId, coverUrl);
            return R.success("封面图片上传成功", Map.of("url", coverUrl));
        } catch (Exception e) {
            log.error("用户[{}]上传收藏集封面图片失败", userId, e);
            throw new BusinessException("封面图片上传失败，请稍后重试");
        }
    }

    private Integer booleanToInteger(Boolean value) {
        if (value == null) {
            return 0;
        }
        return value ? 1 : 0;
    }
}
