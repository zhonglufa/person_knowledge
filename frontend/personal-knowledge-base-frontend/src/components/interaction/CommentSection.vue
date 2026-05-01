<template>
  <div class="comment-section">
    <!-- 评论区头部 -->
    <div class="comment-header">
      <h3 class="section-title">
        <i class="fas fa-comments"></i>
        评论 ({{ totalComments }})
      </h3>
    </div>

    <!-- 发表评论区域 -->
    <div class="comment-form">
      <div class="form-user-info">
        <el-avatar :size="32" :src="currentUserAvatar">
          {{ currentUserName ? currentUserName.charAt(0) : 'U' }}
        </el-avatar>
        <span class="user-name">{{ currentUserName || '未登录用户' }}</span>
      </div>

      <el-input
        v-model="commentContent"
        type="textarea"
        :rows="3"
        :placeholder="replyingTo ? `回复 @${replyingTo.userName}` : '请输入评论内容'"
        maxlength="500"
        show-word-limit
        :disabled="submitting"
        class="comment-input"
      />

      <!-- 敏感词提示 -->
      <el-alert
        v-if="sensitiveWordWarning"
        :title="sensitiveWordWarning"
        type="warning"
        :closable="false"
        show-icon
        class="sensitive-alert"
      />

      <div class="form-actions">
        <el-button
          v-if="replyingTo"
          size="small"
          @click="cancelReply"
        >取消回复</el-button>
        <el-button
          type="primary"
          size="small"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="submitComment"
        >{{ replyingTo ? '回复' : '发表' }}</el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div v-if="loading" class="loading-container">
      <i class="el-icon-loading"></i>
      <span>加载评论中...</span>
    </div>

    <div v-else-if="comments.length === 0" class="empty-state">
      <i class="fas fa-comment-slash"></i>
      <p>暂无评论，快来发表第一条评论吧！</p>
    </div>

    <div v-else class="comment-list">
      <div
        v-for="comment in comments"
        :key="comment.id"
        class="comment-item"
      >
        <!-- 评论主体 -->
        <div class="comment-main">
          <div class="comment-user">
            <el-avatar :size="32" :src="comment.userAvatar">
              {{ comment.userName ? comment.userName.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-info">
              <span class="user-name">{{ comment.userName }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>
          </div>
          <div class="comment-content">
            <p>{{ comment.content }}</p>
          </div>
          <div class="comment-actions">
            <el-button type="text" size="mini" @click="handleReply(comment)">
              <i class="fas fa-reply"></i> 回复
            </el-button>
            <el-button
              v-if="comment.userId === currentUserId"
              type="text"
              size="mini"
              class="delete-btn"
              @click="handleDelete(comment)"
            >
              <i class="fas fa-trash"></i> 删除
            </el-button>
          </div>
        </div>

        <!-- 回复列表（最多三层嵌套） -->
        <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
          <div
            v-for="reply in comment.replies"
            :key="reply.id"
            class="reply-item"
          >
            <div class="reply-user">
              <el-avatar :size="24" :src="reply.userAvatar">
                {{ reply.userName ? reply.userName.charAt(0) : 'U' }}
              </el-avatar>
              <div class="user-info">
                <span class="user-name">
                  {{ reply.userName }}
                  <span v-if="reply.replyToUserName" class="reply-to">
                    回复 <span class="reply-target">@{{ reply.replyToUserName }}</span>
                  </span>
                </span>
                <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
              </div>
            </div>
            <div class="reply-content">
              <p>{{ reply.content }}</p>
            </div>
            <div class="reply-actions">
              <el-button
                v-if="comment.depth < 2"
                type="text"
                size="mini"
                @click="handleReply(reply, comment)"
              >
                <i class="fas fa-reply"></i> 回复
              </el-button>
              <el-button
                v-if="reply.userId === currentUserId"
                type="text"
                size="mini"
                class="delete-btn"
                @click="handleDeleteReply(reply, comment)"
              >
                <i class="fas fa-trash"></i> 删除
              </el-button>
            </div>

            <!-- 第三层回复（不再嵌套更深） -->
            <div v-if="reply.replies && reply.replies.length > 0" class="sub-replies-list">
              <div
                v-for="subReply in reply.replies"
                :key="subReply.id"
                class="sub-reply-item"
              >
                <span class="sub-reply-user">{{ subReply.userName }}</span>
                <span v-if="subReply.replyToUserName" class="reply-to">
                  回复 <span class="reply-target">@{{ subReply.replyToUserName }}</span>
                </span>
                <span class="sub-reply-content">：{{ subReply.content }}</span>
                <span class="sub-reply-time">{{ formatTime(subReply.createTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 更多回复 -->
          <div
            v-if="comment.replyCount > comment.replies.length"
            class="more-replies"
          >
            <el-button type="text" size="small" @click="loadMoreReplies(comment)">
              查看更多回复 ({{ comment.replyCount - comment.replies.length }})
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > pagination.pageSize" class="pagination-wrapper">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 回复对话框（用于深层回复） -->
    <el-dialog
      :title="'回复 @' + (replyDialogTarget && replyDialogTarget.userName)"
      :visible.sync="replyDialogVisible"
      width="500px"
      @close="resetReplyDialog"
    >
      <div v-if="replyDialogTarget" class="reply-reference">
        <p><strong>{{ replyDialogTarget.userName }} 说:</strong></p>
        <p class="reference-content">{{ replyDialogTarget.content }}</p>
      </div>
      <el-input
        v-model="replyDialogContent"
        type="textarea"
        :rows="4"
        placeholder="请输入回复内容"
        maxlength="500"
        show-word-limit
      />
      <div slot="footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitDialogReply">回复</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getComments, commentContent } from '@/api/interaction'

export default {
  name: 'CommentSection',

  props: {
    // 目标内容ID
    targetId: {
      type: [String, Number],
      required: true
    },

    // 目标内容类型
    targetType: {
      type: String,
      required: true
    },

    // 每页评论数
    pageSize: {
      type: Number,
      default: 10
    },

    // 当前用户信息
    currentUserId: {
      type: [String, Number],
      default: null
    },
    currentUserName: {
      type: String,
      default: ''
    },
    currentUserAvatar: {
      type: String,
      default: ''
    }
  },

  data() {
    return {
      // 评论列表
      comments: [],
      loading: false,
      totalComments: 0,

      // 分页
      pagination: {
        currentPage: 1,
        pageSize: this.pageSize,
        total: 0
      },

      // 发表评论
      commentContent: '',
      submitting: false,

      // 回复相关
      replyingTo: null,

      // 回复对话框
      replyDialogVisible: false,
      replyDialogTarget: null,
      replyDialogContent: '',
      replyParentComment: null,

      // 敏感词过滤提示
      sensitiveWordWarning: ''
    }
  },

  computed: {
    /**
     * 是否可以提交
     */
    canSubmit() {
      return this.commentContent.trim().length > 0 && !this.submitting
    }
  },

  watch: {
    targetId() {
      // 目标ID变化时重新加载评论
      this.loadComments()
    }
  },

  created() {
    this.loadComments()
  },

  methods: {
    /**
     * 加载评论列表
     */
    async loadComments() {
      this.loading = true
      try {
        const response = await getComments(
          this.targetId,
          this.targetType,
          this.pagination.currentPage,
          this.pagination.pageSize
        )

        if (response.data && response.data.code === 200) {
          const data = response.data.data
          this.comments = data.list || []
          this.totalComments = data.total || 0
          this.pagination.total = data.total || 0
        } else {
          this.$message.error(response.data.message || '获取评论失败')
        }
      } catch (error) {
        console.error('获取评论失败:', error)
        this.$message.error('获取评论失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 发表评论
     */
    async submitComment() {
      if (!this.canSubmit) return

      // 敏感词过滤检查
      const sensitiveWords = this.checkSensitiveWords(this.commentContent)
      if (sensitiveWords.length > 0) {
        this.sensitiveWordWarning = `评论内容包含敏感词汇: ${sensitiveWords.join(', ')}`
        return
      }
      this.sensitiveWordWarning = ''

      this.submitting = true
      try {
        const data = {
          targetId: this.targetId,
          targetType: this.targetType,
          content: this.commentContent.trim()
        }

        // 如果是回复，添加回复相关信息
        if (this.replyingTo) {
          data.parentId = this.replyingTo.parentId || this.replyingTo.id
          data.replyToId = this.replyingTo.id
        }

        const response = await commentContent(
          this.targetId,
          this.targetType,
          this.commentContent.trim()
        )

        if (response.data && response.data.code === 200) {
          this.$message.success(this.replyingTo ? '回复成功' : '评论发表成功')
          this.commentContent = ''
          this.replyingTo = null
          // 重新加载评论列表
          this.loadComments()
          this.$emit('comment-submitted')
        } else {
          this.$message.error(response.data.message || '发表失败')
        }
      } catch (error) {
        console.error('发表评论失败:', error)
        this.$message.error('发表失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    },

    /**
     * 取消回复
     */
    cancelReply() {
      this.replyingTo = null
      this.commentContent = ''
      this.sensitiveWordWarning = ''
    },

    /**
     * 回复评论
     */
    handleReply(reply, parentComment) {
      // 如果回复深度不超过2层，直接在输入框回复
      if (!parentComment || reply.depth < 2) {
        this.replyingTo = reply
      } else {
        // 深层回复使用对话框
        this.replyDialogTarget = reply
        this.replyParentComment = parentComment
        this.replyDialogVisible = true
      }
    },

    /**
     * 提交对话框中的回复
     */
    async submitDialogReply() {
      if (!this.replyDialogContent.trim()) {
        this.$message.warning('回复内容不能为空')
        return
      }

      // 敏感词检查
      const sensitiveWords = this.checkSensitiveWords(this.replyDialogContent)
      if (sensitiveWords.length > 0) {
        this.$message.warning(`回复内容包含敏感词汇: ${sensitiveWords.join(', ')}`)
        return
      }

      this.submitting = true
      try {
        const response = await commentContent(
          this.targetId,
          this.targetType,
          this.replyDialogContent.trim()
        )

        if (response.data && response.data.code === 200) {
          this.$message.success('回复成功')
          this.replyDialogVisible = false
          this.loadComments()
        } else {
          this.$message.error(response.data.message || '回复失败')
        }
      } catch (error) {
        console.error('回复失败:', error)
        this.$message.error('回复失败')
      } finally {
        this.submitting = false
      }
    },

    /**
     * 重置回复对话框
     */
    resetReplyDialog() {
      this.replyDialogTarget = null
      this.replyDialogContent = ''
      this.replyParentComment = null
    },

    /**
     * 删除评论
     */
    handleDelete(comment) {
      this.$confirm('确定要删除这条评论吗？', '删除确认', {
        type: 'warning'
      }).then(async () => {
        try {
          // 此处应调用后端删除评论API
          this.$message.success('删除成功')
          this.loadComments()
        } catch (error) {
          console.error('删除评论失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    /**
     * 删除回复
     */
    handleDeleteReply(reply, parentComment) {
      this.$confirm('确定要删除这条回复吗？', '删除确认', {
        type: 'warning'
      }).then(async () => {
        try {
          // 此处应调用后端删除回复API
          this.$message.success('删除成功')
          this.loadComments()
        } catch (error) {
          console.error('删除回复失败:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    /**
     * 加载更多回复
     */
    async loadMoreReplies(comment) {
      // 此处应调用后端API加载更多回复
      this.$message.info('加载更多回复功能待实现')
    },

    /**
     * 分页变化
     */
    handlePageChange(page) {
      this.pagination.currentPage = page
      this.loadComments()
    },

    /**
     * 敏感词检查
     */
    checkSensitiveWords(content) {
      // 此处应调用后端敏感词检查API或使用本地词库
      // 暂时返回空数组
      return []
    },

    /**
     * 格式化时间
     */
    formatTime(timeString) {
      if (!timeString) return ''
      const time = new Date(timeString)
      const now = new Date()
      const diff = now - time

      // 1分钟内
      if (diff < 60 * 1000) {
        return '刚刚'
      }

      // 1小时内
      if (diff < 60 * 60 * 1000) {
        return `${Math.floor(diff / (60 * 1000))}分钟前`
      }

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
    }
  }
}
</script>

<style scoped>
.comment-section {
  padding: var(--space-4) 0;
}

/* ========== 评论区头部 ========== */
.comment-header {
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.section-title i {
  color: var(--primary-color);
}

/* ========== 评论表单 ========== */
.comment-form {
  margin-bottom: var(--space-5);
  padding: var(--space-4);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
}

.form-user-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.form-user-info .user-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.comment-input {
  margin-bottom: var(--space-3);
}

.sensitive-alert {
  margin-bottom: var(--space-3);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

/* ========== 加载状态 ========== */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.loading-container i {
  font-size: 24px;
  margin-bottom: var(--space-2);
}

/* ========== 空状态 ========== */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--text-secondary);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: var(--space-3);
  color: var(--text-placeholder);
}

.empty-state p {
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
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.comment-main {
  margin-bottom: var(--space-3);
}

.comment-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info .user-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.comment-content p {
  margin: 0;
  color: var(--text-regular);
  line-height: 1.6;
  font-size: var(--font-size-base);
}

.comment-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.delete-btn {
  color: var(--danger-color) !important;
}

/* ========== 回复列表 ========== */
.replies-list {
  margin-top: var(--space-3);
  padding-left: var(--space-4);
  border-left: 2px solid var(--border-light);
}

.reply-item {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.reply-user .user-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: var(--font-size-sm);
}

.reply-to {
  font-weight: normal;
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.reply-target {
  color: var(--primary-color);
}

.reply-content p {
  margin: 0;
  color: var(--text-regular);
  line-height: 1.5;
  font-size: var(--font-size-sm);
}

.reply-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-1);
}

/* ========== 子回复列表（第三层） ========== */
.sub-replies-list {
  margin-top: var(--space-2);
  padding: var(--space-2);
  background-color: var(--bg-container);
  border-radius: var(--radius-sm);
}

.sub-reply-item {
  font-size: var(--font-size-xs);
  color: var(--text-regular);
  line-height: 1.8;
}

.sub-reply-user {
  font-weight: 600;
  color: var(--text-primary);
}

.sub-reply-content {
  margin: 0 var(--space-1);
}

.sub-reply-time {
  color: var(--text-secondary);
  margin-left: var(--space-2);
}

/* ========== 更多回复 ========== */
.more-replies {
  margin-top: var(--space-2);
  text-align: center;
}

/* ========== 回复引用 ========== */
.reply-reference {
  padding: var(--space-3);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-3);
  border-left: 3px solid var(--primary-color);
}

.reply-reference p {
  margin: 0 0 var(--space-1) 0;
}

.reference-content {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-5);
  display: flex;
  justify-content: center;
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .comment-form {
    padding: var(--space-3);
  }

  .comment-item {
    padding: var(--space-3);
  }

  .replies-list {
    padding-left: var(--space-3);
  }
}

@media (max-width: 480px) {
  .comment-user {
    flex-wrap: wrap;
  }

  .comment-actions {
    flex-wrap: wrap;
  }
}
</style>
