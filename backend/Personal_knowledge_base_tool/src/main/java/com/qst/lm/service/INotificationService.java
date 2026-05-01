package com.qst.lm.service;

import java.util.Map;

public interface INotificationService {

    Map<String, Object> getNotificationList(Integer pageNum, Integer pageSize, Integer isRead, Integer notifyType);

    Integer getUnreadCount();

    boolean markAsRead(Long id);

    int markAllAsRead();

    boolean deleteNotification(Long id);
}
