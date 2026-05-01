package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.collection.CollectionDTO;
import com.qst.lm.dto.collection.CollectionQueryDTO;
import com.qst.lm.service.ICollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏集控制器
 */
@Slf4j
@RestController
@RequestMapping("/collections")
@Tag(name = "收藏集管理模块", description = "收藏集相关接口")
public class CollectionController {

    private final ICollectionService collectionService;

    public CollectionController(ICollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /**
     * 创建收藏集
     */
    @PostMapping
    @Operation(summary = "创建收藏集", description = "创建一个新的收藏集")
    public R createCollection(@RequestAttribute("userId") Long userId,
                              @Valid @RequestBody CollectionDTO dto) {
        return collectionService.createCollection(userId, dto);
    }

    /**
     * 编辑收藏集
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑收藏集", description = "更新指定收藏集信息")
    public R updateCollection(@RequestAttribute("userId") Long userId,
                              @PathVariable Long id,
                              @Valid @RequestBody CollectionDTO dto) {
        return collectionService.updateCollection(userId, id, dto);
    }

    /**
     * 删除收藏集
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收藏集", description = "逻辑删除指定收藏集")
    public R deleteCollection(@RequestAttribute("userId") Long userId,
                              @PathVariable Long id) {
        return collectionService.deleteCollection(userId, id);
    }

    /**
     * 获取我的收藏集列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取收藏集列表", description = "分页获取当前用户的收藏集列表")
    public R getCollectionList(@RequestAttribute("userId") Long userId,
                               CollectionQueryDTO query) {
        return collectionService.getCollectionList(userId, query);
    }

    /**
     * 获取收藏集详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取收藏集详情", description = "获取指定收藏集的详细信息")
    public R getCollectionDetail(@RequestAttribute("userId") Long userId,
                                 @PathVariable Long id) {
        return collectionService.getCollectionDetail(userId, id);
    }

    /**
     * 获取公开收藏集列表
     */
    @GetMapping("/public")
    @Operation(summary = "获取公开收藏集列表", description = "分页获取所有公开的收藏集")
    public R getPublicCollections(CollectionQueryDTO query) {
        return collectionService.getPublicCollections(query);
    }

    // ==================== 扩展接口 ====================

    /**
     * 获取推荐收藏集（支持匿名访问）
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐收藏集", description = "获取系统推荐的收藏集列表，支持匿名访问")
    public R getRecommendedCollections(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取推荐收藏集, 用户ID[{}], 页码[{}], 每页[{}]", userId, pageNum, pageSize);
        return collectionService.getRecommended(userId, pageNum, pageSize);
    }

    /**
     * 获取收藏集内的收藏项
     */
    @GetMapping("/{id}/items")
    @Operation(summary = "获取收藏集内收藏项", description = "获取指定收藏集内的所有收藏项列表")
    public R getCollectionItems(@RequestAttribute("userId") Long userId,
                                @PathVariable Long id,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("用户[{}]获取收藏集[{}]内的收藏项, 页码[{}], 每页[{}]", userId, id, pageNum, pageSize);
        return collectionService.getCollectionItems(userId, id, pageNum, pageSize);
    }
}
