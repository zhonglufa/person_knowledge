<template>
  <div class="global-search">
    <!-- 搜索头部 -->
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="搜索收藏集、笔记..."
        prefix-icon="el-icon-search"
        size="large"
        clearable
        @keyup.enter.native="handleSearch"
        @clear="handleClear"
      />
      <div class="search-info" v-if="hasSearched">
        <span class="result-count">找到 {{ totalResults }} 条结果</span>
        <span class="search-time">耗时 {{ searchTime }}ms</span>
      </div>
    </div>

    <!-- 高级筛选面板 -->
    <div class="advanced-filter" v-if="hasSearched">
      <div class="filter-header">
        <span class="filter-title">
          <i class="fas fa-filter"></i> 高级筛选
        </span>
        <el-button type="text" size="small" @click="showAdvancedFilter = !showAdvancedFilter">
          {{ showAdvancedFilter ? '收起筛选' : '展开筛选' }}
          <i :class="showAdvancedFilter ? 'fas fa-chevron-up' : 'fas fa-chevron-down'"></i>
        </el-button>
      </div>
      
      <transition name="filter-slide">
        <div v-if="showAdvancedFilter" class="filter-content">
          <!-- 内容类型筛选 -->
          <div class="filter-group">
            <span class="filter-label">内容类型</span>
            <el-checkbox-group v-model="filterForm.contentTypes" size="small">
              <el-checkbox label="collection">收藏集</el-checkbox>
              <el-checkbox label="note">笔记</el-checkbox>
              <el-checkbox label="collection_item">收藏项</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <!-- 时间范围筛选 -->
          <div class="filter-group">
            <span class="filter-label">时间范围</span>
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              value-format="yyyy-MM-dd"
              @change="handleFilterChange"
            />
          </div>
          
          <!-- 消化状态筛选 -->
          <div class="filter-group">
            <span class="filter-label">消化状态</span>
            <el-checkbox-group v-model="filterForm.digestStatus" size="small">
              <el-checkbox label="undigested">未消化</el-checkbox>
              <el-checkbox label="digesting">消化中</el-checkbox>
              <el-checkbox label="digested">已消化</el-checkbox>
              <el-checkbox label="abandoned">已放弃</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <!-- 学习进度筛选 -->
          <div class="filter-group">
            <span class="filter-label">学习进度</span>
            <el-select v-model="filterForm.studyProgress" placeholder="全部" size="small" clearable>
              <el-option label="0%" value="0"></el-option>
              <el-option label="25%" value="25"></el-option>
              <el-option label="50%" value="50"></el-option>
              <el-option label="75%" value="75"></el-option>
              <el-option label="100%" value="100"></el-option>
            </el-select>
          </div>
          
          <!-- 排序方式 -->
          <div class="filter-group">
            <span class="filter-label">排序方式</span>
            <el-radio-group v-model="filterForm.sortBy" size="small" @change="handleFilterChange">
              <el-radio-button label="relevance">相关度</el-radio-button>
              <el-radio-button label="createTime">创建时间</el-radio-button>
              <el-radio-button label="updateTime">更新时间</el-radio-button>
              <el-radio-button label="popularity">热度</el-radio-button>
            </el-radio-group>
          </div>
          
          <!-- 操作按钮 -->
          <div class="filter-actions">
            <el-button size="small" @click="resetFilters">重置筛选</el-button>
            <el-button type="primary" size="small" @click="applyFilters">应用筛选</el-button>
          </div>
        </div>
      </transition>
    </div>

    <!-- 结果分类统计 -->
    <div class="result-tabs" v-if="hasSearched">
      <div
        v-for="tab in resultTabs"
        :key="tab.key"
        class="result-tab"
        :class="{ active: activeCategory === tab.key }"
        @click="switchCategory(tab.key)"
      >
        <i :class="tab.icon"></i>
        <span>{{ tab.label }}</span>
        <el-tag size="mini" :type="activeCategory === tab.key ? 'primary' : 'info'">
          {{ tab.count }}
        </el-tag>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="search-results" v-loading="searchLoading">
      <!-- 空状态 -->
      <div v-if="hasSearched && filteredResults.length === 0 && !searchLoading" class="empty-state">
        <div class="empty-icon-wrapper">
          <i class="fas fa-search empty-icon"></i>
        </div>
        <p class="empty-text">未找到相关结果</p>
        <p class="empty-desc">尝试使用其他关键词或调整筛选条件</p>
      </div>

      <!-- 初始状态 -->
      <div v-if="!hasSearched" class="initial-state">
        <div class="initial-icon-wrapper">
          <i class="fas fa-globe initial-icon"></i>
        </div>
        <p class="initial-text">输入关键词开始全局搜索</p>
      </div>

      <!-- 结果列表 -->
      <div v-if="hasSearched && filteredResults.length > 0" class="results-list">
        <div
          v-for="item in paginatedResults"
          :key="item.id"
          class="result-card"
          @click="handleResultClick(item)"
        >
          <div class="result-icon" :class="getTypeClass(item.type)">
            <i :class="getTypeIcon(item.type)"></i>
          </div>
          <div class="result-content">
            <div class="result-header">
              <h3 class="result-title" v-html="highlightKeyword(item.title)"></h3>
              <el-tag :type="getTypeTag(item.type)" size="mini">
                {{ getTypeLabel(item.type) }}
              </el-tag>
            </div>
            <p class="result-desc" v-html="highlightKeyword(item.description || item.excerpt)"></p>
            <div class="result-meta">
              <span class="meta-item">
                <i class="fas fa-calendar"></i>
                {{ formatDate(item.createTime || item.updateTime) }}
              </span>
              <span class="meta-item" v-if="item.category">
                <i class="fas fa-folder"></i>
                {{ item.category }}
              </span>
              <span class="meta-item" v-if="item.digestStatus">
                <i :class="getDigestIcon(item.digestStatus)"></i>
                {{ getDigestLabel(item.digestStatus) }}
              </span>
            </div>
          </div>
          <div class="result-action">
            <i class="fas fa-chevron-right"></i>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="hasSearched && filteredResults.length > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="filteredResults.length"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { searchAll } from '@/api/search'
import { sanitizeHtml, escapeHtml } from '@/utils/sanitize'
import { debounce } from '@/utils/debounce'

export default {
  name: 'GlobalSearch',
  props: {
    keyword: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      activeCategory: 'all',
      searchLoading: false,
      hasSearched: false,
      searchTime: 0,
      currentPage: 1,
      pageSize: 10,
      showAdvancedFilter: false,
      filterForm: {
        contentTypes: [],
        dateRange: null,
        digestStatus: [],
        studyProgress: null,
        sortBy: 'relevance'
      },
      resultTabs: [
        { key: 'all', label: '全部', icon: 'fas fa-layer-group', count: 0 },
        { key: 'collection', label: '收藏集', icon: 'fas fa-folder', count: 0 },
        { key: 'note', label: '笔记', icon: 'fas fa-file-alt', count: 0 },
        { key: 'collection_item', label: '收藏项', icon: 'fas fa-bookmark', count: 0 }
      ],
      allResults: [],
      debouncedSearch: null
    }
  },
  computed: {
    totalResults() {
      return this.filteredResults?.length || 0
    },
    filteredResults() {
      let results = this.allResults
      
      if (this.activeCategory !== 'all' && this.filterForm.contentTypes.length > 0) {
        results = results.filter(item => item.type === this.activeCategory)
      } else if (this.filterForm.contentTypes.length > 0) {
        results = results.filter(item => this.filterForm.contentTypes.includes(item.type))
      }
      
      if (this.filterForm.digestStatus.length > 0) {
        results = results.filter(item => 
          !item.digestStatus || this.filterForm.digestStatus.includes(item.digestStatus)
        )
      }
      
      if (this.filterForm.studyProgress) {
        results = results.filter(item => 
          !item.studyProgress || String(item.studyProgress) === this.filterForm.studyProgress
        )
      }
      
      if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
        const [startDate, endDate] = this.filterForm.dateRange
        results = results.filter(item => {
          const itemDate = new Date(item.createTime || item.updateTime)
          return itemDate >= new Date(startDate) && itemDate <= new Date(endDate + ' 23:59:59')
        })
      }
      
      if (this.filterForm.sortBy !== 'relevance') {
        results = [...results].sort((a, b) => {
          switch (this.filterForm.sortBy) {
            case 'createTime':
              return new Date(b.createTime) - new Date(a.createTime)
            case 'updateTime':
              return new Date(b.updateTime) - new Date(a.updateTime)
            case 'popularity':
              return (b.popularity || 0) - (a.popularity || 0)
            default:
              return 0
          }
        })
      }
      
      return results
    },
    paginatedResults() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredResults.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.debouncedSearch = debounce(() => this._handleSearch(), 300)
  },
  watch: {
    keyword: {
      handler(newVal) {
        if (newVal?.trim()) {
          this.handleSearch()
        }
      },
      immediate: true
    }
  },
  methods: {
    handleSearch() {
      this.debouncedSearch()
    },

    async _handleSearch() {
      const kw = this.keyword?.trim()
      if (!kw) return

      this.searchLoading = true
      this.hasSearched = true
      this.currentPage = 1

      const startTime = Date.now()

      try {
        const response = await searchAll({
          keyword: kw,
          ...this.buildFilterParams()
        })

        if (response?.data?.code === 200) {
          this.allResults = response.data.data?.records || response.data.data || []
        } else {
          this.allResults = this.getMockResults(kw)
        }

        this.searchTime = Date.now() - startTime

        this.resultTabs[0].count = this.allResults.length
        this.resultTabs[1].count = this.allResults.filter(r => r.type === 'collection').length
        this.resultTabs[2].count = this.allResults.filter(r => r.type === 'note').length
        this.resultTabs[3].count = this.allResults.filter(r => r.type === 'collection_item').length
      } catch (error) {
        console.error('搜索失败:', error)
        this.allResults = this.getMockResults(kw)
        this.searchTime = Date.now() - startTime
        this.resultTabs[0].count = this.allResults.length
      } finally {
        this.searchLoading = false
      }
    },
    
    buildFilterParams() {
      const params = {}
      if (this.filterForm.contentTypes.length > 0) {
        params.contentTypes = this.filterForm.contentTypes.join(',')
      }
      if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
        params.startDate = this.filterForm.dateRange[0]
        params.endDate = this.filterForm.dateRange[1]
      }
      if (this.filterForm.digestStatus.length > 0) {
        params.digestStatus = this.filterForm.digestStatus.join(',')
      }
      if (this.filterForm.studyProgress) {
        params.studyProgress = this.filterForm.studyProgress
      }
      if (this.filterForm.sortBy) {
        params.sortBy = this.filterForm.sortBy
      }
      return params
    },
    
    handleFilterChange() {
      this.applyFilters()
    },
    
    applyFilters() {
      this.currentPage = 1
      this.$forceUpdate()
    },
    
    resetFilters() {
      this.filterForm = {
        contentTypes: [],
        dateRange: null,
        digestStatus: [],
        studyProgress: null,
        sortBy: 'relevance'
      }
      this.currentPage = 1
    },
    
    handleClear() {
      this.keyword = ''
      this.hasSearched = false
      this.allResults = []
      this.resetFilters()
    },
    
    switchCategory(category) {
      this.activeCategory = category
      this.currentPage = 1
    },
    
    handlePageChange(page) {
      this.currentPage = page
    },
    
    handleResultClick(item) {
      const routes = {
        collection: `/collections/${item.id}`,
        note: `/notes/${item.id}`,
        collection_item: `/collect/${item.id}`
      }
      const route = routes[item.type]
      if (route) {
        this.$router.push(route)
      }
    },
    
    highlightKeyword(text) {
      if (!text) return ''
      if (!this.keyword) return escapeHtml(text)
      const escaped = escapeHtml(text)
      const escapedKeyword = escapeHtml(this.keyword)
      const regex = new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
      return sanitizeHtml(escaped.replace(regex, '<mark>$1</mark>'))
    },
    
    getTypeClass(type) {
      return `type-${type}`
    },
    
    getTypeIcon(type) {
      const icons = {
        collection: 'fas fa-folder',
        note: 'fas fa-file-alt',
        collection_item: 'fas fa-bookmark'
      }
      return icons[type] || 'fas fa-file'
    },
    
    getTypeTag(type) {
      const tags = {
        collection: 'primary',
        note: 'success',
        collection_item: 'warning'
      }
      return tags[type] || 'info'
    },
    
    getTypeLabel(type) {
      const labels = {
        collection: '收藏集',
        note: '笔记',
        collection_item: '收藏项'
      }
      return labels[type] || '未知'
    },
    
    getDigestIcon(status) {
      const icons = {
        undigested: 'fas fa-clock',
        digesting: 'fas fa-spinner',
        digested: 'fas fa-check',
        abandoned: 'fas fa-times'
      }
      return icons[status] || 'fas fa-circle'
    },
    
    getDigestLabel(status) {
      const labels = {
        undigested: '未消化',
        digesting: '消化中',
        digested: '已消化',
        abandoned: '已放弃'
      }
      return labels[status] || ''
    },
    
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    
    getMockResults(kw) {
      return [
        {
          id: 1,
          type: 'collection',
          title: `Vue.js 学习资料 - ${kw}`,
          description: '包含Vue.js相关的教程、文档和示例项目',
          createTime: '2024-01-15',
          category: '前端开发'
        },
        {
          id: 2,
          type: 'note',
          title: `${kw} 学习笔记总结`,
          description: '关于知识管理方法的个人总结和实践经验',
          createTime: '2024-01-14',
          category: '学习笔记'
        },
        {
          id: 3,
          type: 'collection',
          title: `设计模式 ${kw} 参考`,
          description: '常用设计模式的实现示例和应用场景',
          createTime: '2024-01-13',
          category: '架构设计'
        },
        {
          id: 4,
          type: 'collection_item',
          title: `${kw} 相关文章精选`,
          description: '从网络收集的相关优质文章',
          createTime: '2024-01-12',
          digestStatus: 'digesting',
          studyProgress: 50
        }
      ]
    }
  }
}
</script>

<style scoped>
.global-search {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* Search Header */
.search-header {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.search-info {
  display: flex;
  justify-content: space-between;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* 高级筛选 */
.advanced-filter {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  overflow: hidden;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.filter-title {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-content {
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.filter-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  min-width: 80px;
  flex-shrink: 0;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

/* 筛选过渡动画 */
.filter-slide-enter-active,
.filter-slide-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}

.filter-slide-enter,
.filter-slide-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
}

/* Result Tabs */
.result-tabs {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.result-tab {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all var(--transition-normal);
  background: var(--bg-canvas);
  border: 1px solid var(--border-light);
  font-size: var(--font-size-sm);
  user-select: none;
}

.result-tab:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.result-tab.active {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* Results */
.search-results {
  min-height: 300px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.result-card {
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

.result-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateX(4px);
}

.result-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  color: white;
  flex-shrink: 0;
}

.result-icon.type-collection {
  background: var(--gradient-primary);
}

.result-icon.type-note {
  background: var(--gradient-secondary);
}

.result-icon.type-collection_item {
  background: var(--gradient-warm);
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.result-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  flex: 1;
}

.result-title :deep(.highlight) {
  background: var(--warning-bg);
  color: var(--warning-color);
  padding: 0 2px;
  border-radius: var(--radius-sm);
}

.result-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-desc :deep(.highlight) {
  background: var(--warning-bg);
  color: var(--warning-color);
  padding: 0 2px;
  border-radius: var(--radius-sm);
}

.result-meta {
  display: flex;
  gap: var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.result-action {
  color: var(--text-placeholder);
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.result-card:hover .result-action {
  transform: translateX(2px);
  color: var(--primary-color);
}

/* Empty/Initial States */
.empty-state,
.initial-state {
  text-align: center;
  padding: var(--space-12) var(--space-4);
  animation: fadeIn 0.3s ease;
}

.empty-icon-wrapper,
.initial-icon-wrapper {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-full);
  background: var(--bg-canvas);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
}

.empty-icon,
.initial-icon {
  font-size: 40px;
  color: var(--text-placeholder);
}

.empty-text,
.initial-text {
  font-size: var(--font-size-lg);
  color: var(--text-regular);
  margin-bottom: var(--space-2);
}

.empty-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* Responsive */
@media (max-width: 768px) {
  .result-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-action {
    display: none;
  }
  
  .filter-group {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }
  
  .filter-label {
    min-width: auto;
  }
}
</style>
