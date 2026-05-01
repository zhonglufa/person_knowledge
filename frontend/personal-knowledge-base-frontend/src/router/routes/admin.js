import AdminLayout from '@/components/layout/AdminLayout.vue'

// 管理员路由配置
export default [
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
          isAdmin: true
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: '用户管理',
          requiresAuth: true,
          isAdmin: true
        }
      },
      {
        path: 'content',
        name: 'ContentManagement',
        component: () => import('@/views/admin/ContentManagement.vue'),
        meta: {
          title: '内容管理',
          requiresAuth: true,
          isAdmin: true
        }
      },
      {
        path: 'announcements',
        name: 'AnnouncementManagement',
        component: () => import('@/views/admin/AnnouncementManagement.vue'),
        meta: {
          title: '公告管理',
          requiresAuth: true,
          isAdmin: true
        }
      },
      {
        path: 'content-logs',
        name: 'ContentAuditLogs',
        component: () => import('@/views/admin/ContentAuditLogs.vue'),
        meta: {
          title: '操作日志',
          requiresAuth: true,
          isAdmin: true
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