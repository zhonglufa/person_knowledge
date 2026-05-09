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
    :total-tags="userTagsCount"
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
    @tags-back="handleTagsBack"
    @show-all-collections="handleShowAllCollections"
    @apply-filters="handleApplyFilters"
    @reset-filters="handleResetFilters"
  >
    <template v-if="activeSidebarItem === 'my-collections'">
      <collection-list-page :key="routeKey" />
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
        :current-user-id="currentUserId"
        :processing-request="isProcessingRequest || processingBatch"
        @collection-click="handleCollectionClick"
        @toggle-star="handleToggleStar"
        @delete-collection="handleDeleteCollection"
        @recover-collection="handleRecoverCollection"
        @permanent-delete="handlePermanentDelete"
        @toggle-selection="toggleSelection"
        @filter-by-tag="handleFilterByTag"
        @load-more="loadMoreCollections"
        @batch-delete="handleBatchDelete"
        @batch-recover="handleBatchRecover"
        @batch-permanent-delete="handleBatchPermanentDelete"
        @selection-change="handleSelectionChange"
        @apply-filters="handleApplyFilters"
        @select-all-change="handleSelectAllChange"
        @selection-mode-change="handleSelectionModeChange"
        @add-collection-success="handleAddCollectionSuccess"
        @item-moved="handleItemMoved"
      />
    </template>
  </PageLayout>
</template>

<script>
import { mapState } from 'vuex';
import PageLayout from '@/components/layout/PageLayout.vue';
import ContentArea from '@/components/collect/ContentArea.vue';
import navigationMixin from '@/utils/navigationMixin.js';
import { SIDEBAR_CONFIG } from '@/config/sidebar.js';
import { collectApi } from '@/api/collect.js';
import { collectionsApi } from '@/api/collections.js';
import { getUserTags, getPopularTags } from '@/api/tag';
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
      userTagsCount: 0,
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
    ...mapState('user', ['userInfo']),
    currentUserId() {
      return this.userInfo?.id || null;
    },
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
          { label: '标签数', key: 'totalTags' }
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
    },
    routeKey() {
      return this.$route.query.tab || 'default'
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
        if (tab === 'discover') {
          this.activeSidebarItem = 'discover'
          return
        }
        if (this.activeSidebarItem === 'my-collections' || this.activeSidebarItem === 'discover') {
          this.activeSidebarItem = 'all'
        }
      }
    }
  },
  mounted() {
    if (this.activeSidebarItem !== 'my-collections') {
      this.loadPopularTags();
      this.loadCollections();
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
    async handleTagToggle(tagId) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      if (tagId === undefined || tagId === null) {
        return;
      }

      const normalizedId = Number(tagId);
      if (!Number.isFinite(normalizedId)) {
        return;
      }

      if (this.selectedTags.includes(normalizedId)) {
        this.selectedTags = this.selectedTags.filter(item => item !== normalizedId);
      } else {
        this.selectedTags = [...this.selectedTags, normalizedId];
      }
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    handleTagsBack() {
      this.activeSidebarItem = 'all';
      this.selectedTags = [];
      this.popularTags = [];
      this.resetInfiniteState();
      this.loadCollections();
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
        const tagIds = this.normalizeTagIds(this.selectedTags);
        const params = {
          pageNum: page,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          sortBy: this.normalizeSortBy(this.selectedSort),
          sortOrder: 'desc',
          tagId: tagIds.length > 0 ? tagIds[0] : undefined,
          tagIds: tagIds.length > 0 ? tagIds.join(',') : undefined,
          startDate: this.normalizeDateForApi(this.filters?.startDate),
          endDate: this.normalizeDateForApi(this.filters?.endDate),
          sourceType: this.filters?.type ?? undefined
        };
        let response;
        if (isDiscover) {
          response = await collectionsApi.getPublicCollections(params);
        } else if (this.activeSidebarItem === 'recycle') {
          response = await collectApi.getRecycleBin(params);
        } else {
          response = await collectApi.getCollectList({ ...params, category: this.activeSidebarItem });
        }
        // 适配响应拦截器解包后的数据结构：拦截器已将 {code, message, data} 解包
        const payload = response?.data ?? response ?? {};
        const records = Array.isArray(payload.records) ? payload.records : (Array.isArray(payload) ? payload : []);
        this.collections = append ? [...this.collections, ...records] : records;
        this.currentPage = page;
        this.pageSize = payload.size || this.pageSize;
        this.totalCollections = payload.total || this.collections.length;
        this.hasMore = this.collections.length < this.totalCollections;
        if (!Array.isArray(this.popularTags) || this.popularTags.length === 0) {
          this.popularTags = this.extractPopularTags(this.collections);
        }
        await this.loadUserTagsCount();
      } catch (error) {
        console.error('加载收藏内容失败', error);
        this.$message.error(error?.message || '加载收藏内容失败，请稍后重试');
        this.collections = append ? this.collections : [];
        this.hasMore = false;
      } finally {
        this.isLoading = false;
      }
    },
    async loadPopularTags() {
      try {
        const response = await getPopularTags(50);
        const tags = Array.isArray(response?.data ?? response ?? []) ? (response?.data ?? response ?? []) : [];
        this.popularTags = tags.map(tag => ({
          ...tag,
          count: tag.count ?? tag.usageCount ?? 0
        }));
      } catch (error) {
        console.error('获取热门标签失败:', error);
        this.popularTags = this.extractPopularTags(this.collections);
      }
    },
    async loadUserTagsCount() {
      try {
        const response = await getUserTags();
        this.userTagsCount = response.data?.length || 0;
      } catch (error) {
        console.error('获取用户标签数量失败:', error);
        this.userTagsCount = 0;
      }
    },
    extractPopularTags(collections) {
      const tagMap = new Map();
      collections.forEach(collection => {
        const tags = Array.isArray(collection?.tags) ? collection.tags : [];
        tags.forEach(tag => {
          const name = typeof tag === 'string' ? tag : tag?.name;
          if (!name) {
            return;
          }
          tagMap.set(name, (tagMap.get(name) || 0) + 1);
        });
      });
      return Array.from(tagMap.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 12)
        .map(([name, count]) => ({ id: name, name, count }));
    },
    handleCollectionClick(item) {
      if (!item?.id) {
        return;
      }
      const isCollectionItem = item.sourceType !== undefined && item.sourceType !== null;
      const query = { from: this.activeSidebarItem };
      if (isCollectionItem) {
        this.$router.push({
          path: `/creation/collection-items/${item.id}/note/create`,
          query
        });
      } else {
        this.$router.push({
          path: `/collections/${item.id}`,
          query
        });
      }
    },
    async handleFilterByTag(tag) {
      const tagId = tag?.id ?? tag;
      if (tagId === undefined || tagId === null) {
        return;
      }
      const normalizedId = Number(tagId);
      if (!Number.isFinite(normalizedId)) {
        return;
      }
      if (!this.selectedTags.includes(normalizedId)) {
        this.selectedTags = [normalizedId];
      }
      if (this.activeSidebarItem !== 'tags') {
        this.activeSidebarItem = 'tags';
        this.popularTags = [];
      }
      this.resetInfiniteState();
      await this.fetchCollectionsPage(1);
    },
    handleSelectAllChange(selectAll) {
      if (selectAll) {
        this.selectedItems = this.collections.map(item => item.id).filter(id => id !== undefined && id !== null);
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
    handleSelectionChange(items) {
      this.selectedItems = Array.isArray(items)
        ? items.filter(item => item !== undefined && item !== null)
        : [];
    },
    async handleToggleStar(collection) {
      const id = collection?.id;
      if (!id) {
        return;
      }
      try {
        const response = await collectApi.toggleStar(id);
        const nextStar = response?.data?.isStar;
        const target = this.collections.find(item => item?.id === id);
        if (target && typeof nextStar === 'boolean') {
          target.isStar = nextStar;
          target.isStarred = nextStar;
          target.starred = nextStar;
        } else if (target) {
          const current = typeof target.isStar === 'boolean'
            ? target.isStar
            : (typeof target.isStarred === 'boolean' ? target.isStarred : target.starred);
          const next = !current;
          target.isStar = next;
          target.isStarred = next;
          target.starred = next;
        }
        this.$message.success('已更新星标状态');
      } catch (error) {
        console.error('切换星标失败:', error);
        this.$message.error('切换星标失败');
      }
    },
    async handleDeleteCollection(collection) {
      const id = collection?.id;
      const title = collection?.title || '该收藏项';
      if (!id) {
        return;
      }

      if (this.isProcessingRequest || this.processingBatch) {
        return;
      }

      if (this.currentUserId && collection?.userId && this.currentUserId !== collection.userId) {
        this.$message.error('无权限删除该收藏项');
        return;
      }

      this.$confirm(`确定要删除"${title}"吗？删除后可前往回收站恢复。`, '删除', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.isProcessingRequest = true;
        try {
          await collectApi.deleteCollect(id);
          this.$message.success('删除成功');
          if (this.selectedItems.includes(id)) {
            this.selectedItems = this.selectedItems.filter(itemId => itemId !== id);
          }
          this.resetInfiniteState();
          await this.fetchCollectionsPage(1);
        } catch (error) {
          console.error('删除收藏项失败:', error);
          this.$message.error('删除失败');
        } finally {
          this.isProcessingRequest = false;
        }
      }).catch(() => {});
    },
    async handleRecoverCollection(collection) {
      const id = collection?.id;
      if (!id) {
        return;
      }
      this.isProcessingRequest = true;
      try {
        await collectApi.recoverCollect(id);
        this.$message.success('恢复成功');
        if (this.selectedItems.includes(id)) {
          this.selectedItems = this.selectedItems.filter(itemId => itemId !== id);
        }
        this.resetInfiniteState();
        await this.fetchCollectionsPage(1);
      } catch (error) {
        console.error('恢复收藏项失败:', error);
        this.$message.error('恢复失败');
      } finally {
        this.isProcessingRequest = false;
      }
    },
    async handlePermanentDelete(collection) {
      const id = collection?.id;
      const title = collection?.title || '该收藏项';
      if (!id) {
        return;
      }
      this.$confirm(`确定要永久删除"${title}"吗？此操作不可恢复！`, '永久删除', {
        confirmButtonText: '永久删除',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        this.isProcessingRequest = true;
        try {
          await collectApi.permanentDeleteCollect(id);
          this.$message.success('永久删除成功');
          if (this.selectedItems.includes(id)) {
            this.selectedItems = this.selectedItems.filter(itemId => itemId !== id);
          }
          this.resetInfiniteState();
          await this.fetchCollectionsPage(1);
        } catch (error) {
          console.error('永久删除收藏项失败:', error);
          this.$message.error('永久删除失败');
        } finally {
          this.isProcessingRequest = false;
        }
      }).catch(() => {});
    },
    async handleBatchRecover() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要恢复的收藏项');
        return;
      }
      this.$confirm(`确定要恢复选中的 ${this.selectedItems.length} 个收藏项？`, '批量恢复', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        this.processingBatch = true;
        try {
          const ids = this.selectedItems;
          await collectApi.batchRecoverCollect(ids);
          this.$message.success(`已恢复 ${ids.length} 个收藏项`);
          this.resetSelectionState();
          this.resetInfiniteState();
          await this.fetchCollectionsPage(1);
        } catch (error) {
          console.error('批量恢复失败:', error);
          this.$message.error('批量恢复失败');
        } finally {
          this.processingBatch = false;
        }
      }).catch(() => {});
    },
    async handleBatchPermanentDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要永久删除的收藏项');
        return;
      }
      this.$confirm(`确定要永久删除选中的 ${this.selectedItems.length} 个收藏项？此操作不可恢复！`, '批量永久删除', {
        confirmButtonText: '永久删除',
        cancelButtonText: '取消',
        type: 'error'
      }).then(async () => {
        this.processingBatch = true;
        try {
          const ids = this.selectedItems;
          await collectApi.batchPermanentDeleteCollect(ids);
          this.$message.success(`已永久删除 ${ids.length} 个收藏项`);
          this.resetSelectionState();
          this.resetInfiniteState();
          await this.fetchCollectionsPage(1);
        } catch (error) {
          console.error('批量永久删除失败:', error);
          this.$message.error('批量永久删除失败');
        } finally {
          this.processingBatch = false;
        }
      }).catch(() => {});
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
          const ids = this.selectedItems
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
    toggleSelection(collectionId) {
      if (collectionId === undefined || collectionId === null) {
        this.selectionMode = !this.selectionMode;
        if (!this.selectionMode) {
          this.selectedItems = [];
        }
        return;
      }

      if (!this.selectionMode) {
        this.selectionMode = true;
      }

      const index = this.selectedItems.indexOf(collectionId);
      if (index >= 0) {
        this.selectedItems.splice(index, 1);
      } else {
        this.selectedItems.push(collectionId);
      }
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
          const ids = this.selectedItems
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
    },
    normalizeSortBy(sortValue) {
      const sortMap = {
        latest: 'updatedAt',
        created: 'createdAt',
        name: 'title'
      };
      return sortMap[sortValue] || 'createdAt';
    },
    normalizeTagId(selectedTags) {
      const tagIds = this.normalizeTagIds(selectedTags);
      return tagIds.length > 0 ? tagIds[0] : undefined;
    },
    normalizeTagIds(selectedTags) {
      const tags = Array.isArray(selectedTags) ? selectedTags : [];
      return tags
        .map(tagValue => {
          if (tagValue == null) {
            return undefined;
          }

          const numericId = Number(tagValue);
          if (Number.isFinite(numericId)) {
            return numericId;
          }

          const matched = Array.isArray(this.popularTags)
            ? this.popularTags.find(tag => tag && typeof tag === 'object' && String(tag.id) === String(tagValue))
            : null;
          const normalizedId = Number(matched?.id);
          return Number.isFinite(normalizedId) ? normalizedId : undefined;
        })
        .filter(id => id !== undefined && id !== null);
    },
    normalizeDateForApi(value) {
      if (!value) {
        return undefined;
      }
      if (typeof value === 'string') {
        return value.slice(0, 10);
      }
      const date = value instanceof Date ? value : new Date(value);
      if (Number.isNaN(date.getTime())) {
        return undefined;
      }
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, '0');
      const dd = String(date.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    },
    handleItemMoved() {
      this.resetInfiniteState();
      this.loadCollections();
    }
  }
};
</script>
