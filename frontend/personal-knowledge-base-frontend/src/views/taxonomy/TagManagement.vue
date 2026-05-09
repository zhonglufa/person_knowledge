<template>
  <div class="tag-management">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">标签管理</h2>
        <p class="page-desc">创建和管理内容标签，方便内容分类和检索</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="el-icon-plus" @click="showCreateDialog = true">
          创建标签
        </el-button>
        <el-button icon="fas fa-object-group" @click="showTagCloud = !showTagCloud">
          {{ showTagCloud ? '隐藏标签云' : '显示标签云' }}
        </el-button>
        <el-button icon="fas fa-code-branch" @click="showMergeDialog = true">
          合并标签
        </el-button>
        <el-button icon="el-icon-refresh" @click="refreshTags">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="showTagCloud" class="tag-cloud-section card">
      <div class="section-header">
        <h3 class="section-title">
          <i class="fas fa-cloud"></i> 标签云
        </h3>
        <p class="section-desc">字体大小表示使用频率，点击标签可筛选内容</p>
      </div>
      <div class="tag-cloud-container">
        <span
          v-for="tag in tagCloudData"
          :key="tag.id"
          class="tag-cloud-item"
          :style="{
            fontSize: getTagFontSize(tag.usageCount),
            color: tag.color || 'var(--primary-color)',
            transform: `rotate(${getTagRotation(tag.id)}deg)`
          }"
          @click="handleTagClick(tag)"
        >
          {{ tag.name }}
          <span class="tag-count">({{ tag.usageCount }})</span>
        </span>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-primary">
              <i class="fas fa-tags"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalTags || 0 }}</div>
              <div class="stat-label">总标签数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-warm">
              <i class="fas fa-fire"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.popularTags || 0 }}</div>
              <div class="stat-label">热门标签</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon gradient-secondary">
              <i class="fas fa-palette"></i>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.uniqueColors || 0 }}</div>
              <div class="stat-label">独特颜色</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索标签名称..."
        prefix-icon="el-icon-search"
        clearable
        class="search-input"
      />
      <el-select v-model="sortBy" placeholder="排序" size="small" class="sort-select">
        <el-option label="名称" value="name" />
        <el-option label="使用次数" value="usageCount" />
        <el-option label="创建时间" value="createdAt" />
      </el-select>
      <el-select v-model="sortOrder" placeholder="顺序" size="small" class="sort-select-sm">
        <el-option label="升序" value="asc" />
        <el-option label="降序" value="desc" />
      </el-select>
    </div>

    <div class="tags-content" v-loading="loading">
      <div v-if="!loading && filteredTags.length === 0" class="empty-state">
        <i class="fas fa-tags empty-icon"></i>
        <p class="empty-text">暂无标签</p>
        <p class="empty-desc">创建您的第一个标签来组织内容</p>
        <el-button type="primary" @click="showCreateDialog = true">
          <i class="fas fa-plus"></i> 创建标签
        </el-button>
      </div>

      <div v-else class="tags-grid">
        <div v-for="tag in filteredTags" :key="tag.id" class="tag-card">
          <div class="tag-color-bar" :style="{ backgroundColor: tag.color }"></div>
          <div class="tag-body">
            <div class="tag-header">
              <h3 class="tag-name" @click="editTag(tag)">{{ tag.name }}</h3>
              <div class="tag-actions" @click.stop>
                <el-dropdown @command="handleTagCommand">
                  <el-button type="text" size="small">
                    <i class="fas fa-ellipsis-v"></i>
                  </el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item :command="{ action: 'view', data: tag }">
                      <i class="fas fa-eye"></i> 查看关联内容
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'edit', data: tag }">
                      <i class="fas fa-edit"></i> 编辑
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', data: tag }" divided class="text-danger">
                      <i class="fas fa-trash"></i> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
            </div>
            <div class="tag-stats">
              <span><i class="fas fa-hashtag"></i> {{ tag.usageCount || 0 }} 次使用</span>
            </div>
            <div class="tag-related-quick" @click="viewTagRelated(tag)">
              <i class="fas fa-link"></i>
              <span>查看关联内容</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      :title="'标签 &quot;' + (selectedTag?.name || '') + '&quot; 的关联内容'"
      :visible.sync="showRelatedDialog"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="relatedLoading" class="related-content">
        <el-tabs v-model="relatedActiveTab">
          <el-tab-pane label="关联笔记" name="notes">
            <div v-if="relatedNotes.length === 0" class="empty-related">
              <i class="fas fa-file-alt"></i>
              <p>暂无关联笔记</p>
            </div>
            <div v-else class="related-list">
              <div v-for="note in relatedNotes" :key="note.id" class="related-item" @click="viewNote(note)">
                <div class="related-item-icon">
                  <i class="fas fa-file-alt"></i>
                </div>
                <div class="related-item-content">
                  <h4>{{ note.title }}</h4>
                  <p>{{ getNoteExcerpt(note) }}</p>
                  <div class="related-item-meta">
                    <span><i class="fas fa-calendar"></i> {{ formatDate(note.updateTime) }}</span>
                    <span><i class="fas fa-font"></i> {{ note.wordCount || 0 }} 字</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="关联收藏" name="collections">
            <div v-if="relatedCollections.length === 0" class="empty-related">
              <i class="fas fa-bookmark"></i>
              <p>暂无关联收藏</p>
            </div>
            <div v-else class="related-list">
              <div v-for="collection in relatedCollections" :key="collection.id" class="related-item" @click="viewCollection(collection)">
                <div class="related-item-icon">
                  <i class="fas fa-bookmark"></i>
                </div>
                <div class="related-item-content">
                  <h4>{{ collection.title }}</h4>
                  <p>{{ collection.description || '暂无描述' }}</p>
                  <div class="related-item-meta">
                    <span><i class="fas fa-calendar"></i> {{ formatDate(collection.createTime) }}</span>
                    <el-tag size="mini" :type="getCollectionStatusType(collection.status)">
                      {{ getCollectionStatusLabel(collection.status) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <el-dialog :title="isEditing ? '编辑标签' : '创建标签'" :visible.sync="showCreateDialog" width="450px" @close="resetForm">
      <el-form :model="tagForm" :rules="formRules" ref="tagForm" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <div class="color-picker-wrapper">
            <el-color-picker v-model="tagForm.color" :predefine="predefineColors" />
            <span class="color-value">{{ tagForm.color }}</span>
          </div>
        </el-form-item>
        <el-form-item label="预览">
          <div class="preview-wrapper">
            <el-tag :style="{ backgroundColor: tagForm.color, borderColor: tagForm.color, color: getContrastColor(tagForm.color) }">
              {{ tagForm.name || '标签预览' }}
            </el-tag>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTag" :loading="submitLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </span>
    </el-dialog>

    <el-dialog title="合并标签" :visible.sync="showMergeDialog" width="600px">
      <el-alert title="合并说明" type="info" :closable="false" show-icon style="margin-bottom: 20px">
        <p>选择多个源标签和一个目标标签，合并后源标签的所有关联内容将转移到目标标签，源标签将被删除。</p>
      </el-alert>
      <el-form label-width="100px">
        <el-form-item label="源标签（多选）">
          <el-select v-model="mergeForm.sourceTagIds" multiple filterable placeholder="选择要合并的标签" style="width: 100%">
            <el-option v-for="tag in tags" :key="tag.id" :label="`${tag.name} (${tag.usageCount || 0}次)`" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标标签">
          <el-select v-model="mergeForm.targetTagId" placeholder="选择目标标签" style="width: 100%">
            <el-option v-for="tag in tags" :key="tag.id" :label="`${tag.name} (${tag.usageCount || 0}次)`" :value="tag.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showMergeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleMergeTags" :loading="submitLoading">确认合并</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getTags, createTag, updateTag, deleteTag, getTagStatistics, mergeTags, getTagCloud } from '@/api/tag'
import { searchAll } from '@/api/search'

export default {
  name: 'TagManagement',
  data() {
    return {
      loading: false,
      submitLoading: false,
      searchKeyword: '',
      sortBy: 'name',
      sortOrder: 'asc',
      tags: [],
      stats: { totalTags: 0, popularTags: 0, uniqueColors: 0 },
      showCreateDialog: false,
      showTagCloud: false,
      showMergeDialog: false,
      tagCloudData: [],
      mergeForm: {
        sourceTagIds: [],
        targetTagId: null
      },
      isEditing: false,
      tagForm: { name: '', color: '#409EFF' },
      predefineColors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8B5CF6', '#EC4899'],
      formRules: {
        name: [
          { required: true, message: '请输入标签名称', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ],
        color: [{ required: true, message: '请选择标签颜色', trigger: 'change' }]
      },
      showRelatedDialog: false,
      selectedTag: null,
      relatedLoading: false,
      relatedActiveTab: 'notes',
      relatedNotes: [],
      relatedCollections: []
    }
  },
  computed: {
    filteredTags() {
      let result = [...this.tags]
      if (this.searchKeyword) {
        const kw = this.searchKeyword.toLowerCase()
        result = result.filter(t => t.name?.toLowerCase().includes(kw))
      }
      result.sort((a, b) => {
        let valA, valB
        switch (this.sortBy) {
          case 'name':
            valA = a.name?.toLowerCase() || ''
            valB = b.name?.toLowerCase() || ''
            break
          case 'usageCount':
            valA = a.usageCount || 0
            valB = b.usageCount || 0
            break
          default:
            valA = new Date(a.createdAt || 0).getTime()
            valB = new Date(b.createdAt || 0).getTime()
        }
        return this.sortOrder === 'asc' ? (valA > valB ? 1 : -1) : (valA < valB ? 1 : -1)
      })
      return result
    }
  },
  methods: {
    async loadTags() {
      this.loading = true
      try {
        const response = await getTags()
        if (response?.code === 200) {
          this.tags = response.data || []
        } else {
          this.tags = []
        }
        try {
          const statsResponse = await getTagStatistics()
          if (statsResponse?.code === 200) {
            this.stats = statsResponse.data || this.calculateLocalStats()
          } else {
            this.calculateLocalStats()
          }
        } catch (statsError) {
          console.warn('获取标签统计失败，使用本地计算:', statsError)
          this.calculateLocalStats()
        }
        this.generateTagCloud()
      } catch (error) {
        console.error('加载标签失败:', error)
        this.$message.error('加载标签失败')
        this.tags = []
        this.calculateLocalStats()
        this.generateTagCloud()
      } finally {
        this.loading = false
      }
    },
    calculateLocalStats() {
      this.stats = {
        totalTags: this.tags.length,
        popularTags: this.tags.filter(t => (t.usageCount || 0) > 10).length,
        uniqueColors: new Set(this.tags.map(t => t.color)).size
      }
    },
    async refreshTags() {
      await this.loadTags()
      this.$message.success('刷新成功')
    },
    checkRouteTagId() {
      const tagId = this.$route.query.tagId
      if (tagId) {
        const tag = this.tags.find(t => String(t.id) === String(tagId))
        if (tag) {
          this.viewTagRelated(tag)
        }
      }
    },
    editTag(tag) {
      this.isEditing = true
      this.tagForm = { id: tag.id, name: tag.name, color: tag.color || '#409EFF' }
      this.showCreateDialog = true
    },
    handleTagCommand(command) {
      const { action, data } = command
      if (action === 'view') {
        this.viewTagRelated(data)
      } else if (action === 'edit') {
        this.editTag(data)
      } else if (action === 'delete') {
        this.deleteTag(data)
      }
    },
    async viewTagRelated(tag) {
      this.$router.push({
        path: '/collections',
        query: {
          tagIds: [tag.id],
          tagName: tag.name
        }
      })
    },
    async loadTagRelatedContent(tag) {
      this.relatedLoading = true
      try {
        const params = { keyword: tag.name, pageNum: 1, pageSize: 20 }
        const response = await searchAll(params)
        const data = response?.data?.data || {}
        const noteRecords = data.notes?.records || data.notes?.list || []
        const collectionRecords = data.collections?.records || data.collections?.list || []
        this.relatedNotes = noteRecords.map(n => ({
          id: n.id,
          title: n.title || '未命名笔记',
          content: n.description || n.content || '',
          isPublic: n.isPublic,
          updatedAt: n.updateTime || n.updatedAt
        }))
        this.relatedCollections = collectionRecords.map(c => ({
          id: c.id,
          name: c.name || '未命名收藏集',
          description: c.description || '',
          isPublic: c.isPublic,
          itemCount: c.itemCount || 0,
          digestStatus: c.digestStatus
        }))
      } catch (error) {
        console.error('加载关联内容失败:', error)
        this.$message.error('加载关联内容失败')
        this.relatedNotes = []
        this.relatedCollections = []
      } finally {
        this.relatedLoading = false
      }
    },
    viewNote(note) {
      this.$router.push(`/creation/notes/${note.id}`)
      this.showRelatedDialog = false
    },
    viewCollection(collection) {
      this.$router.push(`/collections/${collection.id}`)
      this.showRelatedDialog = false
    },
    getNoteExcerpt(note) {
      if (!note.content) return '暂无内容'
      const text = note.content.replace(/[#*`_\[\]()>]/g, '').trim()
      return text.length > 100 ? text.substring(0, 100) + '...' : text
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    getCollectionStatusType(status) {
      const types = { undigest: 'warning', digesting: 'info', digested: 'success', abandoned: '' }
      return types[status] || ''
    },
    getCollectionStatusLabel(status) {
      const labels = { undigest: '未消化', digesting: '消化中', digested: '已消化', abandoned: '已放弃' }
      return labels[status] || status
    },
    getMockRelatedNotes() {
      return []
    },
    getMockRelatedCollections() {
      return []
    },
    async deleteTag(tag) {
      try {
        await this.$confirm(`确定要删除标签 "${tag.name}" 吗？`, '删除确认', { type: 'warning' })
        await deleteTag(tag.id)
        this.$message.success('删除成功')
        await this.loadTags()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    async submitTag() {
      try {
        await this.$refs.tagForm.validate()
        this.submitLoading = true
        const data = { name: this.tagForm.name.trim(), color: this.tagForm.color }
        if (this.isEditing) {
          await updateTag(this.tagForm.id, data)
          this.$message.success('更新成功')
        } else {
          await createTag(data)
          this.$message.success('创建成功')
        }
        this.showCreateDialog = false
        await this.loadTags()
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
      this.tagForm = { name: '', color: '#409EFF' }
      this.isEditing = false
      this.$refs.tagForm?.resetFields()
    },
    getContrastColor(hex) {
      const r = parseInt(hex.slice(1, 3), 16)
      const g = parseInt(hex.slice(3, 5), 16)
      const b = parseInt(hex.slice(5, 7), 16)
      const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
      return luminance > 0.5 ? '#333' : '#fff'
    },
    getMockTags() {
      return []
    },
    generateTagCloud() {
      this.tagCloudData = [...this.tags].sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0))
    },
    getTagFontSize(usageCount) {
      const minSize = 12
      const maxSize = 32
      const maxCount = Math.max(...this.tags.map(t => t.usageCount || 0), 1)
      const ratio = (usageCount || 0) / maxCount
      return `${minSize + ratio * (maxSize - minSize)}px`
    },
    getTagRotation(tagId) {
      const seed = tagId * 13 % 31 - 15
      return seed
    },
    handleTagClick(tag) {
      this.$router.push({
        path: '/collections',
        query: {
          tagIds: [tag.id],
          tagName: tag.name
        }
      })
    },
    async handleMergeTags() {
      if (!this.mergeForm.sourceTagIds || this.mergeForm.sourceTagIds.length < 2) {
        this.$message.warning('请至少选择2个源标签')
        return
      }
      if (!this.mergeForm.targetTagId) {
        this.$message.warning('请选择目标标签')
        return
      }
      if (this.mergeForm.sourceTagIds.includes(this.mergeForm.targetTagId)) {
        this.$message.error('目标标签不能是源标签之一')
        return
      }
      try {
        await this.$confirm(
          `确定要合并选中的 ${this.mergeForm.sourceTagIds.length} 个标签到目标标签吗？合并后源标签将被删除。`,
          '合并标签确认',
          { type: 'warning' }
        )
        await mergeTags({
          sourceTagIds: this.mergeForm.sourceTagIds,
          targetTagId: this.mergeForm.targetTagId
        })
        this.$message.success('标签合并成功')
        this.showMergeDialog = false
        this.mergeForm = { sourceTagIds: [], targetTagId: null }
        await this.loadTags()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('合并标签失败:', error)
          this.$message.error('合并标签失败')
        }
      }
    }
  },
  mounted() {
    this.loadTags()
    this.checkRouteTagId()
  },
  watch: {
    '$route.query.tagId'(newTagId) {
      if (newTagId) {
        this.checkRouteTagId()
      }
    }
  }
}
</script>

<style scoped>
.tag-management { padding: var(--space-4); }
.tag-cloud-section {
  margin-bottom: var(--space-6);
  padding: var(--space-5);
}
.section-header { margin-bottom: var(--space-4); }
.section-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}
.section-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}
.tag-cloud-container {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  min-height: 120px;
}
.tag-cloud-item {
  cursor: pointer;
  transition: all var(--transition-normal);
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tag-cloud-item:hover { transform: scale(1.1); }
.tag-count { font-size: 0.8em; opacity: 0.7; }
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
.header-right {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}
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
.toolbar {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
  flex-wrap: wrap;
}
.search-input { width: 280px; }
.sort-select, .sort-select-sm { width: 120px; }
.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
}
.tag-card {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
}
.tag-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.tag-color-bar { height: 4px; }
.tag-body { padding: var(--space-4); }
.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.tag-name {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  cursor: pointer;
}
.tag-stats {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}
.tag-related-quick {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--primary-color);
  font-size: var(--font-size-sm);
  cursor: pointer;
}
.related-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.related-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
}
.related-item-icon {
  width: 40px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  color: var(--primary-color);
}
.related-item-content h4 {
  margin: 0 0 var(--space-2);
}
.related-item-content p {
  margin: 0 0 var(--space-2);
  color: var(--text-secondary);
}
.related-item-meta {
  display: flex;
  gap: var(--space-3);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  flex-wrap: wrap;
}
.empty-related,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--space-10) var(--space-6);
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
.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.color-value {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}
.preview-wrapper {
  display: flex;
  align-items: center;
}
.text-danger { color: var(--danger-color); }
.gradient-primary { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.gradient-secondary { background: linear-gradient(135deg, #10b981, #06b6d4); }
.gradient-warm { background: linear-gradient(135deg, #f59e0b, #ef4444); }
@media (max-width: 768px) {
  .page-header,
  .toolbar,
  .header-right { flex-direction: column; align-items: stretch; }
  .search-input,
  .sort-select,
  .sort-select-sm { width: 100%; }
}
</style>
