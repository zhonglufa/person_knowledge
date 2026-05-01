<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">管理系统中的所有用户账号</p>
      </div>
      <div class="header-right">
        <el-button type="success" icon="el-icon-download" @click="handleExport">导出</el-button>
        <el-button type="primary" @click="handleAddUser">
          <i class="el-icon-plus"></i> 新增用户
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="搜索">
          <el-input
            v-model="queryParams.searchKey"
            placeholder="用户名 / 昵称 / 邮箱"
            clearable
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="queryParams.role"
            placeholder="全部角色"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="普通用户" value="commonUser"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="userList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" align="center" />
        <el-table-column prop="nickname" label="昵称" min-width="120" align="center" />
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" />
        <el-table-column label="角色" width="110" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'" size="small">
              {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" min-width="170" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.lastLoginAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="170" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-edit"
                @click="handleEditUser(scope.row)"
              >编辑</el-button>
              <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                size="mini"
                :icon="scope.row.status === 1 ? 'el-icon-remove' : 'el-icon-circle-check'"
                @click="handleToggleStatus(scope.row)"
              >{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作栏 -->
      <div v-if="selectedUsers.length > 0" class="batch-bar">
        <span class="batch-info">已选择 {{ selectedUsers.length }} 个用户</span>
        <div class="batch-actions">
          <el-button type="danger" size="mini" icon="el-icon-remove" @click="handleBatchDisable">批量禁用</el-button>
          <el-button type="success" size="mini" icon="el-icon-circle-check" @click="handleBatchEnable">批量启用</el-button>
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

    <!-- 新增 / 编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="520px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
        label-position="right"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入用户名"
            :disabled="!isAdd"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item :label="isAdd ? '密码' : '新密码'" :prop="isAdd ? 'password' : ''">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            :placeholder="isAdd ? '请输入密码（8-20位，含字母和数字）' : '不修改请留空'"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="普通用户" value="commonUser"></el-option>
            <el-option label="管理员" value="admin"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { userManageApi } from '@/api/admin'

export default {
  name: 'UserManagement',

  data() {
    return {
      userList: [],
      selectedUsers: [],
      loading: false,
      submitLoading: false,

      // 查询参数
      queryParams: {
        searchKey: '',
        role: ''
      },

      // 分页
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },

      // 对话框
      dialogVisible: false,
      dialogTitle: '',
      isAdd: true,

      // 表单
      userForm: {
        id: '',
        username: '',
        nickname: '',
        email: '',
        password: '',
        role: 'commonUser'
      },

      // 验证规则
      userRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 8, max: 20, message: '密码长度8-20位，需含字母和数字', trigger: 'blur' }
        ],
        role: [
          { required: true, message: '请选择角色', trigger: 'change' }
        ]
      }
    }
  },

  mounted() {
    this.loadUserList()
  },

  methods: {
    async loadUserList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.currentPage,
          pageSize: this.pagination.pageSize,
          keyword: this.queryParams.searchKey,
          role: this.queryParams.role || undefined
        }
        const response = await userManageApi.getUserList(params)
        if (response.code === 200) {
          // MyBatis Plus Page 对象返回的字段是 records 而不是 list
          this.userList = response.data?.records || []
          this.pagination.total = response.data?.total || 0
        } else {
          this.$message.error(response.message || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表错误:', error)
        this.$message.error('获取用户列表失败')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.pagination.currentPage = 1
      this.loadUserList()
    },

    handleReset() {
      this.queryParams.searchKey = ''
      this.queryParams.role = ''
      this.pagination.currentPage = 1
      this.loadUserList()
    },

    handleSelectionChange(selection) {
      this.selectedUsers = selection
    },

    handleAddUser() {
      this.isAdd = true
      this.dialogTitle = '新增用户'
      this.userForm = { id: '', username: '', nickname: '', email: '', password: '', role: 'commonUser' }
      this.dialogVisible = true
    },

    handleEditUser(row) {
      this.isAdd = false
      this.dialogTitle = '编辑用户'
      this.userForm = {
        id: row.id,
        username: row.username,
        nickname: row.nickname,
        email: row.email,
        password: '',
        role: row.role || 'commonUser'
      }
      this.dialogVisible = true
    },

    handleToggleStatus(row) {
      const action = row.status === 1 ? '禁用' : '启用'
      this.$confirm(`确定要${action}用户 "${row.username}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          let response
          if (row.status === 1) {
            response = await userManageApi.disableUser(row.id)
          } else {
            response = await userManageApi.enableUser(row.id)
          }
          if (response.code === 200) {
            this.$message.success(`${action}成功`)
            this.loadUserList()
          } else {
            this.$message.error(response.message || `${action}失败`)
          }
        } catch (error) {
          console.error(`${action}用户错误:`, error)
          this.$message.error(`${action}失败`)
        }
      }).catch(() => {})
    },

    handleSubmit() {
      this.$refs.userFormRef.validate(async (valid) => {
        if (!valid) return

        this.submitLoading = true
        try {
          let response
          if (this.isAdd) {
            const { username, email, password, role, nickname } = this.userForm
            response = await userManageApi.addUser({ username, email, password, role, nickname })
          } else {
            // 后端 AdminEditUserDTO 仅允许修改 nickname 和 role
            response = await userManageApi.editUser(this.userForm.id, {
              nickname: this.userForm.nickname,
              role: this.userForm.role
            })
          }

          if (response.code === 200) {
            this.$message.success(this.isAdd ? '新增成功' : '编辑成功')
            this.dialogVisible = false
            this.loadUserList()
          } else {
            this.$message.error(response.message || (this.isAdd ? '新增失败' : '编辑失败'))
          }
        } catch (error) {
          console.error('提交表单错误:', error)
          this.$message.error(this.isAdd ? '新增失败' : '编辑失败')
        } finally {
          this.submitLoading = false
        }
      })
    },

    handleDialogClose() {
      this.$nextTick(() => {
        this.$refs.userFormRef && this.$refs.userFormRef.resetFields()
      })
    },

    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.loadUserList()
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadUserList()
    },

    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleString('zh-CN')
    },

    /**
     * 导出用户列表
     */
    async handleExport() {
      try {
        const params = {
          keyword: this.queryParams.searchKey || undefined,
          role: this.queryParams.role || undefined
        }
        const response = await userManageApi.exportUsers(params)
        // 创建下载链接
        const blob = new Blob([response], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `用户列表_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '')}.csv`
        link.click()
        URL.revokeObjectURL(link.href)
        this.$message.success('导出成功')
      } catch (error) {
        console.error('导出用户列表错误:', error)
        this.$message.error('导出失败')
      }
    },

    /**
     * 批量禁用用户
     */
    handleBatchDisable() {
      if (this.selectedUsers.length === 0) return
      this.$confirm(`确定要批量禁用选中的 ${this.selectedUsers.length} 个用户吗？`, '批量禁用确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const userIds = this.selectedUsers.map(u => u.id)
          const response = await userManageApi.batchDisable({ userIds, reason: '批量禁用' })
          if (response.code === 200) {
            this.$message.success('批量禁用成功')
            this.loadUserList()
          } else {
            this.$message.error(response.message || '批量禁用失败')
          }
        } catch (error) {
          console.error('批量禁用错误:', error)
          this.$message.error('批量禁用失败')
        }
      }).catch(() => {})
    },

    /**
     * 批量启用用户
     */
    handleBatchEnable() {
      if (this.selectedUsers.length === 0) return
      this.$confirm(`确定要批量启用选中的 ${this.selectedUsers.length} 个用户吗？`, '批量启用确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }).then(async () => {
        try {
          const userIds = this.selectedUsers.map(u => u.id)
          const response = await userManageApi.batchEnable({ userIds })
          if (response.code === 200) {
            this.$message.success('批量启用成功')
            this.loadUserList()
          } else {
            this.$message.error(response.message || '批量启用失败')
          }
        } catch (error) {
          console.error('批量启用错误:', error)
          this.$message.error('批量启用失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.user-management {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 页面头部 ========== */
.page-header {
  padding: var(--space-4) var(--space-5);
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form :deep(.el-input__inner) {
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

/* ========== 表格卡片 ========== */
.table-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.table-card :deep(.el-table) {
  font-size: var(--font-size-sm);
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

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-5);
  display: flex;
  justify-content: flex-end;
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

/* ========== 对话框 ========== */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .filter-form :deep(.el-form-item) {
    margin-bottom: var(--space-3);
  }

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
