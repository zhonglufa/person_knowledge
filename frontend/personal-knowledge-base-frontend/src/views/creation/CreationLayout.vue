<template>
  <div class="creation-layout-page">
    <page-layout
      :nav-items="navItems"
      :active-nav="activeNav"
      :user="currentUser"
      :notification-count="notificationCount"
      :sidebar-items="currentSidebarItems"
      :active-sidebar-item="activeSidebarItem"
      :sidebar-collapsed="sidebarCollapsed"
      :sidebar-header-config="sidebarHeaderConfig"
      :sidebar-stats-config="sidebarStatsConfig"
      :show-stats="!sidebarCollapsed"
      @toggle-sidebar="toggleSidebar"
      @nav-click="handleNavClick"
      @user-command="handleUserCommand"
      @search="handleSearch"
      @sidebar-item-click="handleSidebarItemClick"
    >
      <router-view />
    </page-layout>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import PageLayout from '@/components/layout/PageLayout.vue'
import navigationMixin from '@/utils/navigationMixin'
import { SIDEBAR_CONFIG } from '@/config/sidebar'

export default {
  name: 'CreationLayout',
  components: {
    PageLayout
  },
  mixins: [navigationMixin],
  data() {
    return {
      activeNav: 'creation',
      activeSidebarItem: 'creation-workspace',
      sidebarCollapsed: false,
      notificationCount: 0
    }
  },
  computed: {
    ...mapGetters('user', ['getAvatar', 'getNickname']),
    ...mapState('user', ['userInfo']),
    currentUser() {
      return {
        name: this.getNickname || this.userInfo?.username || '未知用户',
        avatar: this.getAvatar || 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg'
      }
    },
    currentSidebarItems() {
      return SIDEBAR_CONFIG?.creation || []
    },
    sidebarHeaderConfig() {
      return { title: '创作中心', shortTitle: '创作', icon: 'fas fa-edit' }
    },
    sidebarStatsConfig() {
      return {
        title: '创作概览',
        icon: 'fas fa-seedling',
        items: [
          { key: 'processingCount', label: '待加工' },
          { key: 'draftCount', label: '草稿' },
          { key: 'noteCount', label: '笔记' },
          { key: 'reminderCount', label: '提醒' }
        ]
      }
    }
  },
  methods: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    handleNavClick(item) {
      const routes = {
        collect: '/collect/center',
        creation: '/creation/workspace',
        search: '/search/center',
        personal: '/personal/center'
      }
      const route = routes[item?.id]
      if (route) {
        this.$router.push(route)
      }
    },
    handleUserCommand(command) {
      const routes = { profile: '/personal/profile', settings: '/personal/settings' }
      if (command === 'logout') {
        this.handleLogout()
      } else if (routes[command]) {
        this.$router.push(routes[command])
      }
    },
    handleSearch(keyword) {
      if (keyword?.trim()) {
        this.$router.push({ path: '/search/global', query: { q: keyword.trim() } })
      }
    },
    handleSidebarItemClick(item) {
      this.activeSidebarItem = item?.id
      if (item?.route) {
        this.$router.push(item.route)
      }
    },
    async handleLogout() {
      try {
        await this.$store.dispatch('user/logout')
        this.$router.push('/login')
        this.$message.success('已成功退出登录')
      } catch (error) {
        console.error('登出失败:', error)
        this.$message.error('登出失败，请重试')
      }
    },
    syncSidebarWithRoute() {
      const routeMap = {
        '/creation': 'creation-workspace',
        '/creation/workspace': 'creation-workspace',
        '/creation/processing': 'creation-processing',
        '/creation/notes': 'creation-notes',
        '/creation/drafts': 'creation-drafts'
      }
      const currentRoute = this.$route?.path
      this.activeSidebarItem = routeMap[currentRoute] || 'creation-workspace'
    }
  },
  watch: {
    '$route.path'() {
      this.syncSidebarWithRoute()
    }
  },
  mounted() {
    this.syncSidebarWithRoute()
  }
}
</script>
