package com.qst.lm.dto.collect;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量移动收藏项请求DTO
 */
@Data
public class BatchMoveDTO {

    /**
     * 收藏项ID列表
     */
    @NotEmpty(message = "收藏项ID列表不能为空")
    private List<Long> ids;

    /**
     * 目标收藏集ID
     */
    @NotNull(message = "目标收藏集ID不能为空")
    private Long targetCollectionId;
}
