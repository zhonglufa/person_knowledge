<template>
  <div class="section-header">
    <h2 class="title">
      <slot name="title">{{ title }}</slot>
    </h2>

    <div class="header-actions">
      <slot name="actions">
        <button
            v-if="viewMoreText"
            class="view-more-button"
            @click="$emit('view-more')"
            :aria-label="`查看更多${title}`"
        >
          <span class="button-text">{{ viewMoreText }}</span>
          <i class="el-icon-right button-icon"></i>
        </button>
      </slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SectionHeader',
  props: {
    title: String,
    viewMoreText: String
  },
  emits: ['view-more']
};
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
  flex-wrap: wrap;
  gap: 16px;
}

.title {
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 700;
  color: var(--text-dark, #1a1a1a);
  margin: 0;
  line-height: 1.2;
}

.view-more-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: var(--primary-color, #4a6cf7);
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 0;
  transition: all 0.3s ease;
}

.button-text {
  position: relative;
}

.button-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: currentColor;
  transition: width 0.3s ease;
}

.view-more-button:hover .button-text::after {
  width: 100%;
}

.button-icon {
  font-size: 0.9rem;
  transition: transform 0.3s ease;
}

.view-more-button:hover .button-icon {
  transform: translateX(4px);
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .title {
    color: #e2e8f0;
  }
}
</style>
