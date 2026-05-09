<template>
  <div class="collections-center">
    <div class="page-content">
      <div class="toolbar card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索收藏集名称或描述"
              prefix-icon="el-icon-search"
              clearable
              class="search-input"
              @input="handleSearchInput"
            />
          </div>
          <div class="toolbar-right">
            <el-select v-model="sortBy" size="small" class="toolbar-select">
              <el-option label="按创建时间" value="createdAt" />
              <el-option label="按更新时间" value="updatedAt" />
              <el-option label="按收藏项数" value="itemCount" />
              <el-option label="按名称" value="name" />
            </el-select>
            <el-select v-model="filterType" size="small" class="toolbar-select">
              <el-option label="全部类型" value="" />
              <el-option label="公开" value="shared" />
              <el-option label="私有" value="private" />
              <el-option label="默认" value="default" />
            </el-select>
            <el-button
              v-if="isLoggedIn"
              type="primary"
              icon="el-icon-plus"
              size="small"
              class="create-btn"
              @click="showCreateDialog = true"
            >
              创建收藏集
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="collections-container view-grid">
          <div v-for="i in skeletonCount" :key="`skeleton-${i}`" class="skeleton-card">
            <div class="skeleton-cover"></div>
            <div class="skeleton-content">
              <div class="skeleton-title"></div>
              <div class="skeleton-tags">
                <div class="skeleton-tag"></div>
                <div class="skeleton-tag"></div>
              </div>
              <div class="skeleton-description">
                <div class="skeleton-line"></div>
                <div class="skeleton-line short"></div>
              </div>
              <div class="skeleton-meta">
                <div class="skeleton-meta-item"></div>
                <div class="skeleton-meta-item"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="filteredCollections.length === 0" class="empty-state card">
        <i class="fas fa-layer-group empty-icon"></i>
        <h3 class="empty-title">{{ emptyStateTitle }}</h3>
        <p class="empty-description">{{ emptyStateDesc }}</p>
        <el-button type="primary" @click="handleEmptyAction">
          {{ emptyStateButtonText }}
        </el-button>
      </div>

      <div v-else class="collections-container" :class="`view-${viewMode}`" ref="collectionsContainer">
        <div
          v-for="collection in displayedCollections"
          :key="collection?.id || `collection-${collection?.name}-${collection?.createdAt}`"
          class="collection-card-wrapper"
        >
          <el-card
            class="collection-card"
            shadow="hover"
            @click.native="handleCollectionClick(collection)"
          >
            <div class="card-cover-wrapper">
              <img
                v-if="collection?.coverImage || collection?.coverUrl"
                :src="collection?.coverImage || collection?.coverUrl"
                :alt="collection?.name || '收藏集封面'"
                class="card-cover"
                @error="handleCoverError($event, collection)"
              >
              <div v-else class="card-cover placeholder-cover">
                <i class="fas fa-layer-group"></i>
              </div>
            </div>

            <div class="card-header">
              <div class="card-title-area">
                <h3 class="card-title">{{ collection?.name || '未命名收藏集' }}</h3>
                <div class="card-tags">
                  <el-tag v-if="collection?.isDefault" type="warning" size="mini">默认</el-tag>
                  <el-tag v-if="collection?.isPublic === 1" type="success" size="mini">公开</el-tag>
                  <el-tag v-else type="info" size="mini">私有</el-tag>
                </div>
              </div>
              <div v-if="isLoggedIn" class="card-actions" @click.stop>
                <el-dropdown trigger="click" @command="(cmd) => handleCardAction(cmd, collection)">
                  <el-button type="text" icon="el-icon-more" circle />
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item command="view">查看详情</el-dropdown-item>
                    <el-dropdown-item v-if="!collection?.isDefault" command="setDefault">设为默认</el-dropdown-item>
                    <el-dropdown-item v-if="collection?.isPublic !== 1" command="share">分享</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>

            <p class="card-description">{{ collection?.description || '暂无描述' }}</p>

            <div class="card-meta">
              <div class="meta-left">
                <span class="meta-item">
                  <i class="fas fa-file-alt"></i>
                  {{ collection?.itemCount || 0 }} 个收藏项
                </span>
                <span class="meta-item" v-if="collection?.tagCount">
                  <i class="fas fa-tags"></i>
                  {{ collection.tagCount }} 个标签
                </span>
              </div>
              <div class="meta-right">
                <span class="meta-item">
                  <i class="fas fa-clock"></i>
                  {{ formatDate(collection?.updatedAt || collection?.createdAt) }}
                </span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <div v-if="hasMore && !loading && filteredCollections.length > 0" class="load-more-container">
        <div v-if="loadingMore" class="loading-more">
          <i class="el-icon-loading"></i>
          <span>加载中...</span>
        </div>
        <div v-else class="scroll-hint">
          <i class="fas fa-arrow-down"></i>
          <span>向下滚动加载更多</span>
        </div>
      </div>

      <div v-if="!hasMore && displayedCollections.length > 0" class="no-more-data">
        <i class="fas fa-check-circle"></i>
        <span>已加载全部 {{ filteredCollections.length }} 个收藏集</span>
      </div>

      <create-collection-modal
        :visible.sync="showCreateDialog"
        @success="handleCollectionCreated"
      />

      <collection-edit-dialog
        :visible.sync="showEditDialog"
        :collection="editingCollection"
        @success="handleCollectionUpdated"
      />
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { collectionsApi } from '@/api/collections'
import CreateCollectionModal from '@/components/collect/CreateCollectionModal.vue'
import CollectionEditDialog from '@/components/collect/CollectionEditDialog.vue'

export default {
  name: 'CollectionListPage',
  components: {
    CreateCollectionModal,
    CollectionEditDialog
  },
  data() {
    return {
      loading: true,
      loadingMore: false,
      deletingCollectionId: null,
      collections: [],
      searchKeyword: '',
      sortBy: 'createdAt',
      filterType: '',
      viewMode: 'grid',
      displayCount: 12,
      pageSize: 12,
      showCreateDialog: false,
      showEditDialog: false,
      editingCollection: null,
      skeletonCount: 6,
      searchDebounceTimer: null,
      scrollThrottleTimer: null
    }
  },
  computed: {
    ...mapState('user', ['token', 'userInfo']),
    isLoggedIn() {
      return !!this.token || !!localStorage.getItem('token');
    },
    emptyStateTitle() {
      return this.isLoggedIn ? '暂无收藏集' : '暂无公开收藏集';
    },
    emptyStateDesc() {
      return this.isLoggedIn ? '开始创建你的第一个收藏集吧' : '登录后查看更多精彩内容';
    },
    emptyStateButtonText() {
      return this.isLoggedIn ? '创建第一个收藏集' : '立即登录';
    },
    filteredCollections() {
      let result = [...(this.collections || [])]

      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(collection =>
          (collection?.name || '').toLowerCase().includes(keyword) ||
          (collection?.description || '').toLowerCase().includes(keyword)
        )
      }

      if (this.filterType) {
        switch (this.filterType) {
          case 'shared':
            result = result.filter(c => c?.isShared || c?.isPublic)
            break
          case 'private':
            result = result.filter(c => c?.isPublic !== 1)
            break
          case 'default':
            result = result.filter(c => c?.isDefault)
            break
        }
      }

      result.sort((a, b) => {
        switch (this.sortBy) {
          case 'createdAt':
            return new Date(b?.createdAt || 0) - new Date(a?.createdAt || 0)
          case 'updatedAt':
            return new Date(b?.updatedAt || b?.createdAt || 0) - new Date(a?.updatedAt || a?.createdAt || 0)
          case 'itemCount':
            return (b?.itemCount || 0) - (a?.itemCount || 0)
          case 'name':
            return (a?.name || '').localeCompare(b?.name || '')
          default:
            return 0
        }
      })

      return result
    },
    displayedCollections() {
      return this.filteredCollections.slice(0, this.displayCount)
    },
    hasMore() {
      return this.displayCount < this.filteredCollections.length
    }
  },
  async created() {
    await this.loadCollections()
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll)
  },
  watch: {
    sortBy() {
      this.displayCount = this.pageSize
    },
    filterType() {
      this.displayCount = this.pageSize
    }
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
    if (this.searchDebounceTimer) {
      clearTimeout(this.searchDebounceTimer)
    }
    if (this.scrollThrottleTimer) {
      clearTimeout(this.scrollThrottleTimer)
    }
  },
  methods: {
    handleScroll() {
      if (this.scrollThrottleTimer) {
        return
      }

      this.scrollThrottleTimer = setTimeout(() => {
        this.scrollThrottleTimer = null
        this.checkScrollPosition()
      }, 200)
    },
    checkScrollPosition() {
      if (this.loadingMore || !this.hasMore || this.loading) {
        return
      }

      const scrollTop = window.pageYOffset || document.documentElement.scrollTop
      const windowHeight = window.innerHeight
      const documentHeight = document.documentElement.scrollHeight

      const scrollPercentage = (scrollTop + windowHeight) / documentHeight

      if (scrollPercentage > 0.8) {
        this.loadMore()
      }
    },
    loadMore() {
      if (this.loadingMore || !this.hasMore) {
        return
      }

      this.loadingMore = true

      setTimeout(() => {
        const increment = this.pageSize
        this.displayCount = Math.min(
          this.displayCount + increment,
          this.filteredCollections.length
        )
        this.loadingMore = false
      }, 500)
    },
    handleSearchInput() {
      if (this.searchDebounceTimer) {
        clearTimeout(this.searchDebounceTimer)
      }
      this.searchDebounceTimer = setTimeout(() => {
        this.displayCount = this.pageSize
      }, 300)
    },
    async loadCollections() {
      this.loading = true
      try {
        const response = await collectionsApi.getUserCollections()
        const resCode = response?.code ?? response?.data?.code
        const resData = response?.data ?? response?.data?.data ?? []
        const resMsg = response?.message ?? response?.msg ?? response?.data?.msg ?? '加载失败'

        if (resCode === 200 || (Array.isArray(resData) && resData.length >= 0)) {
          const rawCollections = Array.isArray(resData) ? resData : (resData?.records || resData?.list || [])
          this.collections = rawCollections.filter(item => item && typeof item === 'object' && item.id)
        } else {
          this.$message.error(resMsg)
          this.collections = []
        }
      } catch (error) {
        console.error('加载收藏集失败', error)
        const errorMsg = error?.response?.data?.message || error?.message || '网络错误，请稍后重试'
        this.$message.error(errorMsg)
        this.collections = []
      } finally {
        this.loading = false
      }
    },
    handleCollectionClick(collection) {
      this.$router.push({
        path: `/collections/${collection?.id}`,
        query: { from: 'my-collections' }
      })
    },
    handleCardAction(command, collection) {
      switch (command) {
        case 'edit':
          this.editingCollection = collection
          this.showEditDialog = true
          break
        case 'view':
          this.$router.push({
            path: `/collections/${collection?.id}`,
            query: { from: 'my-collections' }
          })
          break
        case 'setDefault':
          this.handleSetDefault(collection)
          break
        case 'share':
          this.handleShare(collection)
          break
        case 'delete':
          this.handleDelete(collection)
          break
      }
    },
    async handleSetDefault(collection) {
      this.$message.info(`设为默认功能待补充：${collection?.name || ''}`)
    },
    async handleShare(collection) {
      this.$message.info(`分享功能待补充：${collection?.name || ''}`)
    },
    async handleDelete(collection) {
      const id = collection?.id
      if (!id) {
        return
      }

      if (this.deletingCollectionId) {
        return
      }

      if (!this.userInfo?.id || this.userInfo.id !== collection?.userId) {
        this.$message.error('无权限删除该收藏集')
        return
      }

      try {
        await this.$confirm(`确定删除收藏集「${collection.title || collection.name || ''}」吗？删除后不可恢复。`, '删除确认', {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        })

        this.deletingCollectionId = id
        await collectionsApi.deleteCollection(id)
        this.$message.success('删除成功')
        await this.loadCollections()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || '删除失败')
        }
      } finally {
        if (this.deletingCollectionId === id) {
          this.deletingCollectionId = null
        }
      }
    },
    handleCollectionCreated() {
      this.showCreateDialog = false
      this.loadCollections()
    },
    handleCollectionUpdated() {
      this.showEditDialog = false
      this.loadCollections()
    },
    handleEmptyAction() {
      if (this.isLoggedIn) {
        this.showCreateDialog = true
        return
      }
      this.$router.push('/login')
    },
    formatDate(date) {
      if (!date) {
        return '未知时间'
      }
      const parsed = new Date(date)
      return Number.isNaN(parsed.getTime()) ? '未知时间' : parsed.toLocaleDateString('zh-CN')
    },
    handleCoverError(event, collection) {
      if (!collection) return

      console.warn(`收藏集「${collection?.name || '未知'}」封面加载失败:`, event?.target?.src || '无URL')

      if (collection.coverImage) {
        this.$set(collection, 'coverImage', null)
      }
      if (collection.coverUrl) {
        this.$set(collection, 'coverUrl', null)
      }

      this.$forceUpdate()
    }
  }
}
</script>

<style scoped>
.collections-center {
  min-height: 100vh;
  background-color: var(--bg-page);
  scroll-behavior: smooth;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-4);
  will-change: scroll-position;
}

.toolbar {
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all var(--transition-base);
}

.toolbar:hover {
  box-shadow: var(--shadow-md);
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.toolbar-left {
  flex: 1;
  min-width: 280px;
}

.search-input {
  width: 100%;
}

.search-input >>> .el-input__inner {
  border-radius: var(--radius-md);
  border-color: var(--border-base);
  transition: all var(--transition-base);
  font-size: var(--font-size-base);
  height: 40px;
}

.search-input >>> .el-input__inner:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-bg);
}

.toolbar-right {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.toolbar-select {
  min-width: 140px;
}

.toolbar-select >>> .el-input__inner {
  border-radius: var(--radius-md);
  border-color: var(--border-base);
  transition: all var(--transition-base);
}

.toolbar-select >>> .el-input__inner:hover {
  border-color: var(--primary-color);
}

.create-btn {
  white-space: nowrap;
  font-weight: 500;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.create-btn:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.create-btn:active {
  transform: translateY(0);
}

.loading-state {
  margin-bottom: var(--space-6);
}

.skeleton-card {
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-light);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.skeleton-cover {
  width: 100%;
  height: 200px;
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

.skeleton-content {
  padding: var(--space-4);
}

.skeleton-title {
  height: 20px;
  width: 70%;
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-3);
}

.skeleton-tags {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.skeleton-tag {
  height: 22px;
  width: 50px;
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

.skeleton-description {
  margin-bottom: var(--space-4);
}

.skeleton-line {
  height: 14px;
  width: 100%;
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-2);
}

.skeleton-line.short {
  width: 60%;
}

.skeleton-meta {
  display: flex;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-lighter);
}

.skeleton-meta-item {
  height: 14px;
  width: 80px;
  background: linear-gradient(
    90deg,
    var(--bg-canvas) 0%,
    var(--bg-page) 50%,
    var(--bg-canvas) 100%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

.empty-state {
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-12) var(--space-6);
  text-align: center;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-6);
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
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-3) 0;
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-6) 0;
  line-height: 1.6;
}

.collections-container {
  display: grid;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
}

.collections-container.view-grid {
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
}

.collections-container.view-list {
  grid-template-columns: 1fr;
}

.collection-card-wrapper {
  animation: fadeInUp 0.4s ease-out;
  will-change: transform, opacity;
  contain: layout style paint;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.collection-card {
  height: 100%;
  cursor: pointer;
  transition: all var(--transition-base);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-light);
  background-color: var(--bg-container);
  will-change: transform, box-shadow;
  backface-visibility: hidden;
  transform: translateZ(0);
}

.collection-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.collection-card >>> .el-card__body {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-cover-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--bg-canvas) 0%, var(--bg-page) 100%);
  flex-shrink: 0;
}

.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.collection-card:hover .card-cover {
  transform: scale(1.08);
}

.placeholder-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 48px;
  opacity: 0.9;
}

.card-header {
  padding: var(--space-4) var(--space-4) var(--space-3);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  border-bottom: 1px solid var(--border-lighter);
}

.card-title-area {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.card-tags {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.card-tags >>> .el-tag {
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
  height: 22px;
  line-height: 20px;
  padding: 0 8px;
  border: none;
}

.card-tags >>> .el-tag--warning {
  background-color: var(--warning-bg);
  color: var(--warning-color);
}

.card-tags >>> .el-tag--success {
  background-color: var(--success-bg);
  color: var(--success-color);
}

.card-tags >>> .el-tag--info {
  background-color: var(--info-bg);
  color: var(--info-color);
}

.card-actions {
  flex-shrink: 0;
}

.card-actions >>> .el-button {
  color: var(--text-secondary);
  transition: all var(--transition-base);
}

.card-actions >>> .el-button:hover {
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

.card-description {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 44px;
  flex: 1;
}

.card-meta {
  padding: var(--space-3) var(--space-4) var(--space-4);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  background-color: var(--bg-canvas);
  border-top: 1px solid var(--border-lighter);
  margin-top: auto;
}

.meta-left,
.meta-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  white-space: nowrap;
}

.meta-item i {
  font-size: var(--font-size-sm);
  color: var(--text-placeholder);
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: var(--space-6) 0;
  margin-bottom: var(--space-6);
}

.loading-more {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-base);
  color: var(--primary-color);
  padding: var(--space-4) var(--space-6);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--primary-light);
}

.loading-more i {
  font-size: var(--font-size-lg);
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.scroll-hint {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  padding: var(--space-3) var(--space-5);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  transition: all var(--transition-base);
  cursor: default;
}

.scroll-hint:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.scroll-hint i {
  font-size: var(--font-size-base);
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(5px);
  }
}

.no-more-data {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-5) 0;
  margin-bottom: var(--space-6);
  font-size: var(--font-size-sm);
  color: var(--text-placeholder);
}

.no-more-data i {
  font-size: var(--font-size-base);
  color: var(--success-color);
}

@media (max-width: 1200px) {
  .collections-container.view-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 992px) {
  .page-content {
    padding: var(--space-4) var(--space-3);
  }

  .collections-container.view-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-4);
  }

  .toolbar {
    padding: var(--space-3);
  }

  .toolbar-row {
    gap: var(--space-3);
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: var(--space-3) var(--space-2);
  }

  .toolbar-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left {
    width: 100%;
    min-width: 100%;
  }

  .toolbar-right {
    width: 100%;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .toolbar-select {
    flex: 1;
    min-width: 0;
  }

  .create-btn {
    width: 100%;
    margin-top: var(--space-2);
  }

  .collections-container.view-grid {
    grid-template-columns: 1fr;
    gap: var(--space-4);
  }

  .card-cover-wrapper {
    height: 180px;
  }

  .card-header {
    padding: var(--space-3);
  }

  .card-description {
    padding: var(--space-2) var(--space-3);
  }

  .card-meta {
    padding: var(--space-2) var(--space-3) var(--space-3);
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }

  .meta-left,
  .meta-right {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .page-content {
    padding: var(--space-2);
  }

  .toolbar {
    padding: var(--space-2);
    margin-bottom: var(--space-4);
  }

  .collections-container {
    gap: var(--space-3);
    margin-bottom: var(--space-4);
  }

  .card-cover-wrapper {
    height: 160px;
  }

  .card-title {
    font-size: var(--font-size-base);
  }

  .empty-state {
    padding: var(--space-8) var(--space-4);
    min-height: 300px;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-title {
    font-size: var(--font-size-xl);
  }
}

@media (prefers-reduced-motion: reduce) {
  .collection-card,
  .card-cover,
  .empty-icon {
    animation: none;
    transition: none;
  }

  .collection-card:hover {
    transform: none;
  }

  .collection-card:hover .card-cover {
    transform: none;
  }
}
</style>
