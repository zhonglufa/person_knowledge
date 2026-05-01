// 笔记相关的 API 接口

import request from './axios'
import publicRequest from './config/interceptors/publicAxios'

// 创建笔记
export const createNote = (data) => {
  return request({
    url: '/note',
    method: 'post',
    data
  })
}

// 更新笔记
export const updateNote = (noteId, data) => {
  return request({
    url: `/note/${noteId}`,
    method: 'put',
    data
  })
}

// 删除笔记
export const deleteNote = (noteId) => {
  return request({
    url: `/note/${noteId}`,
    method: 'delete'
  })
}

// 获取笔记详情
export const getNoteDetail = (noteId) => {
  return request({
    url: `/note/${noteId}`,
    method: 'get'
  })
}

// 保存笔记草稿
export const saveNoteDraft = (data) => {
  return request({
    url: '/note/draft',
    method: 'post',
    data
  })
}

// 发布笔记
export const publishNote = (noteId) => {
  return request({
    url: `/note/${noteId}/publish`,
    method: 'put'
  })
}

// 获取笔记列表
export const getNoteList = (params) => {
  return request({
    url: '/note/list',
    method: 'get',
    params
  })
}

// 获取公开笔记列表
export const getPublicNotes = (params) => {
  return request({
    url: '/note/public',
    method: 'get',
    params
  })
}

// 收藏他人笔记
export const collectNote = (noteId) => {
  return request({
    url: `/note/${noteId}/collect`,
    method: 'post'
  })
}

// 取消收藏笔记
export const uncollectNote = (noteId) => {
  return request({
    url: `/note/${noteId}/collect`,
    method: 'delete'
  })
}

// 获取我收藏的笔记
export const getCollectedNotes = (params) => {
  return request({
    url: '/note/collected',
    method: 'get',
    params
  })
}

// 获取草稿列表
export const getDraftList = (params) => {
  return request({
    url: '/note/draft/list',
    method: 'get',
    params
  })
}

// 获取笔记模板
export const getNoteTemplate = (type = 'default') => {
  return request({
    url: `/note/template/${type}`,
    method: 'get'
  })
}

// 获取推荐笔记（支持匿名访问）
// 用于产品首页展示，按热度评分排序
export const getRecommendedNotes = (pageNum = 1, pageSize = 10) => {
  return publicRequest({
    url: '/note/recommended',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

// 封装成 noteApi 对象导出
export const noteApi = {
  createNote,
  updateNote,
  deleteNote,
  getNoteDetail,
  saveNoteDraft,
  publishNote,
  getNoteList,
  getPublicNotes,
  collectNote,
  uncollectNote,
  getCollectedNotes,
  getDraftList,
  getNoteTemplate,
  getRecommendedNotes
}