package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qst.lm.pojo.InteractionLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 互动点赞Mapper接口
 */
@Mapper
public interface InteractionLikeMapper extends BaseMapper<InteractionLike> {

    /**
     * 统计目标的点赞数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 点赞数
     */
    @Select("SELECT COUNT(*) FROM interaction_like WHERE target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0")
    Long countByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 检查用户是否已点赞
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 点赞记录
     */
    @Select("SELECT * FROM interaction_like WHERE user_id = #{userId} AND target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0 LIMIT 1")
    InteractionLike existsByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);
}
