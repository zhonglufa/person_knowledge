package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qst.lm.mapper.AnnouncementMapper;
import com.qst.lm.mapper.CollectionItemMapper;
import com.qst.lm.mapper.CollectionMapper;
import com.qst.lm.mapper.NoteMapper;
import com.qst.lm.mapper.NotificationMapper;
import com.qst.lm.mapper.UserMapper;
import com.qst.lm.pojo.Announcement;
import com.qst.lm.pojo.Collection;
import com.qst.lm.pojo.CollectionItem;
import com.qst.lm.pojo.Note;
import com.qst.lm.pojo.Notification;
import com.qst.lm.pojo.User;
import com.qst.lm.service.INotificationService;
import com.qst.lm.utils.ThreadLocalUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private CollectionItemMapper collectionItemMapper;
    @Resource
    private NoteMapper noteMapper;
    @Resource
    private CollectionMapper collectionMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AnnouncementMapper announcementMapper;

    @Override
    public Map<String, Object> getNotificationList(Integer pageNum, Integer pageSize, Integer isRead, Integer notifyType) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByAsc(Notification::getIsRead)
                .orderByDesc(Notification::getCreatedAt);

        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        if (notifyType != null) {
            wrapper.eq(Notification::getNotifyType, notifyType);
        }

        int currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (currentPage - 1) * currentSize;

        List<Notification> notifications = notificationMapper.selectList(wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        int end = Math.min(offset + currentSize, notifications.size());
        for (int i = offset; i < end; i++) {
            list.add(convertNotification(notifications.get(i)));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("pageNum", currentPage);
        result.put("pageSize", currentSize);
        result.put("total", notifications.size());
        return result;
    }

    @Override
    public Integer getUnreadCount() {
        Long userId = getCurrentUserId();
        return Math.toIntExact(notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)));
    }

    @Override
    public boolean markAsRead(Long id) {
        Long userId = getCurrentUserId();
        Notification notification = notificationMapper.selectOne(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId));
        if (notification == null) {
            return false;
        }
        if (notification.getIsRead() != null && notification.getIsRead() == 1) {
            return true;
        }
        notification.setIsRead(1);
        notification.setReadAt(LocalDateTime.now());
        return notificationMapper.updateById(notification) > 0;
    }

    @Override
    public int markAllAsRead() {
        Long userId = getCurrentUserId();
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1)
                .set(Notification::getReadAt, LocalDateTime.now());
        return notificationMapper.update(null, wrapper);
    }

    @Override
    public boolean deleteNotification(Long id) {
        Long userId = getCurrentUserId();
        return notificationMapper.delete(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)) > 0;
    }

    private Long getCurrentUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Object userIdObj = claims.get("id");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        return (Long) userIdObj;
    }

    private Map<String, Object> convertNotification(Notification notification) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", notification.getId());
        item.put("notifyType", notification.getNotifyType());
        item.put("title", notification.getTitle());
        item.put("content", notification.getContent());
        item.put("isRead", notification.getIsRead());
        item.put("createdAt", notification.getCreatedAt());
        item.put("readAt", notification.getReadAt());
        item.put("remindAt", notification.getRemindAt());
        item.put("targetId", null);
        item.put("targetType", null);
        item.put("targetTitle", null);
        item.put("targetUrl", null);
        item.put("type", notificationTypeCode(notification.getNotifyType()));
        item.put("createTime", notification.getCreatedAt());
        item.put("businessType", null);
        item.put("businessId", null);

        if (notification.getNotifyType() == null) {
            return item;
        }

        if (notification.getNotifyType() == 1 || notification.getNotifyType() == 4) {
            fillCollectionItemTarget(item, notification.getCollectionItemId());
        } else if (notification.getNotifyType() == 2) {
            fillAnnouncementTarget(item, notification);
        } else if (notification.getNotifyType() == 3) {
            fillNoteTarget(item, notification.getCollectionItemId());
        } else if (notification.getNotifyType() == 5 || notification.getNotifyType() == 6 || notification.getNotifyType() == 7) {
            fillInteractionTarget(item, notification);
        }
        item.put("businessType", item.get("targetType"));
        item.put("businessId", item.get("targetId"));
        return item;
    }

    private void fillCollectionItemTarget(Map<String, Object> item, Long collectionItemId) {
        if (collectionItemId == null) {
            return;
        }
        CollectionItem collectionItem = collectionItemMapper.selectById(collectionItemId);
        if (collectionItem == null) {
            return;
        }
        item.put("targetId", collectionItem.getId());
        item.put("targetType", "collection_item");
        item.put("targetTitle", collectionItem.getTitle());
        item.put("targetUrl", "/collections/" + collectionItem.getId() + "/workspace");
    }

    private void fillAnnouncementTarget(Map<String, Object> item, Notification notification) {
        Announcement announcement = announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getTitle, notification.getTitle())
                .orderByDesc(Announcement::getId)
                .last("limit 1"));
        if (announcement == null) {
            return;
        }
        item.put("targetId", announcement.getId());
        item.put("targetType", "announcement");
        item.put("targetTitle", announcement.getTitle());
        item.put("targetUrl", "/admin/announcements");
    }

    private void fillNoteTarget(Map<String, Object> item, Long noteId) {
        if (noteId == null) {
            return;
        }
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            return;
        }
        item.put("targetId", note.getId());
        item.put("targetType", "note");
        item.put("targetTitle", note.getTitle());
        item.put("targetUrl", "/creation/notes/" + note.getId());
    }

    private void fillInteractionTarget(Map<String, Object> item, Notification notification) {
        String content = notification.getContent();
        Long targetId = parseTargetId(content);
        String targetType = parseTargetType(notification.getTitle());
        if (targetId == null || targetType == null) {
            return;
        }
        item.put("targetId", targetId);
        item.put("targetType", targetType);
        item.put("targetUrl", buildInteractionTargetUrl(targetType, targetId));
        item.put("targetTitle", loadTargetTitle(targetType, targetId));
    }

    private Long parseTargetId(String content) {
        if (content == null) {
            return null;
        }
        int start = content.indexOf("(ID:");
        int end = content.indexOf(")", start);
        if (start < 0 || end < 0) {
            return null;
        }
        try {
            return Long.parseLong(content.substring(start + 4, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseTargetType(String title) {
        if (title == null) {
            return null;
        }
        if (title.contains("笔记")) {
            return "note";
        }
        if (title.contains("收藏集")) {
            return "collection";
        }
        if (title.contains("收藏项")) {
            return "collection_item";
        }
        return null;
    }

    private String buildInteractionTargetUrl(String targetType, Long targetId) {
        if ("note".equals(targetType)) {
            return "/creation/notes/" + targetId + "#comments";
        }
        if ("collection".equals(targetType)) {
            return "/collections/" + targetId;
        }
        if ("collection_item".equals(targetType)) {
            return "/collections/" + targetId + "/workspace";
        }
        return null;
    }

    private String loadTargetTitle(String targetType, Long targetId) {
        if ("note".equals(targetType)) {
            Note note = noteMapper.selectById(targetId);
            return note != null ? note.getTitle() : null;
        }
        if ("collection".equals(targetType)) {
            Collection collection = collectionMapper.selectById(targetId);
            return collection != null ? collection.getName() : null;
        }
        if ("collection_item".equals(targetType)) {
            CollectionItem collectionItem = collectionItemMapper.selectById(targetId);
            return collectionItem != null ? collectionItem.getTitle() : null;
        }
        return null;
    }

    private String notificationTypeCode(Integer notifyType) {
        if (notifyType == null) {
            return "system";
        }
        switch (notifyType) {
            case 1:
                return "study";
            case 2:
                return "announcement";
            case 3:
                return "note";
            case 4:
                return "overdue";
            case 5:
                return "like";
            case 6:
                return "comment";
            case 7:
                return "collect";
            default:
                return "system";
        }
    }
}
