<template>
  <div class="collection-grid-view">
    <!-- 空状态提�?-->
    <div v-if="collections.length === 0" class="empty-state">
      <i class="fas fa-inbox"></i>
      <p>还没有创建收藏集</p>
      <el-button type="primary" @click="$emit('create-collection')">
        <i class="fas fa-plus"></i>
        创建第一个收藏集
      </el-button>
    </div>

    <!-- 收藏集网�?-->
    <div v-else class="grid-container">
      <collection-card
        v-for="collection in collections"
        :key="collection.id"
        :collection="collection"
        :selected="selectedCollections.includes(collection.id)"
        :show-selection-mode="showSelectionMode"
        @collection-click="$emit('collection-click', collection)"
        @show-context-menu="$emit('show-context-menu', $event, collection)"
        @edit-collection="$emit('edit-collection', collection)"
        @delete-collection="$emit('delete-collection', collection)"
        @process-collection="$emit('process-collection', collection)"
        @toggle-selection="$emit('toggle-selection', $event)"
      />
    </div>
  </div>
</template>

<script>
import CollectionCard from './CollectionCard.vue';

/**
 * CollectionGridView 组件
 * 功能：网格布局展示多个收藏集卡�?
 * 
 * Props:
 *   - collections: Array - 收藏集列�?
 *   - selectedCollections: Array - 选中的收藏集ID列表
 *   - showSelectionMode: Boolean - 是否显示选择模式
 * 
 * Emits:
 *   - create-collection: 创建新收藏集
 *   - collection-click: 收藏集卡片被点击
 *   - show-context-menu: 右键菜单
 *   - edit-collection: 编辑收藏�?
 *   - delete-collection: 删除收藏�?
 *   - toggle-selection: 切换选中状�?
 */
export default {
  name: 'CollectionGridView',
  components: {
    CollectionCard
  },
  props: {
    collections: {
      type: Array,
      default: () => []
    },
    selectedCollections: {
      type: Array,
      default: () => []
    },
    showSelectionMode: {
      type: Boolean,
      default: false
    }
  },
  emits: [
    'create-collection',
    'collection-click',
    'show-context-menu',
    'edit-collection',
    'delete-collection',
    'process-collection',
    'toggle-selection'
  ]
}
</script>

<style scoped>
/* 网格容器 */
.collection-grid-view {
  width: 100%;
  padding: 0;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 0;
  margin: 0;
}

/* 空状�?*/
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 400px;
  color: var(--text-light, #999);
  text-align: center;
}

.empty-state i {
  font-size: 64px;
  color: var(--border-light, #ddd);
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 24px;
}

/* 响应式设�?*/
@media (max-width: 1200px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .grid-container {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .empty-state {
    padding: 40px 16px;
    min-height: 300px;
  }

  .empty-state i {
    font-size: 48px;
  }

  .empty-state p {
    font-size: 14px;
  }
}
</style>
