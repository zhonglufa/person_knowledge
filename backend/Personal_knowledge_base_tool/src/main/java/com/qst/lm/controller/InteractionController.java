package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.interaction.CollectDTO;
import com.qst.lm.dto.interaction.CommentDTO;
import com.qst.lm.dto.interaction.LikeDTO;
import com.qst.lm.service.IInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 互动控制器
 * 提供点赞、评论、收藏等互动功能
 */
@Slf4j
@RestController
@RequestMapping("/interaction")
@Tag(name = "互动模块", description = "点赞、评论、收藏等互动相关接口")
public class InteractionController {

    private final IInteractionService interactionService;

    /**
     * 构造函数注入互动服务
     */
    public InteractionController(IInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    // ==================== 点赞相关接口 ====================

    /**
     * 点赞
     */
    @PostMapping("/like")
    @Operation(summary = "点赞", description = "对笔记、收藏项或收藏集进行点赞")
    public R like(
            @RequestAttribute Long userId,
            @Valid @RequestBody LikeDTO likeDTO) {
        log.info("用户[{}]点赞目标[{}], 类型[{}]", userId, likeDTO.getTargetId(), likeDTO.getTargetType());
        return interactionService.like(userId, likeDTO);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/like")
    @Operation(summary = "取消点赞", description = "取消对内容的点赞")
    public R unlike(
            @RequestAttribute Long userId,
            @Valid @RequestBody LikeDTO likeDTO) {
        log.info("用户[{}]取消点赞目标[{}], 类型[{}]", userId, likeDTO.getTargetId(), likeDTO.getTargetType());
        return interactionService.unlike(userId, likeDTO);
    }

    /**
     * 获取点赞数
     */
    @GetMapping("/like/count")
    @Operation(summary = "获取点赞数", description = "获取指定内容的点赞总数")
    public R getLikeCount(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型(note/collection_item/collection)") @RequestParam String targetType) {
        log.info("获取目标[{}]的点赞数, 类型[{}]", targetId, targetType);
        return interactionService.getLikeCount(targetId, targetType);
    }

    /**
     * 检查点赞状态
     */
    @GetMapping("/like/check")
    @Operation(summary = "检查点赞状态", description = "检查当前用户是否已点赞指定内容")
    public R checkLikeStatus(
            @RequestAttribute Long userId,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型(note/collection_item/collection)") @RequestParam String targetType) {
        log.info("用户[{}]检查目标[{}]的点赞状态, 类型[{}]", userId, targetId, targetType);
        return interactionService.checkLikeStatus(userId, targetId, targetType);
    }

    // ==================== 评论相关接口 ====================

    /**
     * 发表评论
     */
    @PostMapping("/comment")
    @Operation(summary = "发表评论", description = "对笔记、收藏项或收藏集发表评论或回复")
    public R addComment(
            @RequestAttribute Long userId,
            @Valid @RequestBody CommentDTO commentDTO) {
        log.info("用户[{}]发表评论到目标[{}], 类型[{}], 内容长度[{}]",
                userId, commentDTO.getTargetId(), commentDTO.getTargetType(),
                commentDTO.getContent() != null ? commentDTO.getContent().length() : 0);
        return interactionService.comment(userId, commentDTO);
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/comment/list")
    @Operation(summary = "获取评论列表", description = "获取指定内容的评论列表，支持分页")
    public R getCommentList(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型(note/collection_item/collection)") @RequestParam String targetType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("获取目标[{}]的评论列表, 类型[{}], 页码[{}], 每页[{}]",
                targetId, targetType, pageNum, pageSize);
        return interactionService.getCommentList(targetId, targetType, null, pageNum, pageSize);
    }

    // ==================== 收藏相关接口 ====================

    /**
     * 收藏内容
     */
    @PostMapping("/collect")
    @Operation(summary = "收藏内容", description = "收藏笔记或收藏集")
    public R collect(
            @RequestAttribute Long userId,
            @Valid @RequestBody CollectDTO collectDTO) {
        log.info("用户[{}]收藏目标[{}], 类型[{}]", userId, collectDTO.getTargetId(), collectDTO.getTargetType());
        return interactionService.collectContent(userId, collectDTO);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/collect")
    @Operation(summary = "取消收藏", description = "取消对内容的收藏")
    public R uncollect(
            @RequestAttribute Long userId,
            @Valid @RequestBody CollectDTO collectDTO) {
        log.info("用户[{}]取消收藏目标[{}], 类型[{}]", userId, collectDTO.getTargetId(), collectDTO.getTargetType());
        return interactionService.uncollectContent(userId, collectDTO);
    }

    /**
     * 获取收藏数
     */
    @GetMapping("/collect/count")
    @Operation(summary = "获取收藏数", description = "获取指定内容的收藏总数")
    public R getCollectCount(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型(note/collection)") @RequestParam String targetType) {
        log.info("获取目标[{}]的收藏数, 类型[{}]", targetId, targetType);
        return interactionService.getCollectCount(targetId, targetType);
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/collect/check")
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏指定内容")
    public R checkCollectStatus(
            @RequestAttribute Long userId,
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型(note/collection)") @RequestParam String targetType) {
        log.info("用户[{}]检查目标[{}]的收藏状态, 类型[{}]", userId, targetId, targetType);
        return interactionService.checkCollectStatus(userId, targetId, targetType);
    }
}
