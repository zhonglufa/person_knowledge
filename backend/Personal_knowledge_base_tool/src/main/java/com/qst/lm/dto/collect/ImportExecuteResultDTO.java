package com.qst.lm.dto.collect;

import lombok.Data;

import java.util.List;

/**
 * 导入执行结果DTO
 */
@Data
public class ImportExecuteResultDTO {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 失败的项
     */
    private List<String> failedItems;
}
