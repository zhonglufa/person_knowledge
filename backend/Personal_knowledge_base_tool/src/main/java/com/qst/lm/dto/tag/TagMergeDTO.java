package com.qst.lm.dto.tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 标签合并请求DTO
 */
@Data
public class TagMergeDTO {

    /**
     * 源标签ID列表(被合并的标签)
     */
    @NotEmpty(message = "源标签ID列表不能为空")
    private List<Long> sourceTagIds;

    /**
     * 目标标签ID(合并后的标签)
     */
    @NotNull(message = "目标标签ID不能为空")
    private Long targetTagId;
}
