<template>
  <div class="page-layout">
    <!-- 头部导航栏 -->
    <top-navbar
      :nav-items="navItems"
      :active-nav="activeNav"
      :user="user"
      :notification-count="notificationCount"
      @toggle-sidebar="toggleSidebar"
      @nav-click="handleNavClick"
      @user-command="handleUserCommand"
      @search="handleSearch"
    />

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 侧边栏 -->
      <sidebar-navigation
        v-if="showSidebar"
        :sidebar-items="sidebarItems"
        :active-sidebar-item="activeSidebarItem"
        :sidebar-collapsed="sidebarCollapsed"
        :header-config="sidebarHeaderConfig"
        :stats-config="sidebarStatsConfig"
        :show-stats="!sidebarCollapsed"
        :show-collection-card="showCollectionCard"
        :total-collections="totalCollections"
        :today-collections="todayCollections"
        :unread-collections="unreadCollections"
        :total-tags="totalTags"
        :knowledge-stats="knowledgeStats"
        :statistics="statistics"
        :popular-tags="popularTags"
        :selected-tags="selectedTags"
        :show-tags-filter="showTagsFilter"
        :show-my-collections="showMyCollections"
        @toggle-sidebar="toggleSidebar"
        @sidebar-item-click="handleSidebarItemClick"
        @show-all-collections="showAllCollections"
        @select="handleSidebarSelect"
        @view-change="handleViewChange"
        @update:selected-tags="$emit('update:selected-tags', $event)"
        @add-tag="$emit('add-tag', $event)"
        @toggle-tag="$emit('toggle-tag', $event)"
        @remove-tag="$emit('remove-tag', $event)"
        @remove-all-tags="$emit('remove-all-tags')"
        @show-all-tags="$emit('show-all-tags', $event)"
        @tags-back="$emit('tags-back', $event)"
        @search-tags="$emit('search-tags', $event)"
        @apply-filters="$emit('apply-filters', $event)"
        @select-collection="$emit('select-collection', $event)"
        @edit-collection="$emit('edit-collection', $event)"
        @collection-created="$emit('collection-created', $event)"
        @new-note="$emit('new-note')"
      />

      <!-- 页面内容 -->
      <div class="content-wrapper" :class="{ 'full-width': !showSidebar || sidebarCollapsed }">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
import TopNavbar from '@/components/layout/TopNavbar.vue';
import SidebarNavigation from '@/components/layout/SidebarNavigation.vue';

export default {
  name: 'PageLayout',
  components: {
    TopNavbar,
    SidebarNavigation
  },
  props: {
    // 导航相关
    navItems: {
      type: Array,
      required: true,
      default: () => [
        { id: 'collect', text: '收藏中心', icon: 'fas fa-bookmark', url: '/collect/center' },
        { id: 'creation', text: '创作中心', icon: 'fas fa-edit', url: '/creation/workspace' },
        { id: 'search', text: '检索中心', icon: 'fas fa-search', url: '/search/center' },
        { id: 'personal', text: '个人中心', icon: 'fas fa-user', url: '/personal/center' }
      ]
    },
    activeNav: {
      type: String,
      required: true
    },
    user: {
      type: Object,
      required: true
    },
    notificationCount: {
      type: Number,
      default: 0
    },
    
    // 侧边栏相关
    showSidebar: {
      type: Boolean,
      default: true
    },
    sidebarItems: {
      type: Array,
      default: () => []
    },
    activeSidebarItem: {
      type: String,
      default: ''
    },
    sidebarCollapsed: {
      type: Boolean,
      default: false
    },
    sidebarHeaderConfig: {
      type: Object,
      default: () => ({
        title: '收藏中心',
        shortTitle: '收藏',
        icon: 'fas fa-bookmark'
      })
    },
    sidebarStatsConfig: {
      type: Object,
      default: () => ({
        title: '统计信息',
        icon: 'fas fa-chart-bar',
        items: []
      })
    },
    showCollectionCard: {
      type: Boolean,
      default: false
    },
    totalCollections: {
      type: Number,
      default: 0
    },
    todayCollections: {
      type: Number,
      default: 0
    },
    unreadCollections: {
      type: Number,
      default: 0
    },
    totalTags: {
      type: Number,
      default: 0
    },
    knowledgeStats: {
      type: Object,
      default: () => ({})
    },
    statistics: {
      type: Object,
      default: () => ({})
    },
    // 标签相关
    popularTags: {
      type: Array,
      default: () => []
    },
    selectedTags: {
      type: Array,
      default: () => []
    },
    showTagsFilter: {
      type: Boolean,
      default: false
    },
    showMyCollections: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    toggleSidebar() {
      this.$emit('toggle-sidebar');
    },
    
    handleNavClick(item) {
      // 根据导航项跳转到对应中心
      switch(item.id) {
        case 'collect':
          this.$router.push('/collect/center');
          break;
        case 'creation':
          this.$router.push('/creation/workspace');
          break;
        case 'search':
          this.$router.push('/search/center');
          break;
        case 'personal':
          this.$router.push('/personal/center');
          break;
        default:
          this.$emit('nav-click', item);
      }
    },
    
    handleUserCommand(command) {
      this.$emit('user-command', command);
    },
    
    handleSearch(keyword) {
      this.$emit('search', keyword);
    },
    
    handleSidebarItemClick(item) {
      this.$emit('sidebar-item-click', item);
    },
    
    showAllCollections() {
      this.$emit('show-all-collections');
    },
    
    handleSidebarSelect(item) {
      this.$emit('select', item);
    },
    
    handleViewChange(view) {
      this.$emit('view-change', view);
    }
  }
};
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  display: flex;
  flex: 1;
  min-height: calc(100vh - 80px);
  overflow-x: hidden; /* 防止水平滚动 */
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  transition: all var(--transition-normal);
  height: calc(100vh - 80px);
  min-width: 0;
  padding: 20px;
}

.content-wrapper.full-width {
  padding: 20px;
  overflow-x: hidden;
}

/* 桌面端-only布局 - 移除移动端响应式代码 */
</style>