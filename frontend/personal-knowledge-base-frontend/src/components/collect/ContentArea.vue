<template>
  <main class="content-area">
    <ContentHeader
      :sidebar-items="sidebarItems"
      :active-sidebar-item="activeSidebarItem"
      :is-mobile="isMobile"
      :selected-count="selectedCollections.length"
      :disable-selection="activeSidebarItem === 'discover'"
      :filter-params="filterParams"
      @add-collection="$emit('add-collection')"
      @import="$emit('import')"
      @export="$emit('export')"
      @apply-filters="$emit('apply-filters', $event)"
      @reset-filters="$emit('reset-filters')"
      @select-all-change="$emit('select-all-change', $event)"
      @selection-mode-change="$emit('selection-mode-change', $event)"
      @batch-delete="$emit('batch-delete')"
      @batch-recover="$emit('batch-recover')"
      @batch-permanent-delete="$emit('batch-permanent-delete')"
      @add-collection-success="$emit('add-collection-success', $event)"
    />

    <div class="content-container">
      <CollectionsDisplay
        :collections="Array.isArray(paginatedCollections) ? paginatedCollections : []"
        :filtered-collections-length="filteredCollections.length"
        :total="total"
        :loading="loading"
        :loading-more="loadingMore"
        :has-more="hasMore"
        :load-more-error="loadMoreError"
        :selected-collections="selectedCollections"
        :active-sidebar-item="activeSidebarItem"
        :current-user-id="currentUserId"
        :user-collected-urls="userCollectedUrls"
        :show-selection-mode="showSelectionMode"
        :processing-request="processingRequest"
        @collection-click="$emit('collection-click', $event)"
        @toggle-star="$emit('toggle-star', $event)"
        @delete-collection="$emit('delete-collection', $event)"
        @recover-collection="$emit('recover-collection', $event)"
        @permanent-delete="$emit('permanent-delete', $event)"
        @filter-by-tag="$emit('filter-by-tag', $event)"
        @show-all-collection-tags="$emit('show-all-collection-tags', $event)"
        @preview-image="$emit('preview-image', $event)"
        @preview-text="$emit('preview-text', $event)"
        @preview-link="$emit('preview-link', $event)"
        @preview-video="$emit('preview-video', $event)"
        @load-more="$emit('load-more')"
        @retry-load-more="$emit('retry-load-more')"
        @show-context-menu="handleShowContextMenu"
        @card-menu="handleCardMenu"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @toggle-selection="$emit('toggle-selection', $event)"
        @item-moved="$emit('item-moved')"
      />
    </div>
  </main>
</template>

<script>
import ContentHeader from './ContentHeader.vue';
import CollectionsDisplay from './CollectionsDisplay.vue';

export default {
  name: 'ContentArea',
  components: {
    ContentHeader,
    CollectionsDisplay
  },
  props: {
    sidebarItems: Array,
    activeSidebarItem: String,
    collections: {
      type: Array,
      default: () => []
    },
    filteredCollections: {
      type: Array,
      default: () => []
    },
    paginatedCollections: {
      type: Array,
      default: () => []
    },
    filterParams: {
      type: Object,
      default: () => ({})
    },
    total: {
      type: Number,
      default: 0
    },
    loading: Boolean,
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
    isMobile: Boolean,
    selectedCollections: {
      type: Array,
      default: () => []
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
    },
    processingRequest: {
      type: Boolean,
      default: false
    }
  },
  emits: [
    'add-collection',
    'import',
    'export',
    'apply-filters',
    'reset-filters',
    'select-all-change',
    'selection-mode-change',
    'batch-delete',
    'batch-recover',
    'batch-permanent-delete',
    'add-collection-success',
    'collection-click',
    'toggle-star',
    'delete-collection',
    'recover-collection',
    'permanent-delete',
    'filter-by-tag',
    'show-all-collection-tags',
    'preview-image',
    'preview-text',
    'preview-link',
    'preview-video',
    'show-context-menu',
    'card-menu',
    'selection-change',
    'sort-change',
    'toggle-selection',
    'load-more',
    'retry-load-more',
    'item-moved'
  ],
  methods: {
    handleShowContextMenu(event, collection) {
      this.$emit('show-context-menu', event, collection);
    },

    handleCardMenu(collection) {
      this.$emit('card-menu', collection);
    },

    handleSelectionChange(selection) {
      this.$emit('selection-change', selection);
    },

    handleSortChange({ column, prop, order }) {
      this.$emit('sort-change', { column, prop, order });
    }
  }
}
</script>

<style scoped>
.content-area {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-primary);
  color: var(--text-dark);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  border-radius: var(--border-radius-lg);
}

.content-container {
  flex: 1;
  min-height: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  transition: var(--transition-base);
  background-color: var(--bg-primary);
  overflow: auto;
}
</style>
