<template>
  <div class="notification-center-page">
    <div class="page-header">
      <div>
        <h2>通知中心</h2>
        <p>查看系统公告、学习提醒与笔记通知</p>
      </div>
      <div class="header-actions">
        <el-button v-if="!batchMode" type="default" plain @click="toggleBatchMode">批量管理</el-button>
        <template v-else>
          <el-button type="default" plain @click="handleSelectAll">
            {{ isAllSelected ? '取消全选' : '全选' }}
          </el-button>
          <el-button type="primary" plain :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            批量删除 ({{ selectedIds.length }})
          </el-button>
          <el-button plain @click="cancelBatchMode">取消</el-button>
        </template>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-button type="primary" plain @click="handleMarkAllRead">全部已读</el-button>
        </el-badge>
      </div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="unread">未读</el-radio-button>
        <el-radio-button label="read">已读</el-radio-button>
      </el-radio-group>

      <el-select v-model="typeFilter" placeholder="通知类型" clearable @change="handleFilterChange">
        <el-option label="全部类型" value="" />
        <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </div>

    <el-card class="list-card" shadow="never">
      <div v-loading="loading">
        <div v-if="notifications.length" class="notification-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: Number(item.isRead) === 0, 'batch-mode': batchMode }"
          >
            <el-checkbox
              v-if="batchMode"
              v-model="selectedIds"
              :label="item.id"
              class="batch-checkbox"
              @click.native.stop
            />
            <div class="item-main" @click="handleNavigate(item)">
              <div class="item-title-row">
                <div class="item-title-wrap">
                  <span v-if="Number(item.isRead) === 0" class="unread-dot"></span>
                  <span class="item-title">{{ item.title }}</span>
                </div>
                <el-tag size="small" :type="resolveTypeTag(item.notifyType)">{{ resolveTypeLabel(item.notifyType) }}</el-tag>
              </div>
              <div class="item-content">{{ item.content }}</div>
              <div class="item-meta">
                <span>创建时间：{{ formatDateTime(item.createdAt) }}</span>
                <span v-if="item.remindAt">提醒时间：{{ formatDateTime(item.remindAt) }}</span>
                <span v-if="item.targetTitle">目标：{{ item.targetTitle }}</span>
              </div>
            </div>
            <div v-if="!batchMode" class="item-actions">
              <el-button v-if="Number(item.isRead) === 0" type="primary" link @click.stop="handleMarkRead(item)">标记已读</el-button>
              <el-button type="danger" link @click.stop="handleDelete(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无通知" />
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
  batchDeleteNotifications,
  deleteNotification,
  getNotificationList,
  getUnreadCount,
  markAllNotificationRead,
  markNotificationRead
} from '@/api/notification'

export default {
  name: 'NotificationCenter',
  data() {
    return {
      loading: false,
      notifications: [],
      unreadCount: 0,
      statusFilter: 'all',
      typeFilter: '',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      batchMode: false,
      selectedIds: [],
      typeOptions: [
        { label: '学习提醒', value: 1 },
        { label: '系统公告', value: 2 },
        { label: '笔记提醒', value: 3 },
        { label: '进度逾期提醒', value: 4 },
        { label: '点赞', value: 5 },
        { label: '评论', value: 6 },
        { label: '收藏', value: 7 }
      ]
    }
  },
  computed: {
    statusValue() {
      if (this.statusFilter === 'unread') return 0
      if (this.statusFilter === 'read') return 1
      return undefined
    },
    isAllSelected() {
      return this.notifications.length > 0 && this.selectedIds.length === this.notifications.length
    }
  },
  async mounted() {
    await this.refreshData()
  },
  methods: {
    async fetchUnreadCount() {
      const { data } = await getUnreadCount()
      this.unreadCount = data || 0
    },
    async fetchNotifications() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.statusValue !== undefined) {
          params.isRead = this.statusValue
        }
        if (this.typeFilter !== '' && this.typeFilter !== null) {
          params.notifyType = this.typeFilter
        }
        const { data } = await getNotificationList(params)
        this.notifications = Array.isArray(data?.list) ? data.list : []
        this.total = Number(data?.total || 0)
        this.pageNum = Number(data?.pageNum || this.pageNum)
        this.pageSize = Number(data?.pageSize || this.pageSize)
      } finally {
        this.loading = false
      }
    },
    async refreshData() {
      await Promise.all([this.fetchNotifications(), this.fetchUnreadCount()])
    },
    handleFilterChange() {
      this.pageNum = 1
      this.fetchNotifications()
    },
    handlePageChange(page) {
      this.pageNum = page
      this.fetchNotifications()
    },
    async handleMarkRead(item) {
      await markNotificationRead(item.id)
      this.$message.success('标记已读成功')
      await this.refreshData()
    },
    async handleMarkAllRead() {
      await markAllNotificationRead()
      this.$message.success('全部已读成功')
      await this.refreshData()
    },
    async handleDelete(item) {
      await this.$confirm(`确定删除通知「${item.title}」吗？`, '删除确认', {
        type: 'warning'
      })
      await deleteNotification(item.id)
      this.$message.success('删除成功')
      await this.refreshData()
    },
    toggleBatchMode() {
      this.batchMode = true
      this.selectedIds = []
    },
    cancelBatchMode() {
      this.batchMode = false
      this.selectedIds = []
    },
    handleSelectAll() {
      if (this.isAllSelected) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.notifications.map(item => item.id)
      }
    },
    async handleBatchDelete() {
      if (this.selectedIds.length === 0) {
        this.$message.warning('请选择要删除的通知')
        return
      }
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedIds.length} 条通知吗？`, '批量删除确认', {
          type: 'warning'
        })
        await batchDeleteNotifications(this.selectedIds)
        this.$message.success(`已删除 ${this.selectedIds.length} 条通知`)
        this.cancelBatchMode()
        await this.refreshData()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }
    },
    normalizeTargetUrl(targetUrl) {
      if (!targetUrl || typeof targetUrl !== 'string') {
        return null
      }

      if (/^https?:\/\//i.test(targetUrl)) {
        return null
      }

      let url = targetUrl.trim()
      if (!url.startsWith('/')) {
        url = '/' + url
      }

      const mapping = [
        { from: /^\/notes\/(\d+)(\?.*)?$/i, to: '/creation/notes/$1' },
        { from: /^\/collection\/(\d+)(\?.*)?$/i, to: '/collections/$1' },
        { from: /^\/creation\/processing\/tasks(\?.*)?$/i, to: '/creation/processing$1' },
        { from: /^\/creation\/processing\/(\?.*)?$/i, to: '/creation/processing$1' }
      ]

      for (const item of mapping) {
        const match = url.match(item.from)
        if (match) {
          url = url.replace(item.from, item.to)
          break
        }
      }

      const validPrefixes = ['/collections/', '/creation/notes/', '/creation/processing', '/taxonomy/', '/announcements/']
      const isValid = validPrefixes.some(prefix => url.startsWith(prefix))
      return isValid ? url : null
    },
    async handleNavigate(item) {
      if (Number(item.isRead) === 0) {
        await markNotificationRead(item.id)
      }

      let target = this.normalizeTargetUrl(item.targetUrl)
      
      if (target && item.notifyType === 6 && item.extraData) {
        try {
          const extraData = typeof item.extraData === 'string' 
            ? JSON.parse(item.extraData) 
            : item.extraData
          
          if (extraData && extraData.commentId) {
            const separator = target.includes('?') ? '&' : '?'
            target = `${target}${separator}commentId=${extraData.commentId}`
          }
        } catch (error) {
          console.warn('解析extraData失败:', error)
        }
      }
      
      if (target) {
        await this.$router.push(target)
      } else if (item.targetUrl) {
        this.$message.warning('该通知关联的页面暂不可访问')
      }

      await this.refreshData()
    },
    resolveTypeLabel(type) {
      const found = this.typeOptions.find(item => item.value === type)
      return found ? found.label : '系统通知'
    },
    resolveTypeTag(type) {
      if (type === 1 || type === 4) return 'warning'
      if (type === 2) return 'success'
      if (type === 3) return 'info'
      if (type === 5) return 'danger'
      if (type === 6) return 'primary'
      if (type === 7) return ''
      return 'info'
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
.notification-center-page {
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

.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.list-card {
  border-radius: 16px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  transition: all 0.2s ease;
}

.notification-item.unread {
  border-color: #c6e2ff;
  background: #f4f9ff;
}

.notification-item:hover {
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

.notification-item.batch-mode {
  padding-left: 12px;
}

.batch-checkbox {
  margin-right: 12px;
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .notification-center-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .notification-item {
    flex-direction: column;
  }

  .item-actions {
    flex-direction: row;
    justify-content: flex-start;
  }
}
</style>
