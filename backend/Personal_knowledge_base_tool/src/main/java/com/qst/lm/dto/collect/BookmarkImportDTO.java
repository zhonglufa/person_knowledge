package com.qst.lm.dto.collect;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 浏览器收藏夹导入请求DTO
 */
@Data
public class BookmarkImportDTO {

    /**
     * HTML文件内容(Base64编码)
     */
    @NotBlank(message = "HTML内容不能为空")
    private String htmlContent;

    /**
     * 目标收藏集ID(可选,不指定则自动创建)
     */
    private Long targetCollectionId;
}
