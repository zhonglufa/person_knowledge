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
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(required = false) Integer notifyType) {
        Map<String, Object> result = notificationService.getNotificationList(pageNum, pageSize, isRead, notifyType);
        return R.success(result);
    }

    @GetMapping("/unread-count")
    public R<Integer> getUnreadCount() {
        return R.success(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public R<String> markNotificationRead(@PathVariable Long id) {
        boolean success = notificationService.markAsRead(id);
        return success ? R.success("标记已读成功") : R.error("通知不存在或无权限");
    }

    @PutMapping("/read-all")
    public R<String> markAllNotificationRead() {
        int count = notificationService.markAllAsRead();
        return R.success("已标记 " + count + " 条通知为已读");
    }

    @DeleteMapping("/{id}")
    public R<String> deleteNotification(@PathVariable Long id) {
        boolean success = notificationService.deleteNotification(id);
        return success ? R.success("删除通知成功") : R.error("通知不存在或无权限");
    }
}
