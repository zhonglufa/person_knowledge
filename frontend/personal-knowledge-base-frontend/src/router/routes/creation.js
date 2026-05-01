// 创作中心路由配置
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
        path: 'processing',
        name: 'CreationProcessing',
        component: () => import('@/views/creation/CollectionProcessing.vue'),
        meta: {
          requiresAuth: true,
          title: '待加工内容'
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
    path: '/collection-item/:id/note/create',
    name: 'CollectionNoteCreation',
    component: () => import('@/views/creation/CollectionNoteCreation.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      title: '收藏项笔记创作'
    }
  }
]
