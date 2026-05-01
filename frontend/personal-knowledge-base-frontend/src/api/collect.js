import request from './axios';
import { getCategoryList } from './category';

export const collectApi = {
  getCollectList(params) {
    return request({
      url: '/collect/list',
      method: 'get',
      params
    });
  },

  getRecycleBin(params) {
    return request({
      url: '/collect/recycle-bin',
      method: 'get',
      params
    });
  },

  getCollectById(id) {
    return request({
      url: `/collect/${id}`,
      method: 'get'
    });
  },

  checkUrlExists(url) {
    return request({
      url: '/collect/url-exists',
      method: 'get',
      params: { url }
    });
  },

  parseUrlMetadata(url) {
    return request({
      url: '/collect/url-metadata',
      method: 'post',
      data: { url }
    });
  },

  getCategoryList() {
    return getCategoryList();
  },

  createCollect(data) {
    return request({
      url: '/collect/add-collect',
      method: 'post',
      data
    });
  },

  updateCollect(id, data) {
    return request({
      url: `/collect/${id}`,
      method: 'put',
      data
    });
  },

  deleteCollect(id) {
    return request({
      url: `/collect/${id}`,
      method: 'delete'
    });
  },

  recoverCollect(id) {
    return request({
      url: `/collect/${id}/recover`,
      method: 'put'
    });
  },

  permanentDeleteCollect(id) {
    return request({
      url: `/collect/${id}/permanent`,
      method: 'delete'
    });
  },

  batchDeleteCollect(ids) {
    return request({
      url: '/collect/batch/delete',
      method: 'post',
      data: { ids }
    });
  },

  batchRecoverCollect(ids) {
    return request({
      url: '/collect/batch/recover',
      method: 'post',
      data: { ids }
    });
  },

  batchPermanentDeleteCollect(ids) {
    return request({
      url: '/collect/batch/permanent-delete',
      method: 'post',
      data: { ids }
    });
  },

  getStatistics() {
    return request({
      url: '/collect/statistics',
      method: 'get'
    });
  },

  toggleStar(id) {
    return request({
      url: `/collect/${id}/star`,
      method: 'put'
    });
  },

  markAsRead(id) {
    return request({
      url: `/collect/${id}/read`,
      method: 'put'
    });
  },

  updateDigestStatus(id, status) {
    return request({
      url: `/collect/${id}/digest-status`,
      method: 'put',
      data: { status }
    });
  },

  updateStudyProgress(id, progress) {
    return request({
      url: `/collect/${id}/study-progress`,
      method: 'put',
      data: { progress }
    });
  },

  moveItem(id, targetCollectionId) {
    return request({
      url: `/collect/${id}/move`,
      method: 'put',
      data: { targetCollectionId }
    });
  },

  importPreview(htmlContent) {
    return request({
      url: '/collect/import/preview',
      method: 'post',
      data: { htmlContent }
    });
  },

  importExecute(data) {
    return request({
      url: '/collect/import/execute',
      method: 'post',
      data
    });
  },

  setRemind(id, remindAt) {
    return request({
      url: `/collect/${id}/remind`,
      method: 'put',
      data: { remindAt }
    });
  },

  cancelRemind(id) {
    return request({
      url: `/collect/${id}/remind`,
      method: 'delete'
    });
  }
};

export default collectApi;
