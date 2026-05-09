import request from './axios'

// 获取个人信息
export function getUserProfile() {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

// 更新个人信息
export function updateUserProfile(data) {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

// 上传头像
export function uploadAvatar(formData) {
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

// 获取个人统计数据
export function getUserStatistics() {
  return request({
    url: '/user/statistics',
    method: 'get'
  })
}

// 获取学习进度
export function getLearningProgress() {
  return request({
    url: '/user/statistics/learning-progress',
    method: 'get'
  })
}

// 获取学习趋势
export function getLearningTrend() {
  return request({
    url: '/user/statistics/learning-trend',
    method: 'get'
  })
}

// 获取学习统计
export function getLearningStats() {
  return request({
    url: '/user/statistics/learning-stats',
    method: 'get'
  })
}

// 获取学习进度列表
export function getLearningProgressList() {
  return request({
    url: '/user/statistics/learning-progress-list',
    method: 'get'
  })
}

// 获取内容类型分布
export function getTypeDistribution() {
  return request({
    url: '/user/statistics/type-distribution',
    method: 'get'
  })
}

// 获取加工状态分布
export function getProcessingDistribution() {
  return request({
    url: '/user/statistics/processing-distribution',
    method: 'get'
  })
}

// 忘记密码 - 重置密码
export function resetPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'post',
    params: data
  })
}

// 获取个性化设置
export function getUserSettings() {
  return request({
    url: '/user/settings',
    method: 'get'
  })
}

// 更新个性化设置
// @param {Object} data - 设置数据 { theme, language, notification, privacy, etc. }
export function updateUserSettings(data) {
  return request({
    url: '/user/settings',
    method: 'put',
    data
  })
}

// 兼容对象式调用
export const userApi = {
  getUserProfile,
  updateProfile: updateUserProfile,
  uploadAvatar,
  changePassword,
  getUserStatistics,
  getLearningProgress,
  getLearningTrend,
  getLearningStats,
  getLearningProgressList,
  getTypeDistribution,
  getProcessingDistribution,
  resetPassword,
  getUserSettings,
  updateUserSettings
}
