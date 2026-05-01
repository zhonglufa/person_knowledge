// 检索中心路由配置
export default [
  {
    path: '/search',
    redirect: '/search/global'
  },
  {
    path: '/search/center',
    redirect: '/search/global'
  },
  {
    path: '/search/global',
    name: 'SearchCenter',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '检索中心'
    }
  },
  {
    path: '/search/history',
    name: 'SearchHistory',
    component: () => import('@/views/search/SearchHistory.vue'),
    meta: {
      requiresAuth: true,
      title: '检索历史'
    }
  },
  {
    path: '/search/collections',
    name: 'SearchCollections',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '收藏集检索'
    }
  },
  {
    path: '/search/items',
    name: 'SearchItems',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '收藏项检索'
    }
  },
  {
    path: '/search/notes',
    name: 'SearchNotes',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '笔记检索'
    }
  },
  {
    path: '/search/tags',
    name: 'SearchTags',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: true,
      title: '标签检索'
    }
  }
]
