<template>
  <div class="content-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">内容管理</h2>
        <p class="page-desc">管理收藏项与笔记内容</p>
      </div>
      <div class="header-right">
        <el-button type="info" icon="el-icon-document" @click="$router.push('/admin/content-logs')">
          操作日志
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.searchKey"
            placeholder="标题 / 摘要"
            clearable
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>
        <el-form-item v-if="activeTab === 'note'" label="公开状态">
          <el-select
            v-model="queryParams.isPublic"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="公开" :value="1"></el-option>
            <el-option label="私有" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 内容类型标签页 -->
    <el-card class="table-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <!-- 收藏项管理 -->
        <el-tab-pane label="收藏项管理" name="collect">
          <el-table
            :data="contentList"
            v-loading="loading"
            border
            stripe
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="source" label="来源" width="100" align="center" />
            <el-table-column label="所属收藏集公开" width="130" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isPublic === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.isPublic === 1 ? '公开' : '私有' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="消化状态" width="110" align="center">
              <template slot-scope="scope">
                <el-tag :type="getDigestStatusType(scope.row.digestStatus)" size="small">
                  {{ getDigestStatusText(scope.row.digestStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" min-width="170" align="center">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="scope">
                <div class="action-buttons">
                  <el-button
                    v-if="scope.row.isPublic === 1"
                    type="warning"
                    size="mini"
                    icon="el-icon-bottom"
                    @click="handleTakeDownContent('collect', scope.row)"
                  >下架</el-button>
                  <el-button
                    v-else
                    type="success"
                    size="mini"
                    icon="el-icon-top"
                    @click="handleRestoreContent('collect', scope.row)"
                  >恢复</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 笔记管理 -->
        <el-tab-pane label="笔记管理" name="note">
          <el-table
            :data="contentList"
            v-loading="loading"
            border
            stripe
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
            <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
            <el-table-column label="笔记类型" width="110" align="center">
              <template slot-scope="scope">
                <el-tag :type="getNoteTypeType(scope.row.noteType)" size="small">
                  {{ getNoteTypeText(scope.row.noteType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="可见性" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isPublic === 1 ? 'success' : 'info'" size="small">
                  {{ scope.row.isPublic === 1 ? '公开' : '私有' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" min-width="170" align="center">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="scope">
                <div class="action-buttons">
                  <el-button
                    v-if="scope.row.isPublic === 1"
                    type="warning"
                    size="mini"
                    icon="el-icon-bottom"
                    @click="handleTakeDownContent('note', scope.row)"
                  >下架</el-button>
                  <el-button
                    v-else
                    type="success"
                    size="mini"
                    icon="el-icon-top"
                    @click="handleRestoreContent('note', scope.row)"
                  >恢复</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 批量操作栏 -->
      <div v-if="selectedRows.length > 0" class="batch-bar">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <div class="batch-actions">
          <el-button type="warning" size="mini" icon="el-icon-bottom" @click="handleBatchTakeDown">批量下架</el-button>
          <el-button type="success" size="mini" icon="el-icon-top" @click="handleBatchRestore">批量恢复</el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { contentManageApi } from '@/api/admin'

export default {
  name: 'ContentManagement',

  data() {
    return {
      activeTab: 'collect',
      contentList: [],
      selectedRows: [],
      loading: false,

      queryParams: {
        searchKey: '',
        isPublic: ''
      },

      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },

  mounted() {
    this.loadContentList()
  },

  methods: {
    async loadContentList() {
      this.loading = true
      try {
        const params = {
          contentType: this.activeTab,
          pageNum: this.pagination.currentPage,
          pageSize: this.pagination.pageSize,
          searchKey: this.queryParams.searchKey,
          isPublic: this.queryParams.isPublic !== '' ? this.queryParams.isPublic : undefined
        }
        const response = await contentManageApi.getContentList(params)
        if (response.code === 200) {
          // MyBatis Plus Page 对象返回的字段是 records 而不是 list
          this.contentList = response.data?.records || []
          this.pagination.total = response.data?.total || 0
        } else {
          this.$message.error(response.message || '获取内容列表失败')
        }
      } catch (error) {
        console.error('获取内容列表错误:', error)
        this.$message.error('获取内容列表失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.pagination.currentPage = 1
      this.loadContentList()
    },

    handleReset() {
      this.queryParams.searchKey = ''
      this.queryParams.isPublic = ''
      this.pagination.currentPage = 1
      this.loadContentList()
    },

    handleTabChange(tab) {
      this.activeTab = tab.name
      this.pagination.currentPage = 1
      this.selectedRows = []
      this.loadContentList()
    },

    handleSelectionChange(selection) {
      this.selectedRows = selection
    },

    handleTakeDownContent(type, row) {
      const contentType = type === 'collect' ? '收藏项' : '笔记'
      this.$confirm(`确定要下架该${contentType} "${row.title}" 吗？下架后其他用户将无法查看。`, '下架确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await contentManageApi.takeDownContent(type, row.id)
          if (response.code === 200) {
            this.$message.success('下架成功')
            this.loadContentList()
          } else {
            this.$message.error(response.message || '下架失败')
          }
        } catch (error) {
          console.error('下架内容错误:', error)
          this.$message.error('下架失败')
        }
      }).catch(() => {})
    },

    handleRestoreContent(type, row) {
      const contentType = type === 'collect' ? '收藏项' : '笔记'
      this.$confirm(`确定要恢复该${contentType} "${row.title}" 吗？恢复后其他用户可以查看。`, '恢复确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        try {
          const response = await contentManageApi.restoreContent({
            contentType: type,
            contentId: row.id
          })
          if (response.code === 200) {
            this.$message.success('恢复成功')
            this.loadContentList()
          } else {
            this.$message.error(response.message || '恢复失败')
          }
        } catch (error) {
          console.error('恢复内容错误:', error)
          this.$message.error('恢复失败')
        }
      }).catch(() => {})
    },

    handleBatchTakeDown() {
      if (this.selectedRows.length === 0) return
      const alreadyDownCount = this.selectedRows.filter(row => row.isPublic !== 1).length
      const actionableCount = this.selectedRows.length - alreadyDownCount
      if (actionableCount === 0) {
        this.$message.warning('选中的内容均已下架，无需重复操作')
        return
      }
      const extraMsg = alreadyDownCount > 0 ? `（其中 ${alreadyDownCount} 条已下架将被跳过）` : ''
      this.$confirm(`确定要批量下架选中的 ${actionableCount} 条公开内容吗？下架后其他用户将无法查看。${extraMsg}`, '批量下架确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const contentIds = this.selectedRows.map(row => row.id)
          const response = await contentManageApi.batchTakeDown({
            contentType: this.activeTab,
            contentIds,
            reason: '批量下架'
          })
          if (response.code === 200) {
            this.$message.success('批量下架成功')
            this.selectedRows = []
            this.loadContentList()
          } else {
            this.$message.error(response.message || '批量下架失败')
          }
        } catch (error) {
          console.error('批量下架错误:', error)
          this.$message.error('批量下架失败')
        }
      }).catch(() => {})
    },

    handleBatchRestore() {
      if (this.selectedRows.length === 0) return
      const alreadyPublicCount = this.selectedRows.filter(row => row.isPublic === 1).length
      const actionableCount = this.selectedRows.length - alreadyPublicCount
      if (actionableCount === 0) {
        this.$message.warning('选中的内容均已公开，无需恢复')
        return
      }
      const extraMsg = alreadyPublicCount > 0 ? `（其中 ${alreadyPublicCount} 条已公开将被跳过）` : ''
      this.$confirm(`确定要批量恢复选中的 ${actionableCount} 条已下架内容吗？恢复后其他用户可以查看。${extraMsg}`, '批量恢复确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        try {
          const contentIds = this.selectedRows.map(row => row.id)
          const response = await contentManageApi.batchRestore({
            contentType: this.activeTab,
            contentIds
          })
          if (response.code === 200) {
            this.$message.success('批量恢复成功')
            this.selectedRows = []
            this.loadContentList()
          } else {
            this.$message.error(response.message || '批量恢复失败')
          }
        } catch (error) {
          console.error('批量恢复错误:', error)
          this.$message.error('批量恢复失败')
        }
      }).catch(() => {})
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.loadContentList()
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadContentList()
    },

    indexMethod(index) {
      return (this.pagination.currentPage - 1) * this.pagination.pageSize + index + 1
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },

    getDigestStatusText(status) {
      const map = {
        undigest: '未消化',
        digesting: '消化中',
        digested: '已消化',
        abandoned: '已放弃'
      }
      return map[status] || '未知'
    },

    getDigestStatusType(status) {
      const map = {
        undigest: 'danger',
        digesting: 'warning',
        digested: 'success',
        abandoned: 'info'
      }
      return map[status] || 'info'
    },

    getNoteTypeText(type) {
      const map = {
        original: '原创',
        summary: '总结',
        normal: '普通'
      }
      return map[type] || '未知'
    },

    getNoteTypeType(type) {
      const map = {
        original: 'success',
        summary: 'warning',
        normal: 'info'
      }
      return map[type] || 'info'
    }
  }
}
</script>

<style scoped>
.content-management {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 页面头部 ========== */
.page-header {
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-5);
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.page-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
}

/* ========== 筛选卡片 ========== */
.filter-card {
  margin-bottom: var(--space-4);
}

.filter-card :deep(.el-card__body) {
  padding: var(--space-4) var(--space-5);
}

/* ========== 表格卡片 ========== */
.table-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.table-card :deep(.el-tabs__header) {
  margin-bottom: var(--space-4);
}

.table-card :deep(.el-table th) {
  background-color: var(--bg-canvas);
  color: var(--text-primary);
  font-weight: 600;
}

.table-card :deep(.el-table tbody tr) {
  transition: background-color var(--transition-fast);
}

.table-card :deep(.el-button) {
  transition: all var(--transition-fast);
}

.table-card :deep(.el-button:hover) {
  transform: translateY(-1px);
}

/* ========== 操作按钮容器 ========== */
.action-buttons {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: flex-start;
}

.action-buttons .el-button {
  margin: 0 !important;
}

/* ========== 批量操作栏 ========== */
.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  margin-top: var(--space-4);
  background-color: var(--danger-bg, #fef0f0);
  border: 1px solid var(--danger-color, #f56c6c);
  border-radius: var(--radius-md, 4px);
}

.batch-info {
  font-size: var(--font-size-sm);
  color: var(--danger-color, #f56c6c);
  font-weight: 500;
}

.batch-actions {
  display: flex;
  gap: var(--space-2);
}

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-5);
  display: flex;
  justify-content: flex-end;
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .pagination-wrapper {
    justify-content: center;
  }
}

/* ========== 过渡增强 ========== */
.filter-card {
  transition: box-shadow var(--transition-normal);
}

.filter-card:hover {
  box-shadow: var(--shadow-sm);
}

.table-card {
  transition: box-shadow var(--transition-normal);
}
</style>
