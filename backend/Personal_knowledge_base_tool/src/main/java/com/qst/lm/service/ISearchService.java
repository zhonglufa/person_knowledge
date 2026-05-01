package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.search.SearchDTO;

/**
 * 搜索服务接口
 */
public interface ISearchService {

    /**
     * 全局搜索
     *
     * @param userId 用户ID
     * @param dto    搜索参数
     * @return 搜索结果
     */
    R search(Long userId, SearchDTO dto);

    /**
     * 获取搜索历史
     *
     * @param userId 用户ID
     * @return 搜索历史列表（最近30条）
     */
    R getSearchHistory(Long userId);

    /**
     * 删除单条搜索历史
     *
     * @param userId 用户ID
     * @param id     历史记录ID
     * @return 操作结果
     */
    R deleteSearchHistory(Long userId, Long id);

    /**
     * 清空搜索历史
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    R clearSearchHistory(Long userId);

    /**
     * 获取热门搜索词
     *
     * @return 热门搜索词列表（TOP20）
     */
    R getHotKeywords();
}
