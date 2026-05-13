export default [
  {
    path: '/discover',
    redirect: '/discover/square',
    meta: {
      title: '发现',
      requiresAuth: false
    }
  },
  {
    path: '/discover/square',
    name: 'PublicNoteSquare',
    component: () => import('@/views/discover/PublicNoteSquare.vue'),
    meta: {
      title: '发现笔记',
      description: '探索社区中的优质知识沉淀',
      requiresAuth: false
    }
  }
]
