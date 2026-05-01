// 用户互动相关API接口
import request from '@/api/axios'

// 用户点赞
export const likeContent = (targetId, targetType) => {
  return request({
    url: '/interaction/like',
    method: 'post',
    data: { targetId, targetType }
  })
}

// 用户取消点赞
// 后端使用 @RequestBody 接收参数，必须用 data 而非 params
export const unlikeContent = (targetId, targetType) => {
  return request({
    url: '/interaction/like',
    method: 'delete',
    data: { targetId, targetType }
  })
}

// 用户发表评论
// @param {number|string} targetId - 目标内容ID
// @param {string} targetType - 目标类型 (note, collection, collection_item)
// @param {string} content - 评论内容
// @param {number|string} parentId - 父评论ID（可选，用于回复）
export const commentContent = (targetId, targetType, content, parentId = null) => {
  return request({
    url: '/interaction/comment',
    method: 'post',
    data: { 
      targetId, 
      targetType, 
      content,
      parentId  // 支持嵌套回复
    }
  })
}

// 用户收藏
export const collectContent = (targetId, targetType) => {
  return request({
    url: '/interaction/collect',
    method: 'post',
    data: { targetId, targetType }
  })
}

// 用户取消收藏
// 后端使用 @RequestBody 接收参数，必须用 data 而非 params
export const uncollectContent = (targetId, targetType) => {
  return request({
    url: '/interaction/collect',
    method: 'delete',
    data: { targetId, targetType }
  })
}

// 获取内容点赞数
export const getLikeCount = (targetId, targetType) => {
  return request({
    url: '/interaction/like/count',
    method: 'get',
    params: { targetId, targetType }
  })
}

// 获取内容评论列表
export const getComments = (targetId, targetType, pageNum = 1, pageSize = 20) => {
  return request({
    url: '/interaction/comment/list',
    method: 'get',
    params: { targetId, targetType, pageNum, pageSize }
  })
}

// 获取内容收藏数
export const getCollectCount = (targetId, targetType) => {
  return request({
    url: '/interaction/collect/count',
    method: 'get',
    params: { targetId, targetType }
  })
}

// 检查用户是否已点赞
export const checkLikeStatus = (targetId, targetType) => {
  return request({
    url: '/interaction/like/check',
    method: 'get',
    params: { targetId, targetType }
  })
}

// 检查用户是否已收藏
export const checkCollectStatus = (targetId, targetType) => {
  return request({
    url: '/interaction/collect/check',
    method: 'get',
    params: { targetId, targetType }
  })
}

// 获取评论列表（带父评论筛选）
// @param {number|string} targetId - 目标ID
// @param {string} targetType - 目标类型 (note, collection, etc.)
// @param {number|string} parentId - 父评论ID（可选，用于获取子评论）
// @param {number} pageNum - 页码
// @param {number} pageSize - 每页条数
export const getCommentList = (targetId, targetType, parentId = null, pageNum = 1, pageSize = 20) => {
  return request({
    url: '/interaction/comment/list',
    method: 'get',
    params: { targetId, targetType, parentId, pageNum, pageSize }
  })
}

// 删除点赞（取消点赞的别名）
// @param {Object} data - { targetId, targetType }
// 后端使用 @RequestBody 接收参数，必须用 data 而非 params
export const deleteLike = (data) => {
  return request({
    url: '/interaction/like',
    method: 'delete',
    data
  })
}

// 导出互动数据
// @param {Object} params - { tab, filterStatus, startDate, endDate }
export const exportInteractions = (params) => {
  return request({
    url: '/interaction/export',
    method: 'post',
    data: params,
    responseType: 'blob' // 重要：接收文件流
  })
}
