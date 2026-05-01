package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.CollectionItemTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollectionItemTagMapper extends BaseMapper<CollectionItemTag> {

    /**
     * 根据标签ID删除关联记录
     */
    @Delete("DELETE FROM collection_item_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 根据收藏项ID删除关联记录
     */
    @Delete("DELETE FROM collection_item_tag WHERE collection_item_id = #{collectionItemId}")
    int deleteByCollectionItemId(@Param("collectionItemId") Long collectionItemId);

    /**
     * 根据标签ID查询关联记录数量
     */
    @Select("SELECT COUNT(*) FROM collection_item_tag WHERE tag_id = #{tagId}")
    int countByTagId(@Param("tagId") Long tagId);

    /**
     * 根据收藏项ID查询关联记录数量
     */
    @Select("SELECT COUNT(*) FROM collection_item_tag WHERE collection_item_id = #{collectionItemId}")
    int countByCollectionItemId(@Param("collectionItemId") Long collectionItemId);
}
