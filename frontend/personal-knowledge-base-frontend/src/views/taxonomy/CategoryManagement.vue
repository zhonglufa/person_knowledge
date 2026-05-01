<template>
  <div class="category-management">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">分类管理</h2>
        <p class="page-desc">创建和管理知识分类树结构</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">
          新建分类
        </el-button>
        <el-button icon="el-icon-refresh" @click="refreshCategories">
          刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-primary">
              <i class="fas fa-folder-tree"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalCategories || 0 }}</div>
              <div class="stat-label">总分类数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-secondary">
              <i class="fas fa-folder"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.rootCategories || 0 }}</div>
              <div class="stat-label">根分类</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-warm">
              <i class="fas fa-leaf"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.leafCategories || 0 }}</div>
              <div class="stat-label">叶子分类</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="tree-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="fas fa-sitemap"></i> 分类树</span>
        <div class="card-tools">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类..."
            prefix-icon="el-icon-search"
            clearable
            class="search-input"
          />
          <el-button size="small" @click="toggleExpandAll">
            <i class="fas" :class="allExpanded ? 'fa-compress-alt' : 'fa-expand-alt'"></i>
            {{ allExpanded ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else-if="categoryTree.length === 0" class="empty-state">
        <i class="fas fa-folder-tree empty-icon"></i>
        <p class="empty-text">暂无分类数据</p>
        <p class="empty-desc">创建您的第一个分类来组织知识内容</p>
        <el-button type="primary" @click="showCreateDialog = true">
          <i class="fas fa-plus"></i> 创建第一个分类
        </el-button>
      </div>

      <div v-else class="tree-wrapper">
        <div class="tree-node" v-for="node in visibleNodes" :key="node.id" :style="{ paddingLeft: node.level * 24 + 'px' }">
          <div class="node-toggle" @click="toggleNode(node)" v-if="node.children?.length > 0">
            <i :class="isNodeExpanded(node.id) ? 'fas fa-chevron-down' : 'fas fa-chevron-right'"></i>
          </div>
          <div class="node-toggle-placeholder" v-else></div>

          <div class="node-body">
            <div class="node-icon" :style="{ color: node.color || 'var(--primary-color)' }">
              <i :class="node.icon || 'fas fa-folder'"></i>
            </div>
            <div class="node-info">
              <div class="node-title">
                <span>{{ node.name }}</span>
                <el-tag size="mini" :type="node.level === 0 ? 'primary' : 'info'">
                  {{ node.level === 0 ? '根分类' : '子分类' }}
                </el-tag>
              </div>
              <p class="node-desc" v-if="node.description">{{ node.description }}</p>
              <div class="node-stats">
                <span><i class="fas fa-file"></i> {{ node.contentCount || 0 }} 项内容</span>
                <span v-if="node.children?.length > 0"><i class="fas fa-folder"></i> {{ node.children.length }} 个子分类</span>
              </div>
            </div>
          </div>

          <div class="node-actions" @click.stop>
            <el-button size="mini" type="primary" @click="addChildCategory(node)" v-if="node.level < 3">
              <i class="fas fa-plus"></i> 子分类
            </el-button>
            <el-button size="mini" @click="editCategory(node)">
              <i class="fas fa-edit"></i>
            </el-button>
            <el-popconfirm title="确定要删除此分类吗？" @confirm="deleteCategory(node)">
              <el-button size="mini" type="danger" slot="reference">
                <i class="fas fa-trash"></i>
              </el-button>
            </el-popconfirm>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      :title="isEditing ? '编辑分类' : (isAddingChild ? '添加子分类' : '新建分类')"
      :visible.sync="showCreateDialog"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="categoryForm" :rules="formRules" ref="categoryForm" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="父分类" v-if="!isEditing && !isAddingChild">
          <el-select v-model="categoryForm.parentId" placeholder="选择父分类（可选）" clearable style="width: 100%">
            <el-option v-for="cat in rootCategories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select v-model="categoryForm.icon" placeholder="选择图标" style="width: 100%">
            <el-option v-for="icon in availableIcons" :key="icon.value" :label="icon.label" :value="icon.value">
              <i :class="icon.value"></i> <span style="margin-left: 8px;">{{ icon.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="categoryForm.color" :predefine="predefineColors" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCategory" :loading="submitLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/category'

export default {
  name: 'CategoryManagement',
  data() {
    return {
      loading: false,
      submitLoading: false,
      searchKeyword: '',
      categoryTree: [],
      stats: { totalCategories: 0, rootCategories: 0, leafCategories: 0 },
      expandedKeys: new Set(),
      allExpanded: false,
      showCreateDialog: false,
      isEditing: false,
      isAddingChild: false,
      categoryForm: {
        id: null, name: '', description: '', parentId: null,
        icon: 'fas fa-folder', color: '#4A6CF7'
      },
      availableIcons: [
        { value: 'fas fa-folder', label: '文件夹' },
        { value: 'fas fa-book', label: '书籍' },
        { value: 'fas fa-code', label: '代码' },
        { value: 'fas fa-laptop', label: '电脑' },
        { value: 'fas fa-paint-brush', label: '画笔' },
        { value: 'fas fa-music', label: '音乐' }
      ],
      predefineColors: ['#4A6CF7', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
      formRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    rootCategories() {
      return this.categoryTree.filter(c => !c.parentId)
    },
    visibleNodes() {
      const result = []
      const traverse = (nodes, level = 0) => {
        nodes.forEach(node => {
          const nodeData = { ...node, level }
          result.push(nodeData)
          if (this.isNodeExpanded(node.id) && node.children?.length > 0) {
            traverse(node.children, level + 1)
          }
        })
      }
      traverse(this.categoryTree)
      if (this.searchKeyword) {
        return result.filter(n => n.name?.toLowerCase().includes(this.searchKeyword.toLowerCase()))
      }
      return result
    }
  },
  methods: {
    async loadCategories() {
      this.loading = true
      try {
        const treeResponse = await getCategoryTree()
        if (treeResponse?.code === 200) {
          this.categoryTree = treeResponse.data?.tree || []
        } else {
          this.categoryTree = []
        }
        this.calculateStats()
      } catch (error) {
        console.error('加载分类失败:', error)
        this.$message.error('加载分类失败')
        this.categoryTree = this.getMockCategories()
        this.calculateStats()
      } finally {
        this.loading = false
      }
    },
    calculateStats() {
      const flat = this.flattenTree(this.categoryTree)
      this.stats = {
        totalCategories: flat.length,
        rootCategories: this.categoryTree.length,
        leafCategories: flat.filter(c => !c.children || c.children.length === 0).length
      }
    },
    flattenTree(nodes) {
      const result = []
      const traverse = (items) => {
        items.forEach(item => {
          result.push(item)
          if (item.children) traverse(item.children)
        })
      }
      traverse(nodes)
      return result
    },
    async refreshCategories() {
      await this.loadCategories()
      this.$message.success('刷新成功')
    },
    toggleExpandAll() {
      if (this.allExpanded) {
        this.expandedKeys.clear()
      } else {
        this.flattenTree(this.categoryTree).forEach(n => this.expandedKeys.add(n.id))
      }
      this.allExpanded = !this.allExpanded
    },
    toggleNode(node) {
      if (this.isNodeExpanded(node.id)) {
        this.expandedKeys.delete(node.id)
      } else {
        this.expandedKeys.add(node.id)
      }
    },
    isNodeExpanded(id) {
      return this.expandedKeys.has(id)
    },
    addChildCategory(parent) {
      this.isEditing = false
      this.isAddingChild = true
      this.categoryForm = { id: null, name: '', description: '', parentId: parent.id, icon: 'fas fa-folder', color: '#4A6CF7' }
      this.showCreateDialog = true
    },
    editCategory(node) {
      this.isEditing = true
      this.isAddingChild = false
      this.categoryForm = {
        id: node.id, name: node.name, description: node.description || '',
        parentId: node.parentId, icon: node.icon || 'fas fa-folder', color: node.color || '#4A6CF7'
      }
      this.showCreateDialog = true
    },
    async deleteCategory(node) {
      try {
        await deleteCategory(node.id)
        this.$message.success('删除成功')
        await this.loadCategories()
      } catch (error) {
        console.error('删除失败:', error)
        this.$message.error('删除失败')
      }
    },
    async submitCategory() {
      try {
        await this.$refs.categoryForm.validate()
        this.submitLoading = true
        if (this.isEditing) {
          await updateCategory(this.categoryForm.id, this.categoryForm)
          this.$message.success('更新成功')
        } else {
          await createCategory(this.categoryForm)
          this.$message.success('创建成功')
        }
        this.showCreateDialog = false
        await this.loadCategories()
      } catch (error) {
        if (!error.errors) {
          console.error('操作失败:', error)
          this.$message.error('操作失败')
        }
      } finally {
        this.submitLoading = false
      }
    },
    resetForm() {
      this.isEditing = false
      this.isAddingChild = false
      this.categoryForm = { id: null, name: '', description: '', parentId: null, icon: 'fas fa-folder', color: '#4A6CF7' }
      this.$refs.categoryForm?.resetFields()
    },
    getMockCategories() {
      return [
        { id: 1, name: '前端开发', description: '前端技术相关分类', icon: 'fas fa-code', color: '#4A6CF7', contentCount: 45, children: [
          { id: 11, name: 'Vue.js', description: 'Vue框架学习', icon: 'fas fa-laptop', color: '#67C23A', contentCount: 20, parentId: 1 },
          { id: 12, name: 'React', description: 'React框架学习', icon: 'fas fa-laptop', color: '#61dafb', contentCount: 15, parentId: 1 }
        ]},
        { id: 2, name: '后端开发', description: '后端技术相关分类', icon: 'fas fa-server', color: '#E6A23C', contentCount: 32, children: [] },
        { id: 3, name: '学习笔记', description: '个人学习笔记', icon: 'fas fa-book', color: '#F56C6C', contentCount: 67, children: [] }
      ]
    }
  },
  mounted() {
    this.loadCategories()
    this.categoryTree.forEach(n => this.expandedKeys.add(n.id))
  }
}
</script>

<style scoped>
.category-management { padding: var(--space-4); }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.header-left { flex: 1; }
.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
}
.page-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}
.header-right { display: flex; gap: var(--space-3); }
.stats-row { margin-bottom: var(--space-6); }
.stat-card :deep(.el-card__body) { padding: var(--space-4); }
.stat-content {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  color: white;
  flex-shrink: 0;
}
.stat-info { flex: 1; }
.stat-number {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}
.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}
.tree-card { border-radius: var(--radius-lg); }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.card-tools { display: flex; align-items: center; }
.tree-wrapper { min-height: 300px; }
.tree-node {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  border: 1px solid transparent;
}
.tree-node:hover {
  background: var(--bg-canvas);
  border-color: var(--border-base);
}
.node-toggle, .node-toggle-placeholder {
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.node-toggle {
  cursor: pointer;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
}
.node-toggle:hover {
  background: var(--primary-bg);
  color: var(--primary-color);
}
.node-body {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}
.node-icon {
  font-size: var(--font-size-lg);
  width: 32px;
  text-align: center;
  flex-shrink: 0;
}
.node-info { flex: 1; min-width: 0; }
.node-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}
.node-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-1);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.node-stats {
  display: flex;
  gap: var(--space-3);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}
.node-stats span {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.node-actions {
  display: flex;
  gap: var(--space-2);
  opacity: 0;
  transition: opacity var(--transition-normal);
}
.tree-node:hover .node-actions { opacity: 1; }
.loading-wrapper { padding: var(--space-8) var(--space-4); }
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--space-12) var(--space-6);
}
.empty-icon {
  font-size: 64px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-4);
}
.empty-text {
  font-size: var(--font-size-lg);
  color: var(--text-regular);
  margin-bottom: var(--space-2);
}
.empty-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}
.search-input { width: 180px; margin-right: var(--space-3); }
.gradient-primary { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.gradient-secondary { background: linear-gradient(135deg, #10b981, #06b6d4); }
.gradient-warm { background: linear-gradient(135deg, #f59e0b, #ef4444); }
@media (max-width: 768px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: var(--space-3); }
  .node-actions { opacity: 1; }
  .node-stats { flex-wrap: wrap; }
}
</style>
