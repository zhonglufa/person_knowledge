import { collectApi } from '@/api/collect';
import { collectionsApi } from '@/api/collections';
import { getPopularTags } from '@/api/tag';

const buildCollectListRequestParams = (params = {}, state) => {
  const requestParams = {};

  requestParams.pageNum = params.pageNum || params.page || state.pagination.currentPage;
  requestParams.pageSize = params.pageSize || state.pagination.pageSize;

  const sourceType = params.sourceType || params.type || state.collectFilterCondition.type;
  if (sourceType && String(sourceType) !== '0') {
    requestParams.sourceType = Number(sourceType);
  }

  if (params.tagId) {
    requestParams.tagId = params.tagId;
  } else if (state.collectFilterCondition.tagId) {
    requestParams.tagId = state.collectFilterCondition.tagId;
  }

  const tagIds = Array.isArray(params.tagIds) && params.tagIds.length > 0
    ? params.tagIds
    : state.collectFilterCondition.tagIds;
  if (Array.isArray(tagIds) && tagIds.length > 0) {
    requestParams.tagIds = tagIds;
  }

  if (params.keyword && params.keyword.trim()) {
    requestParams.keyword = params.keyword.trim();
  } else if (state.searchKeyword && state.searchKeyword.trim()) {
    requestParams.keyword = state.searchKeyword.trim();
  }

  if (params.sortBy) {
    requestParams.sortBy = params.sortBy;
  } else if (state.collectFilterCondition.sortBy) {
    requestParams.sortBy = state.collectFilterCondition.sortBy;
  }

  if (params.sortOrder) {
    requestParams.sortOrder = params.sortOrder;
  } else if (state.collectFilterCondition.sortOrder) {
    requestParams.sortOrder = state.collectFilterCondition.sortOrder;
  }

  return requestParams;
};

const normalizeTagFilterCondition = (condition = {}) => {
  if (Array.isArray(condition.tagIds) && condition.tagIds.length > 0) {
    return {
      ...condition,
      tagId: condition.tagIds[0],
      tagIds: [...condition.tagIds]
    };
  }

  if (condition.tagId) {
    return {
      ...condition,
      tagIds: []
    };
  }

  return condition;
};

const normalizePopularTags = (tags = []) => {
  if (!Array.isArray(tags)) {
    return [];
  }

  return tags.map(tag => ({
    ...tag,
    count: tag.count ?? tag.usageCount ?? 0
  }));
};

const normalizeCollectionResponse = (response) => {
  const data = response?.data || {};
  return {
    records: Array.isArray(data.records) ? data.records : [],
    current: Number(data.current || 1),
    size: Number(data.size || 10),
    total: Number(data.total || 0)
  };
};

const normalizeStatisticsResponse = (stats = {}) => ({
  totalCollects: Number(stats.total ?? stats.totalCollects ?? 0),
  todayCollects: Number(stats.todayCollects ?? 0),
  totalTags: Number(stats.totalTags ?? 0),
  unreadCollects: Number(stats.unreadCount ?? stats.unreadCollects ?? 0),
  recycledCollections: Number(stats.recycledCollections ?? 0)
});

const state = {
  collectList: [],
  pagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  loading: false,
  searchKeyword: '',
  collectTags: [],
  selectedCollectTags: [],
  collectFilterCondition: {
    type: '0',
    tagId: null,
    tagIds: [],
    status: null
  },
  statistics: {
    totalCollects: 0,
    todayCollects: 0,
    totalTags: 0,
    unreadCollects: 0,
    recycledCollections: 0
  }
};

const getters = {
  getTotalCollectItems: (state) => state.statistics.totalCollects,
  getTodayCollectItems: (state) => state.statistics.todayCollects,
  getUnreadCollectItems: (state) => state.statistics.unreadCollects,
  getRecycledCollectItems: (state) => state.statistics.recycledCollections,
  getTotalTags: (state) => state.statistics.totalTags,
  getCurrentPageCollectItems: (state) => {
    const { currentPage, pageSize } = state.pagination;
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    return state.collectList.slice(start, end);
  },
  getTotalCollections: (state, getters) => getters.getTotalCollectItems,
  getTodayCollections: (state, getters) => getters.getTodayCollectItems,
  getUnreadCollections: (state, getters) => getters.getUnreadCollectItems,
  getRecycledCollections: (state, getters) => getters.getRecycledCollectItems,
  getCurrentPageCollections: (state, getters) => getters.getCurrentPageCollectItems
};

const mutations = {
  SET_COLLECT_LIST(state, list) {
    state.collectList = list || [];
  },
  SET_PAGINATION(state, { type, page, pageSize, total }) {
    if (type === 'collectList') {
      state.pagination.currentPage = page;
      state.pagination.pageSize = pageSize;
      state.pagination.total = total;
    }
  },
  SET_LOADING(state, loading) {
    state.loading = loading;
  },
  SET_SEARCH_KEYWORD(state, keyword) {
    state.searchKeyword = keyword;
  },
  SET_COLLECT_TAGS(state, tags) {
    state.collectTags = normalizePopularTags(tags);
  },
  SET_SELECTED_COLLECT_TAGS(state, tagIds) {
    state.selectedCollectTags = tagIds || [];
  },
  SET_COLLECT_FILTER_CONDITION(state, condition) {
    state.collectFilterCondition = {
      ...state.collectFilterCondition,
      ...normalizeTagFilterCondition(condition)
    };
  },
  RESET_COLLECT_FILTER_CONDITION(state) {
    state.collectFilterCondition = {
      type: '0',
      tagId: null,
      tagIds: [],
      status: null
    };
    state.searchKeyword = '';
    state.selectedCollectTags = [];
  },
  SET_STATISTICS(state, stats) {
    state.statistics = { ...state.statistics, ...stats };
  },
  ADD_COLLECTION_ITEM(state, item) {
    state.collectList.unshift(item);
    state.statistics.totalCollects += 1;
  },
  UPDATE_COLLECTION_ITEM(state, updatedItem) {
    const index = state.collectList.findIndex(item => item.id === updatedItem.id);
    if (index !== -1) {
      state.collectList.splice(index, 1, updatedItem);
    }
  },
  DELETE_COLLECTION_ITEM(state, itemId) {
    const index = state.collectList.findIndex(item => item.id === itemId);
    if (index !== -1) {
      state.collectList.splice(index, 1);
    }
  },
  PERMANENT_DELETE_COLLECTION_ITEM(state, itemId) {
    const index = state.collectList.findIndex(item => item.id === itemId);
    if (index !== -1) {
      state.collectList.splice(index, 1);
    }
  },
  RECOVER_COLLECTION_ITEM(state, itemId) {
    const index = state.collectList.findIndex(item => item.id === itemId);
    if (index !== -1) {
      state.collectList.splice(index, 1);
    }
  },
  BATCH_DELETE_COLLECTION_ITEMS(state, ids) {
    state.collectList = state.collectList.filter(item => !ids.includes(item.id));
  },
  BATCH_RECOVER_COLLECTION_ITEMS(state, ids) {
    state.collectList = state.collectList.filter(item => !ids.includes(item.id));
  },
  BATCH_PERMANENT_DELETE_COLLECTION_ITEMS(state, ids) {
    state.collectList = state.collectList.filter(item => !ids.includes(item.id));
  },
  TOGGLE_STAR(state, id) {
    const item = state.collectList.find(item => item.id === id);
    if (item) {
      const current = typeof item.isStar === 'boolean'
        ? item.isStar
        : (typeof item.isStarred === 'boolean' ? item.isStarred : item.starred);
      const next = !current;
      item.isStar = next;
      item.isStarred = next;
      item.starred = next;
    }
  }
};

const actions = {
  async getCollectList({ commit, state }, params = {}) {
    commit('SET_LOADING', true);
    try {
      const requestParams = buildCollectListRequestParams(params, state);
      const response = await collectApi.getCollectList(requestParams);
      // 适配响应拦截器解包后的数据结构
      const collectData = response?.data ?? response ?? {};
      const collectList = Array.isArray(collectData.records) ? collectData.records : (Array.isArray(collectData) ? collectData : []);
      commit('SET_COLLECT_LIST', collectList);
      commit('SET_PAGINATION', {
        type: 'collectList',
        page: collectData.current || response.currentPage || requestParams.pageNum || 1,
        pageSize: collectData.size || response.pageSize || state.pagination.pageSize,
        total: collectData.total || response.total || 0
      });
      return response;
    } catch (error) {
      console.error('获取收藏列表失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  async getRecycleBinList({ commit, state }, params = {}) {
    commit('SET_LOADING', true);
    try {
      const requestParams = buildCollectListRequestParams(params, state);
      const response = await collectApi.getRecycleBin(requestParams);
      // 适配响应拦截器解包后的数据结构
      const collectData = response?.data ?? response ?? {};
      const collectList = Array.isArray(collectData.records) ? collectData.records : (Array.isArray(collectData) ? collectData : []);
      commit('SET_COLLECT_LIST', collectList);
      commit('SET_PAGINATION', {
        type: 'collectList',
        page: collectData.current || requestParams.pageNum || 1,
        pageSize: collectData.size || requestParams.pageSize || state.pagination.pageSize,
        total: collectData.total || 0
      });
      return response;
    } catch (error) {
      console.error('获取回收站列表失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  async getCollectListByTag({ commit, state }, { tagId, tagIds, params = {} }) {
    commit('SET_LOADING', true);
    try {
      const normalizedTagId = tagId || (Array.isArray(tagIds) && tagIds.length > 0 ? tagIds[0] : null);
      const requestParams = buildCollectListRequestParams({
        ...params,
        tagId: normalizedTagId,
        type: '0'
      }, state);
      const response = await collectApi.getCollectList(requestParams);
      // 适配响应拦截器解包后的数据结构
      const collectData = response?.data ?? response ?? {};
      const collectList = Array.isArray(collectData.records) ? collectData.records : (Array.isArray(collectData) ? collectData : []);
      commit('SET_COLLECT_LIST', collectList);
      commit('SET_PAGINATION', {
        type: 'collectList',
        page: collectData.current || response.currentPage || requestParams.pageNum || 1,
        pageSize: collectData.size || response.pageSize || state.pagination.pageSize,
        total: collectData.total || response.total || 0
      });
      if (normalizedTagId) {
        const selectedTagIds = Array.isArray(tagIds) && tagIds.length > 0 ? tagIds : [normalizedTagId];
        commit('SET_SELECTED_COLLECT_TAGS', selectedTagIds);
        commit('SET_COLLECT_FILTER_CONDITION', {
          tagId: normalizedTagId,
          tagIds: selectedTagIds,
          type: '0',
          status: null
        });
      }
      return response;
    } catch (error) {
      console.error('根据标签获取收藏列表失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  async getCollectByCategory({ commit, state, dispatch }, category) {
    commit('SET_LOADING', true);
    try {
      let response;
      let collectList = [];
      let pagination = null;

      switch (category) {
        case 'discover': {
          const requestParams = {
            pageNum: state.pagination.currentPage || 1,
            pageSize: state.pagination.pageSize || 10
          };
          if (state.searchKeyword && state.searchKeyword.trim()) {
            requestParams.keyword = state.searchKeyword.trim();
          }
          response = await collectionsApi.getPublicCollections(requestParams);
          const normalized = normalizeCollectionResponse(response);
          collectList = normalized.records;
          pagination = {
            type: 'collectList',
            page: normalized.current,
            pageSize: normalized.size,
            total: normalized.total
          };
          break;
        }
        case 'recycle':
          response = await dispatch('getRecycleBinList');
          break;
        case 'my-collection':
          response = await collectApi.getCollectList({});
          break;
        default:
          response = await collectApi.getCollectList({});
      }

      if (!collectList.length) {
        // 适配响应拦截器解包后的数据结构
        const collectData = response?.data ?? response ?? {};
        collectList = Array.isArray(collectData.records) ? collectData.records : (Array.isArray(collectData) ? collectData : []);
        if (collectData.total !== undefined) {
          pagination = {
            type: 'collectList',
            page: collectData.current || 1,
            pageSize: collectData.size || 10,
            total: collectData.total
          };
        }
      }

      commit('SET_COLLECT_LIST', collectList);
      if (pagination) {
        commit('SET_PAGINATION', pagination);
      }
      return response;
    } catch (error) {
      console.error('根据分类获取收藏失败:', error);
      throw error;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  async getCollectTags({ commit }) {
    try {
      const response = await getPopularTags(50);
      // 适配响应拦截器解包后的数据结构
      const normalizedTags = normalizePopularTags(response?.data ?? response ?? []);
      commit('SET_COLLECT_TAGS', normalizedTags);
      commit('SET_STATISTICS', { totalTags: normalizedTags.length });
      return response;
    } catch (error) {
      console.error('获取标签列表失败:', error);
      throw error;
    }
  },
  setSelectedCollectTags({ commit }, tagIds) {
    commit('SET_SELECTED_COLLECT_TAGS', tagIds);
  },
  setCollectFilterCondition({ commit }, condition) {
    commit('SET_COLLECT_FILTER_CONDITION', condition);
  },
  resetCollectFilterCondition({ commit }) {
    commit('RESET_COLLECT_FILTER_CONDITION');
  },
  setSearchKeyword({ commit }, keyword) {
    commit('SET_SEARCH_KEYWORD', keyword);
  },
  async getCollectStatistics({ commit, dispatch, state }) {
    try {
      const [statisticsResponse, recycleBinResponse] = await Promise.all([
        collectApi.getStatistics(),
        collectApi.getRecycleBin({ page: 1, size: 1 })
      ]);
      // 适配响应拦截器解包后的数据结构
      const normalizedStatistics = normalizeStatisticsResponse({
        ...(statisticsResponse?.data ?? statisticsResponse ?? {}),
        recycledCollections: recycleBinResponse?.data?.total ?? recycleBinResponse?.total ?? 0,
        totalTags: state.statistics.totalTags
      });
      commit('SET_STATISTICS', normalizedStatistics);
      return statisticsResponse;
    } catch (error) {
      console.error('获取统计信息失败:', error);
      throw error;
    }
  },
  async addCollection({ commit }, collectionData) {
    try {
      const response = await collectApi.createCollect(collectionData);
      commit('ADD_COLLECTION_ITEM', response.data);
      return response;
    } catch (error) {
      console.error('添加收藏失败:', error);
      throw error;
    }
  },
  async updateCollection({ commit }, collectionData) {
    try {
      const response = await collectApi.updateCollect(collectionData.id, collectionData);
      commit('UPDATE_COLLECTION_ITEM', response.data);
      return response;
    } catch (error) {
      console.error('更新收藏失败:', error);
      throw error;
    }
  },
  async deleteCollection({ commit }, collectionId) {
    try {
      await collectApi.deleteCollect(collectionId);
      commit('DELETE_COLLECTION_ITEM', collectionId);
      return { success: true };
    } catch (error) {
      console.error('删除收藏失败:', error);
      throw error;
    }
  },
  async recoverCollection({ commit }, collectionId) {
    try {
      await collectApi.recoverCollect(collectionId);
      commit('RECOVER_COLLECTION_ITEM', collectionId);
      return { success: true };
    } catch (error) {
      console.error('恢复收藏失败:', error);
      throw error;
    }
  },
  async permanentDeleteCollection({ commit }, collectionId) {
    try {
      await collectApi.permanentDeleteCollect(collectionId);
      commit('PERMANENT_DELETE_COLLECTION_ITEM', collectionId);
      return { success: true };
    } catch (error) {
      console.error('永久删除收藏失败:', error);
      throw error;
    }
  },
  async batchDeleteItems({ commit }, ids) {
    try {
      await collectApi.batchDeleteCollect(ids);
      commit('BATCH_DELETE_COLLECTION_ITEMS', ids);
      return { success: true };
    } catch (error) {
      console.error('批量删除收藏失败:', error);
      throw error;
    }
  },
  async batchRecoverItems({ commit }, ids) {
    try {
      await collectApi.batchRecoverCollect(ids);
      commit('BATCH_RECOVER_COLLECTION_ITEMS', ids);
      return { success: true };
    } catch (error) {
      console.error('批量恢复收藏失败:', error);
      throw error;
    }
  },
  async batchPermanentDeleteItems({ commit }, ids) {
    try {
      await collectApi.batchPermanentDeleteCollect(ids);
      commit('BATCH_PERMANENT_DELETE_COLLECTION_ITEMS', ids);
      return { success: true };
    } catch (error) {
      console.error('批量永久删除收藏失败:', error);
      throw error;
    }
  },
  async toggleStar({ commit }, id) {
    try {
      await collectApi.toggleStar(id);
      commit('TOGGLE_STAR', id);
      return { success: true };
    } catch (error) {
      console.error('切换星标失败:', error);
      throw error;
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
