<template>
  <article
    class="knowledge-card"
    :class="{ 'loading': loading }"
    @click="$emit('click')"
    @keydown.enter="$emit('click')"
    tabindex="0"
    role="button"
    :aria-label="`查看${knowledge.title}的详情`"
  >
    <div v-if="loading" class="card-skeleton">
      <div class="skeleton-cover"></div>
      <div class="skeleton-title"></div>
      <div class="skeleton-description"></div>
      <div class="skeleton-footer"></div>
    </div>

    <template v-else>
      <!-- 图片封面 -->
      <div class="card-cover">
        <img
          v-if="knowledge.coverImage || knowledge.url"
          :src="getCoverImageUrl()"
          :alt="knowledge.title"
          class="cover-image"
          @error="handleImageError"
        />
        <div v-else class="cover-placeholder">
          <i class="el-icon-picture-outline"></i>
        </div>
      </div>

      <div class="card-content">
        <div class="card-header">
          <h3 class="card-title">{{ knowledge.title }}</h3>
          <div class="card-badge" :style="typeStyle">
            {{ getTypeName(knowledge.noteType || 'conceptual') }}
          </div>
        </div>

        <p class="card-description">
          {{ knowledge.description }}
        </p>

        <div class="card-footer">
          <time class="card-date" :datetime="knowledge.createTime">
            {{ formatDate(knowledge.createTime) }}
          </time>
          <i class="el-icon-right card-arrow"></i>
        </div>
      </div>
    </template>
  </article>
</template>

<script>
export default {
  name: 'KnowledgeCard',
  props: {
    knowledge: {
      type: Object,
      required: true
    },
    loading: Boolean
  },
  emits: ['click'],
  computed: {
    typeStyle() {
      const styles = {
        procedural: { background: '#4a6cf7', color: 'white' },
        conceptual: { background: '#10b981', color: 'white' },
        factual: { background: '#f59e0b', color: 'white' },
        metacognitive: { background: '#8b5cf6', color: 'white' }
      };
      return styles[this.knowledge.noteType] || { background: '#6b7280', color: 'white' };
    }
  },
  methods: {
    getTypeName(type) {
      const typeMap = {
        procedural: '程序性',
        conceptual: '概念性',
        factual: '事实性',
          metacognitive: '元认知'
      };
      return typeMap[type] || '未知';
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }).format(date);
    },
    // 获取封面图片URL
    getCoverImageUrl() {
      // 优先使用coverImage字段
      if (this.knowledge.coverImage) {
        return this.knowledge.coverImage;
      }
      // 如果没有coverImage，尝试从URL获取favicon或生成默认图片
      if (this.knowledge.url) {
        try {
          const url = new URL(this.knowledge.url);
          return `https://www.google.com/s2/favicons?domain=${url.hostname}&sz=256`;
        } catch (e) {
          return '';
        }
      }
      return '';
    },
    // 处理图片加载错误
    handleImageError(e) {
      e.target.src = '';
      e.target.style.display = 'none';
      e.target.nextElementSibling.style.display = 'flex';
    }
  }
};
</script>

<style scoped>
.knowledge-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.knowledge-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color, #4a6cf7), var(--secondary-color, #36cbcb));
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.knowledge-card:hover,
.knowledge-card:focus {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: var(--primary-color, #4a6cf7);
}

.knowledge-card:hover::before,
.knowledge-card:focus::before {
  transform: scaleX(1);
}

.knowledge-card:active {
  transform: translateY(-2px);
}

/* 图片封面 */
.card-cover {
  height: 160px;
  width: 100%;
  overflow: hidden;
  position: relative;
  background: #f5f5f5;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.knowledge-card:hover .cover-image {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #999;
  font-size: 2rem;
}

/* 卡片内容 */
.card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.card-title {
  flex: 1;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-dark, #1a1a1a);
  line-height: 1.4;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-description {
  color: var(--text-medium, #666);
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 16px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-date {
  font-size: 0.875rem;
  color: var(--text-light, #999);
}

.card-arrow {
  color: var(--primary-color, #4a6cf7);
  transition: transform 0.3s ease;
}

.knowledge-card:hover .card-arrow {
  transform: translateX(4px);
}

/* 骨架屏样式 */
.card-skeleton {
  animation: pulse 1.5s ease-in-out infinite;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.skeleton-cover {
  height: 160px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  margin-bottom: 20px;
}

.skeleton-title {
  height: 20px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin: 0 20px 12px;
}

.skeleton-description {
  height: 40px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin: 0 20px 16px;
}

.skeleton-footer {
  height: 16px;
  width: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  border-radius: 4px;
  margin: 0 20px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .knowledge-card {
    background: #1e293b;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  }

  .card-cover {
    background: #1e293b;
  }

  .cover-placeholder {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    color: #64748b;
  }

  .card-title {
    color: #e2e8f0;
  }

  .card-description {
    color: #94a3b8;
  }

  .card-footer {
    border-top-color: #334155;
  }

  .card-date {
    color: #64748b;
  }

  .skeleton-cover,
  .skeleton-title,
  .skeleton-description,
  .skeleton-footer {
    background: linear-gradient(90deg, #334155 25%, #475569 50%, #334155 75%);
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .knowledge-card,
  .card-arrow,
  .card-skeleton,
  .cover-image {
    transition: none;
    animation: none;
  }
}
</style>
