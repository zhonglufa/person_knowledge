package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.tag.TagDTO;
import com.qst.lm.dto.tag.TagMergeDTO;
import com.qst.lm.service.ITagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/tags")
@Tag(name = "标签管理模块", description = "标签相关接口")
public class TagController {

    private final ITagService tagService;

    public TagController(ITagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 创建标签
     */
    @PostMapping
    @Operation(summary = "创建标签", description = "创建一个新的标签")
    public R createTag(@RequestAttribute Long userId,
                        @Valid @RequestBody TagDTO dto) {
        log.info("用户[{}]创建标签: {}", userId, dto.getName());
        return tagService.createTag(userId, dto);
    }

    /**
     * 编辑标签
     */
    @PutMapping("/{id}")
    @Operation(summary = "编辑标签", description = "编辑指定标签信息")
    public R updateTag(@RequestAttribute Long userId,
                       @PathVariable Long id,
                       @Valid @RequestBody TagDTO dto) {
        log.info("用户[{}]编辑标签[{}]", userId, id);
        return tagService.updateTag(userId, id, dto);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签", description = "删除指定标签")
    public R deleteTag(@RequestAttribute Long userId,
                        @PathVariable Long id) {
        log.info("用户[{}]删除标签[{}]", userId, id);
        return tagService.deleteTag(userId, id);
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有标签", description = "获取所有标签列表")
    public R getTagList(@RequestAttribute Long userId) {
        log.info("用户[{}]获取所有标签", userId);
        return tagService.getTagList(userId);
    }

    /**
     * 获取用户使用过的标签
     */
    @GetMapping("/user-tags")
    @Operation(summary = "获取用户标签", description = "获取当前用户使用过的标签")
    public R getUserTags(@RequestAttribute Long userId) {
        log.info("用户[{}]获取使用过的标签", userId);
        return tagService.getUserTags(userId);
    }

    /**
     * 绑定标签到收藏项
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定标签", description = "给收藏项绑定标签")
    public R bindTagToItem(@RequestAttribute Long userId,
                           @RequestBody Map<String, Object> body) {
        Long itemId = Long.valueOf(body.get("itemId").toString());
        List<Long> tagIds = ((List<Number>) body.get("tagIds")).stream()
                .map(Number::longValue)
                .toList();
        log.info("用户[{}]绑定标签到收藏项[{}], 标签数量[{}]", userId, itemId, tagIds.size());
        return tagService.bindTagToItem(userId, itemId, tagIds);
    }

    /**
     * 解绑标签
     */
    @DeleteMapping("/unbind")
    @Operation(summary = "解绑标签", description = "从收藏项解绑标签")
    public R unbindTagFromItem(@RequestAttribute Long userId,
                               @RequestParam Long itemId,
                               @RequestParam Long tagId) {
        log.info("用户[{}]从收藏项[{}]解绑标签[{}]", userId, itemId, tagId);
        return tagService.unbindTagFromItem(userId, itemId, tagId);
    }

    /**
     * 获取收藏项的标签
     */
    @GetMapping("/item/{itemId}")
    @Operation(summary = "获取收藏项标签", description = "获取指定收藏项的所有标签")
    public R getItemTags(@RequestAttribute Long userId,
                         @PathVariable Long itemId) {
        log.info("用户[{}]获取收藏项[{}]的标签", userId, itemId);
        return tagService.getItemTags(userId, itemId);
    }

    // ==================== 扩展接口 ====================

    /**
     * 获取标签统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取标签统计", description = "获取标签的统计信息，包括每个标签关联的收藏项数量等")
    public R getTagStatistics(@RequestAttribute Long userId) {
        log.info("用户[{}]获取标签统计", userId);
        return tagService.getStatistics(userId);
    }

    /**
     * 批量操作标签
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作标签", description = "批量创建、更新或删除标签")
    public R batchOperateTags(@RequestAttribute Long userId,
                              @RequestBody Map<String, Object> body) {
        String operation = (String) body.get("operation");
        @SuppressWarnings("unchecked")
        List<Number> tagIdNumbers = (List<Number>) body.get("tagIds");
        List<Long> tagIds = tagIdNumbers != null
                ? tagIdNumbers.stream().map(Number::longValue).toList()
                : List.of();
        log.info("用户[{}]批量操作标签, 操作类型[{}], 标签数量[{}]", userId, operation, tagIds.size());
        return tagService.batchOperate(userId, tagIds, operation);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门标签", description = "获取使用频率最高的标签列表")
    public R getPopularTags(@RequestAttribute Long userId,
                           @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取热门标签, 限制[{}]", limit);
        return tagService.getPopular(userId, limit);
    }

    /**
     * 获取标签云
     */
    @GetMapping("/cloud")
    @Operation(summary = "获取标签云", description = "获取标签云数据，包含标签名称和使用频率")
    public R getTagCloud(@RequestAttribute Long userId) {
        log.info("用户[{}]获取标签云", userId);
        return tagService.getCloud(userId);
    }

    /**
     * 合并标签
     */
    @PostMapping("/merge")
    @Operation(summary = "合并标签", description = "将多个标签合并为一个标签，原标签关联的内容自动迁移")
    public R mergeTags(@RequestAttribute Long userId,
                       @Valid @RequestBody TagMergeDTO mergeDTO) {
        log.info("用户[{}]合并标签, 源标签[{}]个, 目标标签[{}]",
                userId,
                mergeDTO.getSourceTagIds() != null ? mergeDTO.getSourceTagIds().size() : 0,
                mergeDTO.getTargetTagId());
        return tagService.mergeTags(userId, mergeDTO);
    }
}
