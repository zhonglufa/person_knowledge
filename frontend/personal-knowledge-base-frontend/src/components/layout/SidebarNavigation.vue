<template>
  <aside class="sidebar">
    <div class="sidebar-content">
      <!-- 标签过滤组件（仅在收藏中心展示） -->
      <div v-if="showTagsFilter" class="sidebar-tags-filter">
        <TagsFilter
          :popular-tags="popularTags"
          :selected-tags="selectedTags"
          @update:selected-tags="handleSelectedTagsChange"
          @add-tag="handleAddTag"
          @toggle-tag="handleToggleTag"
          @remove-tag="handleRemoveTag"
          @remove-all-tags="handleRemoveAllTags"
          @show-all-tags="handleShowAllTags"
          @back="handleTagsBack"
          @search-tags="handleSearchTags"
          @apply-filters="handleApplyFilters"
        />
      </div>

      <!-- 菜单列表 -->
      <nav v-show="menuVisible" class="sidebar-nav">
        <!-- 统一的菜单容器（支持分组和非分组结构） -->
        <el-menu
          :default-active="activeSidebarItem"
          background-color="transparent"
          text-color="var(--text-dark)"
          active-text-color="var(--primary-color)"
          mode="vertical"
          class="unified-menu"
        >
          <!-- 遍历所有菜单分组 -->
          <template v-for="(group, groupIndex) in menuGroups">
            <!-- 分组标题（有分组结构时显示） -->
            <div
              v-if="group.group && hasGroups"
              :key="'group-header-' + groupIndex"
              class="group-header"
            >
              <i v-if="group.icon" :class="group.icon" class="group-icon"></i>
              <span class="group-title">{{ group.group }}</span>
            </div>

            <!-- 普通菜单项 -->
            <el-menu-item
              v-for="item in filterPlainItems(group)"
              :key="item.id"
              :index="item.id"
              :class="['nav-item', {
                active: item.id === activeSidebarItem,
                'has-badge': item.badge
              }]"
              @click="handleMenuItemClick(item)"
            >
              <div class="nav-item-content">
                <div class="item-icon">
                  <i :class="item.icon" :data-icon="item.icon"></i>
                  <el-badge
                    v-if="item.badge"
                    :value="item.badge"
                    :max="99"
                    class="item-badge"
                  />
                </div>
                <div class="item-content">
                  <span class="item-text">{{ item.text }}</span>
                  <span v-if="item.subtext" class="item-subtext">{{ item.subtext }}</span>
                  <span v-if="item.description" class="item-description">{{ item.description }}</span>
                </div>
              </div>
            </el-menu-item>

            <!-- 子菜单 (适用于展开的分类) -->
            <el-submenu
              v-for="item in getSubmenuItems(group)"
              :key="item.id"
              :index="item.id"
              class="nav-submenu"
            >
              <div class="nav-item-content">
                <div class="item-icon">
                  <i :class="item.icon" :data-icon="item.icon"></i>
                </div>
                <span class="item-text">{{ item.text }}</span>
              </div>

              <el-menu-item
                v-for="child in item.children"
                :key="child.id"
                :index="child.id"
                :class="['nav-item', 'nav-child-item', {
                  active: child.id === activeSidebarItem
                }]"
                @click="handleMenuItemClick(child)"
              >
              <div class="nav-item-content">
                <div class="item-icon">
                  <i :class="child.icon" :data-icon="child.icon"></i>
                </div>
                <div class="item-content">
                  <span class="item-text">{{ child.text }}</span>
                  <span v-if="child.description" class="item-description">{{ child.description }}</span>
                </div>
              </div>
              </el-menu-item>
            </el-submenu>
          </template>
        </el-menu>
      </nav>

      <!-- 统计信息 -->
      <div v-if="showStats && menuVisible" class="sidebar-stats">
        <div class="stats-title">
          <i :class="statsConfig.icon"></i>
          {{ statsConfig.title }}
        </div>
        <div class="stats-grid">
          <div
            v-for="stat in statsConfig.items"
            :key="stat.key"
            class="stat-item"
          >
            <div class="stat-value">{{ getStatValue(stat.key) }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script>
import TagsFilter from '@/components/collect/TagsFilter.vue';
import { SIDEBAR_CONFIG } from '@/config/sidebar.js';

export default {
  name: 'SidebarNavigation',
  components: {
    TagsFilter
  },
  props: {
    sidebarItems: {
      type: Array,
      required: true
    },
    activeSidebarItem: {
      type: String,
      required: true
    },
    sidebarCollapsed: {
      type: Boolean,
      default: false
    },
    headerConfig: {
      type: Object,
      default: () => ({
        title: '收藏中心',
        shortTitle: '收藏',
        icon: 'fas fa-bookmark'
      })
    },
    statsConfig: {
      type: Object,
      default: () => ({
        title: '统计信息',
        icon: 'fas fa-chart-bar',
        items: []
      })
    },
    showStats: {
      type: Boolean,
      default: true
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
    // 新增标签相关props
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

  },
  computed: {
    menuVisible() {
      return !this.showTagsFilter;
    },

    /**
     * 根据当前激活的导航中心获取对应的菜单配置
     * 支持四大中心：collect（收藏中心）、creation（创作中心）、search（检索中心）、personal（个人中心）
     */
    currentMenuItems() {
      // 根据headerConfig的title来判断当前中心
      const centerTitle = this.headerConfig?.title || '收藏中心';

      switch(centerTitle) {
        case '创作中心':
          return this.getCreationMenuItems();
        case '检索中心':
          return this.getSearchMenuItems();
        case '个人中心':
          return this.getPersonalMenuItems();
        case '收藏中心':
        default:
          return this.sidebarItems;
      }
    },



    /**
     * 菜单分组处理
     * 支持两种菜单结构：
     * 1. 平铺结构（collect, creation, dashboard）：直接返回菜单项
     * 2. 分组结构（search）：按group分组
     */
    menuGroups() {
      // 使用动态菜单配置
      const items = this.currentMenuItems;

      // 检查是否有分组结构
      if (!items || items.length === 0) {
        return [];
      }

      // 如果第一个item有group属性，说明是分组结构
      if (items[0].group) {
        return items;
      }

      // 否则是平铺结构，包装成单个分组
      return [{
        group: null,
        items: items
      }];
    },

    /**
     * 检查菜单是否有分组
     */
    hasGroups() {
      const items = this.currentMenuItems;
      return items && items.length > 0 && items[0].group;
    }
  },
  methods: {
    /**
     * 获取创作中心菜单项配置
     * 使用 sidebar.js 配置文件
     */
    getCreationMenuItems() {
      return SIDEBAR_CONFIG.creation || [];
    },

    /**
     * 获取检索中心菜单项配置
     * 使用 sidebar.js 配置文件
     */
    getSearchMenuItems() {
      return SIDEBAR_CONFIG.search || [];
    },

    /**
     * 获取个人中心菜单项配置
     * 使用 sidebar.js 配置文件
     */
    getPersonalMenuItems() {
      return SIDEBAR_CONFIG.personal || [];
    },
    /**
     * 从平铺菜单组中过滤无children的项
     * @param {Object} group - 菜单分组
     * @returns {Array} 无children的菜单项
     */
    filterPlainItems(group) {
      if (!group.items) {
        return [];
      }
      return group.items.filter(item => !item.children);
    },

    /**
     * 获取菜单组中有children的提交项（用于渲染子菜单）
     * @param {Object} group - 菜单分组
     * @returns {Array} 有children的菜单项
     */
    getSubmenuItems(group) {
      if (!group.items) {
        return [];
      }
      return group.items.filter(item => item.children);
    },

    getStatValue(key) {
      switch (key) {
        case 'totalCollects':
          return this.totalCollections;
        case 'todayCollects':
          return this.todayCollections;
        case 'totalTags':
          return this.totalTags;
        case 'unreadCollects':
          return this.unreadCollections;
        case 'currentList': {
          const item = this.statsConfig.items.find(i => i.key === 'currentList');
          return item ? item.count : 0;
        }
        default:
          return 0;
      }
    },
    // 标签相关方法
    handleSelectedTagsChange(tags) {
      this.$emit('update:selected-tags', tags);
    },
    handleAddTag(tagName) {
      this.$emit('add-tag', tagName);
    },
    handleToggleTag(tagId) {
      this.$emit('toggle-tag', tagId);
    },
    handleRemoveTag(tagId) {
      this.$emit('remove-tag', tagId);
    },
    handleRemoveAllTags() {
      this.$emit('remove-all-tags');
    },
    handleApplyFilters(filters) {
      this.$emit('apply-filters', filters);
    },
    handleShowAllTags() {
      this.$emit('show-all-tags');
    },
    handleTagsBack() {
      this.$emit('tags-back');
    },
    handleSearchTags(keyword) {
      this.$emit('search-tags', keyword);
    },
    // 处理菜单项点击
    handleMenuItemClick(item) {
      this.$emit('sidebar-item-click', item);
    }
  }
}
</script>

<style scoped>
/* 侧边栏样式 */
.sidebar {
  width: 240px;
  background-color: var(--bg-primary);
  border-right: 1px solid var(--border-light);
  overflow-y: auto;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
}

/* 图标动画效果 */
.nav-item .item-icon {
  transition: all var(--transition-normal);
}

.nav-item .item-icon i {
  transition: all var(--transition-normal);
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

/* 标签过滤组件 */
.sidebar-tags-filter {
  flex: 0 0 auto; /* 不伸缩，根据内容决定高度 */
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 40%;
  /* 移除 max-height 限制，避免预留空白空间 */
}


/* 侧边栏导航 */
.sidebar-nav {
  flex: 1 1 auto;
  padding: 16px 8px;
  overflow-y: auto;
  overflow-x: hidden;
  width: 100%;
  box-sizing: border-box;
  min-height: 0;
  top:0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  border-radius: 6px;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-item:hover {
  background-color: var(--bg-secondary);
  transform: translateX(4px);
}

.nav-item.active {
  background-color: var(--primary-bg);
  border-left: 3px solid var(--primary-color);
  margin-left: 0;
  padding-left: 13px;
}

.nav-child-item {
  padding-left: 40px;
}

.nav-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.nav-item .item-icon {
  position: relative;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-item .item-icon i {
  font-size: 16px;
  color: var(--text-medium);
  transition: color var(--transition-base);
  font-weight: 500;
}

.nav-item.active .item-icon i {
  color: var(--primary-color);
  font-weight: 600;
}

.item-badge {
  position: absolute;
  top: -4px;
  right: -4px;
}

.item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-text {
  font-size: 14px;
  color: var(--text-dark);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-item.active .item-text {
  color: var(--primary-color);
  font-weight: 500;
}

.item-subtext,
.item-desc {
  font-size: 12px;
  color: var(--text-light);
  background-color: var(--bg-gray);
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: auto;
}

/* 侧边栏统计 */
.sidebar-stats {
  flex: 0 0 auto; /* 不伸缩，固定高度 */
  padding: 10px;
  border-top: 1px solid var(--border-light);
  background-color: rgba(74, 108, 247, 0.05);
  border-radius: 8px;
  margin: 12px 12px 0 12px;
  max-height: 200px; /* 限制最大高度 */
  overflow: hidden; /* 超出部分隐藏 */
}

.stats-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-dark);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-title i {
  color: var(--primary-color);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 12px 8px;
  background-color: var(--bg-secondary);
  border-radius: var(--border-radius-sm);
  transition: var(--transition-base);
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
  background-color: rgba(74, 108, 247, 0.1);
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1.2;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 11px;
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 侧边栏底部 */
.sidebar-footer {
  flex: 0 0 auto;
  padding: 12px;
  border-top: 1px solid var(--border-light);
  margin-top: 0;
}

/* Element UI 子菜单和菜单项样式统一 */
:deep(.nav-submenu > .el-submenu__title) {
  padding: 0 16px !important;
  height: 40px !important;
  line-height: normal !important;
  margin: 0 !important;
  border-radius: 6px !important;
  transition: var(--transition-base) !important;
  display: flex !important;
  align-items: center !important;
  position: relative !important;
  z-index: 5 !important;
  transform: translateZ(0) !important;
  width: 100% !important;
  box-sizing: border-box !important;
}

:deep(.nav-submenu > .el-submenu__title:hover) {
  background-color: var(--bg-secondary) !important;
}

:deep(.nav-submenu.is-opened > .el-submenu__title) {
  background-color: var(--primary-bg) !important;
}

:deep(.nav-submenu.is-opened > .el-submenu__title .item-text) {
  color: var(--primary-color) !important;
  font-weight: 500 !important;
}

:deep(.nav-submenu.is-opened > .el-submenu__title .item-icon i) {
  color: var(--primary-color) !important;
  font-weight: 600 !important;
}

:deep(.nav-submenu > .el-submenu__title) {
  display: flex !important;
  align-items: center !important;
  padding: 12px 20px !important;
  cursor: pointer !important;
  position: relative !important;
}

:deep(.nav-submenu > .el-submenu__title .nav-item-content) {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  width: 100% !important;
}

:deep(.nav-submenu > .el-submenu__title .item-text) {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 子菜单的展开/收起箭头 */
.nav-submenu :deep(.el-submenu__icon-arrow) {
  margin-top: 0 !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
}

/* 菜单分组样式 */
.menu-group {

}

.menu-group:not(:last-child) {
  border-bottom: 1px solid var(--border-light);
}

.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-medium);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  user-select: none;
  width: 100%;
  box-sizing: border-box;
}

.group-icon {
  font-size: 14px;
  opacity: 0.6;
}

.group-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.unified-menu .el-menu-item) {
  padding: 12px 16px !important;
  cursor: pointer !important;
}

/* 重置 el-menu 容器的默认边距 */
:deep(.unified-menu) {
  margin: 0 !important;
  padding: 0 !important;
  border: none !important;
  position: relative; /* 确保在 sidebar-nav 内定位 */
}

:deep(.unified-menu .el-menu-item) {
  padding: 12px 16px !important;
  cursor: pointer !important;
}

:deep(.unified-menu .el-menu-item .nav-item-content) {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  width: 100% !important;
}

/* Element UI 折叠状态下图标美化 */
:deep(.unified-menu .el-menu-item.collapsed .item-icon) {
  width: 32px !important;
  height: 32px !important;
  border-radius: 8px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  background-color: var(--primary-bg) !important;
  margin: 0 auto !important;
  flex-shrink: 0 !important;
  position: relative !important;
  z-index: 10 !important;
  transform: translateZ(0) !important;
}

:deep(.unified-menu .el-menu-item.collapsed .item-icon i) {
  font-size: 18px !important;
  color: var(--primary-color) !important;
  font-weight: 600 !important;
}

/* 折叠状态下活动菜单项的优化样式 */
:deep(.unified-menu .el-menu-item.collapsed.is-active) {
  margin-left: 0 !important;
  padding-left: 0 !important;
  border-left: none !important;
}

:deep(.unified-menu .el-menu-item.collapsed.is-active .item-icon) {
  margin-left: 0 !important;
  padding-left: 0 !important;
  background-color: var(--primary-color) !important;
  box-shadow: var(--shadow-primary, 0 4px 12px rgba(74, 108, 247, 0.4)) !important;
}

:deep(.unified-menu .el-menu-item.collapsed.is-active .item-icon i) {
  color: white !important;
  font-weight: 700 !important;
}

:deep(.unified-menu .el-menu-item.collapsed:hover .item-icon) {
  background-color: var(--primary-bg) !important;
  transform: scale(1.1) !important;
  box-shadow: var(--shadow-primary, 0 4px 8px rgba(74, 108, 247, 0.3)) !important;
}

:deep(.unified-menu .el-menu-item.collapsed:hover .item-icon i) {
  color: var(--primary-color) !important;
  transform: scale(1.1) !important;
}

:deep(.unified-menu .el-menu-item:hover) {
  background-color: var(--bg-secondary) !important;
  transform: translateX(4px) !important;
}

:deep(.unified-menu .el-menu-item.is-active) {
  background-color: var(--primary-bg) !important;
  border-left: 3px solid var(--primary-color) !important;
  margin-left: 0 !important;
  padding-left: 13px !important;
}

:deep(.group-menu .el-menu-item.is-active .item-text) {
  color: var(--primary-color) !important;
  font-weight: 500 !important;
}

:deep(.group-menu .el-menu-item.is-active .item-icon i) {
  color: var(--primary-color) !important;
  font-weight: 600 !important;
}

:deep(.group-menu .el-menu-item .item-icon) {
  position: relative;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

:deep(.group-menu .el-menu-item .item-icon i) {
  font-size: 16px;
  color: var(--text-medium);
  transition: color var(--transition-base);
  font-weight: 500;
}

/* 菜单项描述文本样式 */
.item-description {
  display: block;
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 深色模式支持 - 维持白色主题 */
@media (prefers-color-scheme: dark) {
  .sidebar {
    background-color: var(--bg-primary) !important;
    border-right-color: var(--border-light) !important;
  }

  .nav-item:hover {
    background-color: var(--bg-secondary) !important;
  }

  .nav-item.active {
    background-color: var(--primary-bg) !important;
  }

  .nav-item .item-icon i {
    color: var(--text-secondary) !important;
  }

  .nav-item.active .item-icon i {
    color: var(--primary-color) !important;
  }

  .stat-item:hover {
    background-color: var(--primary-bg) !important;
  }
}
</style>
