package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理", description = "通知中心相关接口")
public class NotificationController {

    @Resource
    private INotificationService notificationService;

    @GetMapping("/list")
    @Operation(summary = "获取通知列表", description = "分页查询用户通知列表，支持按已读状态和通知类型筛选")
    public R<Map<String, Object>> getNotificationList(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) Integer notifyType) {
        Map<String, Object> result = notificationService.getNotificationList(userId, pageNum, pageSize, isRead, notifyType);
        return R.success(result);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读数量", description = "获取当前用户的未读通知数量")
    public R<Integer> getUnreadCount(@RequestAttribute Long userId) {
        return R.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    public R<String> markNotificationRead(@RequestAttribute Long userId, @PathVariable Long id) {
        boolean success = notificationService.markAsRead(userId, id);
        return success ? R.success("标记已读成功") : R.error("通知不存在或无权限");
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部已读", description = "将当前用户的所有未读通知标记为已读")
    public R<String> markAllNotificationRead(@RequestAttribute Long userId) {
        int count = notificationService.markAllAsRead(userId);
        return R.success("已标记 " + count + " 条通知为已读");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public R<String> deleteNotification(@RequestAttribute Long userId, @PathVariable Long id) {
        boolean success = notificationService.deleteNotification(userId, id);
        return success ? R.success("删除通知成功") : R.error("通知不存在或无权限");
    }

    @PostMapping("/batch/delete")
    @Operation(summary = "批量删除通知", description = "批量删除多个通知")
    public R<String> batchDeleteNotifications(@RequestAttribute Long userId, @RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return R.error("请选择要删除的通知");
        }
        int count = notificationService.batchDeleteNotifications(userId, ids);
        return R.success("已删除 " + count + " 条通知");
    }
}
