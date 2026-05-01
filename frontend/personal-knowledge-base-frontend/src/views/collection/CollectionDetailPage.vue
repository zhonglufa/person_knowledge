<template>
  <div class="collection-detail-page">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadPageData">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="collection" class="detail-wrapper">
      <div class="page-header card">
        <div class="header-top-row">
          <el-button class="back-button" type="text" icon="el-icon-arrow-left" @click="goBack">
            返回
          </el-button>
          <div class="header-actions" v-if="isOwner">
            <el-button size="small" @click="goToWorkspace">进入工作台</el-button>
            <el-button type="primary" size="small" @click="openEditDialog">编辑收藏集</el-button>
          </div>
        </div>

        <div class="header-main">
          <div class="cover-section">
            <img
              v-if="coverImage"
              :src="coverImage"
              :alt="collection.name || '收藏集封面'"
              class="cover-image"
            >
            <div v-else class="cover-placeholder">
              <i class="fas fa-layer-group"></i>
            </div>
          </div>

          <div class="info-section">
            <div class="title-row">
              <h1 class="page-title">{{ collection.name || '未命名收藏集' }}</h1>
              <el-tag :type="isPublic ? 'success' : 'info'" size="small">
                {{ isPublic ? '公开' : '私有' }}
              </el-tag>
            </div>

            <p class="page-description">{{ collection.description || '暂无描述' }}</p>

            <div class="meta-grid">
              <div class="meta-item">
                <span class="meta-label">创建时间</span>
                <span class="meta-value">{{ formatDate(collection.createdAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">更新时间</span>
                <span class="meta-value">{{ formatDate(collection.updatedAt || collection.createdAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">收藏项数量</span>
                <span class="meta-value">{{ totalItems }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">状态</span>
                <span class="meta-value">{{ isPublic ? '对外可浏览' : '仅自己可见' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="items-section card">
        <div class="section-header">
          <div>
            <h2 class="section-title">收藏项列表</h2>
            <p class="section-subtitle">浏览该收藏集下的内容项，点击后进入收藏项详情页继续加工整理。</p>
          </div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏项标题或内容"
            prefix-icon="el-icon-search"
            clearable
            class="items-search"
            @input="handleSearch"
          />
        </div>

        <div v-if="itemsLoading" class="items-loading">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="filteredItems.length === 0" class="empty-items">
          <i class="fas fa-file-alt"></i>
          <p>当前收藏集暂无可展示的收藏项</p>
        </div>

        <div v-else class="items-list">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            class="item-card"
            @click="goToItemDetail(item)"
          >
            <div class="item-main">
              <h3 class="item-title">{{ item.title || '未命名收藏项' }}</h3>
              <p class="item-summary">{{ getItemSummary(item) }}</p>
            </div>
            <div class="item-side">
              <el-tag size="mini" :type="getSourceTagType(item.sourceType)">
                {{ getSourceTypeText(item.sourceType) }}
              </el-tag>
              <span class="item-date">{{ formatDate(item.createdAt || item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <collection-edit-dialog
        :visible.sync="showEditDialog"
        :collection="collection"
        @success="handleCollectionUpdated"
      />
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { collectionsApi } from '@/api/collections'
import CollectionEditDialog from '@/components/collect/CollectionEditDialog.vue'

export default {
  name: 'CollectionDetailPage',
  components: {
    CollectionEditDialog
  },
  data() {
    return {
      loading: true,
      itemsLoading: false,
      error: '',
      collection: null,
      collectionItems: [],
      searchKeyword: '',
      showEditDialog: false
    }
  },
  computed: {
    ...mapState('user', ['userInfo', 'token']),
    coverImage() {
      return this.collection?.coverImage || this.collection?.coverUrl || ''
    },
    isPublic() {
      return this.collection?.isPublic || this.collection?.isShared || false
    },
    isOwner() {
      return !!this.userInfo?.id && !!this.collection?.userId && this.userInfo.id === this.collection.userId
    },
    filteredItems() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      if (!keyword) {
        return this.collectionItems
      }
      return this.collectionItems.filter(item =>
        String(item?.title || '').toLowerCase().includes(keyword) ||
        String(item?.content || '').toLowerCase().includes(keyword) ||
        String(item?.coreAbstract || '').toLowerCase().includes(keyword)
      )
    },
    totalItems() {
      return this.collection?.itemCount || this.collectionItems.length || 0
    }
  },
  async created() {
    await this.loadPageData()
  },
  watch: {
    '$route.params.id': {
      async handler() {
        await this.loadPageData()
      }
    }
  },
  methods: {
    async loadPageData() {
      this.loading = true
      this.error = ''
      try {
        const response = await collectionsApi.getCollectionDetail(this.$route.params.id)
        const payload = response?.data || {}
        this.collection = payload.data || payload
        await this.loadCollectionItems()
      } catch (error) {
        console.error('加载收藏集详情失败', error)
        this.error = '收藏集详情加载失败'
      } finally {
        this.loading = false
      }
    },
    async loadCollectionItems() {
      if (!this.collection?.id) {
        this.collectionItems = []
        return
      }
      this.itemsLoading = true
      try {
        const response = await collectionsApi.getCollectionItems(this.collection.id, 1, 100)
        const payload = response?.data || {}
        const data = payload.data || payload
        this.collectionItems = Array.isArray(data.records) ? data.records : (Array.isArray(data) ? data : [])
      } catch (error) {
        console.error('加载收藏项失败', error)
        this.collectionItems = []
      } finally {
        this.itemsLoading = false
      }
    },
    handleSearch() {},
    goBack() {
      this.$router.push('/collections')
    },
    goToWorkspace() {
      this.$router.push(`/collections/${this.collection?.id}/workspace`)
    },
    goToItemDetail(item) {
      if (!item?.id) {
        return
      }
      this.$router.push(`/collection-item/${item.id}`)
    },
    openEditDialog() {
      this.showEditDialog = true
    },
    handleCollectionUpdated() {
      this.showEditDialog = false
      this.loadPageData()
    },
    formatDate(date) {
      if (!date) return '未知'
      const parsed = new Date(date)
      return Number.isNaN(parsed.getTime()) ? '未知' : parsed.toLocaleDateString('zh-CN')
    },
    getItemSummary(item) {
      return item?.coreAbstract || item?.content || '暂无摘要'
    },
    getSourceTypeText(sourceType) {
      const sourceMap = {
        1: '网页',
        2: '图片',
        3: '视频',
        4: '文档',
        5: '收藏集'
      }
      return sourceMap[sourceType] || '内容'
    },
    getSourceTagType(sourceType) {
      const tagMap = {
        1: 'primary',
        2: 'success',
        3: 'warning',
        4: 'info',
        5: 'danger'
      }
      return tagMap[sourceType] || 'info'
    }
  }
}
</script>

<style scoped>
.collection-detail-page {
  min-height: 100vh;
  padding: 24px;
  background: #f8fafc;
}

.card {
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(148, 163, 184, 0.14);
}

.detail-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  padding: 24px;
}

.header-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-main {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

.cover-section {
  width: 100%;
}

.cover-image,
.cover-placeholder {
  width: 100%;
  height: 220px;
  border-radius: 16px;
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e2e8f0 0%, #f8fafc 100%);
  color: #64748b;
  font-size: 36px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.page-title {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}

.page-description {
  margin: 0 0 20px;
  color: #475569;
  line-height: 1.7;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 14px;
}

.meta-label {
  font-size: 13px;
  color: #64748b;
}

.meta-value {
  font-size: 15px;
  color: #0f172a;
  font-weight: 500;
}

.items-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 8px;
  font-size: 22px;
  color: #0f172a;
}

.section-subtitle {
  margin: 0;
  color: #64748b;
}

.items-search {
  width: 320px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.item-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.item-card:hover {
  border-color: #6366f1;
  box-shadow: 0 8px 18px rgba(99, 102, 241, 0.08);
  transform: translateY(-2px);
}

.item-title {
  margin: 0 0 8px;
  font-size: 18px;
  color: #0f172a;
}

.item-summary {
  margin: 0;
  color: #475569;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-side {
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.item-date {
  font-size: 13px;
  color: #64748b;
}

.empty-items,
.loading-container,
.error-container,
.items-loading {
  padding: 32px;
}

.empty-items {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
}
</style>
