// 分类标签管理路由配置
import AdminLayout from '@/components/layout/AdminLayout.vue'

export default [
  {
    path: '/taxonomy',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      title: '结构管理'
    },
    children: [
      {
        path: '',
        redirect: '/taxonomy/categories'
      },
      {
        path: 'categories',
        name: 'TaxonomyCategories',
        component: () => import('@/views/taxonomy/CategoryManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '分类管理'
        }
      },
      {
        path: 'tags',
        name: 'TaxonomyTags',
        component: () => import('@/views/taxonomy/TagManagement.vue'),
        meta: {
          requiresAuth: true,
          title: '标签管理'
        }
      }
    ]
  }
]
