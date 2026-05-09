package com.qst.lm.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.NotificationMapper;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.pojo.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ReminderScheduler {

    private final CollectionItemMapper collectionItemMapper;
    private final NotificationMapper notificationMapper;

    public ReminderScheduler(CollectionItemMapper collectionItemMapper, NotificationMapper notificationMapper) {
        this.collectionItemMapper = collectionItemMapper;
        this.notificationMapper = notificationMapper;
    }

    @Scheduled(fixedDelayString = "${reminder.scan.fixed-delay-ms:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void scanDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("开始扫描到期学习提醒, 当前时间: {}", now);

        List<CollectionItem> dueItems = collectionItemMapper.selectList(
                new LambdaQueryWrapper<CollectionItem>()
                        .eq(CollectionItem::getDeleted, 0)
                        .eq(CollectionItem::getRemindTriggered, 0)
                        .isNotNull(CollectionItem::getRemindAt)
                        .le(CollectionItem::getRemindAt, now)
                        .orderByAsc(CollectionItem::getRemindAt)
                        .last("limit 200")
        );

        if (dueItems == null || dueItems.isEmpty()) {
            log.debug("未找到待触发的提醒");
            return;
        }

        log.info("找到 {} 条待触发的学习提醒", dueItems.size());

        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;
        List<Notification> notificationsToInsert = new ArrayList<>();
        List<Long> itemIdsToUpdate = new ArrayList<>();

        for (CollectionItem item : dueItems) {
            if (item.getId() == null || item.getUserId() == null) {
                log.warn("跳过无效的收藏项: id={}, userId={}", item.getId(), item.getUserId());
                skippedCount++;
                continue;
            }

            try {
                Notification notification = new Notification();
                notification.setUserId(item.getUserId());
                notification.setCollectionItemId(item.getId());
                notification.setNotifyType(1);
                notification.setIsRead(0);
                notification.setRemindAt(item.getRemindAt());
                notification.setTitle("学习提醒");
                notification.setContent(item.getTitle() == null ? "您有一条学习提醒" : ("请回顾：" + item.getTitle()));

                notificationsToInsert.add(notification);
                itemIdsToUpdate.add(item.getId());

            } catch (Exception e) {
                log.error("处理收藏项[{}]提醒失败: {}", item.getId(), e.getMessage(), e);
                failCount++;
            }
        }

        if (!notificationsToInsert.isEmpty()) {
            try {
                for (Notification notification : notificationsToInsert) {
                    notificationMapper.insert(notification);
                }
                successCount = notificationsToInsert.size();
                log.info("批量插入  条通知成功", successCount);
            } catch (Exception e) {
                log.error("批量插入通知失败: {}", e.getMessage(), e);
                throw e;
            }
        }

        if (!itemIdsToUpdate.isEmpty()) {
            try {
                LambdaUpdateWrapper<CollectionItem> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.in(CollectionItem::getId, itemIdsToUpdate)
                        .set(CollectionItem::getRemindTriggered, 1);
                collectionItemMapper.update(null, updateWrapper);
                log.info("标记 {} 条提醒为已触发", itemIdsToUpdate.size());
            } catch (Exception e) {
                log.error("更新提醒触发状态失败: {}", e.getMessage(), e);
                throw e;
            }
        }

        log.info("学习提醒扫描完成 - 成功: {}, 失败: {}, 跳过: {}", successCount, failCount, skippedCount);
    }
}
