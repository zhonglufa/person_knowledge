<template>
  <div class="role-management">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" @click="handleCreate">
        <i class="fas fa-plus"></i> 新增角色
      </el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索角色码/角色名/描述"
        clearable
        @clear="handleSearch"
        @keyup.enter.native="handleSearch"
        style="width: 300px">
        <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
      </el-input>
    </div>

    <el-table :data="roleList" v-loading="loading" border stripe>
      <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod"></el-table-column>
      <el-table-column prop="roleCode" label="角色码" width="150">
        <template slot-scope="scope">
          <el-tag type="info">{{ scope.row.roleCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
      <el-table-column prop="roleDesc" label="描述" min-width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="warning" @click="handlePermission(scope.row)">权限</el-button>
          <el-button
            size="mini"
            type="danger"
            @click="handleDelete(scope.row)"
            :disabled="['admin', 'super_admin', 'common_user'].includes(scope.row.roleCode)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper">
      </el-pagination>
    </div>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleForm" label-width="100px">
        <el-form-item label="角色码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色码"></el-input>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="roleDesc">
          <el-input v-model="roleForm.roleDesc" type="textarea" :rows="3" placeholder="请输入角色描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="分配权限" :visible.sync="permissionDialogVisible" width="600px">
      <div class="permission-tree-container">
        <el-tree
          ref="permissionTree"
          :data="permissionTree"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedPermissions"
          :props="treeProps">
        </el-tree>
      </div>
      <div slot="footer">
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissions" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { roleManageApi, permissionApi } from '@/api/admin'

export default {
  name: 'RoleManagement',
  data() {
    return {
      roleList: [],
      loading: false,
      keyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      dialogTitle: '新增角色',
      isEdit: false,
      editRoleId: null,
      roleForm: {
        roleCode: '',
        roleName: '',
        roleDesc: ''
      },
      roleRules: {
        roleCode: [
          { required: true, message: '请输入角色码', trigger: 'blur' },
          { pattern: /^[a-zA-Z_]+$/, message: '角色码只能包含字母和下划线', trigger: 'blur' }
        ],
        roleName: [
          { required: true, message: '请输入角色名称', trigger: 'blur' }
        ]
      },
      submitting: false,
      permissionDialogVisible: false,
      currentRoleId: null,
      permissionTree: [],
      checkedPermissions: [],
      treeProps: {
        children: 'children',
        label: (data) => {
          return data.permissionName || data.permissionCode
        }
      }
    }
  },
  created() {
    this.fetchRoleList()
    this.fetchPermissionTree()
  },
  methods: {
    async fetchRoleList() {
      this.loading = true
      try {
        const response = await roleManageApi.getRoleList({
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.keyword
        })
        if (response.code === 200) {
          const data = response.data
          this.roleList = data.records || data
          this.total = data.total || this.roleList.length
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
        this.$message.error('获取角色列表失败')
      } finally {
        this.loading = false
      }
    },
    async fetchPermissionTree() {
      try {
        const response = await permissionApi.getPermissionTree()
        if (response.code === 200) {
          const tree = response.data || []
          this.permissionTree = tree.map(node => ({
            ...node,
            id: node.id || `module-${node.permissionCode || node.permissionName || Math.random()}`
          }))
        }
      } catch (error) {
        console.error('获取权限树失败:', error)
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchRoleList()
    },
    handlePageChange(page) {
      this.currentPage = page
      this.fetchRoleList()
    },
    handleCreate() {
      this.dialogTitle = '新增角色'
      this.isEdit = false
      this.editRoleId = null
      this.roleForm = {
        roleCode: '',
        roleName: '',
        roleDesc: ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.roleForm && this.$refs.roleForm.clearValidate()
      })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑角色'
      this.isEdit = true
      this.editRoleId = row.id
      this.roleForm = {
        roleCode: row.roleCode,
        roleName: row.roleName,
        roleDesc: row.roleDesc || ''
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.roleForm && this.$refs.roleForm.clearValidate()
      })
    },
    async handleSubmit() {
      try {
        await this.$refs.roleForm.validate()
      } catch (error) {
        return
      }

      this.submitting = true
      try {
        let response
        if (this.isEdit) {
          response = await roleManageApi.updateRole(this.editRoleId, {
            ...this.roleForm
          })
        } else {
          response = await roleManageApi.createRole({
            ...this.roleForm
          })
        }

        if (response.code === 200) {
          this.$message.success(this.isEdit ? '角色更新成功' : '角色创建成功')
          this.dialogVisible = false
          this.fetchRoleList()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        this.$message.error('操作失败')
      } finally {
        this.submitting = false
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该角色吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const response = await roleManageApi.deleteRole(row.id)
        if (response.code === 200) {
          this.$message.success('角色删除成功')
          this.fetchRoleList()
        } else {
          this.$message.error(response.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    async handlePermission(row) {
      this.currentRoleId = row.id
      this.checkedPermissions = []

      try {
        const response = await roleManageApi.getRolePermissions(row.id)
        if (response.code === 200) {
          const permissions = response.data || []
          this.checkedPermissions = permissions.map(p => p.id)
        }
      } catch (error) {
        console.error('获取角色权限失败:', error)
      }

      this.permissionDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.permissionTree) {
          this.$refs.permissionTree.setCheckedKeys(this.checkedPermissions)
        }
      })
    },
    async handleAssignPermissions() {
      if (!this.$refs.permissionTree) return

      const checkedKeys = this.$refs.permissionTree.getCheckedKeys(false)
      const halfCheckedKeys = this.$refs.permissionTree.getHalfCheckedKeys()
      const allCheckedKeys = [...checkedKeys, ...halfCheckedKeys]

      const permissionIds = allCheckedKeys
        .map(k => String(k))
        .filter(k => /^\d+$/.test(k))
        .map(k => Number(k))

      this.submitting = true
      try {
        const response = await roleManageApi.assignPermissions(this.currentRoleId, {
          permissionIds
        })

        if (response.code === 200) {
          this.$message.success('权限分配成功')
          this.permissionDialogVisible = false
        } else {
          this.$message.error(response.message || '权限分配失败')
        }
      } catch (error) {
        console.error('权限分配失败:', error)
        this.$message.error('权限分配失败')
      } finally {
        this.submitting = false
      }
    },
    indexMethod(index) {
      return (this.currentPage - 1) * this.pageSize + index + 1
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.role-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 10px;
}
</style>
