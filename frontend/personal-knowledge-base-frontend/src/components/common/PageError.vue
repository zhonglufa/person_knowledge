<template>
  <div v-if="showError" class="page-error" :class="[`error-${size}`, { 'error-centered': centered }]">
    <div class="error-content">
      <!-- 错误图标 -->
      <div class="error-icon-wrapper" :style="{ fontSize: iconSize }">
        <i :class="iconClass" class="error-icon"></i>
      </div>
      
      <!-- 错误标题 -->
      <h3 class="error-title" v-if="title">{{ title }}</h3>
      
      <!-- 错误描述 -->
      <p class="error-description" v-if="description">{{ description }}</p>
      
      <!-- 错误详情（可选） -->
      <div v-if="details" class="error-details">
        <el-alert 
          :title="details" 
          type="error" 
          :closable="false"
          show-icon
        />
      </div>
      
      <!-- 操作按钮 -->
      <div class="error-actions" v-if="showRetry || showHome || $slots.default">
        <slot>
          <el-button 
            v-if="showRetry" 
            :type="retryButtonType" 
            :icon="retryButtonIcon"
            @click="$emit('retry')"
          >
            {{ retryButtonText }}
          </el-button>
          <el-button 
            v-if="showHome" 
            :type="homeButtonType"
            :icon="homeButtonIcon"
            @click="$emit('go-home')"
          >
            {{ homeButtonText }}
          </el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PageError',
  props: {
    // 是否显示错误
    showError: {
      type: Boolean,
      default: true
    },
    // 尺寸：small / medium / large
    size: {
      type: String,
      default: 'medium',
      validator: (val) => ['small', 'medium', 'large'].includes(val)
    },
    // 是否居中显示
    centered: {
      type: Boolean,
      default: true
    },
    // 错误图标类型
    icon: {
      type: String,
      default: 'error', // error, warning, connection, server, not-found
      validator: (val) => ['error', 'warning', 'connection', 'server', 'not-found'].includes(val)
    },
    // 错误标题
    title: {
      type: String,
      default: '加载失败'
    },
    // 错误描述
    description: {
      type: String,
      default: '抱歉，加载数据时出现问题'
    },
    // 错误详情
    details: {
      type: String,
      default: ''
    },
    // 是否显示重试按钮
    showRetry: {
      type: Boolean,
      default: true
    },
    // 重试按钮文字
    retryButtonText: {
      type: String,
      default: '重新加载'
    },
    // 重试按钮类型
    retryButtonType: {
      type: String,
      default: 'primary'
    },
    // 重试按钮图标
    retryButtonIcon: {
      type: String,
      default: 'el-icon-refresh'
    },
    // 是否显示返回首页按钮
    showHome: {
      type: Boolean,
      default: false
    },
    // 首页按钮文字
    homeButtonText: {
      type: String,
      default: '返回首页'
    },
    // 首页按钮类型
    homeButtonType: {
      type: String,
      default: 'default'
    },
    // 首页按钮图标
    homeButtonIcon: {
      type: String,
      default: 'el-icon-house'
    }
  },
  emits: ['retry', 'go-home'],
  computed: {
    // 图标大小
    iconSize() {
      const sizeMap = {
        small: '48px',
        medium: '64px',
        large: '80px'
      }
      return sizeMap[this.size] || '64px'
    },
    // 图标类名
    iconClass() {
      const iconMap = {
        'error': 'fas fa-exclamation-circle',
        'warning': 'fas fa-exclamation-triangle',
        'connection': 'fas fa-wifi',
        'server': 'fas fa-server',
        'not-found': 'fas fa-file-excel'
      }
      return iconMap[this.icon] || 'fas fa-exclamation-circle'
    }
  }
}
</script>

<style scoped>
.page-error {
  width: 100%;
  padding: var(--space-6);
}

.error-centered {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.error-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.error-icon-wrapper {
  margin-bottom: var(--space-4);
  color: var(--danger-color, #ff6b6b);
}

.error-icon {
  animation: error-shake 0.5s ease-in-out;
}

.error-title {
  font-size: var(--font-size-xl, 20px);
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: var(--space-2);
}

.error-description {
  font-size: var(--font-size-base, 14px);
  color: var(--text-secondary, #666);
  margin-bottom: var(--space-4);
  line-height: 1.6;
}

.error-details {
  margin-bottom: var(--space-4);
  text-align: left;
}

.error-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  flex-wrap: wrap;
}

/* 尺寸变体 */
.error-small .error-title {
  font-size: var(--font-size-lg, 18px);
}

.error-small .error-description {
  font-size: var(--font-size-sm, 13px);
}

.error-large .error-title {
  font-size: var(--font-size-2xl, 24px);
}

.error-large .error-description {
  font-size: var(--font-size-lg, 16px);
}

/* 错误图标动画 */
@keyframes error-shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .error-icon {
    color: #ff7875;
  }
  
  .error-title {
    color: #e0e0e0;
  }
  
  .error-description {
    color: #b0b0b0;
  }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .page-error {
    padding: var(--space-4);
  }
  
  .error-actions {
    flex-direction: column;
  }
  
  .error-actions .el-button {
    width: 100%;
  }
}
</style>
