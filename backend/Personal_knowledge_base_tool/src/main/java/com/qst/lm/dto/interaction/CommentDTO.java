package com.qst.lm.dto.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论请求DTO
 */
@Data
public class CommentDTO {

    /**
     * 目标ID
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 目标类型(note/collection_item/collection)
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 父评论ID(回复)
     */
    private Long parentId;
}
