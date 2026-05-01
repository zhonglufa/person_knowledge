<template>
  <div class="collection-management-page">
    <!-- 页面头部 -->
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
          <p class="page-subtitle">管理您的收藏集和收藏项</p>
        </div>
        <div class="header-right">
          <!-- 导入浏览器收藏夹 -->
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

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 工具栏 -->
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
          <!-- 回收站查看按钮 -->
          <el-button
            :type="showTrash ? 'warning' : 'default'"
            @click="toggleTrashView"
          >
            <i :class="showTrash ? 'fas fa-trash-restore' : 'fas fa-trash'"></i>
            {{ showTrash ? '返回收藏' : '回收站' }}
          </el-button>
        </div>
      </div>

      <!-- 批量操作栏 -->
      <div v-if="selectedItems.length > 0" class="batch-bar">
        <div class="batch-info">
          <i class="fas fa-check-circle"></i>
          <span>已选择 {{ selectedItems.length }} 项</span>
        </div>
        <div class="batch-actions">
          <!-- 正常视图下的批量操作 -->
          <template v-if="!showTrash">
            <el-button size="small" type="danger" @click="handleBatchDelete">
              <i class="fas fa-trash"></i> 批量删除
            </el-button>
            <el-button size="small" type="warning" @click="handleBatchPermanentDelete">
              <i class="fas fa-bomb"></i> 批量永久删除
            </el-button>
          </template>
          <!-- 回收站视图下的批量操作 -->
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

      <!-- 收藏统计 -->
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

      <!-- 回收站提示 -->
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

      <!-- 收藏集列表 -->
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
                <!-- 星标按钮 -->
                <el-button
                  type="text"
                  size="small"
                  :class="{ 'is-starred': collection.isStarred }"
                  @click="toggleStar(collection)"
                >
                  <i :class="collection.isStarred ? 'fas fa-star' : 'far fa-star'"></i>
                </el-button>
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
                <!-- 星标显示 -->
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

      <!-- 最近收藏项 -->
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
                  <!-- 星标显示 -->
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
                  <!-- 星标切换 -->
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
        </div>
      </div>
    </div>

    <!-- 编辑收藏集对话框 -->
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

    <!-- 导入浏览器收藏夹对话框 -->
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

      // 回收站视图
      showTrash: false,

      // 批量选择
      selectedItems: [],

      // 导入对话框
      showImportDialog: false,

      // 统计数据
      stats: {
        collections: 0,
        items: 0,
        starred: 0,
        processed: 0,
        pending: 0
      },

      // 收藏集列表
      collections: [],

      // 收藏项列表
      items: [],

      // 编辑对话框相关
      editCollectionDialogVisible: false,
      editingCollection: null,
      collectionForm: {
        title: '',
        description: '',
        tags: []
      },

      // 可用标签
      availableTags: [
        '前端开发', '后端开发', '设计', '产品', '运营', '工具', '教程', '文章'
      ]
    }
  },

  created() {
    this.loadCollectionStats()
    this.loadCollections()
    this.loadItems()
  },

  methods: {
    // ==================== 加载数据 ====================
    // 加载收藏统计
    async loadCollectionStats() {
      try {
        const response = await collectApi.getStatistics()
        if (response?.data?.code === 200) {
          this.stats = { ...this.stats, ...response.data.data }
        } else {
          // 暂时使用模拟数据
          this.stats = {
            collections: 12,
            items: 45,
            starred: 8,
            processed: 28,
            pending: 17
          }
        }
      } catch (error) {
        console.error('加载统计失败:', error)
        // 使用模拟数据
        this.stats = {
          collections: 12,
          items: 45,
          starred: 8,
          processed: 28,
          pending: 17
        }
      }
    },

    // 加载收藏集列表
    async loadCollections() {
      this.loading = true
      try {
        const params = { trash: this.showTrash ? 1 : 0 }
        const response = await collectionsApi.getUserCollections()
        this.collections = response?.data || []
      } catch (error) {
        console.error('加载收藏集失败:', error)
        this.$message.error('加载收藏集列表失败')
      } finally {
        this.loading = false
      }
    },

    // 加载收藏项列表
    async loadItems() {
      this.loadingItems = true
      try {
        const params = { trash: this.showTrash ? 1 : 0 }
        // 这里应该调用后端API获取收藏项列表
        // 暂时使用模拟数据
        this.items = [
          {
            id: 1,
            title: 'Vue.js 3.0 新特性详解',
            source: 'vuejs.org',
            digestStatus: 'digested',
            type: 'article',
            isStarred: true,
            createTime: '2024-01-20T10:30:00Z'
          },
          {
            id: 2,
            title: 'React Hooks 最佳实践',
            source: 'reactjs.org',
            digestStatus: 'digesting',
            type: 'article',
            isStarred: false,
            createTime: '2024-01-19T15:45:00Z'
          },
          {
            id: 3,
            title: 'JavaScript 异步编程指南',
            source: 'javascript.info',
            digestStatus: 'undigest',
            type: 'tutorial',
            isStarred: false,
            createTime: '2024-01-18T09:20:00Z'
          }
        ]
      } catch (error) {
        console.error('加载收藏项失败:', error)
      } finally {
        this.loadingItems = false
      }
    },

    // ==================== 搜索和筛选 ====================
    handleSearch() {
      console.log('搜索:', this.searchKeyword)
    },

    handleFilterChange() {
      console.log('筛选:', this.filterType)
    },

    handleSortChange() {
      console.log('排序:', this.sortBy)
    },

    // ==================== 回收站视图 ====================
    toggleTrashView() {
      this.showTrash = !this.showTrash
      this.clearSelection()
      this.loadCollections()
      this.loadItems()
    },

    // ==================== 批量选择 ====================
    toggleSelect(id) {
      const index = this.selectedItems.indexOf(id)
      if (index > -1) {
        this.selectedItems.splice(index, 1)
      } else {
        this.selectedItems.push(id)
      }
    },

    clearSelection() {
      this.selectedItems = []
    },

    handleTableSelectionChange(selection) {
      this.selectedItems = selection.map(item => item.id)
    },

    // ==================== 星标功能 ====================
    async toggleStar(collection) {
      try {
        // 乐观更新
        collection.isStarred = !collection.isStarred
        await collectApi.toggleStar(collection.id)
        this.$message.success(collection.isStarred ? '已添加星标' : '已取消星标')
        this.loadCollectionStats()
      } catch (error) {
        // 回滚
        collection.isStarred = !collection.isStarred
        console.error('切换星标失败:', error)
        this.$message.error('操作失败')
      }
    },

    async toggleItemStar(item) {
      try {
        // 乐观更新
        item.isStarred = !item.isStarred
        await collectApi.toggleStar(item.id)
        this.$message.success(item.isStarred ? '已添加星标' : '已取消星标')
        this.loadCollectionStats()
      } catch (error) {
        // 回滚
        item.isStarred = !item.isStarred
        console.error('切换星标失败:', error)
        this.$message.error('操作失败')
      }
    },

    // ==================== 批量操作 ====================
    // 批量删除（软删除到回收站）
    async handleBatchDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要删除的项')
        return
      }

      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedItems.length} 项吗？删除后可在回收站恢复。`, '批量删除确认', {
          type: 'warning'
        })

        const loading = this.$loading({ text: '正在批量删除...' })
        await collectApi.batchDelete(this.selectedItems)
        loading.close()

        this.$message.success('批量删除成功')
        this.clearSelection()
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }
    },

    // 批量恢复（从回收站恢复）
    async handleBatchRecover() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要恢复的项')
        return
      }

      try {
        await this.$confirm(`确定要恢复选中的 ${this.selectedItems.length} 项吗？`, '批量恢复确认', {
          type: 'info'
        })

        const loading = this.$loading({ text: '正在批量恢复...' })
        await collectApi.batchRecover(this.selectedItems)
        loading.close()

        this.$message.success('批量恢复成功')
        this.clearSelection()
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量恢复失败:', error)
          this.$message.error('批量恢复失败')
        }
      }
    },

    // 批量永久删除
    async handleBatchPermanentDelete() {
      if (this.selectedItems.length === 0) {
        this.$message.warning('请先选择要永久删除的项')
        return
      }

      try {
        await this.$confirm(`确定要永久删除选中的 ${this.selectedItems.length} 项吗？此操作不可恢复！`, '永久删除确认', {
          type: 'error',
          confirmButtonText: '确认永久删除',
          cancelButtonText: '取消'
        })

        const loading = this.$loading({ text: '正在永久删除...' })
        await collectApi.batchPermanentDelete(this.selectedItems)
        loading.close()

        this.$message.success('批量永久删除成功')
        this.clearSelection()
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量永久删除失败:', error)
          this.$message.error('批量永久删除失败')
        }
      }
    },

    // ==================== 单个操作 ====================
    async deleteItem(item) {
      try {
        await this.$confirm(`确定要删除 "${item.title}" 吗？删除后可在回收站恢复。`, '删除确认', {
          type: 'warning'
        })

        await collectApi.deleteCollect(item.id)
        this.$message.success('删除成功')
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },

    async recoverItem(item) {
      try {
        await this.$confirm(`确定要恢复 "${item.title}" 吗？`, '恢复确认', {
          type: 'info'
        })

        await collectApi.recoverCollect(item.id)
        this.$message.success('恢复成功')
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        console.error('恢复失败:', error)
        this.$message.error('恢复失败')
      }
    },

    async permanentDeleteItem(item) {
      try {
        await this.$confirm(`确定要永久删除 "${item.title}" 吗？此操作不可恢复！`, '永久删除确认', {
          type: 'error',
          confirmButtonText: '确认永久删除',
          cancelButtonText: '取消'
        })

        await collectApi.permanentDeleteCollect(item.id)
        this.$message.success('永久删除成功')
        this.loadItems()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('永久删除失败:', error)
          this.$message.error('永久删除失败')
        }
      }
    },

    // ==================== 导入处理 ====================
    handleImportComplete() {
      this.showImportDialog = false
      this.$message.success('导入完成')
      this.loadCollections()
      this.loadItems()
      this.loadCollectionStats()
    },

    // ==================== 工具方法 ====================
    // 格式化日期
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },

    // 计算进度
    calculateProgress(processed, total) {
      if (!total || total === 0) return 0
      return Math.round((processed / total) * 100)
    },

    // 获取进度状态
    getProgressStatus(processed, total) {
      if (!total || total === 0) return ''
      const percentage = this.calculateProgress(processed, total)
      if (percentage >= 80) return 'success'
      if (percentage >= 50) return 'warning'
      return ''
    },

    // 获取项目图标
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

    // 获取状态类型
    getStatusType(status) {
      const typeMap = {
        undigest: 'info',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'danger'
      }
      return typeMap[status] || 'info'
    },

    // 获取状态文本
    getStatusText(status) {
      const textMap = {
        undigest: '未加工',
        digesting: '加工中',
        digested: '已加工',
        abandoned: '已放弃'
      }
      return textMap[status] || '未知'
    },

    // 查看收藏集
    viewCollection(collection) {
      this.$router.push(`/collections/${collection.id}`)
    },

    // 查看全部收藏集
    viewAllCollections() {
      this.$router.push('/collection/center')
    },

    // 查看全部收藏项
    viewAllItems() {
      this.$router.push('/collect/center')
    },

    // 处理收藏集操作
    handleCollectionAction(command, collection) {
      switch(command) {
        case 'edit':
          this.editCollection(collection)
          break
        case 'process':
          this.processCollection(collection)
          break
        case 'share':
          this.shareCollection(collection)
          break
        case 'delete':
          this.deleteCollection(collection)
          break
        case 'recover':
          this.recoverCollection(collection)
          break
        case 'permanentDelete':
          this.permanentDeleteCollection(collection)
          break
      }
    },

    // 编辑收藏集
    editCollection(collection) {
      this.editingCollection = collection
      this.collectionForm = {
        title: collection.title,
        description: collection.description,
        tags: collection.tags || []
      }
      this.editCollectionDialogVisible = true
    },

    // 处理收藏集
    processCollection(collection) {
      this.$router.push(`/collections/${collection.id}`)
    },

    // 分享收藏集
    shareCollection(collection) {
      console.log('分享收藏集:', collection)
    },

    // 删除收藏集
    async deleteCollection(collection) {
      try {
        await this.$confirm(`确定要删除收藏集 "${collection.title}" 吗？`, '确认删除', {
          type: 'warning'
        })

        // 这里应该调用后端API删除收藏集
        // 暂时只是模拟删除
        this.collections = this.collections.filter(c => c.id !== collection.id)
        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败')
        }
      }
    },

    // 恢复收藏集
    async recoverCollection(collection) {
      try {
        await collectApi.recoverCollect(collection.id)
        this.$message.success('恢复成功')
        this.loadCollections()
        this.loadCollectionStats()
      } catch (error) {
        console.error('恢复失败:', error)
        this.$message.error('恢复失败')
      }
    },

    // 永久删除收藏集
    async permanentDeleteCollection(collection) {
      try {
        await this.$confirm(`确定要永久删除收藏集 "${collection.title}" 吗？此操作不可恢复！`, '永久删除确认', {
          type: 'error',
          confirmButtonText: '确认永久删除',
          cancelButtonText: '取消'
        })

        await collectApi.permanentDeleteCollect(collection.id)
        this.$message.success('永久删除成功')
        this.loadCollections()
        this.loadCollectionStats()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('永久删除失败:', error)
          this.$message.error('永久删除失败')
        }
      }
    },

    // 创建收藏集
    createCollection() {
      this.editingCollection = null
      this.collectionForm = {
        title: '',
        description: '',
        tags: []
      }
      this.editCollectionDialogVisible = true
    },

    // 处理收藏项
    processItem(item) {
      this.$router.push({
        name: 'CollectionProcessing',
        params: { id: item.collectionId },
        query: { itemId: item.id }
      })
    },

    // 编辑收藏项
    editItem(item) {
      this.$router.push({
        name: 'CollectionItemEdit',
        params: { id: item.id }
      })
    },

    // 保存收藏集
    async saveCollection() {
      try {
        if (this.editingCollection) {
          // 更新现有收藏集
          this.editingCollection.title = this.collectionForm.title
          this.editingCollection.description = this.collectionForm.description
          this.editingCollection.tags = this.collectionForm.tags

          this.$message.success('更新成功')
        } else {
          // 创建新收藏集
          const newCollection = {
            id: Date.now(),
            ...this.collectionForm,
            itemCount: 0,
            processedCount: 0,
            isStarred: false,
            updateTime: new Date().toISOString()
          }

          this.collections.unshift(newCollection)
          this.$message.success('创建成功')
        }

        this.editCollectionDialogVisible = false
      } catch (error) {
        this.$message.error('保存失败')
      }
    },

    // 重置收藏集表单
    resetCollectionForm() {
      this.editingCollection = null
      this.collectionForm = {
        title: '',
        description: '',
        tags: []
      }
    }
  }
}
</script>

<style scoped>
.collection-management-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background-color: var(--bg-container);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}

.header-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.breadcrumb {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin: 0;
}

.header-right {
  display: flex;
  gap: var(--space-3);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-6);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-4) var(--space-6);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.toolbar-left {
  flex: 1;
  max-width: 400px;
}

.search-input {
  width: 100%;
}

.toolbar-right {
  display: flex;
  gap: var(--space-3);
}

/* 批量操作栏 */
.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-5);
  margin-bottom: var(--space-5);
  background-color: var(--primary-bg);
  border: 1px solid var(--primary-color);
  border-radius: var(--radius-lg);
  animation: slideDown var(--transition-slow) ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.batch-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 500;
  color: var(--primary-color);
}

.batch-actions {
  display: flex;
  gap: var(--space-2);
}

/* 回收站提示 */
.trash-alert {
  margin-bottom: var(--space-5);
}

.collection-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-xl);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin-top: var(--space-1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-light);
}

.section-header h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.collections-section,
.items-section {
  margin-bottom: var(--space-6);
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-5);
  color: var(--text-regular);
}

.loading-container i {
  font-size: var(--font-size-3xl);
  margin-bottom: var(--space-3);
}

.empty-state {
  text-align: center;
  padding: var(--space-16) var(--space-5);
}

.empty-icon {
  font-size: 48px;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.empty-state h3 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.empty-state p {
  color: var(--text-regular);
  margin: 0 0 var(--space-6) 0;
}

.collections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--space-5);
}

.collection-card {
  background-color: var(--bg-page);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 2px solid var(--border-light);
}

.collection-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

/* 选中状态 */
.collection-card.is-selected {
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
}

.collection-info {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  flex: 1;
}

.collection-icon {
  font-size: var(--font-size-3xl);
  color: var(--primary-color);
  margin-top: 2px;
}

.collection-text {
  flex: 1;
}

.collection-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
  line-height: 1.3;
}

.collection-desc {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collection-actions {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* 星标样式 */
.collection-actions .is-starred,
.is-starred {
  color: var(--warning-color) !important;
}

.item-star {
  color: var(--warning-color);
  font-size: var(--font-size-xs);
  margin-right: var(--space-1);
  cursor: pointer;
}

.card-body {
  margin-bottom: var(--space-4);
}

.collection-meta {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.meta-item i {
  font-size: var(--font-size-sm);
}

.meta-item.starred {
  color: var(--warning-color);
}

.collection-progress {
  margin-top: var(--space-2);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-4);
}

.collection-date {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.items-list {
  background-color: var(--bg-page);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.item-title-cell {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.item-icon {
  color: var(--primary-color);
  font-size: var(--font-size-sm);
}

/* 文字颜色工具类 */
.text-danger {
  color: var(--danger-color) !important;
}

.text-success {
  color: var(--success-color) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--space-4);
  }

  .header-right {
    width: 100%;
  }

  .toolbar {
    flex-direction: column;
    gap: var(--space-4);
  }

  .toolbar-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .collection-stats {
    grid-template-columns: 1fr 1fr;
  }

  .collections-grid {
    grid-template-columns: 1fr;
  }

  .batch-bar {
    flex-direction: column;
    gap: var(--space-3);
  }
}

@media (max-width: 576px) {
  .page-header,
  .main-content {
    padding: var(--space-4);
  }

  .collection-stats {
    grid-template-columns: 1fr;
  }

  .collection-card {
    padding: var(--space-4);
  }
}
</style>
