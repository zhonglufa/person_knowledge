package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.category.CategoryDTO;
import com.qst.lm.dto.category.CategoryMoveDTO;
import com.qst.lm.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@Tag(name = "分类管理模块", description = "分类相关接口")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建一个新的分类")
    public R createCategory(@RequestAttribute Long userId,
                            @Valid @RequestBody CategoryDTO dto) {
        return categoryService.createCategory(userId, dto);
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑分类", description = "编辑指定分类信息")
    public R updateCategory(@RequestAttribute Long userId,
                            @PathVariable Long id,
                            @Valid @RequestBody CategoryDTO dto) {
        return categoryService.updateCategory(userId, id, dto);
    }

    /**
     * 删除分类（级联删除子分类）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除分类及其所有子分类")
    public R deleteCategory(@RequestAttribute Long userId,
                            @PathVariable Long id) {
        return categoryService.deleteCategory(userId, id);
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取当前用户的分类树形结构")
    public R getCategoryTree(@RequestAttribute Long userId) {
        return categoryService.getCategoryTree(userId);
    }

    /**
     * 获取分类平铺列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "获取当前用户的分类平铺列表")
    public R getCategoryList(@RequestAttribute Long userId) {
        return categoryService.getCategoryList(userId);
    }

    // ==================== 扩展接口 ====================

    /**
     * 获取分类统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取分类统计", description = "获取分类的统计信息，包括每个分类下的收藏项数量等")
    public R getCategoryStatistics(@RequestAttribute Long userId) {
        log.info("用户[{}]获取分类统计", userId);
        return categoryService.getStatistics(userId);
    }

    /**
     * 移动分类
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动分类", description = "将分类移动到其他父分类下或根级")
    public R moveCategory(@RequestAttribute Long userId,
                          @PathVariable Long id,
                          @Valid @RequestBody CategoryMoveDTO moveDTO) {
        log.info("用户[{}]移动分类[{}]到父分类[{}]", userId, id, moveDTO.getTargetParentId());
        return categoryService.moveCategory(userId, id, moveDTO);
    }
}
