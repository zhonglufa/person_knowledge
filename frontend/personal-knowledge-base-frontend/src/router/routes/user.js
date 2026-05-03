import LoginPage from '@/views/auth/Login.vue'
import GuestHome from '@/views/guest/Home.vue'
import CollectionCenter from '@/views/collect/CollectionCenter.vue'
import CollectionDetailPage from '@/views/collection/CollectionDetailPage.vue'
import CollectionListPage from '@/views/collection/CollectionListPage.vue'

export default [
  {
    path: '/login',
    name: 'Login',
    component: LoginPage,
    meta: {
      requiresAuth: false,
      title: '登录'
    }
  },
  {
    path: '/',
    name: 'Root',
    component: GuestHome,
    meta: {
      requiresAuth: false,
      title: '首页'
    }
  },
  {
    path: '/guest/home',
    name: 'GuestHome',
    component: GuestHome,
    meta: {
      requiresAuth: false,
      title: '游客首页'
    }
  },
  {
    path: '/collect/center',
    name: 'CollectionCenter',
    component: CollectionCenter,
    meta: {
      requiresAuth: false,
      title: '收藏中心'
    }
  },
  {
    path: '/collections',
    redirect: '/collect/center?tab=my-collections'
  },
  {
    path: '/collections/list',
    name: 'CollectionListPage',
    component: CollectionListPage,
    meta: {
      requiresAuth: false,
      title: '我的收藏集'
    }
  },
  {
    path: '/collection-item/:id',
    redirect: to => `/creation/collection-items/${to.params.id}/note/create`
  },
  {
    path: '/collection-item/:id/note/create',
    redirect: to => `/creation/collection-items/${to.params.id}/note/create`
  },
  {
    path: '/collections/manage',
    name: 'CollectionManagementPage',
    component: () => import('@/views/collection/CollectionManagement.vue'),
    meta: {
      requiresAuth: true,
      title: '收藏管理'
    }
  },
  {
    path: '/collections/items/:id',
    name: 'CollectionItemDetailPage',
    component: CollectionItemWorkspace,
    props: false,
    meta: {
      requiresAuth: true,
      title: '收藏项详情'
    }
  },
  {
    path: '/collection-item/:id',
    redirect: to => `/collections/items/${to.params.id}`
  },
  {
    path: '/collections/manage',
    name: 'CollectionManagementPage',
    component: () => import('@/views/collection/CollectionManagement.vue'),
    meta: {
      requiresAuth: true,
      title: '收藏管理'
    }
  },
  {
    path: '/collections/:id',
    name: 'CollectionDetailPage',
    component: CollectionDetailPage,
    props: false,
    meta: {
      requiresAuth: false,
      title: '收藏集详情'
    }
  }
]
