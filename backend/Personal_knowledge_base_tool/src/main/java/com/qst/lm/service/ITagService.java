package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.tag.TagDTO;
import com.qst.lm.dto.tag.TagMergeDTO;

import java.util.List;

/**
 * 标签管理服务接口
 */
public interface ITagService {

    /**
     * 创建标签
     *
     * @param userId 用户ID
     * @param dto 标签信息
     * @return 操作结果
     */
    R createTag(Long userId, TagDTO dto);

    /**
     * 更新标签
     *
     * @param userId 用户ID
     * @param id  标签ID
     * @param dto 标签信息
     * @return 操作结果
     */
    R updateTag(Long userId, Long id, TagDTO dto);

    /**
     * 删除标签
     *
     * @param userId 用户ID
     * @param id 标签ID
     * @return 操作结果
     */
    R deleteTag(Long userId, Long id);

    /**
     * 获取用户的所有标签
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    R getTagList(Long userId);

    /**
     * 获取用户使用过的标签
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    R getUserTags(Long userId);

    /**
     * 给收藏项绑定标签
     *
     * @param userId 用户ID
     * @param itemId 收藏项ID
     * @param tagIds 标签ID列表
     * @return 操作结果
     */
    R bindTagToItem(Long userId, Long itemId, List<Long> tagIds);

    /**
     * 解绑标签
     *
     * @param userId 用户ID
     * @param itemId 收藏项ID
     * @param tagId  标签ID
     * @return 操作结果
     */
    R unbindTagFromItem(Long userId, Long itemId, Long tagId);

    /**
     * 获取收藏项的标签
     *
     * @param userId 用户ID
     * @param itemId 收藏项ID
     * @return 标签列表
     */
    R getItemTags(Long userId, Long itemId);

    /**
     * 获取标签统计
     *
     * @param userId 用户ID
     * @return 标签统计信息
     */
    R getStatistics(Long userId);

    /**
     * 批量操作标签
     *
     * @param userId  用户ID
     * @param tagIds  标签ID列表
     * @param operation 操作类型(delete/merge)
     * @return 操作结果
     */
    R batchOperate(Long userId, List<Long> tagIds, String operation);

    /**
     * 获取热门标签
     *
     * @param userId 用户ID
     * @param limit  数量限制
     * @return 热门标签列表
     */
    R getPopular(Long userId, Integer limit);

    /**
     * 获取标签云
     *
     * @param userId 用户ID
     * @return 标签云数据
     */
    R getCloud(Long userId);

    /**
     * 合并标签
     *
     * @param userId 用户ID
     * @param dto    合并参数
     * @return 操作结果
     */
    R mergeTags(Long userId, TagMergeDTO dto);
}
