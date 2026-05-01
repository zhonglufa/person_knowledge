package com.qst.lm.dto.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞请求DTO
 */
@Data
public class LikeDTO {

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
}
