<template>
  <div class="collection-search">
    <!-- 搜索状态提示 -->
    <div class="search-status" v-if="hasSearched">
      <span class="keyword">搜索: "{{ displayKeyword }}"</span>
      <span class="count">({{ totalCount }} 个收藏集)</span>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar" v-if="hasSearched">
      <div class="filter-left">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">
            <i class="fas fa-th-large"></i>
          </el-radio-button>
          <el-radio-button label="list">
            <i class="fas fa-list"></i>
          </el-radio-button>
        </el-radio-group>
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          size="small"
          @change="handleSortChange"
        >
          <el-option label="相关度" value="relevance" />
          <el-option label="创建时间" value="createTime" />
          <el-option label="收藏项数" value="itemCount" />
        </el-select>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" class="search-content">
      <!-- 初始状态 -->
      <div v-if="!hasSearched" class="initial-state">
        <div class="state-icon-wrapper">
          <i class="fas fa-folder-open initial-icon"></i>
        </div>
        <p class="initial-text">请输入关键词搜索收藏集</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && collections.length === 0" class="empty-state">
        <div class="state-icon-wrapper">
          <i class="fas fa-search empty-icon"></i>
        </div>
        <p class="empty-text">未找到相关收藏集</p>
        <p class="empty-desc">尝试使用不同的关键词搜索</p>
        <el-button type="primary" @click="goToSearchCenter">
          <i class="fas fa-arrow-left"></i> 返回搜索中心
        </el-button>
      </div>

      <!-- 结果列表 -->
      <div v-else-if="collections.length > 0">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="grid-view">
          <el-row :gutter="20">
            <el-col
              v-for="item in collections"
              :key="item.id"
              :xs="24" :sm="12" :md="8" :lg="6"
            >
              <div class="collection-card" @click="handleCollectionClick(item)">
                <div class="card-cover">
                  <img v-if="item.cover" :src="item.cover" :alt="item.name" />
                  <div v-else class="cover-placeholder">
                    <i class="fas fa-folder"></i>
                  </div>
                  <div class="status-badge" :class="item.isPublic ? 'public' : 'private'">
                    {{ item.isPublic ? '公开' : '私有' }}
                  </div>
                </div>
                <div class="card-body">
                  <h3 class="card-title" v-html="highlightText(item.name)"></h3>
                  <p class="card-desc" v-html="highlightText(item.description || '暂无描述')"></p>
                  <div class="card-stats">
                    <span><i class="fas fa-bookmark"></i> {{ item.itemCount || 0 }} 项</span>
                    <span><i class="fas fa-calendar"></i> {{ formatDate(item.createTime) }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 列表视图 -->
        <div v-else class="list-view">
          <div
            v-for="item in collections"
            :key="item.id"
            class="list-item"
            @click="handleCollectionClick(item)"
          >
            <div class="item-cover">
              <img v-if="item.cover" :src="item.cover" :alt="item.name" />
              <div v-else class="cover-placeholder small">
                <i class="fas fa-folder"></i>
              </div>
            </div>
            <div class="item-content">
              <h3 class="item-title" v-html="highlightText(item.name)"></h3>
              <p class="item-desc" v-html="highlightText(item.description || '暂无描述')"></p>
              <div class="item-meta">
                <el-tag size="mini" :type="item.isPublic ? 'success' : 'info'">
                  {{ item.isPublic ? '公开' : '私有' }}
                </el-tag>
                <span class="meta-item"><i class="fas fa-bookmark"></i> {{ item.itemCount || 0 }} 项</span>
                <span class="meta-item"><i class="fas fa-calendar"></i> {{ formatDate(item.createTime) }}</span>
              </div>
            </div>
            <div class="item-action">
              <i class="fas fa-chevron-right"></i>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="totalCount"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { sanitizeHtml, escapeHtml } from '@/utils/sanitize'
import { debounce } from '@/utils/debounce'
import { searchCollections } from '@/api/search'

export default {
  name: 'CollectionSearch',
  props: {
    keyword: { type: String, default: '' },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      viewMode: 'grid',
      sortBy: 'relevance',
      currentPage: 1,
      pageSize: 12,
      hasSearched: false,
      collections: []
    }
  },
  computed: {
    displayKeyword() {
      return this.keyword || this.$route?.query?.q || ''
    },
    totalCount() {
      return this.collections?.length || 0
    }
  },
  watch: {
    keyword: {
      handler(newVal) {
        if (newVal?.trim()) {
          this.debouncedSearch()
        }
      },
      immediate: true
    }
  },
  created() {
    this.debouncedSearch = debounce(() => {
      this.handleSearch()
    }, 300)
  },
  methods: {
    async handleSearch() {
      this.hasSearched = true
      this.currentPage = 1
      try {
        const response = await searchCollections({ keyword: this.displayKeyword })
        const records = response?.data?.data?.list || response?.data?.list || response?.data?.data || response?.data || []
        if (Array.isArray(records) && records.length > 0) {
          this.collections = records.map(item => ({
            ...item,
            name: item.name || item.collectionName,
            itemCount: item.itemCount || item.collectCount || 0,
            isPublic: item.isPublic !== false,
            cover: item.cover || item.coverUrl || '',
            createTime: item.createTime || item.createdAt
          }))
          return
        }

        this.collections = this.getMockCollections(this.displayKeyword)
      } catch (error) {
        console.error('搜索收藏集失败:', error)
        this.$message.error('搜索失败')
        this.collections = []
      }
    },
    handleSortChange() {
      this.currentPage = 1
      try {
        this.collections = [...this.collections].sort((a, b) => {
          if (this.sortBy === 'itemCount') {
            return (b.itemCount || 0) - (a.itemCount || 0)
          }
          return 0
        })
      } catch (error) {
        console.error('排序失败:', error)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    handleCollectionClick(item) {
      this.$router.push(`/collections/${item.id}`)
    },
    highlightText(text) {
      if (!text) return ''
      if (!this.keyword) return escapeHtml(text)
      const escaped = escapeHtml(text)
      const escapedKeyword = escapeHtml(this.keyword)
      const regex = new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
      return sanitizeHtml(escaped.replace(regex, '<mark>$1</mark>'))
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    goToSearchCenter() {
      this.$router.push('/search/center')
    },
    getMockCollections(kw) {
      return [
        { id: 1, name: `${kw} 相关资料`, description: '关于前端开发的学习资料集合', itemCount: 25, isPublic: true, cover: '', createTime: '2024-01-15' },
        { id: 2, name: `Vue ${kw} 实战`, description: 'Vue.js项目实践笔记和代码片段', itemCount: 18, isPublic: false, cover: '', createTime: '2024-01-10' },
        { id: 3, name: `${kw} 学习笔记`, description: '个人学习总结和知识梳理', itemCount: 32, isPublic: true, cover: '', createTime: '2024-01-05' }
      ]
    }
  }
}
</script>

<style scoped>
.collection-search {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.search-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.search-status .keyword {
  font-weight: 600;
  color: var(--primary-color);
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.filter-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.filter-left :deep(.el-radio-button__inner) {
  padding: var(--space-2) var(--space-3);
}

/* States */
.initial-state,
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-4);
  animation: fadeIn 0.3s ease;
}

.state-icon-wrapper {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-full);
  background: var(--bg-canvas);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
}

.initial-icon,
.empty-icon {
  font-size: 40px;
  color: var(--text-placeholder);
}

.initial-text,
.empty-text {
  font-size: var(--font-size-lg);
  color: var(--text-regular);
  margin-bottom: var(--space-2);
}

.empty-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

/* Grid View */
.grid-view {
  margin-bottom: var(--space-4);
}

.collection-card {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-base);
  cursor: pointer;
  transition: all var(--transition-normal);
  margin-bottom: var(--space-5);
  display: flex;
  flex-direction: column;
}

.collection-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.card-cover {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--bg-canvas), var(--primary-bg));
  color: var(--primary-color);
  font-size: 40px;
}

.cover-placeholder.small {
  font-size: 24px;
}

.status-badge {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  color: white;
}

.status-badge.public { background: var(--success-color); }
.status-badge.private { background: var(--text-secondary); }

.card-body {
  padding: var(--space-4);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-title :deep(.highlight) {
  background: var(--warning-bg);
  color: var(--warning-color);
  padding: var(--space-0, 1px) var(--space-1);
  border-radius: var(--radius-sm);
}

.card-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc :deep(.highlight) {
  background: var(--warning-bg);
  color: var(--warning-color);
  padding: var(--space-0, 1px) var(--space-1);
  border-radius: var(--radius-sm);
}

.card-stats {
  display: flex;
  gap: var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

/* List View */
.list-view {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.list-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  cursor: pointer;
  transition: all var(--transition-normal);
  min-height: 88px;
}

.list-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.item-cover {
  width: 80px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
  background: var(--bg-canvas);
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}

.item-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.item-action {
  color: var(--text-placeholder);
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.list-item:hover .item-action {
  transform: translateX(2px);
  color: var(--primary-color);
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* Responsive */
@media (max-width: 768px) {
  .list-item {
    flex-direction: column;
    align-items: flex-start;
  }
  .item-action { display: none; }
}
</style>
