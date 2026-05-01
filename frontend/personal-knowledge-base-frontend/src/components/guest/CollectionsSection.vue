<template>
  <section class="recommended-collections-section">
    <div class="container">
      <!-- 区域头部 -->
      <div class="section-header">
        <h2 class="section-title">推荐收藏集</h2>
        <el-button type="text" @click="goToCollections" class="view-more-btn">
          查看更多 <i class="el-icon-right"></i>
        </el-button>
      </div>

      <!-- 加载骨架屏 -->
      <div class="collections-carousel" v-if="loading">
        <div class="carousel-wrapper">
          <div
            v-for="i in 5"
            :key="'skeleton-' + i"
            class="collection-card skeleton-card"
          >
            <el-skeleton animated>
              <el-skeleton-item variant="image" style="width: 100%; height: 120px; margin-bottom: 16px;"></el-skeleton-item>
              <el-skeleton-item variant="text" style="width: 80%; height: 20px; margin-bottom: 12px;"></el-skeleton-item>
              <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 8px;"></el-skeleton-item>
              <el-skeleton-item variant="text" style="width: 50%; height: 16px;"></el-skeleton-item>
            </el-skeleton>
          </div>
        </div>
      </div>

      <!-- 收藏集列表 - 横向滚动 -->
      <div class="collections-carousel" v-else-if="collections.length > 0">
        <div class="carousel-wrapper">
          <div
            v-for="collection in collections"
            :key="collection.id"
            class="collection-card"
            @click="goToCollection(collection.id)"
          >
            <div
              class="collection-image"
              :style="{ backgroundImage: `url(${collection.coverImage})` }"
              @error="handleImageError"
            ></div>
            <div class="collection-info">
              <h3 class="collection-title">{{ collection.title || collection.name }}</h3>
              <p class="collection-desc">{{ collection.description }}</p>
              <div class="collection-stats">
                <i class="el-icon-folder"></i>
                <span>{{ collection.itemCount }}个知识项</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="暂无推荐收藏集" />
      </div>
    </div>
  </section>
</template>

<script>
import SectionHeader from './SectionHeader.vue'
import EmptyState from './EmptyState.vue'

export default {
  name: 'CollectionsSection',
  props: {
    // 可以接收外部传入的收藏数据
    collections: {
      type: Array,
      default: () => []
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    // 跳转到收藏集列表页面
    goToCollections() {
      this.$emit('view-more')
    },

    // 跳转到收藏集详情页面
    goToCollection(id) {
      this.$emit('item-click', id)
    },

    // 处理图片加载错误
    handleImageError(event) {
      // 设置默认图片或隐藏图片区域
      event.target.style.backgroundImage = 'none'
      event.target.style.backgroundColor = '#f5f7fa'
    }
  }
}
</script>

<style scoped>
.recommended-collections-section {
  padding: 40px 0;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.view-more-btn {
  color: #606266;
  font-size: 14px;
  padding: 0;
}

.view-more-btn:hover {
  color: #409eff;
}

/* 横向滚动容器 */
.collections-carousel {
  overflow-x: auto;
  overflow-y: hidden;
  padding: 10px 0;
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 transparent;
}

.collections-carousel::-webkit-scrollbar {
  height: 8px;
}

.collections-carousel::-webkit-scrollbar-track {
  background: transparent;
}

.collections-carousel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.collections-carousel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.carousel-wrapper {
  display: flex;
  gap: 24px;
  padding: 10px 0;
  min-width: min-content;
}

.collection-card {
  flex: 0 0 280px;
  min-width: 280px;
  max-width: 280px;
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.collection-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.12);
}

/* 骨架屏卡片样式 */
.skeleton-card {
  height: 240px;
}

.collection-image {
  width: 100%;
  height: 140px;
  background-size: cover;
  background-position: center;
  background-color: #f5f7fa;
  transition: background-color 0.3s ease;
}

.collection-info {
  padding: 16px;
}

.collection-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.collection-desc {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collection-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background-color: #fff;
  border-radius: 8px;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .collection-card {
    flex: 0 0 250px;
    min-width: 250px;
    max-width: 250px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .carousel-wrapper {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .collection-card {
    flex: 0 0 220px;
    min-width: 220px;
    max-width: 220px;
  }
  
  .container {
    padding: 0 15px;
  }
}
</style>