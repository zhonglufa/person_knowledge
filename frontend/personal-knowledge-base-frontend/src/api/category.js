import request from '@/api/axios'

// 获取分类树
export function getCategoryTree() {
  return request({
    url: '/categories/tree',
    method: 'get'
  })
}

// 获取分类平铺列表
export function getCategoryList() {
  return request({
    url: '/categories/list',
    method: 'get'
  })
}

// 获取分类统计信息
export function getCategoryStatistics() {
  return request({
    url: '/categories/statistics',
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}

// 移动分类
export function moveCategory(categoryId, newParentId) {
  return request({
    url: `/categories/${categoryId}/move`,
    method: 'put',
    data: { newParentId }
  })
}