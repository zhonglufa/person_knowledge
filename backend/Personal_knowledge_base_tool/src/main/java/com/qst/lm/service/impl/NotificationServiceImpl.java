package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.NotificationMapper;
import com.qst.lm.pojo.Notification;
import com.qst.lm.service.INotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public R getNotificationList(Long userId, PageDTO page) {
        Page<Notification> pageObj = new Page<>(page.getPageNum(), page.getPageSize());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByAsc(Notification::getIsRead)
                .orderByDesc(Notification::getCreatedAt);
        Page<Notification> result = notificationMapper.selectPage(pageObj, wrapper);
        return R.success(result);
    }

    @Override
    public R markAsRead(Long userId, Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该通知");
        }
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadAt, LocalDateTime.now());
        notificationMapper.update(null, wrapper);
        log.info("用户[{}]标记通知[{}]为已读", userId, id);
        return R.success("标记已读成功");
    }

    @Override
    public R markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadAt, LocalDateTime.now());
        notificationMapper.update(null, wrapper);
        log.info("用户[{}]标记所有通知为已读", userId);
        return R.success("全部标记已读成功");
    }

    @Override
    public R deleteNotification(Long userId, Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作该通知");
        }
        notificationMapper.deleteById(id);
        log.info("用户[{}]删除通知[{}]", userId, id);
        return R.success("删除通知成功");
    }

    @Override
    public R getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        Long count = notificationMapper.selectCount(wrapper);
        return R.success(count);
    }

    @Override
    public R createNotification(Long userId, Long collectionItemId, String title, String content, Integer notifyType, LocalDateTime remindAt) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCollectionItemId(collectionItemId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotifyType(notifyType);
        notification.setRemindAt(remindAt);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
        log.info("为用户[{}]创建通知[{}]", userId, notification.getId());
        return R.success("创建通知成功", notification);
    }
}
