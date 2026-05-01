package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.InteractionCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 互动收藏Mapper接口
 */
@Mapper
public interface InteractionCollectMapper extends BaseMapper<InteractionCollect> {

    /**
     * 检查用户是否已收藏
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏记录
     */
    @Select("SELECT * FROM interaction_collect WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0 LIMIT 1")
    InteractionCollect existsByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 统计目标的收藏数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    @Select("SELECT COUNT(*) FROM interaction_collect WHERE target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0")
    Long countByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
}
