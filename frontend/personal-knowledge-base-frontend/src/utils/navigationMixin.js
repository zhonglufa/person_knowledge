import { NAV_ITEMS, USER_MENU_ITEMS, DEFAULT_USER_INFO, DEFAULT_NOTIFICATION_COUNT } from '@/config/navigation';

// 通用导航混入
export const navigationMixin = {
  data() {
    return {
      // 导航数据
      navItems: NAV_ITEMS,
      user: DEFAULT_USER_INFO,
      notificationCount: DEFAULT_NOTIFICATION_COUNT,
      
      // 激活的导航项（需要在各组件中覆盖）
      activeNav: ''
    };
  },
  
  methods: {
    // 侧边栏切换
    toggleSidebar() {
      this.$emit('toggle-sidebar');
    },
    
    // 导航点击处理
    handleNavClick(item) {
      this.activeNav = item.id;
      this.$router.push(item.url);
    },
    
    // 用户命令处理
    handleUserCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/personal/profile');
          break;
        case 'collections':
          this.$router.push('/personal/collections');
          break;
        case 'settings':
          this.$router.push('/personal/settings');
          break;
        case 'logout':
          this.handleLogout();
          break;
        default:
          console.warn('未知的用户命令:', command);
      }
    },
    
    // 登出处理
    handleLogout() {
      this.$confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.$store) {
          this.$store.commit('user/LOGOUT');
        }
        
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        sessionStorage.removeItem('user');
        
        this.$message.success('已退出登录');
        this.$router.push('/');
      }).catch(() => {
      });
    },
    
    // 搜索处理
    handleSearch(keyword) {
      if (keyword && keyword.trim()) {
        this.$emit('global-search', keyword.trim());
        this.$router.push({
          path: '/search/global',
          query: { q: keyword.trim() }
        });
      }
    }
  }
};

export default navigationMixin;
