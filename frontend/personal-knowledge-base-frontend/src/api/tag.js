// 标签管理API接口
import request from '@/api/axios'

// 获取用户所有标签
export const getUserTags = () => {
  return request({
    url: '/tags/list',
    method: 'get'
  })
}

// 创建标签
export const createTag = (data) => {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}

// 更新标签
export const updateTag = (id, data) => {
  return request({
    url: `/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export const deleteTag = (id) => {
  return request({
    url: `/tags/${id}`,
    method: 'delete'
  })
}

// 绑定标签到收藏项
// @param {number} itemId - 收藏项ID
// @param {Array<number>} tagIds - 标签ID数组
export const bindTagToItem = (itemId, tagIds) => {
  return request({
    url: '/tags/bind',
    method: 'post',
    data: { itemId, tagIds }
  })
}

// 从收藏项解绑标签
// @param {number} itemId - 收藏项ID
// @param {number} tagId - 标签ID
export const unbindTagFromItem = (itemId, tagId) => {
  return request({
    url: '/tags/unbind',
    method: 'delete',
    params: { itemId, tagId }
  })
}

// 获取收藏项的标签
export const getItemTags = (itemId) => {
  return request({
    url: `/tags/item/${itemId}`,
    method: 'get'
  })
}

// 获取标签统计信息
export const getTagStatistics = () => {
  return request({
    url: '/tags/statistics',
    method: 'get'
  })
}

// 批量操作标签
export const batchOperateTags = (data) => {
  return request({
    url: '/tags/batch',
    method: 'post',
    data
  })
}

// 获取热门标签
export const getPopularTags = (limit = 20) => {
  return request({
    url: '/tags/popular',
    method: 'get',
    params: { limit }
  })
}

// 合并标签
export const mergeTags = (data) => {
  return request({
    url: '/tags/merge',
    method: 'post',
    data
  })
}

// 获取标签云数据
export const getTagCloud = () => {
  return request({
    url: '/tags/cloud',
    method: 'get'
  })
}

// 兼容别名：getTags -> getUserTags
export const getTags = getUserTags