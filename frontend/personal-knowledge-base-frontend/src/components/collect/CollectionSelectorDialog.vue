<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="移动到收藏集"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-input
      v-model="searchKeyword"
      placeholder="搜索收藏集..."
      prefix-icon="el-icon-search"
      clearable
      class="mb-3"
    />
    
    <div class="dialog-body">
      <div v-if="loading" class="loading-container">
        <i class="el-icon-loading"></i>
        <span>加载中...</span>
      </div>
      
      <div v-else-if="filteredCollections.length > 0" class="collection-list">
        <div
          v-for="collection in filteredCollections"
          :key="collection.id"
          :class="['collection-item', { 
            'selected': selectedCollectionId === collection.id,
            'current': collection.id === currentCollectionId 
          }]"
          @click="selectCollection(collection.id)"
        >
          <i class="el-icon-folder folder-icon"></i>
          <div class="collection-info">
            <div class="collection-name">{{ collection.name }}</div>
            <div class="collection-count">{{ collection.itemCount || 0 }} 项</div>
          </div>
          <i v-if="selectedCollectionId === collection.id" class="el-icon-check check-icon"></i>
          <el-tag v-if="collection.id === currentCollectionId" size="small" type="info">
            当前
          </el-tag>
        </div>
      </div>
      
      <div v-else class="empty-container">
        <i class="el-icon-folder-opened empty-icon"></i>
        <p>没有找到收藏集</p>
      </div>
    </div>
    
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button 
        type="primary" 
        :disabled="!selectedCollectionId || selectedCollectionId === currentCollectionId"
        :loading="moving"
        @click="confirmMove"
      >
        确认移动
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { Message } from 'element-ui'
import { getUserCollections } from '@/api/collections'
import { collectApi } from '@/api/collect'

export default {
  name: 'CollectionSelectorDialog',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    value: {
      type: Boolean,
      default: false
    },
    itemIds: {
      type: Array,
      default: () => []
    },
    currentCollectionId: {
      type: Number,
      default: null
    }
  },
  data() {
    return {
      collections: [],
      searchKeyword: '',
      selectedCollectionId: null,
      moving: false,
      loading: false
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.modelValue || this.value;
      },
      set(val) {
        this.$emit('update:modelValue', val);
        this.$emit('input', val);
      }
    },
    filteredCollections() {
      if (!this.searchKeyword) return this.collections
      
      const keyword = this.searchKeyword.toLowerCase()
      return this.collections.filter(c => 
        c.name.toLowerCase().includes(keyword) ||
        c.description?.toLowerCase().includes(keyword)
      )
    }
  },
  watch: {
    dialogVisible(val) {
      if (val) {
        this.loadCollections()
        this.selectedCollectionId = null
        this.searchKeyword = ''
      }
    }
  },
  methods: {
    async loadCollections() {
      this.loading = true
      try {
        const res = await getUserCollections({ pageSize: 999 })
        this.collections = res.data.records || []
      } catch (error) {
        console.error('加载收藏集失败:', error)
        Message.error('加载收藏集失败')
      } finally {
        this.loading = false
      }
    },
    selectCollection(id) {
      if (id === this.currentCollectionId) {
        Message.warning('已在当前收藏集中')
        return
      }
      this.selectedCollectionId = id
    },
    async confirmMove() {
      if (!this.selectedCollectionId) return
      
      const itemIds = this.itemIds
      if (!itemIds || itemIds.length === 0) {
        Message.warning('没有选择要移动的收藏项')
        return
      }
      
      this.moving = true
      try {
        if (itemIds.length === 1) {
          await collectApi.moveItem(itemIds[0], this.selectedCollectionId)
          Message.success('移动成功')
        } else {
          await collectApi.batchMoveItems(itemIds, this.selectedCollectionId)
          Message.success(`成功移动 ${itemIds.length} 项`)
        }
        
        this.dialogVisible = false
        this.$emit('moved', this.selectedCollectionId)
      } catch (error) {
        console.error('移动失败:', error)
        Message.error(error.message || '移动失败')
      } finally {
        this.moving = false
      }
    },
    handleClose() {
      this.selectedCollectionId = null
      this.searchKeyword = ''
    }
  }
}
</script>

<style scoped lang="scss">
.mb-3 {
  margin-bottom: 16px;
}

.dialog-body {
  max-height: 400px;
  overflow-y: auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: var(--el-text-color-secondary);
  
  i {
    font-size: 32px;
    margin-bottom: 12px;
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #909399;
  
  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }
  
  p {
    margin: 0;
    font-size: 14px;
  }
}

.collection-list {
  .collection-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 8px;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    &.selected {
      background-color: #ecf5ff;
      border: 1px solid #409eff;
    }
    
    &.current {
      opacity: 0.6;
      cursor: not-allowed;
      
      &:hover {
        background-color: transparent;
      }
    }
    
    .folder-icon {
      font-size: 24px;
      color: #409eff;
      margin-right: 12px;
      flex-shrink: 0;
    }
    
    .collection-info {
      flex: 1;
      min-width: 0;
      
      .collection-name {
        font-weight: 500;
        margin-bottom: 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .collection-count {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .check-icon {
      color: #409eff;
      font-size: 20px;
      margin-left: 8px;
      flex-shrink: 0;
    }
    
    .el-tag {
      margin-left: 8px;
      flex-shrink: 0;
    }
  }
}
</style>
