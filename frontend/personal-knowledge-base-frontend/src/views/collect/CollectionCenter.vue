<template>
  <page-layout
    :nav-items="navItems"
    :active-nav="activeNav"
    :user="currentUser"
    :notification-count="notificationCount"
    :sidebar-items="sidebarItemsWithBadges"
    :active-sidebar-item="activeSidebarItem"
    :sidebar-collapsed="sidebarCollapsed"
    :sidebar-header-config="sidebarHeaderConfig"
    :sidebar-stats-config="sidebarStatsConfig"
    :show-stats="showSidebarStats"
    :show-collection-card="showCollectionFilters"
    :total-collections="totalCollections"
    :today-collections="todayCollections"
    :unread-collections="unreadCollections"
    :total-tags="totalTags"
    :popular-tags="popularTags"
    :selected-tags="selectedTags"
    :show-tags-filter="activeSidebarItem === 'tags'"
    @toggle-sidebar="toggleSidebar"
    @nav-click="handleNavClick"
    @user-command="handleUserCommand"
    @search="handleSearch"
    @sidebar-item-click="handleSidebarItemClick"
    @show-all-collections="showAllCollections"
    @update:selected-tags="updateSelectedTags"
    @add-tag="addNewTag"
    @toggle-tag="toggleTag"
    @remove-tag="removeTag"
    @remove-all-tags="removeAllTags"
    @show-all-tags="showAllTags"
    @tags-back="handleTagsBack"
    @search-tags="handleTagSearch"
    @apply-filters="handleApplyFilters"
    @reset-filters="handleResetFilters"
  >
    <template v-if="activeSidebarItem === 'my-collections'">
      <my-collections-panel />
    </template>
    <template v-else>
      <ContentArea
        :sidebar-items="sidebarItems"
        :active-sidebar-item="activeSidebarItem"
        :collections="collections"
        :filtered-collections="filteredCollections"
        :paginated-collections="paginatedCollections"
        :popular-tags="popularTags"
        :selected-tags="selectedTags"
        :total="total"
        :loading="loading"
        :loading-more="loadingMore"
        :has-more="hasMore"
        :load-more-error="loadMoreError"
        :is-mobile="isMobile"
        :selected-collections="selectedCollections"
        :current-user-id="currentUserId"
        :user-collected-urls="userCollectedUrls"
        :show-selection-mode="showSelectionMode"
        @update:selected-tags="selectedTags = $event"
        @add-tag="addNewTag"
        @toggle-tag="toggleTag"
        @remove-tag="removeTag"
        @remove-all-tags="removeAllTags"
        @collection-click="handleCollectionClick"
        @toggle-star="handleToggleStar"
        @delete-collection="handleDeleteCollection"
        @recover-collection="handleRecoverCollection"
        @permanent-delete="handlePermanentDelete"
        @add-collection="handleAddCollection"
        @import="handleImport"
        @export="handleExport"
        @show-all-tags="showAllTags"
        @filter-by-tag="filterByTag"
        @preview-image="previewImage"
        @preview-text="previewText"
        @preview-link="previewLink"
        @preview-video="previewVideo"
        @show-context-menu="showContextMenu"
        @card-menu="handleCardMenu"
        @selection-change="handleSelectionChange"
        @select-all-change="handleSelectAllChange"
        @selection-mode-change="handleSelectionModeChange"
        @batch-delete="handleBatchDelete"
        @batch-recover="handleBatchRecover"
        @batch-permanent-delete="handleBatchPermanentDelete"
        @sort-change="handleSortChange"
        @apply-filters="handleApplyFilters"
        @reset-filters="handleResetFilters"
        @toggle-selection="handleToggleSelection"
        @load-more="loadMoreCollections"
        @retry-load-more="retryLoadMore"
      />
    </template>
  </page-layout>
</template>

<script>
import { mapGetters, mapState, mapMutations, mapActions } from 'vuex'
import ContentArea from '@/components/collect/ContentArea.vue';
import PageLayout from '@/components/layout/PageLayout.vue';
import { collectionsApi } from '@/api/collections.js';
import navigationMixin from '@/utils/navigationMixin.js';
import { SIDEBAR_CONFIG } from '@/config/sidebar.js';
import { collectApi } from '@/api/collect.js';
import MyCollectionsPanel from '@/views/collection/MyCollectionsPanel.vue';

export default {
  name: 'CollectionCenter',
  components: {
    PageLayout,
    ContentArea,
    MyCollectionsPanel
  },
  mixins: [navigationMixin],
  data() {
    return {
      activeNav: 'collect',
      sidebarItems: SIDEBAR_CONFIG.collect,
      activeSidebarItem: 'all',
      sidebarCollapsed: false,
      sidebarHeaderConfig: {
        title: '收藏中心',
        shortTitle: '收藏',
        icon: 'fas fa-bookmark'
      },
      sidebarStatsConfig: {
        title: '收藏统计',
        icon: 'fas fa-chart-bar',
        items: [
          { key: 'totalCollects', label: '总收藏数' },
          { key: 'todayCollects', label: '今日新增' },
          { key: 'totalTags', label: '标签总数' },
          { key: 'unreadCollects', label: '未阅读' }
        ]
      },
      isMobile: false,
      selectedCollections: [],
      showSelectionMode: false,
      userCollectedUrls: new Set(),
      loadingMore: false,
      hasMore: true,
      loadMoreError: '',
      lastRequestSignature: '',
      isProcessingRequest: false
    };
  },
  computed: {
    ...mapState('user', ['userInfo']),
    ...mapGetters('user', ['getAvatar', 'getNickname']),
    ...mapState('collect', [
      'collectList',
      'collectTags',
      'selectedCollectTags',
      'collectFilterCondition',
      'loading',
      'searchKeyword'
    ]),
    ...mapGetters('collect', [
      'getTotalCollections',
      'getTodayCollections',
      'getTotalTags',
      'getUnreadCollections',
      'getRecycledCollections'
    ]),
    currentUser() {
      return {
        name: this.getNickname || (this.userInfo && this.userInfo.username) || '未知用户',
        avatar: this.getAvatar || 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg'
      };
    },
    currentUserId() {
      return this.userInfo && this.userInfo.id;
    },
    notificationCount() {
      return this.$store.getters['user/getUnreadNotificationsCount'] || 0;
    },
    sidebarItemsWithBadges() {
      return this.sidebarItems.map(item => {
        if (item.id === 'all') {
          return {
            ...item,
            badge: this.unreadCollections > 0 ? this.unreadCollections : undefined
          };
        }
        if (item.id === 'recycle') {
          return {
            ...item,
            badge: this.getRecycledCollections > 0 ? this.getRecycledCollections : undefined
          };
        }
        return item;
      });
    },
    collections() {
      return Array.isArray(this.collectList) ? this.collectList : [];
    },
    popularTags() {
      return this.collectTags;
    },
    selectedTags() {
      return this.selectedCollectTags;
    },
    filterParams() {
      return this.collectFilterCondition;
    },
    filteredCollections() {
      return this.collections;
    },
    paginatedCollections() {
      return this.filteredCollections;
    },
    totalCollections() {
      return this.getTotalCollections;
    },
    todayCollections() {
      return this.getTodayCollections;
    },
    totalTags() {
      return this.getTotalTags;
    },
    unreadCollections() {
      return this.getUnreadCollections;
    },
    total() {
      return this.$store.state.collect.pagination.total;
    },
    currentPage() {
      return this.$store.state.collect.pagination.currentPage;
    },
    pageSize() {
      return this.$store.state.collect.pagination.pageSize;
    },
    supportsLoadMore() {
      return ['all', 'discover', 'recycle', 'tags'].includes(this.activeSidebarItem);
    },
    showCollectionFilters() {
      return this.activeSidebarItem !== 'my-collections';
    },
    showSidebarStats() {
      return !this.sidebarCollapsed && this.activeSidebarItem !== 'my-collections';
    }
  },
  watch: {
    selectedTags: {
      handler() {
        if (this.activeSidebarItem !== 'tags') {
          return;
        }
        this.loadCollectionByCategory('tags');
      },
      immediate: false
    }
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
    this.handleResize();

    Promise.all([
      this.loadCollections(),
      this.loadCollectTags(),
      this.loadStatistics()
    ]).catch(error => {
      console.error('加载初始数据时发生错误:', error);
    });
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    ...mapActions('collect', [
      'getCollectList',
      'getRecycleBinList',
      'getCollectTags',
      'setSelectedCollectTags',
      'setCollectFilterCondition',
      'resetCollectFilterCondition',
      'getCollectStatistics',
      'setSearchKeyword',
      'deleteCollection',
      'recoverCollection',
      'permanentDeleteCollection',
      'toggleStar'
    ]),
    ...mapMutations('collect', ['SET_PAGINATION', 'SET_COLLECT_LIST']),
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed;
    },
    resetInfiniteState() {
      this.loadingMore = false;
      this.hasMore = true;
      this.loadMoreError = '';
    },
    resetSelectionState() {
      this.selectedCollections = [];
      this.showSelectionMode = false;
    },
    buildListParams(page = 1) {
      const params = {
        pageNum: page,
        pageSize: this.pageSize
      };
      if (this.filterParams.type && this.filterParams.type !== '0') {
        params.sourceType = Number(this.filterParams.type);
      }
      if (this.filterParams.tagId) {
        params.tagId = this.filterParams.tagId;
      }
      if (this.searchKeyword && this.searchKeyword.trim()) {
        params.keyword = this.searchKeyword.trim();
      }
      if (this.filterParams.sortBy) {
        params.sortBy = this.filterParams.sortBy;
      }
      if (this.filterParams.sortOrder) {
        params.sortOrder = this.filterParams.sortOrder;
      }
      return params;
    },
    normalizePagedResponse(response, fallbackPage) {
      const data = response?.data || {};
      const records = Array.isArray(data.records) ? data.records : (Array.isArray(data) ? data : []);
      const current = Number(data.current || fallbackPage || 1);
      const size = Number(data.size || this.pageSize || 10);
      const total = Number(data.total || 0);
      const hasMore = current * size < total;
      return { records, current, size, total, hasMore };
    },
    normalizePublicCollectionResponse(response, fallbackPage) {
      const payload = response?.data || {};
      const responseData = payload?.data && typeof payload.data === 'object' ? payload.data : payload;
      const records = Array.isArray(responseData.records)
        ? responseData.records
        : (Array.isArray(responseData) ? responseData : []);
      const normalizedRecords = records.map(item => ({
        ...item,
        title: item.title || item.name || '未命名收藏集',
        content: item.content || item.description || '',
        coreAbstract: item.coreAbstract || item.description || '',
        sourceType: item.sourceType || 5,
        url: item.url || item.coverImage || item.coverUrl || '',
        sourceUrl: item.sourceUrl || item.coverImage || item.coverUrl || '',
        tags: Array.isArray(item.tags) ? item.tags : (item.tags ? String(item.tags).split(',').map(tag => tag.trim()).filter(Boolean) : []),
        visitCount: item.visitCount || item.viewCount || 0,
        isShared: item.isShared !== undefined ? item.isShared : (item.isPublic !== undefined ? item.isPublic : true),
        createTime: item.createTime || item.createdAt,
        updatedAt: item.updatedAt || item.updateTime,
        positionName: item.positionName || item.name || '公开收藏集',
        collectionType: 'public_collection'
      }));
      const current = Number(responseData.current || responseData.pageNum || responseData.page || fallbackPage || 1);
      const size = Number(responseData.size || responseData.pageSize || this.pageSize || 10);
      const total = Number(responseData.total || normalizedRecords.length || 0);
      return { records: normalizedRecords, current, size, total, hasMore: current * size < total };
    },
    getRequestSignature() {
      return JSON.stringify({
        sidebar: this.activeSidebarItem,
        filterParams: this.filterParams,
        selectedTags: this.selectedTags,
        keyword: this.searchKeyword,
        pageSize: this.pageSize
      });
    },
    async fetchCollectionsPage(page, { append = false } = {}) {
      const signature = this.getRequestSignature();
      if (!append) {
        this.loadMoreError = '';
        this.lastRequestSignature = signature;
        this.$store.commit('collect/SET_PAGINATION', {
          type: 'collectList',
          page,
          pageSize: this.pageSize,
          total: 0
        });
      }
      if (append) {
        if (this.loading || this.loadingMore || !this.hasMore) {
          return;
        }
        this.loadingMore = true;
        this.loadMoreError = '';
      }
      try {
        let pageData;
        if (this.activeSidebarItem === 'discover') {
          const response = await collectionsApi.getPublicCollections({
            pageNum: page,
            pageSize: this.pageSize,
            keyword: this.searchKeyword?.trim() || undefined
          });
          pageData = this.normalizePublicCollectionResponse(response, page);
          if (append) {
            if (signature !== this.lastRequestSignature) {
              return;
            }
            const existingIds = new Set(this.collectList.map(item => item.id));
            const appended = pageData.records.filter(item => !existingIds.has(item.id));
            this.SET_COLLECT_LIST([...this.collectList, ...appended]);
          } else {
            this.SET_COLLECT_LIST(pageData.records);
          }
          this.SET_PAGINATION({ type: 'collectList', page: pageData.current, pageSize: pageData.size, total: pageData.total });
        } else if (this.activeSidebarItem === 'recycle') {
          const response = await this.getRecycleBinList({ pageNum: page, pageSize: this.pageSize });
          pageData = this.normalizePagedResponse(response, page);
          if (append) {
            if (signature !== this.lastRequestSignature) {
              return;
            }
            const existingIds = new Set(this.collectList.map(item => item.id));
            const appended = pageData.records.filter(item => !existingIds.has(item.id));
            this.SET_COLLECT_LIST([...this.collectList, ...appended]);
          }
          this.SET_PAGINATION({ type: 'collectList', page: pageData.current, pageSize: pageData.size, total: pageData.total });
        } else {
          const params = this.buildListParams(page);
          const response = await this.getCollectList(params);
          pageData = this.normalizePagedResponse(response, page);
          if (append) {
            if (signature !== this.lastRequestSignature) {
              return;
            }
            const existingIds = new Set(this.collectList.map(item => item.id));
            const appended = pageData.records.filter(item => !existingIds.has(item.id));
            this.SET_COLLECT_LIST([...this.collectList, ...appended]);
          }
          this.SET_PAGINATION({ type: 'collectList', page: pageData.current, pageSize: pageData.size, total: pageData.total });
        }
        this.hasMore = pageData.hasMore;
        return pageData;
      } catch (error) {
        if (append) {
          this.loadMoreError = '加载更多失败，请重试';
        }
        throw error;
      } finally {
        if (append) {
          this.loadingMore = false;
        }
      }
    },
    async loadMoreCollections() {
      if (!this.supportsLoadMore) {
        return;
      }
      const nextPage = this.currentPage + 1;
      await this.fetchCollectionsPage(nextPage, { append: true });
    },
    async retryLoadMore() {
      this.loadMoreError = '';
      await this.loadMoreCollections();
    },
    handleSidebarItemClick(item) {
      if (this.isProcessingRequest) {
        return;
      }
      this.isProcessingRequest = true;
      this.activeSidebarItem = item.id;
      this.resetInfiniteState();
      this.resetSelectionState();
      if (item.id === 'my-collections') {
        this.isProcessingRequest = false;
        return;
      }
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollectionByCategory(item.id).finally(() => {
        this.isProcessingRequest = false;
      });
    },
    showAllCollections() {
      this.activeSidebarItem = 'all';
      this.resetInfiniteState();
      this.resetSelectionState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollections();
    },
    handleSearch(keyword) {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      const normalizedKeyword = typeof keyword === 'string' ? keyword : this.searchKeyword;
      this.setSearchKeyword(normalizedKeyword || '');
      this.resetInfiniteState();
      this.resetSelectionState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollectionByCategory(this.activeSidebarItem);
    },
    handleTagSearch() {},
    handleTagsBack() {
      this.activeSidebarItem = 'all';
      this.resetInfiniteState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollections();
    },
    updateSelectedTags(tagIds) {
      this.setSelectedCollectTags(tagIds);
    },
    async toggleTag(tag) {
      const tagId = typeof tag === 'object' ? tag.id : tag;
      if (!tagId) {
        return;
      }
      this.setSearchKeyword('');
      this.activeSidebarItem = 'tags';
      const exists = this.selectedTags.includes(tagId);
      const newSelectedTags = exists
        ? this.selectedTags.filter(id => id !== tagId)
        : [...this.selectedTags, tagId];
      this.setSelectedCollectTags(newSelectedTags);
      this.resetInfiniteState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
    },
    async loadCollectionByCategory(category) {
      if (category === 'tags') {
        if (this.selectedTags.length === 0) {
          this.hasMore = false;
          this.SET_COLLECT_LIST([]);
          return;
        }
        await this.fetchCollectionsPage(1);
        return;
      }
      await this.fetchCollectionsPage(1);
    },
    async loadCollections() {
      if (this.activeSidebarItem === 'my-collections') {
        return;
      }
      await this.loadCollectionByCategory(this.activeSidebarItem);
    },
    async loadCollectTags() {
      try {
        await this.getCollectTags();
        await this.loadStatistics();
      } catch (error) {
        console.error('加载标签失败:', error);
      }
    },
    async loadStatistics() {
      try {
        await this.getCollectStatistics();
      } catch (error) {
        console.error('加载统计信息失败:', error);
      }
    },
    async loadUserCollectedUrls() {
      try {
        const urls = new Set();
        let page = 1;
        const pageSize = 100;
        let hasMore = true;
        while (hasMore) {
          const response = await collectApi.getCollectList({ pageNum: page, pageSize });
          const data = response.data || {};
          const records = Array.isArray(data.records) ? data.records : [];
          records.forEach(item => {
            const sourceUrl = item.sourceUrl || item.url;
            if (sourceUrl) {
              urls.add(sourceUrl);
            }
          });
          hasMore = (data.current || page) * (data.size || pageSize) < (data.total || 0);
          page += 1;
        }
        this.userCollectedUrls = urls;
      } catch (error) {
        console.error('加载用户收藏 URL 失败:', error);
      }
    },
    handleResize() {
      this.isMobile = window.innerWidth <= 768;
    },
    handleNavClick(item) {
      this.navigateToNav(item);
    },
    handleUserCommand(command) {
      this.handleUserMenuAction(command);
    },
    resolveCollectionId(collectionOrId) {
      return typeof collectionOrId === 'object' ? collectionOrId?.id : collectionOrId;
    },
    handleCollectionClick(collection) {
      if (!collection?.id) {
        return;
      }
      if (this.activeSidebarItem === 'discover' || collection.collectionType === 'public_collection') {
        this.$router.push(`/collections/${collection.id}`);
        return;
      }
      this.$router.push(`/collections/${collection.id}/workspace`);
    },
    async refreshAfterMutation() {
      await this.loadCollectionByCategory(this.activeSidebarItem);
      await this.loadStatistics();
    },
    async handleToggleStar(collection) {
      const collectionId = this.resolveCollectionId(collection);
      if (!collectionId) {
        return;
      }
      try {
        await this.toggleStar(collectionId);
        await this.refreshAfterMutation();
      } catch (error) {
        this.$message.error('星标切换失败');
      }
    },
    async handleDeleteCollection(collection) {
      const collectionId = this.resolveCollectionId(collection);
      if (!collectionId) {
        return;
      }
      try {
        await this.deleteCollection(collectionId);
        this.$message.success('删除成功');
        await this.refreshAfterMutation();
      } catch (error) {
        this.$message.error('删除失败');
      }
    },
    async handleRecoverCollection(collection) {
      const collectionId = this.resolveCollectionId(collection);
      if (!collectionId) {
        return;
      }
      try {
        await this.recoverCollection(collectionId);
        this.$message.success('恢复成功');
        this.resetSelectionState();
        await this.refreshAfterMutation();
      } catch (error) {
        this.$message.error('恢复失败');
      }
    },
    async handlePermanentDelete(collection) {
      const collectionId = this.resolveCollectionId(collection);
      if (!collectionId) {
        return;
      }
      try {
        await this.permanentDeleteCollection(collectionId);
        this.$message.success('永久删除成功');
        this.resetSelectionState();
        await this.refreshAfterMutation();
      } catch (error) {
        this.$message.error('永久删除失败');
      }
    },
    handleAddCollection() {
      this.$message.info('添加收藏功能暂未实现');
    },
    handleImport() {
      this.$message.info('导入功能入口已保留，可继续接入批量导入面板');
    },
    handleExport() {
      const exportData = JSON.stringify(this.collections, null, 2);
      const blob = new Blob([exportData], { type: 'application/json;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `collections-${Date.now()}.json`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      this.$message.success('导出成功');
    },
    filterByTag(tag) {
      const tagId = typeof tag === 'object' ? tag.id : tag;
      if (!tagId) {
        return;
      }
      this.activeSidebarItem = 'tags';
      this.setSelectedCollectTags([tagId]);
    },
    previewImage() {
      this.$message.info('图片预览功能暂未实现');
    },
    previewText(collection) {
      this.handleCollectionClick(collection);
    },
    previewLink(collection) {
      this.handleCollectionClick(collection);
    },
    previewVideo(collection) {
      this.handleCollectionClick(collection);
    },
    showContextMenu() {
      this.$message.info('右键菜单功能暂未实现');
    },
    handleSelectionChange(selection) {
      this.selectedCollections = Array.isArray(selection) ? selection.map(item => item.id) : [];
    },
    handleSelectAllChange(checked) {
      if (!checked) {
        this.selectedCollections = [];
        return;
      }
      this.selectedCollections = this.filteredCollections.map(item => item.id);
    },
    handleSelectionModeChange(visible) {
      this.showSelectionMode = visible;
      if (!visible) {
        this.selectedCollections = [];
      }
    },
    handleBatchDelete() {},
    handleBatchRecover() {},
    handleBatchPermanentDelete() {},
    handleSortChange(sortOptions) {
      this.setCollectFilterCondition(sortOptions || {});
      this.resetInfiniteState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollectionByCategory(this.activeSidebarItem);
    },
    handleApplyFilters(filters) {
      this.setCollectFilterCondition(filters || {});
      this.resetInfiniteState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollectionByCategory(this.activeSidebarItem);
    },
    handleResetFilters() {
      this.resetCollectFilterCondition();
      this.resetInfiniteState();
      this.SET_PAGINATION({ type: 'collectList', page: 1, pageSize: this.pageSize, total: 0 });
      this.loadCollectionByCategory(this.activeSidebarItem);
    },
    showAllTags() {},
    addNewTag() {},
    removeTag() {},
    removeAllTags() {
      this.setSelectedCollectTags([]);
      this.resetCollectFilterCondition();
      this.showAllCollections();
    },
    handleCardMenu() {},
    handleToggleSelection() {}
  }
}
</script>
