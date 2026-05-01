package com.qst.lm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.pojo.InteractionComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 互动评论Mapper接口
 */
@Mapper
public interface InteractionCommentMapper extends BaseMapper<InteractionComment> {

    /**
     * 分页查询目标的评论列表(包含用户信息)
     * @param page 分页对象
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param parentId 父评论ID(查询回复)
     * @return 评论列表
     */
    IPage<InteractionComment> selectByTargetWithPage(Page<InteractionComment> page,
                                                      @Param("targetId") Long targetId,
                                                      @Param("targetType") String targetType,
                                                      @Param("parentId") Long parentId);

    /**
     * 统计目标的评论数
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 评论数
     */
    @Select("SELECT COUNT(*) FROM interaction_comment WHERE target_id = #{targetId} AND target_type = #{targetType} AND deleted = 0")
    Long countByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
}
