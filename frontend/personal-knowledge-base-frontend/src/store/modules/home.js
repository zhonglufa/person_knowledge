import { collectionsApi } from '@/api';
import { noteApi } from '@/api';

// 首页模块的状态
const state = {
  // 热门知识卡片
  popularKnowledge: [],
  // 推荐收藏集卡片
  recommendedCollections: [],
  // 加载状态
  loading: false,
  // 错误状态
  loadError: false,
  // 错误信息
  errorMessage: ''
};

// Getter 函数用于获取状态
const getters = {
  // 获取热门知识卡片
  getPopularKnowledge: state => state.popularKnowledge,
  // 获取推荐收藏集卡片
  getRecommendedCollections: state => state.recommendedCollections,
  // 获取加载状态
  isLoading: state => state.loading,
  // 获取错误状态
  hasLoadError: state => state.loadError
};

// 同步修改状态的 Mutation
const mutations = {
  // 设置热门知识卡片
  SET_POPULAR_KNOWLEDGE(state, knowledge) {
    state.popularKnowledge = knowledge;
  },
  // 设置推荐收藏集卡片
  SET_RECOMMENDED_COLLECTIONS(state, collections) {
    state.recommendedCollections = collections;
  },
  // 设置加载状态
  SET_LOADING(state, loading) {
    state.loading = loading;
  },
  // 设置错误状态
  SET_LOAD_ERROR(state, error) {
    state.loadError = error;
  },
  // 设置错误信息
  SET_ERROR_MESSAGE(state, message) {
    state.errorMessage = message;
  },
  // 清除错误状态
  CLEAR_ERROR(state) {
    state.loadError = false;
    state.errorMessage = '';
  }
};

// 异步操作的 Action
const actions = {
  // 获取首页数据
  async fetchHomeData({ commit, dispatch }, { retryCount = 0, maxRetries = 2 } = {}) {
    commit('SET_LOADING', true);
    commit('CLEAR_ERROR');
      
    try {
      // 并行获取热门知识和推荐收藏集（双数据源）
      // 注：这两个接口使用publicAxios，401错误会静默处理
      const [notesRes, collectionsRes] = await Promise.all([
        noteApi.getRecommendedNotes(1, 10),        // 推荐笔记
        collectionsApi.getRecommended(1, 10)       // 推荐收藏集
      ]);
  
      // 设置热门知识（笔记数据）
      // publicAxios返回格式：{ code, message, data }
      // 后端笔记接口返回：{ list: [], total, pageNum, pageSize }
      const notesData = notesRes?.code === 200 ? (notesRes.data?.list || []) : [];
      commit('SET_POPULAR_KNOWLEDGE', notesData);
      
      // 设置推荐收藏集（收藏集数据）
      // 后端收藏集接口返回：Page对象 { records: [], total, current, size }
      const collectionsData = collectionsRes?.code === 200 
        ? (collectionsRes.data?.records || collectionsRes.data?.list || []) 
        : [];
      commit('SET_RECOMMENDED_COLLECTIONS', collectionsData);
        
      commit('SET_LOAD_ERROR', false);
    } catch (error) {
      console.warn('[Home] 加载首页数据失败:', error.message);
        
      // 产品首页为公开页面，API失败时静默处理，不影响页面展示
      // 仅记录错误，不显示错误状态
      const errorMessage = error.response?.data?.message || error.message || '加载数据失败';
      commit('SET_ERROR_MESSAGE', errorMessage);
      
      // 如果还有重试机会，递归重试
      if (retryCount < maxRetries) {
        console.log(`[Home] 重试加载数据，第${retryCount + 1}次`);
        await new Promise(resolve => setTimeout(resolve, 1000)); // 等待1秒后重试
        return await dispatch('fetchHomeData', { retryCount: retryCount + 1, maxRetries });
      } else {
        // 最终失败时，设置为空数据而非错误状态
        console.warn('[Home] 首页数据加载最终失败，使用空数据');
        commit('SET_POPULAR_KNOWLEDGE', []);
        commit('SET_RECOMMENDED_COLLECTIONS', []);
        commit('SET_LOAD_ERROR', false); // 不显示错误状态
      }
    } finally {
      commit('SET_LOADING', false);
    }
  },
  
  // 获取热门知识
  async fetchPopularKnowledge({ commit }, limit = 10) {
    commit('SET_LOADING', true);
    try {
      const response = await collectionsApi.getRecommended(limit);
      commit('SET_POPULAR_KNOWLEDGE', response.data || []);
    } catch (error) {
      console.error('获取热门知识失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  
  // 获取推荐收藏集
  async fetchRecommendedCollections({ commit }, limit = 10) {
    commit('SET_LOADING', true);
    try {
      const response = await collectionsApi.getRecommended(limit);
      commit('SET_RECOMMENDED_COLLECTIONS', response.data || []);
    } catch (error) {
      console.error('获取推荐收藏集失败:', error);
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