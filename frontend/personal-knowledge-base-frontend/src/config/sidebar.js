// 侧边栏配置文件
export const SIDEBAR_CONFIG = {
  collect: [
    { id: 'all', text: '全部收藏', icon: 'fas fa-star' },
    { id: 'discover', text: '发现', icon: 'fas fa-compass' },
    { id: 'tags', text: '标签', icon: 'fas fa-tag' },
    { id: 'my-collections', text: '我的收藏集', icon: 'fas fa-bookmark' },
    { id: 'recycle', text: '回收站', icon: 'fas fa-trash-alt' }
  ],

  creation: [
    {
      id: 'creation-workspace',
      text: '工作台',
      icon: 'fas fa-columns',
      route: '/creation/workspace'
    },
    {
      id: 'creation-processing',
      text: '待加工',
      icon: 'fas fa-seedling',
      route: '/creation/processing'
    },
    {
      id: 'creation-notes',
      text: '我的笔记',
      icon: 'fas fa-note-sticky',
      route: '/creation/notes'
    },
    {
      id: 'creation-drafts',
      text: '草稿箱',
      icon: 'fas fa-file-alt',
      route: '/creation/drafts',
      badge: 'draft'
    }
  ],

  taxonomy: [
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

  search: [
    {
      group: '检索',
      icon: 'fas fa-search',
      items: [
        {
          id: 'search-global',
          text: '综合检索',
          icon: 'fas fa-search',
          route: '/search/global',
          description: '跨收藏集、收藏项、笔记与标签统一检索',
          badge: 'core',
          importance: 'high'
        },
        {
          id: 'search-collections',
          text: '收藏集',
          icon: 'fas fa-folder-open',
          route: '/search/collections',
          description: '按收藏集名称与描述检索',
          importance: 'high'
        },
        {
          id: 'search-items',
          text: '收藏项',
          icon: 'fas fa-bookmark',
          route: '/search/items',
          description: '按标题、摘要、关键词与状态筛选',
          importance: 'high'
        },
        {
          id: 'search-notes',
          text: '笔记',
          icon: 'fas fa-file-alt',
          route: '/search/notes',
          description: '按标题、内容与笔记类型检索',
          importance: 'medium'
        },
        {
          id: 'search-tags',
          text: '标签',
          icon: 'fas fa-tag',
          route: '/search/tags',
          description: '按标签与关联内容检索',
          importance: 'medium'
        },
        {
          id: 'search-history',
          text: '历史记录',
          icon: 'fas fa-history',
          route: '/search/history',
          description: '查看最近搜索记录',
          importance: 'low'
        }
      ]
    }
  ],

  personal: [
    {
      id: 'personal-center',
      text: '个人中心',
      icon: 'fas fa-user-circle',
      route: '/personal/center'
    },
    {
      id: 'personal-dashboard',
      text: '统计看板',
      icon: 'fas fa-chart-line',
      route: '/personal/dashboard'
    },
    {
      id: 'personal-profile',
      text: '个人资料',
      icon: 'fas fa-user',
      route: '/personal/profile'
    },
    {
      id: 'personal-collections',
      text: '收藏管理',
      icon: 'fas fa-folder',
      route: '/collections/manage'
    },
    {
      id: 'personal-processing',
      text: '加工管理',
      icon: 'fas fa-cogs',
      route: '/creation/processing'
    },
    {
      id: 'personal-public',
      text: '公开内容',
      icon: 'fas fa-globe',
      route: '/personal/public'
    },
    {
      id: 'personal-settings',
      text: '账户设置',
      icon: 'fas fa-cog',
      route: '/personal/settings'
    },
    {
      id: 'personal-notifications',
      text: '通知中心',
      icon: 'fas fa-bell',
      route: '/personal/notifications'
    }
  ]
}
