<template>
  <div class="page-wrapper">
    <main class="page-main">
      <div class="page-container">
        <section class="welcome-section" v-if="!hasSearched">
          <h2 class="welcome-title">搜索你的知识库</h2>
          <p class="welcome-desc">快速查找收藏集、收藏项、笔记和标签内容</p>

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

          <div class="quick-entry">
            <div class="entry-card" @click="switchTab('collections', true)">
              <div class="entry-icon gradient-primary">
                <i class="fas fa-folder-open"></i>
              </div>
              <h3>收藏集</h3>
              <p>搜索已整理的收藏集与说明</p>
            </div>
            <div class="entry-card" @click="switchTab('items', true)">
              <div class="entry-icon gradient-secondary">
                <i class="fas fa-bookmark"></i>
              </div>
              <h3>收藏项</h3>
              <p>按标题、摘要与状态检索收藏内容</p>
            </div>
            <div class="entry-card" @click="switchTab('notes', true)">
              <div class="entry-icon gradient-warm">
                <i class="fas fa-file-alt"></i>
              </div>
              <h3>笔记</h3>
              <p>搜索个人笔记和知识记录</p>
            </div>
          </div>

          <div class="search-loading-hint" v-if="searchLoading">
            <i class="el-icon-loading"></i>
            <span>正在搜索...</span>
          </div>

          <div class="search-history" v-if="searchHistory.length > 0 && !hasSearched">
            <div class="history-header">
              <h4 class="history-title">
                <i class="fas fa-history"></i> 搜索历史
              </h4>
              <el-button
                type="text"
                size="small"
                @click="clearHistory"
                class="clear-history-btn"
              >
                <i class="fas fa-trash-alt"></i> 清空
              </el-button>
            </div>
            <div class="history-tags">
              <el-tag
                v-for="item in searchHistory"
                :key="item.id || item.keyword"
                size="small"
                closable
                @click="handleHistorySearch(item.keyword)"
                @close="removeHistoryItem(item)"
                class="history-tag"
              >
                {{ item.keyword }}
              </el-tag>
            </div>
          </div>

          <div class="hot-search" v-if="hotKeywords.length > 0">
            <h4 class="hot-title"><i class="fas fa-fire"></i> 热门搜索</h4>
            <div class="hot-tags">
              <el-tag
                v-for="(keyword, index) in hotKeywords"
                :key="keyword"
                :type="getHotTagType(index)"
                size="small"
                @click="handleHotSearch(keyword)"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </div>
        </section>

        <section class="result-section" v-else>
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
              <el-tab-pane :label="`综合 (${resultCounts.global})`" name="global"></el-tab-pane>
              <el-tab-pane :label="`收藏集 (${resultCounts.collections})`" name="collections"></el-tab-pane>
              <el-tab-pane :label="`收藏项 (${resultCounts.items})`" name="items"></el-tab-pane>
              <el-tab-pane :label="`笔记 (${resultCounts.notes})`" name="notes"></el-tab-pane>
              <el-tab-pane :label="`标签 (${resultCounts.tags})`" name="tags"></el-tab-pane>
            </el-tabs>

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

          <div class="tab-content">
            <div class="result-summary">
              <div>
                <h3>{{ currentSummary.title }}</h3>
                <p>{{ currentSummary.description }}</p>
              </div>
              <el-tag size="small" type="info">关键词：{{ searchKeyword || '未输入' }}</el-tag>
            </div>

            <div v-if="searchLoading" class="result-state loading-state">
              <i class="el-icon-loading"></i>
              <span>正在整理搜索结果...</span>
            </div>

            <template v-else>
              <div v-if="currentResults.length > 0" class="result-list">
                <article v-for="item in currentResults" :key="item.id" class="result-card">
                  <div class="result-card-header">
                    <div class="result-card-title-wrap">
                      <h4 class="result-card-title">{{ item.title }}</h4>
                      <el-tag size="mini" :type="item.typeTagType">{{ item.typeLabel }}</el-tag>
                    </div>
                    <span class="result-card-time">{{ item.timeLabel }}</span>
                  </div>
                  <p class="result-card-desc">{{ item.description }}</p>
                  <div class="result-card-meta">
                    <span v-for="meta in item.meta" :key="meta.label" class="meta-item">
                      <strong>{{ meta.label }}：</strong>{{ meta.value }}
                    </span>
                  </div>
                </article>
              </div>
              <div v-else class="result-state empty-state">
                <i class="fas fa-search"></i>
                <h3>{{ currentSummary.emptyTitle }}</h3>
                <p>{{ currentSummary.emptyDescription }}</p>
              </div>
            </template>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import {
  searchAll,
  searchCollections,
  searchItems,
  searchNotes,
  getSearchHistory,
  clearSearchHistory as clearSearchHistoryApi,
  deleteSearchHistory,
  getHotSearchKeywords
} from '@/api/search'

const TAB_CONFIG = {
  global: {
    label: '综合检索',
    route: '/search/global',
    placeholder: '搜索收藏集、收藏项、笔记和标签...',
    title: '综合结果',
    description: '统一展示收藏集、收藏项、笔记与标签结果，适合快速定位知识内容。',
    emptyTitle: '暂无综合结果',
    emptyDescription: '可以尝试切换到具体类型，或调整关键词与筛选条件。'
  },
  collections: {
    label: '收藏集检索',
    route: '/search/collections',
    placeholder: '搜索收藏集名称或描述...',
    title: '收藏集结果',
    description: '按收藏集名称、描述与维护时间定位知识主题容器。',
    emptyTitle: '暂无匹配的收藏集',
    emptyDescription: '建议尝试输入主题名称、来源方向或调整时间范围。'
  },
  items: {
    label: '收藏项检索',
    route: '/search/items',
    placeholder: '搜索收藏项标题、摘要、关键词或来源...',
    title: '收藏项结果',
    description: '按收藏内容标题、摘要、关键词、来源与消化状态快速筛选。',
    emptyTitle: '暂无匹配的收藏项',
    emptyDescription: '可以更换关键词，或结合消化状态、时间范围继续筛选。'
  },
  notes: {
    label: '笔记检索',
    route: '/search/notes',
    placeholder: '搜索笔记标题或内容...',
    title: '笔记结果',
    description: '按笔记标题、内容与知识类型定位已加工成果。',
    emptyTitle: '暂无匹配的笔记',
    emptyDescription: '建议尝试知识概念、结论关键词或切换到收藏项检索。'
  },
  tags: {
    label: '标签检索',
    route: '/search/tags',
    placeholder: '搜索标签名称或标签相关内容...',
    title: '标签结果',
    description: '按标签名称定位知识主题词，并作为结构化入口回查相关知识。',
    emptyTitle: '暂无匹配的标签',
    emptyDescription: '建议尝试主题词、技术词，或切换到综合检索扩大范围。'
  }
}

const NOTE_TYPE_LABELS = {
  conceptual: '概念性知识',
  procedural: '程序性知识',
  factual: '事实性知识',
  metacognitive: '元认知知识',
  original: '原创笔记',
  summary: '总结笔记',
  normal: '普通笔记'
}

export default {
  name: 'SearchCenter',
  data() {
    return {
      searchKeyword: '',
      activeTab: 'global',
      hasSearched: false,
      searchLoading: false,
      hotKeywords: [],
      searchHistory: [],
      showAdvancedFilter: false,
      filterTimeRange: '',
      filterDigestStatus: '',
      filterNoteType: '',
      sortBy: 'relevance',
      resultCounts: {
        collections: 0,
        items: 0,
        notes: 0,
        tags: 0,
        global: 0
      },
      searchResults: {
        collections: [],
        items: [],
        notes: [],
        tags: []
      }
    }
  },
  computed: {
    currentTabConfig() {
      return TAB_CONFIG[this.activeTab] || TAB_CONFIG.global
    },
    placeholderText() {
      return this.currentTabConfig.placeholder
    },
    currentSummary() {
      return this.currentTabConfig
    },
    currentResults() {
      if (this.activeTab === 'global') {
        return [
          ...this.searchResults.collections,
          ...this.searchResults.items,
          ...this.searchResults.notes,
          ...this.searchResults.tags
        ]
      }
      return this.searchResults[this.activeTab] || []
    }
  },
  methods: {
    buildSearchParams() {
      return {
        keyword: this.searchKeyword.trim(),
        pageNum: 1,
        pageSize: 20,
        sortBy: this.sortBy,
        timeRange: this.filterTimeRange || undefined,
        digestStatus: this.filterDigestStatus || undefined,
        noteType: this.filterNoteType || undefined
      }
    },
    async loadSearchHistory() {
      try {
        const response = await getSearchHistory()
        const records = response?.data?.data || []
        this.searchHistory = records.map(item => ({
          id: item.id,
          keyword: item.keyword
        }))
      } catch (error) {
        this.searchHistory = []
      }
    },
    async loadHotKeywords() {
      try {
        const response = await getHotSearchKeywords()
        const records = response?.data?.data || []
        this.hotKeywords = records.map(item => item.keyword).filter(Boolean)
      } catch (error) {
        this.hotKeywords = ['Vue', '知识管理', '学习笔记', '前端开发', '设计模式']
      }
    },
    async clearHistory() {
      try {
        await clearSearchHistoryApi()
        this.searchHistory = []
        this.$message.success('搜索历史已清空')
      } catch (error) {
        this.$message.error('清空搜索历史失败')
      }
    },
    async removeHistoryItem(item) {
      if (!item?.id) {
        this.searchHistory = this.searchHistory.filter(historyItem => historyItem.keyword !== item.keyword)
        return
      }
      try {
        await deleteSearchHistory(item.id)
        this.searchHistory = this.searchHistory.filter(historyItem => historyItem.id !== item.id)
      } catch (error) {
        this.$message.error('删除搜索历史失败')
      }
    },
    handleHistorySearch(keyword) {
      this.searchKeyword = keyword
      this.handleSearch()
    },
    mapDigestStatusLabel(status) {
      return {
        undigest: '未消化',
        undigested: '未消化',
        digesting: '消化中',
        digested: '已消化',
        abandoned: '已放弃'
      }[status] || status || '未设置'
    },
    mapNoteTypeLabel(type) {
      return NOTE_TYPE_LABELS[type] || type || '普通笔记'
    },
    formatTimeLabel(record) {
      const timeValue = record.updatedAt || record.updateTime || record.createdAt || record.createTime
      if (!timeValue) {
        return '时间未知'
      }
      const date = new Date(timeValue)
      if (Number.isNaN(date.getTime())) {
        return `更新时间 ${timeValue}`
      }
      return `更新于 ${date.toLocaleDateString('zh-CN')}`
    },
    normalizeCollectionRecord(record) {
      return {
        id: `collection-${record.id}`,
        rawId: record.id,
        title: record.name || '未命名收藏集',
        description: record.description || '暂无收藏集描述',
        typeLabel: '收藏集',
        typeTagType: 'success',
        timeLabel: this.formatTimeLabel(record),
        meta: [
          { label: '公开状态', value: record.isPublic === 1 ? '公开' : '私有' },
          { label: '收藏项数', value: record.itemCount ?? '-' }
        ]
      }
    },
    normalizeItemRecord(record) {
      return {
        id: `item-${record.id}`,
        rawId: record.id,
        title: record.title || '未命名收藏项',
        description: record.coreAbstract || record.keywords || record.source || '暂无收藏项摘要',
        typeLabel: '收藏项',
        typeTagType: 'warning',
        timeLabel: this.formatTimeLabel(record),
        meta: [
          { label: '来源', value: record.source || '未知来源' },
          { label: '消化状态', value: this.mapDigestStatusLabel(record.digestStatus) },
          { label: '学习进度', value: record.studyProgress ?? '-' }
        ]
      }
    },
    normalizeNoteRecord(record) {
      return {
        id: `note-${record.id}`,
        rawId: record.id,
        title: record.title || '未命名笔记',
        description: record.description || record.content || '暂无笔记内容',
        typeLabel: '笔记',
        typeTagType: 'primary',
        timeLabel: this.formatTimeLabel(record),
        meta: [
          { label: '类型', value: this.mapNoteTypeLabel(record.noteType) },
          { label: '公开状态', value: record.isPublic === 1 ? '公开' : '私有' },
          { label: '消化状态', value: this.mapDigestStatusLabel(record.digestStatus) }
        ]
      }
    },
    normalizeTagRecord(record) {
      return {
        id: `tag-${record.id}`,
        rawId: record.id,
        title: record.name || '未命名标签',
        description: `颜色标识：${record.color || '未设置'}`,
        typeLabel: '标签',
        typeTagType: 'info',
        timeLabel: this.formatTimeLabel(record),
        meta: [
          { label: '颜色', value: record.color || '-' },
          { label: '归属用户', value: record.userId || '-' }
        ]
      }
    },
    normalizePagedRecords(pageData, type) {
      const records = pageData?.records || pageData?.list || []
      if (type === 'collections') {
        return records.map(record => this.normalizeCollectionRecord(record))
      }
      if (type === 'items') {
        return records.map(record => this.normalizeItemRecord(record))
      }
      if (type === 'notes') {
        return records.map(record => this.normalizeNoteRecord(record))
      }
      if (type === 'tags') {
        return records.map(record => this.normalizeTagRecord(record))
      }
      return []
    },
    async fetchGlobalResults(params) {
      const response = await searchAll(params)
      const data = response?.data?.data || {}
      return {
        collections: this.normalizePagedRecords(data.collections, 'collections'),
        items: this.normalizePagedRecords(data.items, 'items'),
        notes: this.normalizePagedRecords(data.notes, 'notes'),
        tags: this.normalizePagedRecords(data.tags, 'tags')
      }
    },
    async fetchSingleTypeResults(params, type) {
      if (type === 'collections') {
        const response = await searchCollections(params)
        const data = response?.data?.data || {}
        return {
          collections: this.normalizePagedRecords(data.collections, 'collections'),
          items: [],
          notes: [],
          tags: []
        }
      }
      if (type === 'items') {
        const response = await searchItems(params)
        const data = response?.data?.data || {}
        return {
          collections: [],
          items: this.normalizePagedRecords(data.items, 'items'),
          notes: [],
          tags: []
        }
      }
      if (type === 'notes') {
        const response = await searchNotes(params)
        const data = response?.data?.data || {}
        return {
          collections: [],
          items: [],
          notes: this.normalizePagedRecords(data.notes, 'notes'),
          tags: []
        }
      }
      if (type === 'tags') {
        const response = await searchAll({ ...params, searchType: 'tag' })
        const data = response?.data?.data || {}
        return {
          collections: [],
          items: [],
          notes: [],
          tags: this.normalizePagedRecords(data.tags, 'tags')
        }
      }
      return {
        collections: [],
        items: [],
        notes: [],
        tags: []
      }
    },
    updateResultCounts() {
      this.resultCounts = {
        collections: this.searchResults.collections.length,
        items: this.searchResults.items.length,
        notes: this.searchResults.notes.length,
        tags: this.searchResults.tags.length,
        global: this.searchResults.collections.length + this.searchResults.items.length + this.searchResults.notes.length + this.searchResults.tags.length
      }
    },
    async performSearch() {
      const params = this.buildSearchParams()
      if (this.activeTab === 'global') {
        this.searchResults = await this.fetchGlobalResults(params)
      } else {
        this.searchResults = await this.fetchSingleTypeResults(params, this.activeTab)
      }
      this.updateResultCounts()
    },
    async handleSearch() {
      const keyword = this.searchKeyword.trim()
      if (!keyword) {
        this.$message.warning('请输入搜索关键词')
        return
      }
      this.hasSearched = true
      this.searchLoading = true
      try {
        await this.performSearch()
        await Promise.all([this.loadSearchHistory(), this.loadHotKeywords()])
        this.$router.push({
          path: this.currentTabConfig.route,
          query: {
            q: keyword,
            sortBy: this.sortBy,
            timeRange: this.filterTimeRange || undefined,
            digestStatus: this.filterDigestStatus || undefined,
            noteType: this.filterNoteType || undefined
          }
        })
      } catch (error) {
        this.searchResults = {
          collections: [],
          items: [],
          notes: [],
          tags: []
        }
        this.updateResultCounts()
        this.$message.error('搜索失败，请稍后重试')
      } finally {
        this.searchLoading = false
      }
    },
    handleClear() {
      this.searchKeyword = ''
      this.hasSearched = false
      this.resultCounts = {
        collections: 0,
        items: 0,
        notes: 0,
        tags: 0,
        global: 0
      }
      this.searchResults = {
        collections: [],
        items: [],
        notes: [],
        tags: []
      }
    },
    switchTab(tab, autoSearch = false) {
      this.activeTab = TAB_CONFIG[tab] ? tab : 'global'
      if (autoSearch && this.searchKeyword.trim()) {
        this.handleSearch()
        return
      }
      this.$router.push({
        path: this.currentTabConfig.route,
        query: {
          ...this.$route.query,
          q: this.searchKeyword || undefined
        }
      })
    },
    handleTabChange(tab) {
      this.activeTab = tab?.name || 'global'
      if (this.searchKeyword.trim()) {
        this.handleSearch()
      } else {
        this.$router.push({ path: this.currentTabConfig.route })
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
    async syncFromRoute() {
      const routeTab = this.$route.path.split('/').pop()
      this.activeTab = TAB_CONFIG[routeTab] ? routeTab : 'global'
      this.searchKeyword = typeof this.$route.query.q === 'string' ? this.$route.query.q : ''
      this.sortBy = this.$route.query.sortBy || 'relevance'
      this.filterTimeRange = this.$route.query.timeRange || ''
      this.filterDigestStatus = this.$route.query.digestStatus || ''
      this.filterNoteType = this.$route.query.noteType || ''
      if (this.searchKeyword.trim()) {
        this.hasSearched = true
        this.searchLoading = true
        try {
          await this.performSearch()
        } finally {
          this.searchLoading = false
        }
      }
    }
  },
  watch: {
    '$route.fullPath'() {
      this.syncFromRoute()
    }
  },
  async mounted() {
    await Promise.all([this.loadSearchHistory(), this.loadHotKeywords()])
    await this.syncFromRoute()
  }
}
</script>

<style scoped>
.search-center {
  min-height: 100vh;
  background: var(--bg-canvas);
}

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
}

.history-tags,
.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.history-tag,
.hot-tags .el-tag {
  cursor: pointer;
}

.hot-search {
  margin-top: var(--space-8);
}

.hot-title {
  font-size: var(--font-size-md);
  color: var(--text-regular);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.result-section {
  padding: var(--space-8) 0;
}

.tab-bar {
  background: var(--bg-container);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  padding: var(--space-6);
}

.tab-bar-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-6);
  align-items: center;
  margin-bottom: var(--space-4);
}

.search-input-wrapper {
  flex: 1;
}

.header-actions {
  display: flex;
  gap: var(--space-3);
  white-space: nowrap;
}

.filter-btn,
.history-btn {
  color: var(--text-secondary);
}

.advanced-filter {
  background: var(--bg-page);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-top: var(--space-4);
  border: 1px solid var(--border-light);
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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
}

.tab-content {
  margin-top: var(--space-6);
}

.result-summary {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.result-summary h3 {
  margin: 0 0 var(--space-2);
  color: var(--text-primary);
}

.result-summary p {
  margin: 0;
  color: var(--text-secondary);
}

.result-list {
  display: grid;
  gap: var(--space-4);
}

.result-card {
  background: var(--bg-container);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-xs);
}

.result-card-header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}

.result-card-title-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.result-card-title {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.result-card-time {
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

.result-card-desc {
  margin: 0 0 var(--space-3);
  color: var(--text-regular);
  line-height: 1.7;
}

.result-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.meta-item {
  background: var(--bg-page);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-2);
}

.result-state {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  color: var(--text-secondary);
  background: var(--bg-container);
  border: 1px dashed var(--border-base);
  border-radius: var(--radius-lg);
}

.result-state i {
  font-size: var(--font-size-2xl);
}

@media (max-width: 1024px) {
  .filter-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .quick-entry {
    grid-template-columns: 1fr;
  }

  .tab-bar-header,
  .result-summary,
  .result-card-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
