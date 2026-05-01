package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分类Mapper接口
 * 继承MyBatis-Plus的BaseMapper提供基础CRUD操作
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 根据父分类ID查询子分类数量
     */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId} AND deleted = 0")
    int countChildren(@Param("parentId") Long parentId);

    /**
     * 批量更新分类的父分类ID为null
     */
    @Update("UPDATE category SET parent_id = NULL, updated_at = NOW() WHERE parent_id = #{parentId} AND deleted = 0")
    int clearParentReference(@Param("parentId") Long parentId);

    /**
     * 获取分类的完整路径（递归查询）
     */
    @Select("WITH RECURSIVE category_path AS (" +
            "SELECT id, name, parent_id, 0 as level FROM category WHERE id = #{categoryId} AND deleted = 0 " +
            "UNION ALL " +
            "SELECT c.id, c.name, c.parent_id, cp.level + 1 " +
            "FROM category c " +
            "JOIN category_path cp ON c.id = cp.parent_id " +
            "WHERE c.deleted = 0" +
            ") " +
            "SELECT id, name, parent_id, level FROM category_path ORDER BY level DESC")
    java.util.List<Category> getCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 按用户ID查询所有分类（XML实现，用于构建分类树）
     */
    List<Category> selectByUserId(@Param("userId") Long userId);
}
