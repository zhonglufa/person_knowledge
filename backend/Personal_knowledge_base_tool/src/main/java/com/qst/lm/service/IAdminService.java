package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.admin.AdminCreateUserDTO;
import com.qst.lm.dto.admin.AdminEditUserDTO;
import com.qst.lm.dto.announcement.AnnouncementDTO;

/**
 * 后台管理服务接口
 */
public interface IAdminService {

    /**
     * 获取用户列表（分页）
     *
     * @param page    分页参数
     * @param keyword 搜索关键词
     * @param role    角色筛选
     * @return 用户列表
     */
    R getUserList(PageDTO page, String keyword, String role);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    R disableUser(Long userId);

    /**
     * 解封用户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    R enableUser(Long userId);

    /**
     * 获取公开内容列表
     *
     * @param page        分页参数
     * @param contentType 内容类型（collection/note）
     * @return 内容列表
     */
    R getPublicContent(PageDTO page, String contentType);

    /**
     * 内容下架（设置为非公开）
     *
     * @param contentType 内容类型
     * @param contentId   内容ID
     * @return 操作结果
     */
    R takeDownContent(String contentType, Long contentId);

    /**
     * 后台首页统计
     *
     * @return 统计数据
     */
    R getDashboard();

    /**
     * 后台新增用户（管理员创建，无需验证码）
     *
     * @param dto 用户信息（用户名、邮箱、密码、角色）
     * @return 操作结果
     */
    R createUser(AdminCreateUserDTO dto);

    /**
     * 后台编辑用户
     *
     * @param id  用户ID
     * @param dto 用户信息
     * @return 操作结果
     */
    R updateUser(Long id, AdminEditUserDTO dto);

    /**
     * 创建公告
     *
     * @param adminId 管理员ID
     * @param dto     公告信息
     * @return 操作结果
     */
    R createAnnouncement(Long adminId, AnnouncementDTO dto);

    /**
     * 更新公告
     *
     * @param adminId 管理员ID
     * @param id      公告ID
     * @param dto     公告信息
     * @return 操作结果
     */
    R updateAnnouncement(Long adminId, Long id, AnnouncementDTO dto);

    /**
     * 下架公告（修改状态为已下架）
     *
     * @param adminId 管理员ID
     * @param id      公告ID
     * @return 操作结果
     */
    R takeDownAnnouncement(Long adminId, Long id);

    /**
     * 删除公告（逻辑删除）
     *
     * @param adminId 管理员ID
     * @param id      公告ID
     * @return 操作结果
     */
    R deleteAnnouncement(Long adminId, Long id);

    /**
     * 获取公告列表（分页，支持状态和类型筛选）
     *
     * @param page   页码
     * @param size   每页大小
     * @param status 公告状态（0=草稿,1=已发布,2=已下架,3=已过期），为null时不筛选
     * @param type   公告类型（system=系统公告,activity=活动通知,maintenance=维护通知），为null时不筛选
     * @return 公告列表
     */
    R getAnnouncementList(Integer page, Integer size, Integer status, String type);

    /**
     * 恢复已下架内容
     *
     * @param contentType 内容类型
     * @param contentId   内容ID
     * @param adminId     管理员ID
     * @return 操作结果
     */
    R restoreContent(String contentType, Long contentId, Long adminId);

    /**
     * 获取内容操作日志
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param operationType 操作类型
     * @param targetType    目标类型
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return 操作日志列表
     */
    R getContentAuditLogs(Integer pageNum, Integer pageSize, String operationType, String targetType, String startTime, String endTime);

    /**
     * 发布公告（触发全站通知）
     *
     * @param adminId        管理员ID
     * @param announcementId 公告ID
     * @return 操作结果
     */
    R publishAnnouncement(Long adminId, Long announcementId);

    /**
     * 获取公告触达统计
     *
     * @param announcementId 公告ID
     * @return 统计数据
     */
    R getAnnouncementStatistics(Long announcementId);

    /**
     * 获取仪表盘趋势数据
     *
     * @param days     天数
     * @param dataType 数据类型(users/content/interactions)
     * @return 趋势数据
     */
    R getDashboardTrends(Integer days, String dataType);

    /**
     * 获取系统健康状态
     *
     * @return 健康状态
     */
    R getDashboardHealth();

    // ==================== 批量操作接口 ====================

    /**
     * 批量禁用用户
     *
     * @param adminIds 管理员ID
     * @param userIds  用户ID列表
     * @param reason   禁用原因
     * @return 操作结果
     */
    R batchDisableUsers(Long adminIds, java.util.List<Long> userIds, String reason);

    /**
     * 批量启用用户
     *
     * @param adminIds 管理员ID
     * @param userIds  用户ID列表
     * @return 操作结果
     */
    R batchEnableUsers(Long adminIds, java.util.List<Long> userIds);

    /**
     * 批量下架内容
     *
     * @param adminId     管理员ID
     * @param contentType 内容类型
     * @param contentIds  内容ID列表
     * @param reason      违规原因
     * @return 操作结果
     */
    R batchTakeDownContent(Long adminId, String contentType, java.util.List<Long> contentIds, String reason);

    /**
     * 批量恢复内容
     *
     * @param adminId     管理员ID
     * @param contentType 内容类型
     * @param contentIds  内容ID列表
     * @return 操作结果
     */
    R batchRestoreContent(Long adminId, String contentType, java.util.List<Long> contentIds);

    // ==================== 用户导出接口 ====================

    /**
     * 导出用户列表（返回Excel文件数据）
     *
     * @param keyword 搜索关键词
     * @param role    角色筛选
     * @return Excel文件字节数组
     */
    byte[] exportUsersToExcel(String keyword, String role);

    // ==================== 公告增强接口 ====================

    /**
     * 定时发布公告
     *
     * @param adminId     管理员ID
     * @param announcementId 公告ID
     * @param scheduledAt 定时发布时间
     * @return 操作结果
     */
    R scheduleAnnouncement(Long adminId, Long announcementId, java.time.LocalDateTime scheduledAt);

    /**
     * 获取公告模板列表
     *
     * @return 模板列表
     */
    R getAnnouncementTemplates();

    /**
     * 创建公告模板
     *
     * @param adminId 管理员ID
     * @param name    模板名称
     * @param content 模板内容
     * @param type    模板类型
     * @return 操作结果
     */
    R createAnnouncementTemplate(Long adminId, String name, String content, String type);

    /**
     * 删除公告模板
     *
     * @param adminId     管理员ID
     * @param templateId 模板ID
     * @return 操作结果
     */
    R deleteAnnouncementTemplate(Long adminId, Long templateId);
}
