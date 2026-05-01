<template>
  <div class="page-wrapper">
    <!-- 页面主内容 -->
    <main class="page-main">
      <div class="page-container">
        <!-- 页面标题区 -->
        <div class="page-header-section">
          <div class="page-header-content">
            <div class="page-header-left">
              <div class="page-breadcrumb">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
                  <el-breadcrumb-item>公开内容管理</el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              <h1 class="page-title">公开内容管理</h1>
              <p class="page-subtitle">管理您对外公开的收藏集和笔记</p>
            </div>
            <div class="page-header-right">
              <el-button type="primary" @click="createPublicCollection">
                <i class="fas fa-plus"></i>
                创建公开收藏集
              </el-button>
            </div>
          </div>
        </div>

        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索公开内容"
              prefix-icon="el-icon-search"
              class="search-input"
              @input="handleSearch"
            />
          </div>
          <div class="toolbar-right">
            <el-select
              v-model="contentType"
              placeholder="内容类型"
              @change="handleContentTypeChange"
            >
              <el-option label="全部" value="all"></el-option>
              <el-option label="收藏集" value="collection"></el-option>
              <el-option label="笔记" value="note"></el-option>
            </el-select>
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              @change="handleSortChange"
            >
              <el-option label="创建时间" value="createTime"></el-option>
              <el-option label="更新时间" value="updateTime"></el-option>
              <el-option label="浏览量" value="views"></el-option>
            </el-select>
          </div>
        </div>

        <!-- 内容统计 -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon gradient-primary">
              <i class="fas fa-folder"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ publicStats.collections }}</div>
              <div class="stat-label">公开收藏集</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon gradient-secondary">
              <i class="fas fa-file-alt"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ publicStats.notes }}</div>
              <div class="stat-label">公开笔记</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon gradient-warm">
              <i class="fas fa-eye"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ publicStats.totalViews }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon gradient-cool">
              <i class="fas fa-heart"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ publicStats.likes }}</div>
              <div class="stat-label">获得点赞</div>
            </div>
          </div>
        </div>

        <!-- 公开内容列表 -->
        <div class="content-list content-card">
          <div v-if="loading" class="loading-container">
            <i class="fas fa-spinner fa-spin loading-icon"></i>
            <span>加载中...</span>
          </div>

          <div v-else-if="publicContent.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-book-open"></i>
            </div>
            <h3 class="empty-title">暂无公开内容</h3>
            <p class="empty-description">您还没有创建任何公开的收藏集或笔记</p>
            <el-button type="primary" @click="createPublicCollection">
              创建第一个公开收藏集
            </el-button>
          </div>

          <div v-else class="content-grid">
            <div
              v-for="item in publicContent"
              :key="item.id"
              class="content-card-item"
              @click="viewContent(item)"
            >
              <div class="content-card-header">
                <div class="content-type">
                  <i :class="item.type === 'collection' ? 'fas fa-folder' : 'fas fa-file-alt'"></i>
                  <span>{{ item.type === 'collection' ? '收藏集' : '笔记' }}</span>
                </div>
                <div class="content-status">
                  <el-tag :type="item.status === 'published' ? 'success' : 'warning'">
                    {{ item.status === 'published' ? '已发布' : '草稿' }}
                  </el-tag>
                </div>
              </div>

              <div class="content-card-body">
                <h3 class="content-title">{{ item.title }}</h3>
                <p class="content-description">{{ item.description }}</p>

                <div class="content-meta">
                  <div class="meta-item">
                    <i class="fas fa-eye"></i>
                    <span>{{ item.views }}</span>
                  </div>
                  <div class="meta-item">
                    <i class="fas fa-heart"></i>
                    <span>{{ item.likes }}</span>
                  </div>
                  <div class="meta-item">
                    <i class="fas fa-comment"></i>
                    <span>{{ item.comments }}</span>
                  </div>
                </div>
              </div>

              <div class="content-card-footer">
                <div class="content-date">
                  {{ formatDate(item.updateTime) }}
                </div>
                <div class="content-actions">
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="editContent(item)"
                  >
                    <i class="fas fa-edit"></i>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="togglePublicStatus(item)"
                  >
                    <i :class="item.isPublic ? 'fas fa-lock' : 'fas fa-globe'"></i>
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click.stop="deleteContent(item)"
                  >
                    <i class="fas fa-trash"></i>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 编辑公开内容对话框 -->
    <el-dialog
      :visible.sync="editDialogVisible"
      :title="editingContent ? '编辑公开内容' : '创建公开内容'"
      width="600px"
      @close="resetEditForm"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="内容类型">
          <el-select v-model="editForm.type" placeholder="选择内容类型">
            <el-option label="收藏集" value="collection"></el-option>
            <el-option label="笔记" value="note"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="公开状态">
          <el-switch
            v-model="editForm.isPublic"
            active-text="公开"
            inactive-text="私密"
          ></el-switch>
        </el-form-item>
        
        <el-form-item label="访问权限">
          <el-radio-group v-model="editForm.accessLevel">
            <el-radio label="public">完全公开</el-radio>
            <el-radio label="protected">需要密码</el-radio>
            <el-radio label="private">仅自己可见</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="editForm.accessLevel === 'protected'" label="访问密码">
          <el-input
            v-model="editForm.accessPassword"
            type="password"
            placeholder="设置访问密码"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveContent">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'PublicContent',
  data() {
    return {
      loading: false,
      searchKeyword: '',
      contentType: 'all',
      sortBy: 'updateTime',
      publicContent: [],
      publicStats: {
        collections: 0,
        notes: 0,
        totalViews: 0,
        likes: 0
      },
      editDialogVisible: false,
      editingContent: null,
      editForm: {
        title: '',
        description: '',
        type: 'collection',
        isPublic: true,
        accessLevel: 'public',
        accessPassword: ''
      }
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo'])
  },
  created() {
    this.loadPublicContent()
    this.loadPublicStats()
  },
  methods: {
    async loadPublicContent() {
      this.loading = true
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 模拟数据
        this.publicContent = [
          {
            id: 1,
            title: '前端开发学习笔记',
            description: '记录前端开发过程中的学习心得和技术要点',
            type: 'note',
            status: 'published',
            isPublic: true,
            views: 1234,
            likes: 89,
            comments: 23,
            createTime: '2024-01-15T10:30:00Z',
            updateTime: '2024-01-20T15:45:00Z'
          },
          {
            id: 2,
            title: 'Vue.js 最佳实践',
            description: 'Vue.js 开发中的最佳实践和技巧总结',
            type: 'collection',
            status: 'published',
            isPublic: true,
            views: 2567,
            likes: 156,
            comments: 45,
            createTime: '2024-01-10T09:15:00Z',
            updateTime: '2024-01-18T14:20:00Z'
          }
        ]
      } catch (error) {
        this.$message.error('加载公开内容失败')
      } finally {
        this.loading = false
      }
    },
    
    async loadPublicStats() {
      try {
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 模拟统计数据
        this.publicStats = {
          collections: 12,
          notes: 28,
          totalViews: 15678,
          likes: 892
        }
      } catch (error) {
        console.error('加载统计数据失败')
      }
    },
    
    handleSearch() {
      // 实现搜索逻辑
      console.log('搜索:', this.searchKeyword)
    },
    
    handleContentTypeChange() {
      this.loadPublicContent()
    },
    
    handleSortChange() {
      this.loadPublicContent()
    },
    
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },
    
    viewContent(item) {
      // 跳转到内容详情页
      if (item.type === 'collection') {
        this.$router.push(`/collections/${item.id}`)
      } else {
        this.$router.push(`/notes/${item.id}`)
      }
    },
    
    editContent(item) {
      this.editingContent = item
      this.editForm = {
        title: item.title,
        description: item.description,
        type: item.type,
        isPublic: item.isPublic,
        accessLevel: 'public',
        accessPassword: ''
      }
      this.editDialogVisible = true
    },
    
    createPublicCollection() {
      this.editingContent = null
      this.editForm = {
        title: '',
        description: '',
        type: 'collection',
        isPublic: true,
        accessLevel: 'public',
        accessPassword: ''
      }
      this.editDialogVisible = true
    },
    
    async togglePublicStatus(item) {
      try {
        const newStatus = !item.isPublic
        // 调用API更新公开状态
        await new Promise(resolve => setTimeout(resolve, 500))
        
        item.isPublic = newStatus
        this.$message.success(`${newStatus ? '公开' : '私密'}设置已更新`)
        this.loadPublicStats() // 重新加载统计数据
      } catch (error) {
        this.$message.error('更新状态失败')
      }
    },
    
    async deleteContent(item) {
      try {
        await this.$confirm('确定要删除这个公开内容吗？', '确认删除', {
          type: 'warning'
        })
        
        // 调用API删除内容
        await new Promise(resolve => setTimeout(resolve, 500))
        
        this.publicContent = this.publicContent.filter(content => content.id !== item.id)
        this.$message.success('删除成功')
        this.loadPublicStats() // 重新加载统计数据
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },
    
    async saveContent() {
      try {
        if (this.editingContent) {
          // 更新现有内容
          await new Promise(resolve => setTimeout(resolve, 500))
          Object.assign(this.editingContent, this.editForm)
          this.$message.success('更新成功')
        } else {
          // 创建新内容
          await new Promise(resolve => setTimeout(resolve, 500))
          const newContent = {
            id: Date.now(),
            ...this.editForm,
            status: 'published',
            views: 0,
            likes: 0,
            comments: 0,
            createTime: new Date().toISOString(),
            updateTime: new Date().toISOString()
          }
          this.publicContent.unshift(newContent)
          this.$message.success('创建成功')
        }
        
        this.editDialogVisible = false
        this.loadPublicStats() // 重新加载统计数据
      } catch (error) {
        this.$message.error('保存失败')
      }
    },
    
    resetEditForm() {
      this.editingContent = null
      this.editForm = {
        title: '',
        description: '',
        type: 'collection',
        isPublic: true,
        accessLevel: 'public',
        accessPassword: ''
      }
    }
  }
}
</script>

<style scoped>
/* 响应式设计 */
@media (max-width: 768px) {
  .page-header-content {
    flex-direction: column;
    gap: var(--space-4);
  }

  .toolbar {
    flex-direction: column;
    gap: var(--space-4);
  }

  .toolbar-right {
    width: 100%;
    justify-content: space-between;
  }

  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--space-4);
  }

  .content-card-item {
    padding: var(--space-4);
  }
}
</style>