<template>
  <div id="app">
    <transition name="page-fade" mode="out-in">
      <router-view/>
    </transition>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'App',
  components: {
  },
  data() {
    return {
      //启动时就恢复用户状态
      isInitializing: false
    };
  },
  async created() {
    // 应用初始化时尝试恢复用户状态
    await this.initializeAuthState();
  },
  methods: {
    ...mapActions('user', ['fetchUserInfo']),
    
    async initializeAuthState() {
      // 防止重复初始化
      if (this.isInitializing) {
        return;
      }
      
      this.isInitializing = true;
      
      try {
        // 检查是否有存储的token
        const token = localStorage.getItem('token');
        if (token) {
          // 检查Vuex中是否已有用户信息
          const userInfo = this.$store.state.user.userInfo;
          if (!userInfo || !userInfo.id) {
            // 尝试获取用户信息来验证token有效性
            await this.fetchUserInfo();
          }
        }
      } catch (error) {
        console.warn('初始化认证状态失败', error);
        // 如果获取用户信息失败，清除无效的token
        localStorage.removeItem('token');
        // 清除Vuex中的用户状态
        this.$store.commit('user/CLEAR_USER_STATE');
      } finally {
        this.isInitializing = false;
      }
    }
  }
}
</script>

<style scoped>
</style>
