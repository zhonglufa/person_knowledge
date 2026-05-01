// 分类标签管理路由配置
import PageLayout from '@/components/layout/PageLayout.vue'

export default [
  {
    path: '/taxonomy',
    component: PageLayout,
    props: {
      activeNav: 'personal',
      showSidebar: true,
      sidebarItems: [
        {
          id: 'taxonomy-categories',
          text: '分类管理',
          icon: 'fas fa-folder-open',
          route: '/taxonomy/categories'
        },
        {
          id: 'taxonomy-tags',
          text: '标签管理',
          icon: 'fas fa-tag',
          route: '/taxonomy/tags'
        }
      ],
      sidebarHeaderConfig: {
        title: '知识结构',
        shortTitle: '结构',
        icon: 'fas fa-sitemap'
      },
      sidebarStatsConfig: {
        title: '结构管理',
        icon: 'fas fa-sitemap',
        items: []
      }
    },
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
