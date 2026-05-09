import CreationLayout from '@/views/creation/CreationLayout.vue'

export default [
  {
    path: '/creation',
    component: CreationLayout,
    meta: {
      requiresAuth: true,
      title: '创作中心'
    },
    children: [
      {
        path: '',
        redirect: '/creation/workspace'
      },
      {
        path: 'center',
        redirect: '/creation/workspace'
      },
      {
        path: 'workspace',
        name: 'CreationWorkspace',
        component: () => import('@/views/creation/CreationWorkspace.vue'),
        meta: {
          requiresAuth: true,
          title: '创作工作台'
        }
      },
      {
        path: 'processing',
        name: 'CollectionProcessing',
        component: () => import('@/views/creation/CollectionProcessing.vue'),
        meta: {
          requiresAuth: true,
          title: '收藏项加工'
        }
      },
      {
        path: 'notes',
        name: 'NoteManagement',
        component: () => import('@/views/creation/NoteManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '笔记管理'
        }
      },
      {
        path: 'drafts',
        name: 'DraftManagement',
        component: () => import('@/views/creation/DraftManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '草稿管理'
        }
      },
      {
        path: 'notes/:id',
        name: 'NoteDetailPage',
        component: () => import('@/views/creation/NoteDetailPage.vue'),
        meta: {
          requiresAuth: true,
          title: '笔记详情',
          fullscreen: true
        }
      },
      {
        path: 'collection-items/:id/note/create',
        name: 'CollectionNoteCreation',
        component: () => import('@/views/creation/CollectionNoteCreation.vue'),
        meta: {
          requiresAuth: true,
          title: '基于收藏项创建笔记',
          fullscreen: true
        }
      }
    ]
  }
]
