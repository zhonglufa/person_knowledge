<template>
  <div 
    :class="['collection-card', { 'selected': selected }]"
    @click="$emit('collection-click', collection)"
    @contextmenu.prevent="$emit('show-context-menu', $event, collection)"
  >
    <!-- 封面区域 -->
    <div class="collection-cover">
      <!-- 加工状态角标-->
      <div 
        v-if="collection.digestStatus" 
        class="digest-status-badge"
        :class="`status-${collection.digestStatus}`"
      >
        {{ getDigestStatusText(collection.digestStatus) }}
      </div>
      
      <!-- 有图片时显示图片 -->
      <img 
        v-if="collection.coverImage" 
        :src="collection.coverImage"
        :alt="collection.name"
        class="cover-image"
        @error="handleImageError"
      />
      <!-- 无图片时显示颜色背景 -->
      <div 
        v-else 
        class="cover-background"
        :style="{ backgroundColor: collection.coverColor || '#4ECDC4' }"
      >
        <i class="fas fa-layer-group"></i>
      </div>
      
      <!-- 覆盖层 - 显示操作按钮 -->
      <div class="cover-overlay">
        <div class="overlay-actions">
          <el-button 
            type="text" 
            icon="el-icon-edit"
            @click.stop="$emit('edit-collection', collection)"
            title="编辑收藏集"
          />
          <el-button 
            type="text" 
            icon="el-icon-document"
            @click.stop="$emit('process-collection', collection)"
            title="加工编辑"
            class="process-btn"
          />
          <el-button 
            type="text" 
            icon="el-icon-delete"
            @click.stop="$emit('delete-collection', collection)"
            title="删除收藏集"
          />
        </div>
      </div>

      <!-- 选中标记 -->
      <div 
        v-if="showSelectionMode"
        class="selection-check"
        :class="{ 'selected': selected }"
        @click.stop="$emit('toggle-selection', collection.id)"
      >
        <i :class="selected ? 'fas fa-check-circle' : 'far fa-circle'"></i>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="collection-info">
      <!-- 标题 -->
      <h3 class="collection-name">{{ collection.name }}</h3>
      
      <!-- 描述 -->
      <p v-if="collection.description" class="collection-description">
        {{ truncateText(collection.description, 60) }}
      </p>

      <!-- 统计信息 -->
      <div class="collection-stats">
        <span class="stat-item">
          <i class="fas fa-file-alt"></i>
          {{ collection.itemCount || 0 }} 项
        </span>
        <span class="stat-item" v-if="collection.isDefault">
          <i class="fas fa-star"></i>
          默认
        </span>
        <span class="stat-item" v-if="collection.isShared">
          <i class="fas fa-share-alt"></i>
          已共享
        </span>
      </div>

      <!-- 时间信息 -->
      <p class="collection-time">
        {{ formatTime(collection.createTime) }}
      </p>
    </div>
  </div>
</template>

<script>
/**
 * CollectionCard 组件
 * 功能：展示单个收藏集卡片，类似音乐播放列表风格
 * 
 * Props:
 *   - collection: Object - 收藏集对象，包含：
 *     * id: 收藏集ID
 *     * name: 收藏集名称
 *     * description: 描述
 *     * coverImage: 封面图片URL
 *     * coverColor: 封面背景色（#RRGGBB格式）
 *     * itemCount: 收藏项数
 *     * isDefault: 是否为默认收藏集
 *     * isShared: 是否已共享
 *     * createTime: 创建时间
 *   - selected: Boolean - 是否被选中
 *   - showSelectionMode: Boolean - 是否显示选择模式
 * 
 * Emits:
 *   - collection-click: 卡片被点击
 *   - show-context-menu: 右键菜单显示
 *   - edit-collection: 编辑收藏集
 *   - delete-collection: 删除收藏集
 *   - toggle-selection: 切换选中状态
 */
export default {
  name: 'CollectionCard',
  props: {
    collection: {
      type: Object,
      required: true,
      validator(value) {
        return value.id && value.name;
      }
    },
    selected: {
      type: Boolean,
      default: false
    },
    showSelectionMode: {
      type: Boolean,
      default: false
    }
  },
  emits: [
    'collection-click',
    'show-context-menu',
    'edit-collection',
    'delete-collection',
    'toggle-selection',
    'process-collection'
  ],
  methods: {
    /**
     * 截断文本到指定长度
     */
    truncateText(text, length) {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    },

    /**
     * 格式化时间显示
     */
    formatTime(timeString) {
      if (!timeString) return '';
      const time = new Date(timeString);
      const now = new Date();
      const diff = now - time;

      // 今天
      if (time.toDateString() === now.toDateString()) {
        return `今天 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`;
      }

      // 昨天
      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      if (time.toDateString() === yesterday.toDateString()) {
        return '昨天';
      }

      // 3天内
      if (diff < 3 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000));
        return `${days}天前`;
      }

      // 日期格式
      return `${time.getFullYear()}-${(time.getMonth() + 1).toString().padStart(2, '0')}-${time.getDate().toString().padStart(2, '0')}`;
    },

    /**
     * 处理图片加载失败
     */
    handleImageError(event) {
      console.warn(`收藏集封面加载失败 ${event.target.src}`);
      event.target.style.display = 'none';
      event.target.parentElement.classList.add('image-failed');
    },
    
    /**
     * 获取加工状态文本
     */
    getDigestStatusText(status) {
      const statusMap = {
        'undigest': '未消化',
        'digesting': '消化中',
        'digested': '已消化',
        'abandoned': '已放弃'
      };
      return statusMap[status] || status;
    }
  }
}
</script>

<style scoped>
/* 收藏集卡片容器*/
.collection-card {
  position: relative;
  cursor: pointer;
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid var(--border-light);
}

.collection-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--primary-color);
}

.collection-card.selected {
  border-color: var(--primary-color);
  background-color: rgba(74, 108, 247, 0.05);
  box-shadow: 0 0 0 2px rgba(74, 108, 247, 0.1);
}

/* 封面区域 */
.collection-cover {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: var(--bg-gray);
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--border-light);
}

/* 加工状态角标*/
.digest-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.digest-status-badge.status-undigest {
  background-color: #F56C6C;
  color: white;
  animation: pulse 2s infinite;
}

.digest-status-badge.status-digesting {
  background-color: #E6A23C;
  color: white;
}

.digest-status-badge.status-digested {
  background-color: #67C23A;
  color: white;
}

.digest-status-badge.status-abandoned {
  background-color: #909399;
  color: white;
}

@keyframes pulse {
  0% {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  50% {
    box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
  }
  100% {
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
}

/* 封面图片 */
.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.collection-card:hover .cover-image {
  transform: scale(1.05);
}

/* 颜色背景（无图片时） */
.cover-background {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  opacity: 0.9;
}

/* 覆盖层*/
.cover-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.collection-card:hover .cover-overlay {
  opacity: 1;
}

/* 覆盖层操作按钮*/
.overlay-actions {
  display: flex;
  gap: var(--spacing-md);
}

.overlay-actions :deep(.el-button) {
  color: white !important;
  background-color: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  padding: 8px 16px !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

.overlay-actions :deep(.el-button:hover) {
  background-color: rgba(255, 255, 255, 0.3) !important;
  border-color: white !important;
}

.process-btn :deep(.el-button:hover) {
  background-color: rgba(74, 108, 247, 0.3) !important;
  border-color: rgba(74, 108, 247, 0.5) !important;
}

/* 选中标记 */
.selection-check {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--bg-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--transition-base);
  color: var(--text-light);
  border: 1px solid var(--border-light);
}

.selection-check:hover {
  transform: scale(1.1);
  color: var(--primary-color, #4a6cf7);
}

.selection-check.selected {
  background-color: var(--primary-color, #4a6cf7);
  color: white;
}

.selection-check i {
  font-size: 16px;
}

/* 卡片内容区域 */
.collection-info {
  flex: 1;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

/* 收藏集名称*/
.collection-name {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-dark);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 收藏集描述*/
.collection-description {
  font-size: var(--font-size-sm);
  color: var(--text-light);
  margin: 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 统计信息 */
.collection-stats {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin: var(--spacing-xs) 0;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--primary-color, #4a6cf7);
  background-color: rgba(74, 108, 247, 0.1);
  padding: 3px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.stat-item i {
  font-size: 11px;
}

/* 时间信息 */
.collection-time {
  font-size: var(--font-size-xs);
  color: var(--text-light);
  margin: 0;
  margin-top: auto;
}

/* 响应式设置*/
@media (max-width: 768px) {
  .collection-cover {
    height: 160px;
  }

  .collection-info {
    padding: var(--spacing-md);
  }

  .collection-name {
    font-size: var(--font-size-base);
  }

  .overlay-actions {
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .collection-stats {
    gap: var(--spacing-xs);
  }
}

@media (max-width: 480px) {
  .collection-cover {
    height: 140px;
  }

  .collection-info {
    padding: var(--spacing-sm);
  }

  .collection-name {
    font-size: var(--font-size-sm);
  }

  .collection-description {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 1;
  }
}

@media (min-width: 1200px) {
  .collection-cover {
    height: 220px;
  }

  .collection-info {
    padding: var(--spacing-xl);
  }

  .collection-name {
    font-size: var(--font-size-xl);
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .collection-card {
    background-color: var(--bg-primary);
    border-color: var(--border-light);
  }

  .collection-name {
    color: var(--text-dark);
  }

  .collection-description {
    color: var(--text-light);
  }

  .cover-background {
    background-color: var(--bg-gray);
  }

  .collection-cover {
    background-color: var(--bg-gray);
    border-color: var(--border-light);
  }
}