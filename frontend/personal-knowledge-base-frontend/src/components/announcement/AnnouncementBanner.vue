<template>
  <div v-if="announcements.length > 0 && !isClosed" class="announcement-banner">
    <el-carousel
      :interval="carouselInterval"
      :autoplay="announcements.length > 1"
      arrow="always"
      indicator-position="none"
      height="auto"
      class="banner-carousel"
      @change="handleCarouselChange"
    >
      <el-carousel-item v-for="announcement in announcements" :key="announcement.id">
        <div
          :class="['banner-item', `banner-${announcement.type}`]"
          @click="handleViewDetail(announcement)"
        >
          <div class="banner-content">
            <div class="banner-icon">
              <i :class="getTypeIcon(announcement.type)"></i>
            </div>
            <div class="banner-text">
              <span :class="['banner-badge', `badge-${announcement.priority}`]">
                {{ getPriorityText(announcement.priority) }}
              </span>
              <h4 class="banner-title">{{ announcement.title }}</h4>
              <p class="banner-summary">{{ truncateText(announcement.content, 80) }}</p>
            </div>
          </div>
          <div class="banner-action">
            <span class="view-detail">查看详情 <i class="el-icon-arrow-right"></i></span>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 关闭按钮 -->
    <button class="close-btn" @click="handleClose" title="关闭公告">
      <i class="el-icon-close"></i>
    </button>

    <!-- 轮播指示器 -->
    <div v-if="announcements.length > 1" class="carousel-indicators">
      <span
        v-for="(item, index) in announcements"
        :key="item.id"
        :class="['indicator', { 'active': currentIndex === index }]"
        @click="goToSlide(index)"
      ></span>
    </div>
  </div>
</template>

<script>
import request from '@/api/axios'

export default {
  name: 'AnnouncementBanner',

  props: {
    // 轮播间隔时间（毫秒）
    carouselInterval: {
      type: Number,
      default: 5000
    }
  },

  data() {
    return {
      announcements: [],
      loading: false,
      currentIndex: 0,
      isClosed: false
    }
  },

  created() {
    this.loadAnnouncements()
  },

  methods: {
    /**
     * 加载生效中的公告列表
     */
    async loadAnnouncements() {
      // 检查是否已关闭过公告（session级别）
      if (sessionStorage.getItem('announcementBannerClosed')) {
        this.isClosed = true
        return
      }

      this.loading = true
      try {
        // 获取生效中的公告
        const response = await request({
          url: '/announcement/active',
          method: 'get'
        })

        if (response.code === 200) {
          this.announcements = response.data?.list || [] || []
        }
      } catch (error) {
        console.error('加载公告失败:', error)
        // 静默失败，不影响页面
      } finally {
        this.loading = false
      }
    },

    /**
     * 处理轮播切换
     */
    handleCarouselChange(index) {
      this.currentIndex = index
    },

    /**
     * 跳转到指定轮播
     */
    goToSlide(index) {
      this.currentIndex = index
      // 通过ref控制carousel，这里emit事件让父组件处理
      this.$emit('carousel-change', index)
    },

    /**
     * 查看公告详情
     */
    handleViewDetail(announcement) {
      this.$emit('view-detail', announcement)

      // 可以跳转到公告详情页或打开详情对话框
      if (announcement.id) {
        this.$router.push(`/announcements/${announcement.id}`).catch(() => {})
      }
    },

    /**
     * 关闭公告横幅
     */
    handleClose() {
      this.isClosed = true
      // 记录关闭状态（session级别，刷新页面后重新显示）
      sessionStorage.setItem('announcementBannerClosed', 'true')
      this.$emit('close')
    },

    /**
     * 获取类型图标
     */
    getTypeIcon(type) {
      const iconMap = {
        system: 'el-icon-bell',
        activity: 'el-icon-present',
        maintenance: 'el-icon-tools'
      }
      return iconMap[type] || 'el-icon-bell'
    },

    /**
     * 获取优先级文本
     */
    getPriorityText(priority) {
      const textMap = {
        low: '',
        medium: '',
        high: '重要',
        urgent: '紧急'
      }
      return textMap[priority] || ''
    },

    /**
     * 截断文本
     */
    truncateText(text, length) {
      if (!text) return ''
      return text.length > length ? text.substring(0, length) + '...' : text
    }
  }
}
</script>

<style scoped>
/* ========== 公告横幅容器 ========== */
.announcement-banner {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  animation: slideInDown 0.3s ease;
}

@keyframes slideInDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* ========== 轮播 ========== */
.banner-carousel {
  border-radius: var(--radius-lg);
}

.banner-carousel :deep(.el-carousel__container) {
  height: auto !important;
}

.banner-carousel :deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.3);
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
}

.banner-carousel :deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.5);
}

.banner-carousel :deep(.el-carousel__arrow--left) {
  left: 8px;
}

.banner-carousel :deep(.el-carousel__arrow--right) {
  right: 40px;
}

/* ========== 公告项目 ========== */
.banner-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  cursor: pointer;
  transition: all var(--transition-base);
  min-height: 80px;
}

.banner-item:hover {
  filter: brightness(1.05);
}

/* 不同类型背景色 */
.banner-system {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.banner-activity {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.banner-maintenance {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

/* ========== 公告内容 ========== */
.banner-content {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  flex: 1;
  min-width: 0;
}

.banner-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.banner-text {
  flex: 1;
  min-width: 0;
}

.banner-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.3);
  margin-bottom: var(--space-1);
}

.badge-urgent {
  background-color: rgba(255, 77, 79, 0.8);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.banner-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  margin: 0 0 var(--space-1) 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.banner-summary {
  font-size: var(--font-size-sm);
  margin: 0;
  line-height: 1.5;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ========== 操作按钮 ========== */
.banner-action {
  flex-shrink: 0;
  margin-left: var(--space-4);
}

.view-detail {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  font-weight: 500;
  white-space: nowrap;
  opacity: 0.8;
  transition: opacity var(--transition-base);
}

.banner-item:hover .view-detail {
  opacity: 1;
}

/* ========== 关闭按钮 ========== */
.close-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 24px;
  height: 24px;
  border: none;
  border-radius: var(--radius-full);
  background-color: rgba(0, 0, 0, 0.2);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all var(--transition-base);
  z-index: 10;
}

.close-btn:hover {
  background-color: rgba(0, 0, 0, 0.4);
  transform: scale(1.1);
}

/* ========== 轮播指示器 ========== */
.carousel-indicators {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) 0;
  background-color: rgba(0, 0, 0, 0.1);
}

.indicator {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background-color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all var(--transition-base);
}

.indicator.active {
  background-color: white;
  width: 20px;
  border-radius: var(--radius-md);
}

.indicator:hover:not(.active) {
  background-color: rgba(255, 255, 255, 0.7);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .banner-item {
    padding: var(--space-3) var(--space-4);
    min-height: 70px;
  }

  .banner-content {
    gap: var(--space-3);
  }

  .banner-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  .banner-title {
    font-size: var(--font-size-sm);
  }

  .banner-summary {
    font-size: var(--font-size-xs);
    -webkit-line-clamp: 1;
  }

  .banner-action {
    display: none;
  }

  .close-btn {
    top: var(--space-1);
    right: var(--space-1);
    width: 20px;
    height: 20px;
  }
}

@media (max-width: 480px) {
  .banner-item {
    padding: var(--space-2) var(--space-3);
  }

  .banner-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }

  .banner-title {
    font-size: var(--font-size-xs);
  }
}
</style>
