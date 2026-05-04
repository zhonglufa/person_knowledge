<template>
  <page-layout
    :active-nav="activeNav"
    :show-sidebar="true"
    :sidebar-items="sidebarItems"
    :active-sidebar-item="activeSidebarItem"
    :sidebar-header-config="sidebarHeaderConfig"
    :sidebar-stats-config="sidebarStatsConfig"
    :user="user"
    :notification-count="notificationCount"
    @sidebar-item-click="handleSidebarItemClick"
    @user-command="handleUserCommand"
  >
    <router-view />
  </page-layout>
</template>

<script>
import PageLayout from '@/components/layout/PageLayout.vue';

export default {
  name: 'TaxonomyLayout',
  components: {
    PageLayout
  },
  data() {
    return {
      activeNav: 'personal',
      sidebarItems: [
        {
          id: 'taxonomy-categories',
          text: '分类管理',
          icon: 'fas fa-folder-tree',
          route: '/taxonomy/categories'
        },
        {
          id: 'taxonomy-tags',
          text: '标签管理',
          icon: 'fas fa-tags',
          route: '/taxonomy/tags'
        }
      ],
      sidebarHeaderConfig: {
        title: '知识结构',
        shortTitle: '结构',
        icon: 'fas fa-sitemap'
      },
      sidebarStatsConfig: {
        title: '结构管理',
        icon: 'fas fa-sitemap',
        items: []
      },
      user: {},
      notificationCount: 0
    };
  },
  computed: {
    activeSidebarItem() {
      const path = this.$route.path;
      if (path.includes('/taxonomy/categories')) {
        return 'taxonomy-categories';
      } else if (path.includes('/taxonomy/tags')) {
        return 'taxonomy-tags';
      }
      return 'taxonomy-categories';
    }
  },
  mounted() {
    this.loadUserInfo();
  },
  methods: {
    handleSidebarItemClick(item) {
      if (item.route && item.route !== this.$route.path) {
        this.$router.push(item.route);
      }
    },
    
    handleUserCommand(command) {
      switch(command) {
        case 'profile':
          this.$router.push('/personal/profile');
          break;
        case 'settings':
          this.$router.push('/personal/settings');
          break;
        case 'logout':
          this.handleLogout();
          break;
      }
    },
    
    async loadUserInfo() {
      try {
        const userStr = localStorage.getItem('user');
        if (userStr) {
          this.user = JSON.parse(userStr);
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      }
    },
    
    handleLogout() {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
</style>
