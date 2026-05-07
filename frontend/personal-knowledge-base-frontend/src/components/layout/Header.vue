<template>
  <el-container class="header-container">
    <el-header class="main-header" :style="{ height: headerHeight }">
      <div class="header-wrapper">
        <div class="header-content">
          <div class="logo-section" @click="handleLogoClick">
            <div class="logo-icon-wrapper">
              <i class="fas fa-book-open logo-icon"></i>
            </div>
            <div class="logo-text">
              <h1 class="logo-title">个人知识库</h1>
              <p v-show="showLogoSubtitle" class="logo-subtitle">Personal Knowledge Base</p>
            </div>
          </div>

          <div class="search-section hidden md:flex">
            <div class="search-wrapper" :class="{ 'search-focused': isSearchFocused }">
              <el-input
                  v-model="searchQuery"
                  placeholder="搜索知识、标签、收藏集..."
                  prefix-icon="el-icon-search"
                  class="search-input"
                  @focus="isSearchFocused = true"
                  @blur="isSearchFocused = false"
                  @keyup.enter="handleSearch"
              />
            </div>
          </div>

          <div class="nav-section hidden lg:flex">
            <el-menu
                :default-active="activeIndex"
                mode="horizontal"
                class="main-nav"
                @select="handleNavSelect"
            >
              <el-menu-item
                  index="home"
                  class="nav-item"
                  @mouseenter="onNavHover('home')"
                  @mouseleave="onNavLeave"
              >
                <i class="fas fa-home nav-icon"></i>
                <span class="nav-text">首页</span>
              </el-menu-item>

              <el-menu-item
                  index="collect"
                  class="nav-item"
                  @mouseenter="onNavHover('collect')"
                  @mouseleave="onNavLeave"
              >
                <i class="fas fa-bookmark nav-icon"></i>
                <span class="nav-text">收藏中心</span>
              </el-menu-item>

              <el-menu-item
                  index="creation"
                  class="nav-item"
                  @mouseenter="onNavHover('creation')"
                  @mouseleave="onNavLeave"
              >
                <i class="fas fa-pen-fancy nav-icon"></i>
                <span class="nav-text">创作中心</span>
              </el-menu-item>

              <el-menu-item
                  index="search"
                  class="nav-item"
                  @mouseenter="onNavHover('search')"
                  @mouseleave="onNavLeave"
              >
                <i class="fas fa-search nav-icon"></i>
                <span class="nav-text">检索中心</span>
              </el-menu-item>
            </el-menu>

            <div class="user-actions">
              <template v-if="isLoggedIn">
                <el-tooltip
                    :content="notificationCount > 0 ? `${notificationCount}条未读通知` : '通知中心'"
                    placement="bottom"
                    :open-delay="300"
                >
                  <el-badge
                      :value="notificationCount"
                      :max="99"
                      :hidden="notificationCount === 0"
                      class="notification-badge-wrapper"
                  >
                    <button
                        class="notification-btn"
                        :class="{
                          'has-unread': notificationCount > 0,
                          'is-active': isNotificationActive,
                          'is-pressed': isNotificationPressed
                        }"
                        :aria-label="notificationCount > 0 ? `${notificationCount}条未读通知，点击查看` : '通知中心，点击查看'"
                        @click="handleNotificationClick"
                        @mousedown="isNotificationPressed = true"
                        @mouseup="isNotificationPressed = false"
                        @mouseleave="isNotificationPressed = false"
                    >
                      <i class="fas fa-bell notification-icon" :class="{ 'bell-shake': notificationCount > 0 }"></i>
                    </button>
                  </el-badge>
                </el-tooltip>

                <el-dropdown
                    trigger="click"
                    class="user-dropdown"
                    @command="handleUserCommand"
                >
                  <div class="user-profile">
                    <div class="avatar-wrapper">
                      <img
                          :src="userAvatar"
                          alt="用户头像"
                          class="user-avatar"
                          :class="{ 'avatar-hover': isAvatarHover }"
                          @mouseenter="isAvatarHover = true"
                          @mouseleave="isAvatarHover = false"
                      />
                      <div
                          class="avatar-status"
                          :class="userStatus"
                      />
                    </div>
                    <div class="user-info hidden md:block">
                      <span class="user-name">{{ userName }}</span>
                      <i class="el-icon-arrow-down user-dropdown-icon" />
                    </div>
                  </div>

                  <el-dropdown-menu slot="dropdown" class="user-dropdown-menu">
                    <el-dropdown-item command="profile" class="dropdown-item">
                      <i class="fas fa-user-circle"></i>
                      <span>个人中心</span>
                    </el-dropdown-item>

                    <el-dropdown-item command="collections" class="dropdown-item">
                      <i class="fas fa-book"></i>
                      <span>我的收藏</span>
                    </el-dropdown-item>

                    <el-dropdown-item command="creations" class="dropdown-item">
                      <i class="fas fa-pen"></i>
                      <span>我的创作</span>
                    </el-dropdown-item>

                    <el-dropdown-item  command="settings" class="dropdown-item">
                      <i class="fas fa-cog"></i>
                      <span>设置</span>
                    </el-dropdown-item>

                    <el-dropdown-item command="help" class="dropdown-item">
                      <i class="fas fa-question-circle"></i>
                      <span>帮助中心</span>
                    </el-dropdown-item>

                    <el-dropdown-item  command="logout" class="dropdown-item logout-item">
                      <i class="fas fa-sign-out-alt"></i>
                      <span>退出登录</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>

              <template v-else>
                <el-button
                    type="text"
                    class="login-btn hidden md:inline-flex"
                    @click="handleLogin"
                >
                  登录
                </el-button>
                <el-button
                    type="primary"
                    class="register-btn hidden md:inline-flex"
                    @click="handleRegister"
                >
                  注册
                </el-button>
              </template>

              <el-button
                  class="mobile-menu-btn md:hidden"
                  icon="el-icon-menu"
                  type="text"
                  @click="toggleMobileMenu"
              />
            </div>
          </div>
        </div>

        <div v-if="showMobileSearch" class="mobile-search-section md:hidden">
          <el-input
              v-model="searchQuery"
              placeholder="搜索..."
              prefix-icon="el-icon-search"
              class="mobile-search-input"
              size="small"
              @keyup.enter="handleSearch"
          />
        </div>
      </div>


    </el-header>
  </el-container>
</template>

<script>
import { mapState, mapGetters, mapActions } from 'vuex';
import { getUnreadCount } from '@/api/notification'
import { authApi } from '@/api/auth';

export default {
  name: 'Header',
  data() {
    return {
      mobileMenuOpen: false,
      activeIndex: '/',
      searchQuery: '',
      isSearchFocused: false,
      isAvatarHover: false,
      showMobileSearch: false,
      showLogoSubtitle: true,
      notificationCount: 0,
      isDesktop: true,
      windowWidth: 0,
      notificationTimer: null,
      isNotificationPressed: false
    }
  },
  computed: {
    ...mapState('user', [
      'userInfo',
      'token'
    ]),
    ...mapGetters('user', [
      'getNickname',
      'getAvatar',
      'isAuthenticated'
    ]),
    isLoggedIn() {
      return this.isAuthenticated;
    },
    userName() {
      return this.getNickname || '用户';
    },
    userEmail() {
      return this.userInfo?.email || 'user@example.com';
    },
    userAvatar() {
      return this.getAvatar || 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg';
    },
    userStatus() {
      return this.userInfo?.status || 'online';
    },
    headerHeight() {
      return this.showMobileSearch ? '100px' : '64px';
    },
    isNotificationActive() {
      return this.$route.path === '/personal/notifications';
    }
  },
  watch: {
    windowWidth(newWidth) {
      this.isDesktop = newWidth >= 768;
      this.showLogoSubtitle = newWidth >= 1024;
    },
    '$route'(to, from) {
      this.checkLoginStatus();
      this.updateActiveNavigation();
      if (this.isLoggedIn) {
        this.fetchNotificationCount();
      }
    },
    userInfo: {
      handler(newUserInfo) {
        if (newUserInfo) {
          console.log('用户信息已更新:', newUserInfo);
          console.log('当前computed值 - userName:', this.userName);
          console.log('当前computed值 - userEmail:', this.userEmail);
          console.log('当前computed值 - userAvatar:', this.userAvatar);
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    ...mapActions('user', [
      'fetchUserInfo',
      'logout'
    ]),
    async checkLoginStatus() {
      if (this.isLoggedIn && !this.userInfo) {
        try {
          await this.fetchUserInfo();
        } catch (error) {
          console.error('获取用户信息失败:', error);
          this.handleLogout();
        }
      }
    },
    updateActiveNavigation() {
      const currentPath = this.$route.path;

      const routeToNavMap = {
        '/': 'home',
        '/home': 'home',
        '/collect': 'collect',
        '/collect/center': 'collect',
        '/collection': 'collect',
        '/creation': 'creation',
        '/creation/center': 'creation',
        '/create': 'creation',
        '/search': 'search',
        '/search/center': 'search',
        '/personal': 'personal',
        '/personal/center': 'personal',
        '/user': 'personal',
        '/profile': 'personal',
        '/settings': 'settings',
        '/personal/settings': 'settings'
      };

      if (routeToNavMap[currentPath]) {
        this.activeIndex = routeToNavMap[currentPath];
        return;
      }

      for (const [routePrefix, navIndex] of Object.entries(routeToNavMap)) {
        if (currentPath.startsWith(routePrefix) && routePrefix !== '/') {
          this.activeIndex = navIndex;
          return;
        }
      }

      this.activeIndex = 'home';
    },
    handleResize() {
      this.windowWidth = window.innerWidth;
    },
    handleNavSelect(index) {
      const routeMap = {
        'home': '/personal/center',
        'collect': '/collect/center',
        'creation': '/creation/workspace',
        'search': '/search/center'
      };

      const targetRoute = routeMap[index];
      if (targetRoute && targetRoute !== this.$route.path) {
        this.$router.push(targetRoute);
      }
    },
    onNavHover(index) {
    },
    onNavLeave() {
      this.updateActiveNavigation();
    },
    handleLogoClick() {
      if (this.isLoggedIn) {
        this.$router.push('/personal/dashboard');
      } else {
        this.$router.push('/login');
      }
    },
    goToHome() {
      this.$router.push('/personal/dashboard');
    },
    goToCollectionCenter() {
      this.$router.push('/collect/center');
    },
    goToCreationCenter() {
      this.$router.push('/creation/center');
    },
    goToSearchCenter() {
      this.$router.push('/search/center');
    },
    handleSearch() {
      if (this.searchQuery.trim()) {
        this.$router.push({
          path: '/search/center',
          query: { q: this.searchQuery.trim() }
        });
      }
    },
    async handleNotificationClick() {
      if (this.$route.path === '/personal/notifications') {
        try {
          await this.fetchNotificationCount();
        } catch (e) {
          // silent
        }
        return;
      }
      try {
        await this.$router.push('/personal/notifications');
      } catch (err) {
        if (err.name !== 'NavigationDuplicated') {
          console.error('导航到通知中心失败:', err);
        }
      }
      this.$nextTick(() => this.fetchNotificationCount());
    },
    async fetchNotificationCount() {
      if (!this.isLoggedIn) {
        this.notificationCount = 0;
        return;
      }
      try {
        const { data } = await getUnreadCount();
        this.notificationCount = data || 0;
      } catch (error) {
        console.error('获取未读通知数失败:', error);
      }
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen;
      this.showMobileSearch = !this.showMobileSearch;
    },
    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/personal/center');
          break;
        case 'collections':
          this.$router.push('/collections/manage');
          break;
        case 'creations':
          this.$router.push('/creation/workspace');
          break;
        case 'settings':
          this.$router.push('/personal/settings');
          break;
        case 'help':
          this.$router.push('/help');
          break;
        case 'logout':
          this.handleLogout();
          break;
      }
    },
    async handleLogout() {
      try {
        await this.logout();
        this.$message.success('已退出登录');
        this.$router.push('/guest/home');
      } catch (error) {
        console.error('退出登录失败:', error);
        this.$message.error('退出登录失败');
      }
    },
    handleLogin() {
      this.$router.push('/login');
    },
    handleRegister() {
      this.$router.push('/login?tab=register');
    }
  },
  mounted() {
    this.handleResize();
    this.updateActiveNavigation();
    this.checkLoginStatus();
    this.fetchNotificationCount();
    this.notificationTimer = setInterval(() => {
      if (this.isLoggedIn) {
        this.fetchNotificationCount();
      }
    }, 60000);
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    if (this.notificationTimer) {
      clearInterval(this.notificationTimer);
      this.notificationTimer = null;
    }
  }
}
</script>

<style scoped>
.main-header {
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.header-wrapper {
  width: 100%;
  height: 100%;
}

.header-content {
  max-width: 1440px;
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  gap: 24px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.logo-section:hover {
  transform: scale(1.02);
}

.logo-icon-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.logo-icon {
  font-size: 20px;
  color: white;
}

.logo-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}

.logo-subtitle {
  margin: 0;
  font-size: 11px;
  color: #6b7280;
  line-height: 1.2;
  letter-spacing: 0.5px;
}

.search-section {
  flex: 1;
  max-width: 400px;
}

.search-wrapper {
  position: relative;
  transition: all 0.3s ease;
}

.search-focused {
  transform: translateY(-1px);
}

.search-input ::v-deep .el-input__inner {
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  transition: all 0.3s ease;
  height: 40px;
}

.search-input ::v-deep .el-input__inner:focus {
  border-color: #409eff;
  background: white;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.nav-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.main-nav {
  border-bottom: none;
  background: transparent;
}

.main-nav ::v-deep .el-menu-item {
  border-bottom: 2px solid transparent;
  color: #6b7280;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 0 16px;
  height: 64px;
  line-height: 64px;
}

.main-nav ::v-deep .el-menu-item:hover,
.main-nav ::v-deep .el-menu-item.is-active {
  color: #409eff;
  border-bottom-color: #409eff;
  background: rgba(64, 158, 255, 0.04);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-icon {
  font-size: 14px;
}

.nav-text {
  font-size: 14px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  outline: none;
  background: transparent;
  color: #6b7280;
  font-size: 18px;
  padding: 0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  -webkit-tap-highlight-color: transparent;
}

.notification-btn:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.08);
}

.notification-btn:focus-visible {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

.notification-btn:active,
.notification-btn.is-pressed {
  color: #337ecc;
  background: rgba(64, 158, 255, 0.15);
  transform: scale(0.92);
}

.notification-btn.is-active {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.notification-btn.has-unread {
  color: #409eff;
}

.notification-btn.has-unread:hover {
  color: #337ecc;
  background: rgba(64, 158, 255, 0.12);
}

.notification-icon {
  font-size: 18px;
  line-height: 1;
  transition: transform 0.25s ease;
}

.notification-btn:hover .notification-icon {
  transform: scale(1.1);
}

.bell-shake {
  animation: bell-shake 0.8s ease-in-out;
  transform-origin: top center;
}

@keyframes bell-shake {
  0% { transform: rotate(0deg); }
  15% { transform: rotate(14deg); }
  30% { transform: rotate(-12deg); }
  45% { transform: rotate(8deg); }
  60% { transform: rotate(-6deg); }
  75% { transform: rotate(3deg); }
  90% { transform: rotate(-1deg); }
  100% { transform: rotate(0deg); }
}

.notification-badge-wrapper {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

.notification-badge-wrapper ::v-deep .el-badge__content {
  top: 2px;
  right: 2px;
  font-size: 11px;
  line-height: 16px;
  padding: 0 5px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.user-profile:hover {
  background: rgba(64, 158, 255, 0.05);
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.avatar-hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid white;
  background: #10b981;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.user-dropdown-icon {
  font-size: 12px;
  color: #9ca3af;
}

.login-btn,
.register-btn {
  font-weight: 500;
  border-radius: 10px;
}

.mobile-search-section {
  padding: 0 24px 16px;
  background: white;
}

.mobile-search-input ::v-deep .el-input__inner {
  border-radius: 20px;
}

@media (max-width: 1024px) {
  .header-content {
    padding: 0 16px;
    gap: 16px;
  }

  .search-section {
    max-width: 300px;
  }
}

@media (max-width: 768px) {
  .header-content {
    height: 64px;
  }

  .logo-subtitle {
    display: none;
  }

  .logo-title {
    font-size: 16px;
  }
}
</style>
