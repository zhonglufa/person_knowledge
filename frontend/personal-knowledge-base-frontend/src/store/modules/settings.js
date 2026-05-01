import request from '@/api/axios';

// 状态
const state = {
  // 主题设置: 'light' | 'dark'
  theme: localStorage.getItem('theme') || 'light',
  // 显示模式: 'grid' | 'list'
  displayMode: localStorage.getItem('displayMode') || 'card',
  // 通知偏好设置
  notifyPreferences: {
    emailNotify: true,
    systemNotify: true,
    collectionNotify: true,
    commentNotify: true,
    likeNotify: true
  },
  // 其他设置
  settings: {},
  // 加载状态
  loading: false
};

// Getters
const getters = {
  // 获取当前主题
  getTheme: (state) => state.theme,
  // 获取显示模式
  getDisplayMode: (state) => state.displayMode,
  // 获取通知偏好
  getNotifyPreferences: (state) => state.notifyPreferences,
  // 获取所有设置
  getSettings: (state) => state.settings,
  // 是否为暗色主题
  isDarkTheme: (state) => state.theme === 'dark'
};

// Mutations
const mutations = {
  // 设置主题
  SET_THEME(state, theme) {
    state.theme = theme;
    localStorage.setItem('theme', theme);
    // 应用主题到DOM
    document.documentElement.setAttribute('data-theme', theme);
  },
  // 设置显示模式
  SET_DISPLAY_MODE(state, mode) {
    state.displayMode = mode;
    localStorage.setItem('displayMode', mode);
  },
  // 设置通知偏好
  SET_NOTIFY_PREFERENCES(state, preferences) {
    state.notifyPreferences = { ...state.notifyPreferences, ...preferences };
  },
  // 设置完整设置信息
  SET_SETTINGS(state, settings) {
    state.settings = { ...state.settings, ...settings };
    // 同步到本地状态
    if (settings.theme) {
      state.theme = settings.theme;
      localStorage.setItem('theme', settings.theme);
    }
    if (settings.displayMode) {
      state.displayMode = settings.displayMode;
      localStorage.setItem('displayMode', settings.displayMode);
    }
    if (settings.notifyPreferences) {
      state.notifyPreferences = { ...state.notifyPreferences, ...settings.notifyPreferences };
    }
  },
  // 设置加载状态
  SET_LOADING(state, loading) {
    state.loading = loading;
  }
};

// Actions
const actions = {
  /**
   * 获取用户设置
   */
  async fetchSettings({ commit }) {
    commit('SET_LOADING', true);
    try {
      const response = await request({
        url: '/user/settings',
        method: 'get'
      });

      if (response && response.data) {
        commit('SET_SETTINGS', response.data);
      }

      return response;
    } catch (error) {
      console.error('获取用户设置失败:', error);
      // 设置失败时不抛出错误，使用默认设置
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 更新用户设置
   * @param {Object} data - 设置数据
   */
  async updateSettings({ commit }, data) {
    commit('SET_LOADING', true);
    try {
      const response = await request({
        url: '/user/settings',
        method: 'put',
        data
      });

      if (response && response.data) {
        commit('SET_SETTINGS', response.data);
      }

      return response;
    } catch (error) {
      console.error('更新用户设置失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 设置主题
   * @param {String} theme - 'light' | 'dark'
   */
  async setTheme({ commit }, theme) {
    commit('SET_THEME', theme);
    
    // 同步到服务器
    try {
      await request({
        url: '/user/settings',
        method: 'put',
        data: { theme }
      });
    } catch (error) {
      console.warn('同步主题设置到服务器失败:', error);
    }
  },

  /**
   * 设置显示模式
   * @param {String} mode - 'card' | 'list'
   */
  async setDisplayMode({ commit }, mode) {
    commit('SET_DISPLAY_MODE', mode);
    
    // 同步到服务器
    try {
      await request({
        url: '/user/settings',
        method: 'put',
        data: { displayMode: mode }
      });
    } catch (error) {
      console.warn('同步显示模式设置到服务器失败:', error);
    }
  },

  /**
   * 更新通知偏好
   * @param {Object} preferences - 通知偏好设置
   */
  async updateNotifyPreferences({ commit }, preferences) {
    commit('SET_NOTIFY_PREFERENCES', preferences);
    
    // 同步到服务器
    try {
      await request({
        url: '/user/settings/notify',
        method: 'put',
        data: { notifyPreferences: preferences }
      });
    } catch (error) {
      console.warn('同步通知偏好设置到服务器失败:', error);
    }
  }
};

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};
