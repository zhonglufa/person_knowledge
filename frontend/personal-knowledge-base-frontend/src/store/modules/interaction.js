import {
  likeContent,
  unlikeContent,
  getComments,
  commentContent,
  collectContent,
  uncollectContent,
  checkLikeStatus,
  checkCollectStatus
} from '@/api/interaction';

// 状态
const state = {
  // 点赞状态缓存，格式: { 'targetType_targetId': true/false }
  likeStatus: {},
  // 评论列表
  commentList: [],
  // 评论分页信息
  commentPagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  // 收藏状态缓存，格式: { 'targetType_targetId': true/false }
  collectStatus: {},
  // 加载状态
  loading: false
};

// Getters
const getters = {
  // 获取点赞状态
  getLikeStatus: (state) => (targetId, targetType) => {
    const key = `${targetType}_${targetId}`;
    return state.likeStatus[key] || false;
  },
  // 获取收藏状态
  getCollectStatus: (state) => (targetId, targetType) => {
    const key = `${targetType}_${targetId}`;
    return state.collectStatus[key] || false;
  },
  // 获取评论列表
  getCommentList: (state) => state.commentList,
  // 获取评论总数
  getCommentTotal: (state) => state.commentPagination.total
};

// Mutations
const mutations = {
  // 设置点赞状态
  SET_LIKE_STATUS(state, { targetId, targetType, isLiked }) {
    const key = `${targetType}_${targetId}`;
    state.likeStatus[key] = isLiked;
  },
  // 设置评论列表
  SET_COMMENT_LIST(state, { list, total, page, pageSize }) {
    state.commentList = list || [];
    state.commentPagination = {
      currentPage: page || 1,
      pageSize: pageSize || 10,
      total: total || 0
    };
  },
  // 添加评论到列表
  ADD_COMMENT(state, comment) {
    state.commentList.unshift(comment);
    state.commentPagination.total += 1;
  },
  // 设置收藏状态
  SET_COLLECT_STATUS(state, { targetId, targetType, isCollected }) {
    const key = `${targetType}_${targetId}`;
    state.collectStatus[key] = isCollected;
  },
  // 设置加载状态
  SET_LOADING(state, loading) {
    state.loading = loading;
  }
};

// Actions
const actions = {
  /**
   * 切换点赞状态
   * @param {Object} data - { targetId, targetType }
   */
  async toggleLike({ commit, state }, data) {
    const { targetId, targetType } = data;
    const key = `${targetType}_${targetId}`;
    const isCurrentlyLiked = state.likeStatus[key] || false;

    commit('SET_LOADING', true);
    try {
      if (isCurrentlyLiked) {
        // 取消点赞
        await unlikeContent(targetId, targetType);
        commit('SET_LIKE_STATUS', { targetId, targetType, isLiked: false });
      } else {
        // 点赞
        await likeContent(targetId, targetType);
        commit('SET_LIKE_STATUS', { targetId, targetType, isLiked: true });
      }
      return { success: true };
    } catch (error) {
      console.error('切换点赞状态失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 获取评论列表
   * @param {Object} params - { targetId, targetType, currentPage, pageSize }
   */
  async fetchCommentList({ commit }, params) {
    const { targetId, targetType, currentPage = 1, pageSize = 10 } = params;

    commit('SET_LOADING', true);
    try {
      const response = await getComments(targetId, targetType, currentPage, pageSize);
      
      // 处理后端响应格式
      let commentList = [];
      let total = 0;
      
      if (response && response.data) {
        const data = response.data;
        // 分页格式
        if (data.records) {
          commentList = data.records;
          total = data.total || 0;
        } else if (Array.isArray(data)) {
          commentList = data;
          total = data.length;
        }
      }

      commit('SET_COMMENT_LIST', {
        list: commentList,
        total,
        page: currentPage,
        pageSize
      });
      
      return response;
    } catch (error) {
      console.error('获取评论列表失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 提交评论
   * @param {Object} data - { targetId, targetType, content }
   */
  async submitComment({ commit }, data) {
    const { targetId, targetType, content } = data;

    commit('SET_LOADING', true);
    try {
      const response = await commentContent(targetId, targetType, content);
      
      // 将新评论添加到列表
      if (response && response.data) {
        commit('ADD_COMMENT', response.data);
      }
      
      return response;
    } catch (error) {
      console.error('提交评论失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 切换收藏状态
   * @param {Object} data - { targetId, targetType }
   */
  async toggleCollect({ commit, state }, data) {
    const { targetId, targetType } = data;
    const key = `${targetType}_${targetId}`;
    const isCurrentlyCollected = state.collectStatus[key] || false;

    commit('SET_LOADING', true);
    try {
      if (isCurrentlyCollected) {
        // 取消收藏
        await uncollectContent(targetId, targetType);
        commit('SET_COLLECT_STATUS', { targetId, targetType, isCollected: false });
      } else {
        // 收藏
        await collectContent(targetId, targetType);
        commit('SET_COLLECT_STATUS', { targetId, targetType, isCollected: true });
      }
      return { success: true };
    } catch (error) {
      console.error('切换收藏状态失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  /**
   * 检查点赞状态
   * @param {Object} data - { targetId, targetType }
   */
  async checkLikeStatusAction({ commit }, data) {
    const { targetId, targetType } = data;
    try {
      const response = await checkLikeStatus(targetId, targetType);
      const isLiked = response.data === true || response.data?.liked === true;
      commit('SET_LIKE_STATUS', { targetId, targetType, isLiked });
      return isLiked;
    } catch (error) {
      console.error('检查点赞状态失败:', error);
      return false;
    }
  },

  /**
   * 检查收藏状态
   * @param {Object} data - { targetId, targetType }
   */
  async checkCollectStatusAction({ commit }, data) {
    const { targetId, targetType } = data;
    try {
      const response = await checkCollectStatus(targetId, targetType);
      const isCollected = response.data === true || response.data?.collected === true;
      commit('SET_COLLECT_STATUS', { targetId, targetType, isCollected });
      return isCollected;
    } catch (error) {
      console.error('检查收藏状态失败:', error);
      return false;
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
