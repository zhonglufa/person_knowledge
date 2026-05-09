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
// ⚠️ 警告：笔记收藏请使用 /note/{noteId}/collect 接口（见 note.js 的 collectNote）
// 本接口仅支持 targetType = 'collection' 或 'collection_item'
// 后端已禁止 targetType = 'note'，调用会返回错误
export const collectContent = (targetId, targetType) => {
  return request({
    url: '/interaction/collect',
    method: 'post',
    data: { targetId, targetType }
  })
}

// 用户取消收藏
// ⚠️ 警告：笔记取消收藏请使用 /note/{noteId}/collect DELETE 接口（见 note.js 的 uncollectNote）
// 本接口仅支持 targetType = 'collection' 或 'collection_item'
// 后端已禁止 targetType = 'note'，调用会返回错误
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
export const getComments = (targetId, targetType, parentId = null, pageNum = 1, pageSize = 20) => {
  return request({
    url: '/interaction/comment/list',
    method: 'get',
    params: { targetId, targetType, parentId, pageNum, pageSize }
  })
}

export const deleteComment = (commentId) => {
  return request({
    url: `/interaction/comment/${commentId}`,
    method: 'delete'
  })
}

// 获取内容收藏数
// ⚠️ 警告：笔记收藏数统计后端已禁用，请使用笔记模块专用接口
// 本接口仅支持 targetType = 'collection' 或 'collection_item'
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
// ⚠️ 警告：笔记收藏状态检查后端已禁用，请使用笔记模块专用接口
// 本接口仅支持 targetType = 'collection' 或 'collection_item'
export const checkCollectStatus = (targetId, targetType) => {
  return request({
    url: '/interaction/collect/check',
    method: 'get',
    params: { targetId, targetType }
  })
}

