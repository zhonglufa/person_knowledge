package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.collect.BatchMoveDTO;
import com.qst.lm.dto.collect.BatchOperationDTO;
import com.qst.lm.dto.collect.BookmarkImportDTO;
import com.qst.lm.dto.collect.CollectionItemDTO;
import com.qst.lm.dto.collect.CollectionItemQueryDTO;
import com.qst.lm.dto.collect.UrlMetadataRequestDTO;
import com.qst.lm.service.ICollectionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏项控制器
 */
@Slf4j
@RestController
@RequestMapping("/collect")
@Tag(name = "收藏项管理模块", description = "收藏项相关接口")
public class CollectionItemController {

    private final ICollectionItemService collectionItemService;

    public CollectionItemController(ICollectionItemService collectionItemService) {
        this.collectionItemService = collectionItemService;
    }

    @PostMapping("/add-collect")
    @Operation(summary = "创建收藏项", description = "创建一个新的收藏项；sourceType 支持 1=网页、2=图片、3=文本、4=视频、5=笔记。笔记类型可额外传 noteId、relatedNoteId、noteSourceLabel。")
    public R createItem(@RequestAttribute("userId") Long userId,
                        @Valid @RequestBody CollectionItemDTO dto) {
        log.info("用户[{}]创建收藏项: {}", userId, dto.getTitle());
        return collectionItemService.createItem(userId, dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑收藏项", description = "更新指定收藏项信息；当 sourceType=5 时，支持更新 noteId、relatedNoteId、noteSourceLabel。")
    public R updateItem(@RequestAttribute("userId") Long userId,
                        @PathVariable Long id,
                        @Valid @RequestBody CollectionItemDTO dto) {
        log.info("用户[{}]编辑收藏项[{}]", userId, id);
        return collectionItemService.updateItem(userId, id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除收藏项", description = "逻辑删除指定收藏项")
    public R deleteItem(@RequestAttribute("userId") Long userId,
                        @PathVariable Long id) {
        log.info("用户[{}]删除收藏项[{}]", userId, id);
        return collectionItemService.deleteItem(userId, id);
    }

    @GetMapping("/list")
    @Operation(summary = "获取收藏项列表", description = "分页获取当前用户的收藏项列表，支持多条件筛选。返回 records 中包含 sourceType=5 所需的 noteId、relatedNoteId、noteSourceLabel 字段。")
    public R getItemList(@RequestAttribute("userId") Long userId,
                         CollectionItemQueryDTO query) {
        log.info("用户[{}]获取收藏项列表, 页码[{}], 每页[{}]", userId, query.getPageNum(), query.getPageSize());
        return collectionItemService.getItemList(userId, query);
    }

    @GetMapping("/recycle-bin")
    @Operation(summary = "获取回收站列表", description = "分页获取当前用户已软删除的收藏项列表，支持关键字、类型、时间和标签等筛选。")
    public R getRecycleBin(@RequestAttribute("userId") Long userId,
                           CollectionItemQueryDTO query) {
        log.info("用户[{}]获取回收站列表, 页码[{}], 每页[{}]", userId, query.getPageNum(), query.getPageSize());
        return collectionItemService.getRecycleBin(userId, query);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取收藏项详情", description = "获取指定收藏项的详细信息，自动增加访问次数。详情返回同样包含 noteId、relatedNoteId、noteSourceLabel 字段。")
    public R getItemDetail(@RequestAttribute("userId") Long userId,
                           @PathVariable Long id) {
        log.info("用户[{}]获取收藏项详情[{}]", userId, id);
        return collectionItemService.getItemDetail(userId, id);
    }

    @GetMapping("/url-exists")
    @Operation(summary = "检查链接是否已收藏", description = "按当前用户和链接地址检查收藏项是否已存在")
    public R checkUrlExists(@RequestAttribute("userId") Long userId,
                            @Parameter(description = "待检查的链接地址") @RequestParam String url) {
        log.info("用户[{}]检查链接是否已收藏: {}", userId, url);
        return collectionItemService.checkUrlExists(userId, url);
    }

    @PostMapping("/url-metadata")
    @Operation(summary = "解析链接元信息", description = "抓取网页标题、摘要、缩略图和来源信息，用于快捷收藏自动回填")
    public R parseUrlMetadata(@RequestAttribute("userId") Long userId,
                              @Valid @RequestBody UrlMetadataRequestDTO dto) {
        log.info("用户[{}]解析链接元信息: {}", userId, dto.getUrl());
        return collectionItemService.parseUrlMetadata(userId, dto);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定收藏项标记为已读")
    public R markAsRead(@RequestAttribute("userId") Long userId,
                        @PathVariable Long id) {
        log.info("用户[{}]标记收藏项[{}]为已读", userId, id);
        return collectionItemService.markAsRead(userId, id);
    }

    @PutMapping("/{id}/digest-status")
    @Operation(summary = "更新消化状态", description = "更新指定收藏项的消化状态")
    public R updateDigestStatus(@RequestAttribute("userId") Long userId,
                                @PathVariable Long id,
                                @RequestBody Map<String, String> body) {
        String status = body.get("digestStatus") != null ? body.get("digestStatus") : body.get("status");
        log.info("用户[{}]更新收藏项[{}]消化状态为[{}]", userId, id, status);
        return collectionItemService.updateDigestStatus(userId, id, status);
    }

    @PutMapping("/{id}/study-progress")
    @Operation(summary = "更新学习进度", description = "更新指定收藏项的学习进度")
    public R updateStudyProgress(@RequestAttribute("userId") Long userId,
                                 @PathVariable Long id,
                                 @RequestBody Map<String, String> body) {
        String progress = body.get("progress");
        log.info("用户[{}]更新收藏项[{}]学习进度为[{}]", userId, id, progress);
        return collectionItemService.updateStudyProgress(userId, id, progress);
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "移动收藏项", description = "将收藏项移动到其他收藏集")
    public R moveItem(@RequestAttribute("userId") Long userId,
                      @PathVariable Long id,
                      @RequestBody Map<String, Long> body) {
        Long targetCollectionId = body.get("targetCollectionId");
        log.info("用户[{}]移动收藏项[{}]到收藏集[{}]", userId, id, targetCollectionId);
        return collectionItemService.moveItem(userId, id, targetCollectionId);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "获取当前用户的收藏项统计数据")
    public R getStatistics(@RequestAttribute("userId") Long userId) {
        log.info("用户[{}]获取收藏项统计数据", userId);
        return collectionItemService.getStatistics(userId);
    }

    @PutMapping("/{id}/recover")
    @Operation(summary = "恢复删除项", description = "恢复被软删除的收藏项")
    public R recoverItem(@RequestAttribute("userId") Long userId,
                         @PathVariable Long id) {
        log.info("用户[{}]恢复收藏项[{}]", userId, id);
        return collectionItemService.recoverItem(userId, id);
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "永久删除", description = "从数据库中永久删除收藏项（不可恢复）")
    public R permanentDeleteItem(@RequestAttribute("userId") Long userId,
                                 @PathVariable Long id) {
        log.info("用户[{}]永久删除收藏项[{}]", userId, id);
        return collectionItemService.permanentDeleteItem(userId, id);
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "批量软删除", description = "批量软删除多个收藏项")
    public R batchDeleteItems(@RequestAttribute("userId") Long userId,
                              @Valid @RequestBody BatchOperationDTO batchDTO) {
        log.info("用户[{}]批量软删除收藏项, 数量[{}]", userId, batchDTO.getIds() != null ? batchDTO.getIds().size() : 0);
        return collectionItemService.batchDelete(userId, batchDTO);
    }

    @PostMapping("/batch/recover")
    @Operation(summary = "批量恢复", description = "批量恢复多个被软删除的收藏项")
    public R batchRecoverItems(@RequestAttribute("userId") Long userId,
                               @Valid @RequestBody BatchOperationDTO batchDTO) {
        log.info("用户[{}]批量恢复收藏项, 数量[{}]", userId, batchDTO.getIds() != null ? batchDTO.getIds().size() : 0);
        return collectionItemService.batchRecover(userId, batchDTO);
    }

    @PostMapping("/batch/permanent-delete")
    @Operation(summary = "批量永久删除", description = "从数据库中永久删除多个收藏项（不可恢复）")
    public R batchPermanentDeleteItems(@RequestAttribute("userId") Long userId,
                                       @Valid @RequestBody BatchOperationDTO batchDTO) {
        log.info("用户[{}]批量永久删除收藏项, 数量[{}]", userId, batchDTO.getIds() != null ? batchDTO.getIds().size() : 0);
        return collectionItemService.batchPermanentDelete(userId, batchDTO);
    }

    @PostMapping("/batch/move")
    @Operation(summary = "批量移动收藏项", description = "批量移动多个收藏项到指定收藏集")
    public R batchMoveItems(@RequestAttribute("userId") Long userId,
                            @Valid @RequestBody BatchMoveDTO batchMoveDTO) {
        log.info("用户[{}]批量移动收藏项, 数量[{}], 目标收藏集[{}]", 
                userId, 
                batchMoveDTO.getIds() != null ? batchMoveDTO.getIds().size() : 0,
                batchMoveDTO.getTargetCollectionId());
        return collectionItemService.batchMoveItems(userId, batchMoveDTO);
    }

    @PutMapping("/{id}/star")
    @Operation(summary = "切换星标", description = "切换收藏项的星标状态（星标/非星标）")
    public R toggleStar(@RequestAttribute("userId") Long userId,
                        @PathVariable Long id) {
        log.info("用户[{}]切换收藏项[{}]星标状态", userId, id);
        return collectionItemService.toggleStar(userId, id);
    }

    @PostMapping("/import/preview")
    @Operation(summary = "预览浏览器书签导入", description = "上传浏览器书签HTML内容，解析并返回预览数据")
    public R previewImport(@RequestAttribute("userId") Long userId,
                           @Valid @RequestBody BookmarkImportDTO dto) {
        log.info("用户[{}]预览浏览器书签导入", userId);
        return collectionItemService.previewImport(userId, dto);
    }

    @PostMapping("/import/execute")
    @Operation(summary = "执行浏览器书签导入", description = "确认导入浏览器书签，将预览数据落库")
    public R executeImport(@RequestAttribute("userId") Long userId,
                           @Valid @RequestBody BookmarkImportDTO dto) {
        log.info("用户[{}]执行浏览器书签导入", userId);
        return collectionItemService.executeImport(userId, dto);
    }

    @PutMapping("/{id}/remind")
    @Operation(summary = "设置学习提醒", description = "为指定收藏项设置学习提醒时间")
    public R setRemind(@RequestAttribute("userId") Long userId,
                       @PathVariable Long id,
                       @RequestBody Map<String, String> body) {
        String remindAt = body.get("remindAt");
        log.info("用户[{}]设置收藏项[{}]提醒时间[{}]", userId, id, remindAt);
        java.time.LocalDateTime remindTime = null;
        if (remindAt != null && !remindAt.isEmpty()) {
            remindTime = java.time.LocalDateTime.parse(remindAt, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return collectionItemService.setRemind(userId, id, remindTime);
    }

    @DeleteMapping("/{id}/remind")
    @Operation(summary = "取消学习提醒", description = "取消指定收藏项的学习提醒")
    public R cancelRemind(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id) {
        log.info("用户[{}]取消收藏项[{}]提醒", userId, id);
        return collectionItemService.cancelRemind(userId, id);
    }

    @GetMapping("/{id}/notes")
    @Operation(summary = "查询关联笔记列表", description = "获取指定收藏项的所有关联笔记（闭环功能）")
    public R getRelatedNotes(@RequestAttribute("userId") Long userId,
                             @PathVariable Long id) {
        log.info("用户[{}]查询收藏项[{}]的关联笔记", userId, id);
        return collectionItemService.getRelatedNotes(userId, id);
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "获取公开收藏项详情", description = "获取指定公开收藏项的详细信息，无需登录，仅返回isPublic=1的收藏项")
    public R getPublicItemDetail(@PathVariable Long id) {
        log.info("访问公开收藏项详情[{}]", id);
        return collectionItemService.getPublicItemDetail(id);
    }

    @PutMapping("/{id}/category")
    @Operation(summary = "设置收藏项分类", description = "为指定收藏项设置分类")
    public R setCategory(@RequestAttribute("userId") Long userId,
                         @PathVariable Long id,
                         @RequestBody Map<String, Long> body) {
        Long categoryId = body.get("categoryId");
        log.info("用户[]设置收藏项[{}]分类为[{}]", userId, id, categoryId);
        return collectionItemService.setCategory(userId, id, categoryId);
    }

    @PostMapping("/batch/category")
    @Operation(summary = "批量设置分类", description = "批量为多个收藏项设置分类")
    public R batchSetCategory(@RequestAttribute("userId") Long userId,
                              @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        java.util.List<Long> ids = (java.util.List<Long>) body.get("ids");
        Long categoryId = body.get("categoryId") != null ? 
            ((Number) body.get("categoryId")).longValue() : null;
        log.info("用户[{}]批量设置收藏项分类, 数量[{}], 分类[{}]", userId, ids != null ? ids.size() : 0, categoryId);
        return collectionItemService.batchSetCategory(userId, ids, categoryId);
    }
}
