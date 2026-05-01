package com.qst.lm.controller;

import com.qst.lm.annotation.RequiresPermission;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.admin.AdminCreateUserDTO;
import com.qst.lm.dto.admin.AdminEditUserDTO;
import com.qst.lm.dto.announcement.AnnouncementDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.pojo.User;
import com.qst.lm.service.IAdminService;
import com.qst.lm.service.IPermissionService;
import com.qst.lm.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "后台管理模块", description = "后台管理相关接口（仅管理员）")
public class AdminController {

    private final IAdminService adminService;
    private final IPermissionService permissionService;
    private final IUserService userService;

    public AdminController(IAdminService adminService, IPermissionService permissionService, IUserService userService) {
        this.adminService = adminService;
        this.permissionService = permissionService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员专用登录接口，需具有admin或super_admin角色")
    public R adminLogin(@Valid @RequestBody LoginDTO dto) {
        R loginResult = userService.login(dto);
        if (loginResult == null || loginResult.getCode() != 200) {
            return loginResult;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) loginResult.getData();
        if (data == null) {
            return loginResult;
        }

        User userInfo = (User) data.get("userInfo");
        if (userInfo == null) {
            throw new BusinessException("登录失败，用户信息异常");
        }

        List<String> roles = permissionService.getUserRoles(userInfo.getId()).stream()
                .map(role -> role.getRoleCode())
                .toList();

        boolean isAdmin = roles.contains("admin") || roles.contains("super_admin")
                || "admin".equals(userInfo.getRole());

        if (!isAdmin) {
            log.warn("非管理员用户[{}]尝试访问后台", userInfo.getId());
            throw new BusinessException("权限不足，仅管理员可登录后台");
        }

        log.info("管理员[{}]登录后台成功", userInfo.getId());
        return loginResult;
    }

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "获取用户列表（支持关键词搜索和角色筛选）")
    @RequiresPermission("user:view")
    public R getUserList(@RequestAttribute Long userId,
                         PageDTO page,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String role) {
        log.info("管理员[{}]获取用户列表, 页码[{}], 每页[{}]", userId, page.getPageNum(), page.getPageSize());
        return adminService.getUserList(page, keyword, role);
    }

    @PutMapping("/users/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    @RequiresPermission("user:disable")
    public R disableUser(@RequestAttribute Long userId, @PathVariable Long id) {
        log.info("管理员[{}]禁用用户[{}]", userId, id);
        return adminService.disableUser(id);
    }

    @PutMapping("/users/{id}/enable")
    @Operation(summary = "解封用户", description = "解封指定用户")
    @RequiresPermission("user:disable")
    public R enableUser(@RequestAttribute Long userId, @PathVariable Long id) {
        log.info("管理员[{}]解封用户[{}]", userId, id);
        return adminService.enableUser(id);
    }

    @GetMapping("/content")
    @Operation(summary = "公开内容列表", description = "获取公开内容列表（收藏集或笔记）")
    @RequiresPermission("content:view")
    public R getPublicContent(@RequestAttribute Long userId,
                              PageDTO page,
                              @RequestParam String contentType) {
        log.info("管理员[{}]获取公开内容列表, 类型[{}], 页码[{}]", userId, contentType, page.getPageNum());
        return adminService.getPublicContent(page, contentType);
    }

    @PutMapping("/content/take-down")
    @Operation(summary = "内容下架", description = "将指定公开内容下架（设为非公开）")
    @RequiresPermission("content:take_down")
    public R takeDownContent(@RequestAttribute Long userId,
                             @RequestBody Map<String, Object> params) {
        String contentType = (String) params.get("contentType");
        Long contentId = Long.valueOf(params.get("contentId").toString());
        log.info("管理员[{}]下架内容, 类型[{}], ID[{}]", userId, contentType, contentId);
        return adminService.takeDownContent(contentType, contentId);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "后台首页统计", description = "获取后台管理首页的统计数据，包括用户数、内容数、访问量等")
    @RequiresPermission("admin:dashboard:view")
    public R getDashboard(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取后台首页统计", userId);
        return adminService.getDashboard();
    }

    @PostMapping("/users")
    @Operation(summary = "后台新增用户", description = "管理员在后台创建新用户")
    @RequiresPermission("user:create")
    public R createUser(@RequestAttribute Long userId,
                        @Valid @RequestBody AdminCreateUserDTO dto) {
        log.info("管理员[{}]创建新用户: {}", userId, dto.getUsername());
        return adminService.createUser(dto);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "后台编辑用户", description = "管理员编辑指定用户的信息")
    @RequiresPermission("user:update")
    public R updateUser(@RequestAttribute Long userId,
                        @PathVariable Long id,
                        @Valid @RequestBody AdminEditUserDTO dto) {
        log.info("管理员[{}]编辑用户[{}]", userId, id);
        return adminService.updateUser(id, dto);
    }

    @PostMapping("/announcements")
    @Operation(summary = "创建公告", description = "创建新的系统公告")
    @RequiresPermission("announcement:create")
    public R createAnnouncement(@RequestAttribute Long userId,
                                @Valid @RequestBody AnnouncementDTO dto) {
        log.info("管理员[{}]创建公告: {}", userId, dto.getTitle());
        return adminService.createAnnouncement(userId, dto);
    }

    @PutMapping("/announcements/{id}")
    @Operation(summary = "更新公告", description = "更新指定公告的信息")
    @RequiresPermission("announcement:update")
    public R updateAnnouncement(@RequestAttribute Long userId,
                                @PathVariable Long id,
                                @Valid @RequestBody AnnouncementDTO dto) {
        log.info("管理员[{}]更新公告[{}]", userId, id);
        return adminService.updateAnnouncement(userId, id, dto);
    }

    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "下架公告", description = "下架指定公告，使其不再显示")
    @RequiresPermission("announcement:update")
    public R deleteAnnouncement(@RequestAttribute Long userId,
                                @PathVariable Long id) {
        log.info("管理员[{}]下架公告[{}]", userId, id);
        return adminService.deleteAnnouncement(userId, id);
    }

    @GetMapping("/announcements/list")
    @Operation(summary = "获取公告列表", description = "获取公告列表，支持分页和状态筛选")
    @RequiresPermission("announcement:view")
    public R getAnnouncementList(@RequestAttribute Long userId,
                                 PageDTO page,
                                 @Parameter(description = "公告状态") @RequestParam(required = false) Integer status,
                                 @Parameter(description = "公告类型") @RequestParam(required = false) String type) {
        log.info("管理员[{}]获取公告列表, 页码[{}], 每页[{}], 状态[{}], 类型[{}]",
                userId, page.getPageNum(), page.getPageSize(), status, type);
        return adminService.getAnnouncementList(page.getPageNum(), page.getPageSize(), status, type);
    }

    @PutMapping("/content/restore")
    @Operation(summary = "恢复内容", description = "恢复已下架的公开内容")
    @RequiresPermission("content:restore")
    public R restoreContent(@RequestAttribute Long userId,
                            @RequestBody Map<String, Object> params) {
        String contentType = (String) params.get("contentType");
        Long contentId = Long.valueOf(params.get("contentId").toString());
        log.info("管理员[{}]恢复内容, 类型[{}], ID[{}]", userId, contentType, contentId);
        return adminService.restoreContent(contentType, contentId, userId);
    }

    @GetMapping("/content/audit-logs")
    @Operation(summary = "内容操作日志", description = "获取管理员对内容的操作日志")
    @RequiresPermission("content:view")
    public R getContentAuditLogs(@RequestAttribute Long userId,
                                 PageDTO page,
                                 @RequestParam(required = false) String operationType,
                                 @RequestParam(required = false) String targetType,
                                 @RequestParam(required = false) String startTime,
                                 @RequestParam(required = false) String endTime) {
        log.info("管理员[{}]获取内容操作日志", userId);
        return adminService.getContentAuditLogs(page.getPageNum(), page.getPageSize(), operationType, targetType, startTime, endTime);
    }

    @PostMapping("/announcements/{id}/publish")
    @Operation(summary = "发布公告", description = "将草稿状态的公告发布并推送给所有用户")
    @RequiresPermission("announcement:publish")
    public R publishAnnouncement(@RequestAttribute Long userId,
                                 @PathVariable Long id) {
        log.info("管理员[{}]发布公告[{}]", userId, id);
        return adminService.publishAnnouncement(userId, id);
    }

    @GetMapping("/announcements/{id}/statistics")
    @Operation(summary = "公告统计", description = "获取公告的触达统计数据")
    @RequiresPermission("announcement:view")
    public R getAnnouncementStatistics(@RequestAttribute Long userId,
                                       @PathVariable Long id) {
        log.info("管理员[{}]获取公告[{}]统计", userId, id);
        return adminService.getAnnouncementStatistics(id);
    }

    @GetMapping("/dashboard/trends")
    @Operation(summary = "趋势数据", description = "获取用户增长或内容发布的趋势数据")
    @RequiresPermission("admin:dashboard:view")
    public R getDashboardTrends(@RequestAttribute Long userId,
                                @RequestParam(defaultValue = "30") Integer days,
                                @RequestParam(required = false) String dataType) {
        log.info("管理员[{}]获取趋势数据, 天数[{}], 类型[{}]", userId, days, dataType);
        return adminService.getDashboardTrends(days, dataType);
    }

    @GetMapping("/dashboard/health")
    @Operation(summary = "系统健康", description = "获取系统健康状态信息")
    @RequiresPermission("admin:dashboard:view")
    public R getDashboardHealth(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取系统健康状态", userId);
        return adminService.getDashboardHealth();
    }

    @PostMapping("/users/batch-disable")
    @Operation(summary = "批量禁用用户", description = "批量禁用多个用户账号")
    @RequiresPermission("user:disable")
    public R batchDisableUsers(@RequestAttribute Long userId,
                               @RequestBody Map<String, Object> params) {
        List<Long> userIds = ((List<?>) params.get("userIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        String reason = (String) params.get("reason");
        log.info("管理员[{}]批量禁用用户, 数量[{}]", userId, userIds.size());
        return adminService.batchDisableUsers(userId, userIds, reason);
    }

    @PostMapping("/users/batch-enable")
    @Operation(summary = "批量启用用户", description = "批量启用多个用户账号")
    @RequiresPermission("user:disable")
    public R batchEnableUsers(@RequestAttribute Long userId,
                             @RequestBody Map<String, Object> params) {
        List<Long> userIds = ((List<?>) params.get("userIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        log.info("管理员[{}]批量启用用户, 数量[{}]", userId, userIds.size());
        return adminService.batchEnableUsers(userId, userIds);
    }

    @GetMapping("/users/export")
    @Operation(summary = "导出用户列表", description = "导出用户列表为Excel/CSV文件")
    @RequiresPermission("user:view")
    public org.springframework.http.HttpEntity<byte[]> exportUsers(@RequestAttribute Long userId,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(required = false) String role) {
        log.info("管理员[{}]导出用户列表", userId);
        byte[] excelData = adminService.exportUsersToExcel(keyword, role);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "users_export.csv");
        return new org.springframework.http.HttpEntity<>(excelData, headers);
    }

    @PostMapping("/content/batch-take-down")
    @Operation(summary = "批量下架内容", description = "批量下架多个公开内容")
    @RequiresPermission("content:take_down")
    public R batchTakeDownContent(@RequestAttribute Long userId,
                                  @RequestBody Map<String, Object> params) {
        String contentType = (String) params.get("contentType");
        List<Long> contentIds = ((List<?>) params.get("contentIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        String reason = (String) params.get("reason");
        log.info("管理员[{}]批量下架内容, 类型[{}], 数量[{}]", userId, contentType, contentIds.size());
        return adminService.batchTakeDownContent(userId, contentType, contentIds, reason);
    }

    @PostMapping("/content/batch-restore")
    @Operation(summary = "批量恢复内容", description = "批量恢复多个已下架内容")
    @RequiresPermission("content:restore")
    public R batchRestoreContent(@RequestAttribute Long userId,
                                @RequestBody Map<String, Object> params) {
        String contentType = (String) params.get("contentType");
        List<Long> contentIds = ((List<?>) params.get("contentIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        log.info("管理员[{}]批量恢复内容, 类型[{}], 数量[{}]", userId, contentType, contentIds.size());
        return adminService.batchRestoreContent(userId, contentType, contentIds);
    }

    @PostMapping("/announcements/{id}/schedule")
    @Operation(summary = "定时发布公告", description = "设置公告定时发布时间")
    @RequiresPermission("announcement:publish")
    public R scheduleAnnouncement(@RequestAttribute Long userId,
                                  @PathVariable Long id,
                                  @RequestBody Map<String, String> params) {
        java.time.LocalDateTime scheduledAt = java.time.LocalDateTime.parse(params.get("scheduledAt"));
        log.info("管理员[{}]设置公告[{}]定时发布", userId, id);
        return adminService.scheduleAnnouncement(userId, id, scheduledAt);
    }

    @GetMapping("/announcements/templates")
    @Operation(summary = "获取公告模板", description = "获取预设的公告模板列表")
    @RequiresPermission("announcement:view")
    public R getAnnouncementTemplates(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取公告模板列表", userId);
        return adminService.getAnnouncementTemplates();
    }

    @PostMapping("/announcements/templates")
    @Operation(summary = "创建公告模板", description = "创建自定义公告模板")
    @RequiresPermission("announcement:create")
    public R createAnnouncementTemplate(@RequestAttribute Long userId,
                                         @RequestBody Map<String, String> params) {
        String name = params.get("name");
        String content = params.get("content");
        String type = params.get("type");
        log.info("管理员[{}]创建公告模板[{}]", userId, name);
        return adminService.createAnnouncementTemplate(userId, name, content, type);
    }

    @DeleteMapping("/announcements/templates/{templateId}")
    @Operation(summary = "删除公告模板", description = "删除自定义公告模板")
    @RequiresPermission("announcement:update")
    public R deleteAnnouncementTemplate(@RequestAttribute Long userId,
                                        @PathVariable Long templateId) {
        log.info("管理员[{}]删除公告模板[{}]", userId, templateId);
        return adminService.deleteAnnouncementTemplate(userId, templateId);
    }

    @GetMapping("/users/{id}/roles")
    @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
    @RequiresPermission("user:view")
    public R getUserRoles(@RequestAttribute Long userId,
                          @PathVariable Long id) {
        log.info("管理员[{}]获取用户[{}]角色", userId, id);
        return R.success(permissionService.getUserRoles(id));
    }

    @PutMapping("/users/{id}/roles")
    @Operation(summary = "分配用户角色", description = "为指定用户分配角色")
    @RequiresPermission("user:update")
    public R assignUserRole(@RequestAttribute Long userId,
                            @PathVariable Long id,
                            @RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("roleId").toString());
        log.info("管理员[{}]为用户[{}]分配角色[{}]", userId, id, roleId);
        return permissionService.assignRoleToUser(id, roleId);
    }
}
