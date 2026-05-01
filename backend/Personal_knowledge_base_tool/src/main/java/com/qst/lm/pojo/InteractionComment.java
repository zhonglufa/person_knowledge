package com.qst.lm.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 互动评论实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interaction_comment")
public class InteractionComment extends BasePojo {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标类型(note/collection_item/collection)
     */
    private String targetType;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 父评论ID(回复)
     */
    private Long parentId;
}
