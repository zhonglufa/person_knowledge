// 导航配置文件
export const NAV_ITEMS = [
  { 
    id: 'personal', 
    text: '个人中心',
    icon: 'fas fa-compass', 
    url: '/personal/center' 
  },
  { 
    id: 'discover', 
    text: '发现', 
    icon: 'fas fa-fire', 
    url: '/discover/square' 
  },
  { 
    id: 'creation', 
    text: '创作', 
    icon: 'fas fa-pen', 
    url: '/creation/workspace' 
  },
  { 
    id: 'search', 
    text: '搜索', 
    icon: 'fas fa-search', 
    url: '/search/center' 
  },
  { 
    id: 'collect', 
    text: '收藏', 
    icon: 'fas fa-star', 
    url: '/collect/center' 
  }

];

export const USER_MENU_ITEMS = [
  { 
    command: 'profile', 
    text: '个人中心', 
    icon: 'fas fa-user' 
  },
  { 
    command: 'collections', 
    text: '我的收藏', 
    icon: 'fas fa-bookmark' 
  },
  { 
    command: 'settings', 
    text: '账户设置', 
    icon: 'fas fa-cog' 
  },
  { 
    command: 'logout', 
    text: '退出登录', 
    icon: 'fas fa-sign-out-alt',
    divided: true 
  }
];

export const DEFAULT_USER_INFO = {
  name: '用户名',
  avatar: 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg'
};

export const DEFAULT_NOTIFICATION_COUNT = 5;

export default {
  NAV_ITEMS,
  USER_MENU_ITEMS,
  DEFAULT_USER_INFO,
  DEFAULT_NOTIFICATION_COUNT
};