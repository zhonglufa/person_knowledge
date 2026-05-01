<template>
  <section class="values-section">
    <div class="container">
      <div class="values-grid">
        <ValueCard
          v-for="value in values"
          :key="value.id"
          :value="value"
          @click="$emit('value-click', value.id)"
        />
      </div>
    </div>
  </section>
</template>

<script>
import ValueCard from './ValueCard.vue';

export default {
  name: 'CoreValuesSection',
  components: {
    ValueCard
  },
  data() {
    return {
      values: [
        {
          id: 1,
          icon: 'el-icon-s-grid',
          title: '结构化知识管理',
          description: '按照知识类型分类存储，构建清晰的知识体系结构。支持多维度关联，形成个人知识网络，让知识不再孤立存在。'
        },
        {
          id: 2,
          icon: 'el-icon-search',
          title: '高效检索与应用',
          description: '基于知识主题、关键词和节点内容的精准搜索，快速定位所需信息。提供预设模板和结构化展示，帮助您快速理解和应用知识内容。'
        }
      ]
    };
  },
  emits: ['value-click']
};
</script>

<style scoped>
.values-section {
  padding: clamp(60px, 15vw, 100px) 0;
  background: linear-gradient(to bottom, #f8fafc 0%, #ffffff 100%);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 400px), 1fr));
  gap: clamp(24px, 5vw, 40px);
  counter-reset: value-counter;
}
</style>