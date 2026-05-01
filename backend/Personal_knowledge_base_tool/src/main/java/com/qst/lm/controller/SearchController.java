package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.search.SearchDTO;
import com.qst.lm.service.ISearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/search")
@Tag(name = "搜索模块", description = "搜索相关接口")
public class SearchController {

    private final ISearchService searchService;

    public SearchController(ISearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * 全局搜索
     */
    @GetMapping
    @Operation(summary = "全局搜索", description = "根据关键词搜索收藏项、笔记、收藏集")
    public R search(@RequestAttribute Long userId, @Valid SearchDTO dto) {
        return searchService.search(userId, dto);
    }

    /**
     * 获取搜索历史
     */
    @GetMapping("/history")
    @Operation(summary = "搜索历史", description = "获取当前用户的搜索历史（最近30条）")
    public R getSearchHistory(@RequestAttribute Long userId) {
        return searchService.getSearchHistory(userId);
    }

    /**
     * 删除单条搜索历史
     */
    @DeleteMapping("/history/{id}")
    @Operation(summary = "删除搜索历史", description = "删除指定搜索历史记录")
    public R deleteSearchHistory(@RequestAttribute Long userId, @PathVariable Long id) {
        return searchService.deleteSearchHistory(userId, id);
    }

    /**
     * 清空搜索历史
     */
    @DeleteMapping("/history/clear")
    @Operation(summary = "清空搜索历史", description = "清空当前用户所有搜索历史")
    public R clearSearchHistory(@RequestAttribute Long userId) {
        return searchService.clearSearchHistory(userId);
    }

    /**
     * 获取热门搜索词
     */
    @GetMapping("/hot")
    @Operation(summary = "热门搜索词", description = "获取热门搜索词TOP20")
    public R getHotKeywords() {
        return searchService.getHotKeywords();
    }
}
