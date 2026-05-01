package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.category.CategoryDTO;
import com.qst.lm.dto.category.CategoryMoveDTO;

/**
 * 分类管理服务接口
 */
public interface ICategoryService {

    /**
     * 创建分类
     *
     * @param userId 用户ID
     * @param dto    分类信息
     * @return 操作结果
     */
    R createCategory(Long userId, CategoryDTO dto);

    /**
     * 更新分类
     *
     * @param userId 用户ID
     * @param id     分类ID
     * @param dto    分类信息
     * @return 操作结果
     */
    R updateCategory(Long userId, Long id, CategoryDTO dto);

    /**
     * 删除分类（级联删除子分类）
     *
     * @param userId 用户ID
     * @param id     分类ID
     * @return 操作结果
     */
    R deleteCategory(Long userId, Long id);

    /**
     * 获取分类树
     *
     * @param userId 用户ID
     * @return 分类树
     */
    R getCategoryTree(Long userId);

    /**
     * 获取分类平铺列表
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    R getCategoryList(Long userId);

    /**
     * 获取分类统计
     *
     * @param userId 用户ID
     * @return 分类统计信息
     */
    R getStatistics(Long userId);

    /**
     * 移动分类
     *
     * @param userId 用户ID
     * @param id     分类ID
     * @param dto    移动参数
     * @return 操作结果
     */
    R moveCategory(Long userId, Long id, CategoryMoveDTO dto);
}
