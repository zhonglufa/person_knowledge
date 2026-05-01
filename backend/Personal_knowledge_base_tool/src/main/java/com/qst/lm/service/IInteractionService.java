package com.qst.lm.service;

import com.qst.lm.common.R;

import java.time.LocalDateTime;

/**
 * 互动服务接口
 * 提供点赞、评论、收藏等互动功能
 */
public interface IInteractionService {

    /**
     * 点赞
     *
     * @param userId 用户ID
     * @param dto    点赞请求参数
     * @return 操作结果
     */
    R like(Long userId, Object dto);

    /**
     * 取消点赞
     *
     * @param userId 用户ID
     * @param dto    取消点赞请求参数
     * @return 操作结果
     */
    R unlike(Long userId, Object dto);

    /**
     * 发表评论
     *
     * @param userId 用户ID
     * @param dto    评论请求参数
     * @return 操作结果
     */
    R comment(Long userId, Object dto);

    /**
     * 获取评论列表
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @param parentId   父评论ID(查询回复时为评论ID,查询一级评论时为null)
     * @param page       页码
     * @param size       每页大小
     * @return 评论列表
     */
    R getCommentList(Long targetId, String targetType, Long parentId, Integer page, Integer size);

    /**
     * 收藏内容
     *
     * @param userId 用户ID
     * @param dto    收藏请求参数
     * @return 操作结果
     */
    R collectContent(Long userId, Object dto);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param dto    取消收藏请求参数
     * @return 操作结果
     */
    R uncollectContent(Long userId, Object dto);

    /**
     * 获取点赞数
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 点赞数
     */
    R getLikeCount(Long targetId, String targetType);

    /**
     * 获取收藏数
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    R getCollectCount(Long targetId, String targetType);

    /**
     * 检查点赞状态
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 点赞状态
     */
    R checkLikeStatus(Long userId, Long targetId, String targetType);

    /**
     * 检查收藏状态
     *
     * @param userId     用户ID
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 收藏状态
     */
    R checkCollectStatus(Long userId, Long targetId, String targetType);
}
