<template>
  <div class="note-search">
    <!-- 搜索状态提示 -->
    <div class="search-status" v-if="hasSearched">
      <span class="keyword">搜索: "{{ displayKeyword }}"</span>
      <span class="count">({{ totalCount }} 篇笔记)</span>
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
          v-model="filterType"
          placeholder="笔记类型"
          size="small"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="原创笔记" value="original" />
          <el-option label="总结笔记" value="summary" />
          <el-option label="普通笔记" value="normal" />
        </el-select>
      </div>
    </div>

    <!-- 内容区域 -->
    <div v-loading="loading" class="search-content">
      <!-- 初始状态 -->
      <div v-if="!hasSearched" class="initial-state">
        <div class="state-icon-wrapper">
          <i class="fas fa-file-alt initial-icon"></i>
        </div>
        <p class="initial-text">请输入关键词搜索笔记</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && notes.length === 0" class="empty-state">
        <div class="state-icon-wrapper">
          <i class="fas fa-search empty-icon"></i>
        </div>
        <p class="empty-text">未找到相关笔记</p>
        <p class="empty-desc">尝试使用不同的关键词搜索</p>
        <el-button type="primary" @click="goToSearchCenter">
          <i class="fas fa-arrow-left"></i> 返回搜索中心
        </el-button>
      </div>

      <!-- 结果列表 -->
      <div v-else-if="notes.length > 0">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="grid-view">
          <el-row :gutter="20">
            <el-col
              v-for="note in paginatedNotes"
              :key="note.id"
              :xs="24" :sm="12" :md="8" :lg="6"
            >
              <div class="note-card" @click="handleNoteClick(note)">
                <div class="card-header">
                  <el-tag :type="getTypeTag(note.type)" size="mini">
                    {{ getTypeLabel(note.type) }}
                  </el-tag>
                  <el-tag v-if="note.isPublic" size="mini" type="success">公开</el-tag>
                </div>
                <div class="card-body">
                  <h3 class="card-title" v-html="highlightText(note.title)"></h3>
                  <p class="card-content" v-html="highlightText(getExcerpt(note.content))"></p>
                  <div class="card-footer">
                    <span class="meta-item">
                      <i class="fas fa-font"></i> {{ note.wordCount || 0 }} 字
                    </span>
                    <span class="meta-item">
                      <i class="fas fa-calendar"></i> {{ formatDate(note.updateTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 列表视图 -->
        <div v-else class="list-view">
          <div
            v-for="note in paginatedNotes"
            :key="note.id"
            class="list-item"
            @click="handleNoteClick(note)"
          >
            <div class="item-type-indicator" :class="getTypeClass(note.type)"></div>
            <div class="item-content">
              <div class="item-header">
                <h3 class="item-title" v-html="highlightText(note.title)"></h3>
                <div class="item-tags">
                  <el-tag :type="getTypeTag(note.type)" size="mini">
                    {{ getTypeLabel(note.type) }}
                  </el-tag>
                  <el-tag v-if="note.isPublic" size="mini" type="success">公开</el-tag>
                </div>
              </div>
              <p class="item-excerpt" v-html="highlightText(getExcerpt(note.content))"></p>
              <div class="item-meta">
                <span class="meta-item"><i class="fas fa-font"></i> {{ note.wordCount || 0 }} 字</span>
                <span class="meta-item"><i class="fas fa-tags"></i> {{ note.tags?.length || 0 }} 标签</span>
                <span class="meta-item"><i class="fas fa-calendar"></i> {{ formatDate(note.updateTime) }}</span>
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
            :total="total"
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

export default {
  name: 'NoteSearch',
  props: {
    keyword: { type: String, default: '' },
    loading: { type: Boolean, default: false }
  },
  data() {
    return {
      viewMode: 'grid',
      filterType: '',
      currentPage: 1,
      pageSize: 12,
      hasSearched: false,
      notes: [],
      total: 0,
      debouncedSearch: null
    }
  },
  mounted() {
    this.debouncedSearch = debounce(() => this._handleSearch(), 300)
  },
  computed: {
    displayKeyword() {
      return this.keyword || this.$route?.query?.q || ''
    },
    totalCount() {
      return this.notes?.length || 0
    },
    paginatedNotes() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.notes.slice(start, start + this.pageSize)
    }
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
      if (this.debouncedSearch) {
        this.debouncedSearch()
      } else {
        this._handleSearch()
      }
    },
    _handleSearch() {
      this.hasSearched = true
      this.currentPage = 1
      try {
        this.notes = this.getMockNotes(this.displayKeyword)
        this.total = this.notes.length
      } catch (error) {
        console.error('搜索笔记失败:', error)
        this.$message.error('搜索失败')
        this.notes = []
        this.total = 0
      }
    },
    handleFilterChange() {
      this.currentPage = 1
      try {
        this.applyFilters()
      } catch (error) {
        console.error('筛选失败:', error)
      }
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    handleNoteClick(note) {
      this.$router.push(`/notes/${note.id}`)
    },
    highlightText(text) {
      if (!text) return ''
      if (!this.keyword) return escapeHtml(text)
      const escaped = escapeHtml(text)
      const escapedKeyword = escapeHtml(this.keyword)
      const regex = new RegExp(`(${escapedKeyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
      return sanitizeHtml(escaped.replace(regex, '<mark>$1</mark>'))
    },
    getExcerpt(content) {
      if (!content) return '暂无内容'
      return content.length > 100 ? content.substring(0, 100) + '...' : content
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    getTypeClass(type) {
      const map = { original: 'type-original', summary: 'type-summary', normal: 'type-normal' }
      return map[type] || 'type-normal'
    },
    getTypeTag(type) {
      const map = { original: 'success', summary: 'warning', normal: 'info' }
      return map[type] || 'info'
    },
    getTypeLabel(type) {
      const map = { original: '原创', summary: '总结', normal: '普通' }
      return map[type] || '普通'
    },
    goToSearchCenter() {
      this.$router.push('/search/center')
    },
    applyFilters() {
      const allNotes = this.getMockNotes(this.displayKeyword)
      this.notes = this.filterType
        ? allNotes.filter(n => n.type === this.filterType)
        : allNotes
      this.total = this.notes.length
    },
    getMockNotes(kw) {
      return [
        { id: 1, title: `${kw} 学习总结`, type: 'summary', content: '经过一段时间的学习，我总结了关于知识管理的最佳实践方法。包含知识收集、整理、消化和输出的完整流程。', wordCount: 2345, isPublic: true, tags: ['学习', '总结'], updateTime: '2024-01-15' },
        { id: 2, title: `深入理解 ${kw}`, type: 'original', content: '通过实际项目经验，深入探讨了核心概念和应用场景，并总结了常见问题的解决方案。', wordCount: 3567, isPublic: false, tags: ['原创', '深度'], updateTime: '2024-01-12' },
        { id: 3, title: `${kw} 快速笔记`, type: 'normal', content: '日常学习中记录的零散笔记，包含重要知识点的快速记录。', wordCount: 890, isPublic: false, tags: ['笔记'], updateTime: '2024-01-10' }
      ]
    }
  }
}
</script>

<style scoped>
.note-search {
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
.grid-view { margin-bottom: var(--space-4); }

.note-card {
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

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4) 0;
  gap: var(--space-2);
}

.card-body { padding: var(--space-4); }

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

.card-title :deep(.highlight),
.card-content :deep(.highlight),
.item-title :deep(.highlight),
.item-excerpt :deep(.highlight) {
  background: var(--warning-bg);
  color: var(--warning-color);
  padding: var(--space-0, 1px) var(--space-1);
  border-radius: var(--radius-sm);
}

.card-content {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
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

.item-type-indicator {
  width: 6px;
  align-self: stretch;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
  min-height: 40px;
}

.item-type-indicator.type-original { background: var(--success-color); }
.item-type-indicator.type-summary { background: var(--warning-color); }
.item-type-indicator.type-normal { background: var(--text-placeholder); }

.item-content { flex: 1; min-width: 0; }

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-1);
  gap: var(--space-2);
}

.item-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.item-tags {
  display: flex;
  gap: var(--space-1);
  flex-shrink: 0;
}

.item-excerpt {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
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
