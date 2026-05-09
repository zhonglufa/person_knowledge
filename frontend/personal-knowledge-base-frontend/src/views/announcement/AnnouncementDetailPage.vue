<template>
  <div class="announcement-detail-page">
    <div class="page-header card">
      <el-button class="back-button" type="text" icon="el-icon-arrow-left" @click="goBack">
        返回
      </el-button>
      <div class="header-main">
        <h1 class="page-title">{{ announcement?.title || '公告详情' }}</h1>
      </div>
    </div>

    <div v-if="loading" class="loading-container card">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else-if="error" class="error-container card">
      <div class="error-content">
        <i class="fas fa-exclamation-circle error-icon"></i>
        <h3 class="error-title">{{ getErrorTitle(error) }}</h3>
        <p class="error-message">{{ error }}</p>
        <el-button v-if="!isAnnouncementUnavailable(error)" type="primary" @click="loadAnnouncementDetail">
          <i class="el-icon-refresh"></i>
          重新加载
        </el-button>
        <el-button v-else type="default" @click="goBack">
          <i class="el-icon-back"></i>
          返回
        </el-button>
      </div>
    </div>

    <div v-else class="detail-card card">
      <div class="meta-row">
        <el-tag size="small" :type="typeTagType" class="meta-tag">
          {{ typeText }}
        </el-tag>
        <el-tag v-if="priorityText" size="small" :type="priorityTagType" class="meta-tag">
          {{ priorityText }}
        </el-tag>
        <span v-if="announcement?.effectiveAt" class="meta-item">
          生效时间：{{ formatDate(announcement.effectiveAt) }}
        </span>
        <span v-if="announcement?.expireAt" class="meta-item">
          失效时间：{{ formatDate(announcement.expireAt) }}
        </span>
      </div>

      <div class="content">
        <pre class="content-text">{{ announcement?.content || '' }}</pre>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/api/axios'

export default {
  name: 'AnnouncementDetailPage',

  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },

  data() {
    return {
      loading: false,
      error: null,
      announcement: null,
      statistics: null
    }
  },

  computed: {
    typeText() {
      const map = {
        system: '系统公告',
        activity: '活动通知',
        maintenance: '维护通知'
      }
      return map[this.announcement?.type] || '公告'
    },
    typeTagType() {
      const map = {
        system: '',
        activity: 'success',
        maintenance: 'warning'
      }
      return map[this.announcement?.type] || 'info'
    },
    priorityText() {
      const map = {
        low: '',
        medium: '',
        high: '重要',
        urgent: '紧急'
      }
      return map[this.announcement?.priority] || ''
    },
    priorityTagType() {
      const map = {
        low: 'info',
        medium: '',
        high: 'warning',
        urgent: 'danger'
      }
      return map[this.announcement?.priority] || 'info'
    }
  },

  watch: {
    id: {
      immediate: true,
      handler() {
        this.loadAnnouncementDetail()
      }
    }
  },

  methods: {
    goBack() {
      this.$router.back()
    },

    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },

    getErrorTitle(error) {
      if (error.includes('不存在')) return '公告不存在'
      if (error.includes('未发布') || error.includes('已下架')) return '公告已下架'
      if (error.includes('未生效')) return '公告未生效'
      if (error.includes('已过期')) return '公告已过期'
      return '加载失败'
    },

    isAnnouncementUnavailable(error) {
      return error.includes('不存在') || 
             error.includes('未发布') || 
             error.includes('已下架') || 
             error.includes('未生效') || 
             error.includes('已过期')
    },

    async loadAnnouncementDetail() {
      const announcementId = this.id
      if (!announcementId) {
        this.error = '无效的公告ID'
        return
      }

      this.loading = true
      this.error = null
      try {
        const response = await request({
          url: `/announcement/${announcementId}`,
          method: 'get'
        })

        if (response.code !== 200) {
          this.error = response.message || '获取公告失败'
          return
        }

        this.announcement = response.data?.announcement || null
        this.statistics = response.data?.statistics || null

        request({
          url: `/announcement/${announcementId}/read`,
          method: 'post'
        }).catch(() => {})
      } catch (e) {
        this.error = e?.message || '获取公告失败'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.announcement-detail-page {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.page-title {
  margin: 0;
  font-size: var(--font-size-xl);
}

.loading-container,
.error-container,
.detail-card {
  padding: var(--space-6);
}

.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.meta-item {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.content-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: var(--font-size-base);
  line-height: 1.7;
  margin: 0;
  font-family: inherit;
}

.error-content {
  text-align: center;
}

.error-icon {
  font-size: 32px;
  color: #f56c6c;
}

.error-title {
  margin: var(--space-3) 0 var(--space-2);
}

.error-message {
  margin: 0 0 var(--space-4);
  color: var(--text-secondary);
}
</style>
