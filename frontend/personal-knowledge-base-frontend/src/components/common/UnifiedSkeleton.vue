<template>
  <div class="skeleton-wrapper" :class="{ 'skeleton-animated': animated }">
    <!-- 卡片骨架屏 -->
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-image" :style="{ height: imageHeight }"></div>
      <div class="skeleton-content">
        <div class="skeleton-title" :style="{ width: titleWidth }"></div>
        <div class="skeleton-text" :style="{ width: textWidth }"></div>
        <div class="skeleton-meta">
          <div class="skeleton-tag"></div>
          <div class="skeleton-tag short"></div>
        </div>
      </div>
    </div>

    <!-- 列表骨架屏 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div v-for="i in rows" :key="i" class="skeleton-list-item">
        <div class="skeleton-avatar" v-if="showAvatar"></div>
        <div class="skeleton-list-content">
          <div class="skeleton-title" :style="{ width: titleWidth }"></div>
          <div class="skeleton-text" :style="{ width: textWidth }"></div>
        </div>
      </div>
    </div>

    <!-- 表格骨架屏 -->
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div v-for="i in rows" :key="i" class="skeleton-table-row">
        <div v-for="col in columns" :key="col" class="skeleton-cell" :style="{ width: getColumnWidth(col) }"></div>
      </div>
    </div>

    <!-- 统计卡片骨架屏 -->
    <div v-else-if="type === 'stats'" class="skeleton-stats">
      <div v-for="i in count" :key="i" class="skeleton-stat-card">
        <div class="skeleton-icon"></div>
        <div class="skeleton-number"></div>
        <div class="skeleton-label"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UnifiedSkeleton',
  props: {
    // 骨架屏类型：card / list / table / stats
    type: {
      type: String,
      default: 'card',
      validator: (val) => ['card', 'list', 'table', 'stats'].includes(val)
    },
    // 是否显示动画
    animated: {
      type: Boolean,
      default: true
    },
    // 列表/表格行数
    rows: {
      type: Number,
      default: 5
    },
    // 表格列数
    columns: {
      type: Number,
      default: 4
    },
    // 统计卡片数量
    count: {
      type: Number,
      default: 3
    },
    // 图片高度
    imageHeight: {
      type: String,
      default: '200px'
    },
    // 标题宽度
    titleWidth: {
      type: String,
      default: '80%'
    },
    // 文本宽度
    textWidth: {
      type: String,
      default: '100%'
    },
    // 列表是否显示头像
    showAvatar: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    getColumnWidth(index) {
      const widths = ['30%', '25%', '20%', '25%']
      return widths[index % widths.length]
    }
  }
}
</script>

<style scoped>
.skeleton-wrapper {
  width: 100%;
}

/* 基础骨架样式 */
.skeleton-image,
.skeleton-title,
.skeleton-text,
.skeleton-tag,
.skeleton-avatar,
.skeleton-cell,
.skeleton-icon,
.skeleton-number,
.skeleton-label {
  background: linear-gradient(
    90deg,
    var(--bg-secondary) 25%,
    var(--bg-tertiary) 50%,
    var(--bg-secondary) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-sm);
}

/* 动画效果 */
.skeleton-animated .skeleton-image,
.skeleton-animated .skeleton-title,
.skeleton-animated .skeleton-text,
.skeleton-animated .skeleton-tag,
.skeleton-animated .skeleton-avatar,
.skeleton-animated .skeleton-cell,
.skeleton-animated .skeleton-icon,
.skeleton-animated .skeleton-number,
.skeleton-animated .skeleton-label {
  animation: skeleton-loading 1.5s ease-in-out infinite;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 卡片骨架屏 */
.skeleton-card {
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.skeleton-image {
  width: 100%;
  margin-bottom: var(--space-4);
}

.skeleton-content {
  padding: var(--space-4);
}

.skeleton-title {
  height: 20px;
  margin-bottom: var(--space-3);
}

.skeleton-text {
  height: 14px;
  margin-bottom: var(--space-3);
}

.skeleton-meta {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.skeleton-tag {
  height: 24px;
  width: 60px;
  border-radius: var(--radius-sm);
}

.skeleton-tag.short {
  width: 40px;
}

/* 列表骨架屏 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.skeleton-list-item {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
  padding: var(--space-3);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
}

.skeleton-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-list-content {
  flex: 1;
}

/* 表格骨架屏 */
.skeleton-table {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.skeleton-table-row {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
}

.skeleton-cell {
  height: 20px;
  flex: 1;
}

/* 统计卡片骨架屏 */
.skeleton-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
}

.skeleton-stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-5);
  background: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.skeleton-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-bottom: var(--space-3);
}

.skeleton-number {
  width: 80px;
  height: 28px;
  margin-bottom: var(--space-2);
}

.skeleton-label {
  width: 60px;
  height: 14px;
}
</style>
