package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.collect.BatchMoveDTO;
import com.qst.lm.dto.collect.BatchOperationDTO;
import com.qst.lm.dto.collect.BookmarkImportDTO;
import com.qst.lm.dto.collect.CollectionItemDTO;
import com.qst.lm.dto.collect.CollectionItemQueryDTO;
import com.qst.lm.dto.collect.UrlMetadataRequestDTO;
import com.qst.lm.dto.collect.UrlMetadataResponseDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionItemTagMapper;
import com.qst.lm.mapper.ImportRecordMapper;
import com.qst.lm.mapper.TagMapper;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.pojo.CollectionItemTag;
import com.qst.lm.pojo.ImportRecord;
import com.qst.lm.pojo.Tag;
import com.qst.lm.service.ICollectionItemService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectionItemServiceImpl implements ICollectionItemService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final CollectionItemMapper collectionItemMapper;
    private final CollectionItemTagMapper collectionItemTagMapper;
    private final ImportRecordMapper importRecordMapper;
    private final TagMapper tagMapper;
    private final com.qst.lm.mapper.NoteMapper noteMapper;
    private final com.qst.lm.mapper.CollectionMapper collectionMapper;

    public CollectionItemServiceImpl(CollectionItemMapper collectionItemMapper,
                                     CollectionItemTagMapper collectionItemTagMapper,
                                     ImportRecordMapper importRecordMapper,
                                     TagMapper tagMapper,
                                     com.qst.lm.mapper.NoteMapper noteMapper,
                                     com.qst.lm.mapper.CollectionMapper collectionMapper) {
        this.collectionItemMapper = collectionItemMapper;
        this.collectionItemTagMapper = collectionItemTagMapper;
        this.importRecordMapper = importRecordMapper;
        this.tagMapper = tagMapper;
        this.noteMapper = noteMapper;
        this.collectionMapper = collectionMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createItem(Long userId, CollectionItemDTO dto) {
        String normalizedSourceUrl = normalizeUrl(dto.getSourceUrl());
        if (dto.getSourceType() != null && dto.getSourceType() == 1 && StringUtils.hasText(normalizedSourceUrl)) {
            CollectionItem existingItem = findExistingByUrl(userId, normalizedSourceUrl);
            if (existingItem != null) {
                throw new BusinessException("该链接已被收藏");
            }
        }

        CollectionItem item = new CollectionItem();
        item.setUserId(userId);
        item.setTitle(dto.getTitle());
        item.setSourceType(dto.getSourceType());
        item.setCollectionId(dto.getCollectionId());
        item.setCoreAbstract(dto.getCoreAbstract());
        item.setDigestStatus(StringUtils.hasText(dto.getDigestStatus()) ? dto.getDigestStatus() : "undigest");
        item.setStudyProgress(dto.getStudyProgress());
        item.setStudyGoal(dto.getStudyGoal());
        item.setCategoryId(dto.getCategoryId());
        item.setKeywords(dto.getKeywords());
        item.setSource(dto.getSource());
        item.setSourceUrl(normalizedSourceUrl);
        item.setNoteId(dto.getNoteId());
        item.setRelatedNoteId(dto.getRelatedNoteId());
        item.setNoteSourceLabel(dto.getNoteSourceLabel());
        collectionItemMapper.insert(item);

        saveItemTags(userId, item.getId(), dto.getTagIds());

        log.info("用户[{}]创建收藏项[{}]成功", userId, item.getId());
        return R.success("创建收藏项成功", item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateItem(Long userId, Long id, CollectionItemDTO dto) {
        CollectionItem currentItem = getAndCheckOwnership(userId, id);
        String normalizedSourceUrl = normalizeUrl(dto.getSourceUrl());
        if (dto.getSourceType() != null && dto.getSourceType() == 1 && StringUtils.hasText(normalizedSourceUrl)) {
            CollectionItem existingItem = findExistingByUrl(userId, normalizedSourceUrl);
            if (existingItem != null && !existingItem.getId().equals(currentItem.getId())) {
                throw new BusinessException("该链接已被收藏");
            }
        }

        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getDeleted, 0)
                .set(CollectionItem::getTitle, dto.getTitle())
                .set(CollectionItem::getSourceType, dto.getSourceType())
                .set(CollectionItem::getCollectionId, dto.getCollectionId())
                .set(CollectionItem::getCoreAbstract, dto.getCoreAbstract())
                .set(CollectionItem::getStudyProgress, dto.getStudyProgress())
                .set(CollectionItem::getStudyGoal, dto.getStudyGoal())
                .set(CollectionItem::getCategoryId, dto.getCategoryId())
                .set(CollectionItem::getKeywords, dto.getKeywords())
                .set(CollectionItem::getSource, dto.getSource())
                .set(CollectionItem::getSourceUrl, normalizedSourceUrl)
                .set(CollectionItem::getNoteId, dto.getNoteId())
                .set(CollectionItem::getRelatedNoteId, dto.getRelatedNoteId())
                .set(CollectionItem::getNoteSourceLabel, dto.getNoteSourceLabel());
        if (StringUtils.hasText(dto.getDigestStatus())) {
            updateWrapper.set(CollectionItem::getDigestStatus, dto.getDigestStatus());
        }
        if (dto.getIsRead() != null) {
            updateWrapper.set(CollectionItem::getIsRead, dto.getIsRead());
        }
        if (dto.getIsStar() != null) {
            updateWrapper.set(CollectionItem::getIsStar, dto.getIsStar());
        }
        if (dto.getIsPublic() != null) {
            updateWrapper.set(CollectionItem::getIsPublic, dto.getIsPublic());
        }
        collectionItemMapper.update(null, updateWrapper);

        collectionItemTagMapper.deleteByCollectionItemId(id);
        saveItemTags(userId, id, dto.getTagIds());

        log.info("用户[{}]更新收藏项[{}]成功", userId, id);
        return R.success("更新收藏项成功");
    }

    @Override
    public R deleteItem(Long userId, Long id) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getDeleted, 1);
        collectionItemMapper.update(null, updateWrapper);
        log.info("用户[{}]删除收藏项[{}]成功", userId, id);
        return R.success("删除收藏项成功");
    }

    @Override
    public R getItemList(Long userId, CollectionItemQueryDTO query) {
        Page<CollectionItem> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 0);

        if (query.getCollectionId() != null) {
            wrapper.eq(CollectionItem::getCollectionId, query.getCollectionId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(CollectionItem::getTitle, query.getKeyword())
                    .or().like(CollectionItem::getKeywords, query.getKeyword())
                    .or().like(CollectionItem::getCoreAbstract, query.getKeyword()));
        }
        if (query.getSourceType() != null) {
            wrapper.eq(CollectionItem::getSourceType, query.getSourceType());
        }
        if (StringUtils.hasText(query.getDigestStatus())) {
            wrapper.eq(CollectionItem::getDigestStatus, query.getDigestStatus());
        }
        if (StringUtils.hasText(query.getStudyProgress())) {
            wrapper.eq(CollectionItem::getStudyProgress, query.getStudyProgress());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(CollectionItem::getCategoryId, query.getCategoryId());
        }
        if (query.getIsRead() != null) {
            wrapper.eq(CollectionItem::getIsRead, query.getIsRead() == 1);
        }
        if (query.getTagId() != null) {
            wrapper.inSql(CollectionItem::getId, "SELECT collection_item_id FROM collection_item_tag WHERE tag_id = " + query.getTagId());
        }

        if (!CollectionUtils.isEmpty(query.getTagIds())) {
            wrapper.inSql(CollectionItem::getId,
                "SELECT collection_item_id FROM collection_item_tag WHERE tag_id IN (" + joinIds(query.getTagIds()) + ") GROUP BY collection_item_id HAVING COUNT(DISTINCT tag_id) = " + query.getTagIds().size());
        }

        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(CollectionItem::getCreatedAt, query.getStartDate() + " 00:00:00");
        }

        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(CollectionItem::getCreatedAt, query.getEndDate() + " 23:59:59");
        }

        handleSort(wrapper, query.getSortBy(), query.getSortOrder());
        Page<CollectionItem> resultPage = collectionItemMapper.selectPage(page, wrapper);
        fillItemTags(userId, resultPage.getRecords());
        fillCollectionNames(resultPage.getRecords());
        return R.success(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getItemDetail(Long userId, Long id) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getVisitCount, item.getVisitCount() + 1);
        collectionItemMapper.update(null, updateWrapper);
        item.setVisitCount(item.getVisitCount() + 1);
        fillItemTags(userId, List.of(item));
        fillCollectionNames(List.of(item));
        return R.success(item);
    }

    @Override
    public R checkUrlExists(Long userId, String url) {
        String normalizedUrl = normalizeUrl(url);
        if (!StringUtils.hasText(normalizedUrl)) {
            throw new BusinessException("链接地址不能为空");
        }
        CollectionItem existingItem = findExistingByUrl(userId, normalizedUrl);
        Map<String, Object> result = new HashMap<>(2);
        result.put("exists", existingItem != null);
        result.put("itemId", existingItem != null ? existingItem.getId() : null);
        return R.success(result);
    }

    @Override
    public R parseUrlMetadata(Long userId, UrlMetadataRequestDTO dto) {
        String normalizedUrl = normalizeUrl(dto.getUrl());
        if (!StringUtils.hasText(normalizedUrl)) {
            throw new BusinessException("链接地址不能为空");
        }

        UrlMetadataResponseDTO responseDTO = new UrlMetadataResponseDTO();
        responseDTO.setUrl(dto.getUrl());
        responseDTO.setNormalizedUrl(normalizedUrl);
        responseDTO.setSourceType(detectSourceType(normalizedUrl));
        responseDTO.setSource(extractSource(normalizedUrl));

        try {
            Connection connection = Jsoup.connect(normalizedUrl)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .followRedirects(true)
                    .ignoreContentType(true);
            Connection.Response response = connection.execute();
            String contentType = response.contentType();
            String finalUrl = normalizeUrl(response.url().toString());
            responseDTO.setNormalizedUrl(finalUrl);
            responseDTO.setUrl(finalUrl);
            responseDTO.setSourceType(detectSourceType(finalUrl));
            responseDTO.setSource(extractSource(finalUrl));

            if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().contains("text/html")) {
                fillNonHtmlMetadata(responseDTO, finalUrl, contentType);
                return R.success(responseDTO);
            }

            Document document = response.parse();
            fillHtmlMetadata(responseDTO, document, finalUrl);
            return R.success(responseDTO);
        } catch (IOException e) {
            log.warn("解析链接元信息失败: {}", normalizedUrl, e);
            throw new BusinessException("解析链接元信息失败，请检查链接是否可访问");
        }
    }

    @Override
    public R markAsRead(Long userId, Long id) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getIsRead, true);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("标记已读成功");
    }

    @Override
    public R updateDigestStatus(Long userId, Long id, String status) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getDigestStatus, status);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("更新消化状态成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateStudyProgress(Long userId, Long id, String progress) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getStudyProgress, progress);
        if ("100%".equals(progress) || "100".equals(progress)) {
            updateWrapper.set(CollectionItem::getDigestStatus, "digested");
        }
        collectionItemMapper.update(null, updateWrapper);
        return R.success("更新学习进度成功");
    }

    @Override
    public R moveItem(Long userId, Long id, Long targetCollectionId) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getCollectionId, targetCollectionId);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("移动收藏项成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchMoveItems(Long userId, BatchMoveDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要移动的收藏项");
        }
        if (dto.getTargetCollectionId() == null) {
            throw new BusinessException("请选择目标收藏集");
        }

        for (Long id : dto.getIds()) {
            getAndCheckOwnership(userId, id);
        }

        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CollectionItem::getId, dto.getIds())
                .set(CollectionItem::getCollectionId, dto.getTargetCollectionId());
        collectionItemMapper.update(null, updateWrapper);

        return R.success("批量移动成功，共移动 " + dto.getIds().size() + " 项");
    }

    @Override
    public R getStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>(8);
        LambdaQueryWrapper<CollectionItem> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 0);
        long total = collectionItemMapper.selectCount(totalWrapper);
        statistics.put("total", total);

        String[] digestStatuses = {"undigest", "digesting", "digested", "abandoned"};
        for (String status : digestStatuses) {
            LambdaQueryWrapper<CollectionItem> statusWrapper = new LambdaQueryWrapper<>();
            statusWrapper.eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 0).eq(CollectionItem::getDigestStatus, status);
            statistics.put(status + "Count", collectionItemMapper.selectCount(statusWrapper));
        }

        LambdaQueryWrapper<CollectionItem> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 0).eq(CollectionItem::getIsRead, true);
        long readCount = collectionItemMapper.selectCount(readWrapper);
        statistics.put("readCount", readCount);
        statistics.put("unreadCount", total - readCount);
        return R.success(statistics);
    }

    private void saveItemTags(Long userId, Long itemId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }

        List<Long> distinctTagIds = tagIds.stream()
                .filter(tagId -> tagId != null)
                .distinct()
                .toList();
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getDeleted, 0)
                .in(Tag::getId, distinctTagIds);
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        if (tags.size() != distinctTagIds.size()) {
            throw new BusinessException("存在无效标签或无权使用的标签");
        }

        for (Long tagId : distinctTagIds) {
            CollectionItemTag itemTag = new CollectionItemTag();
            itemTag.setUserId(userId);
            itemTag.setCollectionItemId(itemId);
            itemTag.setTagId(tagId);
            collectionItemTagMapper.insert(itemTag);
        }
    }

    private void fillItemTags(Long userId, List<CollectionItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        List<Long> itemIds = items.stream()
                .map(CollectionItem::getId)
                .filter(id -> id != null)
                .toList();
        if (itemIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<CollectionItemTag> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(CollectionItemTag::getUserId, userId)
                .in(CollectionItemTag::getCollectionItemId, itemIds);
        List<CollectionItemTag> relations = collectionItemTagMapper.selectList(relationWrapper);
        if (relations.isEmpty()) {
            for (CollectionItem item : items) {
                item.setTags(List.of());
                item.setTagIds(List.of());
            }
            return;
        }

        Map<Long, List<Long>> itemIdToTagIds = relations.stream()
                .collect(Collectors.groupingBy(CollectionItemTag::getCollectionItemId,
                        Collectors.mapping(CollectionItemTag::getTagId, Collectors.toList())));

        List<Long> allTagIds = relations.stream()
                .map(CollectionItemTag::getTagId)
                .distinct()
                .toList();

        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getDeleted, 0)
                .in(Tag::getId, allTagIds);
        List<Tag> tags = tagMapper.selectList(tagWrapper);
        Map<Long, Tag> tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, t -> t, (a, b) -> a));

        for (CollectionItem item : items) {
            List<Long> tagIds = itemIdToTagIds.getOrDefault(item.getId(), List.of()).stream()
                    .distinct()
                    .toList();
            item.setTagIds(tagIds);
            List<Tag> itemTags = tagIds.stream()
                    .map(tagMap::get)
                    .filter(t -> t != null)
                    .toList();
            item.setTags(itemTags);
        }
    }

    private void fillCollectionNames(List<CollectionItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        List<Long> collectionIds = items.stream()
                .map(CollectionItem::getCollectionId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (collectionIds.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<com.qst.lm.pojo.Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(com.qst.lm.pojo.Collection::getId, collectionIds)
                .eq(com.qst.lm.pojo.Collection::getDeleted, 0);
        List<com.qst.lm.pojo.Collection> collections = collectionMapper.selectList(wrapper);

        Map<Long, String> collectionNameMap = collections.stream()
                .collect(Collectors.toMap(
                        com.qst.lm.pojo.Collection::getId,
                        com.qst.lm.pojo.Collection::getName,
                        (a, b) -> a
                ));

        for (CollectionItem item : items) {
            if (item.getCollectionId() != null) {
                String collectionName = collectionNameMap.get(item.getCollectionId());
                item.setCollectionName(collectionName);
            }
        }
    }

    private CollectionItem getAndCheckOwnership(Long userId, Long id) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getDeleted, 0);
        CollectionItem item = collectionItemMapper.selectOne(wrapper);
        if (item == null) {
            throw new BusinessException("收藏项不存在");
        }
        if (!item.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该收藏项");
        }
        return item;
    }

    private void handleSort(LambdaQueryWrapper<CollectionItem> wrapper, String sortBy, String sortOrder) {
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        if (!StringUtils.hasText(sortBy)) {
            wrapper.orderByDesc(CollectionItem::getCreatedAt);
            return;
        }
        switch (sortBy) {
            case "updatedAt":
                wrapper.orderBy(true, isAsc, CollectionItem::getUpdatedAt);
                break;
            case "visitCount":
                wrapper.orderBy(true, isAsc, CollectionItem::getVisitCount);
                break;
            case "title":
            case "name":
                wrapper.orderBy(true, isAsc, CollectionItem::getTitle);
                break;
            case "createdAt":
                wrapper.orderBy(true, isAsc, CollectionItem::getCreatedAt);
                break;
            default:
                wrapper.orderBy(true, isAsc, CollectionItem::getCreatedAt);
                break;
        }
    }

    @Override
    public R previewImport(Long userId, BookmarkImportDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getHtmlContent())) {
            throw new BusinessException("HTML内容不能为空");
        }

        List<Map<String, Object>> previewItems = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(dto.getHtmlContent());
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                String title = link.text();
                if (StringUtils.hasText(href) && !href.startsWith("#") && !href.startsWith("javascript:")) {
                    Map<String, Object> item = new HashMap<>(4);
                    item.put("url", href);
                    item.put("title", StringUtils.hasText(title) ? title : href);
                    item.put("sourceType", 1);
                    previewItems.add(item);
                }
            }

            Elements images = doc.select("img[src]");
            for (Element img : images) {
                String src = img.attr("src");
                String alt = img.attr("alt");
                if (StringUtils.hasText(src)) {
                    Map<String, Object> item = new HashMap<>(4);
                    item.put("url", src);
                    item.put("title", StringUtils.hasText(alt) ? alt : "图片");
                    item.put("sourceType", 2);
                    previewItems.add(item);
                }
            }
        } catch (Exception e) {
            log.error("解析HTML内容失败", e);
            throw new BusinessException("解析HTML内容失败");
        }

        Map<String, Object> result = new HashMap<>(4);
        result.put("totalCount", previewItems.size());
        result.put("items", previewItems);
        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R executeImport(Long userId, BookmarkImportDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getHtmlContent())) {
            throw new BusinessException("导入内容不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(dto.getHtmlContent());
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                try {
                    String href = link.attr("href");
                    String title = link.text();
                    if (!StringUtils.hasText(href) || href.startsWith("#") || href.startsWith("javascript:")) {
                        continue;
                    }
                    CollectionItem item = new CollectionItem();
                    item.setUserId(userId);
                    item.setTitle(StringUtils.hasText(title) ? title : href);
                    item.setSourceUrl(href);
                    item.setSourceType(1);
                    item.setCollectionId(dto.getTargetCollectionId());
                    item.setDigestStatus("undigest");
                    item.setIsRead(false);
                    item.setIsStar(false);
                    item.setVisitCount(0);
                    collectionItemMapper.insert(item);
                    successCount++;
                } catch (Exception e) {
                    log.error("导入收藏项失败", e);
                    failCount++;
                    errors.add("导入失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("解析HTML内容失败", e);
            throw new BusinessException("解析HTML内容失败");
        }

        ImportRecord record = new ImportRecord();
        record.setUserId(userId);
        record.setSourceType("browser");
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setTotalCount(successCount + failCount);
        importRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>(8);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", successCount + failCount);
        result.put("errors", errors);
        return R.success("导入完成", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R recoverItem(Long userId, Long id) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 1);
        if (collectionItemMapper.selectOne(wrapper) == null) {
            throw new BusinessException("回收站中不存在该收藏项");
        }
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getDeleted, 0);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("恢复成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R permanentDeleteItem(Long userId, Long id) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 1);
        if (collectionItemMapper.selectOne(wrapper) == null) {
            throw new BusinessException("回收站中不存在该收藏项");
        }
        collectionItemTagMapper.deleteByCollectionItemId(id);
        collectionItemMapper.deleteById(id);
        return R.success("永久删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchDelete(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要操作的数据");
        }
        for (Long id : dto.getIds()) {
            getAndCheckOwnership(userId, id);
        }
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CollectionItem::getId, dto.getIds()).set(CollectionItem::getDeleted, 1);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("批量删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchRecover(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要操作的数据");
        }
        for (Long id : dto.getIds()) {
            LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 1);
            if (collectionItemMapper.selectOne(wrapper) == null) {
                throw new BusinessException("回收站中存在无效收藏项");
            }
        }
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(CollectionItem::getId, dto.getIds()).set(CollectionItem::getDeleted, 0);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("批量恢复成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchPermanentDelete(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要操作的数据");
        }
        for (Long id : dto.getIds()) {
            LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getUserId, userId).eq(CollectionItem::getDeleted, 1);
            if (collectionItemMapper.selectOne(wrapper) == null) {
                throw new BusinessException("回收站中存在无效收藏项");
            }
            collectionItemTagMapper.deleteByCollectionItemId(id);
        }
        collectionItemMapper.deleteBatchIds(dto.getIds());
        return R.success("批量永久删除成功");
    }

    @Override
    public R toggleStar(Long userId, Long id) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        boolean newStatus = !Boolean.TRUE.equals(item.getIsStar());
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getIsStar, newStatus);
        collectionItemMapper.update(null, updateWrapper);
        Map<String, Object> result = new HashMap<>(1);
        result.put("isStar", newStatus);
        return R.success("切换星标成功", result);
    }

    @Override
    public R setRemind(Long userId, Long id, LocalDateTime remindAt) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getRemindAt, remindAt)
                .set(CollectionItem::getRemindTriggered, 0);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("设置提醒成功");
    }

    @Override
    public R cancelRemind(Long userId, Long id) {
        getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getRemindAt, null)
                .set(CollectionItem::getRemindTriggered, 0);
        collectionItemMapper.update(null, updateWrapper);
        return R.success("取消提醒成功");
    }


    @Override
    public R getRecycleBin(Long userId, CollectionItemQueryDTO query) {
        Page<CollectionItem> pageRequest = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
            .eq(CollectionItem::getDeleted, 1);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(CollectionItem::getTitle, query.getKeyword())
                .or().like(CollectionItem::getKeywords, query.getKeyword())
                .or().like(CollectionItem::getCoreAbstract, query.getKeyword()));
        }

        if (query.getSourceType() != null) {
            wrapper.eq(CollectionItem::getSourceType, query.getSourceType());
        }

        if (query.getCategoryId() != null) {
            wrapper.eq(CollectionItem::getCategoryId, query.getCategoryId());
        }

        if (query.getTagId() != null) {
            wrapper.inSql(CollectionItem::getId,
                "SELECT collection_item_id FROM collection_item_tag WHERE tag_id = " + query.getTagId());
        }

        if (!CollectionUtils.isEmpty(query.getTagIds())) {
            wrapper.inSql(CollectionItem::getId,
                "SELECT collection_item_id FROM collection_item_tag WHERE tag_id IN (" + joinIds(query.getTagIds()) + ") GROUP BY collection_item_id HAVING COUNT(DISTINCT tag_id) = " + query.getTagIds().size());
        }

        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(CollectionItem::getCreatedAt, query.getStartDate() + " 00:00:00");
        }

        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(CollectionItem::getCreatedAt, query.getEndDate() + " 23:59:59");
        }

        handleSort(wrapper, query.getSortBy(), query.getSortOrder());

        Page<CollectionItem> resultPage = collectionItemMapper.selectPage(pageRequest, wrapper);
        fillItemTags(userId, resultPage.getRecords());
        fillCollectionNames(resultPage.getRecords());
        return R.success(resultPage);
    }

 /*   @Override
    public R getRecycleBinStatistics(Long userId) {
        Map<String, Object> statistics = new HashMap<>(4);

        LambdaQueryWrapper<CollectionItem> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(CollectionItem::getUserId, userId)
            .eq(CollectionItem::getDeleted, 1);
        long totalCount = collectionItemMapper.selectCount(totalWrapper);
        statistics.put("totalCount", totalCount);

        LambdaQueryWrapper<CollectionItem> recentWrapper = new LambdaQueryWrapper<>();
        recentWrapper.eq(CollectionItem::getUserId, userId)
            .eq(CollectionItem::getDeleted, 1)
            .ge(CollectionItem::getUpdatedAt, LocalDateTime.now().minusDays(30));
        long recentCount = collectionItemMapper.selectCount(recentWrapper);
        statistics.put("recentDeletedCount", recentCount);

        return R.success(statistics);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R clearRecycleBin(Long userId) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
            .eq(CollectionItem::getDeleted, 1);
        List<CollectionItem> deletedItems = collectionItemMapper.selectList(wrapper);

        for (CollectionItem item : deletedItems) {
            collectionItemTagMapper.deleteByCollectionItemId(item.getId());
        }

        collectionItemMapper.delete(wrapper);

        log.info("用户[{}]清空回收站，共删除[{}]项", userId, deletedItems.size());
        return R.success("清空回收站成功");
    }
*/



    private CollectionItem findExistingByUrl(Long userId, String normalizedUrl) {
        if (!StringUtils.hasText(normalizedUrl)) {
            return null;
        }
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getDeleted, 0)
                .eq(CollectionItem::getSourceUrl, normalizedUrl)
                .last("LIMIT 1");
        return collectionItemMapper.selectOne(wrapper);
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase() : null;
            String host = uri.getHost() != null ? uri.getHost().toLowerCase() : null;
            String path = uri.getPath();
            if (StringUtils.hasText(path) && path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            URI normalizedUri = new URI(
                    scheme,
                    uri.getUserInfo(),
                    host,
                    uri.getPort(),
                    path,
                    uri.getQuery(),
                    uri.getFragment()
            );
            return normalizedUri.toString();
        } catch (URISyntaxException e) {
            throw new BusinessException("链接地址格式不正确");
        }
    }

    private Integer detectSourceType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.matches(".*\\.(jpg|jpeg|png|gif|webp|svg|bmp)(\\?.*)?$")) {
            return 2;
        }
        if (lowerUrl.matches(".*\\.(mp4|avi|mov|mkv|flv|webm|m3u8)(\\?.*)?$")) {
            return 4;
        }
        if (lowerUrl.matches(".*\\.(txt|md|doc|docx|pdf)(\\?.*)?$")) {
            return 3;
        }
        return 1;
    }

    private String extractSource(String url) {
      try {
          URI uri = new URI(url);
          return uri.getHost();
      } catch (URISyntaxException e) {
          return null;
      }
    }

    private String joinIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Long id : ids) {
            if (id == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id);
        }
        return builder.toString();
    }

    private void fillNonHtmlMetadata(UrlMetadataResponseDTO responseDTO, String finalUrl, String contentType) {
        responseDTO.setTitle(defaultTitleFromUrl(finalUrl));
        if (responseDTO.getSourceType() == 2) {
            responseDTO.setThumbnail(finalUrl);
            responseDTO.setDescription("检测到图片资源");
            return;
        }
        if (responseDTO.getSourceType() == 4) {
            responseDTO.setDescription("检测到视频资源");
            return;
        }
        if (responseDTO.getSourceType() == 3) {
            responseDTO.setDescription("检测到文档资源");
            return;
        }
        if (StringUtils.hasText(contentType)) {
            responseDTO.setDescription("资源类型：" + contentType);
        }
    }

    private void fillHtmlMetadata(UrlMetadataResponseDTO responseDTO, Document document, String finalUrl) {
        String title = firstNonBlank(
                document.select("meta[property=og:title]").attr("content"),
                document.title(),
                defaultTitleFromUrl(finalUrl)
        );
        String description = firstNonBlank(
                document.select("meta[property=og:description]").attr("content"),
                document.select("meta[name=description]").attr("content")
        );
        String thumbnail = firstNonBlank(
                document.select("meta[property=og:image]").attr("content"),
                document.select("meta[name=twitter:image]").attr("content")
        );
        if (StringUtils.hasText(thumbnail)) {
            thumbnail = resolveUrl(finalUrl, thumbnail);
        }

        responseDTO.setTitle(title);
        responseDTO.setDescription(description);
        responseDTO.setThumbnail(thumbnail);
    }

    private String defaultTitleFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String path = uri.getPath();
            if (!StringUtils.hasText(path) || "/".equals(path)) {
                return host;
            }
            return host + path;
        } catch (URISyntaxException e) {
            return url;
        }
    }

    private String resolveUrl(String baseUrl, String targetUrl) {
        try {
            return new URI(baseUrl).resolve(targetUrl).toString();
        } catch (URISyntaxException e) {
            return targetUrl;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    public R getRelatedNotes(Long userId, Long collectionItemId) {
        getAndCheckOwnership(userId, collectionItemId);
        
        LambdaQueryWrapper<com.qst.lm.pojo.Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.qst.lm.pojo.Note::getUserId, userId)
               .eq(com.qst.lm.pojo.Note::getCollectionItemId, collectionItemId)
               .orderByDesc(com.qst.lm.pojo.Note::getUpdateTime);
        
        List<com.qst.lm.pojo.Note> notes = noteMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>(2);
        result.put("notes", notes);
        result.put("count", notes.size());
        
        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getPublicItemDetail(Long id) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, id)
               .eq(CollectionItem::getDeleted, 0)
               .eq(CollectionItem::getIsPublic, 1);
        
        CollectionItem item = collectionItemMapper.selectOne(wrapper);
        if (item == null) {
            throw new BusinessException("收藏项不存在或未公开");
        }
        
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                    .set(CollectionItem::getVisitCount, item.getVisitCount() + 1);
        collectionItemMapper.update(null, updateWrapper);
        item.setVisitCount(item.getVisitCount() + 1);
        
        fillCollectionNames(List.of(item));
        
        return R.success(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R setCategory(Long userId, Long id, Long categoryId) {
        CollectionItem item = collectionItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("收藏项不存在");
        }

        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                    .set(CollectionItem::getCategoryId, categoryId);
        collectionItemMapper.update(null, updateWrapper);

        log.info("用户[{}]设置收藏项[{}]分类为[{}]成功", userId, id, categoryId);
        return R.success("设置分类成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchSetCategory(Long userId, List<Long> ids, Long categoryId) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("收藏项ID列表不能为空");
        }

        LambdaQueryWrapper<CollectionItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CollectionItem::getUserId, userId)
                   .in(CollectionItem::getId, ids)
                   .eq(CollectionItem::getDeleted, 0);
        List<CollectionItem> items = collectionItemMapper.selectList(queryWrapper);

        if (items.isEmpty()) {
            throw new BusinessException("未找到可操作的收藏项");
        }

        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getUserId, userId)
                    .in(CollectionItem::getId, ids)
                    .eq(CollectionItem::getDeleted, 0)
                    .set(CollectionItem::getCategoryId, categoryId);
        collectionItemMapper.update(null, updateWrapper);

        log.info("用户[{}]批量设置收藏项分类成功, 数量[{}]", userId, items.size());
        return R.success("批量设置分类成功", Map.of("count", items.size()));
    }
}
