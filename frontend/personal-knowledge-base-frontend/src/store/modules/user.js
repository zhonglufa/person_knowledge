import { authApi } from '@/api';

// 防止重复请求的标志
let isFetchingUserInfo = false;
let pendingUserInfoPromise = null;

// 用户模块的状态
const state = {
  // 用户基本信息
  userInfo: null,
  // 认证令牌
  token: localStorage.getItem('token') || null,
  // 加载状态
  loading: false,
  // 错误信息
  error: null,
  // 是否已认证
  isAuthenticated: !!(localStorage.getItem('token')),
  // 用户设置信息
  userSettings: null
};

// Getter 函数用于获取状态
const getters = {
  // 获取用户信息
  getUserInfo: state => state.userInfo,
  // 获取认证状态
  isAuthenticated: state => !!state.token && !!state.userInfo,
  // 获取加载状态
  isLoading: state => state.loading,
  // 获取错误信息
  getError: state => state.error,
  // 获取用户角色（如果有的话）
  getUserRole: state => state.userInfo?.role || null,
  // 获取用户昵称
  getNickname: state => state.userInfo?.nickname || state.userInfo?.username || '用户',
  // 获取用户头像
  getAvatar: state => state.userInfo?.avatar || null,
  // 获取用户设置
  getUserSettings: state => state.userSettings
};

// 同步修改状态的 Mutation
const mutations = {
  // 设置用户信息
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo;
  },
  // 设置认证令牌
  SET_TOKEN(state, token) {
    state.token = token;
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  },
  // 设置加载状态
  SET_LOADING(state, loading) {
    state.loading = loading;
  },
  // 设置错误信息
  SET_ERROR(state, error) {
    state.error = error;
  },
  // 设置认证状态
  SET_AUTHENTICATED(state, isAuthenticated) {
    state.isAuthenticated = isAuthenticated;
  },
  // 清除用户状态
  CLEAR_USER_STATE(state) {
    state.userInfo = null;
    state.token = null;
    state.isAuthenticated = false;
    state.error = null;
    state.userSettings = null;
    localStorage.removeItem('token');
  },
  // 设置用户设置信息
  SET_USER_SETTINGS(state, settings) {
    state.userSettings = { ...state.userSettings, ...settings };
  }
};

// 异步操作的 Action
const actions = {
  // 用户登录
  async login({ commit }, credentials) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const response = await authApi.login(credentials);

      // 检查后端响应格式
      if (response && typeof response === 'object' && 'code' in response) {
        // 后端返回标准格式 {code, message, data}
        if (response.code === 200) {
          // 登录成功
          const loginData = response.data;
          if (loginData && loginData.token) {
            commit('SET_TOKEN', loginData.token);
            // 从登录数据中提取用户信息（排除token等非用户字段）
            if (loginData.userInfo) {
              const userInfo = {
                id: loginData.userInfo.id,
                username: loginData.userInfo.username,
                nickname: loginData.userInfo.nickname,
                email: loginData.userInfo.email,
                avatar: loginData.userInfo.avatar,
                createdAt: loginData.userInfo.createdAt,
                updatedAt: loginData.userInfo.updatedAt,
                deleted: loginData.userInfo.deleted
              };
              commit('SET_USER_INFO', userInfo);
            }
            commit('SET_AUTHENTICATED', true);
            return loginData.userInfo;
          } else {
            throw new Error('登录失败：未收到认证令牌');
          }
        } else {
          // 登录失败，使用后端返回的错误信息
          const errorMessage = response.message || '登录失败';
          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        }
      } else {
        // 兼容旧格式
        const { token, user } = response.data;
        commit('SET_TOKEN', token);
        commit('SET_USER_INFO', user);
        commit('SET_AUTHENTICATED', true);
        return user;
      }
    } catch (error) {
      // 处理网络错误等其他错误
      const errorMessage = error.message || '登录失败，请检查账号和密码';
      commit('SET_ERROR', errorMessage);
      throw new Error(errorMessage);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  // 用户注册
  async register({ commit }, userData) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const response = await authApi.register(userData);

      // 处理后端返回的标准响应格式 {code, message, data}
      if (response && typeof response === 'object' && 'code' in response) {
        if (response.code === 200) {
          // 注册成功，但没有返回 token 和 user 数据
          // 可以考虑后续让后端返回 token 或者引导用户去登录
          return { message: response.message || '注册成功' };
        } else {
          // 注册失败
          const errorMessage = response.message || '注册失败';
          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        }
      } else {
        // 兼容旧格式或其他格式
        const { token, user } = response.data || {};
        if (token && user) {
          // 提交用户信息和令牌到状态
          commit('SET_TOKEN', token);
          commit('SET_USER_INFO', user);
          commit('SET_AUTHENTICATED', true);
          return user;
        } else {
          // 没有返回认证信息，只返回成功消息
          return { message: '注册成功' };
        }
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || '注册失败，请稍后重试';
      commit('SET_ERROR', errorMessage);
      throw new Error(errorMessage);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  // 用户登出
  async logout({ commit }) {
    try {
      // 调用登出API
      await authApi.logout();
    } catch (error) {
      // 即使API失败也要清除本地状态
      console.warn('登出API调用失败，但仍需清除本地状态', error);
    } finally {
      // 清除用户状态
      commit('CLEAR_USER_STATE');
    }
  },

  // 获取用户信息
  async fetchUserInfo({ commit, state }) {
    // 如果已经有用户信息且token有效，直接返回
    if (state.userInfo && state.token) {
      return state.userInfo;
    }

    // 检查是否有token
    const token = localStorage.getItem('token') || state.token;
    if (!token) {
      throw new Error('未认证，无法获取用户信息');
    }

    // 防止重复请求
    if (isFetchingUserInfo) {
      // 如果正在请求中，返回已存在的Promise
      return pendingUserInfoPromise;
    }

    // 设置请求状态
    isFetchingUserInfo = true;
    commit('SET_LOADING', true);

    // 创建新的Promise
    pendingUserInfoPromise = (async () => {
      try {
        const response = await authApi.getUserInfo();

        // 处理后端返回的标准响应格式 {code, message, data}
        if (response && typeof response === 'object' && 'code' in response) {
          if (response.code === 200) {
            const user = response.data;
            if (!user) {
              throw new Error('用户信息为空');
            }
            commit('SET_USER_INFO', user);
            commit('SET_AUTHENTICATED', true);
            return user;
          } else {
            // API返回错误码
            const errorMessage = response.message || '获取用户信息失败';
            commit('SET_ERROR', errorMessage);
            throw new Error(errorMessage);
          }
        } else {
          // 兼容旧格式
          const user = response.data;
          if (!user) {
            throw new Error('用户信息为空');
          }
          commit('SET_USER_INFO', user);
          commit('SET_AUTHENTICATED', true);
          return user;
        }
      } catch (error) {
        // 区分不同类型的错误
        if (error.response) {
          // 服务器返回错误响应
          const status = error.response.status;
          let errorMessage = '获取用户信息失败';

          if (status === 401) {
            errorMessage = '认证已过期，请重新登录';
          } else if (status === 403) {
            errorMessage = '权限不足';
          } else {
            errorMessage = error.response.data?.message || `服务器错误 (${status})`;
          }

          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        } else if (error.request) {
          // 网络错误
          const errorMessage = '网络连接失败，请检查网络设置';
          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        } else {
          // 其他错误
          const errorMessage = error.message || '获取用户信息失败';
          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        }
      } finally {
        // 重置请求状态
        isFetchingUserInfo = false;
        pendingUserInfoPromise = null;
        commit('SET_LOADING', false);
      }
    })();

    return pendingUserInfoPromise;
  },

  // 初始化用户状态（页面刷新后恢复状态）
  async initializeAuth({ commit, dispatch }) {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        // 尝试获取用户信息来验证token有效性
        const userInfo = await dispatch('fetchUserInfo');
        commit('SET_AUTHENTICATED', true);
        return userInfo;
      } catch (error) {
        // token无效，清除本地存储
        console.warn('Token无效，清除认证状态:', error.message);
        commit('CLEAR_USER_STATE');
        return null;
      }
    }
    return null;
  },

  // 发送验证码
  async sendVerifyCode({ commit }, emailData) {
    commit('SET_LOADING', true);
    try {
      const response = await authApi.sendVerifyCode(emailData);

      // 检查后端响应格式
      if (response && typeof response === 'object' && 'code' in response) {
        // 后端返回标准格式 {code, message, data}
        if (response.code === 200) {
          // 发送成功
          return response.data;
        } else {
          // 发送失败，使用后端返回的错误信息
          const errorMessage = response.message || '发送验证码失败';
          commit('SET_ERROR', errorMessage);
          throw new Error(errorMessage);
        }
      } else {
        // 兼容旧格式
        return response.data;
      }
    } catch (error) {
      const errorMessage = error.message || '发送验证码失败';
      commit('SET_ERROR', errorMessage);
      throw new Error(errorMessage);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  // ==================== 用户设置相关 ====================

  /**
   * 获取用户设置
   */
  async fetchUserSettings({ commit, state }) {
    // 如果已经有设置信息，直接返回
    if (state.userSettings) {
      return state.userSettings;
    }

    commit('SET_LOADING', true);
    try {
      const { userApi } = await import('@/api/user');
      const response = await userApi.getUserSettings();

      if (response && response.data) {
        commit('SET_USER_SETTINGS', response.data);
        return response.data;
      }
      return null;
    } catch (error) {
      console.error('获取用户设置失败:', error);
      // 设置失败时不抛出错误，返回null
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 更新用户设置
   * @param {Object} settingsData - 设置数据
   */
  async updateUserSettings({ commit }, settingsData) {
    commit('SET_LOADING', true);
    try {
      const { userApi } = await import('@/api/user');
      const response = await userApi.updateUserSettings(settingsData);

      if (response && response.data) {
        commit('SET_USER_SETTINGS', response.data);
        return response.data;
      }
      return response;
    } catch (error) {
      console.error('更新用户设置失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  }
};

export default {
  namespaced: true, // 命名空间，避免与其他模块冲突
  state,
  getters,
  mutations,
  actions
};
