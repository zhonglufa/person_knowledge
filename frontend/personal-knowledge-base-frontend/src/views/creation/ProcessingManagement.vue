<template>
  <div class="processing-management-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
              <el-breadcrumb-item>加工管理</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <h1 class="page-title">加工管理</h1>
          <p class="page-subtitle">这是创作中心的过渡工作页，后续会进一步并入创作工作台与加工流程。</p>
        </div>
        <div class="header-right">
          <el-button type="text" @click="$router.push('/creation/workspace')">
            前往创作工作台 <i class="fas fa-arrow-right"></i>
          </el-button>
          <el-button type="primary" @click="startProcessing">
            <i class="fas fa-play"></i>
            开始加工
          </el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索加工项目"
            prefix-icon="fas fa-search"
            class="search-input"
            @input="handleSearch"
          />
        </div>
        <div class="toolbar-right">
          <el-select
            v-model="filterStatus"
            placeholder="状态筛选"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="all"></el-option>
            <el-option label="未加工" value="undigest"></el-option>
            <el-option label="加工中" value="digesting"></el-option>
            <el-option label="已加工" value="digested"></el-option>
            <el-option label="已放弃" value="abandoned"></el-option>
          </el-select>
          <el-select
            v-model="sortBy"
            placeholder="排序方式"
            @change="handleSortChange"
          >
            <el-option label="加工进度" value="progress"></el-option>
            <el-option label="创建时间" value="createTime"></el-option>
            <el-option label="更新时间" value="updateTime"></el-option>
            <el-option label="标题" value="title"></el-option>
          </el-select>
        </div>
      </div>

      <div class="processing-stats">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-bookmark"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.total }}</div>
            <div class="stat-label">总计</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.pending }}</div>
            <div class="stat-label">待加工</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-cogs"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.processing }}</div>
            <div class="stat-label">加工中</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </div>

      <div class="processing-section">
        <div class="section-header">
          <h3>加工进度</h3>
          <el-button type="text" @click="viewAllProcessing">查看全部</el-button>
        </div>

        <div v-if="loading" class="loading-container">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>

        <div v-else-if="processingItems.length === 0" class="empty-state">
          <div class="empty-icon">
            <i class="fas fa-cogs"></i>
          </div>
          <h3>暂无加工项目</h3>
          <p>您还没有开始任何收藏项的加工</p>
          <el-button type="primary" @click="startProcessing">
            开始加工
          </el-button>
        </div>

        <div v-else class="processing-list">
          <el-table
            :data="processingItems"
            style="width: 100%"
            @row-dblclick="viewDetail"
          >
            <el-table-column prop="title" label="标题" width="300">
              <template slot-scope="scope">
                <div class="item-title-cell">
                  <i :class="getItemIcon(scope.row.type)" class="item-icon"></i>
                  <span>{{ scope.row.title }}</span>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="source" label="来源" width="200"></el-table-column>

            <el-table-column prop="digestStatus" label="状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.digestStatus)">
                  {{ getStatusText(scope.row.digestStatus) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="progress" label="进度" width="150">
              <template slot-scope="scope">
                <el-progress
                  :percentage="scope.row.progress || 0"
                  :status="getProgressStatus(scope.row.digestStatus)"
                ></el-progress>
              </template>
            </el-table-column>

            <el-table-column prop="createTime" label="创建时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <el-button
                  type="text"
                  size="small"
                  :disabled="scope.row.digestStatus === 'digested'"
                  @click="processItem(scope.row)"
                >
                  <i class="fas fa-cog"></i>
                  {{ scope.row.digestStatus === 'digested' ? '已完成' : '加工' }}
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  @click="viewDetail(scope.row)"
                >
                  <i class="fas fa-eye"></i>
                  查看
                </el-button>
                <el-dropdown @command="handleItemAction($event, scope.row)">
                  <el-button type="text" size="small">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item
                      v-if="scope.row.digestStatus === 'digesting'"
                      command="markComplete"
                    >
                      标记完成
                    </el-dropdown-item>
                    <el-dropdown-item
                      v-if="scope.row.digestStatus === 'digesting'"
                      command="markAbandoned"
                    >
                      标记放弃
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </div>

    <el-dialog
      :visible.sync="processDialogVisible"
      :title="`加工编辑 - ${editingItem ? editingItem.title : ''}`"
      width="800px"
      @close="resetProcessForm"
    >
      <div v-if="editingItem" class="process-editor">
        <div class="process-header">
          <h3>{{ editingItem.title }}</h3>
          <p class="item-source">{{ editingItem.source }}</p>
        </div>

        <el-form :model="processForm" label-width="100px">
          <el-form-item label="加工状态">
            <el-select v-model="processForm.digestStatus" placeholder="选择加工状态">
              <el-option label="未加工" value="undigest"></el-option>
              <el-option label="加工中" value="digesting"></el-option>
              <el-option label="已加工" value="digested"></el-option>
              <el-option label="已放弃" value="abandoned"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="核心摘要">
            <el-input
              v-model="processForm.digestContent"
              type="textarea"
              :rows="4"
              placeholder="请输入核心摘要..."
            ></el-input>
          </el-form-item>

          <el-form-item label="关键词">
            <el-input
              v-model="processForm.keywords"
              type="textarea"
              :rows="2"
              placeholder="请输入关键词，用逗号分隔..."
            ></el-input>
          </el-form-item>

          <el-form-item label="学习目标">
            <el-input
              v-model="processForm.learningGoal"
              type="textarea"
              :rows="2"
              placeholder="请输入学习目标..."
            ></el-input>
          </el-form-item>

          <el-form-item label="复习周期">
            <el-select v-model="processForm.reviewCycle" placeholder="选择复习周期">
              <el-option label="每天" value="daily"></el-option>
              <el-option label="每周" value="weekly"></el-option>
              <el-option label="每月" value="monthly"></el-option>
              <el-option label="每季度" value="quarterly"></el-option>
              <el-option label="每年" value="yearly"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="分类标签">
            <el-select
              v-model="processForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或添加标签"
            >
              <el-option
                v-for="tag in availableTags"
                :key="tag"
                :label="tag"
                :value="tag"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProcessing">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { collectionsApi } from '@/api/collections'

export default {
  name: 'ProcessingManagement',
  data() {
    return {
      loading: false,
      searchKeyword: '',
      filterStatus: 'all',
      sortBy: 'progress',
      currentPage: 1,
      pageSize: 20,
      total: 0,
      stats: {
        total: 0,
        pending: 0,
        processing: 0,
        completed: 0
      },
      processingItems: [],
      processDialogVisible: false,
      editingItem: null,
      processForm: {
        digestStatus: '',
        digestContent: '',
        keywords: '',
        learningGoal: '',
        reviewCycle: '',
        tags: []
      },
      availableTags: [
        '前端开发', '后端开发', '设计', '产品', '运营', '工具', '教程', '文章'
      ]
    }
  },
  created() {
    this.loadProcessingStats()
    this.loadProcessingItems()
  },
  methods: {
    async loadProcessingStats() {
      try {
        this.stats = {
          total: 45,
          pending: 15,
          processing: 12,
          completed: 18
        }
      } catch (error) {
        console.error('加载加工统计失败:', error)
      }
    },
    async loadProcessingItems() {
      this.loading = true
      try {
        this.processingItems = [
          {
            id: 1,
            title: 'Vue.js 3.0 新特性详解',
            source: 'vuejs.org',
            digestStatus: 'digested',
            progress: 100,
            digestContent: 'Vue.js 3.0引入了Composition API、更好的TypeScript支持、性能优化等重要特性...',
            keywords: 'Vue.js, Composition API, TypeScript',
            learningGoal: '掌握Vue.js 3.0新特性，提升开发效率',
            reviewCycle: 'monthly',
            tags: ['前端开发', 'Vue.js'],
            createTime: '2024-01-20T10:30:00Z',
            updateTime: '2024-01-21T15:45:00Z'
          },
          {
            id: 2,
            title: 'React Hooks 最佳实践',
            source: 'reactjs.org',
            digestStatus: 'digesting',
            progress: 65,
            digestContent: 'React Hooks提供了函数组件中使用状态和其他React特性的能力...',
            keywords: 'React, Hooks, State Management',
            learningGoal: '熟练使用React Hooks替代Class组件',
            reviewCycle: 'weekly',
            tags: ['前端开发', 'React'],
            createTime: '2024-01-19T15:45:00Z',
            updateTime: '2024-01-20T14:20:00Z'
          },
          {
            id: 3,
            title: 'JavaScript 异步编程指南',
            source: 'javascript.info',
            digestStatus: 'undigest',
            progress: 0,
            digestContent: '',
            keywords: '',
            learningGoal: '深入理解JavaScript异步编程',
            reviewCycle: 'daily',
            tags: ['前端开发', 'JavaScript'],
            createTime: '2024-01-18T09:20:00Z',
            updateTime: '2024-01-18T09:20:00Z'
          }
        ]
        this.total = this.processingItems.length
      } catch (error) {
        console.error('加载加工项目失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      console.log('搜索:', this.searchKeyword)
    },
    handleFilterChange() {
      console.log('筛选:', this.filterStatus)
    },
    handleSortChange() {
      console.log('排序:', this.sortBy)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadProcessingItems()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadProcessingItems()
    },
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },
    getItemIcon(type) {
      const iconMap = {
        article: 'fas fa-file-alt',
        tutorial: 'fas fa-graduation-cap',
        video: 'fas fa-video',
        image: 'fas fa-image',
        document: 'fas fa-file-pdf',
        link: 'fas fa-link'
      }
      return iconMap[type] || 'fas fa-bookmark'
    },
    getStatusType(status) {
      const typeMap = {
        undigest: 'info',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        undigest: '未加工',
        digesting: '加工中',
        digested: '已加工',
        abandoned: '已放弃'
      }
      return textMap[status] || '未知'
    },
    getProgressStatus(status) {
      const statusMap = {
        undigest: 'exception',
        digesting: '',
        digested: 'success',
        abandoned: 'exception'
      }
      return statusMap[status] || ''
    },
    startProcessing() {
      this.$router.push('/creation/processing/tasks')
    },
    viewAllProcessing() {
      this.$router.push('/creation/processing/tasks')
    },
    processItem(item) {
      this.editingItem = item
      this.processForm = {
        digestStatus: item.digestStatus || 'undigest',
        digestContent: item.digestContent || '',
        keywords: item.keywords || '',
        learningGoal: item.learningGoal || '',
        reviewCycle: item.reviewCycle || '',
        tags: item.tags || []
      }
      this.processDialogVisible = true
    },
    viewDetail(item) {
      this.processItem(item)
    },
    handleItemAction(command, item) {
      const actionMap = {
        edit: () => this.processItem(item),
        markComplete: () => this.updateItemStatus(item, 'digested'),
        markAbandoned: () => this.updateItemStatus(item, 'abandoned'),
        delete: () => this.deleteItem(item)
      }
      const action = actionMap[command]
      if (action) {
        action()
      }
    },
    updateItemStatus(item, status) {
      item.digestStatus = status
      item.progress = status === 'digested' ? 100 : item.progress
      this.$message.success('状态更新成功')
    },
    async saveProcessing() {
      if (!this.editingItem) {
        return
      }
      try {
        const payload = {
          digestStatus: this.processForm.digestStatus,
          digestContent: this.processForm.digestContent,
          tags: this.processForm.tags,
          categoryId: null,
          noteTitle: this.editingItem.title,
          noteContent: [
            this.processForm.digestContent,
            this.processForm.keywords,
            this.processForm.learningGoal
          ].filter(Boolean).join('\n\n')
        }
        await collectionsApi.processCollectionItem(this.editingItem.id, payload)
        Object.assign(this.editingItem, {
          digestStatus: this.processForm.digestStatus,
          digestContent: this.processForm.digestContent,
          keywords: this.processForm.keywords,
          learningGoal: this.processForm.learningGoal,
          reviewCycle: this.processForm.reviewCycle,
          tags: this.processForm.tags,
          progress: this.processForm.digestStatus === 'digested' ? 100 : this.editingItem.progress
        })
        await this.loadProcessingStats()
        this.processDialogVisible = false
        this.$message.success('保存成功')
      } catch (error) {
        console.error('保存加工内容失败:', error)
        this.$message.error('保存失败')
      }
    },
    resetProcessForm() {
      this.processForm = {
        digestStatus: '',
        digestContent: '',
        keywords: '',
        learningGoal: '',
        reviewCycle: '',
        tags: []
      }
      this.editingItem = null
    },
    deleteItem(item) {
      this.processingItems = this.processingItems.filter(current => current.id !== item.id)
      this.total = this.processingItems.length
      this.$message.success('删除成功')
    }
  }
}
</script>

<style scoped>
.processing-management-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 24px 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1400px;
  margin: 0 auto;
}

.header-left {
  flex: 1;
}

.breadcrumb {
  margin-bottom: 12px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.toolbar-left {
  flex: 1;
  max-width: 400px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 100%;
}

.processing-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #67c23a, #85ce61);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.processing-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.loading-container,
.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.item-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-icon {
  color: #67c23a;
}

.process-editor {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.process-header h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #303133;
}

.item-source {
  margin: 0;
  color: #909399;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .page-header,
  .main-content {
    padding: 20px;
  }

  .header-content,
  .toolbar,
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-right,
  .toolbar-right {
    flex-wrap: wrap;
  }
}
</style>
