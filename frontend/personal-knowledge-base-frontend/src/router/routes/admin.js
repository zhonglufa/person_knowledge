import AdminLayout from '@/components/layout/AdminLayout.vue'

// 管理员路由配置
export const adminRoutes = [
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      isAdmin: true
    },
    children: [
      {
        path: '',
        redirect: '/admin/home'
      },
      {
        path: 'home',
        name: 'AdminHome',
        component: () => import('@/views/admin/Home.vue'),
        meta: {
          title: '管理员首页',
          requiresAuth: true,
          isAdmin: true,
          permission: 'admin:dashboard:view'
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          isAdmin: true,
          permission: 'user:view'
        }
      },
      {
        path: 'content',
        name: 'ContentManagement',
        component: () => import('@/views/admin/ContentManagement.vue'),
        meta: {
          title: '内容管理',
          requiresAuth: true,
          isAdmin: true,
          permission: 'content:view'
        }
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/admin/AnnouncementManagement.vue'),
        meta: {
          title: '公告管理',
          requiresAuth: true,
          isAdmin: true,
          permission: 'announcement:view'
        }
      },
      {
        path: 'content-logs',
        name: 'ContentAuditLogs',
        component: () => import('@/views/admin/ContentAuditLogs.vue'),
        meta: {
          title: '操作日志',
          requiresAuth: true,
          isAdmin: true,
          permission: 'content:view'
        }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/RoleManagement.vue'),
        meta: {
          title: '角色管理',
          requiresAuth: true,
          isAdmin: true,
          permission: 'user:view'
        }
      },
      {
        path: 'permissions',
        name: 'PermissionManagement',
        component: () => import('@/views/admin/PermissionManagement.vue'),
        meta: {
          title: '权限管理',
          requiresAuth: true,
          isAdmin: true,
          permission: 'user:view'
        }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: {
      title: '管理员登录',
      requiresAuth: false
    }
  }
]

export default adminRoutes
