<template>
  <div class="collection-detail-page">
    <div v-if="loading" class="loading-container">
      <div class="skeleton-header card">
        <div class="skeleton-cover"></div>
        <div class="skeleton-info">
          <div class="skeleton-title"></div>
          <div class="skeleton-description"></div>
          <div class="skeleton-meta">
            <div class="skeleton-meta-item"></div>
            <div class="skeleton-meta-item"></div>
            <div class="skeleton-meta-item"></div>
          </div>
        </div>
      </div>
      <div class="skeleton-items card">
        <div v-for="i in 3" :key="i" class="skeleton-item-card">
          <div class="skeleton-item-content"></div>
        </div>
      </div>
    </div>

    <div v-else-if="error" class="error-container card">
      <div class="error-content">
        <i class="fas fa-exclamation-circle error-icon"></i>
        <h3 class="error-title">加载失败</h3>
        <p class="error-message">{{ error }}</p>
        <el-button type="primary" @click="loadPageData">
          <i class="el-icon-refresh"></i>
          重新加载
        </el-button>
      </div>
    </div>

    <div v-else-if="collection" class="detail-wrapper">
      <div class="page-header card">
        <div class="header-top-row">
          <el-button class="back-button" type="text" icon="el-icon-arrow-left" @click="goBack">
            返回
          </el-button>
          <div class="header-actions" v-if="isOwner">
            <el-button size="small" @click="goToCollectionsList">
              <i class="el-icon-s-grid"></i>
              返回列表
            </el-button>
            <el-button type="primary" size="small" @click="showAddItemDialog = true">
              <i class="el-icon-plus"></i>
              添加收藏项
            </el-button>
            <el-button type="primary" size="small" @click="openEditDialog">
              <i class="el-icon-edit"></i>
              编辑
            </el-button>
            <el-button type="danger" size="small" :loading="deletingCollection" :disabled="deletingCollection" @click="deleteCollection">
              <i class="el-icon-delete"></i>
              删除
            </el-button>
          </div>
        </div>

        <div class="header-main">
          <div class="cover-section">
            <img
              v-if="coverImage"
              :src="coverImage"
              :alt="collection.name || '收藏集封面'"
              class="cover-image"
              @error="handleCoverError"
            >
            <div v-else class="cover-placeholder">
              <i class="fas fa-layer-group"></i>
            </div>
          </div>

          <div class="info-section">
            <div class="title-row">
              <h1 class="page-title">{{ collection.name || '未命名收藏集' }}</h1>
              <el-tag :type="isPublic ? 'success' : 'info'" size="small" class="status-tag">
                <i :class="isPublic ? 'el-icon-view' : 'el-icon-lock'"></i>
                {{ isPublic ? '公开' : '私有' }}
              </el-tag>
            </div>

            <p class="page-description">{{ collection.description || '暂无描述' }}</p>

            <div class="meta-grid">
              <div class="meta-item">
                <i class="el-icon-time meta-icon"></i>
                <div class="meta-content">
                  <span class="meta-label">创建时间</span>
                  <span class="meta-value">{{ formatDate(collection.createdAt) }}</span>
                </div>
              </div>
              <div class="meta-item">
                <i class="el-icon-refresh meta-icon"></i>
                <div class="meta-content">
                  <span class="meta-label">更新时间</span>
                  <span class="meta-value">{{ formatDate(collection.updatedAt || collection.createdAt) }}</span>
                </div>
              </div>
              <div class="meta-item">
                <i class="el-icon-document meta-icon"></i>
                <div class="meta-content">
                  <span class="meta-label">收藏项</span>
                  <span class="meta-value">{{ totalItems }} 项</span>
                </div>
              </div>
              <div class="meta-item">
                <i :class="isPublic ? 'el-icon-view' : 'el-icon-lock'" class="meta-icon"></i>
                <div class="meta-content">
                  <span class="meta-label">可见性</span>
                  <span class="meta-value">{{ isPublic ? '对外可浏览' : '仅自己可见' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="items-section card">
        <div class="section-header">
          <div class="section-header-left">
            <h2 class="section-title">
              <i class="el-icon-document"></i>
              收藏项列表
            </h2>
            <p class="section-subtitle">浏览该收藏集下的内容项，点击后进入收藏项工作区继续加工整理。</p>
          </div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏项标题或内容"
            prefix-icon="el-icon-search"
            clearable
            class="items-search"
            @input="handleSearchDebounced"
          />
        </div>

        <div v-if="itemsLoading" class="items-loading">
          <div v-for="i in 3" :key="i" class="skeleton-item">
            <div class="skeleton-item-title"></div>
            <div class="skeleton-item-summary"></div>
            <div class="skeleton-item-footer">
              <div class="skeleton-tag"></div>
              <div class="skeleton-date"></div>
            </div>
          </div>
        </div>

        <div v-else-if="filteredItems.length === 0" class="empty-items">
          <i class="fas fa-inbox empty-icon"></i>
          <h3 class="empty-title">{{ searchKeyword ? '未找到匹配的收藏项' : '暂无收藏项' }}</h3>
          <p class="empty-description">
            {{ searchKeyword ? '尝试使用其他关键词搜索' : '点击上方"添加收藏项"按钮开始添加内容' }}
          </p>
          <el-button v-if="!searchKeyword && isOwner" type="primary" @click="showAddItemDialog = true">
            <i class="el-icon-plus"></i>
            添加第一个收藏项
          </el-button>
        </div>

        <transition-group v-else name="list" tag="div" class="items-list">
          <div
            v-for="item in paginatedItems"
            :key="item.id"
            class="item-card"
            @click="goToItemWorkspace(item)"
          >
            <div class="item-header">
              <el-tag size="small" :type="getSourceTagType(item.sourceType)" class="source-tag">
                <i :class="getSourceIcon(item.sourceType)"></i>
                {{ getSourceTypeText(item.sourceType) }}
              </el-tag>
              <span class="item-date">
                <i class="el-icon-time"></i>
                {{ formatDate(item.createdAt || item.createTime) }}
              </span>
            </div>
            <div class="item-main">
              <h3 class="item-title">{{ item.title || '未命名收藏项' }}</h3>
              <p class="item-summary">{{ getItemSummary(item) }}</p>
            </div>
            <div class="item-footer">
              <div class="item-meta">
                <span v-if="item.keywords" class="meta-keywords">
                  <i class="el-icon-price-tag"></i>
                  {{ item.keywords }}
                </span>
              </div>
              <i class="el-icon-arrow-right item-arrow"></i>
            </div>
          </div>
        </transition-group>

        <div class="items-pagination" v-if="filteredItems.length > itemsPageSize">
          <el-pagination
            background
            layout="total, prev, pager, next, sizes"
            :total="filteredItems.length"
            :page-size="itemsPageSize"
            :current-page="itemsCurrentPage"
            :page-sizes="[10, 20, 50]"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <collection-edit-dialog
        :visible.sync="showEditDialog"
        :collection="collection"
        @success="handleCollectionUpdated"
      />

      <el-dialog title="添加收藏项" :visible.sync="showAddItemDialog" width="560px" @open="resetAddItemForm">
        <el-form :model="addItemForm" label-width="100px" ref="addItemFormRef" :rules="addItemRules">
          <el-form-item label="标题" prop="title">
            <el-input v-model="addItemForm.title" placeholder="请输入收藏项标题" />
          </el-form-item>
          <el-form-item label="来源类型" prop="sourceType">
            <el-select v-model="addItemForm.sourceType" style="width:100%">
              <el-option label="网页" :value="1"></el-option>
              <el-option label="图片" :value="2"></el-option>
              <el-option label="文本" :value="3"></el-option>
              <el-option label="视频" :value="4"></el-option>
              <el-option label="笔记" :value="5"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="来源URL">
            <el-input v-model="addItemForm.sourceUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="来源名称">
            <el-input v-model="addItemForm.source" placeholder="来源网站或作者" />
          </el-form-item>
          <el-form-item label="核心摘要">
            <el-input v-model="addItemForm.coreAbstract" type="textarea" :rows="3" placeholder="简要概括内容" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="addItemForm.keywords" placeholder="多个关键词用逗号分隔" />
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="showAddItemDialog = false">取消</el-button>
          <el-button type="primary" :loading="addItemLoading" @click="submitAddItem">确认添加</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { collectionsApi } from '@/api/collections'
import { collectApi } from '@/api/collect'
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
      showEditDialog: false,
      showAddItemDialog: false,
      itemsCurrentPage: 1,
      itemsPageSize: 10,
      addItemLoading: false,
      deletingCollection: false,
      searchDebounceTimer: null,
      addItemForm: {
        title: '',
        sourceType: 1,
        sourceUrl: '',
        source: '',
        coreAbstract: '',
        keywords: ''
      },
      addItemRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }]
      }
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
    },
    paginatedItems() {
      const start = (this.itemsCurrentPage - 1) * this.itemsPageSize
      return this.filteredItems.slice(start, start + this.itemsPageSize)
    }
  },
  async created() {
    await this.loadPageData()
  },
  beforeDestroy() {
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer)
    }
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
    handleSearchDebounced() {
      if (this.searchDebounceTimer) {
        clearTimeout(this.searchDebounceTimer)
      }
      this.searchDebounceTimer = setTimeout(() => {
        this.itemsCurrentPage = 1
      }, 300)
    },
    handleSizeChange(size) {
      this.itemsPageSize = size
      this.itemsCurrentPage = 1
    },
    handleCurrentChange(page) {
      this.itemsCurrentPage = page
    },
    goBack() {
      // 根据来源参数智能返回
      const from = this.$route.query.from;
      if (from) {
        // 所有来源都返回收藏中心对应的标签页，包括我的收藏集
        this.$router.push({
          path: '/collect/center',
          query: { tab: from }
        });
      } else {
        this.$router.back()
      }
    },
    goToCollectionsList() {
      this.$router.push('/collections/list')
    },
    goToItemWorkspace(item) {
      if (!item?.id) {
        return
      }
      // 传递来源信息
      const query = { ...this.$route.query };
      this.$router.push({
        path: `/creation/collection-items/${item.id}/note/create`,
        query
      })
    },
    openEditDialog() {
      this.showEditDialog = true
    },
    handleCollectionUpdated() {
      this.showEditDialog = false
      this.loadPageData()
    },
    resetAddItemForm() {
      this.addItemForm = {
        title: '',
        sourceType: 1,
        sourceUrl: '',
        source: '',
        coreAbstract: '',
        keywords: ''
      }
    },
    async submitAddItem() {
      try {
        await this.$refs.addItemFormRef.validate()
      } catch {
        return
      }
      this.addItemLoading = true
      try {
        await collectApi.createCollect({
          ...this.addItemForm,
          collectionId: this.collection.id
        })
        this.$message.success('收藏项添加成功')
        this.showAddItemDialog = false
        await this.loadCollectionItems()
      } catch (error) {
        console.error('添加收藏项失败:', error)
        this.$message.error(error?.response?.data?.message || '添加收藏项失败')
      } finally {
        this.addItemLoading = false
      }
    },
    deleteCollection() {
      if (this.deletingCollection) {
        return
      }
      this.$confirm('确定要删除该收藏集？收藏集内的收藏项将被移至回收站。', '删除收藏集', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.deletingCollection = true
        try {
          await collectionsApi.deleteCollection(this.collection.id)
          this.$message.success('收藏集已删除')
          this.$router.push('/collections')
        } catch (error) {
          console.error('删除收藏集失败:', error)
          this.$message.error('删除收藏集失败')
        } finally {
          this.deletingCollection = false
        }
      }).catch(() => {})
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
        3: '文本',
        4: '视频',
        5: '笔记'
      }
      return sourceMap[sourceType] || '内容'
    },
    getSourceTagType(sourceType) {
      const tagMap = {
        1: 'primary',
        2: 'success',
        3: 'info',
        4: 'warning',
        5: ''
      }
      return tagMap[sourceType] || ''
    },
    getSourceIcon(sourceType) {
      const iconMap = {
        1: 'el-icon-link',
        2: 'el-icon-picture',
        3: 'el-icon-document',
        4: 'el-icon-video-camera',
        5: 'el-icon-edit'
      }
      return iconMap[sourceType] || 'el-icon-document'
    },
    handleCoverError(event) {
      console.warn('封面图片加载失败:', event.target.src)
      event.target.style.display = 'none'
    }
  }
}
</script>

<style scoped>
.collection-detail-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: var(--space-6);
}

.loading-container,
.error-container {
  animation: fadeIn 0.3s ease;
}

.skeleton-header {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

.skeleton-cover {
  width: 200px;
  height: 200px;
  border-radius: var(--radius-lg);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.skeleton-title {
  height: 32px;
  width: 60%;
  border-radius: var(--radius-md);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-description {
  height: 20px;
  width: 80%;
  border-radius: var(--radius-md);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-meta {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.skeleton-meta-item {
  height: 60px;
  border-radius: var(--radius-md);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-items {
  padding: var(--space-6);
}

.skeleton-item-card {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.skeleton-item-card:last-child {
  border-bottom: none;
}

.skeleton-item-content {
  height: 80px;
  border-radius: var(--radius-md);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.error-container {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-content {
  text-align: center;
  max-width: 400px;
}

.error-icon {
  font-size: 64px;
  color: var(--error-color);
  margin-bottom: var(--space-4);
  animation: pulse 2s ease-in-out infinite;
}

.error-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.error-message {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-6) 0;
  line-height: 1.6;
}

.detail-wrapper {
  animation: fadeIn 0.4s ease;
}

.page-header {
  margin-bottom: var(--space-6);
  overflow: visible;
}

.header-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.back-button {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  transition: all var(--transition-base);
}

.back-button:hover {
  color: var(--primary-color);
  transform: translateX(-2px);
}

.header-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.header-main {
  display: flex;
  gap: var(--space-6);
}

.cover-section {
  flex-shrink: 0;
  width: 200px;
  height: 200px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
}

.cover-section:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.cover-section:hover .cover-image {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-light) 100%);
  color: white;
  font-size: 64px;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.page-title {
  font-size: var(--font-size-4xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.2;
  flex: 1;
  min-width: 0;
}

.status-tag {
  flex-shrink: 0;
}

.page-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
  margin-top: var(--space-2);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.meta-item:hover {
  background-color: var(--primary-bg);
  transform: translateY(-2px);
}

.meta-icon {
  font-size: 20px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.meta-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.meta-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  line-height: 1.4;
}

.meta-value {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.items-section {
  animation: slideInUp 0.4s ease;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.section-header-left {
  flex: 1;
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.section-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.5;
}

.items-search {
  width: 320px;
  flex-shrink: 0;
}

.items-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.skeleton-item {
  padding: var(--space-4);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}

.skeleton-item-title {
  height: 20px;
  width: 70%;
  border-radius: var(--radius-sm);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: var(--space-3);
}

.skeleton-item-summary {
  height: 16px;
  width: 90%;
  border-radius: var(--radius-sm);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  margin-bottom: var(--space-3);
}

.skeleton-item-footer {
  display: flex;
  justify-content: space-between;
  gap: var(--space-3);
}

.skeleton-tag {
  height: 24px;
  width: 60px;
  border-radius: var(--radius-full);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-date {
  height: 16px;
  width: 100px;
  border-radius: var(--radius-sm);
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.empty-items {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-6);
  text-align: center;
  min-height: 300px;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-4);
  opacity: 0.6;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-6) 0;
  line-height: 1.6;
  max-width: 400px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.item-card {
  padding: var(--space-5);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  background-color: var(--bg-container);
  cursor: pointer;
  transition: all var(--transition-base);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.item-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.item-card:active {
  transform: translateY(0);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
}

.source-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.item-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.item-main {
  flex: 1;
}

.item-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-base);
}

.item-card:hover .item-title {
  color: var(--primary-color);
}

.item-summary {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-lighter);
}

.item-meta {
  flex: 1;
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.meta-keywords {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  padding: 2px 8px;
  background-color: var(--bg-canvas);
  border-radius: var(--radius-full);
}

.item-arrow {
  font-size: 16px;
  color: var(--text-placeholder);
  transition: all var(--transition-base);
}

.item-card:hover .item-arrow {
  color: var(--primary-color);
  transform: translateX(4px);
}

.list-enter-active,
.list-leave-active {
  transition: all var(--transition-base);
}

.list-enter,
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.list-move {
  transition: transform var(--transition-base);
}

.items-pagination {
  display: flex;
  justify-content: center;
  padding-top: var(--space-6);
  margin-top: var(--space-6);
  border-top: 1px solid var(--border-light);
}

@media (max-width: 1200px) {
  .meta-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .collection-detail-page {
    padding: var(--space-4);
  }

  .header-main {
    flex-direction: column;
  }

  .cover-section {
    width: 100%;
    height: 240px;
  }

  .page-title {
    font-size: var(--font-size-2xl);
  }

  .meta-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .items-search {
    width: 100%;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions .el-button {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .collection-detail-page {
    padding: var(--space-3);
  }

  .page-header,
  .items-section {
    padding: var(--space-4);
  }

  .cover-section {
    height: 200px;
  }

  .page-title {
    font-size: var(--font-size-xl);
  }

  .item-card {
    padding: var(--space-4);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
