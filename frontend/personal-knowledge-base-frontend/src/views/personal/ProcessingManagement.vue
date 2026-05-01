<template>
  <div class="processing-management-page">
    <!-- 页面头部 -->
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
          <p class="page-subtitle">管理您的收藏项加工进度和状态</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="startProcessing">
            <i class="fas fa-play"></i>
            开始加工
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 工具栏 -->
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

      <!-- 加工统计 -->
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

      <!-- 加工进度列表 -->
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
                  @click="processItem(scope.row)"
                  :disabled="scope.row.digestStatus === 'digested'"
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
                      command="markComplete" 
                      v-if="scope.row.digestStatus === 'digesting'"
                    >
                      标记完成
                    </el-dropdown-item>
                    <el-dropdown-item 
                      command="markAbandoned" 
                      v-if="scope.row.digestStatus === 'digesting'"
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

      <!-- 分页 -->
      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </div>

    <!-- 加工编辑对话框 -->
    <el-dialog
      :visible.sync="processDialogVisible"
      :title="`加工编辑 - ${editingItem?.title || ''}`"
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
      
      // 统计数据
      stats: {
        total: 0,
        pending: 0,
        processing: 0,
        completed: 0
      },
      
      // 加工项目列表
      processingItems: [],
      
      // 编辑对话框相关
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
      
      // 可用标签
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
    // 加载加工统计
    async loadProcessingStats() {
      try {
        // 这里应该调用后端API获取加工统计
        // 暂时使用模拟数据
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
    
    // 加载加工项目列表
    async loadProcessingItems() {
      this.loading = true
      try {
        // 这里应该调用后端API获取加工项目列表
        // 暂时使用模拟数据
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
    
    // 搜索处理
    handleSearch() {
      console.log('搜索:', this.searchKeyword)
    },
    
    // 筛选变化
    handleFilterChange() {
      console.log('筛选:', this.filterStatus)
    },
    
    // 排序变化
    handleSortChange() {
      console.log('排序:', this.sortBy)
    },
    
    // 分页大小变化
    handleSizeChange(val) {
      this.pageSize = val
      this.loadProcessingItems()
    },
    
    // 当前页变化
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadProcessingItems()
    },
    
    // 格式化日期
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },
    
    // 获取项目图标
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
    
    // 获取状态类型
    getStatusType(status) {
      const typeMap = {
        undigest: 'info',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'danger'
      }
      return typeMap[status] || 'info'
    },
    
    // 获取状态文本
    getStatusText(status) {
      const textMap = {
        undigest: '未加工',
        digesting: '加工中',
        digested: '已加工',
        abandoned: '已放弃'
      }
      return textMap[status] || '未知'
    },
    
    // 获取进度状态
    getProgressStatus(status) {
      const statusMap = {
        undigest: '',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'exception'
      }
      return statusMap[status] || ''
    },
    
    // 查看全部加工项目
    viewAllProcessing() {
      console.log('查看全部加工项目')
    },
    
    // 处理加工项目
    processItem(item) {
      this.editingItem = item
      this.processForm = {
        digestStatus: item.digestStatus,
        digestContent: item.digestContent || '',
        keywords: item.keywords || '',
        learningGoal: item.learningGoal || '',
        reviewCycle: item.reviewCycle || '',
        tags: item.tags || []
      }
      this.processDialogVisible = true
    },
    
    // 查看详情
    viewDetail(item) {
      this.$router.push(`/collections/${item.collectionId}/items/${item.id}`)
    },
    
    // 处理项目操作
    handleItemAction(command, item) {
      switch(command) {
        case 'edit':
          this.editItem(item)
          break
        case 'markComplete':
          this.markComplete(item)
          break
        case 'markAbandoned':
          this.markAbandoned(item)
          break
        case 'delete':
          this.deleteItem(item)
          break
      }
    },
    
    // 编辑项目
    editItem(item) {
      this.processItem(item)
    },
    
    // 标记完成
    async markComplete(item) {
      try {
        // 这里应该调用后端API更新状态
        item.digestStatus = 'digested'
        item.progress = 100
        this.$message.success('已标记为完成')
        this.loadProcessingStats()
      } catch (error) {
        this.$message.error('标记完成失败')
      }
    },
    
    // 标记放弃
    async markAbandoned(item) {
      try {
        // 这里应该调用后端API更新状态
        item.digestStatus = 'abandoned'
        item.progress = 0
        this.$message.success('已标记为放弃')
        this.loadProcessingStats()
      } catch (error) {
        this.$message.error('标记放弃失败')
      }
    },
    
    // 删除项目
    async deleteItem(item) {
      try {
        await this.$confirm(`确定要删除 "${item.title}" 吗？`, '确认删除', {
          type: 'warning'
        })
        
        // 这里应该调用后端API删除项目
        // 暂时只是模拟删除
        this.processingItems = this.processingItems.filter(i => i.id !== item.id)
        this.total = this.processingItems.length
        this.$message.success('删除成功')
        this.loadProcessingStats()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    
    // 开始加工
    startProcessing() {
      this.$router.push('/creation/processing')
    },
    
    // 保存加工
    async saveProcessing() {
      try {
        if (this.editingItem) {
          // 更新现有项目
          Object.assign(this.editingItem, this.processForm)
          
          // 根据状态更新进度
          if (this.processForm.digestStatus === 'digested') {
            this.editingItem.progress = 100
          } else if (this.processForm.digestStatus === 'digesting') {
            this.editingItem.progress = 65 // 示例值
          } else if (this.processForm.digestStatus === 'undigest') {
            this.editingItem.progress = 0
          } else if (this.processForm.digestStatus === 'abandoned') {
            this.editingItem.progress = 0
          }
          
          this.$message.success('保存成功')
          this.processDialogVisible = false
          this.loadProcessingStats()
        }
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    
    // 重置加工表单
    resetProcessForm() {
      this.editingItem = null
      this.processForm = {
        digestStatus: '',
        digestContent: '',
        keywords: '',
        learningGoal: '',
        reviewCycle: '',
        tags: []
      }
    }
  }
}
</script>

<style scoped>
.processing-management-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background-color: var(--bg-container);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}

.header-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.breadcrumb {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-6);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-4) var(--space-6);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.toolbar-left {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.toolbar-right {
  display: flex;
  gap: var(--space-3);
}

.processing-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-xl);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin-top: var(--space-1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-light);
}

.section-header h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.processing-section {
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--space-4);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-5);
  color: var(--text-regular);
}

.loading-container i {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--space-3);
}

.empty-state {
  text-align: center;
  padding: var(--space-16) var(--space-5);
}

.empty-icon {
  font-size: 48px;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.empty-state h3 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-state p {
  color: var(--text-regular);
  margin: 0 0 var(--space-6) 0;
}

.processing-list {
  background-color: var(--bg-page);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.item-title-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.item-icon {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
}

.process-editor {
  max-height: 60vh;
  overflow-y: auto;
}

.process-header {
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.process-header h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.item-source {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin: 0;
}

.pagination {
  margin-top: var(--space-6);
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--space-4);
  }
  
  .toolbar {
    flex-direction: column;
    gap: var(--space-4);
  }
  
  .processing-stats {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 576px) {
  .page-header,
  .main-content {
    padding: var(--space-4);
  }
  
  .processing-stats {
    grid-template-columns: 1fr;
  }
}
</style>