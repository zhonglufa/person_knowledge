<template>
  <div class="content-audit-logs">
    <div class="page-header">
      <h2 class="page-title"><i class="el-icon-s-order"></i> 内容操作日志</h2>
      <p class="page-desc">查看管理员对内容的操作记录，包括下架、恢复等操作</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="操作类型">
          <el-select v-model="filterForm.operationType" placeholder="全部" clearable @change="handleFilterChange">
            <el-option label="下架" value="take_down" />
            <el-option label="恢复" value="restore" />
            <el-option label="禁用用户" value="disable_user" />
            <el-option label="解封用户" value="enable_user" />
            <el-option label="发布公告" value="publish_announcement" />
            <el-option label="创建公告" value="create_announcement" />
            <el-option label="更新公告" value="update_announcement" />
            <el-option label="删除公告" value="delete_announcement" />
            <el-option label="批量禁用用户" value="batch_disable_user" />
            <el-option label="批量启用用户" value="batch_enable_user" />
            <el-option label="批量下架" value="batch_take_down" />
            <el-option label="批量恢复" value="batch_restore" />
            <el-option label="定时发布" value="schedule_announcement" />
          </el-select>
        </el-form-item>

        <el-form-item label="目标类型">
          <el-select v-model="filterForm.targetType" placeholder="全部" clearable @change="handleFilterChange">
            <el-option label="收藏集" value="collection" />
            <el-option label="笔记" value="note" />
            <el-option label="用户" value="user" />
            <el-option label="公告" value="announcement" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd HH:mm:ss"
            @change="handleFilterChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleFilterChange">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover">
      <el-table
        :data="logList"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column label="操作类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="getOperationTypeTag(scope.row.operationType)" size="small">
              {{ getOperationTypeName(scope.row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="100">
          <template slot-scope="scope">
            {{ getTargetTypeName(scope.row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationDetail" label="操作详情" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { contentManageApi } from '@/api/admin'

export default {
  name: 'ContentAuditLogs',

  data() {
    return {
      filterForm: {
        operationType: '',
        targetType: '',
        dateRange: []
      },
      logList: [],
      loading: false,
      pagination: {
        pageNum: 1,
        pageSize: 10,
        total: 0
      }
    }
  },

  created() {
    this.fetchLogs()
  },

  methods: {
    async fetchLogs() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          operationType: this.filterForm.operationType || undefined,
          targetType: this.filterForm.targetType || undefined
        }

        if (this.filterForm.dateRange && this.filterForm.dateRange.length === 2) {
          params.startTime = this.filterForm.dateRange[0]
          params.endTime = this.filterForm.dateRange[1]
        }

        const response = await contentManageApi.getAuditLogs(params)
        if (response.code === 200) {
          this.logList = response.data.records || []
          this.pagination.total = response.data.total || 0
        }
      } catch (error) {
        console.error('获取操作日志错误:', error)
        this.$message.error('获取操作日志失败')
      } finally {
        this.loading = false
      }
    },

    handleFilterChange() {
      this.pagination.pageNum = 1
      this.fetchLogs()
    },

    handleReset() {
      this.filterForm = {
        operationType: '',
        targetType: '',
        dateRange: []
      }
      this.pagination.pageNum = 1
      this.fetchLogs()
    },

    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.pageNum = 1
      this.fetchLogs()
    },

    handleCurrentChange(page) {
      this.pagination.pageNum = page
      this.fetchLogs()
    },

    getOperationTypeTag(type) {
      const map = {
        'take_down': 'danger',
        'restore': 'success',
        'disable_user': 'warning',
        'enable_user': 'primary',
        'publish_announcement': 'info',
        'create_announcement': '',
        'update_announcement': '',
        'delete_announcement': 'danger',
        'batch_disable_user': 'warning',
        'batch_enable_user': 'success',
        'batch_take_down': 'danger',
        'batch_restore': 'success',
        'schedule_announcement': 'info'
      }
      return map[type] || ''
    },

    getOperationTypeName(type) {
      const map = {
        'take_down': '下架',
        'restore': '恢复',
        'disable_user': '禁用用户',
        'enable_user': '解封用户',
        'publish_announcement': '发布公告',
        'create_announcement': '创建公告',
        'update_announcement': '更新公告',
        'delete_announcement': '删除公告',
        'batch_disable_user': '批量禁用用户',
        'batch_enable_user': '批量启用用户',
        'batch_take_down': '批量下架',
        'batch_restore': '批量恢复',
        'schedule_announcement': '定时发布'
      }
      return map[type] || type
    },

    getTargetTypeName(type) {
      const map = {
        'collection': '收藏集',
        'note': '笔记',
        'user': '用户',
        'announcement': '公告'
      }
      return map[type] || type
    }
  }
}
</script>

<style scoped>
.content-audit-logs {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.page-title i {
  color: var(--primary-color);
}

.page-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}

.filter-card {
  margin-bottom: var(--space-4);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  margin-bottom: var(--space-4);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}
</style>
