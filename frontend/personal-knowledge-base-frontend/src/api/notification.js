import request from './axios'

export function getNotificationList(params) {
  return request({
    url: '/notification/list',
    method: 'get',
    params
  })
}

export function markNotificationRead(id) {
  return request({
    url: `/notification/${id}/read`,
    method: 'put'
  })
}

export function markAllNotificationRead() {
  return request({
    url: '/notification/read-all',
    method: 'put'
  })
}

export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}

export function getUnreadCount() {
  return request({
    url: '/notification/unread-count',
    method: 'get'
  })
}

export function getInteractionStats(params) {
  return request({
    url: '/interaction/messages/stats',
    method: 'get',
    params
  })
}

export function getReceivedLikes(params) {
  return request({
    url: '/interaction/messages',
    method: 'get',
    params: { ...params, type: 'like' }
  })
}

export function getReceivedComments(params) {
  return request({
    url: '/interaction/messages',
    method: 'get',
    params: { ...params, type: 'comment' }
  })
}

export function getReceivedCollects(params) {
  return request({
    url: '/interaction/messages',
    method: 'get',
    params: { ...params, type: 'collect' }
  })
}

export function markInteractionRead(id) {
  return request({
    url: `/interaction/messages/${id}/read`,
    method: 'put'
  })
}

export function markAllInteractionsRead(type) {
  return request({
    url: '/interaction/messages/read-all',
    method: 'put',
    params: { type }
  })
}

export function deleteInteraction(id) {
  return request({
    url: `/interaction/messages/${id}`,
    method: 'delete'
  })
}
