package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 收藏集Mapper接口
 */
@Mapper
public interface CollectionMapper extends BaseMapper<Collection> {

    /**
     * 逻辑删除收藏集 - 将deleted字段设置为1更新
     * @param id 收藏集ID
     * @return 影响的行数
     */
    @Update("UPDATE collection SET deleted = 1, updated_at = NOW() WHERE id = #{id} AND deleted = 0")
    int updateDeletedById(@Param("id") Long id);

    /**
     * 恢复收藏集 - 将deleted字段设置为0更新
     * @param id 收藏集ID
     * @return 影响的行数
     */
    @Update("UPDATE collection SET deleted = 0, updated_at = NOW() WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);
}
