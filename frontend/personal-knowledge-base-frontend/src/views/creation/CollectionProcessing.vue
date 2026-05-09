<template>
  <div class="collection-processing">
    <div class="processing-overview">
      <div class="overview-card" v-for="metric in overviewMetrics" :key="metric.key">
        <div class="overview-icon" :class="metric.theme">
          <i :class="metric.icon"></i>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ metric.value }}</div>
          <div class="overview-label">{{ metric.label }}</div>
        </div>
      </div>
    </div>

    <div class="processing-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddCollectionItem">
          添加收藏项
        </el-button>
        <el-button icon="el-icon-refresh" @click="handleRefresh">
          刷新
        </el-button>
        <el-button icon="el-icon-s-operation" @click="showBatchDialog = true" :disabled="selectedItems.length === 0">
          批量操作 ({{ selectedItems.length }})
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索收藏项"
          prefix-icon="el-icon-search"
          clearable
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
          class="search-input"
        />
        <el-select
          v-model="filterStatus"
          placeholder="加工状态"
          clearable
          @change="handleFilterChange"
          class="filter-select"
        >
          <el-option label="全部状态" value="" />
          <el-option label="未消化" value="undigest" />
          <el-option label="消化中" value="digesting" />
          <el-option label="已消化" value="digested" />
          <el-option label="已放弃" value="abandoned" />
        </el-select>
        <el-select
          v-model="filterSource"
          placeholder="来源筛选"
          clearable
          @change="handleFilterChange"
          class="filter-select"
        >
          <el-option label="全部来源" value="" />
          <el-option label="网页" value="web" />
          <el-option label="文本" value="text" />
          <el-option label="图片" value="image" />
          <el-option label="视频" value="video" />
        </el-select>
      </div>
    </div>

    <div v-if="selectedItems.length > 0" class="batch-operation-bar">
      <div class="batch-info">已选择 {{ selectedItems.length }} 项收藏项</div>
      <div class="batch-actions">
        <el-button type="primary" size="small" @click="handleBatchProcess">批量加工</el-button>
        <el-button type="success" size="small" @click="handleBatchComplete">标记完成</el-button>
        <el-button type="warning" size="small" @click="handleBatchAbandon">标记放弃</el-button>
        <el-button size="small" @click="clearSelection">取消选择</el-button>
      </div>
    </div>

    <div class="processing-workbench" v-loading="loading">
      <div class="workbench-list">
        <div class="list-header">
          <div>
            <h3>收藏项列表</h3>
            <p>围绕收藏项推进摘要提炼、学习目标与后续沉淀。</p>
          </div>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="grid">卡片</el-radio-button>
            <el-radio-button label="list">列表</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="!loading && filteredItems.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-seedling"></i>
          </div>
          <p class="empty-text">暂无收藏项</p>
          <p class="empty-desc">先收藏内容，再回到这里推进你的知识加工流程。</p>
          <el-button type="primary" @click="handleAddCollectionItem">添加收藏项</el-button>
        </div>

        <div v-else-if="viewMode === 'grid'" class="card-list">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            class="processing-card"
            :class="{ active: activeItem && activeItem.id === item.id }"
            @click="handleItemClick(item)"
          >
            <div class="card-top">
              <el-checkbox
                v-model="item.selected"
                @change="handleItemSelect(item)"
                @click.native.stop
              />
              <el-tag :type="getStatusTagType(item.digestStatus)" size="small">{{ getStatusText(item.digestStatus) }}</el-tag>
            </div>
            <div class="card-title">{{ item.title }}</div>
            <div class="card-meta">{{ getSourceText(item.source) }} · {{ item.collectionName || '未分类' }}</div>
            <div class="card-abstract">{{ getAbstract(item) }}</div>
            <div class="card-progress">
              <div class="progress-row">
                <span>学习进度</span>
                <span>{{ item.studyProgress || 0 }}%</span>
              </div>
              <el-progress :percentage="item.studyProgress || 0" :show-text="false" :stroke-width="6" />
            </div>
            <div class="card-actions" @click.stop>
              <el-button type="text" size="small" @click="handleProcessItem(item)">加工</el-button>
              <el-button type="text" size="small" @click="handleCreateNote(item)">写笔记</el-button>
            </div>
          </div>
        </div>

        <div v-else class="table-list">
          <el-table :data="filteredItems" @selection-change="handleSelectionChange" style="width: 100%">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column prop="source" label="来源" width="100" align="center">
              <template slot-scope="scope">{{ getSourceText(scope.row.source) }}</template>
            </el-table-column>
            <el-table-column prop="digestStatus" label="状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getStatusTagType(scope.row.digestStatus)" size="small">{{ getStatusText(scope.row.digestStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学习进度" width="120" align="center">
              <template slot-scope="scope">{{ scope.row.studyProgress || 0 }}%</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
              <template slot-scope="scope">{{ formatDate(scope.row.updateTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="handleProcessItem(scope.row)">加工</el-button>
                <el-button type="text" size="small" @click="handleCreateNote(scope.row)">写笔记</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="workbench-detail">
        <div v-if="activeItem" class="detail-card">
          <div class="detail-header">
            <div>
              <h3>{{ activeItem.title }}</h3>
              <p>{{ getSourceText(activeItem.source) }} · {{ activeItem.collectionName || '未分类' }}</p>
            </div>
            <el-tag :type="getStatusTagType(activeItem.digestStatus)">{{ getStatusText(activeItem.digestStatus) }}</el-tag>
          </div>

          <div class="detail-section">
            <div class="section-title">核心摘要</div>
            <div class="section-content">{{ activeItem.coreAbstract || '尚未提炼摘要，建议先概括关键观点。' }}</div>
          </div>

          <div class="detail-grid">
            <div class="detail-section">
              <div class="section-title">学习目标</div>
              <div class="section-content">{{ activeItem.studyGoal || '尚未设置学习目标' }}</div>
            </div>
            <div class="detail-section">
              <div class="section-title">关键词</div>
              <div class="section-content">{{ activeItem.keywords || '尚未补充关键词' }}</div>
            </div>
          </div>

          <div class="detail-grid">
            <div class="detail-section">
              <div class="section-title">提醒时间</div>
              <div class="section-content">{{ formatDate(activeItem.remindAt) || '未设置提醒' }}</div>
            </div>
            <div class="detail-section">
              <div class="section-title">学习进度</div>
              <div class="section-content">{{ activeItem.studyProgress || 0 }}%</div>
            </div>
          </div>

          <div class="detail-actions">
            <el-button type="primary" @click="handleProcessItem(activeItem)">继续加工</el-button>
            <el-button @click="handleCreateNote(activeItem)">创建笔记</el-button>
            <el-button type="success" plain @click="handleMarkComplete(activeItem)">标记完成</el-button>
            <el-button type="warning" plain @click="handleMarkAbandon(activeItem)">标记放弃</el-button>
          </div>
        </div>
        <div v-else class="detail-placeholder">
          <i class="fas fa-book-open"></i>
          <h3>选择一个收藏项</h3>
          <p>右侧将展示该收藏项的摘要、学习目标与后续沉淀动作。</p>
        </div>
      </div>
    </div>

    <div v-if="filteredItems.length > 0" class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[12, 24, 48, 96]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
      />
    </div>

    <CollectionItemDigestEditor :visible.sync="showProcessDialog" :item-id="currentItemId" @success="handleProcessSuccess" />
    <BatchProcessingDialog :visible.sync="showBatchDialog" :selected-items="selectedItems" @success="handleBatchSuccess" />
    <AddCollectionItemDialog :visible.sync="showAddDialog" :collection-id="null" @success="handleAddSuccess" />
  </div>
</template>

<script>
import { collectApi } from '@/api/collect'
import CollectionItemDigestEditor from '@/components/collect/CollectionItemDigestEditor.vue'
import BatchProcessingDialog from '@/components/collect/BatchProcessingDialog.vue'
import AddCollectionItemDialog from '@/components/collect/AddCollectionItemDialog.vue'

export default {
  name: 'CollectionProcessing',
  components: {
    CollectionItemDigestEditor,
    BatchProcessingDialog,
    AddCollectionItemDialog
  },
  data() {
    return {
      viewMode: 'grid',
      loading: false,
      searchKeyword: '',
      filterStatus: '',
      filterSource: '',
      currentPage: 1,
      pageSize: 12,
      total: 0,
      items: [],
      selectedItems: [],
      showProcessDialog: false,
      showBatchDialog: false,
      showAddDialog: false,
      currentItemId: null,
      activeItem: null
    }
  },
  computed: {
    filteredItems() {
      let result = [...this.items]
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(item =>
          item.title?.toLowerCase().includes(keyword) ||
          item.content?.toLowerCase().includes(keyword) ||
          item.coreAbstract?.toLowerCase().includes(keyword)
        )
      }
      if (this.filterStatus) {
        result = result.filter(item => item.digestStatus === this.filterStatus)
      }
      if (this.filterSource) {
        result = result.filter(item => item.source === this.filterSource)
      }
      return result
    },
    overviewMetrics() {
      const total = this.items.length
      const undigest = this.items.filter(item => item.digestStatus === 'undigest').length
      const digesting = this.items.filter(item => item.digestStatus === 'digesting').length
      const drafts = this.items.filter(item => (item.noteCount || 0) === 0).length
      return [
        { key: 'total', label: '待加工总数', value: total, icon: 'fas fa-inbox', theme: 'primary' },
        { key: 'undigest', label: '未消化', value: undigest, icon: 'fas fa-clock', theme: 'warning' },
        { key: 'digesting', label: '消化中', value: digesting, icon: 'fas fa-seedling', theme: 'success' },
        { key: 'drafts', label: '待沉淀', value: drafts, icon: 'fas fa-note-sticky', theme: 'info' }
      ]
    }
  },
  methods: {
    async loadItems() {
      this.loading = true
      try {
        const response = await collectApi.getCollectList({
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword,
          status: this.filterStatus,
          source: this.filterSource
        })
        this.items = (response.data.records || []).map(item => ({
          ...item,
          selected: false,
          studyProgress: parseInt(item.studyProgress) || 0,
          collectionName: item.collectionName || (item.collection ? item.collection.name : null)
        }))
        this.total = response.data.total || 0
        if (this.items.length > 0) {
          const current = this.activeItem && this.items.find(item => item.id === this.activeItem.id)
          this.activeItem = current || this.items[0]
        } else {
          this.activeItem = null
        }
      } catch (error) {
        console.error('加载收藏项失败:', error)
        this.$message.error('加载收藏项失败')
        this.items = []
        this.total = 0
        this.activeItem = null
      } finally {
        this.loading = false
      }
    },
    async handleAddCollectionItem() {
      this.showAddDialog = true
    },
    handleItemClick(item) {
      this.activeItem = item
    },
    handleItemSelect(item) {
      const index = this.selectedItems.indexOf(item.id)
      if (item.selected) {
        if (index === -1) this.selectedItems.push(item.id)
      } else if (index > -1) {
        this.selectedItems.splice(index, 1)
      }
    },
    handleSelectionChange(selection) {
      this.selectedItems = selection.map(item => item.id)
      this.items.forEach(item => {
        item.selected = this.selectedItems.includes(item.id)
      })
      this.activeItem = selection[0] || this.activeItem
    },
    clearSelection() {
      this.selectedItems = []
      this.items.forEach(item => {
        item.selected = false
      })
    },
    handleProcessItem(item) {
      this.currentItemId = item.id
      this.activeItem = item
      this.showProcessDialog = true
    },
    handleCreateNote(item) {
      this.$router.push(`/collection-item/${item.id}/note/create`)
    },
    async handleMarkComplete(item) {
      try {
        await collectApi.updateCollectionItemStatus(item.id, 'digested')
        this.$message.success('标记完成成功')
        this.loadItems()
      } catch (error) {
        console.error('标记完成失败:', error)
        this.$message.error('操作失败')
      }
    },
    async handleMarkAbandon(item) {
      try {
        await this.$confirm('确定要放弃这个收藏项的加工吗？', '确认放弃', { type: 'warning' })
        await collectApi.updateCollectionItemStatus(item.id, 'abandoned')
        this.$message.success('标记放弃成功')
        this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('标记放弃失败:', error)
          this.$message.error('操作失败')
        }
      }
    },
    handleBatchProcess() {
      this.showBatchDialog = true
    },
    async handleBatchComplete() {
      try {
        await this.$confirm(`确定要将选中的 ${this.selectedItems.length} 项标记为完成吗？`, '确认操作', { type: 'warning' })
        await collectApi.batchUpdateCollectionItemStatus(this.selectedItems, 'digested')
        this.$message.success('批量标记完成成功')
        this.clearSelection()
        this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量操作失败:', error)
          this.$message.error('操作失败')
        }
      }
    },
    async handleBatchAbandon() {
      try {
        await this.$confirm(`确定要放弃选中的 ${this.selectedItems.length} 项的加工吗？`, '确认放弃', { type: 'warning' })
        await collectApi.batchUpdateCollectionItemStatus(this.selectedItems, 'abandoned')
        this.$message.success('批量标记放弃成功')
        this.clearSelection()
        this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量操作失败:', error)
          this.$message.error('操作失败')
        }
      }
    },
    handleProcessSuccess() {
      this.showProcessDialog = false
      this.loadItems()
    },
    handleBatchSuccess() {
      this.showBatchDialog = false
      this.clearSelection()
      this.loadItems()
    },
    handleAddSuccess() {
      this.showAddDialog = false
      this.loadItems()
    },
    handleRefresh() {
      this.loadItems()
    },
    handleSearch() {
      this.currentPage = 1
      this.loadItems()
    },
    handleFilterChange() {
      this.currentPage = 1
      this.loadItems()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadItems()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadItems()
    },
    getStatusText(status) {
      const texts = {
        undigest: '未消化',
        digesting: '消化中',
        digested: '已消化',
        abandoned: '已放弃'
      }
      return texts[status] || '未消化'
    },
    getStatusTagType(status) {
      const types = {
        undigest: 'info',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'danger'
      }
      return types[status] || 'info'
    },
    getSourceText(source) {
      const texts = {
        web: '网页',
        text: '文本',
        image: '图片',
        video: '视频'
      }
      return texts[source] || '未知'
    },
    getAbstract(item) {
      return item.coreAbstract || item.content?.substring(0, 100) + '...' || '暂无内容'
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    }
  },
  mounted() {
    this.loadItems()
  }
}
</script>

<style scoped>
.collection-processing {
  height: 100%;
}

.processing-overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.overview-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
}

.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.overview-icon.primary { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.overview-icon.warning { background: linear-gradient(135deg, #f59e0b, #f97316); }
.overview-icon.success { background: linear-gradient(135deg, #10b981, #06b6d4); }
.overview-icon.info { background: linear-gradient(135deg, #3b82f6, #06b6d4); }

.overview-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.overview-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.processing-toolbar,
.batch-operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
}

.processing-toolbar {
  margin-bottom: var(--space-4);
}

.toolbar-left,
.toolbar-right,
.batch-actions {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.search-input { width: 240px; }
.filter-select { width: 140px; }

.batch-operation-bar {
  padding: var(--space-3) var(--space-4);
  background: var(--primary-bg);
  border: 1px solid var(--primary-light);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-4);
}

.processing-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(320px, 0.9fr);
  gap: var(--space-5);
}

.workbench-list,
.workbench-detail {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.list-header h3,
.detail-header h3 {
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
}

.list-header p,
.detail-header p,
.detail-placeholder p {
  margin: 0;
  color: var(--text-secondary);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.processing-card {
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  cursor: pointer;
  transition: all var(--transition-base);
}

.processing-card:hover,
.processing-card.active {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  background: var(--primary-bg);
}

.card-top,
.card-actions,
.progress-row,
.detail-header,
.detail-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.card-title {
  margin: var(--space-3) 0 var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
}

.card-meta,
.card-abstract,
.section-content {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.card-abstract {
  margin-top: var(--space-2);
  line-height: 1.6;
}

.card-progress {
  margin-top: var(--space-3);
}

.detail-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.section-title {
  color: var(--text-primary);
  font-weight: 600;
  margin-bottom: var(--space-2);
}

.detail-placeholder,
.empty-state {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: var(--space-3);
}

.detail-placeholder i,
.empty-icon i {
  font-size: 40px;
  color: var(--primary-color);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--space-5);
}

@media (max-width: 1200px) {
  .processing-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .processing-workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .processing-overview {
    grid-template-columns: 1fr;
  }

  .processing-toolbar,
  .batch-operation-bar,
  .toolbar-left,
  .toolbar-right,
  .detail-grid,
  .detail-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }
}
</style>
