package com.qst.lm.dto.collection;

import com.qst.lm.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏集查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CollectionQueryDTO extends PageDTO {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 筛选公开状态 (0=私有, 1=公开)
     */
    private Integer isPublic;
}
