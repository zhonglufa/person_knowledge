// 管理员API统一入口
import request from '@/api/axios'

// ==================== 用户管理 API ====================
export const userManageApi = {
  // 获取用户列表
  getUserList(params) {
    return request({
      url: '/admin/users',
      method: 'get',
      params
    })
  },
  // 禁用用户
  disableUser(id) {
    return request({
      url: `/admin/users/${id}/disable`,
      method: 'put'
    })
  },
  // 新增用户
  addUser(data) {
    return request({
      url: '/admin/users',
      method: 'post',
      data
    })
  },
  // 编辑用户
  editUser(id, data) {
    return request({
      url: `/admin/users/${id}`,
      method: 'put',
      data
    })
  },
  // 解封用户
  enableUser(id) {
    return request({
      url: `/admin/users/${id}/enable`,
      method: 'put'
    })
  },
  // 批量禁用用户
  batchDisable(data) {
    return request({
      url: '/admin/users/batch-disable',
      method: 'post',
      data
    })
  },
  // 批量启用用户
  batchEnable(data) {
    return request({
      url: '/admin/users/batch-enable',
      method: 'post',
      data
    })
  },
  // 导出用户列表
  exportUsers(params) {
    return request({
      url: '/admin/users/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}

// ==================== 管理员通用 API ====================
export const adminApi = {
  // 管理员登录
  login(data) {
    return request({
      url: '/user/login',
      method: 'post',
      data
    })
  },
  // 获取后台首页统计数据
  // 别名: getDashboard
  getHomeData() {
    return request({
      url: '/admin/dashboard',
      method: 'get'
    })
  },
  // 获取后台首页统计数据（别名）
  getDashboard() {
    return this.getHomeData()
  },
  // 获取仪表盘趋势数据
  getDashboardTrends(params) {
    return request({
      url: '/admin/dashboard/trends',
      method: 'get',
      params
    })
  },
  // 获取系统健康状态
  getDashboardHealth() {
    return request({
      url: '/admin/dashboard/health',
      method: 'get'
    })
  }
}

// ==================== 内容管理 API ====================
export const contentManageApi = {
  // 获取公开内容列表
  getContentList(params) {
    return request({
      url: '/admin/content',
      method: 'get',
      params
    })
  },
  // 下架内容
  takeDownContent(type, id) {
    return request({
      url: '/admin/content/take-down',
      method: 'put',
      data: { contentType: type, contentId: id }
    })
  },
  // 批量下架内容
  batchTakeDown(data) {
    return request({
      url: '/admin/content/batch-take-down',
      method: 'post',
      data
    })
  },
  // 批量恢复内容
  batchRestore(data) {
    return request({
      url: '/admin/content/batch-restore',
      method: 'post',
      data
    })
  },
  // 恢复已下架内容
  restoreContent(data) {
    return request({
      url: '/admin/content/restore',
      method: 'put',
      data
    })
  },
  // 获取内容操作日志
  getAuditLogs(params) {
    return request({
      url: '/admin/content/audit-logs',
      method: 'get',
      params
    })
  }
}

// ==================== 公告管理 API ====================
export const announcementApi = {
  // 获取公告列表（支持分页和筛选参数）
  getAnnouncementList(params = {}) {
    return request({
      url: '/admin/announcements/list',
      method: 'get',
      params: {
        pageNum: params.pageNum || 1,
        pageSize: params.pageSize || 10,
        status: params.status,
        type: params.type
      }
    })
  },
  // 创建公告
  createAnnouncement(data) {
    return request({
      url: '/admin/announcements',
      method: 'post',
      data
    })
  },
  // 更新公告
  updateAnnouncement(id, data) {
    return request({
      url: `/admin/announcements/${id}`,
      method: 'put',
      data
    })
  },
  // 下架公告
  deleteAnnouncement(id) {
    return request({
      url: `/admin/announcements/${id}`,
      method: 'delete'
    })
  },
  // 发布公告（触发全站通知）
  publishAnnouncement(id) {
    return request({
      url: `/admin/announcements/${id}/publish`,
      method: 'post'
    })
  },
  // 获取公告触达统计
  getAnnouncementStatistics(id) {
    return request({
      url: `/admin/announcements/${id}/statistics`,
      method: 'get'
    })
  },
  // 定时发布公告
  scheduleAnnouncement(id, data) {
    return request({
      url: `/admin/announcements/${id}/schedule`,
      method: 'post',
      data
    })
  },
  // 获取公告模板列表
  getAnnouncementTemplates() {
    return request({
      url: '/admin/announcements/templates',
      method: 'get'
    })
  },
  // 创建公告模板
  createAnnouncementTemplate(data) {
    return request({
      url: '/admin/announcements/templates',
      method: 'post',
      data
    })
  },
  // 删除公告模板
  deleteAnnouncementTemplate(templateId) {
    return request({
      url: `/admin/announcements/templates/${templateId}`,
      method: 'delete'
    })
  }
}

// 重新导出子模块（已移除非必要模块：interaction, publish, report）
// export * from './interaction'
// export * from './publish'
// export * from './report'
