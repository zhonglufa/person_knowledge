package com.qst.lm.dto.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 互动收藏请求DTO
 */
@Data
public class CollectDTO {

    /**
     * 目标ID
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 目标类型(note/collection)
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;
}
