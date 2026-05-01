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

import java.time.LocalDateTime;
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
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchHotMapper searchHotMapper;

    public SearchServiceImpl(CollectionItemMapper collectionItemMapper,
                             CollectionMapper collectionMapper,
                             NoteMapper noteMapper,
                             SearchHistoryMapper searchHistoryMapper,
                             SearchHotMapper searchHotMapper) {
        this.collectionItemMapper = collectionItemMapper;
        this.collectionMapper = collectionMapper;
        this.noteMapper = noteMapper;
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
        String likeKeyword = "%" + keyword + "%";
        Map<String, Object> result = new HashMap<>(4);

        // 搜索收藏项
        if ("all".equals(searchType) || "item".equals(searchType)) {
            Page<CollectionItem> itemPage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(CollectionItem::getUserId, userId)
                    .and(w -> w.like(CollectionItem::getTitle, keyword)
                            .or().like(CollectionItem::getCoreAbstract, keyword)
                            .or().like(CollectionItem::getKeywords, keyword));
            Page<CollectionItem> items = collectionItemMapper.selectPage(itemPage, itemWrapper);
            result.put("items", items);
        }

        // 搜索笔记：公开笔记 + 自己的私有笔记
        if ("all".equals(searchType) || "note".equals(searchType)) {
            Page<Note> notePage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
            noteWrapper.and(w -> w.eq(Note::getUserId, userId).or().eq(Note::getIsPublic, 1))
                    .and(w -> w.like(Note::getTitle, keyword)
                            .or().like(Note::getContent, keyword));
            Page<Note> notes = noteMapper.selectPage(notePage, noteWrapper);
            result.put("notes", notes);
        }

        // 搜索收藏集：公开 + 自己的私有
        if ("all".equals(searchType) || "collection".equals(searchType)) {
            Page<Collection> collPage = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Collection> collWrapper = new LambdaQueryWrapper<>();
            collWrapper.and(w -> w.eq(Collection::getUserId, userId).or().eq(Collection::getIsPublic, 1))
                    .and(w -> w.like(Collection::getName, keyword)
                            .or().like(Collection::getDescription, keyword));
            Page<Collection> collections = collectionMapper.selectPage(collPage, collWrapper);
            result.put("collections", collections);
        }

        // 记录搜索历史
        saveSearchHistory(userId, keyword);

        // 更新热门搜索词
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

    /**
     * 记录搜索历史
     */
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

    /**
     * 更新热门搜索词
     */
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
