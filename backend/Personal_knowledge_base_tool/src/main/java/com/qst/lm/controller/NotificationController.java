package com.qst.lm.controller;

import com.qst.lm.common.R;
import com.qst.lm.service.INotificationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private INotificationService notificationService;

    @GetMapping("/list")
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
    public R<Integer> getUnreadCount(@RequestAttribute Long userId) {
        return R.success(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public R<String> markNotificationRead(@RequestAttribute Long userId, @PathVariable Long id) {
        boolean success = notificationService.markAsRead(userId, id);
        return success ? R.success("标记已读成功") : R.error("通知不存在或无权限");
    }

    @PutMapping("/read-all")
    public R<String> markAllNotificationRead(@RequestAttribute Long userId) {
        int count = notificationService.markAllAsRead(userId);
        return R.success("已标记 " + count + " 条通知为已读");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteNotification(@RequestAttribute Long userId, @PathVariable Long id) {
        boolean success = notificationService.deleteNotification(userId, id);
        return success ? R.success("删除通知成功") : R.error("通知不存在或无权限");
    }
}
