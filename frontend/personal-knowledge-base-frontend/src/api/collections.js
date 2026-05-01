// 收藏集相关的 API 接口

import apiClient from './axios'
import publicRequest from './config/interceptors/publicAxios'

// 获取推荐收藏集（支持匿名访问）
// @param {number} pageNum - 页码
// @param {number} pageSize - 每页条数
export const getRecommended = (pageNum = 1, pageSize = 10) => {
  return publicRequest.get('/collections/recommended', { params: { pageNum, pageSize } })
}

// 获取收藏集详情
export const getCollectionDetail = (id) => {
  return apiClient.get(`/collections/${id}`)
}

// 获取我的收藏集列表（复用 /collections/list 接口）
// @param {Object} params - 查询参数 { pageNum, pageSize, keyword, categoryId, isPublic }
export const getUserCollections = (params = {}) => {
  return apiClient.get('/collections/list', { params })
}

// 搜索收藏集（复用全局搜索接口）
// @param {string} query - 搜索关键词
export const searchCollections = (query) => {
  return apiClient.get('/search', { params: { keyword: query, type: 'collection' } })
}

// 获取收藏集内的收藏项
// @param {number|string} collectionId - 收藏集ID
// @param {number} pageNum - 页码
// @param {number} pageSize - 每页条数
export const getCollectionItems = (collectionId, pageNum = 1, pageSize = 10) => {
  return apiClient.get(`/collections/${collectionId}/items`, { params: { pageNum, pageSize } })
}

// 创建收藏集
export const createCollection = (data) => {
  return apiClient.post('/collections', data)
}

// 更新收藏集
export const updateCollection = (id, data) => {
  return apiClient.put(`/collections/${id}`, data)
}

// 删除收藏集
export const deleteCollection = (id) => {
  return apiClient.delete(`/collections/${id}`)
}

// 获取公开收藏集列表
// @param {Object} params - 查询参数 { pageNum, pageSize, keyword }
export const getPublicCollections = (params = {}) => {
  return apiClient.get('/collections/public', { params })
}

// 封装成 collectionsApi 对象导出
export const collectionsApi = {
  getRecommended,
  getCollectionDetail,
  searchCollections,
  getUserCollections,
  getCollectionItems,
  getPublicCollections,
  createCollection,
  updateCollection,
  deleteCollection
}

export default collectionsApi