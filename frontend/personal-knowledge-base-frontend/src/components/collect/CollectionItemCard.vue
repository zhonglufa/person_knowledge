<template>
  <div 
    class="collection-item-card"
    :class="{ 'selected': selected }"
    @click="$emit('click', item)"
    @contextmenu.prevent="$emit('contextmenu', $event, item)"
  >
    <!-- 封面/缩略图区�?-->
    <div class="item-cover">
      <!-- 加工状态角�?-->
      <div 
        v-if="item.digestStatus" 
        class="digest-status-badge"
        :class="`status-${item.digestStatus}`"
      >
        {{ getDigestStatusText(item.digestStatus) }}
      </div>
      
      <!-- 图片类型的封�?-->
      <img 
        v-if="item.sourceType === 2 && item.sourceUrl"
        :src="item.sourceUrl"
        :alt="item.title"
        class="cover-image"
        @error="handleImageError"
      />
      
      <!-- 网页类型的预览图或favicon -->
      <div v-else-if="item.sourceType === 1" class="web-preview">
        <img 
          v-if="faviconUrl"
          :src="faviconUrl"
          :alt="item.title"
          class="favicon"
          @error="handleFaviconError"
        />
        <div v-else class="source-icon">
          <i class="fas fa-globe"></i>
        </div>
      </div>
      
      <!-- 文本类型 -->
      <div v-else-if="item.sourceType === 3" class="text-preview">
        <i class="fas fa-file-alt"></i>
      </div>
      
      <!-- 视频类型 -->
      <div v-else-if="item.sourceType === 4" class="video-preview">
        <i class="fas fa-video"></i>
      </div>
      
      <!-- 默认占位�?-->
      <div v-else class="default-preview">
        <i class="fas fa-bookmark"></i>
      </div>
      
      <!-- 覆盖层操作按�?-->
      <div class="cover-overlay">
        <div class="overlay-actions">
          <el-button 
            type="text" 
            icon="el-icon-edit"
            @click.stop="$emit('edit', item)"
            title="编辑"
          />
          <el-button 
            type="text" 
            icon="el-icon-document"
            @click.stop="$emit('process', item)"
            title="加工编辑"
            class="process-btn"
          />
          <el-button 
            type="text" 
            icon="el-icon-delete"
            @click.stop="$emit('delete', item)"
            title="删除"
          />
        </div>
      </div>
      
      <!-- 状态标�?-->
      <div class="status-indicators">
        <el-tag 
          v-if="!item.isRead" 
          type="danger" 
          size="mini"
          class="unread-tag"
        >
          未读
        </el-tag>
        <el-tag 
          v-if="item.isPublic === 1" 
          type="success" 
          size="mini"
          class="shared-tag"
        >
          公开
        </el-tag>
      </div>
    </div>
    
    <!-- 内容信息区域 -->
    <div class="item-content">
      <!-- 标题 -->
      <h3 class="item-title" :title="item.title">
        {{ truncatedTitle }}
      </h3>
      
      <!-- 摘要 -->
      <p v-if="item.coreAbstract" class="item-abstract">
        {{ truncatedAbstract }}
      </p>
      
      <!-- 标签 -->
      <div v-if="item.tags && item.tags.length > 0" class="item-tags">
        <el-tag
          v-for="tag in displayTags"
          :key="tag.id"
          :color="tag.color"
          size="mini"
          class="tag-item"
        >
          {{ tag.name }}
        </el-tag>
        <el-tag
          v-if="item.tags.length > 3"
          size="mini"
          type="info"
          class="more-tags"
        >
          +{{ item.tags.length - 3 }}
        </el-tag>
      </div>
      
      <!-- 底部信息 -->
      <div class="item-footer">
        <div class="footer-left">
          <!-- 学习进度 -->
          <div v-if="item.studyProgress" class="progress-info">
            <el-progress
              :percentage="parseProgress(item.studyProgress)"
              :stroke-width="4"
              :show-text="false"
              class="progress-bar"
            />
            <span class="progress-text">{{ item.studyProgress }}</span>
          </div>
          
          <!-- 消化状�?-->
          <el-tag
            v-if="item.digestStatus"
            :type="getDigestStatusType(item.digestStatus)"
            size="mini"
            class="status-tag"
          >
            {{ getDigestStatusText(item.digestStatus) }}
          </el-tag>
        </div>
        
        <div class="footer-right">
          <!-- 来源信息 -->
          <div v-if="item.source" class="source-info">
            <i class="fas fa-link"></i>
            <span>{{ item.source }}</span>
          </div>
          
          <!-- 访问次数 -->
          <div v-if="item.visitCount > 0" class="visit-count">
            <i class="fas fa-eye"></i>
            <span>{{ item.visitCount }}</span>
          </div>
        </div>
      </div>
      
      <!-- 时间信息 -->
      <div class="item-time">
        <i class="fas fa-clock"></i>
        <span>{{ formatTime(item.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CollectionItemCard',
  props: {
    item: {
      type: Object,
      required: true,
      validator(value) {
        return value.id && value.title
      }
    },
    selected: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click', 'contextmenu', 'edit', 'delete', 'process'],
  data() {
    return {
      faviconLoaded: true
    }
  },
  computed: {
    truncatedTitle() {
      const title = this.item.title || '无标题';
      return title.length > 50 ? title.substring(0, 50) + '...' : title
    },
    
    truncatedAbstract() {
      const abstract = this.item.coreAbstract || ''
      return abstract.length > 100 ? abstract.substring(0, 100) + '...' : abstract
    },
    
    displayTags() {
      return (this.item.tags || []).slice(0, 3)
    },
    
    faviconUrl() {
      if (this.item.sourceType !== 1 || !this.item.sourceUrl) return ''
      
      try {
        const url = new URL(this.item.sourceUrl)
        return `https://www.google.com/s2/favicons?domain=${url.hostname}&sz=32`
      } catch (error) {
        console.warn('解析URL失败:', error);
        return ''
      }
    }
  },
  methods: {
    parseProgress(progress) {
      if (!progress) return 0
      if (progress === 'overdue') return 0
      return parseInt(progress.replace('%', '')) || 0
    },
    
    getDigestStatusType(status) {
      const statusMap = {
        'undigest': 'info',
        'digesting': 'warning',
        'digested': 'success',
        'abandoned': 'danger'
      }
      return statusMap[status] || 'info'
    },
    
    getDigestStatusText(status) {
      const statusMap = {
        'undigest': '未消化',
        'digesting': '消化中',
        'digested': '已消化',
        'abandoned': '已放弃'
      }
      return statusMap[status] || status
    },
    
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
      
      // 本周
      if (diff < 7 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000))
        return `${days}天前`
      }
      
      // 日期格式
      return `${time.getFullYear()}-${(time.getMonth() + 1).toString().padStart(2, '0')}-${time.getDate().toString().padStart(2, '0')}`
    },
    
    handleImageError(event) {
      console.warn(`收藏项图片加载失�? ${event.target.src}`)
      event.target.style.display = 'none'
      event.target.parentElement.classList.add('image-error')
    },
    
    handleFaviconError() {
      this.faviconLoaded = false
    }
  }
}
</script>

<style scoped>
.collection-item-card {
  position: relative;
  background: var(--bg-primary);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.collection-item-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--primary-color);
}

.collection-item-card.selected {
  border-color: var(--primary-color);
  background-color: rgba(74, 108, 247, 0.05);
  box-shadow: 0 0 0 2px rgba(74, 108, 247, 0.1);
}

/* 封面区域 */
.item-cover {
  position: relative;
  height: 160px;
  background: var(--bg-gray);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-bottom: 1px solid var(--border-light);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.collection-item-card:hover .cover-image {
  transform: scale(1.05);
}

.web-preview,
.text-preview,
.video-preview,
.default-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-light);
  font-size: 32px;
  background: linear-gradient(135deg, var(--bg-gray) 0%, var(--bg-secondary) 100%);
}

.favicon {
  width: 32px;
  height: 32px;
}

.source-icon {
  font-size: 28px;
  opacity: 0.7;
}

/* 覆盖�?*/
.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.collection-item-card:hover .cover-overlay {
  opacity: 1;
}

.overlay-actions {
  display: flex;
  gap: 12px;
}

.overlay-actions :deep(.el-button) {
  color: white !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  padding: 8px 12px !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

.overlay-actions :deep(.el-button:hover) {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: white !important;
}

.process-btn :deep(.el-button:hover) {
  background: rgba(74, 108, 247, 0.3) !important;
  border-color: rgba(74, 108, 247, 0.5) !important;
}

/* 状态标�?*/
.status-indicators {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.unread-tag,
.shared-tag {
  margin: 0;
}

/* 内容区域 */
.item-content {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-dark);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-abstract {
  font-size: 14px;
  color: var(--text-light);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 标签区域 */
.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  margin: 0;
  border: none;
}

.more-tags {
  margin: 0;
}

/* 底部信息 */
.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
  margin-top: auto;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 80px;
}

.progress-bar {
  flex: 1;
  max-width: 60px;
}

.progress-text {
  font-size: 12px;
  color: var(--text-light);
  white-space: nowrap;
}

.status-tag {
  margin: 0;
}

/* 加工状态角�?*/
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

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-light);
}

.source-info,
.visit-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 时间信息 */
.item-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-light);
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

/* 响应式设�?*/
@media (max-width: 768px) {
  .item-cover {
    height: 140px;
  }
  
  .item-content {
    padding: 12px;
  }
  
  .item-title {
    font-size: 15px;
  }
  
  .item-abstract {
    font-size: 13px;
  }
  
  .footer-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .progress-info {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .item-cover {
    height: 120px;
  }
  
  .web-preview,
  .text-preview,
  .video-preview,
  .default-preview {
    font-size: 24px;
  }
  
  .item-content {
    padding: 10px;
  }
  
  .item-title {
    font-size: 14px;
    -webkit-line-clamp: 1;
  }
  
  .item-abstract {
    font-size: 12px;
    -webkit-line-clamp: 2;
  }
  
  .item-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .footer-right {
    align-self: flex-end;
  }
}
</style>
