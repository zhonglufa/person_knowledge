<template>
  <div class="page-wrapper">
    <!-- 页面主内容 -->
    <main class="page-main">
      <div class="page-container">
        <!-- 页面标题区 -->
        <div class="page-header-section">
          <div class="page-header-content">
            <div class="page-header-left">
              <el-button type="text" @click="goBack" class="back-btn">
                <i class="fas fa-arrow-left"></i> 返回
              </el-button>
              <h1 class="page-title">搜索历史</h1>
            </div>
            <div class="page-header-right">
              <el-button type="danger" size="small" @click="handleClearAll" :disabled="historyList.length === 0">
                <i class="fas fa-trash"></i> 清空全部
              </el-button>
            </div>
          </div>
        </div>

        <!-- 统计概览 -->
        <div class="stats-grid" v-if="historyList.length > 0">
          <div class="stat-card">
            <div class="stat-icon gradient-primary">
              <i class="fas fa-search"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ historyList.length }}</div>
              <div class="stat-label">搜索次数</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon gradient-secondary">
              <i class="fas fa-calendar"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ latestDate }}</div>
              <div class="stat-label">最近搜索</div>
            </div>
          </div>
        </div>

        <!-- 历史列表 -->
        <div class="content-card" v-loading="loading">
          <!-- 空状态 -->
          <div v-if="!loading && historyList.length === 0" class="empty-state">
            <div class="empty-icon-wrapper">
              <i class="fas fa-history"></i>
            </div>
            <h3 class="empty-title">暂无搜索历史</h3>
            <p class="empty-description">开始搜索后，您的历史记录将显示在这里</p>
            <el-button type="primary" @click="goToSearchCenter">
              <i class="fas fa-search"></i> 去搜索
            </el-button>
          </div>

          <!-- 历史记录 -->
          <div v-else class="history-list">
            <div
              v-for="(item, index) in historyList"
              :key="item.id"
              class="history-item"
              @click="handleSearchAgain(item.keyword)"
            >
              <div class="item-index">{{ index + 1 }}</div>
              <div class="item-content">
                <div class="item-keyword">
                  <i class="fas fa-search"></i>
                  <span>{{ item.keyword }}</span>
                </div>
                <div class="item-meta">
                  <span class="time"><i class="fas fa-clock"></i> {{ formatDate(item.time) }}</span>
                  <span class="source" v-if="item.source">
                    <i class="fas fa-folder"></i> {{ getSourceLabel(item.source) }}
                  </span>
                </div>
              </div>
              <div class="item-actions">
                <el-button type="text" size="small" @click.stop="handleSearchAgain(item.keyword)">
                  <i class="fas fa-redo"></i> 重新搜索
                </el-button>
                <el-button type="text" size="small" class="text-danger" @click.stop="handleDeleteItem(item)">
                  <i class="fas fa-trash"></i>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { getSearchHistory, deleteSearchHistory, clearSearchHistory } from '@/api/search'

export default {
  name: 'SearchHistory',
  data() {
    return {
      loading: false,
      historyList: []
    }
  },
  computed: {
    latestDate() {
      if (this.historyList?.length === 0) return '-'
      const latest = this.historyList[0]?.time
      return latest ? new Date(latest).toLocaleDateString('zh-CN') : '-'
    }
  },
  methods: {
    async loadHistory() {
      this.loading = true
      try {
        const response = await getSearchHistory()
        const records = response?.data?.data || response?.data || []
        if (Array.isArray(records) && records.length > 0) {
          this.historyList = records.map((item, index) => ({
            id: item.id || index + 1,
            keyword: item.keyword || item.content,
            time: item.createTime || item.time || '',
            source: item.source || ''
          }))
          localStorage.setItem('searchHistory', JSON.stringify(this.historyList))
          return
        }

        const stored = localStorage.getItem('searchHistory')
        if (stored) {
          this.historyList = JSON.parse(stored)
        } else {
          this.historyList = this.getMockHistory()
        }
      } catch (error) {
        console.error('加载搜索历史失败:', error)
        const stored = localStorage.getItem('searchHistory')
        this.historyList = stored ? JSON.parse(stored) : []
      } finally {
        this.loading = false
      }
    },
    async loadHotKeywords() {
      try {
        const response = await getHotSearchKeywords()
        const records = response?.data?.data || response?.data || []
        if (Array.isArray(records) && records.length > 0) {
          this.hotKeywords = records.map((item, index) => ({
            rank: index + 1,
            keyword: item.keyword || item.content || item,
            count: item.count || item.searchCount || 0
          }))
          return
        }

        this.hotKeywords = this.getMockHotKeywords()
      } catch (error) {
        console.error('加载热门搜索失败:', error)
        this.hotKeywords = this.getMockHotKeywords()
      }
    },
    handleSearchAgain(keyword) {
      if (!keyword?.trim()) return
      this.$router.push({
        path: '/search/global',
        query: { q: keyword.trim() }
      })
    },
    handleDeleteItem(item) {
      try {
        this.$confirm('确定要删除这条搜索记录吗？', '提示', {
          type: 'warning'
        }).then(async () => {
          if (item.id) {
            await deleteSearchHistory(item.id)
          }
          this.historyList = this.historyList.filter(h => h.id !== item.id)
          this.saveHistory()
          this.$message.success('删除成功')
        }).catch(() => {})
      } catch (error) {
        console.error('删除失败:', error)
      }
    },
    handleClearAll() {
      try {
        this.$confirm('确定要清空所有搜索历史吗？此操作不可撤销。', '确认清空', {
          type: 'warning'
        }).then(async () => {
          await clearSearchHistory()
          this.historyList = []
          this.saveHistory()
          this.$message.success('已清空搜索历史')
        }).catch(() => {})
      } catch (error) {
        console.error('清空失败:', error)
      }
    },
    saveHistory() {
      try {
        localStorage.setItem('searchHistory', JSON.stringify(this.historyList))
      } catch (error) {
        console.error('保存搜索历史失败:', error)
      }
    },
    formatDate(date) {
      if (!date) return ''
      const now = new Date()
      const target = new Date(date)
      const diff = now - target
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
      return target.toLocaleDateString('zh-CN')
    },
    getSourceLabel(source) {
      const map = { global: '全局搜索', collection: '收藏集', note: '笔记' }
      return map[source] || '全局搜索'
    },
    goBack() {
      this.$router.back()
    },
    goToSearchCenter() {
      this.$router.push('/search/center')
    },
    getMockHistory() {
      return [
        { id: 1, keyword: 'Vue 组件设计', time: new Date().toISOString(), source: 'global' },
        { id: 2, keyword: '知识管理方法', time: new Date(Date.now() - 3600000).toISOString(), source: 'collection' },
        { id: 3, keyword: '前端开发技巧', time: new Date(Date.now() - 86400000).toISOString(), source: 'note' },
        { id: 4, keyword: '设计模式', time: new Date(Date.now() - 172800000).toISOString(), source: 'global' }
      ]
    }
  },
  mounted() {
    this.loadHistory()
  }
}
</script>

<style scoped>
/* 返回按钮 */
.back-btn {
  color: var(--text-secondary);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.back-btn:hover {
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

/* Header */
.page-header-section {
  padding: var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

.page-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

/* History List */
.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.history-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.history-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  transform: translateX(4px);
}

.item-index {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  background: var(--bg-canvas);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--font-size-sm);
  flex-shrink: 0;
}

.history-item:nth-child(1) .item-index {
  background: var(--primary-color);
  color: white;
}

.history-item:nth-child(2) .item-index {
  background: var(--success-color);
  color: white;
}

.history-item:nth-child(3) .item-index {
  background: var(--warning-color);
  color: white;
}

.item-content { flex: 1; min-width: 0; }

.item-keyword {
  font-size: var(--font-size-md);
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.item-meta {
  display: flex;
  gap: var(--space-4);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.time, .source {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.item-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.history-item:hover .item-actions {
  opacity: 1;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-4);
  animation: fadeIn 0.3s ease;
}

.empty-icon-wrapper {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-full);
  background: var(--bg-canvas);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
  font-size: 40px;
  color: var(--text-placeholder);
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
  margin: 0 0 var(--space-4) 0;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--primary-color);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl);
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

/* Content Card */
.content-card {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  padding: var(--space-6);
  min-height: 200px;
}

.text-danger { color: var(--danger-color); }

/* Responsive */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .item-actions {
    opacity: 1;
    align-self: flex-end;
  }
}
</style>
