package com.qst.lm.dto.admin;

import lombok.Data;

/**
 * 内容恢复参数DTO
 */
@Data
public class ContentRestoreDTO {

    /**
     * 内容类型: collection/note
     */
    private String contentType;

    /**
     * 内容ID
     */
    private Long contentId;
}
