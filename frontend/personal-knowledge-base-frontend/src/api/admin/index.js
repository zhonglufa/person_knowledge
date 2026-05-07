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
      url: '/admin/login',
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
  // 下架公告（修改状态为已下架，可恢复）
  takeDownAnnouncement(id) {
    return request({
      url: `/admin/announcements/${id}`,
      method: 'delete'
    })
  },
  // 删除公告（逻辑删除，不可恢复）
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

// ==================== 角色管理 API ====================
export const roleManageApi = {
  // 获取角色列表
  getRoleList(params) {
    return request({
      url: '/admin/roles',
      method: 'get',
      params
    })
  },
  // 获取角色详情
  getRoleDetail(id) {
    return request({
      url: `/admin/roles/${id}`,
      method: 'get'
    })
  },
  // 创建角色
  createRole(data) {
    return request({
      url: '/admin/roles',
      method: 'post',
      data
    })
  },
  // 更新角色
  updateRole(id, data) {
    return request({
      url: `/admin/roles/${id}`,
      method: 'put',
      data
    })
  },
  // 删除角色
  deleteRole(id) {
    return request({
      url: `/admin/roles/${id}`,
      method: 'delete'
    })
  },
  // 获取角色权限
  getRolePermissions(id) {
    return request({
      url: `/admin/roles/${id}/permissions`,
      method: 'get'
    })
  },
  // 分配权限
  assignPermissions(id, data) {
    return request({
      url: `/admin/roles/${id}/permissions`,
      method: 'put',
      data
    })
  },
  // 获取所有角色
  getAllRoles() {
    return request({
      url: '/admin/roles/all',
      method: 'get'
    })
  }
}

// ==================== 权限管理 API ====================
export const permissionApi = {
  // 获取权限列表
  getPermissionList() {
    return request({
      url: '/admin/permissions',
      method: 'get'
    })
  },
  // 获取权限树
  getPermissionTree() {
    return request({
      url: '/admin/permissions/tree',
      method: 'get'
    })
  },
  // 获取当前用户权限
  getCurrentUserPermissions() {
    return request({
      url: '/admin/permissions/user',
      method: 'get'
    })
  },
  // 获取用户角色
  getUserRoles(userId) {
    return request({
      url: `/admin/users/${userId}/roles`,
      method: 'get'
    })
  },
  // 分配用户角色
  assignUserRole(userId, data) {
    return request({
      url: `/admin/users/${userId}/roles`,
      method: 'put',
      data
    })
  }
}

// 重新导出子模块（已移除非必要模块：interaction, publish, report）
// export * from './interaction'
// export * from './publish'
// export * from './report'
