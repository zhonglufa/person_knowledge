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
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-state card">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else-if="filteredCollections.length === 0" class="empty-state card">
        <i class="fas fa-layer-group empty-icon"></i>
        <h3 class="empty-title">{{ emptyStateTitle }}</h3>
        <p class="empty-description">{{ emptyStateDesc }}</p>
        <el-button type="primary" @click="handleEmptyAction">
          {{ emptyStateButtonText }}
        </el-button>
      </div>

      <div v-else class="collections-container" :class="`view-${viewMode}`">
        <div
          v-for="collection in filteredCollections"
          :key="collection?.id || Math.random()"
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
                  <el-tag v-if="collection?.isShared || collection?.isPublic" type="success" size="mini">公开</el-tag>
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
                    <el-dropdown-item v-if="!(collection?.isShared || collection?.isPublic)" command="share">分享</el-dropdown-item>
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

      <div v-if="collections?.length > 0" class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="collections.length"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
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
      collections: [],
      searchKeyword: '',
      sortBy: 'createdAt',
      filterType: '',
      viewMode: 'grid',
      currentPage: 1,
      pageSize: 12,
      showCreateDialog: false,
      showEditDialog: false,
      editingCollection: null
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
            result = result.filter(c => !(c?.isShared || c?.isPublic))
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

      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return result.slice(start, end)
    }
  },
  async created() {
    await this.loadCollections()
  },
  methods: {
    async loadCollections() {
      this.loading = true
      try {
        const response = await collectionsApi.getUserCollections()
        // 适配响应拦截器解包后的数据结构
        const resCode = response?.code ?? response?.data?.code
        const resData = response?.data ?? response?.data?.data ?? []
        const resMsg = response?.message ?? response?.msg ?? response?.data?.msg ?? '加载失败'

        if (resCode === 200 || (Array.isArray(resData) && resData.length >= 0)) {
          this.collections = Array.isArray(resData) ? resData : (resData?.records || resData?.list || [])
        } else {
          this.$message.error(resMsg)
        }
      } catch (error) {
        console.error('加载收藏集失败', error)
        this.$message.error(error?.message || '网络错误，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    handleCollectionClick(collection) {
      this.$router.push(`/collections/${collection?.id}`)
    },
    handleCardAction(command, collection) {
      switch (command) {
        case 'edit':
          this.editingCollection = collection
          this.showEditDialog = true
          break
        case 'view':
          this.$router.push(`/collections/${collection?.id}`)
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
      try {
        await this.$confirm(`确定删除收藏集「${collection.title || collection.name || ''}」吗？删除后不可恢复。`, '删除确认', {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        })
        await collectionsApi.deleteCollection(collection.id)
        this.$message.success('删除成功')
        await this.loadCollections()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error?.message || '删除失败')
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
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handleCurrentChange(page) {
      this.currentPage = page
    },
    formatDate(date) {
      if (!date) {
        return '未知时间'
      }
      const parsed = new Date(date)
      return Number.isNaN(parsed.getTime()) ? '未知时间' : parsed.toLocaleDateString('zh-CN')
    },
    handleCoverError(event, collection) {
      console.warn(`收藏集「${collection?.name}」封面加载失败:`, event.target.src)
      // 移除 src 使 v-if 失效，显示占位图
      if (collection) {
        collection.coverImage = null
        collection.coverUrl = null
      }
    }
  }
}
</script>
