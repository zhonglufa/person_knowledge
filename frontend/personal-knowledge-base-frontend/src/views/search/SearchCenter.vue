<template>
  <div class="page-wrapper">
    <!-- 主内容区 -->
    <main class="page-main">
      <div class="page-container">
        <!-- 欢迎区域 -->
        <section class="welcome-section" v-if="!hasSearched">
          <h2 class="welcome-title">搜索你的知识库</h2>
          <p class="welcome-desc">快速查找收藏集、笔记和个人知识内容</p>

          <!-- 搜索框 -->
          <div class="main-search-bar">
            <el-input
              v-model="searchKeyword"
              :placeholder="placeholderText"
              prefix-icon="el-icon-search"
              size="large"
              clearable
              @keyup.enter.native="handleSearch"
              @clear="handleClear"
              class="main-search-input"
            >
              <el-button
                slot="append"
                icon="el-icon-search"
                @click="handleSearch"
              >
                搜索
              </el-button>
            </el-input>
          </div>

          <!-- 快捷搜索入口 -->
          <div class="quick-entry">
            <div class="entry-card" @click="switchTab('collections')">
              <div class="entry-icon gradient-primary">
                <i class="fas fa-folder-open"></i>
              </div>
              <h3>收藏集</h3>
              <p>搜索已收藏的内容集合</p>
            </div>
            <div class="entry-card" @click="switchTab('notes')">
              <div class="entry-icon gradient-secondary">
                <i class="fas fa-file-alt"></i>
              </div>
              <h3>笔记</h3>
              <p>搜索个人笔记和记录</p>
            </div>
            <div class="entry-card" @click="switchTab('global')">
              <div class="entry-icon gradient-warm">
                <i class="fas fa-globe"></i>
              </div>
              <h3>全局搜索</h3>
              <p>跨模块综合检索</p>
            </div>
          </div>

          <!-- 搜索加载指示 -->
          <div class="search-loading-hint" v-if="searchLoading">
            <i class="el-icon-loading"></i>
            <span>正在搜索...</span>
          </div>

          <!-- 搜索历史 -->
          <div class="search-history" v-if="searchHistory.length > 0 && !hasSearched">
            <div class="history-header">
              <h4 class="history-title">
                <i class="fas fa-history"></i> 搜索历史
              </h4>
              <el-button 
                type="text" 
                size="small" 
                @click="clearSearchHistory"
                class="clear-history-btn"
              >
                <i class="fas fa-trash-alt"></i> 清空
              </el-button>
            </div>
            <div class="history-tags">
              <el-tag
                v-for="(keyword, index) in searchHistory"
                :key="index"
                size="small"
                closable
                @click="handleHistorySearch(keyword)"
                @close="removeHistoryItem(index)"
                class="history-tag"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </div>

          <!-- 热门搜索 -->
          <div class="hot-search" v-if="hotKeywords?.length > 0">
            <h4 class="hot-title"><i class="fas fa-fire"></i> 热门搜索</h4>
            <div class="hot-tags">
              <el-tag
                v-for="(keyword, index) in hotKeywords"
                :key="index"
                :type="getHotTagType(index)"
                size="small"
                @click="handleHotSearch(keyword)"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </div>
        </section>

        <!-- 搜索结果区 -->
        <section class="result-section" v-else>
          <!-- 标签切换 -->
          <div class="tab-bar">
            <div class="tab-bar-header">
              <div class="search-input-wrapper">
                <el-input
                  v-model="searchKeyword"
                  :placeholder="placeholderText"
                  prefix-icon="el-icon-search"
                  size="large"
                  clearable
                  @keyup.enter.native="handleSearch"
                  @clear="handleClear"
                >
                  <el-button
                    slot="append"
                    icon="el-icon-search"
                    @click="handleSearch"
                  >
                    搜索
                  </el-button>
                </el-input>
              </div>
              <div class="header-actions">
                <el-button type="text" @click="showAdvancedFilter = !showAdvancedFilter" class="filter-btn">
                  <i class="fas fa-filter"></i> 高级筛选
                </el-button>
                <el-button type="text" @click="navigateToHistory" class="history-btn">
                  <i class="fas fa-history"></i> 历史记录
                </el-button>
              </div>
            </div>
            <el-tabs v-model="activeTab" @tab-click="handleTabChange">
              <el-tab-pane label="全局" name="global"></el-tab-pane>
              <el-tab-pane :label="`收藏集 (${resultCounts.collections})`" name="collections"></el-tab-pane>
              <el-tab-pane :label="`收藏项 (${resultCounts.items})`" name="items"></el-tab-pane>
              <el-tab-pane :label="`笔记 (${resultCounts.notes})`" name="notes"></el-tab-pane>
              <el-tab-pane :label="`标签 (${resultCounts.tags})`" name="tags"></el-tab-pane>
            </el-tabs>
            
            <!-- 高级筛选面板 -->
            <div v-if="showAdvancedFilter" class="advanced-filter">
              <div class="filter-row">
                <div class="filter-item">
                  <label class="filter-label">时间范围</label>
                  <el-select v-model="filterTimeRange" placeholder="全部时间" size="small" clearable @change="handleSearch">
                    <el-option label="全部时间" value=""></el-option>
                    <el-option label="今天" value="today"></el-option>
                    <el-option label="本周" value="week"></el-option>
                    <el-option label="本月" value="month"></el-option>
                    <el-option label="近三月" value="quarter"></el-option>
                  </el-select>
                </div>
                <div class="filter-item">
                  <label class="filter-label">消化状态</label>
                  <el-select v-model="filterDigestStatus" placeholder="全部状态" size="small" clearable @change="handleSearch">
                    <el-option label="全部状态" value=""></el-option>
                    <el-option label="未消化" value="undigest"></el-option>
                    <el-option label="消化中" value="digesting"></el-option>
                    <el-option label="已消化" value="digested"></el-option>
                    <el-option label="已放弃" value="abandoned"></el-option>
                  </el-select>
                </div>
                <div class="filter-item">
                  <label class="filter-label">笔记类型</label>
                  <el-select v-model="filterNoteType" placeholder="全部类型" size="small" clearable @change="handleSearch">
                    <el-option label="全部类型" value=""></el-option>
                    <el-option label="概念性知识" value="conceptual"></el-option>
                    <el-option label="程序性知识" value="procedural"></el-option>
                    <el-option label="事实性知识" value="factual"></el-option>
                    <el-option label="元认知知识" value="metacognitive"></el-option>
                  </el-select>
                </div>
                <div class="filter-item">
                  <label class="filter-label">排序方式</label>
                  <el-select v-model="sortBy" placeholder="相关度" size="small" @change="handleSearch">
                    <el-option label="相关度" value="relevance"></el-option>
                    <el-option label="创建时间" value="createTime"></el-option>
                    <el-option label="更新时间" value="updateTime"></el-option>
                    <el-option label="热度" value="hot"></el-option>
                  </el-select>
                </div>
              </div>
            </div>
          </div>

          <!-- 结果内容 -->
          <div class="tab-content">
            <component
              :is="currentTabComponent"
              :keyword="searchKeyword"
              :results="currentResults"
              :loading="searchLoading"
            />
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'SearchCenter',
  data() {
    return {
      searchKeyword: '',
      activeTab: 'global',
      hasSearched: false,
      searchLoading: false,
      isLoggedIn: false,
      hotKeywords: ['Vue', '知识管理', '学习笔记', '前端开发', '设计模式'],
      searchHistory: [], // 搜索历史
      showHistory: false, // 是否显示搜索历史
      showAdvancedFilter: false, // 是否显示高级筛选
      filterTimeRange: '', // 时间范围筛选
      filterDigestStatus: '', // 消化状态筛选
      filterNoteType: '', // 笔记类型筛选
      sortBy: 'relevance', // 排序方式
      resultCounts: {
        collections: 0,
        items: 0,
        notes: 0,
        tags: 0,
        global: 0
      }
    }
  },
  computed: {
    placeholderText() {
      const placeholders = {
        global: '搜索收藏集、笔记...',
        collections: '搜索收藏集名称或描述...',
        notes: '搜索笔记标题或内容...'
      }
      return placeholders[this.activeTab] || '输入关键词搜索...'
    },
    currentTabComponent() {
      const components = {
        global: 'global-search',
        collections: 'collection-search',
        notes: 'note-search'
      }
      return components[this.activeTab] || 'global-search'
    },
    currentResults() {
      return []
    }
  },
  methods: {
    // 加载搜索历史
    loadSearchHistory() {
      try {
        const history = localStorage.getItem('searchHistory');
        if (history) {
          this.searchHistory = JSON.parse(history);
        }
      } catch (error) {
        console.error('加载搜索历史失败:', error);
        this.searchHistory = [];
      }
    },
    
    // 保存搜索历史
    saveSearchHistory(keyword) {
      if (!keyword?.trim()) return;
      
      const trimmedKeyword = keyword.trim();
      
      // 移除重复项
      this.searchHistory = this.searchHistory.filter(k => k !== trimmedKeyword);
      
      // 添加到开头
      this.searchHistory.unshift(trimmedKeyword);
      
      // 只保留最近10条
      if (this.searchHistory.length > 10) {
        this.searchHistory = this.searchHistory.slice(0, 10);
      }
      
      // 保存到localStorage
      try {
        localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory));
      } catch (error) {
        console.error('保存搜索历史失败:', error);
      }
    },
    
    // 清空搜索历史
    clearSearchHistory() {
      this.searchHistory = [];
      localStorage.removeItem('searchHistory');
      this.$message.success('搜索历史已清空');
    },
    
    // 使用历史记录搜索
    handleHistorySearch(keyword) {
      this.searchKeyword = keyword;
      this.showHistory = false;
      this.handleSearch();
    },
    
    // 删除单条历史记录
    removeHistoryItem(index) {
      this.searchHistory.splice(index, 1);
      localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory));
    },
    
    handleSearch() {
      const keyword = this.searchKeyword?.trim()
      if (!keyword) {
        this.$message.warning('请输入搜索关键词')
        return
      }
      
      // 保存搜索历史
      this.saveSearchHistory(keyword);
      
      this.hasSearched = true
      this.searchLoading = true
      try {
        this.$message.success(`正在搜索: ${keyword}`)
        this.$router.push({
          path: `/search/${this.activeTab}`,
          query: { q: keyword }
        })
      } catch (error) {
        console.error('搜索失败:', error)
        this.$message.error('搜索失败，请重试')
      } finally {
        this.searchLoading = false
      }
    },
    handleClear() {
      this.searchKeyword = ''
      this.hasSearched = false
    },
    switchTab(tab) {
      this.activeTab = tab
      this.searchKeyword = ''
    },
    handleTabChange(tab) {
      this.activeTab = tab?.name
      if (this.searchKeyword?.trim()) {
        this.handleSearch()
      }
    },
    handleHotSearch(keyword) {
      this.searchKeyword = keyword
      this.handleSearch()
    },
    getHotTagType(index) {
      const types = ['', 'success', 'warning', 'info', '']
      return types[index] || ''
    },
    navigateToHistory() {
      this.$router.push('/search/history')
    },
    navigateToLogin() {
      this.$router.push('/login')
    }
  },
  mounted() {
    // 加载搜索历史
    this.loadSearchHistory();
    
    const query = this.$route?.query
    if (query?.q) {
      this.searchKeyword = query.q
      this.handleSearch()
    }
  }
}
</script>

<style scoped>
.search-center {
  min-height: 100vh;
  background: var(--bg-canvas);
}

/* Welcome Section */
.welcome-section {
  text-align: center;
  padding: var(--space-12) 0;
}

.welcome-title {
  font-size: var(--font-size-4xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
}

.welcome-desc {
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
  margin-bottom: var(--space-8);
}

/* Main Search Bar */
.main-search-bar {
  max-width: 600px;
  margin: 0 auto var(--space-8);
}

.main-search-input :deep(.el-input-group__append) {
  background: var(--primary-color);
  color: white;
  border: none;
}

.main-search-input :deep(.el-input-group__append:hover) {
  background: var(--primary-hover);
}

/* Quick Entry */
.quick-entry {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-6);
  margin-bottom: var(--space-8);
}

.entry-card {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-base);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.entry-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: var(--primary-color);
  opacity: 0;
  transition: opacity var(--transition-fast);
  pointer-events: none;
}

.entry-card:hover::after {
  opacity: 0.03;
}

.entry-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.entry-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
  font-size: var(--font-size-2xl);
  color: white;
}

.entry-card h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.entry-card p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}

/* Search History */
.search-history {
  margin-top: var(--space-6);
  text-align: left;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.history-title {
  font-size: var(--font-size-md);
  color: var(--text-regular);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
}

.clear-history-btn {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  padding: 4px 8px;
}

.clear-history-btn:hover {
  color: var(--danger-color, #f56c6c);
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.history-tag {
  cursor: pointer;
  transition: all var(--transition-normal);
}

.history-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* Hot Search */
.hot-search {
  margin-top: var(--space-6);
}

.hot-title {
  font-size: var(--font-size-md);
  color: var(--text-regular);
  margin-bottom: var(--space-4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-3);
}

.hot-tags :deep(.el-tag) {
  cursor: pointer;
  transition: all var(--transition-normal);
  padding: var(--space-2) var(--space-4);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-full);
}

.hot-tags :deep(.el-tag:hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* Result Section */
.result-section {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  overflow: hidden;
}

.tab-bar {
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--border-light);
}

.tab-bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.search-input-wrapper {
  flex: 1;
  max-width: 500px;
}

.header-actions {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.filter-btn, .history-btn {
  color: var(--text-secondary);
  transition: var(--transition-base);
  white-space: nowrap;
}

.filter-btn:hover, .history-btn:hover {
  color: var(--primary-color);
}

/* 高级筛选面板 */
.advanced-filter {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  margin-top: var(--space-4);
  border: 1px solid var(--border-light);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.filter-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  font-weight: 500;
}

.search-input-wrapper :deep(.el-input-group__append) {
  background: var(--primary-color);
  color: white;
  border: none;
}

.history-btn {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.history-btn:hover {
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

.history-btn i {
  margin-right: var(--space-2);
}

.tab-content {
  padding: var(--space-6);
}

/* Search loading hint */
.search-loading-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) 0;
  font-size: var(--font-size-sm);
  color: var(--primary-color);
  animation: pulse 1.5s ease-in-out infinite;
}

.search-loading-hint i {
  font-size: var(--font-size-lg);
}

/* Responsive */
@media (max-width: 768px) {
  .quick-entry {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }

  .welcome-title {
    font-size: var(--font-size-2xl);
  }

  .welcome-section {
    padding: var(--space-6) 0;
  }
  
  .tab-bar-header {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }
  
  .search-input-wrapper {
    max-width: none;
  }
  
  .history-btn {
    align-self: flex-end;
  }
}
</style>
