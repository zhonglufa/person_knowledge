package com.qst.lm.dto.search;

import com.qst.lm.dto.PageDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 搜索请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchDTO extends PageDTO {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 搜索范围：all-全部, collection-收藏集, item-收藏项, note-笔记
     */
    private String searchType = "all";
}
