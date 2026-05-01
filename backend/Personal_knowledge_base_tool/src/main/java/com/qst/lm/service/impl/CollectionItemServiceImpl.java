package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.BusinessException;
import com.qst.lm.common.R;
import com.qst.lm.dto.collect.*;
import com.qst.lm.entity.CollectionItem;
import com.qst.lm.entity.CollectionItemTag;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionItemTagMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 收藏项服务实现类
 */
@Slf4j
@Service
public class CollectionItemServiceImpl implements ICollectionItemService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private static final Pattern OG_TITLE_PATTERN = Pattern.compile("<meta[^>]+property=['\"]og:title['\"][^>]+content=['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern OG_DESCRIPTION_PATTERN = Pattern.compile("<meta[^>]+property=['\"]og:description['\"][^>]+content=['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile("<meta[^>]+property=['\"]og:image['\"][^>]+content=['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
    private static final int MAX_PREVIEW_TITLE_LENGTH = 120;
    private static final int MAX_PREVIEW_SUMMARY_LENGTH = 300;

    private final CollectionItemMapper collectionItemMapper;
    private final CollectionItemTagMapper collectionItemTagMapper;

    public CollectionItemServiceImpl(CollectionItemMapper collectionItemMapper,
                                     CollectionItemTagMapper collectionItemTagMapper) {
        this.collectionItemMapper = collectionItemMapper;
        this.collectionItemTagMapper = collectionItemTagMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createItem(Long userId, CollectionItemDTO dto) {
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (dto.getSourceType() == null) {
            throw new BusinessException("来源类型不能为空");
        }
        if (!StringUtils.hasText(dto.getSourceUrl()) && !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("收藏链接或标题至少填写一项");
        }
        String normalizedUrl = normalizeUrl(dto.getSourceUrl());
        if (StringUtils.hasText(normalizedUrl)) {
            CollectionItem existingItem = findExistingByUrl(userId, normalizedUrl);
            if (existingItem != null) {
                throw new BusinessException("该链接已被收藏");
            }
        }

        CollectionItem item = new CollectionItem();
        item.setUserId(userId);
        item.setCollectionId(dto.getCollectionId());
        item.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle().trim() : deriveTitleFromUrl(normalizedUrl));
        item.setSourceType(dto.getSourceType());
        item.setSourceUrl(normalizedUrl);
        item.setSource(StringUtils.hasText(dto.getSource()) ? dto.getSource() : extractSource(normalizedUrl));
        item.setCoverImage(dto.getCoverImage());
        item.setKeywords(dto.getKeywords());
        item.setCoreAbstract(dto.getCoreAbstract());
        item.setDigestStatus(StringUtils.hasText(dto.getDigestStatus()) ? dto.getDigestStatus() : "undigest");
        item.setStudyProgress(StringUtils.hasText(dto.getStudyProgress()) ? dto.getStudyProgress() : "0%");
        item.setCategoryId(dto.getCategoryId());
        item.setIsRead(Boolean.TRUE.equals(dto.getIsRead()));
        item.setVisitCount(dto.getVisitCount() == null ? 0 : dto.getVisitCount());
        item.setDeleted(0);
        item.setStarred(Boolean.TRUE.equals(dto.getStarred()));
        item.setAllowShare(Boolean.TRUE.equals(dto.getAllowShare()));
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        collectionItemMapper.insert(item);
        saveItemTags(userId, item.getId(), dto.getTagIds());
        log.info("用户[{}]创建收藏项[{}]成功", userId, item.getId());
        return R.success(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateItem(Long userId, Long id, CollectionItemDTO dto) {
        CollectionItem existing = getAndCheckOwnership(userId, id);
        if (dto == null) {
            throw new BusinessException("请求参数不能为空");
        }
        String normalizedUrl = normalizeUrl(dto.getSourceUrl());
        if (StringUtils.hasText(normalizedUrl)) {
            CollectionItem duplicate = findExistingByUrl(userId, normalizedUrl);
            if (duplicate != null && !duplicate.getId().equals(id)) {
                throw new BusinessException("该链接已被收藏");
            }
        }
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(dto.getCollectionId() != null, CollectionItem::getCollectionId, dto.getCollectionId())
                .set(StringUtils.hasText(dto.getTitle()), CollectionItem::getTitle, dto.getTitle().trim())
                .set(dto.getSourceType() != null, CollectionItem::getSourceType, dto.getSourceType())
                .set(StringUtils.hasText(normalizedUrl), CollectionItem::getSourceUrl, normalizedUrl)
                .set(StringUtils.hasText(dto.getSource()), CollectionItem::getSource, dto.getSource())
                .set(dto.getCoverImage() != null, CollectionItem::getCoverImage, dto.getCoverImage())
                .set(dto.getKeywords() != null, CollectionItem::getKeywords, dto.getKeywords())
                .set(dto.getCoreAbstract() != null, CollectionItem::getCoreAbstract, dto.getCoreAbstract())
                .set(dto.getDigestStatus() != null, CollectionItem::getDigestStatus, dto.getDigestStatus())
                .set(dto.getStudyProgress() != null, CollectionItem::getStudyProgress, dto.getStudyProgress())
                .set(dto.getCategoryId() != null, CollectionItem::getCategoryId, dto.getCategoryId())
                .set(dto.getIsRead() != null, CollectionItem::getIsRead, dto.getIsRead())
                .set(dto.getStarred() != null, CollectionItem::getStarred, dto.getStarred())
                .set(dto.getAllowShare() != null, CollectionItem::getAllowShare, dto.getAllowShare())
                .set(CollectionItem::getUpdatedAt, LocalDateTime.now());
        collectionItemMapper.update(null, updateWrapper);
        if (!CollectionUtils.isEmpty(dto.getTagIds())) {
            LambdaQueryWrapper<CollectionItemTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(CollectionItemTag::getCollectionItemId, id);
            collectionItemTagMapper.delete(tagWrapper);
            saveItemTags(userId, id, dto.getTagIds());
        }
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
        return queryItemsByDeleted(userId, query, 0);
    }

    @Override
    public R getRecycleBin(Long userId, CollectionItemQueryDTO query) {
        return queryItemsByDeleted(userId, query, 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R getItemDetail(Long userId, Long id) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id).set(CollectionItem::getVisitCount, item.getVisitCount() + 1);
        collectionItemMapper.update(null, updateWrapper);
        item.setVisitCount(item.getVisitCount() + 1);
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

        List<Map<String, Object>> importedItems = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try {
            Document doc = Jsoup.parse(dto.getHtmlContent());
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                try {
                    String href = link.attr("href");
                    String title = link.text();
                    if (StringUtils.hasText(href) && !href.startsWith("#") && !href.startsWith("javascript:")) {
                        CollectionItemDTO itemDTO = new CollectionItemDTO();
                        itemDTO.setSourceUrl(href);
                        itemDTO.setTitle(StringUtils.hasText(title) ? title : href);
                        itemDTO.setSourceType(1);
                        itemDTO.setCollectionId(dto.getCollectionId());
                        if (dto.getTagIds() != null) {
                            itemDTO.setTagIds(dto.getTagIds());
                        }

                        createItem(userId, itemDTO);
                        successCount++;

                        Map<String, Object> importedItem = new HashMap<>(4);
                        importedItem.put("title", itemDTO.getTitle());
                        importedItem.put("url", itemDTO.getSourceUrl());
                        importedItem.put("status", "success");
                        importedItems.add(importedItem);
                    }
                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> failItem = new HashMap<>(4);
                    failItem.put("title", link.text());
                    failItem.put("url", link.attr("href"));
                    failItem.put("status", "failed");
                    failItem.put("reason", e.getMessage());
                    importedItems.add(failItem);
                }
            }

            Elements images = doc.select("img[src]");
            for (Element img : images) {
                try {
                    String src = img.attr("src");
                    String alt = img.attr("alt");
                    if (StringUtils.hasText(src)) {
                        CollectionItemDTO itemDTO = new CollectionItemDTO();
                        itemDTO.setSourceUrl(src);
                        itemDTO.setTitle(StringUtils.hasText(alt) ? alt : "图片");
                        itemDTO.setSourceType(2);
                        itemDTO.setCollectionId(dto.getCollectionId());
                        if (dto.getTagIds() != null) {
                            itemDTO.setTagIds(dto.getTagIds());
                        }

                        createItem(userId, itemDTO);
                        successCount++;

                        Map<String, Object> importedItem = new HashMap<>(4);
                        importedItem.put("title", itemDTO.getTitle());
                        importedItem.put("url", itemDTO.getSourceUrl());
                        importedItem.put("status", "success");
                        importedItems.add(importedItem);
                    }
                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> failItem = new HashMap<>(4);
                    failItem.put("title", img.attr("alt"));
                    failItem.put("url", img.attr("src"));
                    failItem.put("status", "failed");
                    failItem.put("reason", e.getMessage());
                    importedItems.add(failItem);
                }
            }
        } catch (Exception e) {
            log.error("执行HTML导入失败", e);
            throw new BusinessException("执行HTML导入失败");
        }

        Map<String, Object> result = new HashMap<>(6);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", successCount + failCount);
        result.put("items", importedItems);
        result.put("message", String.format("成功导入%d项，失败%d项", successCount, failCount));
        return R.success(result);
    }

    @Override
    public R recoverItem(Long userId, Long id) {
        CollectionItem item = getAndCheckDeletedOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getDeleted, 0)
                .set(CollectionItem::getUpdatedAt, LocalDateTime.now());
        collectionItemMapper.update(null, updateWrapper);
        log.info("用户[{}]恢复收藏项[{}]成功", userId, item.getId());
        return R.success("恢复收藏成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R permanentDeleteItem(Long userId, Long id) {
        CollectionItem item = getAndCheckDeletedOwnership(userId, id);
        LambdaQueryWrapper<CollectionItemTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(CollectionItemTag::getCollectionItemId, id);
        collectionItemTagMapper.delete(tagWrapper);
        collectionItemMapper.deleteById(id);
        log.info("用户[{}]永久删除收藏项[{}]成功", userId, item.getId());
        return R.success("永久删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchDelete(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要删除的收藏项");
        }
        for (Long id : dto.getIds()) {
            deleteItem(userId, id);
        }
        return R.success("批量删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchRecover(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要恢复的收藏项");
        }
        for (Long id : dto.getIds()) {
            recoverItem(userId, id);
        }
        return R.success("批量恢复成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchPermanentDelete(Long userId, BatchOperationDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getIds())) {
            throw new BusinessException("请选择要永久删除的收藏项");
        }
        for (Long id : dto.getIds()) {
            permanentDeleteItem(userId, id);
        }
        return R.success("批量永久删除成功");
    }

    @Override
    public R toggleStar(Long userId, Long id) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        boolean targetStarred = !Boolean.TRUE.equals(item.getStarred());
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getStarred, targetStarred)
                .set(CollectionItem::getUpdatedAt, LocalDateTime.now());
        collectionItemMapper.update(null, updateWrapper);
        item.setStarred(targetStarred);
        return R.success(item);
    }

    @Override
    public R setRemind(Long userId, Long id, LocalDateTime remindAt) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getRemindAt, remindAt)
                .set(CollectionItem::getUpdatedAt, LocalDateTime.now());
        collectionItemMapper.update(null, updateWrapper);
        item.setRemindAt(remindAt);
        return R.success(item);
    }

    @Override
    public R cancelRemind(Long userId, Long id) {
        CollectionItem item = getAndCheckOwnership(userId, id);
        LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CollectionItem::getId, id)
                .set(CollectionItem::getRemindAt, null)
                .set(CollectionItem::getUpdatedAt, LocalDateTime.now());
        collectionItemMapper.update(null, updateWrapper);
        item.setRemindAt(null);
        return R.success(item);
    }

    private R queryItemsByDeleted(Long userId, CollectionItemQueryDTO query, int deletedFlag) {
        long pageNum = query != null && query.getPageNum() != null ? query.getPageNum() : 1L;
        long pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 10L;
        Page<CollectionItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CollectionItem> wrapper = buildItemQueryWrapper(userId, query, deletedFlag);
        return R.success(collectionItemMapper.selectPage(page, wrapper));
    }

    private LambdaQueryWrapper<CollectionItem> buildItemQueryWrapper(Long userId, CollectionItemQueryDTO query, int deletedFlag) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getUserId, userId)
                .eq(CollectionItem::getDeleted, deletedFlag);

        if (query == null) {
            wrapper.orderByDesc(CollectionItem::getCreatedAt);
            return wrapper;
        }

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
            wrapper.inSql(CollectionItem::getId,
                    "SELECT collection_item_id FROM collection_item_tag WHERE tag_id = " + query.getTagId());
        }
        if (!CollectionUtils.isEmpty(query.getTagIds())) {
            String joinedTagIds = query.getTagIds().stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .reduce((left, right) -> left + "," + right)
                    .orElse(null);
            if (StringUtils.hasText(joinedTagIds)) {
                wrapper.inSql(CollectionItem::getId,
                        "SELECT collection_item_id FROM collection_item_tag WHERE tag_id IN (" + joinedTagIds + ") GROUP BY collection_item_id");
            }
        }
        if (StringUtils.hasText(query.getStartDate())) {
            LocalDateTime startDateTime = parseStartOfDay(query.getStartDate());
            if (startDateTime != null) {
                wrapper.ge(CollectionItem::getCreatedAt, startDateTime);
            }
        }
        if (StringUtils.hasText(query.getEndDate())) {
            LocalDateTime endDateTime = parseEndOfDay(query.getEndDate());
            if (endDateTime != null) {
                wrapper.le(CollectionItem::getCreatedAt, endDateTime);
            }
        }

        handleSort(wrapper, query.getSortBy(), query.getSortOrder());
        return wrapper;
    }

    private void saveItemTags(Long userId, Long itemId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        for (Long tagId : tagIds) {
            CollectionItemTag itemTag = new CollectionItemTag();
            itemTag.setUserId(userId);
            itemTag.setCollectionItemId(itemId);
            itemTag.setTagId(tagId);
            collectionItemTagMapper.insert(itemTag);
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

    private CollectionItem getAndCheckDeletedOwnership(Long userId, Long id) {
        LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionItem::getId, id).eq(CollectionItem::getDeleted, 1);
        CollectionItem item = collectionItemMapper.selectOne(wrapper);
        if (item == null) {
            throw new BusinessException("回收站中不存在该收藏项");
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
            case "updatedAt" -> wrapper.orderBy(true, isAsc, CollectionItem::getUpdatedAt);
            case "visitCount" -> wrapper.orderBy(true, isAsc, CollectionItem::getVisitCount);
            default -> wrapper.orderBy(true, isAsc, CollectionItem::getCreatedAt);
        }
    }

    private LocalDateTime parseStartOfDay(String dateStr) {
        try {
            return LocalDate.parse(dateStr).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseEndOfDay(String dateStr) {
        try {
            return LocalDate.parse(dateStr).atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

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

    private String normalizeUrl(String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return null;
        }
        String url = rawUrl.trim();
        if (!StringUtils.hasText(url)) {
            return null;
        }
        if (!url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
            url = "https://" + url;
        }
        try {
            URI uri = new URI(url).normalize();
            String scheme = uri.getScheme() == null ? "https" : uri.getScheme().toLowerCase();
            String host = uri.getHost();
            if (!StringUtils.hasText(host) && StringUtils.hasText(uri.getAuthority())) {
                host = uri.getAuthority();
            }
            if (!StringUtils.hasText(host)) {
                return url;
            }
            host = host.toLowerCase();
            int port = uri.getPort();
            String path = StringUtils.hasText(uri.getPath()) ? uri.getPath() : "/";
            String query = uri.getQuery();
            String normalized = scheme + "://" + host;
            if (port > 0 && port != 80 && port != 443) {
                normalized += ":" + port;
            }
            normalized += path;
            if (StringUtils.hasText(query)) {
                normalized += "?" + query;
            }
            return normalized;
        } catch (URISyntaxException e) {
            return url;
        }
    }

    private String extractSource(String normalizedUrl) {
        if (!StringUtils.hasText(normalizedUrl)) {
            return null;
        }
        try {
            URI uri = new URI(normalizedUrl);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String deriveTitleFromUrl(String normalizedUrl) {
        if (!StringUtils.hasText(normalizedUrl)) {
            return "未命名收藏";
        }
        try {
            URI uri = new URI(normalizedUrl);
            String host = uri.getHost();
            return StringUtils.hasText(host) ? host : normalizedUrl;
        } catch (URISyntaxException e) {
            return normalizedUrl;
        }
    }

    private Integer detectSourceType(String normalizedUrl) {
        if (!StringUtils.hasText(normalizedUrl)) {
            return 1;
        }
        String lowerUrl = normalizedUrl.toLowerCase();
        if (lowerUrl.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp|svg)(\\?.*)?$")) {
            return 2;
        }
        if (lowerUrl.matches(".*\\.(mp4|mov|avi|mkv|wmv|flv|webm)(\\?.*)?$")) {
            return 4;
        }
        if (lowerUrl.matches(".*\\.(txt|md|pdf|doc|docx)(\\?.*)?$")) {
            return 3;
        }
        return 1;
    }

    private void fillNonHtmlMetadata(UrlMetadataResponseDTO responseDTO, String url, String contentType) {
        responseDTO.setTitle(deriveTitleFromUrl(url));
        responseDTO.setSummary(contentType);
        if (responseDTO.getSourceType() == 2) {
            responseDTO.setCoverImage(url);
        }
    }

    private void fillHtmlMetadata(UrlMetadataResponseDTO responseDTO, Document document, String fallbackUrl) {
        String html = document.outerHtml();
        responseDTO.setTitle(extractTitle(document, html, fallbackUrl));
        responseDTO.setSummary(extractDescription(document, html));
        responseDTO.setCoverImage(extractImage(document, html));
    }

    private String extractTitle(Document document, String html, String fallbackUrl) {
        String title = metaContent(document, "meta[property=og:title]");
        if (!StringUtils.hasText(title)) {
            title = metaContent(document, "meta[name=twitter:title]");
        }
        if (!StringUtils.hasText(title)) {
            Matcher matcher = OG_TITLE_PATTERN.matcher(html);
            if (matcher.find()) {
                title = matcher.group(1);
            }
        }
        if (!StringUtils.hasText(title)) {
            title = document.title();
        }
        if (!StringUtils.hasText(title)) {
            title = deriveTitleFromUrl(fallbackUrl);
        }
        return truncate(title, MAX_PREVIEW_TITLE_LENGTH);
    }

    private String extractDescription(Document document, String html) {
        String description = metaContent(document, "meta[property=og:description]");
        if (!StringUtils.hasText(description)) {
            description = metaContent(document, "meta[name=description]");
        }
        if (!StringUtils.hasText(description)) {
            description = metaContent(document, "meta[name=twitter:description]");
        }
        if (!StringUtils.hasText(description)) {
            Matcher matcher = OG_DESCRIPTION_PATTERN.matcher(html);
            if (matcher.find()) {
                description = matcher.group(1);
            }
        }
        if (!StringUtils.hasText(description)) {
            String bodyText = document.body() != null ? document.body().text() : null;
            if (StringUtils.hasText(bodyText)) {
                description = bodyText;
            }
        }
        return truncate(description, MAX_PREVIEW_SUMMARY_LENGTH);
    }

    private String extractImage(Document document, String html) {
        String image = metaContent(document, "meta[property=og:image]");
        if (!StringUtils.hasText(image)) {
            image = metaContent(document, "meta[name=twitter:image]");
        }
        if (!StringUtils.hasText(image)) {
            Matcher matcher = OG_IMAGE_PATTERN.matcher(html);
            if (matcher.find()) {
                image = matcher.group(1);
            }
        }
        if (!StringUtils.hasText(image)) {
            Element firstImage = document.selectFirst("img[src]");
            if (firstImage != null) {
                image = firstImage.absUrl("src");
                if (!StringUtils.hasText(image)) {
                    image = firstImage.attr("src");
                }
            }
        }
        return image;
    }

    private String metaContent(Document document, String cssQuery) {
        Element element = document.selectFirst(cssQuery);
        if (element == null) {
            return null;
        }
        String content = element.attr("content");
        return StringUtils.hasText(content) ? content.trim() : null;
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
