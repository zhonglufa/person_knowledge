// 搜索相关API接口
import request from '@/api/axios'

// 全局搜索（后端唯一搜索接口，支持type参数区分内容类型）
export function searchAll(params) {
  return request({
    url: '/search',
    method: 'get',
    params
  })
}

// 搜索收藏集（复用全局搜索接口，通过type=collection区分）
export function searchCollections(params) {
  return request({
    url: '/search',
    method: 'get',
    params: { ...params, type: 'collection' }
  })
}

// 搜索笔记（复用全局搜索接口，通过type=note区分）
export function searchNotes(params) {
  return request({
    url: '/search',
    method: 'get',
    params: { ...params, type: 'note' }
  })
}

// 搜索收藏项（复用全局搜索接口，通过type=collection_item区分）
export function searchItems(params) {
  return request({
    url: '/search',
    method: 'get',
    params: { ...params, type: 'collection_item' }
  })
}

// 获取搜索历史
export function getSearchHistory() {
  return request({
    url: '/search/history',
    method: 'get'
  })
}

// 删除单条搜索历史
export function deleteSearchHistory(id) {
  return request({
    url: `/search/history/${id}`,
    method: 'delete'
  })
}

// 清空搜索历史
export function clearSearchHistory() {
  return request({
    url: '/search/history/clear',
    method: 'delete'
  })
}

// 获取热门搜索词
export function getHotSearchKeywords() {
  return request({
    url: '/search/hot',
    method: 'get'
  })
}

// 搜索建议/自动补全（后端暂未实现独立接口，使用全局搜索限制条数代替）
export function getSearchSuggestions(keyword) {
  return request({
    url: '/search',
    method: 'get',
    params: { keyword, pageNum: 1, pageSize: 10 }
  })
}

// 高级搜索（支持多维度筛选）
// @param {Object} params - { keyword, type, timeRange, digestStatus, noteType, sortBy, sortOrder }
export function advancedSearch(params) {
  return request({
    url: '/search/advanced',
    method: 'post',
    data: params
  })
}


export default {
  searchAll,
  searchCollections,
  searchNotes,
  searchItems,
  getSearchHistory,
  deleteSearchHistory,
  clearSearchHistory,
  getHotSearchKeywords,
  getSearchSuggestions
}
