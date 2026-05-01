<template>
  <section class="knowledge-section">
    <div class="container">
      <SectionHeader
        title="热门知识内容"
        view-more-text="查看更多"
        @view-more="$emit('view-more')"
      />
      
      <div class="knowledge-carousel" v-if="knowledge.length > 0">
        <div class="carousel-wrapper">
          <KnowledgeCard
            v-for="item in knowledge"
            :key="item.id"
            :knowledge="item"
            :loading="loading"
            class="carousel-item"
            @click="$emit('item-click', item.id)"
          />
        </div>
      </div>
      
      <EmptyState
        v-else
        :loading="loading"
        message="暂无热门知识内容"
      />
    </div>
  </section>
</template>

<script>
import SectionHeader from './SectionHeader.vue';
import KnowledgeCard from './GuestKnowledgeCard.vue';
import EmptyState from './EmptyState.vue';

export default {
  name: 'PopularKnowledgeSection',
  components: {
    SectionHeader,
    KnowledgeCard,
    EmptyState
  },
  props: {
    knowledge: {
      type: Array,
      default: () => []
    },
    loading: Boolean
  },
  emits: ['item-click', 'view-more']
};
</script>

<style scoped>
.knowledge-section {
  padding: clamp(60px, 15vw, 100px) 0;
  background: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 横向滚动容器 */
.knowledge-carousel {
  overflow-x: auto;
  overflow-y: hidden;
  padding: 10px 0;
  scrollbar-width: thin;
  scrollbar-color: #c1c1c1 transparent;
}

.knowledge-carousel::-webkit-scrollbar {
  height: 8px;
}

.knowledge-carousel::-webkit-scrollbar-track {
  background: transparent;
}

.knowledge-carousel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.knowledge-carousel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.carousel-wrapper {
  display: flex;
  gap: 28px;
  padding: 10px 0;
  min-width: min-content;
}

.carousel-item {
  flex: 0 0 350px;
  min-width: 350px;
  max-width: 350px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .carousel-item {
    flex: 0 0 300px;
    min-width: 300px;
    max-width: 300px;
  }
  
  .carousel-wrapper {
    gap: 20px;
  }
}

@media (max-width: 480px) {
  .carousel-item {
    flex: 0 0 280px;
    min-width: 280px;
    max-width: 280px;
  }
  
  .container {
    padding: 0 15px;
  }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .knowledge-section {
    background: #0f172a;
  }
}
</style>