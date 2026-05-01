<template>
  <div class="collection-item-workspace">
    <div v-if="loading" class="loading-container card-panel">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-container card-panel">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadCollectionItem">重新加载</el-button>
          <el-button @click="goBack">返回收藏中心</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="item" class="page-content">
      <div class="page-header card-panel">
        <div class="header-left">
          <el-button type="text" icon="el-icon-arrow-left" @click="goBack">返回</el-button>
          <div class="title-group">
            <h2 class="page-title">{{ item.title || `收藏项 #${routeItemId}` }}</h2>
            <p class="page-desc">围绕收藏项查看来源内容、处理状态与互动反馈</p>
          </div>
          <el-tag size="small">ID: {{ item.id }}</el-tag>
          <el-tag size="small" :type="digestStatusTag.type">{{ digestStatusTag.text }}</el-tag>
        </div>
        <div class="header-right">
          <el-button @click="goToCollection">查看所属收藏集</el-button>
          <el-button v-if="item.relatedNoteId" type="primary" plain @click="goToRelatedNote">查看关联笔记</el-button>
          <el-button type="success" @click="goToCreateNote">继续沉淀</el-button>
        </div>
      </div>

      <div class="context-banner card-panel">
        <div class="context-item">
          <span class="context-label">所属收藏集</span>
          <span class="context-value">{{ item.collectionName || `收藏集 #${item.collectionId || '--'}` }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">来源类型</span>
          <span class="context-value">{{ sourceTypeText }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">学习进度</span>
          <span class="context-value">{{ item.studyProgress || 0 }}%</span>
        </div>
        <div class="context-item">
          <span class="context-label">笔记关联</span>
          <span class="context-value">{{ item.noteSourceLabel || '尚未关联沉淀笔记' }}</span>
        </div>
      </div>

      <div class="main-content-area">
        <div class="content-panel card-panel">
          <div class="panel-header">
            <h3>来源内容</h3>
            <div class="panel-actions">
              <el-button v-if="item.sourceUrl" size="small" @click="openSourceUrl">打开原链接</el-button>
            </div>
          </div>

          <div class="basic-info">
            <div class="info-item">
              <label>标题</label>
              <span>{{ item.title || '--' }}</span>
            </div>
            <div class="info-item">
              <label>来源</label>
              <span>{{ item.source || item.sourceUrl || '--' }}</span>
            </div>
            <div class="info-item">
              <label>核心摘要</label>
              <span>{{ item.coreAbstract || '暂无摘要' }}</span>
            </div>
            <div class="info-item">
              <label>学习目标</label>
              <span>{{ item.studyGoal || '尚未设置学习目标' }}</span>
            </div>
            <div class="info-item keywords-block">
              <label>关键词</label>
              <div class="keyword-list">
                <el-tag v-for="keyword in keywords" :key="keyword" size="small" effect="plain">{{ keyword }}</el-tag>
                <span v-if="keywords.length === 0">暂无关键词</span>
              </div>
            </div>
          </div>

          <div class="source-content">
            <template v-if="Number(item.sourceType) === 1">
              <iframe
                :src="item.sourceUrl"
                class="source-iframe"
                frameborder="0"
                sandbox="allow-scripts allow-same-origin allow-forms"
              ></iframe>
            </template>
            <template v-else-if="Number(item.sourceType) === 2">
              <el-image
                :src="item.sourceUrl"
                class="source-image"
                fit="contain"
                :preview-src-list="item.sourceUrl ? [item.sourceUrl] : []"
              />
            </template>
            <template v-else-if="Number(item.sourceType) === 4">
              <video :src="item.sourceUrl" class="source-video" controls preload="metadata">
                您的浏览器不支持视频播放。
              </video>
            </template>
            <template v-else>
              <div class="source-text" v-html="formattedContent"></div>
            </template>
          </div>
        </div>

        <div class="side-panel">
          <div class="interaction-panel card-panel">
            <div class="panel-header compact">
              <div>
                <h3>互动区</h3>
                <p>围绕当前收藏项承接点赞与评论互动</p>
              </div>
            </div>
            <div class="interaction-actions">
              <like-button
                :target-id="item.id"
                target-type="collection_item"
                :initial-count="interactionStats.likeCount"
                :show-text="true"
                @like="refreshInteractionData"
                @unlike="refreshInteractionData"
              />
            </div>
            <comment-section
              :target-id="item.id"
              target-type="collection_item"
              :current-user-id="currentUserId"
              :current-user-name="currentUserName"
              :current-user-avatar="currentUserAvatar"
              @comment-submitted="refreshInteractionData"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { collectApi } from '@/api/collect'
import { getLikeCount } from '@/api/interaction'
import LikeButton from '@/components/interaction/LikeButton.vue'
import CommentSection from '@/components/interaction/CommentSection.vue'
import { sanitizeHtml } from '@/utils/sanitize'

export default {
  name: 'CollectionItemWorkspace',
  components: {
    LikeButton,
    CommentSection
  },
  data() {
    return {
      loading: false,
      error: '',
      item: null,
      interactionStats: {
        likeCount: 0
      }
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo']),
    routeItemId() {
      return this.$route.params.id
    },
    currentUserId() {
      return this.getUserInfo?.id || null
    },
    currentUserName() {
      return this.getUserInfo?.nickname || this.getUserInfo?.username || ''
    },
    currentUserAvatar() {
      return this.getUserInfo?.avatar || ''
    },
    digestStatusTag() {
      const status = this.item?.digestStatus
      const map = {
        undigest: { text: '未加工', type: 'info' },
        digesting: { text: '加工中', type: 'warning' },
        digested: { text: '已加工', type: 'success' },
        abandoned: { text: '已废弃', type: 'danger' }
      }
      return map[status] || { text: '待处理', type: 'info' }
    },
    sourceTypeText() {
      const type = Number(this.item?.sourceType)
      const map = {
        1: '网页',
        2: '图片',
        3: '文本',
        4: '视频',
        5: '笔记'
      }
      return map[type] || '未知类型'
    },
    keywords() {
      return String(this.item?.keywords || '')
        .split(/[，,、\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
    },
    formattedContent() {
      return sanitizeHtml(String(this.item?.content || this.item?.contentSnapshot || '暂无内容').replace(/\n/g, '<br>'))
    }
  },
  watch: {
    '$route.params.id': {
      async handler() {
        await this.loadCollectionItem()
      }
    }
  },
  async mounted() {
    await this.loadCollectionItem()
  },
  methods: {
    async loadCollectionItem() {
      if (!this.routeItemId) {
        this.error = '缺少收藏项ID'
        return
      }
      this.loading = true
      this.error = ''
      try {
        const response = await collectApi.getCollectById(this.routeItemId)
        const payload = response?.data?.data || response?.data || {}
        if (!payload?.id) {
          throw new Error('未找到对应收藏项')
        }
        this.item = payload
        await this.refreshInteractionData()
      } catch (error) {
        console.error('加载收藏项工作台失败:', error)
        this.error = error?.message || '加载收藏项失败'
      } finally {
        this.loading = false
      }
    },
    async refreshInteractionData() {
      if (!this.item?.id) {
        return
      }
      try {
        const likeResponse = await getLikeCount(this.item.id, 'collection_item')
        this.interactionStats.likeCount = likeResponse?.data?.likeCount || likeResponse?.data || 0
      } catch (error) {
        console.error('加载收藏项互动数据失败:', error)
      }
    },
    openSourceUrl() {
      if (!this.item?.sourceUrl) {
        return
      }
      window.open(this.item.sourceUrl, '_blank')
    },
    goToCollection() {
      if (!this.item?.collectionId) {
        return
      }
      this.$router.push(`/collections/${this.item.collectionId}`)
    },
    goToRelatedNote() {
      const noteId = this.item?.relatedNoteId || this.item?.noteId
      if (!noteId) {
        return
      }
      this.$router.push(`/creation/notes/${noteId}`)
    },
    goToCreateNote() {
      this.$router.push(`/collection-item/${this.routeItemId}/note/create`)
    },
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        this.$router.push('/collections')
      }
    }
  }
}
</script>

<style scoped>
.collection-item-workspace {
  padding: 24px;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-panel {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}

.page-header,
.context-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-title {
  margin: 0;
}

.page-desc,
.context-label,
.panel-header p {
  margin: 0;
  color: #909399;
}

.context-banner {
  flex-wrap: wrap;
}

.context-item {
  min-width: 180px;
}

.context-label {
  display: block;
  font-size: 12px;
  margin-bottom: 6px;
}

.context-value {
  color: #303133;
  font-weight: 500;
}

.main-content-area {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(340px, 0.9fr);
  gap: 20px;
}

.panel-header,
.panel-header.compact {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.basic-info {
  display: grid;
  gap: 14px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item label {
  color: #909399;
  font-size: 12px;
}

.keyword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.source-iframe,
.source-image,
.source-video {
  width: 100%;
  min-height: 360px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
}

.source-text {
  min-height: 360px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  line-height: 1.8;
  color: #303133;
  background: #fafafa;
}

.interaction-panel {
  position: sticky;
  top: 24px;
}

.interaction-actions {
  margin-bottom: 20px;
}

.loading-container,
.error-container {
  min-height: 240px;
}

@media (max-width: 1024px) {
  .main-content-area {
    grid-template-columns: 1fr;
  }

  .interaction-panel {
    position: static;
  }
}

@media (max-width: 768px) {
  .collection-item-workspace {
    padding: 16px;
  }

  .card-panel {
    padding: 18px;
  }

  .page-header,
  .context-banner,
  .panel-header,
  .panel-header.compact {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
