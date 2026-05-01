// 收藏集相关的 API 接口

import apiClient from './axios'
import publicRequest from './config/interceptors/publicAxios'

export const getRecommended = (pageNum = 1, pageSize = 10) => {
  return publicRequest.get('/collections/recommended', { params: { pageNum, pageSize } })
}

export const getCollectionDetail = (id) => {
  return apiClient.get(`/collections/${id}`)
}

export const getUserCollections = (params = {}) => {
  return apiClient.get('/collections/list', { params })
}

export const searchCollections = (query) => {
  return apiClient.get('/search', { params: { keyword: query, searchType: 'collection' } })
}

export const getCollectionItems = (collectionId, pageNum = 1, pageSize = 10) => {
  return apiClient.get(`/collections/${collectionId}/items`, { params: { pageNum, pageSize } })
}

export const createCollection = (data) => {
  return apiClient.post('/collections', data)
}

export const updateCollection = (id, data) => {
  return apiClient.put(`/collections/${id}`, data)
}

export const deleteCollection = (id) => {
  return apiClient.delete(`/collections/${id}`)
}

export const toggleCollectionStar = (id) => {
  return apiClient.put(`/collections/${id}/star`)
}

export const processCollectionItem = (id, data) => {
  return apiClient.put(`/collect/${id}/digest-status`, data)
}

export const getPublicCollections = (params = {}) => {
  return publicRequest.get('/collections/public', { params })
}

export const updateCollectionPublicStatus = (id, isPublic) => {
  return apiClient.put(`/collections/${id}/public`, { isPublic })
}

export const collectionsApi = {
  getRecommended,
  getCollectionDetail,
  searchCollections,
  getUserCollections,
  getCollectionItems,
  getPublicCollections,
  createCollection,
  updateCollection,
  deleteCollection,
  toggleCollectionStar,
  processCollectionItem,
  updateCollectionPublicStatus
}

export default collectionsApi
