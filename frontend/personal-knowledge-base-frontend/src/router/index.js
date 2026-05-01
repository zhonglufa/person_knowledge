import Vue from 'vue'
import Router from 'vue-router'
import allRoutes from './routes'
import store from "@/store/index.js";

Vue.use(Router)

const router = new Router({
  mode: 'history',
  routes: allRoutes,
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title + ' - 个人知识库'
  }

  if (to.meta.description) {
    const metaDesc = document.querySelector('meta[name="description"]');
    if (metaDesc) {
      metaDesc.content = to.meta.description;
    }
  }

  if (to.meta.title) {
    const ogTitle = document.querySelector('meta[property="og:title"]');
    if (ogTitle) {
      ogTitle.content = to.meta.title + ' - 个人知识库';
    }
  }

  if (to.path === '/') {
    const token = localStorage.getItem('token');
    if (token) {
      next('/personal/dashboard');
    } else {
      next('/guest/home');
    }
    return;
  }

  if (to.path === '/guest/home') {
    const token = localStorage.getItem('token') || store.state.user.token;

    if (token) {
      try {
        const userInfo = store.state.user.userInfo;
        if (!userInfo || !userInfo.id) {
          await store.dispatch('user/fetchUserInfo');
          if (to.path !== router.currentRoute.path) {
            return;
          }
        }
        next('/personal/dashboard');
      } catch (error) {
        console.warn('游客首页登录态校验失败:', error);
        store.commit('user/CLEAR_USER_STATE');
        next();
      }
      return;
    }
  }

  if (to.matched.some(record => record.meta.requiresAuth)) {
    const isAdminRoute = to.matched.some(record => record.meta.isAdmin);

    let token;
    let userInfo;

    if (isAdminRoute) {
      token = localStorage.getItem('adminToken');
      if (token) {
        const adminInfoStr = localStorage.getItem('adminInfo');
        if (adminInfoStr) {
          try {
            userInfo = JSON.parse(adminInfoStr);
          } catch (e) {
            userInfo = null;
          }
        }
      }

      if (!token) {
        next({
          path: '/admin/login',
          query: { redirect: to.fullPath }
        })
        return;
      }
    } else {
      token = localStorage.getItem('token');

      if (!token) {
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
        return;
      }

      userInfo = store.state.user.userInfo;

      if (!userInfo || !userInfo.id) {
        try {
          await store.dispatch('user/fetchUserInfo');
          if (to.path !== router.currentRoute.path) {
            return;
          }
          next();
        } catch (error) {
          console.warn('Token验证失败:', error);
          store.commit('user/CLEAR_USER_STATE');

          if (to.path !== '/login') {
            next({
              path: '/login',
              query: { redirect: to.fullPath }
            });
          } else {
            next(false);
          }
        }
        return;
      }
    }

    next();
  }
  else {
    next();
  }
})

export default router
