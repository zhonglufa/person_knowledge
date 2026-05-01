// 公告管理相关API接口（管理员）
import request from '@/api/axios'

// 公告API对象
export const announcementApi = {
  // 获取公告列表（分页）
  // @param {number} page - 页码
  // @param {number} size - 每页条数
  getAnnouncementList(page = 1, size = 10) {
    return request({
      url: '/admin/announcements/list',
      method: 'get',
      params: { pageNum: page, pageSize: size }
    })
  },

  // 创建公告（管理员权限）
  // @param {Object} data - 公告数据 { title, content, type, priority, effectiveAt, expireAt }
  createAnnouncement(data) {
    return request({
      url: '/admin/announcements',
      method: 'post',
      data
    })
  },

  // 更新公告（管理员权限）
  // @param {number} id - 公告ID
  // @param {Object} data - 更新的公告数据
  updateAnnouncement(id, data) {
    return request({
      url: `/admin/announcements/${id}`,
      method: 'put',
      data
    })
  },

  // 下架公告（管理员权限）
  // @param {number} id - 公告ID
  deleteAnnouncement(id) {
    return request({
      url: `/admin/announcements/${id}`,
      method: 'delete'
    })
  }
}

export default announcementApi
