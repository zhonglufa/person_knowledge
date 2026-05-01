package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;

import java.time.LocalDateTime;

/**
 * 通知服务接口
 */
public interface INotificationService {

    /**
     * 获取通知列表（分页）
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 通知列表
     */
    R getNotificationList(Long userId, PageDTO page);

    /**
     * 标记通知为已读
     *
     * @param userId 用户ID
     * @param id     通知ID
     * @return 操作结果
     */
    R markAsRead(Long userId, Long id);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    R markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param userId 用户ID
     * @param id     通知ID
     * @return 操作结果
     */
    R deleteNotification(Long userId, Long id);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    R getUnreadCount(Long userId);

    /**
     * 创建通知
     *
     * @param userId           用户ID
     * @param collectionItemId 关联收藏项ID
     * @param title            通知标题
     * @param content          通知内容
     * @param notifyType       通知类型
     * @param remindAt         提醒时间
     * @return 操作结果
     */
    R createNotification(Long userId, Long collectionItemId, String title, String content, Integer notifyType, LocalDateTime remindAt);
}
