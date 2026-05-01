package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.admin.AdminCreateUserDTO;
import com.qst.lm.dto.admin.AdminEditUserDTO;
import com.qst.lm.dto.announcement.AnnouncementDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.UserMapper;
import com.qst.lm.pojo.User;
import com.qst.lm.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "后台管理模块", description = "后台管理相关接口（仅管理员）")
public class AdminController {

    private final IAdminService adminService;
    private final UserMapper userMapper;

    public AdminController(IAdminService adminService, UserMapper userMapper) {
        this.adminService = adminService;
        this.userMapper = userMapper;
    }

    /**
     * 校验管理员权限
     */
    private void checkAdminRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"admin".equals(user.getRole())) {
            throw new BusinessException(403, "无权访问，需要管理员权限");
        }
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "获取用户列表（支持关键词搜索和角色筛选）")
    public R getUserList(@RequestAttribute Long userId,
                         PageDTO page,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String role) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取用户列表, 页码[{}], 每页[{}]", userId, page.getPageNum(), page.getPageSize());
        return adminService.getUserList(page, keyword, role);
    }

    /**
     * 禁用用户
     */
    @PutMapping("/users/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public R disableUser(@RequestAttribute Long userId, @PathVariable Long id) {
        checkAdminRole(userId);
        log.info("管理员[{}]禁用用户[{}]", userId, id);
        return adminService.disableUser(id);
    }

    /**
     * 解封用户
     */
    @PutMapping("/users/{id}/enable")
    @Operation(summary = "解封用户", description = "解封指定用户")
    public R enableUser(@RequestAttribute Long userId, @PathVariable Long id) {
        checkAdminRole(userId);
        log.info("管理员[{}]解封用户[{}]", userId, id);
        return adminService.enableUser(id);
    }

    /**
     * 获取公开内容列表
     */
    @GetMapping("/content")
    @Operation(summary = "公开内容列表", description = "获取公开内容列表（收藏集或笔记）")
    public R getPublicContent(@RequestAttribute Long userId,
                              PageDTO page,
                              @RequestParam String contentType) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取公开内容列表, 类型[{}], 页码[{}]", userId, contentType, page.getPageNum());
        return adminService.getPublicContent(page, contentType);
    }

    /**
     * 内容下架
     */
    @PutMapping("/content/take-down")
    @Operation(summary = "内容下架", description = "将指定公开内容下架（设为非公开）")
    public R takeDownContent(@RequestAttribute Long userId,
                             @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        String contentType = (String) params.get("contentType");
        Long contentId = Long.valueOf(params.get("contentId").toString());
        log.info("管理员[{}]下架内容, 类型[{}], ID[{}]", userId, contentType, contentId);
        return adminService.takeDownContent(contentType, contentId);
    }

    // ==================== 扩展接口 ====================

    /**
     * 后台首页统计
     */
    @GetMapping("/dashboard")
    @Operation(summary = "后台首页统计", description = "获取后台管理首页的统计数据，包括用户数、内容数、访问量等")
    public R getDashboard(@RequestAttribute Long userId) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取后台首页统计", userId);
        return adminService.getDashboard();
    }

    /**
     * 后台新增用户
     */
    @PostMapping("/users")
    @Operation(summary = "后台新增用户", description = "管理员在后台创建新用户")
    public R createUser(@RequestAttribute Long userId,
                        @Valid @RequestBody AdminCreateUserDTO dto) {
        checkAdminRole(userId);
        log.info("管理员[{}]创建新用户: {}", userId, dto.getUsername());
        return adminService.createUser(dto);
    }

    /**
     * 后台编辑用户
     */
    @PutMapping("/users/{id}")
    @Operation(summary = "后台编辑用户", description = "管理员编辑指定用户的信息")
    public R updateUser(@RequestAttribute Long userId,
                        @PathVariable Long id,
                        @Valid @RequestBody AdminEditUserDTO dto) {
        checkAdminRole(userId);
        log.info("管理员[{}]编辑用户[{}]", userId, id);
        return adminService.updateUser(id, dto);
    }

    /**
     * 创建公告
     */
    @PostMapping("/announcements")
    @Operation(summary = "创建公告", description = "创建新的系统公告")
    public R createAnnouncement(@RequestAttribute Long userId,
                                @Valid @RequestBody AnnouncementDTO dto) {
        checkAdminRole(userId);
        log.info("管理员[{}]创建公告: {}", userId, dto.getTitle());
        return adminService.createAnnouncement(userId, dto);
    }

    /**
     * 更新公告
     */
    @PutMapping("/announcements/{id}")
    @Operation(summary = "更新公告", description = "更新指定公告的信息")
    public R updateAnnouncement(@RequestAttribute Long userId,
                                @PathVariable Long id,
                                @Valid @RequestBody AnnouncementDTO dto) {
        checkAdminRole(userId);
        log.info("管理员[{}]更新公告[{}]", userId, id);
        return adminService.updateAnnouncement(userId, id, dto);
    }

    /**
     * 下架公告
     */
    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "下架公告", description = "下架指定公告，使其不再显示")
    public R deleteAnnouncement(@RequestAttribute Long userId,
                                @PathVariable Long id) {
        checkAdminRole(userId);
        log.info("管理员[{}]下架公告[{}]", userId, id);
        return adminService.deleteAnnouncement(userId, id);
    }

    /**
     * 获取公告列表
     */
    @GetMapping("/announcements/list")
    @Operation(summary = "获取公告列表", description = "获取公告列表，支持分页和状态筛选")
    public R getAnnouncementList(@RequestAttribute Long userId,
                                 PageDTO page,
                                 @Parameter(description = "公告状态") @RequestParam(required = false) Integer status) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取公告列表, 页码[{}], 每页[{}], 状态[{}]",
                userId, page.getPageNum(), page.getPageSize(), status);
        return adminService.getAnnouncementList(page.getPageNum(), page.getPageSize(), status);
    }

    // ==================== 新增增强接口 ====================

    /**
     * 恢复已下架内容
     */
    @PutMapping("/content/restore")
    @Operation(summary = "恢复内容", description = "恢复已下架的公开内容")
    public R restoreContent(@RequestAttribute Long userId,
                            @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        String contentType = (String) params.get("contentType");
        Long contentId = Long.valueOf(params.get("contentId").toString());
        log.info("管理员[{}]恢复内容, 类型[{}], ID[{}]", userId, contentType, contentId);
        return adminService.restoreContent(contentType, contentId, userId);
    }

    /**
     * 获取内容操作日志
     */
    @GetMapping("/content/audit-logs")
    @Operation(summary = "内容操作日志", description = "获取管理员对内容的操作日志")
    public R getContentAuditLogs(@RequestAttribute Long userId,
                                 PageDTO page,
                                 @RequestParam(required = false) String operationType,
                                 @RequestParam(required = false) String targetType,
                                 @RequestParam(required = false) String startTime,
                                 @RequestParam(required = false) String endTime) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取内容操作日志", userId);
        return adminService.getContentAuditLogs(page.getPageNum(), page.getPageSize(), operationType, targetType, startTime, endTime);
    }

    /**
     * 发布公告（触发全站通知）
     */
    @PostMapping("/announcements/{id}/publish")
    @Operation(summary = "发布公告", description = "将草稿状态的公告发布并推送给所有用户")
    public R publishAnnouncement(@RequestAttribute Long userId,
                                 @PathVariable Long id) {
        checkAdminRole(userId);
        log.info("管理员[{}]发布公告[{}]", userId, id);
        return adminService.publishAnnouncement(userId, id);
    }

    /**
     * 获取公告触达统计
     */
    @GetMapping("/announcements/{id}/statistics")
    @Operation(summary = "公告统计", description = "获取公告的触达统计数据")
    public R getAnnouncementStatistics(@RequestAttribute Long userId,
                                       @PathVariable Long id) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取公告[{}]统计", userId, id);
        return adminService.getAnnouncementStatistics(id);
    }

    /**
     * 获取仪表盘趋势数据
     */
    @GetMapping("/dashboard/trends")
    @Operation(summary = "趋势数据", description = "获取用户增长或内容发布的趋势数据")
    public R getDashboardTrends(@RequestAttribute Long userId,
                                @RequestParam(defaultValue = "30") Integer days,
                                @RequestParam(required = false) String dataType) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取趋势数据, 天数[{}], 类型[{}]", userId, days, dataType);
        return adminService.getDashboardTrends(days, dataType);
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/dashboard/health")
    @Operation(summary = "系统健康", description = "获取系统健康状态信息")
    public R getDashboardHealth(@RequestAttribute Long userId) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取系统健康状态", userId);
        return adminService.getDashboardHealth();
    }

    // ==================== 批量操作接口 ====================

    /**
     * 批量禁用用户
     */
    @PostMapping("/users/batch-disable")
    @Operation(summary = "批量禁用用户", description = "批量禁用多个用户账号")
    public R batchDisableUsers(@RequestAttribute Long userId,
                               @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        List<Long> userIds = ((List<?>) params.get("userIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        String reason = (String) params.get("reason");
        log.info("管理员[{}]批量禁用用户, 数量[{}]", userId, userIds.size());
        return adminService.batchDisableUsers(userId, userIds, reason);
    }

    /**
     * 批量启用用户
     */
    @PostMapping("/users/batch-enable")
    @Operation(summary = "批量启用用户", description = "批量启用多个用户账号")
    public R batchEnableUsers(@RequestAttribute Long userId,
                             @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        List<Long> userIds = ((List<?>) params.get("userIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        log.info("管理员[{}]批量启用用户, 数量[{}]", userId, userIds.size());
        return adminService.batchEnableUsers(userId, userIds);
    }

    /**
     * 导出用户列表
     */
    @GetMapping("/users/export")
    @Operation(summary = "导出用户列表", description = "导出用户列表为Excel/CSV文件")
    public org.springframework.http.HttpEntity<byte[]> exportUsers(@RequestAttribute Long userId,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) String role) {
        checkAdminRole(userId);
        log.info("管理员[{}]导出用户列表", userId);
        byte[] excelData = adminService.exportUsersToExcel(keyword, role);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users_export.csv");
        return new org.springframework.http.HttpEntity<>(excelData, headers);
    }

    /**
     * 批量下架内容
     */
    @PostMapping("/content/batch-take-down")
    @Operation(summary = "批量下架内容", description = "批量下架多个公开内容")
    public R batchTakeDownContent(@RequestAttribute Long userId,
                                  @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        String contentType = (String) params.get("contentType");
        List<Long> contentIds = ((List<?>) params.get("contentIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        String reason = (String) params.get("reason");
        log.info("管理员[{}]批量下架内容, 类型[{}], 数量[{}]", userId, contentType, contentIds.size());
        return adminService.batchTakeDownContent(userId, contentType, contentIds, reason);
    }

    /**
     * 批量恢复内容
     */
    @PostMapping("/content/batch-restore")
    @Operation(summary = "批量恢复内容", description = "批量恢复多个已下架内容")
    public R batchRestoreContent(@RequestAttribute Long userId,
                                @RequestBody Map<String, Object> params) {
        checkAdminRole(userId);
        String contentType = (String) params.get("contentType");
        List<Long> contentIds = ((List<?>) params.get("contentIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        log.info("管理员[{}]批量恢复内容, 类型[{}], 数量[{}]", userId, contentType, contentIds.size());
        return adminService.batchRestoreContent(userId, contentType, contentIds);
    }

    // ==================== 公告增强接口 ====================

    /**
     * 定时发布公告
     */
    @PostMapping("/announcements/{id}/schedule")
    @Operation(summary = "定时发布公告", description = "设置公告定时发布时间")
    public R scheduleAnnouncement(@RequestAttribute Long userId,
                                  @PathVariable Long id,
                                  @RequestBody Map<String, String> params) {
        checkAdminRole(userId);
        java.time.LocalDateTime scheduledAt = java.time.LocalDateTime.parse(params.get("scheduledAt"));
        log.info("管理员[{}]设置公告[{}]定时发布", userId, id);
        return adminService.scheduleAnnouncement(userId, id, scheduledAt);
    }

    /**
     * 获取公告模板列表
     */
    @GetMapping("/announcements/templates")
    @Operation(summary = "获取公告模板", description = "获取预设的公告模板列表")
    public R getAnnouncementTemplates(@RequestAttribute Long userId) {
        checkAdminRole(userId);
        log.info("管理员[{}]获取公告模板列表", userId);
        return adminService.getAnnouncementTemplates();
    }

    /**
     * 创建公告模板
     */
    @PostMapping("/announcements/templates")
    @Operation(summary = "创建公告模板", description = "创建自定义公告模板")
    public R createAnnouncementTemplate(@RequestAttribute Long userId,
                                         @RequestBody Map<String, String> params) {
        checkAdminRole(userId);
        String name = params.get("name");
        String content = params.get("content");
        String type = params.get("type");
        log.info("管理员[{}]创建公告模板[{}]", userId, name);
        return adminService.createAnnouncementTemplate(userId, name, content, type);
    }

    /**
     * 删除公告模板
     */
    @DeleteMapping("/announcements/templates/{templateId}")
    @Operation(summary = "删除公告模板", description = "删除自定义公告模板")
    public R deleteAnnouncementTemplate(@RequestAttribute Long userId,
                                        @PathVariable Long templateId) {
        checkAdminRole(userId);
        log.info("管理员[{}]删除公告模板[{}]", userId, templateId);
        return adminService.deleteAnnouncementTemplate(userId, templateId);
    }
}
