<template>
  <div class="permission-management">
    <div class="page-header">
      <h2>权限管理</h2>
      <div class="header-actions">
        <el-select v-model="filterModule" placeholder="按模块筛选" clearable @change="handleFilter">
          <el-option
            v-for="module in modules"
            :key="module.value"
            :label="module.label"
            :value="module.value">
          </el-option>
        </el-select>
      </div>
    </div>

    <div class="permission-stats">
      <el-card class="stat-card">
        <div class="stat-value">{{ totalPermissions }}</div>
        <div class="stat-label">权限总数</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ totalModules }}</div>
        <div class="stat-label">模块数量</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ pagePermissions }}</div>
        <div class="stat-label">页面权限</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ buttonPermissions }}</div>
        <div class="stat-label">按钮权限</div>
      </el-card>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="列表视图" name="list">
        <el-table :data="filteredPermissions" v-loading="loading" border stripe>
          <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
          <el-table-column prop="permissionCode" label="权限码" width="200">
            <template slot-scope="scope">
              <el-tag type="info" size="small">{{ scope.row.permissionCode }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="permissionName" label="权限名称" width="150"></el-table-column>
          <el-table-column prop="permissionType" label="类型" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.permissionType === 'page' ? 'primary' : 'success'" size="small">
                {{ scope.row.permissionType === 'page' ? '页面' : '按钮' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="module" label="模块" width="120">
            <template slot-scope="scope">
              <el-tag type="warning" size="small">{{ getModuleName(scope.row.module) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="path" label="路径" min-width="200"></el-table-column>
          <el-table-column prop="method" label="方法" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getMethodType(scope.row.method)" size="small">
                {{ scope.row.method || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sortNo" label="排序" width="80" align="center"></el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="树形视图" name="tree">
        <div class="tree-container">
          <el-tree
            :data="permissionTree"
            node-key="id"
            default-expand-all
            :props="treeProps">
            <template slot-scope="{ node, data }">
              <div class="tree-node">
                <span class="node-label">
                  <i :class="getNodeIcon(data)"></i>
                  {{ data.permissionName || data.permissionCode }}
                </span>
                <span class="node-tag" v-if="data.permissionCode">
                  <el-tag size="mini" type="info">{{ data.permissionCode }}</el-tag>
                </span>
              </div>
            </template>
          </el-tree>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { permissionApi } from '@/api/admin'

export default {
  name: 'PermissionManagement',
  data() {
    return {
      loading: false,
      permissions: [],
      permissionTree: [],
      activeTab: 'list',
      filterModule: '',
      modules: [
        { value: 'admin', label: '后台管理' },
        { value: 'user', label: '用户管理' },
        { value: 'content', label: '内容管理' },
        { value: 'announcement', label: '公告管理' },
        { value: 'taxonomy', label: '分类标签' }
      ],
      treeProps: {
        children: 'children',
        label: 'permissionName'
      }
    }
  },
  computed: {
    filteredPermissions() {
      if (!this.filterModule) {
        return this.permissions
      }
      return this.permissions.filter(p => p.module === this.filterModule)
    },
    totalPermissions() {
      return this.permissions.length
    },
    totalModules() {
      const modules = new Set(this.permissions.map(p => p.module))
      return modules.size
    },
    pagePermissions() {
      return this.permissions.filter(p => p.permissionType === 'page').length
    },
    buttonPermissions() {
      return this.permissions.filter(p => p.permissionType === 'button').length
    }
  },
  created() {
    this.fetchPermissions()
    this.fetchPermissionTree()
  },
  methods: {
    async fetchPermissions() {
      this.loading = true
      try {
        const response = await permissionApi.getPermissionList()
        if (response.code === 200) {
          this.permissions = response.data || []
        }
      } catch (error) {
        console.error('获取权限列表失败:', error)
        this.$message.error('获取权限列表失败')
      } finally {
        this.loading = false
      }
    },
    async fetchPermissionTree() {
      try {
        const response = await permissionApi.getPermissionTree()
        if (response.code === 200) {
          this.permissionTree = response.data || []
        }
      } catch (error) {
        console.error('获取权限树失败:', error)
      }
    },
    handleFilter() {
      this.$forceUpdate()
    },
    handleTabClick() {
      if (this.activeTab === 'tree' && this.permissionTree.length === 0) {
        this.fetchPermissionTree()
      }
    },
    getModuleName(module) {
      const moduleNames = {
        admin: '后台管理',
        user: '用户管理',
        content: '内容管理',
        announcement: '公告管理',
        taxonomy: '分类标签'
      }
      return moduleNames[module] || module
    },
    getMethodType(method) {
      const types = {
        GET: 'success',
        POST: 'primary',
        PUT: 'warning',
        DELETE: 'danger'
      }
      return types[method] || 'info'
    },
    getNodeIcon(data) {
      if (data.permissionType === 'module') {
        return 'el-icon-folder'
      }
      if (data.permissionType === 'page') {
        return 'el-icon-document'
      }
      return 'el-icon-key'
    }
  }
}
</script>

<style scoped>
.permission-management {
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

.permission-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--primary-color);
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 5px;
}

.tree-container {
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 20px;
  max-height: 600px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-right: 20px;
}

.node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-label i {
  color: var(--primary-color);
}
</style>
