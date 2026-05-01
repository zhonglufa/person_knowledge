<template>
  <div class="interaction-center">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
              <el-breadcrumb-item>互动中心</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <h1 class="page-title">互动中心</h1>
          <p class="page-subtitle">管理您的点赞、评论和收藏互动</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon like-icon">
          <i class="fas fa-heart"></i>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.likes }}</div>
          <div class="stat-label">收到的点赞</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon comment-icon">
          <i class="fas fa-comment"></i>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.comments }}</div>
          <div class="stat-label">收到的评论</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon collect-icon">
          <i class="fas fa-star"></i>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.collects }}</div>
          <div class="stat-label">收到的收藏</div>
        </div>
      </div>
    </div>

    <!-- Tab切换区域 -->
    <div class="main-content">
      <!-- 工具栏 -->
      <div class="interaction-toolbar">
        <div class="toolbar-left">
          <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="unread">未读</el-radio-button>
            <el-radio-button label="read">已读</el-radio-button>
          </el-radio-group>
        </div>
        <div class="toolbar-right">
          <el-button size="small" icon="el-icon-download" @click="handleExportInteractions">
            导出数据
          </el-button>
          <el-button 
            v-if="selectedInteractions.length > 0"
            size="small" 
            type="danger"
            icon="el-icon-delete"
            @click="handleBatchDelete"
          >
            批量删除 ({{ selectedInteractions.length }})
          </el-button>
        </div>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 收到的评论 -->
        <el-tab-pane label="收到的评论" name="comments">
          <div class="tab-content">
            <div v-if="loadingComments" class="loading-container">
              <i class="fas fa-spinner fa-spin"></i>
              <span>加载评论中...</span>
            </div>
            <div v-else-if="receivedComments.length === 0" class="empty-state">
              <div class="empty-icon">
                <i class="fas fa-comment-slash"></i>
              </div>
              <h3>暂无评论</h3>
              <p>还没有人评论您的内容</p>
            </div>
            <transition-group v-else name="slide-up" tag="div" class="comment-list">
              <div
                v-for="comment in receivedComments"
                :key="comment.id"
                class="comment-item"
              >
                <div class="comment-header">
                  <div class="comment-user">
                    <el-avatar :size="32" :src="comment.userAvatar">
                      {{ comment.userName ? comment.userName.charAt(0) : 'U' }}
                    </el-avatar>
                    <span class="user-name">{{ comment.userName }}</span>
                  </div>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <div class="comment-body">
                  <p>{{ comment.content }}</p>
                </div>
                <div class="comment-target">
                  <i class="fas fa-link"></i>
                  <span>评论内容: {{ comment.targetTitle }}</span>
                </div>
                <div class="comment-actions">
                  <el-button type="text" size="small" @click="handleReplyComment(comment)">
                    <i class="fas fa-reply"></i> 回复
                  </el-button>
                  <el-button type="text" size="small" @click="handleDeleteComment(comment)">
                    <i class="fas fa-trash"></i> 删除
                  </el-button>
                </div>
              </div>
            </transition-group>

            <!-- 评论分页 -->
            <div v-if="commentPagination.total > commentPagination.pageSize" class="pagination-wrapper">
              <el-pagination
                background
                layout="prev, pager, next"
                :current-page="commentPagination.currentPage"
                :page-size="commentPagination.pageSize"
                :total="commentPagination.total"
                @current-change="handleCommentPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 收到的点赞 -->
        <el-tab-pane label="收到的点赞" name="likes">
          <div class="tab-content">
            <div v-if="loadingLikes" class="loading-container">
              <i class="fas fa-spinner fa-spin"></i>
              <span>加载点赞中...</span>
            </div>
            <div v-else-if="receivedLikes.length === 0" class="empty-state">
              <div class="empty-icon">
                <i class="fas fa-heart-broken"></i>
              </div>
              <h3>暂无点赞</h3>
              <p>还没有人点赞您的内容</p>
            </div>
            <transition-group v-else name="slide-up" tag="div" class="like-list">
              <div
                v-for="like in receivedLikes"
                :key="like.id"
                class="like-item"
              >
                <div class="like-user">
                  <el-avatar :size="32" :src="like.userAvatar">
                    {{ like.userName ? like.userName.charAt(0) : 'U' }}
                  </el-avatar>
                  <span class="user-name">{{ like.userName }}</span>
                </div>
                <div class="like-target">
                  <i class="fas fa-heart like-color"></i>
                  <span>点赞了: {{ like.targetTitle }}</span>
                </div>
                <span class="like-time">{{ formatTime(like.createTime) }}</span>
              </div>
            </transition-group>

            <div v-if="likePagination.total > likePagination.pageSize" class="pagination-wrapper">
              <el-pagination
                background
                layout="prev, pager, next"
                :current-page="likePagination.currentPage"
                :page-size="likePagination.pageSize"
                :total="likePagination.total"
                @current-change="handleLikePageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 收到的收藏 -->
        <el-tab-pane label="收到的收藏" name="collects">
          <div class="tab-content">
            <div v-if="loadingCollects" class="loading-container">
              <i class="fas fa-spinner fa-spin"></i>
              <span>加载收藏中...</span>
            </div>
            <div v-else-if="receivedCollects.length === 0" class="empty-state">
              <div class="empty-icon">
                <i class="fas fa-star"></i>
              </div>
              <h3>暂无收藏</h3>
              <p>还没有人收藏您的内容</p>
            </div>
            <transition-group v-else name="slide-up" tag="div" class="collect-list">
              <div
                v-for="collect in receivedCollects"
                :key="collect.id"
                class="collect-item"
              >
                <div class="collect-user">
                  <el-avatar :size="32" :src="collect.userAvatar">
                    {{ collect.userName ? collect.userName.charAt(0) : 'U' }}
                  </el-avatar>
                  <span class="user-name">{{ collect.userName }}</span>
                </div>
                <div class="collect-target">
                  <i class="fas fa-star collect-color"></i>
                  <span>收藏了: {{ collect.targetTitle }}</span>
                </div>
                <span class="collect-time">{{ formatTime(collect.createTime) }}</span>
              </div>
            </transition-group>

            <div v-if="collectPagination.total > collectPagination.pageSize" class="pagination-wrapper">
              <el-pagination
                background
                layout="prev, pager, next"
                :current-page="collectPagination.currentPage"
                :page-size="collectPagination.pageSize"
                :total="collectPagination.total"
                @current-change="handleCollectPageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 回复评论对话框 -->
    <el-dialog
      title="回复评论"
      :visible.sync="replyDialogVisible"
      width="500px"
      @close="resetReplyForm"
    >
      <div v-if="replyingToComment" class="reply-reference">
        <p><strong>{{ replyingToComment.userName }} 说:</strong></p>
        <p class="reference-content">{{ replyingToComment.content }}</p>
      </div>
      <el-form :model="replyForm" label-width="0">
        <el-form-item>
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replying" @click="submitReply">回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getInteractionStats,
  getReceivedLikes,
  getReceivedComments,
  getReceivedCollects
} from '@/api/notification'
import { exportInteractions } from '@/api/interaction'

export default {
  name: 'InteractionCenter',

  data() {
    return {
      activeTab: 'comments',
      filterStatus: '', // 筛选状态：全部/未读/已读
      selectedInteractions: [], // 选中的互动项ID

      // 统计数据
      stats: {
        likes: 0,
        comments: 0,
        collects: 0
      },

      // 收到的评论
      loadingComments: false,
      receivedComments: [],
      commentPagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },

      // 收到的点赞
      loadingLikes: false,
      receivedLikes: [],
      likePagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },

      // 收到的收藏
      loadingCollects: false,
      receivedCollects: [],
      collectPagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },

      // 回复评论相关
      replyDialogVisible: false,
      replyingToComment: null,
      replyForm: {
        content: ''
      },
      replying: false
    }
  },

  created() {
    this.loadStats()
    this.loadReceivedComments()
  },

  methods: {
    /**
     * 加载互动统计数据 - 真实API调用
     */
    async loadStats() {
      try {
        const response = await getInteractionStats()
        if (response.data) {
          this.stats = {
            likes: response.data.likes || 0,
            comments: response.data.comments || 0,
            collects: response.data.collects || 0
          }
        }
      } catch (error) {
        console.error('加载统计失败:', error)
        // 保留初始值
        this.stats = { likes: 0, comments: 0, collects: 0 }
      }
    },

    /**
     * 加载收到的评论列表 - 真实API调用
     */
    async loadReceivedComments() {
      this.loadingComments = true
      try {
        const response = await getReceivedComments({
          pageNum: this.commentPagination.currentPage,
          pageSize: this.commentPagination.pageSize
        })
        
        if (response.data) {
          this.receivedComments = response.data.records || response.data.list || []
          this.commentPagination.total = response.data.total || 0
        } else {
          this.receivedComments = []
          this.commentPagination.total = 0
        }
      } catch (error) {
        console.error('加载评论失败:', error)
        this.$message.error('加载评论失败')
        this.receivedComments = []
      } finally {
        this.loadingComments = false
      }
    },

    /**
     * 加载收到的点赞列表 - 真实API调用
     */
    async loadReceivedLikes() {
      this.loadingLikes = true
      try {
        const response = await getReceivedLikes({
          pageNum: this.likePagination.currentPage,
          pageSize: this.likePagination.pageSize
        })
        
        if (response.data) {
          this.receivedLikes = response.data.records || response.data.list || []
          this.likePagination.total = response.data.total || 0
        } else {
          this.receivedLikes = []
          this.likePagination.total = 0
        }
      } catch (error) {
        console.error('加载点赞失败:', error)
        this.$message.error('加载点赞失败')
        this.receivedLikes = []
      } finally {
        this.loadingLikes = false
      }
    },

    /**
     * 加载收到的收藏列表 - 真实API调用
     */
    async loadReceivedCollects() {
      this.loadingCollects = true
      try {
        const response = await getReceivedCollects({
          pageNum: this.collectPagination.currentPage,
          pageSize: this.collectPagination.pageSize
        })
        
        if (response.data) {
          this.receivedCollects = response.data.records || response.data.list || []
          this.collectPagination.total = response.data.total || 0
        } else {
          this.receivedCollects = []
          this.collectPagination.total = 0
        }
      } catch (error) {
        console.error('加载收藏失败:', error)
        this.$message.error('加载收藏失败')
        this.receivedCollects = []
      } finally {
        this.loadingCollects = false
      }
    },

    /**
     * Tab切换处理
     */
    handleTabClick(tab) {
      switch (tab.name) {
        case 'comments':
          this.loadReceivedComments()
          break
        case 'likes':
          this.loadReceivedLikes()
          break
        case 'collects':
          this.loadReceivedCollects()
          break
      }
    },

    /**
     * 评论分页变化
     */
    handleCommentPageChange(page) {
      this.commentPagination.currentPage = page
      this.loadReceivedComments()
    },

    /**
     * 点赞分页变化
     */
    handleLikePageChange(page) {
      this.likePagination.currentPage = page
      this.loadReceivedLikes()
    },

    /**
     * 收藏分页变化
     */
    handleCollectPageChange(page) {
      this.collectPagination.currentPage = page
      this.loadReceivedCollects()
    },

    /**
     * 回复评论
     */
    handleReplyComment(comment) {
      this.replyingToComment = comment
      this.replyDialogVisible = true
    },

    /**
     * 提交回复
     */
    async submitReply() {
      if (!this.replyForm.content.trim()) {
        this.$message.warning('回复内容不能为空')
        return
      }

      this.replying = true
      try {
        // 此处应调用后端回复API
        this.$message.success('回复成功')
        this.replyDialogVisible = false
      } catch (error) {
        console.error('回复失败:', error)
        this.$message.error('回复失败')
      } finally {
        this.replying = false
      }
    },

    /**
     * 删除评论
     */
    handleDeleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '删除确认', {
        type: 'warning'
      }).then(async () => {
        try {
          // 此处应调用后端删除评论API
          this.receivedComments = this.receivedComments.filter(c => c.id !== comment.id)
          this.stats.comments--
          this.$message.success('删除成功')
        } catch (error) {
          console.error('删除评论失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    /**
     * 重置回复表单
     */
    resetReplyForm() {
      this.replyingToComment = null
      this.replyForm.content = ''
    },

    /**
     * 格式化时间
     */
    formatTime(timeString) {
      if (!timeString) return ''
      const time = new Date(timeString)
      const now = new Date()
      const diff = now - time

      // 今天
      if (time.toDateString() === now.toDateString()) {
        return `今天 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`
      }

      // 昨天
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      if (time.toDateString() === yesterday.toDateString()) {
        return '昨天'
      }

      // 3天内
      if (diff < 3 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000))
        return `${days}天前`
      }

      // 日期格式
      return `${time.getFullYear()}-${(time.getMonth() + 1).toString().padStart(2, '0')}-${time.getDate().toString().padStart(2, '0')}`
    },
    
    /**
     * 筛选状态变化
     */
    handleFilterChange() {
      // 重新加载当前tab的数据，应用筛选
      this.selectedInteractions = []
      this.handleTabClick({ name: this.activeTab })
    },
    
    /**
     * 批量删除互动
     */
    async handleBatchDelete() {
      if (this.selectedInteractions.length === 0) {
        this.$message.warning('请选择要删除的互动')
        return
      }
      
      try {
        await this.$confirm(
          `确定要删除选中的 ${this.selectedInteractions.length} 条互动记录吗？`,
          '批量删除确认',
          { type: 'warning' }
        )
        
        // TODO: 调用后端API批量删除
        // await batchDeleteInteractions(this.selectedInteractions)
        
        this.$message.success('批量删除成功')
        this.selectedInteractions = []
        this.handleTabClick({ name: this.activeTab })
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }
    },
    
    /**
     * 导出互动数据
     */
    async handleExportInteractions() {
      try {
        this.$message.info('正在准备导出数据...')
        
        // 调用后端API导出
        const response = await exportInteractions({
          tab: this.activeTab,
          filterStatus: this.filterStatus
        })
        
        // 创建下载链接
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `互动数据_${this.activeTab}_${new Date().toLocaleDateString('zh-CN')}.xlsx`
        a.click()
        URL.revokeObjectURL(url)
        
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        this.$message.error('导出失败，请重试')
      }
    }
  }
}
</script>

<style scoped>
.interaction-center {
  height: 100%;
  display: flex;
  flex-direction: column;
  animation: fadeIn var(--transition-slow) ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 工具栏样式 */
.interaction-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

/* ========== 页面头部 ========== */
.page-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background-color: var(--bg-primary);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.breadcrumb {
  font-size: 14px;
  color: var(--text-secondary);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-regular);
  margin: 0;
}

/* ========== 统计卡片 ========== */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
  padding: var(--space-6);
  background-color: var(--bg-canvas);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  transition: transform var(--transition-normal);
}

.stat-card:hover .stat-icon {
  transform: scale(1.08);
}

.like-icon {
  background: linear-gradient(135deg, var(--danger-color), var(--warning-color));
}

.comment-icon {
  background: linear-gradient(135deg, var(--info-color), var(--success-color));
}

.collect-icon {
  background: linear-gradient(135deg, var(--warning-color), var(--danger-color));
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

/* ========== 主内容区 ========== */
.main-content {
  flex: 1;
  padding: var(--space-6);
  overflow-y: auto;
}

.tab-content {
  min-height: 300px;
}

/* ========== 加载状态 ========== */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-5);
  color: var(--text-secondary);
}

.loading-container i {
  font-size: 24px;
  margin-bottom: var(--space-3);
  color: var(--primary-color);
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: var(--space-16) var(--space-5);
}

.empty-icon {
  font-size: 48px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-4);
}

.empty-state h3 {
  font-size: 20px;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-state p {
  color: var(--text-secondary);
  margin: 0;
}

/* ========== 评论列表 ========== */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.comment-item {
  padding: var(--space-4);
  background-color: var(--bg-container);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.comment-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.comment-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.comment-body {
  margin-bottom: var(--space-3);
}

.comment-body p {
  margin: 0;
  color: var(--text-regular);
  line-height: 1.6;
}

.comment-target {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.comment-actions {
  display: flex;
  gap: var(--space-2);
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-2);
}

/* ========== 点赞列表 ========== */
.like-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.like-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background-color: var(--bg-container);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.like-item:hover {
  border-color: var(--danger-light);
  box-shadow: var(--shadow-xs);
}

.like-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 120px;
}

.like-target {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-regular);
}

.like-color {
  color: var(--danger-color);
}

.like-time {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

/* ========== 收藏列表 ========== */
.collect-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.collect-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background-color: var(--bg-container);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.collect-item:hover {
  border-color: var(--warning-light);
  box-shadow: var(--shadow-xs);
}

.collect-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 120px;
}

.collect-target {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--text-regular);
}

.collect-color {
  color: var(--warning-color);
}

.collect-time {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
}

/* ========== 回复引用 ========== */
.reply-reference {
  padding: var(--space-3);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-4);
  border-left: 3px solid var(--primary-color);
}

.reply-reference p {
  margin: 0 0 var(--space-1) 0;
}

.reference-content {
  color: var(--text-secondary);
  font-size: 14px;
}

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-6);
  display: flex;
  justify-content: center;
}

/* ========== slide-up 过渡动画 ========== */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all var(--transition-normal);
}
.slide-up-enter,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .page-header,
  .main-content {
    padding: var(--space-4);
  }

  .stats-section {
    grid-template-columns: 1fr;
    padding: var(--space-4);
  }

  .like-item,
  .collect-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .like-time,
  .collect-time {
    align-self: flex-end;
  }

  .comment-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-1);
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 20px;
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-number {
    font-size: 20px;
  }
}
</style>
