package com.qst.lm.dto.category;

import lombok.Data;

/**
 * 分类移动请求DTO
 */
@Data
public class CategoryMoveDTO {

    /**
     * 目标父分类ID(移动到这个父分类下,为null表示移动到根级)
     */
    private Long targetParentId;
}
