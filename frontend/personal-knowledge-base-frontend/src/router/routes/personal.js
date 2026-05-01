// 个人中心正式主页面
export default [
  {
    path: '/personal/center',
    name: 'PersonalCenter',
    component: () => import('@/views/personal/PersonalCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '个人中心'
    }
  },
  {
    path: '/personal/profile',
    name: 'PersonalProfile',
    component: () => import('@/views/personal/Profile.vue'),
    meta: {
      requiresAuth: true,
      title: '个人资料'
    }
  },
  {
    path: '/personal/settings',
    name: 'PersonalSettings',
    component: () => import('@/views/personal/Settings.vue'),
    meta: {
      requiresAuth: true,
      title: '账户设置'
    }
  },
  {
    path: '/personal/public',
    name: 'PersonalPublic',
    component: () => import('@/views/personal/PublicContent.vue'),
    meta: {
      requiresAuth: true,
      title: '公开内容管理'
    }
  },
  {
    path: '/personal/dashboard',
    name: 'PersonalDashboard',
    component: () => import('@/views/personal/Dashboard.vue'),
    meta: {
      requiresAuth: true,
      title: '统计看板'
    }
  },
  {
    path: '/personal/collections',
    redirect: '/collections/manage'
  },
  {
    path: '/personal/processing',
    redirect: '/creation/processing'
  },
  {
    path: '/personal/notifications',
    name: 'NotificationCenter',
    component: () => import('@/views/personal/NotificationCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '通知中心'
    }
  },
  {
    path: '/personal/tags',
    redirect: '/taxonomy/tags'
  },
  {
    path: '/personal/notes',
    redirect: '/creation/notes'
  },
  {
    path: '/interaction/center',
    name: 'InteractionCenter',
    component: () => import('@/views/interaction/InteractionCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '互动中心'
    }
  }
]
