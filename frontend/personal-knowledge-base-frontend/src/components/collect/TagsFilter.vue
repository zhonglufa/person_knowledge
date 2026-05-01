<template>
  <div class="tags-filter sidebar-tags">
    <div class="sidebar-header">
      <div class="header-top-row">
        <div class="header-left">
          <el-button
            type="text"
            size="small"
            icon="el-icon-back"
            class="back-btn"
            @click="$emit('back')"
          >
          </el-button>
          <h3 class="filter-title">
            <i class="fas fa-tags"></i>
            标签
            <span v-if="selectedTags.length > 0" class="selected-count">
              ({{ selectedTags.length }})
            </span>
          </h3>
        </div>
      </div>

      <div class="search-row">
        <el-input
          ref="searchInput"
          v-model="searchKeyword"
          placeholder="搜索标签"
          prefix-icon="el-icon-search"
          class="search-input"
          @keyup.enter="handleSearch"
          @input="handleSearch"
        >
        </el-input>
      </div>
    </div>
    
    <div class="tags-container">
      <div
        v-for="tag in filteredTags"
        :key="tag.id"
        class="tag-item"
        :class="{ 'selected': selectedTags.includes(tag.id) }"
        @click="handleTagClick(tag)"
      >
        <div class="tag-content">
          <el-tag
            :type="selectedTags.includes(tag.id) ? 'primary' : 'info'"
            size="medium"
            class="tag-name"
          >
            {{ tag.name }}
          </el-tag>
          <span v-if="tag.count !== undefined" class="tag-count">
            {{ tag.count }}
          </span>
        </div>
      </div>
      
      <div v-if="filteredTags.length === 0" class="empty-tags">
        <i class="el-icon-collection-tag"></i>
        <p>{{ emptyStateText }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TagsFilter',
  props: {
    popularTags: {
      type: Array,
      default: () => []
    },
    selectedTags: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      searchKeyword: ''
    };
  },
  computed: {
    filteredTags() {
      if (!this.searchKeyword.trim()) {
        return this.popularTags;
      }
      const keyword = this.searchKeyword.toLowerCase();
      return this.popularTags.filter(tag => 
        tag.name.toLowerCase().includes(keyword)
      );
    },
    emptyStateText() {
      return this.searchKeyword ? '未找到匹配的标签' : '暂无可用于筛选收藏项的标签';
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.searchInput?.focus();
    });
  },
  methods: {
    handleSearch() {
      this.$emit('search-tags', this.searchKeyword);
    },
    handleTagClick(tag) {
      this.$emit('toggle-tag', tag.id);
    }
  }
}
</script>

<style scoped>
.tags-filter {
  padding: 0;
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  transition: all var(--transition-base);
}

.sidebar-header {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-light);
  background-color: var(--bg-primary);
  transition: all var(--transition-base);
}

.header-top-row {
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.filter-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-dark);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all var(--transition-base);
}

.filter-title i {
  color: var(--primary-color);
  font-size: var(--font-size-base);
}

.selected-count {
  color: var(--text-light);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.back-btn {
  color: var(--text-light) !important;
  font-size: 16px !important;
  transition: all var(--transition-base);
}

.back-btn:hover {
  color: var(--primary-color) !important;
  transform: scale(1.1);
}

.search-row {
  width: 100%;
}

.search-input {
  width: 100%;
}

.tags-container {
  flex: 1;
  padding: var(--spacing-lg) var(--spacing-xl);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all var(--transition-base);
}

.tag-item {
  cursor: pointer;
  transition: all var(--transition-base);
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--bg-gray);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-item:hover {
  background-color: var(--bg-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.tag-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: var(--spacing-md);
}

.tag-name {
  border-radius: var(--border-radius-md) !important;
  font-size: var(--font-size-base) !important;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.tag-count {
  min-width: 20px;
  text-align: right;
  color: var(--text-light);
  font-size: var(--font-size-sm);
}

.empty-tags {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-light);
  padding: var(--spacing-xl) 0;
}
</style>
