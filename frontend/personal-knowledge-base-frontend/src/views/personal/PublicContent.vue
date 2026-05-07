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
              <p class="page-subtitle">查看和管理您的公开收藏集和笔记</p>
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
              clearable
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
              <el-option label="更新时间" value="updateTime"></el-option>
              <el-option label="创建时间" value="createTime"></el-option>
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

          <div v-else-if="filteredContent.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="fas fa-book-open"></i>
            </div>
            <h3 class="empty-title">暂无公开内容</h3>
            <p class="empty-description">您还没有设置任何公开的收藏集或笔记</p>
          </div>

          <div v-else class="content-grid">
            <div
              v-for="item in filteredContent"
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
                  <el-switch
                    v-model="item.isPublic"
                    active-text="公开"
                    inactive-text="私密"
                    @click.native.stop="togglePublicStatus(item)"
                  />
                </div>
              </div>

              <div class="content-card-body">
                <h3 class="content-title">{{ item.title }}</h3>
                <p class="content-description">{{ item.description || '暂无描述' }}</p>

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
                    <i class="fas fa-clock"></i>
                    <span>{{ formatDate(item.updateTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { collectionsApi } from '@/api/collections'
import { noteApi } from '@/api/note'
import { debounce } from '@/utils/debounce'

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
      }
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo']),
    requestSort() {
      if (this.sortBy === 'views') {
        return { sortBy: 'visitCount', sortOrder: 'desc' }
      }
      if (this.sortBy === 'createTime') {
        return { sortBy: 'createdAt', sortOrder: 'desc' }
      }
      return { sortBy: 'updatedAt', sortOrder: 'desc' }
    },
    filteredContent() {
      let result = [...this.publicContent]

      if (this.contentType !== 'all') {
        result = result.filter(item => item.type === this.contentType)
      }

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(item =>
          item.title?.toLowerCase().includes(keyword) ||
          item.description?.toLowerCase().includes(keyword)
        )
      }

      result.sort((a, b) => {
        if (this.sortBy === 'views') {
          return (b.views || 0) - (a.views || 0)
        }
        if (this.sortBy === 'createTime') {
          return new Date(b.createTime || 0) - new Date(a.createTime || 0)
        }
        return new Date(b.updateTime || 0) - new Date(a.updateTime || 0)
      })

      return result
    }
  },
  created() {
    this.debouncedReload = debounce(() => {
      this.loadPublicContent()
      this.loadPublicStats()
    }, 300)

    this.loadPublicContent()
    this.loadPublicStats()
  },
  beforeDestroy() {
    this.debouncedReload?.cancel?.()
  },
  methods: {
    async loadPublicContent() {
      this.loading = true
      try {
        const sort = this.requestSort
        const params = {
          pageNum: 1,
          pageSize: 50,
          keyword: this.searchKeyword || undefined,
          ...sort
        }

        const [collectionsRes, notesRes] = await Promise.all([
          collectionsApi.getPublicCollections(params),
          noteApi.getPublicNotes(params)
        ])

        const collectionsPayload = collectionsRes?.data ?? collectionsRes ?? {}
        const collectionsPage = collectionsPayload.data ?? collectionsPayload
        const collectionsRaw = Array.isArray(collectionsPage.records)
          ? collectionsPage.records
          : (Array.isArray(collectionsPage.list) ? collectionsPage.list : (Array.isArray(collectionsPage) ? collectionsPage : []))

        const notesPayload = notesRes?.data ?? notesRes ?? {}
        const notesPage = notesPayload.data ?? notesPayload
        const notesRaw = Array.isArray(notesPage.records)
          ? notesPage.records
          : (Array.isArray(notesPage.list) ? notesPage.list : (Array.isArray(notesPage) ? notesPage : []))

        const collections = collectionsRaw.map(item => ({
          ...item,
          type: 'collection',
          title: item.name || item.title,
          isPublic: Number(item.isPublic) === 1,
          views: item.views || item.viewCount || 0,
          likes: item.likes || item.likeCount || 0,
          createTime: item.createTime || item.createdAt,
          updateTime: item.updateTime || item.updatedAt
        }))

        const notes = notesRaw.map(item => ({
          ...item,
          type: 'note',
          isPublic: Number(item.isPublic) === 1,
          views: item.views || item.viewCount || 0,
          likes: item.likes || item.likeCount || 0,
          createTime: item.createTime || item.createdAt,
          updateTime: item.updateTime || item.updatedAt
        }))

        this.publicContent = [...collections, ...notes]
      } catch (error) {
        console.error('加载公开内容失败:', error)
        this.$message.error('加载公开内容失败')
      } finally {
        this.loading = false
      }
    },

    async loadPublicStats() {
      try {
        const sort = this.requestSort
        const params = {
          pageNum: 1,
          pageSize: 1,
          keyword: this.searchKeyword || undefined,
          ...sort
        }

        const [collectionsRes, notesRes] = await Promise.all([
          collectionsApi.getPublicCollections(params),
          noteApi.getPublicNotes(params)
        ])

        const collectionsPayload = collectionsRes?.data ?? collectionsRes ?? {}
        const collectionsPage = collectionsPayload.data ?? collectionsPayload
        const notesPayload = notesRes?.data ?? notesRes ?? {}
        const notesPage = notesPayload.data ?? notesPayload

        const collectionTotal = Number(collectionsPage.total || 0)
        const noteTotal = Number(notesPage.total || 0)

        this.publicStats = {
          collections: collectionTotal,
          notes: noteTotal,
          totalViews: this.publicContent.reduce((sum, item) => sum + (item.views || 0), 0),
          likes: this.publicContent.reduce((sum, item) => sum + (item.likes || 0), 0)
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },

    handleSearch() {
      this.debouncedReload()
    },

    handleContentTypeChange() {
      this.debouncedReload()
    },

    handleSortChange() {
      this.debouncedReload()
    },

    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },

    viewContent(item) {
      if (item.type === 'collection') {
        this.$router.push(`/collections/${item.id}`)
      } else {
        this.$router.push(`/creation/notes/${item.id}`)
      }
    },

    async togglePublicStatus(item) {
      try {
        const newStatus = !item.isPublic
        if (item.type === 'collection') {
          await collectionsApi.updateCollectionPublicStatus(item.id, newStatus)
        } else {
          await noteApi.updateNotePublicStatus(item.id, newStatus)
        }

        item.isPublic = newStatus
        this.$message.success(`${newStatus ? '公开' : '私密'}设置已更新`)
        this.loadPublicStats()
      } catch (error) {
        console.error('更新状态失败:', error)
        this.$message.error('更新状态失败')
        item.isPublic = !item.isPublic
      }
    }
  }
}
</script>

<style scoped>
/* 页面标题区 */
.page-header-section {
  padding: var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

.page-header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--bg-canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  border-color: var(--border-base);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-number {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
  padding: var(--space-4);
  background: var(--bg-canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.toolbar-left {
  flex: 1;
  max-width: 320px;
}

.search-input {
  width: 100%;
}

.toolbar-right {
  display: flex;
  gap: var(--space-3);
}

.toolbar-right .el-select {
  width: 140px;
}

/* 内容卡片 */
.content-list {
  background: var(--bg-canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  padding: var(--space-5);
}

.content-card-item {
  background: var(--bg-page);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  padding: var(--space-5);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.content-card-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.content-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.content-type {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.content-type i {
  color: var(--primary-color);
}

.content-status .el-tag {
  border-radius: var(--radius-full);
}

.content-card-body {
  margin-bottom: var(--space-4);
}

.content-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-description {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-4) 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.6;
  min-height: 2 * 1.6em;
}

.content-meta {
  display: flex;
  gap: var(--space-4);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.meta-item i {
  color: var(--text-placeholder);
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10);
  color: var(--text-secondary);
  gap: var(--space-3);
}

.loading-icon {
  font-size: var(--font-size-2xl);
  color: var(--primary-color);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-10);
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: var(--primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);
}

.empty-icon i {
  font-size: 32px;
  color: var(--primary-color);
}

.empty-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-5) 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    gap: var(--space-4);
  }

  .toolbar-left {
    max-width: none;
    width: 100%;
  }

  .toolbar-right {
    width: 100%;
    justify-content: flex-start;
  }

  .toolbar-right .el-select {
    flex: 1;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .page-header-section {
    padding: var(--space-4) 0;
    margin-bottom: var(--space-4);
  }

  .page-title {
    font-size: var(--font-size-xl);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: var(--font-size-lg);
  }

  .stat-number {
    font-size: var(--font-size-xl);
  }

  .content-card-item {
    padding: var(--space-4);
  }

  .content-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }

  .content-meta {
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  .content-card-footer {
    flex-direction: column;
    gap: var(--space-3);
    align-items: flex-start;
  }

  .content-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>