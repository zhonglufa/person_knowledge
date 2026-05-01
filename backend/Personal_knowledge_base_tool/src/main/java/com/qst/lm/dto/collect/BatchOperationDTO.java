package com.qst.lm.dto.collect;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求DTO
 */
@Data
public class BatchOperationDTO {

    /**
     * ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
