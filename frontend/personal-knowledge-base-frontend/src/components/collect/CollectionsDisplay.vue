<template>
  <div class="collections-display">
    <div class="empty-state" v-if="collections.length === 0 && !loading && !loadingMore">
      <div class="empty-icon">
        <i class="fas fa-bookmark"></i>
      </div>
      <h3 class="empty-title">{{ emptyTitle }}</h3>
      <p class="empty-description">{{ emptyDescription }}</p>
    </div>

    <div class="loading-state" v-if="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <div class="content-wrapper" v-if="collections.length > 0 || loading || loadingMore">
      <div class="grid-container">
        <PublicCollectionGrid
          v-if="activeSidebarItem === 'discover'"
          :collections="collections"
          @collection-click="$emit('collection-click', $event)"
        />

        <GridView
          v-else
          :collections="collections"
          :selected-collections="selectedCollections"
          :active-sidebar-item="activeSidebarItem"
          :current-user-id="currentUserId"
          :user-collected-urls="userCollectedUrls"
          :show-selection-mode="showSelectionMode"
          @collection-click="$emit('collection-click', $event)"
          @toggle-star="$emit('toggle-star', $event)"
          @delete-collection="$emit('delete-collection', $event)"
          @card-menu="$emit('card-menu', $event)"
          @filter-by-tag="$emit('filter-by-tag', $event)"
          @show-all-collection-tags="$emit('show-all-collection-tags', $event)"
          @preview-image="$emit('preview-image', $event)"
          @preview-text="$emit('preview-text', $event)"
          @preview-link="$emit('preview-link', $event)"
          @preview-video="$emit('preview-video', $event)"
          @show-context-menu="$emit('show-context-menu', $event)"
          @recover-collection="$emit('recover-collection', $event)"
          @permanent-delete="$emit('permanent-delete', $event)"
          @toggle-selection="$emit('toggle-selection', $event)"
        />

        <div v-if="!loading && collections.length > 0" class="load-more-section">
          <div v-if="loadingMore" class="load-more-state loading-more-state">
            <i class="fas fa-spinner fa-spin"></i>
            <span>正在加载更多...</span>
          </div>

          <div v-else-if="loadMoreError" class="load-more-state error-more-state">
            <span>{{ loadMoreError }}</span>
            <button class="retry-button" type="button" @click="$emit('retry-load-more')">重试</button>
          </div>

          <div v-else-if="!hasMore" class="load-more-state end-more-state">
            <span>已经到底了</span>
          </div>

          <div
            v-else
            ref="loadMoreSentinel"
            class="load-more-sentinel"
            aria-hidden="true"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GridView from './GridView.vue';
import PublicCollectionGrid from './PublicCollectionGrid.vue';

export default {
  name: 'CollectionsDisplay',
  components: {
    GridView,
    PublicCollectionGrid
  },
  props: {
    collections: {
      type: Array,
      default: () => []
    },
    filteredCollectionsLength: {
      type: Number,
      default: 0
    },
    total: {
      type: Number,
      default: 0
    },
    loading: {
      type: Boolean,
      default: false
    },
    loadingMore: {
      type: Boolean,
      default: false
    },
    hasMore: {
      type: Boolean,
      default: true
    },
    loadMoreError: {
      type: String,
      default: ''
    },
    selectedCollections: {
      type: Array,
      default: () => []
    },
    activeSidebarItem: {
      type: String,
      default: ''
    },
    currentUserId: {
      type: Number,
      default: null
    },
    userCollectedUrls: {
      type: Set,
      default: () => new Set()
    },
    showSelectionMode: {
      type: Boolean,
      default: false
    }
  },
  emits: [
    'collection-click',
    'toggle-star',
    'delete-collection',
    'recover-collection',
    'permanent-delete',
    'card-menu',
    'filter-by-tag',
    'show-all-collection-tags',
    'preview-image',
    'preview-text',
    'preview-link',
    'preview-video',
    'show-context-menu',
    'toggle-selection',
    'load-more',
    'retry-load-more'
  ],
  data() {
    return {
      observer: null
    };
  },
  computed: {
    emptyTitle() {
      return this.activeSidebarItem === 'discover' ? '暂无公开收藏集' : '暂无收藏项';
    },
    emptyDescription() {
      return this.activeSidebarItem === 'discover'
        ? '当前还没有可展示的公开收藏集'
        : '点击上方"添加收藏"按钮开始收藏您感兴趣的内容';
    }
  },
  mounted() {
    this.initLoadMoreObserver();
  },
  beforeDestroy() {
    this.destroyLoadMoreObserver();
  },
  watch: {
    hasMore() {
      this.$nextTick(() => {
        this.initLoadMoreObserver();
      });
    },
    loading() {
      this.$nextTick(() => {
        this.initLoadMoreObserver();
      });
    },
    loadingMore() {
      this.$nextTick(() => {
        this.initLoadMoreObserver();
      });
    },
    collections() {
      this.$nextTick(() => {
        this.initLoadMoreObserver();
      });
    },
    activeSidebarItem() {
      this.$nextTick(() => {
        this.initLoadMoreObserver();
      });
    }
  },
  methods: {
    initLoadMoreObserver() {
      this.destroyLoadMoreObserver();

      if (!this.hasMore || this.loading || this.loadingMore) {
        return;
      }

      const sentinel = this.$refs.loadMoreSentinel;
      if (!sentinel || typeof IntersectionObserver === 'undefined') {
        return;
      }

      this.observer = new IntersectionObserver(entries => {
        const entry = entries[0];
        if (entry && entry.isIntersecting && this.hasMore && !this.loading && !this.loadingMore) {
          this.$emit('load-more');
        }
      }, {
        root: null,
        rootMargin: '200px 0px 200px 0px',
        threshold: 0
      });

      this.observer.observe(sentinel);
    },
    destroyLoadMoreObserver() {
      if (this.observer) {
        this.observer.disconnect();
        this.observer = null;
      }
    }
  }
}
</script>

<style scoped>
.collections-display {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
  border-radius: 0 0 var(--border-radius-lg) var(--border-radius-lg);
  box-shadow: none;
  transition: all var(--transition-base);
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 12px;
  padding: 48px 24px;
  color: #64748b;
}

.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  color: #4f46e5;
  font-size: 28px;
}

.empty-title {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.empty-description {
  margin: 0;
  font-size: 14px;
}

.loading-state {
  padding: 24px;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.grid-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.load-more-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 48px;
}

.load-more-state {
  color: #64748b;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.retry-button {
  border: none;
  background: transparent;
  color: #4f46e5;
  cursor: pointer;
}

.load-more-sentinel {
  width: 100%;
  height: 1px;
}
</style>
