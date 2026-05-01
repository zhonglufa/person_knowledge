package com.qst.lm.dto.collect;

import com.qst.lm.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 收藏项查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CollectionItemQueryDTO extends PageDTO {

    /**
     * 筛选收藏集
     */
    private Long collectionId;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 来源类型
     */
    private Integer sourceType;

    /**
     * 消化状态
     */
    private String digestStatus;

    /**
     * 学习进度
     */
    private String studyProgress;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签筛选
     */
    private Long tagId;

    /**
     * 多标签筛选
     */
    private List<Long> tagIds;

    /**
     * 开始日期，格式 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期，格式 yyyy-MM-dd
     */
    private String endDate;

    /**
     * 已读状态
     */
    private Integer isRead;

    /**
     * 排序字段(createdAt/updatedAt/visitCount)
     */
    private String sortBy;

    /**
     * 排序方式(asc/desc)
     */
    private String sortOrder;
}
