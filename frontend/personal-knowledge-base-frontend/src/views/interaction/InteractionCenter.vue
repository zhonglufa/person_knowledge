<template>
  <div class="interaction-center-page">
    <div class="page-header">
      <div>
        <h2>互动中心</h2>
        <p>查看收到的评论、点赞和收藏互动</p>
      </div>
      <div class="header-actions">
        <el-badge :value="currentUnreadCount" :hidden="currentUnreadCount === 0">
          <el-button type="primary" plain @click="handleMarkAllRead">全部已读</el-button>
        </el-badge>
      </div>
    </div>

    <div class="stats-section">
      <div class="stat-card" :class="{ active: activeTab === 'comment' }" @click="switchTab('comment')">
        <div class="stat-value">{{ stats.commentTotal }}</div>
        <div class="stat-label">收到的评论</div>
        <div class="stat-meta">未读 {{ stats.commentUnread }}</div>
      </div>
      <div class="stat-card" :class="{ active: activeTab === 'like' }" @click="switchTab('like')">
        <div class="stat-value">{{ stats.likeTotal }}</div>
        <div class="stat-label">收到的点赞</div>
        <div class="stat-meta">未读 {{ stats.likeUnread }}</div>
      </div>
      <div class="stat-card" :class="{ active: activeTab === 'collect' }" @click="switchTab('collect')">
        <div class="stat-value">{{ stats.collectTotal }}</div>
        <div class="stat-label">收到的收藏</div>
        <div class="stat-meta">未读 {{ stats.collectUnread }}</div>
      </div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unread">未读</el-radio-button>
        <el-radio-button label="read">已读</el-radio-button>
      </el-radio-group>
    </div>

    <el-card class="list-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="收到的评论" name="comment" />
        <el-tab-pane label="收到的点赞" name="like" />
        <el-tab-pane label="收到的收藏" name="collect" />
      </el-tabs>

      <div v-loading="loading">
        <div v-if="messages.length" class="interaction-list">
          <div
            v-for="item in messages"
            :key="item.id"
            class="interaction-item"
            :class="{ unread: Number(item.isRead) === 0 }"
          >
            <div class="item-main" @click="handleNavigate(item)">
              <div class="item-title-row">
                <div class="item-title-wrap">
                  <span v-if="Number(item.isRead) === 0" class="unread-dot"></span>
                  <span class="item-title">{{ buildItemTitle(item) }}</span>
                </div>
                <el-tag size="small" :type="resolveTagType(item.interactionType)">{{ resolveTabLabel(item.interactionType) }}</el-tag>
              </div>
              <div class="item-content">{{ item.summary || '收到一条新的互动消息' }}</div>
              <div class="item-meta">
                <span>互动时间：{{ formatDateTime(item.createdAt) }}</span>
                <span v-if="item.targetTitle">目标内容：{{ item.targetTitle }}</span>
              </div>
            </div>
            <div class="item-actions">
              <el-button v-if="Number(item.isRead) === 0" type="primary" link @click.stop="handleMarkRead(item)">标记已读</el-button>
              <el-button type="danger" link @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else :description="emptyText" />
      </div>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import {
  deleteInteraction,
  getInteractionStats,
  getReceivedCollects,
  getReceivedComments,
  getReceivedLikes,
  markAllInteractionsRead,
  markInteractionRead
} from '@/api/notification'

export default {
  name: 'InteractionCenter',
  data() {
    return {
      loading: false,
      activeTab: 'comment',
      statusFilter: 'all',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      messages: [],
      stats: {
        commentUnread: 0,
        likeUnread: 0,
        collectUnread: 0,
        commentTotal: 0,
        likeTotal: 0,
        collectTotal: 0
      }
    }
  },
  computed: {
    statusValue() {
      if (this.statusFilter === 'unread') return 0
      if (this.statusFilter === 'read') return 1
      return undefined
    },
    currentUnreadCount() {
      if (this.activeTab === 'comment') return this.stats.commentUnread || 0
      if (this.activeTab === 'like') return this.stats.likeUnread || 0
      if (this.activeTab === 'collect') return this.stats.collectUnread || 0
      return 0
    },
    emptyText() {
      if (this.activeTab === 'comment') return '暂无收到的评论'
      if (this.activeTab === 'like') return '暂无收到的点赞'
      return '暂无收到的收藏'
    }
  },
  async mounted() {
    await this.refreshData()
  },
  methods: {
    async fetchStats() {
      const { data } = await getInteractionStats()
      this.stats = {
        commentUnread: Number(data?.commentUnread || 0),
        likeUnread: Number(data?.likeUnread || 0),
        collectUnread: Number(data?.collectUnread || 0),
        commentTotal: Number(data?.commentTotal || 0),
        likeTotal: Number(data?.likeTotal || 0),
        collectTotal: Number(data?.collectTotal || 0)
      }
    },
    async fetchMessages() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.statusValue !== undefined) {
          params.isRead = this.statusValue
        }

        let requestFn = getReceivedComments
        if (this.activeTab === 'like') {
          requestFn = getReceivedLikes
        } else if (this.activeTab === 'collect') {
          requestFn = getReceivedCollects
        }

        const { data } = await requestFn(params)
        this.messages = Array.isArray(data?.list) ? data.list : []
        this.total = Number(data?.total || 0)
        this.pageNum = Number(data?.pageNum || this.pageNum)
        this.pageSize = Number(data?.pageSize || this.pageSize)
      } finally {
        this.loading = false
      }
    },
    async refreshData() {
      await Promise.all([this.fetchStats(), this.fetchMessages()])
    },
    switchTab(tab) {
      if (this.activeTab === tab) return
      this.activeTab = tab
      this.pageNum = 1
      this.fetchMessages()
    },
    handleTabClick() {
      this.pageNum = 1
      this.fetchMessages()
    },
    handleFilterChange() {
      this.pageNum = 1
      this.fetchMessages()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.fetchMessages()
    },
    async handleMarkRead(item) {
      await markInteractionRead(item.id)
      this.$message.success('标记已读成功')
      await this.refreshData()
    },
    async handleMarkAllRead() {
      await markAllInteractionsRead(this.activeTab)
      this.$message.success('全部已读成功')
      await this.refreshData()
    },
    async handleDelete(item) {
      await this.$confirm(`确定删除这条互动消息吗？`, '删除确认', {
        type: 'warning'
      })
      await deleteInteraction(item.id)
      this.$message.success('删除成功')
      await this.refreshData()
    },
    async handleNavigate(item) {
      if (Number(item.isRead) === 0) {
        await markInteractionRead(item.id)
      }
      if (item.targetUrl) {
        await this.$router.push(item.targetUrl)
      }
      await this.refreshData()
    },
    resolveTabLabel(type) {
      if (type === 'comment') return '评论'
      if (type === 'like') return '点赞'
      if (type === 'collect') return '收藏'
      return '互动'
    },
    resolveTagType(type) {
      if (type === 'comment') return 'primary'
      if (type === 'like') return 'danger'
      if (type === 'collect') return 'warning'
      return 'info'
    },
    buildItemTitle(item) {
      const actor = item.actorName || '有用户'
      if (item.interactionType === 'comment') return `${actor} 评论了您的内容`
      if (item.interactionType === 'like') return `${actor} 点赞了您的内容`
      if (item.interactionType === 'collect') return `${actor} 收藏了您的内容`
      return `${actor} 与您的内容产生了互动`
    },
    formatDateTime(value) {
      if (!value) return '--'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      const yyyy = date.getFullYear()
      const mm = String(date.getMonth() + 1).padStart(2, '0')
      const dd = String(date.getDate()).padStart(2, '0')
      const hh = String(date.getHours()).padStart(2, '0')
      const mi = String(date.getMinutes()).padStart(2, '0')
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
    }
  }
}
</script>

<style scoped>
.interaction-center-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.stat-card:hover,
.stat-card.active {
  border-color: #409eff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

.stat-meta {
  margin-top: 6px;
  font-size: 13px;
  color: #909399;
}

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.list-card {
  border-radius: 16px;
}

.interaction-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.interaction-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  transition: all 0.2s ease;
}

.interaction-item.unread {
  border-color: #c6e2ff;
  background: #f4f9ff;
}

.interaction-item:hover {
  border-color: #409eff;
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.08);
}

.item-main {
  flex: 1;
  cursor: pointer;
}

.item-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 10px;
}

.item-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.item-content {
  margin-bottom: 10px;
  color: #606266;
  line-height: 1.6;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #909399;
  font-size: 13px;
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .interaction-center-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-section {
    grid-template-columns: 1fr;
  }

  .interaction-item {
    flex-direction: column;
  }

  .item-actions {
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>
