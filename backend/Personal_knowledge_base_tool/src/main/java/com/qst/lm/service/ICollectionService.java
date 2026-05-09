package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.collection.CollectionDTO;
import com.qst.lm.dto.collection.CollectionQueryDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 收藏集服务接口
 */
public interface ICollectionService {

    /**
     * 创建收藏集
     *
     * @param userId 当前用户ID
     * @param dto    收藏集信息
     * @return 操作结果
     */
    R createCollection(Long userId, CollectionDTO dto);

    /**
     * 更新收藏集
     *
     * @param userId 当前用户ID
     * @param id     收藏集ID
     * @param dto    收藏集信息
     * @return 操作结果
     */
    R updateCollection(Long userId, Long id, CollectionDTO dto);

    /**
     * 删除收藏集（逻辑删除）
     *
     * @param userId 当前用户ID
     * @param id     收藏集ID
     * @return 操作结果
     */
    R deleteCollection(Long userId, Long id);

    /**
     * 获取当前用户的收藏集列表（分页）
     *
     * @param userId 当前用户ID
     * @param query  查询参数
     * @return 分页结果
     */
    R getCollectionList(Long userId, CollectionQueryDTO query);

    /**
     * 获取收藏集详情
     *
     * @param userId 当前用户ID
     * @param id     收藏集ID
     * @return 收藏集详情
     */
    R getCollectionDetail(Long userId, Long id);

    /**
     * 获取公开收藏集列表
     *
     * @param query 查询参数
     * @return 分页结果
     */
    R getPublicCollections(CollectionQueryDTO query);

    /**
     * 获取推荐收藏集
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 推荐收藏集列表
     */
    R getRecommended(Long userId, Integer page, Integer size);

    /**
     * 获取收藏集内的收藏项
     *
     * @param userId       用户ID
     * @param collectionId 收藏集ID
     * @param page         页码
     * @param size         每页大小
     * @return 收藏项列表
     */
    R getCollectionItems(Long userId, Long collectionId, Integer page, Integer size);

    /**
     * 上传收藏集封面图片
     *
     * @param userId 用户ID
     * @param file   图片文件
     * @return 图片URL
     */
    R uploadCoverImage(Long userId, MultipartFile file);
}
