package com.qst.lm.dto.collect;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 导入预览结果DTO
 */
@Data
public class ImportPreviewResultDTO {

    /**
     * 文件夹结构
     */
    private List<Map<String, Object>> folders;

    /**
     * 收藏项列表
     */
    private List<Map<String, Object>> items;

    /**
     * 总数量
     */
    private Integer totalCount;
}
