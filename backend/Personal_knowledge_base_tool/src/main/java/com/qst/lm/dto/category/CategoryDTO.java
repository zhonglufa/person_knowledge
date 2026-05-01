package com.qst.lm.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类数据传输对象
 */
@Data
public class CategoryDTO {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 父分类ID（null或0表示一级分类）
     */
    private Long parentId;
}
