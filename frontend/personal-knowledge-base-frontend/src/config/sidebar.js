// 侧边栏配置文件
export const SIDEBAR_CONFIG = {
  collect: [
    { id: 'all', text: '全部收藏', icon: 'fas fa-star' },
    { id: 'discover', text: '发现', icon: 'fas fa-compass' },
    { id: 'tags', text: '标签', icon: 'fas fa-tag' },
    { id: 'my-collections', text: '我的收藏集', icon: 'fas fa-bookmark' },
    { id: 'recycle', text: '回收站', icon: 'fas fa-trash-alt' },
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
      group: '搜索',
      icon: 'fas fa-search',
      items: [
        {
          id: 'search',
          text: '内容检索',
          icon: 'fas fa-search',
          route: '/search',
          description: '快速查找你的内容',
          badge: 'core',
          importance: 'high'
        }
      ]
    },
    {
      group: '发现',
      icon: 'fas fa-compass',
      items: [
        {
          id: 'trends',
          text: '热门搜索',
          icon: 'fas fa-fire',
          route: '/search/trends',
          description: '今日热搜排行',
          importance: 'medium'
        },
        {
          id: 'my-searches',
          text: '我的搜索',
          icon: 'fas fa-star',
          route: '/search/my-searches',
          description: '常用搜索和历史记录',
          badge: 'dynamic',
          importance: 'high'
        }
      ]
    },
    {
      group: '分析',
      icon: 'fas fa-chart-bar',
      items: [
        {
          id: 'analytics',
          text: '搜索分析',
          icon: 'fas fa-chart-bar',
          route: '/search/analytics',
          description: '搜索行为统计',
          importance: 'medium'
        }
      ]
    },
    {
      group: '高级',
      icon: 'fas fa-sliders-h',
      items: [
        {
          id: 'advanced',
          text: '高级搜索',
          icon: 'fas fa-sliders-h',
          route: '/search/advanced',
          description: '复杂条件组合搜索',
          importance: 'medium'
        }
      ]
    },
    {
      group: '设置',
      icon: 'fas fa-cog',
      items: [
        {
          id: 'settings',
          text: '搜索设置',
          icon: 'fas fa-cog',
          route: '/search/settings',
          description: '自定义搜索体验',
          importance: 'low'
        }
      ]
    }
  ],

  personal: [
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
      route: '/personal/collections'
    },
    {
      id: 'personal-processing',
      text: '加工管理',
      icon: 'fas fa-cogs',
      route: '/personal/processing'
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
  ],

  dashboard: [
    { id: 'overview', text: '概览', icon: 'fas fa-chart-line', route: '/dashboard' },
    { id: 'collections', text: '我的收藏', icon: 'fas fa-bookmark', route: '/dashboard/collections' },
    { id: 'notes', text: '我的笔记', icon: 'fas fa-sticky-note', route: '/dashboard/notes' },
    { id: 'tasks', text: '待办任务', icon: 'fas fa-tasks', route: '/dashboard/tasks' }
  ]
}
