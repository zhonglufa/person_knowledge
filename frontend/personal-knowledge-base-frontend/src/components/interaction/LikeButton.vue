<template>
  <button
    :class="['like-button', { 'is-liked': isLiked, 'is-disabled': isDisabled }]"
    :disabled="isDisabled"
    @click="handleLikeClick"
  >
    <i :class="['like-icon', isLiked ? 'fas fa-heart' : 'far fa-heart']"></i>
    <span v-if="showCount" class="like-count">{{ likeCount }}</span>
    <span v-if="showText" class="like-text">{{ isLiked ? '已赞' : '点赞' }}</span>
  </button>
</template>

<script>
import { likeContent, unlikeContent, getLikeCount, checkLikeStatus } from '@/api/interaction'

export default {
  name: 'LikeButton',

  props: {
    // 目标内容ID
    targetId: {
      type: [String, Number],
      required: true
    },

    // 目标内容类型 (如 'note', 'collection', 'item' 等)
    targetType: {
      type: String,
      required: true
    },

    // 初始点赞数
    initialCount: {
      type: Number,
      default: 0
    },

    // 初始是否已点赞
    initialLiked: {
      type: Boolean,
      default: false
    },

    // 是否显示点赞数
    showCount: {
      type: Boolean,
      default: true
    },

    // 是否显示文字
    showText: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      isLiked: this.initialLiked,
      likeCount: this.initialCount,
      isDisabled: false,
      // 防抖计时器
      debounceTimer: null,
      // 上次点击时间戳
      lastClickTime: 0,
      // 防抖间隔 (毫秒)
      DEBOUNCE_INTERVAL: 1000
    }
  },

  created() {
    // 如果提供了targetId但没有提供初始状态，则从后端获取
    if (this.targetId && (!this.initialCount || !this.initialLiked)) {
      this.fetchLikeStatus()
    }
  },

  beforeDestroy() {
    // 清理计时器
    if (this.debounceTimer) {
      clearTimeout(this.debounceTimer)
    }
  },

  methods: {
    /**
     * 获取点赞状态（点赞数和是否已点赞）
     */
    async fetchLikeStatus() {
      try {
        // 获取点赞数
        if (!this.initialCount) {
          const countResponse = await getLikeCount(this.targetId, this.targetType)
          if (countResponse.code === 200) {
            this.likeCount = countResponse.data || 0
          }
        }

        // 检查用户是否已点赞
        if (!this.initialLiked) {
          const checkResponse = await checkLikeStatus(this.targetId, this.targetType)
          if (checkResponse.code === 200) {
            this.isLiked = checkResponse.data || false
          }
        }
      } catch (error) {
        console.error('获取点赞状态失败:', error)
        // 静默失败，不影响显示
      }
    },

    /**
     * 处理点击事件（带防抖）
     */
    handleLikeClick() {
      // 防抖检查：如果距离上次点击不足1秒，则忽略
      const now = Date.now()
      if (now - this.lastClickTime < this.DEBOUNCE_INTERVAL) {
        return
      }

      this.lastClickTime = now
      this.toggleLike()
    },

    /**
     * 切换点赞状态
     */
    async toggleLike() {
      if (this.isDisabled) return

      this.isDisabled = true

      try {
        if (this.isLiked) {
          // 取消点赞
          await this.unlike()
        } else {
          // 点赞
          await this.like()
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        this.$message.error('操作失败，请稍后重试')
        // 恢复状态
        this.isDisabled = false
      }
    },

    /**
     * 执行点赞
     */
    async like() {
      // 乐观更新
      this.isLiked = true
      this.likeCount++

      const response = await likeContent(this.targetId, this.targetType)

      if (response.code === 200) {
        // 服务器确认成功
        this.$emit('like', this.targetId)
      } else {
        // 服务器返回失败，回滚
        this.isLiked = false
        this.likeCount--
        this.$message.error(response.message || '点赞失败')
      }

      this.isDisabled = false
    },

    /**
     * 执行取消点赞
     */
    async unlike() {
      // 乐观更新
      this.isLiked = false
      this.likeCount--

      const response = await unlikeContent(this.targetId, this.targetType)

      if (response.code === 200) {
        // 服务器确认成功
        this.$emit('unlike', this.targetId)
      } else {
        // 服务器返回失败，回滚
        this.isLiked = true
        this.likeCount++
        this.$message.error(response.message || '取消点赞失败')
      }

      this.isDisabled = false
    },

    /**
     * 重置点赞状态
     */
    reset() {
      this.isLiked = this.initialLiked
      this.likeCount = this.initialCount
      this.isDisabled = false
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
    }
  }
}
</script>

<style scoped>
/* ========== 点赞按钮 ========== */
.like-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--border-base);
  border-radius: var(--radius-full);
  background-color: var(--bg-container);
  color: var(--text-regular);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: var(--font-size-sm);
  outline: none;
}

.like-button:hover:not(.is-disabled) {
  border-color: #F56C6C;
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.05);
}

.like-button:active:not(.is-disabled) {
  transform: scale(0.95);
}

.like-button.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ========== 点赞图标 ========== */
.like-icon {
  font-size: 16px;
  transition: all 0.2s ease;
}

.like-button:hover:not(.is-disabled) .like-icon {
  transform: scale(1.1);
}

/* ========== 已点赞状态 ========== */
.like-button.is-liked {
  border-color: #F56C6C;
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.like-button.is-liked .like-icon {
  animation: heartBeat 0.4s ease;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  25% { transform: scale(1.3); }
  50% { transform: scale(1); }
  75% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* ========== 点赞数 ========== */
.like-count {
  font-weight: 600;
  min-width: 12px;
  text-align: center;
}

/* ========== 点赞文字 ========== */
.like-text {
  font-size: var(--font-size-sm);
}

/* ========== 响应式设计 ========== */
@media (max-width: 480px) {
  .like-button {
    padding: 4px 8px;
    gap: 4px;
  }

  .like-icon {
    font-size: 14px;
  }

  .like-count,
  .like-text {
    font-size: var(--font-size-xs);
  }
}
</style>
