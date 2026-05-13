// 搜索相关API接口
import request from '@/api/axios'

function buildSearchParams(params = {}, searchType = 'all') {
  const nextParams = { ...params }
  if (!nextParams.searchType) {
    nextParams.searchType = searchType
  }
  delete nextParams.type
  return nextParams
}

export function searchAll(params) {
  return request({
    url: '/search',
    method: 'get',
    params: buildSearchParams(params, 'all')
  })
}

export function searchCollections(params) {
  return request({
    url: '/search',
    method: 'get',
    params: buildSearchParams(params, 'collection')
  })
}

export function searchNotes(params) {
  return request({
    url: '/search',
    method: 'get',
    params: buildSearchParams(params, 'note')
  })
}

export function searchItems(params) {
  return request({
    url: '/search',
    method: 'get',
    params: buildSearchParams(params, 'item')
  })
}

export function getSearchHistory() {
  return request({
    url: '/search/history',
    method: 'get'
  })
}

export function deleteSearchHistory(id) {
  return request({
    url: `/search/history/${id}`,
    method: 'delete'
  })
}

export function clearSearchHistory() {
  return request({
    url: '/search/history/clear',
    method: 'delete'
  })
}

export function getHotSearchKeywords() {
  return request({
    url: '/search/hot',
    method: 'get'
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
  getHotSearchKeywords
}
