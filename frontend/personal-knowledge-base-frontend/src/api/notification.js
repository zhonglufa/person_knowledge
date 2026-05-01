import request from './axios'

// 通知类型枚举
export const NotificationType = {
  LEARNING_REMINDER: 1,    // 收藏项学习提醒
  SYSTEM_ANNOUNCEMENT: 2, // 系统公告
  NOTE_REMINDER: 3,       // 笔记提醒
  PROGRESS_OVERDUE: 4,    // 学习进度逾期提醒
  LIKE: 5,                // 点赞通知
  COMMENT: 6,             // 评论通知
  COLLECT: 7              // 收藏通知
}

// 获取通知列表
export function getNotificationList(params) {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

// 标记单条通知已读
export function markNotificationRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

// 标记所有通知已读
export function markAllNotificationRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

// 删除通知
export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

// 获取互动中心统计
export function getInteractionStats() {
  return request({
    url: '/notification/interaction/stats',
    method: 'get'
  })
}

// 获取收到的点赞列表
export function getReceivedLikes(params) {
  return request({
    url: '/notification/interaction/likes',
    method: 'get',
    params
  })
}

// 获取收到的评论列表
export function getReceivedComments(params) {
  return request({
    url: '/notification/interaction/comments',
    method: 'get',
    params
  })
}

// 获取收到的收藏列表
export function getReceivedCollects(params) {
  return request({
    url: '/notification/interaction/collects',
    method: 'get',
    params
  })
}
