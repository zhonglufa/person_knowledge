// 检索中心路由配置
export default [
  {
    path: '/search/center',
    name: 'SearchCenter',
    component: () => import('@/views/search/SearchCenter.vue'),
    meta: {
      requiresAuth: false,
      title: '检索中心'
    }
  },
  {
    path: '/search/global',
    name: 'SearchGlobal',
    component: () => import('@/views/search/GlobalSearch.vue'),
    meta: {
      requiresAuth: false,
      title: '全局检索'
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
    component: () => import('@/views/search/CollectionSearch.vue'),
    meta: {
      requiresAuth: false,
      title: '收藏项检索'
    }
  },
  {
    path: '/search/notes',
    name: 'SearchNotes',
    component: () => import('@/views/search/NoteSearch.vue'),
    meta: {
      requiresAuth: false,
      title: '笔记检索'
    }
  }
]
