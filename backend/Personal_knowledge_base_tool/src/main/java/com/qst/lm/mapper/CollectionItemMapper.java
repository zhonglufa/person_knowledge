package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.pojo.CollectionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 收藏项Mapper接口
 */
@Mapper
public interface CollectionItemMapper extends BaseMapper<CollectionItem> {

    /**
     * 查询回收站数据（绕过逻辑删除）
     */
    @Select("SELECT * FROM collection_item WHERE user_id = #{userId} AND deleted = 1 ORDER BY created_at DESC")
    IPage<CollectionItem> selectRecycledItems(Page<CollectionItem> page, @Param("userId") Long userId);

    /**
     * 根据分类ID查询收藏项数量
     */
    @Select("SELECT COUNT(*) FROM collection_item WHERE category_id = #{categoryId} AND deleted = 0")
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据收藏集ID查询收藏项数量
     */
    @Select("SELECT COUNT(*) FROM collection_item WHERE collection_id = #{collectionId} AND deleted = 0")
    int countByCollectionId(@Param("collectionId") Long collectionId);

    /**
     * 批量更新收藏项的分类ID为null
     */
    @Update("UPDATE collection_item SET category_id = NULL, updated_at = NOW() WHERE category_id = #{categoryId} AND deleted = 0")
    int clearCategoryReference(@Param("categoryId") Long categoryId);

    /**
     * 批量更新收藏项的收藏集ID为null
     */
    @Update("UPDATE collection_item SET collection_id = NULL, updated_at = NOW() WHERE collection_id = #{collectionId} AND deleted = 0")
    int clearCollectionReference(@Param("collectionId") Long collectionId);

    /**
     * 收藏项带标签联合查询（XML实现）
     */
    List<Map<String, Object>> selectItemWithTags(
            @Param("userId") Long userId,
            @Param("collectionId") Long collectionId,
            @Param("keyword") String keyword,
            @Param("digestStatus") String digestStatus,
            @Param("studyProgress") String studyProgress,
            @Param("categoryId") Long categoryId,
            @Param("sourceType") Integer sourceType,
            @Param("isRead") Boolean isRead);

    /**
     * 收藏项统计查询（XML实现）
     */
    Map<String, Object> selectStatistics(@Param("userId") Long userId);
}
