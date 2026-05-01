package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
@Tag(name = "通知管理模块", description = "通知相关接口")
public class NotificationController {

    private final INotificationService notificationService;

    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "通知列表", description = "获取当前用户的通知列表，支持分页")
    public R getNotificationList(@RequestAttribute Long userId, PageDTO page) {
        return notificationService.getNotificationList(userId, page);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    public R markAsRead(@RequestAttribute Long userId, @PathVariable Long id) {
        return notificationService.markAsRead(userId, id);
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将当前用户所有未读通知标记为已读")
    public R markAllAsRead(@RequestAttribute Long userId) {
        return notificationService.markAllAsRead(userId);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知", description = "删除指定通知（逻辑删除）")
    public R deleteNotification(@RequestAttribute Long userId, @PathVariable Long id) {
        return notificationService.deleteNotification(userId, id);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "未读数量", description = "获取当前用户未读通知数量")
    public R getUnreadCount(@RequestAttribute Long userId) {
        return notificationService.getUnreadCount(userId);
    }
}
