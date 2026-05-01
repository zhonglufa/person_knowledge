package com.qst.lm.dto;

import lombok.Data;

/**
 * 分页请求DTO
 */
@Data
public class PageDTO {

    /**
     * 页码，默认第1页
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10条
     */
    private Integer pageSize = 10;
}
