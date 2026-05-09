package com.qst.lm.service;

import java.util.List;
import java.util.Map;

public interface INotificationService {

    Map<String, Object> getNotificationList(Long userId, Integer pageNum, Integer pageSize, Integer isRead, Integer notifyType);

    Integer getUnreadCount(Long userId);

    boolean markAsRead(Long userId, Long id);

    int markAllAsRead(Long userId);

    boolean deleteNotification(Long userId, Long id);

    int batchDeleteNotifications(Long userId, List<Long> ids);
}
