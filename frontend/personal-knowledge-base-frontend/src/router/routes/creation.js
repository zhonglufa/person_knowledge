// 创作中心正式主入口与工作台路由配置
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
        path: 'workspace',
        name: 'CreationWorkspace',
        component: () => import('@/views/creation/CreationWorkspace.vue'),
        meta: {
          requiresAuth: true,
          title: '创作工作台'
        }
      },
      {
        path: 'processing/tasks',
        name: 'CreationProcessingTasks',
        component: () => import('@/views/creation/CollectionProcessing.vue'),
        meta: {
          requiresAuth: true,
          title: '待加工内容'
        }
      },
      {
        path: 'processing',
        name: 'CreationProcessingManagement',
        component: () => import('@/views/creation/ProcessingManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '加工管理'
        }
      },
      {
        path: 'notes',
        name: 'CreationNotes',
        component: () => import('@/views/creation/NoteManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '我的笔记'
        }
      },
      {
        path: 'notes/create',
        name: 'CreationNoteCreate',
        component: () => import('@/views/creation/NoteDetailPage.vue'),
        meta: {
          requiresAuth: true,
          title: '新建笔记'
        }
      },
      {
        path: 'notes/:id',
        name: 'CreationNoteDetail',
        component: () => import('@/views/creation/NoteDetailPage.vue'),
        meta: {
          requiresAuth: true,
          title: '笔记详情'
        }
      },
      {
        path: 'drafts',
        name: 'CreationDrafts',
        component: () => import('@/views/creation/DraftManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '草稿箱'
        }
      }
    ]
  },
  {
    path: '/creation/center',
    redirect: '/creation/workspace'
  },
  {
    path: '/creation/categories',
    redirect: '/taxonomy/categories'
  },
  {
    path: '/creation/tags',
    redirect: '/taxonomy/tags'
  },
  {
    path: '/collections/items/:id/note/create',
    name: 'CollectionNoteCreation',
    component: () => import('@/views/creation/CollectionNoteCreation.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      title: '收藏项笔记创作'
    }
  },
  {
    path: '/collection-item/:id/note/create',
    redirect: to => `/collections/items/${to.params.id}/note/create`
  }
]
