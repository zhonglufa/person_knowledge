package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.search.SearchDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.*;
import com.qst.lm.pojo.*;
import com.qst.lm.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    private final CollectionItemMapper collectionItemMapper;
    private final CollectionMapper collectionMapper;
    private final NoteMapper noteMapper;
    private final TagMapper tagMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchHotMapper searchHotMapper;

    public SearchServiceImpl(CollectionItemMapper collectionItemMapper,
                             CollectionMapper collectionMapper,
                             NoteMapper noteMapper,
                             TagMapper tagMapper,
                             SearchHistoryMapper searchHistoryMapper,
                             SearchHotMapper searchHotMapper) {
        this.collectionItemMapper = collectionItemMapper;
        this.collectionMapper = collectionMapper;
        this.noteMapper = noteMapper;
        this.tagMapper = tagMapper;
        this.searchHistoryMapper = searchHistoryMapper;
        this.searchHotMapper = searchHotMapper;
    }

    @Override
    public R search(Long userId, SearchDTO dto) {
        String keyword = dto.getKeyword().trim();
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException("搜索关键词不能为空");
        }
        String searchType = dto.getSearchType();
        Map<String, Object> result = new HashMap<>(5);

        if ("all".equals(searchType) || "item".equals(searchType)) {
            Page<CollectionItem> itemPage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(CollectionItem::getUserId, userId)
                    .and(w -> w.like(CollectionItem::getTitle, keyword)
                            .or().like(CollectionItem::getCoreAbstract, keyword)
                            .or().like(CollectionItem::getKeywords, keyword)
                            .or().like(CollectionItem::getSource, keyword));
            applyCollectionItemFilters(itemWrapper, dto);
            applyCollectionItemSort(itemWrapper, dto.getSortBy());
            Page<CollectionItem> items = collectionItemMapper.selectPage(itemPage, itemWrapper);
            result.put("items", items);
        }

        if ("all".equals(searchType) || "note".equals(searchType)) {
            Page<Note> notePage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
            noteWrapper.and(w -> w.eq(Note::getUserId, userId).or().eq(Note::getIsPublic, 1))
                    .and(w -> w.like(Note::getTitle, keyword)
                            .or().like(Note::getContent, keyword)
                            .or().like(Note::getDescription, keyword));
            applyNoteFilters(noteWrapper, dto);
            applyNoteSort(noteWrapper, dto.getSortBy());
            Page<Note> notes = noteMapper.selectPage(notePage, noteWrapper);
            result.put("notes", notes);
        }

        if ("all".equals(searchType) || "collection".equals(searchType)) {
            Page<Collection> collPage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Collection> collWrapper = new LambdaQueryWrapper<>();
            collWrapper.and(w -> w.eq(Collection::getUserId, userId).or().eq(Collection::getIsPublic, 1))
                    .and(w -> w.like(Collection::getName, keyword)
                            .or().like(Collection::getDescription, keyword));
            applyCollectionFilters(collWrapper, dto);
            applyCollectionSort(collWrapper, dto.getSortBy());
            Page<Collection> collections = collectionMapper.selectPage(collPage, collWrapper);
            result.put("collections", collections);
        }

        if ("all".equals(searchType) || "tag".equals(searchType)) {
            Page<Tag> tagPage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(Tag::getUserId, userId)
                    .like(Tag::getName, keyword);
            applyTagFilters(tagWrapper, dto);
            applyTagSort(tagWrapper, dto.getSortBy());
            Page<Tag> tags = tagMapper.selectPage(tagPage, tagWrapper);
            result.put("tags", tags);
        }

        saveSearchHistory(userId, keyword);
        updateHotKeyword(keyword);

        log.info("用户[{}]搜索关键词[{}]，类型[{}]", userId, keyword, searchType);
        return R.success(result);
    }

    @Override
    public R getSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getLastSearchAt)
                .last("LIMIT 30");
        List<SearchHistory> list = searchHistoryMapper.selectList(wrapper);
        return R.success(list);
    }

    @Override
    public R deleteSearchHistory(Long userId, Long id) {
        SearchHistory history = searchHistoryMapper.selectById(id);
        if (history == null) {
            throw new BusinessException("搜索历史不存在");
        }
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该记录");
        }
        searchHistoryMapper.deleteById(id);
        return R.success("删除成功");
    }

    @Override
    public R clearSearchHistory(Long userId) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        searchHistoryMapper.delete(wrapper);
        log.info("用户[{}]清空搜索历史", userId);
        return R.success("清空成功");
    }

    @Override
    public R getHotKeywords() {
        LambdaQueryWrapper<SearchHot> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SearchHot::getSearchTotal)
                .last("LIMIT 20");
        List<SearchHot> list = searchHotMapper.selectList(wrapper);
        return R.success(list);
    }

    private void applyCollectionItemFilters(LambdaQueryWrapper<CollectionItem> wrapper, SearchDTO dto) {
        if (StringUtils.hasText(dto.getDigestStatus())) {
            wrapper.eq(CollectionItem::getDigestStatus, dto.getDigestStatus());
        }
        applyBaseTimeRange(wrapper, dto.getTimeRange());
    }

    private void applyNoteFilters(LambdaQueryWrapper<Note> wrapper, SearchDTO dto) {
        if (StringUtils.hasText(dto.getDigestStatus())) {
            wrapper.eq(Note::getDigestStatus, dto.getDigestStatus());
        }
        if (StringUtils.hasText(dto.getNoteType())) {
            wrapper.eq(Note::getNoteType, dto.getNoteType());
        }
        applyNoteTimeRange(wrapper, dto.getTimeRange());
    }

    private void applyCollectionFilters(LambdaQueryWrapper<Collection> wrapper, SearchDTO dto) {
        applyBaseTimeRange(wrapper, dto.getTimeRange());
    }

    private void applyTagFilters(LambdaQueryWrapper<Tag> wrapper, SearchDTO dto) {
        applyBaseTimeRange(wrapper, dto.getTimeRange());
    }

    private void applyCollectionItemSort(LambdaQueryWrapper<CollectionItem> wrapper, String sortBy) {
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(CollectionItem::getVisitCount)
                    .orderByDesc(CollectionItem::getUpdatedAt);
            return;
        }
        if ("createTime".equals(sortBy)) {
            wrapper.orderByDesc(CollectionItem::getCreatedAt);
            return;
        }
        wrapper.orderByDesc(CollectionItem::getUpdatedAt);
    }

    private void applyNoteSort(LambdaQueryWrapper<Note> wrapper, String sortBy) {
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Note::getHotScore)
                    .orderByDesc(Note::getUpdateTime);
            return;
        }
        if ("createTime".equals(sortBy)) {
            wrapper.orderByDesc(Note::getCreateTime);
            return;
        }
        wrapper.orderByDesc(Note::getUpdateTime);
    }

    private void applyCollectionSort(LambdaQueryWrapper<Collection> wrapper, String sortBy) {
        if ("createTime".equals(sortBy)) {
            wrapper.orderByDesc(Collection::getCreatedAt);
            return;
        }
        wrapper.orderByDesc(Collection::getUpdatedAt);
    }

    private void applyTagSort(LambdaQueryWrapper<Tag> wrapper, String sortBy) {
        if ("createTime".equals(sortBy)) {
            wrapper.orderByDesc(Tag::getCreatedAt);
            return;
        }
        wrapper.orderByDesc(Tag::getUpdatedAt);
    }

    private void applyBaseTimeRange(LambdaQueryWrapper<?> wrapper, String timeRange) {
        LocalDateTime[] range = buildTimeRange(timeRange);
        if (range == null) {
            return;
        }
        wrapper.apply("created_at BETWEEN {0} AND {1}", range[0], range[1]);
    }

    private void applyNoteTimeRange(LambdaQueryWrapper<Note> wrapper, String timeRange) {
        LocalDateTime[] range = buildTimeRange(timeRange);
        if (range == null) {
            return;
        }
        wrapper.between(Note::getCreateTime, range[0], range[1]);
    }

    private LocalDateTime[] buildTimeRange(String timeRange) {
        if (!StringUtils.hasText(timeRange)) {
            return null;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);
        LocalDate startDate;
        switch (timeRange) {
            case "today":
                startDate = today;
                break;
            case "week":
                startDate = today.minusDays(6);
                break;
            case "month":
                startDate = today.minusDays(29);
                break;
            case "quarter":
                startDate = today.minusDays(89);
                break;
            default:
                return null;
        }
        return new LocalDateTime[]{LocalDateTime.of(startDate, LocalTime.MIN), end};
    }

    private void saveSearchHistory(Long userId, String keyword) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword);
        SearchHistory existing = searchHistoryMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setSearchCount(existing.getSearchCount() + 1);
            existing.setLastSearchAt(LocalDateTime.now());
            searchHistoryMapper.updateById(existing);
        } else {
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(keyword);
            history.setSearchCount(1);
            history.setLastSearchAt(LocalDateTime.now());
            searchHistoryMapper.insert(history);
        }
    }

    private void updateHotKeyword(String keyword) {
        LambdaQueryWrapper<SearchHot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHot::getKeyword, keyword);
        SearchHot existing = searchHotMapper.selectOne(wrapper);
        if (existing != null) {
            existing.setSearchTotal(existing.getSearchTotal() + 1);
            searchHotMapper.updateById(existing);
        } else {
            SearchHot hot = new SearchHot();
            hot.setKeyword(keyword);
            hot.setSearchTotal(1L);
            searchHotMapper.insert(hot);
        }
    }
}
