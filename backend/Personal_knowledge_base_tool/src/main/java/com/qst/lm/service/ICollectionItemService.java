package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.collect.BatchMoveDTO;
import com.qst.lm.dto.collect.BatchOperationDTO;
import com.qst.lm.dto.collect.BookmarkImportDTO;
import com.qst.lm.dto.collect.CollectionItemDTO;
import com.qst.lm.dto.collect.CollectionItemQueryDTO;
import com.qst.lm.dto.collect.UrlMetadataRequestDTO;

import java.time.LocalDateTime;

/**
 * 收藏项服务接口
 */
public interface ICollectionItemService {

    /**
     * 创建收藏项
     */
    R createItem(Long userId, CollectionItemDTO dto);

    /**
     * 更新收藏项
     */
    R updateItem(Long userId, Long id, CollectionItemDTO dto);

    /**
     * 删除收藏项（逻辑删除）
     */
    R deleteItem(Long userId, Long id);

    /**
     * 获取收藏项列表（分页+筛选）
     */
    R getItemList(Long userId, CollectionItemQueryDTO query);

    /**
     * 获取回收站列表（分页+筛选）
     */
    R getRecycleBin(Long userId, CollectionItemQueryDTO query);

    /**
     * 获取收藏项详情（自动增加访问次数）
     */
    R getItemDetail(Long userId, Long id);

    /**
     * 检查链接是否已收藏
     */
    R checkUrlExists(Long userId, String url);

    /**
     * 解析链接元信息
     */
    R parseUrlMetadata(Long userId, UrlMetadataRequestDTO dto);

    /**
     * 标记已读
     */
    R markAsRead(Long userId, Long id);

    /**
     * 更新消化状态
     */
    R updateDigestStatus(Long userId, Long id, String status);

    /**
     * 更新学习进度
     */
    R updateStudyProgress(Long userId, Long id, String progress);

    /**
     * 移动收藏项到其他收藏集
     */
    R moveItem(Long userId, Long id, Long targetCollectionId);

    /**
     * 批量移动收藏项到其他收藏集
     */
    R batchMoveItems(Long userId, BatchMoveDTO dto);

    /**
     * 获取收藏项统计数据
     */
    R getStatistics(Long userId);

    /**
     * 预览浏览器导入
     */
    R previewImport(Long userId, BookmarkImportDTO dto);

    /**
     * 执行浏览器导入
     */
    R executeImport(Long userId, BookmarkImportDTO dto);

    /**
     * 恢复删除项
     */
    R recoverItem(Long userId, Long id);

    /**
     * 永久删除
     */
    R permanentDeleteItem(Long userId, Long id);

    /**
     * 批量软删除
     */
    R batchDelete(Long userId, BatchOperationDTO dto);

    /**
     * 批量恢复
     */
    R batchRecover(Long userId, BatchOperationDTO dto);

    /**
     * 批量永久删除
     */
    R batchPermanentDelete(Long userId, BatchOperationDTO dto);

    /**
     * 切换星标
     */
    R toggleStar(Long userId, Long id);

    /**
     * 设置学习提醒
     */
    R setRemind(Long userId, Long id, LocalDateTime remindAt);

    /**
     * 取消提醒
     */
    R cancelRemind(Long userId, Long id);

    /**
     * 获取收藏项关联的笔记列表（闭环功能）
     */
    R getRelatedNotes(Long userId, Long collectionItemId);

    /**
     * 获取公开收藏项详情（无需登录）
     */
    R getPublicItemDetail(Long id);

    /**
     * 设置收藏项分类
     */
    R setCategory(Long userId, Long id, Long categoryId);

    /**
     * 批量设置收藏项分类
     */
    R batchSetCategory(Long userId, java.util.List<Long> ids, Long categoryId);
}
