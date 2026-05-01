<template>
  <div v-loading="pageLoading" class="notification-center">
    <!-- 页面头部 -->
    <div class="page-header card">
      <div class="header-content">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
              <el-breadcrumb-item>通知中心</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <h2 class="page-title">
            <i class="fas fa-bell"></i>
            通知中心
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="unread-badge" />
          </h2>
        </div>
        <div class="header-right">
          <el-button
            type="primary"
            plain
            size="small"
            icon="el-icon-check"
            :disabled="unreadCount === 0"
            @click="handleMarkAllRead"
          >
            全部标记已读
          </el-button>
          <el-button
            type="danger"
            plain
            size="small"
            icon="el-icon-delete"
            :disabled="notificationList.length === 0"
            @click="handleClearAll"
          >
            清空通知
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar card">
      <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
        <el-radio-button label="all">
          <i class="fas fa-list"></i> 全部
          <span v-if="totalCount > 0" class="filter-count">({{ totalCount }})</span>
        </el-radio-button>
        <el-radio-button label="unread">
          <i class="fas fa-circle"></i> 未读
          <span v-if="unreadCount > 0" class="filter-count">({{ unreadCount }})</span>
        </el-radio-button>
        <el-radio-button label="read">
          <i class="fas fa-check-circle"></i> 已读
        </el-radio-button>
      </el-radio-group>

      <div class="filter-type">
        <el-select v-model="filterNotifyType" size="small" placeholder="通知类型" @change="handleFilterChange" clearable>
          <el-option label="全部类型" value=""></el-option>
          <el-option label="系统公告" :value="2">系统公告</el-option>
          <el-option label="学习提醒" :value="1">学习提醒</el-option>
          <el-option label="笔记提醒" :value="3">笔记提醒</el-option>
          <el-option label="进度逾期" :value="4">进度逾期</el-option>
          <el-option label="点赞通知" :value="5">点赞通知</el-option>
          <el-option label="评论通知" :value="6">评论通知</el-option>
          <el-option label="收藏通知" :value="7">收藏通知</el-option>
        </el-select>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <!-- 空状态 -->
      <div v-if="!pageLoading && notificationList.length === 0" class="empty-state">
        <i class="fas fa-bell-slash"></i>
        <h3>暂无通知</h3>
        <p>当您有新的通知时会在这里显示</p>
      </div>

      <!-- 通知卡片列表 -->
      <transition-group name="notification-slide" tag="div" v-else>
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item card"
          :class="{ 'is-unread': item.isRead === 0 }"
          @click="handleClickNotification(item)"
        >
          <!-- 未读圆点 -->
          <div class="unread-dot" v-if="item.isRead === 0"></div>

          <!-- 通知图标 -->
          <div class="notification-icon" :class="getIconClass(item.type)">
            <i :class="getIconName(item.type)"></i>
          </div>

          <!-- 通知内容 -->
          <div class="notification-content">
            <div class="notification-header">
              <span class="notification-type-tag" :class="`tag-${item.type || 'system'}`">
                {{ getTypeLabel(item.type) }}
              </span>
              <h4 class="notification-title">{{ item.title || '系统通知' }}</h4>
            </div>
            <p class="notification-message">{{ item.content || '' }}</p>
            <div class="notification-meta">
              <span class="notification-time">
                <i class="fas fa-clock"></i>
                {{ formatTime(item.createTime) }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="notification-actions" @click.stop>
            <el-button
              v-if="item.isRead === 0"
              type="text"
              size="small"
              class="action-btn"
              @click.stop="handleMarkRead(item)"
            >
              <i class="fas fa-check"></i> 标记已读
            </el-button>
            <el-button
              type="text"
              size="small"
              class="action-btn delete-btn"
              @click.stop="handleDelete(item)"
            >
              <i class="fas fa-trash-alt"></i> 删除
            </el-button>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="handlePageChange"
      ></el-pagination>
    </div>
  </div>
</template>

<script>
import {
  getNotificationList,
  markNotificationRead,
  markAllNotificationRead,
  deleteNotification,
  getUnreadCount
} from '@/api/notification';

export default {
  name: 'NotificationCenter',

  data() {
    return {
      pageLoading: false,
      notificationList: [],
      filterType: 'all',
      filterNotifyType: '',
      currentPage: 1,
      pageSize: 15,
      total: 0,
      unreadCount: 0,
      totalCount: 0
    };
  },

  created() {
    this.fetchNotifications();
    this.fetchUnreadCount();
  },

  methods: {
    /**
     * 获取通知列表
     */
    async fetchNotifications() {
      this.pageLoading = true;
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        };
        if (this.filterType === 'unread') {
          params.isRead = 0;
        } else if (this.filterType === 'read') {
          params.isRead = 1;
        }
        if (this.filterNotifyType) {
          params.type = this.filterNotifyType;
        }
        const res = await getNotificationList(params);
        const data = (res && res.data) || res || {};
        this.notificationList = Array.isArray(data.records) ? data.records : (Array.isArray(data) ? data : []);
        this.total = data.total || this.notificationList.length || 0;
        this.totalCount = data.totalCount || this.total;
      } catch (error) {
        console.error('获取通知列表失败:', error);
        this.notificationList = [];
        this.total = 0;
        this.totalCount = 0;
        this.$message.error('获取通知列表失败');
      } finally {
        this.pageLoading = false;
      }
    },

    /**
     * 获取未读数量
     */
    async fetchUnreadCount() {
      try {
        const res = await getUnreadCount();
        this.unreadCount = (res && res.data) || res || 0;
      } catch (error) {
        console.error('获取未读数量失败:', error);
        this.unreadCount = 0;
      }
    },

    /**
     * 标记单条已读
     */
    async handleMarkRead(item) {
      if (!item || !item.id) return;
      try {
        await markNotificationRead(item.id);
        item.isRead = 1;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
        this.$message.success('已标记为已读');
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    /**
     * 全部标记已读
     */
    async handleMarkAllRead() {
      try {
        await markAllNotificationRead();
        this.notificationList.forEach(item => {
          if (item) item.isRead = 1;
        });
        this.unreadCount = 0;
        this.$message.success('已全部标记为已读');
        if (this.filterType === 'unread') {
          this.fetchNotifications();
        }
      } catch (error) {
        this.$message.error('操作失败');
      }
    },

    /**
     * 清空所有通知
     */
    async handleClearAll() {
      try {
        await this.$confirm('确定清空所有通知吗？此操作不可撤销', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        // TODO: 调用清空通知 API
        this.notificationList = [];
        this.total = 0;
        this.unreadCount = 0;
        this.totalCount = 0;
        this.$message.success('已清空通知');
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败');
        }
      }
    },

    /**
     * 删除通知
     */
    async handleDelete(item) {
      if (!item || !item.id) return;
      try {
        await this.$confirm('确定删除该通知吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        await deleteNotification(item.id);
        if (item.isRead === 0) {
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
        this.fetchNotifications();
        this.fetchUnreadCount();
        this.$message.success('删除成功');
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败');
        }
      }
    },

    /**
     * 点击通知跳转
     */
    handleClickNotification(item) {
      if (!item) return;
      // 先标记已读
      if (item.isRead === 0) {
        this.handleMarkRead(item);
      }
      // 根据业务类型跳转
      if (item.targetUrl) {
        this.$router.push(item.targetUrl).catch(() => {});
      } else if (item.businessType === 'collect' && item.businessId) {
        this.$router.push('/collect/center').catch(() => {});
      } else if (item.businessType === 'note' && item.businessId) {
        this.$router.push('/creation').catch(() => {});
      }
    },

    /**
     * 筛选变化
     */
    handleFilterChange() {
      this.currentPage = 1;
      this.fetchNotifications();
    },

    /**
     * 分页变化
     */
    handlePageChange(page) {
      this.currentPage = page;
      this.fetchNotifications();
    },

    /**
     * 获取图标类名
     */
    getIconClass(type) {
      const map = {
        1: 'icon-learning',      // 学习提醒
        2: 'icon-system',        // 系统公告
        3: 'icon-note',          // 笔记提醒
        4: 'icon-warning',       // 进度逾期
        5: 'icon-like',          // 点赞通知
        6: 'icon-comment',       // 评论通知
        7: 'icon-collect',       // 收藏通知
        system: 'icon-system',
        collect: 'icon-collect',
        interaction: 'icon-interaction',
        note: 'icon-note'
      };
      return map[type] || 'icon-system';
    },

    /**
     * 获取图标名
     */
    getIconName(type) {
      const map = {
        1: 'fas fa-clock',           // 学习提醒
        2: 'fas fa-bullhorn',         // 系统公告
        3: 'fas fa-sticky-note',      // 笔记提醒
        4: 'fas fa-exclamation-triangle', // 进度逾期
        5: 'fas fa-heart',            // 点赞通知
        6: 'fas fa-comment',          // 评论通知
        7: 'fas fa-star',             // 收藏通知
        system: 'fas fa-cog',
        collect: 'fas fa-bookmark',
        interaction: 'fas fa-comments',
        note: 'fas fa-file-alt'
      };
      return map[type] || 'fas fa-bell';
    },

    /**
     * 获取类型标签
     */
    getTypeLabel(type) {
      const map = {
        1: '学习提醒',
        2: '系统公告',
        3: '笔记提醒',
        4: '进度逾期',
        5: '点赞',
        6: '评论',
        7: '收藏',
        system: '系统',
        collect: '收藏',
        interaction: '互动',
        note: '笔记'
      };
      return map[type] || '系统';
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
      if (!time) return '';
      const date = new Date(time);
      if (isNaN(date.getTime())) return time;
      const now = new Date();
      const diff = now - date;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
      const y = date.getFullYear();
      const m = String(date.getMonth() + 1).padStart(2, '0');
      const d = String(date.getDate()).padStart(2, '0');
      return `${y}-${m}-${d}`;
    }
  }
};
</script>

<style scoped>
.notification-center {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--space-6);
  background-color: var(--bg-page);
  min-height: 100%;
}

/* 页面头部 */
.page-header {
  margin-bottom: var(--space-4);
  border: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
}

.header-left {
  flex: 1;
}

.breadcrumb {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: 0;
}

.page-title i {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  gap: var(--space-3);
  flex-shrink: 0;
}

/* 筛选栏 */
.filter-bar {
  margin-bottom: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
}

.filter-bar:hover {
  transform: none;
}

.filter-bar .el-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.filter-bar .el-radio-button__inner {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
}

.filter-bar .el-radio-button__inner i {
  font-size: var(--font-size-xs);
}

.filter-count {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  margin-left: var(--space-1);
}

.filter-type {
  flex-shrink: 0;
}

/* 通知列表 */
.notification-list {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--space-12) var(--space-6);
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-base);
}

.empty-state i {
  font-size: 48px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-4);
}

.empty-state h3 {
  font-size: var(--font-size-lg);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2) 0;
}

.empty-state p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}

/* 通知卡片 */
.notification-item {
  display: flex;
  align-items: flex-start;
  padding: var(--space-4);
  cursor: pointer;
  position: relative;
  transition: all var(--transition-normal);
  border: 1px solid var(--border-base);
}

.notification-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.notification-item.is-unread {
  background: var(--primary-bg);
  border-color: var(--primary-light);
}

.notification-item.is-unread:hover {
  border-color: var(--primary-color);
}

.unread-dot {
  position: absolute;
  top: var(--space-5);
  left: var(--space-2);
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--primary-color);
  animation: pulse-dot 2s infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.notification-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--space-3);
  margin-left: var(--space-2);
  flex-shrink: 0;
  font-size: var(--font-size-lg);
}

.icon-system { background: var(--info-bg); color: var(--info-color); }
.icon-collect { background: var(--warning-bg); color: var(--warning-color); }
.icon-interaction { background: var(--success-bg); color: var(--success-color); }
.icon-note { background: rgba(114, 46, 209, 0.08); color: var(--info-dark); }
.icon-learning { background: var(--primary-bg); color: var(--primary-color); }
.icon-warning { background: var(--danger-bg); color: var(--danger-color); }
.icon-like { background: var(--danger-bg); color: var(--danger-color); }
.icon-comment { background: var(--success-bg); color: var(--success-color); }

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.notification-type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 500;
  flex-shrink: 0;
}

.tag-system { background: var(--info-bg); color: var(--info-color); }
.tag-collect { background: var(--warning-bg); color: var(--warning-color); }
.tag-interaction { background: var(--success-bg); color: var(--success-color); }
.tag-note { background: rgba(114, 46, 209, 0.08); color: var(--info-dark); }

.notification-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-message {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0 0 var(--space-2) 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.notification-time {
  font-size: var(--font-size-xs);
  color: var(--text-placeholder);
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
  margin-left: var(--space-3);
}

.action-btn {
  padding: var(--space-1) var(--space-2);
  font-size: var(--font-size-xs);
}

.delete-btn {
  color: var(--danger-color) !important;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* 动画 */
.notification-slide-enter-active {
  transition: all var(--transition-slow) ease;
}

.notification-slide-leave-active {
  transition: all var(--transition-normal) ease;
}

.notification-slide-enter {
  opacity: 0;
  transform: translateY(-10px);
}

.notification-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notification-center {
    padding: var(--space-4);
  }

  .header-content {
    flex-direction: column;
    gap: var(--space-3);
  }

  .header-right {
    width: 100%;
    justify-content: flex-end;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-type {
    width: 100%;
  }

  .notification-item {
    flex-wrap: wrap;
  }

  .notification-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: calc(44px + var(--space-3));
    margin-top: var(--space-2);
  }
}

@media (max-width: 576px) {
  .notification-center {
    padding: var(--space-3);
  }

  .page-title {
    font-size: var(--font-size-lg);
  }

  .notification-icon {
    width: 36px;
    height: 36px;
    font-size: var(--font-size-base);
  }

  .notification-actions {
    margin-left: calc(36px + var(--space-3));
  }
}
</style>
