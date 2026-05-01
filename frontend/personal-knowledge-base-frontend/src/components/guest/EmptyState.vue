<template>
  <div class="empty-state" :class="{ 'loading': loading }">
    <template v-if="loading">
      <div class="skeleton-container">
        <div class="skeleton-icon"></div>
        <div class="skeleton-text"></div>
      </div>
    </template>

    <template v-else>
      <slot name="icon">
        <i class="el-icon-folder-opened empty-icon"></i>
      </slot>

      <h3 class="empty-title">
        <slot name="title">{{ title }}</slot>
      </h3>

      <p class="empty-message">
        <slot name="message">{{ message }}</slot>
      </p>

      <slot name="action"></slot>
    </template>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    loading: Boolean,
    title: {
      type: String,
      default: '暂无内容'
    },
    message: {
      type: String,
      default: '暂时没有找到相关内容'
    }
  }
};
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  min-height: 300px;
  animation: fadeIn 0.5s ease;
}

.empty-icon {
  font-size: 64px;
  color: var(--primary-color, #4a6cf7);
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-dark, #333);
  margin: 0 0 12px 0;
}

.empty-message {
  font-size: 1rem;
  color: var(--text-medium, #666);
  margin: 0 0 32px 0;
  line-height: 1.6;
  max-width: 400px;
}

/* 骨架屏样式 */
.skeleton-container {
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 50%;
  margin: 0 auto 24px;
}

.skeleton-text {
  height: 20px;
  width: 200px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin: 0 auto;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .empty-title {
    color: #e2e8f0;
  }

  .empty-message {
    color: #94a3b8;
  }

  .skeleton-icon,
  .skeleton-text {
    background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
  }
}
</style>