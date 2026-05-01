package com.qst.lm.dto.dashboard;

import lombok.Data;

/**
 * 后台首页统计数据DTO
 */
@Data
public class DashboardStatisticsDTO {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 今日新增用户
     */
    private Long todayNewUsers;

    /**
     * 总收藏集数
     */
    private Long totalCollections;

    /**
     * 总收藏项数
     */
    private Long totalCollectionItems;

    /**
     * 总笔记数
     */
    private Long totalNotes;

    /**
     * 总评论数
     */
    private Long totalComments;

    /**
     * 禁用用户数
     */
    private Long disabledUsers;

    /**
     * 待审核内容数
     */
    private Long pendingContent;
}
