# Collect Store Module

## 概述
Collect模块是个人知识库前端应用中用于管理收藏功能的Vuex状态管理模块。它提供收藏列表、回收站列表、标签筛选、统计信息与分页加载能力，并保证回收站相关数据以后端真实结果为准。

## 模块结构

### State 状态
```javascript
{
  collectList: [],
  pagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  loading: false,
  searchKeyword: '',
  collectTags: [],
  selectedCollectTags: [],
  collectFilterCondition: {
    type: '0',
    tagId: null,
    tagIds: [],
    status: null
  },
  statistics: {
    totalCollects: 0,
    todayCollects: 0,
    totalTags: 0,
    unreadCollects: 0,
    recycledCollections: 0
  }
}
```

### Getters 计算属性
- `getTotalCollections`: 获取总收藏数
- `getTodayCollections`: 获取今日新增收藏数
- `getUnreadCollections`: 获取未读收藏数
- `getRecycledCollections`: 获取回收站收藏数
- `getTotalTags`: 获取标签总数
- `getCurrentPageCollections`: 获取当前页收藏列表切片

### Mutations 状态变更
- `SET_COLLECT_LIST`: 设置当前展示列表
- `SET_PAGINATION`: 设置分页信息
- `SET_LOADING`: 设置加载状态
- `SET_SEARCH_KEYWORD`: 设置搜索关键词
- `SET_COLLECT_TAGS`: 设置标签列表
- `SET_SELECTED_COLLECT_TAGS`: 设置选中的标签
- `SET_COLLECT_FILTER_CONDITION`: 设置筛选条件
- `RESET_COLLECT_FILTER_CONDITION`: 重置筛选条件
- `SET_STATISTICS`: 设置统计信息
- `ADD_COLLECTION_ITEM`: 新增收藏项
- `UPDATE_COLLECTION_ITEM`: 更新收藏项
- `DELETE_COLLECTION_ITEM`: 从当前列表移除已删除项
- `RECOVER_COLLECTION_ITEM`: 从当前回收站列表移除已恢复项
- `PERMANENT_DELETE_COLLECTION_ITEM`: 从当前回收站列表移除已永久删除项
- `BATCH_DELETE_COLLECTION_ITEMS`: 批量从当前列表移除已删除项
- `BATCH_RECOVER_COLLECTION_ITEMS`: 批量从当前回收站列表移除已恢复项
- `BATCH_PERMANENT_DELETE_COLLECTION_ITEMS`: 批量从当前回收站列表移除已永久删除项

### Actions 异步操作
- `getCollectList`: 获取正常收藏列表
- `getRecycleBinList`: 获取回收站列表
- `getCollectListByTag`: 根据标签获取收藏列表
- `getCollectByCategory`: 根据分类获取收藏
- `getCollectTags`: 获取标签列表
- `getCollectStatistics`: 获取统计信息，并补齐回收站真实数量
- `deleteCollection`: 软删除收藏项
- `recoverCollection`: 恢复收藏项
- `permanentDeleteCollection`: 永久删除收藏项
- `batchDeleteItems`: 批量软删除
- `batchRecoverItems`: 批量恢复
- `batchPermanentDeleteItems`: 批量永久删除

## 回收站数据规则

### 1. 列表来源
回收站列表不再复用普通 `/collect/list` 接口，而是调用专门的回收站接口：
- `GET /collect/recycle-bin`

这样可以确保右侧显示的就是 `deleted = 1` 的软删除收藏项，而不是未删除数据。

### 2. badge 与统计来源
回收站数量不再依赖前端本地加减推算，而是通过回收站接口返回的 `total` 字段获取真实总数，再写入：
- `statistics.recycledCollections`

普通统计字段则对后端返回字段做标准化映射：
- `total -> totalCollects`
- `unreadCount -> unreadCollects`

### 3. 删除/恢复/永久删除后的刷新策略
删除、恢复、批量恢复、永久删除、批量永久删除完成后，页面统一执行：
1. 重新加载当前分类列表
2. 重新拉取统计信息

这样可以保证：
- 回收站列表
- 正常收藏列表
- store 统计
- 侧边栏 badge

都以后端真实结果为准。

## 在组件中使用

### 映射 Actions
```javascript
...mapActions('collect', [
  'getCollectList',
  'getRecycleBinList',
  'getCollectTags',
  'getCollectListByTag',
  'getCollectByCategory',
  'getCollectStatistics',
  'deleteCollection',
  'recoverCollection',
  'permanentDeleteCollection',
  'batchDeleteItems',
  'batchRecoverItems',
  'batchPermanentDeleteItems'
])
```

### 切换到回收站时加载
```javascript
if (this.activeSidebarItem === 'recycle') {
  await this.getRecycleBinList({
    pageNum: 1,
    pageSize: this.pageSize
  });
}
```

### 变更后的统一刷新
```javascript
async refreshAfterMutation() {
  await this.loadCollectionByCategory(this.activeSidebarItem);
  await this.loadStatistics();
}
```

## 产品验证建议

建议按以下路径验证：
1. 点击回收站，右侧是否展示软删除收藏项列表
2. 普通列表删除后，回收站 badge 是否立即反映真实数量
3. 回收站恢复后，该项是否从回收站列表移除
4. 批量恢复后，回收站列表、badge、统计是否同步更新
5. 回收站无限滚动加载下一页后，再执行恢复/永久删除，列表是否仍然一致
6. 页面刷新后，回收站数量是否仍然正确

## 注意事项
1. 回收站属于可信数据域，不能只靠前端本地计数推算
2. 列表与统计必须尽量通过后端接口重新收敛
3. 标签总数当前仍来自已使用标签接口返回条数，不代表后端全量标签总数
4. `todayCollects` 若后端未返回真实字段，则保持默认值
