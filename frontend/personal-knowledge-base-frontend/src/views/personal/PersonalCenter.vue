<template>
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
    <div v-loading="pageLoading" class="personal-center">
      <!-- 页面头部 -->
      <div class="page-header card">
        <div class="header-content">
          <div class="header-left">
            <div class="user-avatar-large">
              <img :src="currentUser?.avatar || defaultAvatar" :alt="currentUser?.name || ''" />
              <div class="avatar-status online"></div>
            </div>
            <div class="user-info">
              <h1 class="user-name">{{ currentUser?.name || '未知用户' }}</h1>
              <p class="user-role">个人知识管理者</p>
              <p class="user-desc">持续学习，不断积累，让知识创造价值</p>
            </div>
          </div>
          <div class="header-right">
            <div class="quick-stats">
              <div class="stat-item">
                <i class="fas fa-bookmark"></i>
                <div class="stat-content">
                  <span class="stat-number">{{ userStats?.collections || 0 }}</span>
                  <span class="stat-label">收藏项</span>
                </div>
              </div>
              <div class="stat-item">
                <i class="fas fa-file-alt"></i>
                <div class="stat-content">
                  <span class="stat-number">{{ userStats?.notes || 0 }}</span>
                  <span class="stat-label">笔记</span>
                </div>
              </div>
              <div class="stat-item">
                <i class="fas fa-share-alt"></i>
                <div class="stat-content">
                  <span class="stat-number">{{ userStats?.public || 0 }}</span>
                  <span class="stat-label">公开内容</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作区 -->
      <div class="quick-actions">
        <h2 class="section-title">快捷操作</h2>
        <div class="actions-grid">
          <div
            v-for="action in quickActions"
            :key="action.id"
            class="card action-card"
            @click="handleQuickAction(action)"
          >
            <div class="action-icon" :class="`bg-${action.color || 'primary'}`">
              <i :class="action.icon"></i>
            </div>
            <div class="action-content">
              <h3 class="action-title">{{ action.title || '' }}</h3>
              <p class="action-desc">{{ action.description || '' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据概览 -->
      <div class="data-overview">
        <h2 class="section-title">数据概览</h2>
        <div class="overview-grid">
          <!-- 收藏统计 -->
          <div class="overview-card card">
            <div class="card-header-inner">
              <h3 class="inner-card-title">收藏统计</h3>
              <i class="fas fa-bookmark text-primary"></i>
            </div>
            <div class="card-content-inner">
              <div class="stats-grid">
                <div class="stat-box">
                  <div class="stat-value">{{ collectionStats?.total || 0 }}</div>
                  <div class="stat-label">总收藏</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-warning">{{ collectionStats?.undigest || 0 }}</div>
                  <div class="stat-label">未消化</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-info">{{ collectionStats?.digesting || 0 }}</div>
                  <div class="stat-label">消化中</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-success">{{ collectionStats?.digested || 0 }}</div>
                  <div class="stat-label">已消化</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 笔记统计 -->
          <div class="overview-card card">
            <div class="card-header-inner">
              <h3 class="inner-card-title">笔记统计</h3>
              <i class="fas fa-file-alt text-success"></i>
            </div>
            <div class="card-content-inner">
              <div class="stats-grid">
                <div class="stat-box">
                  <div class="stat-value">{{ noteStats?.total || 0 }}</div>
                  <div class="stat-label">总笔记</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-success">{{ noteStats?.original || 0 }}</div>
                  <div class="stat-label">原创</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-warning">{{ noteStats?.summary || 0 }}</div>
                  <div class="stat-label">总结</div>
                </div>
                <div class="stat-box">
                  <div class="stat-value text-info">{{ noteStats?.normal || 0 }}</div>
                  <div class="stat-label">普通</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 活跃度统计 -->
          <div class="overview-card card">
            <div class="card-header-inner">
              <h3 class="inner-card-title">活跃度</h3>
              <i class="fas fa-chart-line text-info"></i>
            </div>
            <div class="card-content-inner">
              <div class="activity-stats">
                <div class="activity-item">
                  <span class="activity-label">今日新增</span>
                  <span class="activity-value">{{ activityStats?.today || 0 }}</span>
                </div>
                <div class="activity-item">
                  <span class="activity-label">本周新增</span>
                  <span class="activity-value">{{ activityStats?.week || 0 }}</span>
                </div>
                <div class="activity-item">
                  <span class="activity-label">本月新增</span>
                  <span class="activity-value">{{ activityStats?.month || 0 }}</span>
                </div>
                <div class="activity-item">
                  <span class="activity-label">连续打卡</span>
                  <span class="activity-value">{{ activityStats?.streak || 0 }}天</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="recent-activity card">
        <div class="card-header-inner">
          <h2 class="section-title mb-0">最近活动</h2>
          <el-button type="text" size="small" @click="handleViewAllActivities">
            查看全部 <i class="fas fa-arrow-right"></i>
          </el-button>
        </div>
        <div v-if="recentActivities && recentActivities.length > 0" class="activity-list">
          <div
            v-for="activity in recentActivities"
            :key="activity.id"
            class="activity-item"
          >
            <div class="activity-icon" :class="`bg-${activity.type || 'primary'}`">
              <i :class="activity.icon"></i>
            </div>
            <div class="activity-content">
              <h4 class="activity-title">{{ activity.title || '' }}</h4>
              <p class="activity-desc">{{ activity.description || '' }}</p>
              <span class="activity-time">{{ formatDate(activity.time) }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无活动记录" :image-size="80" />
      </div>
    </div>
  </page-layout>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import PageLayout from '@/components/layout/PageLayout.vue'
import navigationMixin from '@/utils/navigationMixin'
import { SIDEBAR_CONFIG } from '@/config/sidebar'
import { getUserStatistics } from '@/api/user'

export default {
  name: 'PersonalCenter',
  components: {
    PageLayout
  },
  mixins: [navigationMixin],
  data() {
    return {
      pageLoading: false,
      activeNav: 'personal',
      activeSidebarItem: 'personal-dashboard',
      sidebarCollapsed: false,
      notificationCount: 0,
      defaultAvatar: 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg',

      // 用户统计数据
      userStats: {
        collections: 0,
        notes: 0,
        public: 0
      },

      // 收藏统计
      collectionStats: {
        total: 0,
        undigest: 0,
        digesting: 0,
        digested: 0,
        abandoned: 0
      },

      // 笔记统计
      noteStats: {
        total: 0,
        original: 0,
        summary: 0,
        normal: 0
      },

      // 活跃度统计
      activityStats: {
        today: 0,
        week: 0,
        month: 0,
        streak: 0
      },

      // 快捷操作
      quickActions: [
        {
          id: 'new-note',
          title: '新建笔记',
          description: '创建新的学习笔记或总结',
          icon: 'fas fa-file-alt',
          color: 'success',
          route: '/personal/notes'
        },
        {
          id: 'view-collections',
          title: '我的收藏集',
          description: '管理和浏览所有收藏集',
          icon: 'fas fa-folder-open',
          color: 'warning',
          route: '/collection/list'
        },
        {
          id: 'view-public',
          title: '公开内容',
          description: '管理我的公开分享内容',
          icon: 'fas fa-globe',
          color: 'info',
          route: '/personal/public'
        },
        {
          id: 'processing-tasks',
          title: '待处理任务',
          description: '查看和处理待消化的收藏项',
          icon: 'fas fa-tasks',
          color: 'danger',
          route: '/personal/processing'
        },
        {
          id: 'view-tags',
          title: '标签管理',
          description: '整理和管理所有标签',
          icon: 'fas fa-tags',
          color: 'primary',
          route: '/personal/tags'
        },
        {
          id: 'edit-profile',
          title: '个人资料',
          description: '完善个人信息和偏好设置',
          icon: 'fas fa-user-edit',
          color: 'warning',
          route: '/personal/profile'
        },
        {
          id: 'notifications',
          title: '通知中心',
          description: '查看系统通知和互动消息',
          icon: 'fas fa-bell',
          color: 'info',
          route: '/personal/notifications'
        },
        {
          id: 'settings',
          title: '系统设置',
          description: '个性化配置和偏好设置',
          icon: 'fas fa-cog',
          color: 'success',
          route: '/personal/settings'
        }
      ],

      // 最近活动
      recentActivities: []
    }
  },

  computed: {
    ...mapGetters('user', [
      'getUserInfo',
      'getAvatar',
      'getNickname'
    ]),

    ...mapState('user', ['userInfo']),

    // 当前用户信息
    currentUser() {
      return {
        name: this.getNickname || this.userInfo?.username || '未知用户',
        avatar: this.getAvatar || this.defaultAvatar
      }
    },

    // 当前侧边栏配置
    currentSidebarItems() {
      return SIDEBAR_CONFIG?.personal || []
    },

    // 侧边栏头部配置
    sidebarHeaderConfig() {
      return {
        title: '个人中心',
        shortTitle: '个人',
        icon: 'fas fa-user'
      }
    },

    // 侧边栏统计配置
    sidebarStatsConfig() {
      return {
        title: '个人统计',
        icon: 'fas fa-chart-bar',
        items: [
          { key: 'collections', label: '总收藏数' },
          { key: 'notes', label: '笔记总数' },
          { key: 'publicContent', label: '公开内容' },
          { key: 'days', label: '连续打卡' }
        ]
      }
    }
  },

  created() {
    this.fetchNotificationCount()
    this.fetchUserStatistics()
  },

  methods: {
    // 获取通知数量
    async fetchNotificationCount() {
      try {
        const { getUnreadCount } = await import('@/api/notification')
        const res = await getUnreadCount()
        this.notificationCount = res?.data ?? res ?? 0
      } catch (error) {
        console.error('获取通知数量失败:', error)
        this.notificationCount = 0
      }
    },

    // 获取用户统计数据
    async fetchUserStatistics() {
      this.pageLoading = true
      try {
        const res = await getUserStatistics()
        const data = res?.data || res || {}
        this.parseStatisticsData(data)
      } catch (error) {
        console.error('获取统计数据失败:', error)
        this.useFallbackStatistics()
      } finally {
        this.pageLoading = false
      }
    },

    // 解析统计数据
    parseStatisticsData(data) {
      this.userStats = {
        collections: data?.collections ?? this.userStats.collections,
        notes: data?.notes ?? this.userStats.notes,
        public: data?.publicContent ?? this.userStats.public
      }

      this.collectionStats = {
        total: data?.collectionTotal ?? data?.collections ?? this.collectionStats.total,
        undigest: data?.collectionUndigest ?? this.collectionStats.undigest,
        digesting: data?.collectionDigesting ?? this.collectionStats.digesting,
        digested: data?.collectionDigested ?? this.collectionStats.digested,
        abandoned: data?.collectionAbandoned ?? this.collectionStats.abandoned
      }

      this.noteStats = {
        total: data?.noteTotal ?? data?.notes ?? this.noteStats.total,
        original: data?.noteOriginal ?? this.noteStats.original,
        summary: data?.noteSummary ?? this.noteStats.summary,
        normal: data?.noteNormal ?? this.noteStats.normal
      }

      this.activityStats = {
        today: data?.todayNew ?? this.activityStats.today,
        week: data?.weekNew ?? this.activityStats.week,
        month: data?.monthNew ?? this.activityStats.month,
        streak: data?.streakDays ?? this.activityStats.streak
      }

      this.recentActivities = Array.isArray(data?.recentActivities)
        ? data.recentActivities
        : this.getFallbackActivities()
    },

    // 使用备用统计数据
    useFallbackStatistics() {
      this.userStats = { collections: 156, notes: 45, public: 23 }
      this.collectionStats = { total: 156, undigest: 23, digesting: 15, digested: 89, abandoned: 29 }
      this.noteStats = { total: 45, original: 18, summary: 12, normal: 15 }
      this.activityStats = { today: 5, week: 23, month: 87, streak: 12 }
      this.recentActivities = this.getFallbackActivities()
    },

    // 获取备用活动数据
    getFallbackActivities() {
      return [
        {
          id: 1,
          type: 'primary',
          icon: 'fas fa-bookmark',
          title: '新增收藏',
          description: '收藏了 "Vue.js 3.0 组件设计最佳实践"',
          time: new Date('2024-01-15T15:30:00')
        },
        {
          id: 2,
          type: 'success',
          icon: 'fas fa-file-alt',
          title: '笔记更新',
          description: '更新了 "React Hooks 深入理解" 笔记',
          time: new Date('2024-01-15T14:15:00')
        },
        {
          id: 3,
          type: 'info',
          icon: 'fas fa-cogs',
          title: '开始加工',
          description: '开始加工 "前端性能优化" 收藏项',
          time: new Date('2024-01-15T11:20:00')
        },
        {
          id: 4,
          type: 'warning',
          icon: 'fas fa-share-alt',
          title: '内容公开',
          description: '将 "个人知识管理方法论" 笔记设为公开',
          time: new Date('2024-01-14T16:45:00')
        }
      ]
    },

    // 切换侧边栏
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    // 处理导航点击
    handleNavClick(item) {
      const routeMap = {
        collect: '/collect/center',
        creation: '/creation/center',
        search: '/search/center',
        personal: '/personal/center'
      }
      const path = routeMap[item?.id]
      if (path) {
        this.$router.push(path)
      }
    },

    // 处理用户命令
    handleUserCommand(command) {
      const routeMap = {
        profile: '/personal/profile',
        settings: '/personal/settings'
      }
      if (command === 'logout') {
        this.handleLogout()
      } else if (routeMap[command]) {
        this.$router.push(routeMap[command])
      }
    },

    // 处理搜索
    handleSearch(keyword) {
      if (keyword?.trim()) {
        this.$router.push({
          path: '/search/global',
          query: { q: keyword.trim() }
        })
      }
    },

    // 处理侧边栏项目点击
    handleSidebarItemClick(item) {
      this.activeSidebarItem = item?.id || ''

      const routeMap = {
        'personal-profile': '/personal/profile',
        'personal-settings': '/personal/settings',
        'personal-public': '/personal/public',
        'personal-dashboard': '/personal/dashboard',
        'personal-collections': '/personal/collections',
        'personal-processing': '/personal/processing'
      }
      const path = routeMap[item?.id]
      if (path) {
        this.$router.push(path)
      }
    },

    // 处理快捷操作
    handleQuickAction(action) {
      if (action?.route) {
        this.$router.push(action.route)
      }
    },

    // 查看全部活动
    handleViewAllActivities() {
      this.$router.push('/personal/dashboard')
    },

    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      if (isNaN(d.getTime())) return ''
      return d.toLocaleString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 处理登出
    async handleLogout() {
      try {
        await this.$store.dispatch('user/logout')
        this.$router.push('/login')
        this.$message.success('已成功退出登录')
      } catch (error) {
        console.error('登出失败:', error)
        this.$message.error('登出失败，请重试')
      }
    }
  }
}
</script>

<style scoped>
.personal-center {
  padding: var(--space-6);
  height: 100%;
  overflow-y: auto;
  background-color: var(--bg-page);
}

/* 页面头部 */
.page-header {
  margin-bottom: var(--space-6);
  border: none;
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-md);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-8);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-6);
}

.user-avatar-large {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
}

.user-avatar-large img {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-full);
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-md);
}

.avatar-status {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border-radius: var(--radius-full);
  border: 3px solid white;
}

.avatar-status.online {
  background: var(--success-color);
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: var(--font-size-4xl);
  font-weight: 700;
  color: white;
  margin: 0 0 var(--space-2) 0;
  line-height: 1.2;
}

.user-role {
  font-size: var(--font-size-lg);
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  margin: 0 0 var(--space-2) 0;
}

.user-desc {
  font-size: var(--font-size-base);
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  line-height: 1.5;
}

.header-right {
  flex-shrink: 0;
}

.quick-stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-md);
  min-width: 180px;
  backdrop-filter: blur(8px);
  transition: all var(--transition-normal);
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.25);
}

.stat-item i {
  font-size: var(--font-size-2xl);
  color: white;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: white;
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.8);
}

/* 快捷操作区 */
.quick-actions {
  margin-bottom: var(--space-8);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-5) 0;
}

.section-title.mb-0 {
  margin-bottom: 0;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-5);
}

.action-card {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-6);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon i {
  font-size: var(--font-size-xl);
  color: white;
}

.bg-primary { background: var(--gradient-primary); }
.bg-success { background: var(--gradient-secondary); }
.bg-info { background: var(--gradient-cool); }
.bg-warning { background: var(--gradient-warm); }

.action-content {
  flex: 1;
  min-width: 0;
}

.action-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.action-desc {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

/* 数据概览 */
.data-overview {
  margin-bottom: var(--space-8);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-6);
}

.overview-card {
  padding: 0;
  overflow: hidden;
}

.overview-card:hover {
  transform: none;
  box-shadow: var(--shadow-md);
}

.card-header-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-canvas);
}

.inner-card-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.card-header-inner i {
  font-size: var(--font-size-xl);
}

.card-content-inner {
  padding: var(--space-6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.stat-box {
  text-align: center;
  padding: var(--space-4);
  background: var(--bg-canvas);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.stat-box:hover {
  background: var(--primary-bg);
}

.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.activity-stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.activity-stats .activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-canvas);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.activity-stats .activity-item:hover {
  background: var(--primary-bg);
}

.activity-label {
  font-size: var(--font-size-base);
  color: var(--text-regular);
}

.activity-value {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--primary-color);
}

/* 最近活动 */
.recent-activity {
  padding: 0;
  overflow: hidden;
}

.recent-activity:hover {
  transform: none;
}

.activity-list {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.activity-list .activity-item {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-canvas);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.activity-list .activity-item:hover {
  background: var(--primary-bg);
  transform: translateX(var(--space-1));
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon i {
  font-size: var(--font-size-base);
  color: white;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
}

.activity-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2) 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: var(--font-size-xs);
  color: var(--text-placeholder);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-6);
  }

  .header-right {
    width: 100%;
  }

  .quick-stats {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: space-between;
  }

  .stat-item {
    min-width: 150px;
    flex: 1;
  }
}

@media (max-width: 768px) {
  .personal-center {
    padding: var(--space-4);
  }

  .page-header {
    padding: var(--space-5);
  }

  .header-left {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .user-avatar-large {
    width: 80px;
    height: 80px;
  }

  .user-name {
    font-size: var(--font-size-3xl);
  }

  .quick-stats {
    flex-direction: column;
  }

  .stat-item {
    min-width: auto;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 576px) {
  .page-header {
    padding: var(--space-4);
  }

  .user-avatar-large {
    width: 60px;
    height: 60px;
  }

  .user-name {
    font-size: var(--font-size-xl);
  }

  .action-card {
    padding: var(--space-4);
    gap: var(--space-4);
  }

  .action-icon {
    width: 48px;
    height: 48px;
  }

  .action-icon i {
    font-size: var(--font-size-lg);
  }

  .stat-box {
    padding: var(--space-3);
  }

  .stat-value {
    font-size: var(--font-size-xl);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
