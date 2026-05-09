<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
      <div class="header-left">
        <div class="logo">
          <i class="el-icon-s-grid"></i>
          <span class="logo-text">知识库管理后台</span>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-info">
            <div class="avatar">
              <i class="el-icon-user-solid"></i>
            </div>
            <span class="username">{{ adminInfo.nickname || adminInfo.username || '管理员' }}</span>
            <i class="el-icon-arrow-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="logout">
              <i class="el-icon-switch-button"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体区域 -->
    <div class="admin-body">
      <!-- 左侧菜单 -->
      <aside class="admin-sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :unique-opened="true"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">控制台</span>
          </el-menu-item>

          <el-menu-item index="/admin/users">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/content">
            <i class="el-icon-document"></i>
            <span slot="title">内容管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/announcements">
            <i class="el-icon-bell"></i>
            <span slot="title">公告管理</span>
          </el-menu-item>

          <el-menu-item index="/admin/content-logs">
            <i class="el-icon-s-order"></i>
            <span slot="title">操作日志</span>
          </el-menu-item>

          <el-submenu index="/admin/rbac">
            <template slot="title">
              <i class="el-icon-lock"></i>
              <span>权限管理</span>
            </template>
            <el-menu-item index="/admin/roles">
              <span slot="title">角色管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/permissions">
              <span slot="title">权限管理</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>

        <!-- 折叠按钮 -->
        <div class="sidebar-toggle" @click="toggleSidebar">
          <i :class="isCollapsed ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"></i>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <main class="admin-main">
        <!-- 面包屑导航 -->
        <div class="admin-breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <!-- 页面内容 -->
        <div class="admin-content">
          <transition name="fade" mode="out-in">
            <router-view :key="$route.path" />
          </transition>
        </div>
      </main>
    </div>
  </div>
</template>

<script>
import { MessageBox, Message } from 'element-ui'

export default {
  name: 'AdminLayout',

  data() {
    return {
      adminInfo: {},
      isCollapsed: false
    }
  },

  computed: {
    activeMenu() {
      return this.$route.path
    },
    currentRouteTitle() {
      return this.$route.meta.title || ''
    }
  },

  created() {
    this.loadAdminInfo()
  },

  methods: {
    loadAdminInfo() {
      const adminInfoStr = localStorage.getItem('adminInfo')
      if (adminInfoStr) {
        try {
          this.adminInfo = JSON.parse(adminInfoStr)
        } catch (error) {
          console.error('解析管理员信息失败', error)
          this.handleLogout(true)
        }
      } else {
        this.handleLogout(true)
      }
    },

    handleMenuSelect(index) {
      if (index !== this.$route.path) {
        this.$router.push(index)
      }
    },

    handleCommand(command) {
      if (command === 'logout') {
        this.handleLogout()
      }
    },

    handleLogout(silent) {
      const doLogout = () => {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        if (!silent) {
          Message.success('退出登录成功')
        }
        this.$router.push('/admin/login')
      }

      if (silent) {
        doLogout()
      } else {
        MessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          doLogout()
        }).catch(() => {})
      }
    },

    toggleSidebar() {
      this.isCollapsed = !this.isCollapsed
    }
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-canvas);
}

/* ========== 顶部导航栏 ========== */
.admin-header {
  height: 56px;
  background: var(--bg-container);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  box-shadow: var(--shadow-xs);
  position: relative;
  z-index: 100;
}

.header-left .logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--primary-color);
}

.header-left .logo i {
  font-size: 24px;
}

.header-right .user-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  transition: background-color var(--transition-base);
}

.header-right .user-info:hover {
  background-color: var(--bg-canvas);
}

.header-right .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}

.header-right .username {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  font-weight: 500;
}

/* ========== 主体区域 ========== */
.admin-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ========== 左侧菜单 ========== */
.admin-sidebar {
  width: 220px;
  background: var(--bg-container);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base);
  position: relative;
}

.sidebar-collapsed {
  width: 64px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  padding-top: var(--space-2);
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 2px var(--space-2);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  transition: all var(--transition-base);
}

::v-deep .el-submenu__title {
  height: 48px;
  line-height: 48px;
  margin: 2px var(--space-2);
  margin: 2px 6px;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  transition: all var(--transition-base);
}

.sidebar-menu .el-menu-item i {
  color: var(--text-secondary);
  margin-right: var(--space-3);
  font-size: 18px;
  width: 20px;
  text-align: center;
}


.sidebar-menu .el-menu-item:hover {
  background-color: var(--primary-bg) !important;
  color: var(--primary-color) !important;
}

.sidebar-menu .el-menu-item:hover i {
  color: var(--primary-color) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: var(--primary-color) !important;
  color: #ffffff !important;
}

.sidebar-menu .el-menu-item.is-active i {
  color: #ffffff !important;
}

.sidebar-toggle {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--border-light);
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 14px;
  transition: all var(--transition-base);
}

.sidebar-toggle:hover {
  background-color: var(--bg-canvas);
  color: var(--primary-color);
}

/* ========== 右侧内容区 ========== */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg-canvas);
}

.admin-breadcrumb {
  padding: var(--space-4) var(--space-6);
  background: var(--bg-container);
  border-bottom: 1px solid var(--border-light);
}

.admin-content {
  flex: 1;
  padding: var(--space-6);
  overflow-y: auto;
}

/* ========== 过渡动画 ========== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-base);
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 56px;
    bottom: 0;
    z-index: 99;
    box-shadow: var(--shadow-lg);
  }

  .sidebar-collapsed {
    width: 0;
    overflow: hidden;
  }

  .sidebar-toggle {
    display: none;
  }

  .admin-content {
    padding: var(--space-4);
  }

  .header-right .username {
    display: none;
  }
}
</style>
