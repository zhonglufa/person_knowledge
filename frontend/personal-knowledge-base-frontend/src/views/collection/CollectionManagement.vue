<template>
  <div class="collection-management-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
              <el-breadcrumb-item>收藏管理</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <h1 class="page-title">收藏管理</h1>
          <p class="page-subtitle">这是收藏中心的过渡工作页，后续会进一步回归收藏域主入口与统一布局。</p>
        </div>
        <div class="header-right">
          <el-button type="text" @click="$router.push('/collect/center')">
            前往收藏中心 <i class="fas fa-arrow-right"></i>
          </el-button>
          <el-button @click="showImportDialog = true">
            <i class="fas fa-download"></i>
            导入收藏夹
          </el-button>
          <el-button type="primary" @click="createCollection">
            <i class="fas fa-plus"></i>
            新建收藏集
          </el-button>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏集或收藏项"
            prefix-icon="fas fa-search"
            class="search-input"
            @input="handleSearch"
          />
        </div>
        <div class="toolbar-right">
          <el-select
            v-model="filterType"
            placeholder="类型筛选"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="all"></el-option>
            <el-option label="收藏集" value="collection"></el-option>
            <el-option label="收藏项" value="item"></el-option>
          </el-select>
          <el-select
            v-model="sortBy"
            placeholder="排序方式"
            @change="handleSortChange"
          >
            <el-option label="创建时间" value="createTime"></el-option>
            <el-option label="更新时间" value="updateTime"></el-option>
            <el-option label="名称" value="title"></el-option>
          </el-select>
          <el-button
            :type="showTrash ? 'warning' : 'default'"
            @click="toggleTrashView"
          >
            <i :class="showTrash ? 'fas fa-trash-restore' : 'fas fa-trash'"></i>
            {{ showTrash ? '返回收藏' : '回收站' }}
          </el-button>
        </div>
      </div>

      <div v-if="selectedItems.length > 0" class="batch-bar">
        <div class="batch-info">
          <i class="fas fa-check-circle"></i>
          <span>已选择 {{ selectedItems.length }} 项</span>
        </div>
        <div class="batch-actions">
          <template v-if="!showTrash">
            <el-button size="small" type="danger" @click="handleBatchDelete">
              <i class="fas fa-trash"></i> 批量删除
            </el-button>
            <el-button size="small" type="warning" @click="handleBatchPermanentDelete">
              <i class="fas fa-bomb"></i> 批量永久删除
            </el-button>
          </template>
          <template v-else>
            <el-button size="small" type="success" @click="handleBatchRecover">
              <i class="fas fa-trash-restore"></i> 批量恢复
            </el-button>
            <el-button size="small" type="danger" @click="handleBatchPermanentDelete">
              <i class="fas fa-bomb"></i> 批量永久删除
            </el-button>
          </template>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </div>
      </div>

      <div class="collection-stats">
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-folder"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.collections }}</div>
            <div class="stat-label">收藏集</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-bookmark"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.items }}</div>
            <div class="stat-label">收藏项</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-star"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.starred }}</div>
            <div class="stat-label">星标项</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.processed }}</div>
            <div class="stat-label">已加工</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon">
            <i class="fas fa-clock"></i>
          </div>
          <div class="stat-info">
            <div class="stat-number">{{ stats.pending }}</div>
            <div class="stat-label">待加工</div>
          </div>
        </div>
      </div>

      <el-alert
        v-if="showTrash"
        title="当前为回收站视图"
        type="warning"
        :closable="false"
        show-icon
        class="trash-alert"
      >
        <template slot="default">
          回收站中的内容将在30天后自动永久删除，请及时恢复或永久删除。
        </template>
      </el-alert>

      <div class="collections-section">
        <div class="section-header">
          <h3>{{ showTrash ? '回收站 - 收藏集' : '我的收藏集' }}</h3>
          <el-button type="text" @click="viewAllCollections">查看全部</el-button>
        </div>

        <div v-if="loading" class="loading-container">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>

        <div v-else-if="collections.length === 0" class="empty-state">
          <div class="empty-icon">
            <i :class="showTrash ? 'fas fa-trash' : 'fas fa-folder-open'"></i>
          </div>
          <h3>{{ showTrash ? '回收站为空' : '暂无收藏集' }}</h3>
          <p>{{ showTrash ? '回收站中没有收藏集' : '您还没有创建任何收藏集' }}</p>
          <el-button v-if="!showTrash" type="primary" @click="createCollection">
            创建第一个收藏集
          </el-button>
        </div>

        <div v-else class="collections-grid">
          <div
            v-for="collection in collections"
            :key="collection.id"
            class="collection-card"
            :class="{ 'is-selected': selectedItems.includes(collection.id) }"
            @click="toggleSelect(collection.id)"
          >
            <div class="card-header">
              <div class="collection-info">
                <i class="fas fa-folder collection-icon"></i>
                <div class="collection-text">
                  <h4 class="collection-title">{{ collection.title }}</h4>
                  <p class="collection-desc">{{ collection.description || '暂无描述' }}</p>
                </div>
              </div>
              <div class="collection-actions" @click.stop>
                <el-dropdown @command="handleCollectionAction($event, collection)">
                  <el-button type="text" size="small">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <template v-if="!showTrash">
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="process">加工</el-dropdown-item>
                      <el-dropdown-item command="share">分享</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <i class="fas fa-trash text-danger"></i> 删除
                      </el-dropdown-item>
                    </template>
                    <template v-else>
                      <el-dropdown-item command="recover">
                        <i class="fas fa-trash-restore text-success"></i> 恢复
                      </el-dropdown-item>
                      <el-dropdown-item command="permanentDelete" divided>
                        <i class="fas fa-bomb text-danger"></i> 永久删除
                      </el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>

            <div class="card-body">
              <div class="collection-meta">
                <div class="meta-item">
                  <i class="fas fa-book"></i>
                  <span>{{ collection.itemCount || 0 }} 项</span>
                </div>
                <div class="meta-item">
                  <i class="fas fa-check-circle"></i>
                  <span>{{ collection.processedCount || 0 }} 已加工</span>
                </div>
                <div v-if="collection.isStarred" class="meta-item starred">
                  <i class="fas fa-star"></i>
                  <span>星标</span>
                </div>
              </div>

              <div class="collection-progress">
                <el-progress
                  :percentage="calculateProgress(collection.processedCount, collection.itemCount)"
                  :status="getProgressStatus(collection.processedCount, collection.itemCount)"
                ></el-progress>
              </div>
            </div>

            <div class="card-footer">
              <div class="collection-date">
                {{ formatDate(collection.updateTime) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="items-section">
        <div class="section-header">
          <h3>{{ showTrash ? '回收站 - 收藏项' : '最近收藏项' }}</h3>
          <el-button type="text" @click="viewAllItems">查看全部</el-button>
        </div>

        <div v-if="loadingItems" class="loading-container">
          <i class="fas fa-spinner fa-spin"></i>
          <span>加载中...</span>
        </div>

        <div v-else-if="items.length === 0" class="empty-state">
          <div class="empty-icon">
            <i :class="showTrash ? 'fas fa-trash' : 'fas fa-bookmark'"></i>
          </div>
          <h3>{{ showTrash ? '回收站为空' : '暂无收藏项' }}</h3>
          <p>{{ showTrash ? '回收站中没有收藏项' : '您还没有添加任何收藏项' }}</p>
        </div>

        <div v-else class="items-list">
          <el-table
            :data="items"
            style="width: 100%"
            @selection-change="handleTableSelectionChange"
          >
            <el-table-column type="selection" width="50"></el-table-column>
            <el-table-column prop="title" label="标题" width="300">
              <template slot-scope="scope">
                <div class="item-title-cell">
                  <i :class="getItemIcon(scope.row.type)" class="item-icon"></i>
                  <i
                    v-if="scope.row.isStarred"
                    class="fas fa-star item-star"
                    @click.stop="toggleItemStar(scope.row)"
                  ></i>
                  <span>{{ scope.row.title }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="source" label="来源" width="200"></el-table-column>
            <el-table-column prop="digestStatus" label="加工状态" width="120">
              <template slot-scope="scope">
                <el-tag :type="getStatusType(scope.row.digestStatus)">
                  {{ getStatusText(scope.row.digestStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="收藏时间" width="180">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="scope">
                <template v-if="!showTrash">
                  <el-button
                    type="text"
                    size="small"
                    @click="processItem(scope.row)"
                  >
                    加工
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click="editItem(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    :class="{ 'is-starred': scope.row.isStarred }"
                    @click="toggleItemStar(scope.row)"
                  >
                    <i :class="scope.row.isStarred ? 'fas fa-star' : 'far fa-star'"></i>
                    {{ scope.row.isStarred ? '取消星标' : '星标' }}
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    class="text-danger"
                    @click="deleteItem(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
                <template v-else>
                  <el-button
                    type="text"
                    size="small"
                    class="text-success"
                    @click="recoverItem(scope.row)"
                  >
                    <i class="fas fa-trash-restore"></i> 恢复
                  </el-button>
                  <el-button
                    type="text"
                    size="small"
                    class="text-danger"
                    @click="permanentDeleteItem(scope.row)"
                  >
                    <i class="fas fa-bomb"></i> 永久删除
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="itemsTotal > itemsPageSize" class="items-pagination">
            <el-pagination
              :current-page="itemsCurrentPage"
              :page-sizes="[10, 20, 50]"
              :page-size="itemsPageSize"
              layout="total, sizes, prev, pager, next"
              :total="itemsTotal"
              @size-change="handleItemsSizeChange"
              @current-change="handleItemsPageChange"
            ></el-pagination>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      :visible.sync="editCollectionDialogVisible"
      :title="editingCollection ? '编辑收藏集' : '创建收藏集'"
      width="500px"
      @close="resetCollectionForm"
    >
      <el-form :model="collectionForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="collectionForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="collectionForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          ></el-input>
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="collectionForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="选择或添加标签"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag"
              :label="tag"
              :value="tag"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button @click="editCollectionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCollection">保存</el-button>
      </div>
    </el-dialog>

    <bookmark-import-dialog
      :visible.sync="showImportDialog"
      @import-complete="handleImportComplete"
    />
  </div>
</template>

<script>
import { collectionsApi } from '@/api/collections'
import { collectApi } from '@/api/collect'
import BookmarkImportDialog from '@/components/collect/BookmarkImportDialog.vue'

export default {
  name: 'CollectionManagement',
  components: {
    BookmarkImportDialog
  },
  data() {
    return {
      loading: false,
      loadingItems: false,
      searchKeyword: '',
      filterType: 'all',
      sortBy: 'updateTime',
      showTrash: false,
      selectedItems: [],
      showImportDialog: false,
      collectionsCurrentPage: 1,
      collectionsPageSize: 10,
      collectionsTotal: 0,
      itemsCurrentPage: 1,
      itemsPageSize: 10,
      itemsTotal: 0,
      stats: {
        collections: 0,
        items: 0,
        starred: 0,
        processed: 0,
        pending: 0
      },
      collections: [],
      items: [],
      editCollectionDialogVisible: false,
      editingCollection: null,
      collectionForm: {
        title: '',
        description: '',
        tags: []
      },
      availableTags: [
        '前端开发', '后端开发', '设计', '产品', '运营', '工具', '教程', '文章'
      ]
    }
  },
  created() {
    this.loadStats()
    this.loadCollections()
    this.loadItems()
  },
  methods: {
    async loadStats() {
      try {
        const [collectionRes, itemStatsRes] = await Promise.all([
          collectionsApi.getUserCollections({ pageNum: 1, pageSize: 1 }),
          collectApi.getStatistics()
        ])
        const collectionPayload = collectionRes?.data?.data || collectionRes?.data || {}
        const totalCollections = collectionPayload.total || collectionPayload.records?.length || 0

        const itemPayload = itemStatsRes?.data?.data || itemStatsRes?.data || {}
        const totalItems = itemPayload.total || 0
        const starred = itemPayload.starred || 0
        const processed = itemPayload.digested || 0
        const pending = totalItems - processed

        this.stats = {
          collections: totalCollections,
          items: totalItems,
          starred,
          processed,
          pending: pending > 0 ? pending : 0
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    async loadCollections() {
      this.loading = true
      try {
        const params = {
          pageNum: this.collectionsCurrentPage,
          pageSize: this.collectionsPageSize
        }
        if (this.searchKeyword) params.keyword = this.searchKeyword
        if (this.showTrash) params.deleted = true

        const response = await collectionsApi.getUserCollections(params)
        const payload = response?.data?.data || response?.data || {}
        this.collections = Array.isArray(payload) ? payload : (payload.records || payload.list || [])
        this.collectionsTotal = payload.total || this.collections.length
      } catch (error) {
        console.error('加载收藏集失败:', error)
        this.$message.error('加载收藏集失败')
      } finally {
        this.loading = false
      }
    },
    async loadItems() {
      this.loadingItems = true
      try {
        const params = {
          pageNum: this.itemsCurrentPage,
          pageSize: this.itemsPageSize
        }
        if (this.searchKeyword) params.keyword = this.searchKeyword
        if (this.showTrash) params.deleted = true

        const response = await collectApi.getCollectList(params)
        const payload = response?.data?.data || response?.data || {}
        this.items = Array.isArray(payload) ? payload : (payload.records || payload.list || [])
        this.itemsTotal = payload.total || this.items.length
      } catch (error) {
        console.error('加载收藏项失败:', error)
        this.$message.error('加载收藏项失败')
      } finally {
        this.loadingItems = false
      }
    },
    handleSearch() {
      this.collectionsCurrentPage = 1
      this.itemsCurrentPage = 1
      this.loadCollections()
      this.loadItems()
    },
    handleFilterChange() {
      this.collectionsCurrentPage = 1
      this.itemsCurrentPage = 1
      this.loadCollections()
      this.loadItems()
    },
    handleSortChange() {
      this.collectionsCurrentPage = 1
      this.itemsCurrentPage = 1
      this.loadCollections()
      this.loadItems()
    },
    handleItemsSizeChange(val) {
      this.itemsPageSize = val
      this.itemsCurrentPage = 1
      this.loadItems()
    },
    handleItemsPageChange(val) {
      this.itemsCurrentPage = val
      this.loadItems()
    },
    toggleTrashView() {
      this.showTrash = !this.showTrash
      this.clearSelection()
      this.collectionsCurrentPage = 1
      this.itemsCurrentPage = 1
      this.loadCollections()
      this.loadItems()
    },
    toggleSelect(id) {
      const index = this.selectedItems.indexOf(id)
      if (index > -1) {
        this.selectedItems.splice(index, 1)
      } else {
        this.selectedItems.push(id)
      }
    },
    handleTableSelectionChange(selection) {
      this.selectedItems = selection.map(item => item.id)
    },
    clearSelection() {
      this.selectedItems = []
    },
    calculateProgress(processedCount, itemCount) {
      if (!itemCount) return 0
      return Math.round((processedCount || 0) / itemCount * 100)
    },
    getProgressStatus(processedCount, itemCount) {
      const progress = this.calculateProgress(processedCount, itemCount)
      if (progress === 100) return 'success'
      if (progress >= 50) return ''
      return 'exception'
    },
    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },
    getItemIcon(type) {
      const iconMap = {
        article: 'fas fa-file-alt',
        tutorial: 'fas fa-graduation-cap',
        video: 'fas fa-video',
        image: 'fas fa-image',
        document: 'fas fa-file-pdf',
        link: 'fas fa-link'
      }
      return iconMap[type] || 'fas fa-bookmark'
    },
    getStatusType(status) {
      const typeMap = {
        undigest: 'info',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        undigest: '未加工',
        digesting: '加工中',
        digested: '已加工',
        abandoned: '已放弃'
      }
      return textMap[status] || '未知'
    },
    createCollection() {
      this.editingCollection = null
      this.resetCollectionForm()
      this.editCollectionDialogVisible = true
    },
    editCollection(collection) {
      this.editingCollection = collection
      this.collectionForm = {
        title: collection.title || '',
        description: collection.description || '',
        tags: collection.tags || []
      }
      this.editCollectionDialogVisible = true
    },
    resetCollectionForm() {
      this.collectionForm = {
        title: '',
        description: '',
        tags: []
      }
    },
    async saveCollection() {
      if (!this.collectionForm.title.trim()) {
        this.$message.warning('请输入收藏集标题')
        return
      }

      try {
        if (this.editingCollection) {
          await collectionsApi.updateCollection(this.editingCollection.id, this.collectionForm)
          this.$message.success('收藏集更新成功')
        } else {
          await collectionsApi.createCollection(this.collectionForm)
          this.$message.success('收藏集创建成功')
        }
        this.editCollectionDialogVisible = false
        this.resetCollectionForm()
        await this.loadStats()
        await this.loadCollections()
      } catch (error) {
        console.error('保存收藏集失败:', error)
        this.$message.error('保存收藏集失败')
      }
    },
    async toggleItemStar(item) {
      try {
        await collectApi.toggleStar(item.id)
        item.isStarred = !item.isStarred
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        console.error('切换收藏项星标失败:', error)
        this.$message.error('操作失败')
      }
    },
    handleCollectionAction(command, collection) {
      const actionMap = {
        edit: () => this.editCollection(collection),
        process: () => this.processCollection(collection),
        share: () => this.shareCollection(collection),
        delete: () => this.deleteCollection(collection),
        recover: () => this.recoverCollection(collection),
        permanentDelete: () => this.permanentDeleteCollection(collection)
      }
      const action = actionMap[command]
      if (action) {
        action()
      }
    },
    processCollection(collection) {
      this.$router.push(`/creation/processing/tasks?collectionId=${collection.id}`)
    },
    shareCollection() {
      this.$message.info('分享功能开发中')
    },
    async deleteCollection(collection) {
      try {
        await this.$confirm(`确定删除收藏集「${collection.title}」吗？`, '删除确认', {
          type: 'warning'
        })
        await collectionsApi.deleteCollection(collection.id)
        this.$message.success('删除成功')
        await this.loadStats()
        await this.loadCollections()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除收藏集失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    recoverCollection() {
      this.$message.info('恢复功能开发中')
    },
    permanentDeleteCollection() {
      this.$message.info('永久删除功能开发中')
    },
    processItem(item) {
      this.$router.push(`/creation/processing/tasks?itemId=${item.id}`)
    },
    editItem(item) {
      this.$router.push(`/creation/collection-items/${item.id}/note/create`)
    },
    async deleteItem(item) {
      try {
        await this.$confirm(`确定删除收藏项「${item.title}」吗？`, '删除确认', {
          type: 'warning'
        })
        await collectApi.deleteCollect(item.id)
        this.$message.success('删除成功')
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除收藏项失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    async recoverItem(item) {
      try {
        await collectApi.recoverCollect(item.id)
        this.$message.success('恢复成功')
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        console.error('恢复收藏项失败:', error)
        this.$message.error('恢复失败')
      }
    },
    async permanentDeleteItem(item) {
      try {
        await this.$confirm(`确定永久删除收藏项「${item.title}」吗？此操作不可恢复！`, '永久删除确认', {
          type: 'error',
          confirmButtonText: '永久删除',
          cancelButtonText: '取消'
        })
        await collectApi.permanentDeleteCollect(item.id)
        this.$message.success('永久删除成功')
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('永久删除收藏项失败:', error)
          this.$message.error('永久删除失败')
        }
      }
    },
    async handleBatchDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要操作的项目')
        return
      }
      try {
        await this.$confirm(`确定删除选中的 ${this.selectedItems.length} 个项目吗？`, '批量删除确认', {
          type: 'warning'
        })
        await collectApi.batchDeleteCollect(this.selectedItems)
        this.$message.success('批量删除成功')
        this.clearSelection()
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }
    },
    async handleBatchRecover() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要操作的项目')
        return
      }
      try {
        await collectApi.batchRecoverCollect(this.selectedItems)
        this.$message.success('批量恢复成功')
        this.clearSelection()
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        console.error('批量恢复失败:', error)
        this.$message.error('批量恢复失败')
      }
    },
    async handleBatchPermanentDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要操作的项目')
        return
      }
      try {
        await this.$confirm(`确定永久删除选中的 ${this.selectedItems.length} 个项目吗？此操作不可恢复！`, '批量永久删除确认', {
          type: 'error',
          confirmButtonText: '永久删除',
          cancelButtonText: '取消'
        })
        await collectApi.batchPermanentDeleteCollect(this.selectedItems)
        this.$message.success('批量永久删除成功')
        this.clearSelection()
        await this.loadStats()
        await this.loadItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量永久删除失败:', error)
          this.$message.error('批量永久删除失败')
        }
      }
    },
    viewAllCollections() {
      this.$router.push('/collect/center')
    },
    viewAllItems() {
      this.$router.push('/collect/center')
    },
    handleImportComplete() {
      this.loadStats()
      this.loadCollections()
      this.loadItems()
      this.$message.success('导入完成')
    }
  }
}
</script>

<style scoped>
.collection-management-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  padding: 24px 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  max-width: 1400px;
  margin: 0 auto;
}

.header-left {
  flex: 1;
}

.breadcrumb {
  margin-bottom: 12px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.toolbar-left {
  flex: 1;
  max-width: 400px;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 100%;
}

.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.batch-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-weight: 500;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.collection-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.trash-alert {
  margin-bottom: 24px;
}

.collections-section,
.items-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.loading-container,
.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #c0c4cc;
}

.collections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.collection-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.collection-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.collection-card.is-selected {
  border-color: #409eff;
  background: #f0f9ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.collection-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.collection-icon {
  font-size: 24px;
  color: #409eff;
  margin-top: 2px;
}

.collection-text {
  flex: 1;
  min-width: 0;
}

.collection-title {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.collection-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.collection-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.collection-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.meta-item.starred,
.is-starred {
  color: #e6a23c;
}

.collection-progress {
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.collection-date {
  font-size: 12px;
  color: #909399;
}

.item-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-icon {
  color: #409eff;
}

.item-star {
  color: #e6a23c;
  cursor: pointer;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.items-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .page-header,
  .main-content {
    padding: 20px;
  }

  .header-content,
  .toolbar,
  .batch-bar,
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-right,
  .toolbar-right,
  .batch-actions {
    flex-wrap: wrap;
  }

  .collections-grid {
    grid-template-columns: 1fr;
  }
}
</style>
