<template>
  <PageLayout
    :nav-items="navItems"
    :active-nav="activeNav"
    :user="user"
    :notification-count="notificationCount"
    :sidebar-collapsed="sidebarCollapsed"
    :sidebar-items="sidebarItems"
    :active-sidebar-item="activeSidebarItem"
    :sidebar-header-config="sidebarHeaderConfig"
    :show-stats="showSidebarStats"
    :sidebar-stats-config="sidebarStatsConfig"
    :total-collections="totalCollections"
    :total-tags="popularTags.length"
    :show-search-filter="showSearchFilter"
    :search-placeholder="searchPlaceholder"
    :search-value="searchKeyword"
    :show-sort-filter="showSortFilter"
    :sort-options="sortOptions"
    :selected-sort="selectedSort"
    :show-tags-filter="showTagsFilter"
    :popular-tags="popularTags"
    :selected-tags="selectedTags"
    :show-my-collections="showMyCollections"
    @toggle-sidebar="toggleSidebar"
    @sidebar-item-click="handleSidebarItemClick"
    @search="handleSearch"
    @sort-change="handleSortChange"
    @tag-toggle="handleTagToggle"
    @show-all-collections="handleShowAllCollections"
    @apply-filters="handleApplyFilters"
    @reset-filters="handleResetFilters"
  >
    <template v-if="activeSidebarItem === 'my-collections'">
      <collection-list-page />
    </template>
    <template v-else>
      <ContentArea
        :sidebar-items="sidebarItems"
        :active-sidebar-item="activeSidebarItem"
        :collections="collections"
        :filtered-collections="collections"
        :paginated-collections="collections"
        :total="totalCollections"
        :loading="isLoading"
        :has-more="hasMore"
        :show-selection-mode="selectionMode"
        :selected-collections="selectedItems"
        :filter-params="filters"
        @collection-click="handleCollectionClick"
        @toggle-selection="toggleSelection"
        @load-more="loadMoreCollections"
        @batch-delete="handleBatchDelete"
        @selection-change="handleSelectionChange"
        @apply-filters="handleApplyFilters"
        @select-all-change="handleSelectAllChange"
        @selection-mode-change="handleSelectionModeChange"
        @add-collection-success="handleAddCollectionSuccess"
      />
    </template>
  </PageLayout>
</template>

<script>
import PageLayout from '@/components/layout/PageLayout.vue';
import ContentArea from '@/components/collect/ContentArea.vue';
import navigationMixin from '@/utils/navigationMixin.js';
import { SIDEBAR_CONFIG } from '@/config/sidebar.js';
import { collectApi } from '@/api/collect.js';
import { collectionsApi } from '@/api/collections.js';
import CollectionListPage from '@/views/collection/CollectionListPage.vue';

export default {
  name: 'CollectionCenter',
  components: {
    PageLayout,
    ContentArea,
    CollectionListPage
  },
  mixins: [navigationMixin],
  data() {
    return {
      activeNav: 'collect',
      sidebarCollapsed: false,
      sidebarItems: SIDEBAR_CONFIG.collect,
      sidebarTitle: '收藏中心',
      sidebarIcon: 'fas fa-star',
      activeSidebarItem: 'all',
      collections: [],
      isLoading: false,
      loadingMessage: '正在加载收藏内容...',
      hasMore: false,
      searchKeyword: '',
      selectedSort: 'latest',
      selectedTags: [],
      popularTags: [],
      selectionMode: false,
      selectedItems: [],
      processingBatch: false,
      isProcessingRequest: false,
      currentPage: 1,
      pageSize: 20,
      totalCollections: 0,
      filters: {
        type: undefined,
        startDate: null,
        endDate: null
      }
    };
  },
  computed: {
    showSearchFilter() {
      return ['all', 'discover', 'recycle', 'tags'].includes(this.activeSidebarItem);
    },
    showSortFilter() {
      return ['all', 'discover', 'recycle', 'tags'].includes(this.activeSidebarItem);
    },
    showTagsFilter() {
      return this.activeSidebarItem === 'tags';
    },
    showMyCollections() {
      return this.activeSidebarItem === 'my-collections';
    },
    showSidebarStats() {
      return !this.sidebarCollapsed && this.activeSidebarItem !== 'my-collections';
    },
    sidebarHeaderConfig() {
      return {
        title: '收藏中心',
        shortTitle: '收藏',
        icon: 'fas fa-star'
      };
    },
    sidebarStatsConfig() {
      return {
        title: '统计信息',
        icon: 'fas fa-chart-bar',
        items: [
          { label: '总收藏', key: 'totalCollects' },
          { label: '标签数', key: 'totalTags' },
          { label: '当前列表', key: 'currentList', count: this.collections.length }
        ]
      };
    },
    searchPlaceholder() {
      const map = {
        all: '搜索收藏标题、摘要或关键词',
        discover: '搜索推荐收藏内容',
        tags: '搜索已打标签内容',
        recycle: '搜索回收站内容'
      }
      return map[this.activeSidebarItem] || '搜索收藏内容'
    },
    sortOptions() {
      return [
        { label: '最近更新', value: 'latest' },
        { label: '创建时间', value: 'created' },
        { label: '标题名称', value: 'name' }
      ]
    }
  },
  watch: {
    '$route.query.tab': {
      immediate: true,
      handler(tab) {
        if (tab === 'my-collections') {
          this.activeSidebarItem = 'my-collections'
          return
        }
        if (this.activeSidebarItem === 'my-collections') {
          this.activeSidebarItem = 'all'
        }
      }
    }
  },
  mounted() {
    if (this.activeSidebarItem !== 'my-collections') {
      this.loadCollections()
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    async handleSidebarItemClick(item) {
      this.activeSidebarItem = item.id;
      this.resetSelectionState();
      this.isProcessingRequest = false;
      if (item.id === 'my-collections') {
        return;
      }
      this.resetInfiniteState();
      await this.loadCollections();
    },
    async handleShowAllCollections() {
      this.activeSidebarItem = 'my-collections'
      this.$router.push('/collections/list')
    },
    async handleSearch(keyword) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      this.searchKeyword = typeof keyword === 'string' ? keyword : this.searchKeyword;
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    async handleSortChange(sortValue) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      this.selectedSort = sortValue;
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    async handleTagToggle(tag) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      const tagName = typeof tag === 'string' ? tag : tag?.name;
      if (!tagName) {
        return;
      }
      if (this.selectedTags.includes(tagName)) {
        this.selectedTags = this.selectedTags.filter(item => item !== tagName);
      } else {
        this.selectedTags = [...this.selectedTags, tagName];
      }
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    async handleApplyFilters(filters) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      this.filters = {
        ...this.filters,
        ...filters
      };
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    async handleResetFilters() {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      this.filters = {
        type: undefined,
        startDate: null,
        endDate: null
      };
      this.selectedTags = [];
      this.selectedSort = 'latest';
      this.searchKeyword = '';
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    async loadCollections() {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      await this.fetchCollectionsPage(1);
    },
    async loadMoreCollections() {
      if (!this.hasMore || this.isLoading || this.activeSidebarItem === 'my-collections') {
        return;
      }
      await this.fetchCollectionsPage(this.currentPage + 1, true);
    },
    async fetchCollectionsPage(page, append = false) {
      this.isLoading = true;
      this.loadingMessage = append ? '正在加载更多内容...' : '正在加载收藏内容...';
      try {
        const isDiscover = this.activeSidebarItem === 'discover';
        const params = {
          pageNum: page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          sortBy: this.selectedSort,
          tags: this.selectedTags.length ? this.selectedTags.join(',') : undefined,
          ...this.filters
        };
        let response;
        if (isDiscover) {
          response = await collectionsApi.getPublicCollections(params);
        } else {
          response = await collectApi.getCollectList({ ...params, category: this.activeSidebarItem });
        }
        const payload = response?.data?.data || response?.data || {};
        const records = Array.isArray(payload.records) ? payload.records : [];
        this.collections = append ? [...this.collections, ...records] : records;
        this.currentPage = page;
        this.pageSize = payload.size || this.pageSize;
        this.totalCollections = payload.total || this.collections.length;
        this.hasMore = this.collections.length < this.totalCollections;
        this.popularTags = this.extractPopularTags(this.collections);
      } catch (error) {
        console.error('加载收藏内容失败', error);
        this.collections = append ? this.collections : [];
        this.hasMore = false;
      } finally {
        this.isLoading = false;
      }
    },
    extractPopularTags(collections) {
      const tagMap = new Map();
      collections.forEach(collection => {
        const tags = Array.isArray(collection?.tags) ? collection.tags : [];
        tags.forEach(tag => {
          const key = tag?.name || tag;
          if (!key) {
            return;
          }
          tagMap.set(key, (tagMap.get(key) || 0) + 1);
        });
      });
      return Array.from(tagMap.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 12)
        .map(([name]) => name);
    },
    handleCollectionClick(item) {
      if (!item?.id) {
        return;
      }
      const isCollectionItem = item.sourceType !== undefined && item.sourceType !== null;
      if (isCollectionItem) {
        this.$router.push(`/creation/collection-items/${item.id}/note/create`);
      } else {
        this.$router.push(`/collections/${item.id}`);
      }
    },
    handleSelectAllChange(selectAll) {
      if (selectAll) {
        this.selectedItems = [...this.collections];
      } else {
        this.selectedItems = [];
      }
    },
    handleSelectionModeChange(enabled) {
      this.selectionMode = enabled;
      if (!enabled) {
        this.selectedItems = [];
      }
    },
    handleAddCollectionSuccess() {
      this.resetInfiniteState();
      this.loadCollections();
    },
    toggleSelection() {
      this.selectionMode = !this.selectionMode;
      if (!this.selectionMode) {
        this.selectedItems = [];
      }
    },
    handleSelectionChange(items) {
      this.selectedItems = items;
    },
    resetSelectionState() {
      this.selectionMode = false;
      this.selectedItems = [];
    },
    resetInfiniteState() {
      this.currentPage = 1;
      this.totalCollections = 0;
      this.hasMore = false;
    },
    async handleBatchDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要删除的收藏项')
        return
      }
      this.$confirm(`确定要删除选中的 ${this.selectedItems.length} 个收藏项？删除后可前往回收站恢复。`, '批量删除', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.processingBatch = true
        try {
          const ids = this.selectedItems.map(item => item.id)
          await collectApi.batchDeleteCollect(ids)
          this.$message.success(`已删除 ${ids.length} 个收藏项`)
          this.resetSelectionState()
          this.resetInfiniteState()
          await this.fetchCollectionsPage(1)
        } catch (error) {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        } finally {
          this.processingBatch = false
        }
      }).catch(() => {})
    },
    async handleBatchProcess() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要处理的收藏项')
        return
      }
      this.$confirm(`确定将选中的 ${this.selectedItems.length} 个收藏项标记为"已加工"？`, '批量处理', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        this.processingBatch = true
        try {
          const ids = this.selectedItems.map(item => item.id)
          await collectApi.batchUpdateCollectionItemStatus({ itemIds: ids, digestStatus: 'digested' })
          this.$message.success(`已处理 ${ids.length} 个收藏项`)
          this.resetSelectionState()
          this.resetInfiniteState()
          await this.fetchCollectionsPage(1)
        } catch (error) {
          console.error('批量处理失败:', error)
          this.$message.error('批量处理失败')
        } finally {
          this.processingBatch = false
        }
      }).catch(() => {})
    }
  }
};
</script>
