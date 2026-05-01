package com.qst.lm.dto.collection;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收藏集创建/更新请求DTO
 */
@Data
public class CollectionDTO {

    /**
     * 收藏集名称
     */
    @NotBlank(message = "收藏集名称不能为空")
    private String name;

    /**
     * 收藏集描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 是否公开 (0=私有, 1=公开)，默认0
     */
    private Integer isPublic = 0;
}
