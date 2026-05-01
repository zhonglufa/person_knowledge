<template>
  <div class="note-management">
    <div class="notes-overview">
      <div class="overview-card" v-for="metric in overviewMetrics" :key="metric.key">
        <div class="overview-icon" :class="metric.theme">
          <i :class="metric.icon"></i>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ metric.value }}</div>
          <div class="overview-label">{{ metric.label }}</div>
        </div>
      </div>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" icon="el-icon-plus" @click="handleCreateNote">新建笔记</el-button>
        <el-button icon="el-icon-refresh" @click="handleRefresh">刷新</el-button>
        <el-button :type="showBatchMode ? 'danger' : ''" icon="el-icon-check" @click="toggleBatchMode">
          {{ showBatchMode ? '退出批量' : '批量操作' }}
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记标题或内容"
          prefix-icon="el-icon-search"
          clearable
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
          class="search-input"
        />
        <el-select v-model="filterType" placeholder="类型" clearable size="small" @change="handleFilterChange" class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="概念性知识" value="conceptual" />
          <el-option label="程序性知识" value="procedural" />
          <el-option label="事实性知识" value="factual" />
          <el-option label="元认知知识" value="metacognitive" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="沉淀状态" clearable size="small" @change="handleFilterChange" class="filter-select-sm">
          <el-option label="全部" value="" />
          <el-option label="草稿" value="draft" />
          <el-option label="已完成" value="published" />
          <el-option label="公开" value="public" />
          <el-option label="私有" value="private" />
        </el-select>
      </div>
    </div>

    <div v-if="showBatchMode && selectedNotes.length > 0" class="batch-operation-bar">
      <div class="batch-info">
        <i class="fas fa-check-circle"></i>
        <span>已选择 <strong>{{ selectedNotes.length }}</strong> 篇笔记</span>
      </div>
      <div class="batch-actions">
        <el-button type="success" size="small" icon="el-icon-view" @click="batchSetPublic(true)">设为公开</el-button>
        <el-button type="warning" size="small" icon="el-icon-lock" @click="batchSetPublic(false)">设为私有</el-button>
        <el-button type="danger" size="small" icon="el-icon-delete" @click="batchDelete">批量删除</el-button>
        <el-button size="small" icon="el-icon-close" @click="clearSelection">取消选择</el-button>
      </div>
    </div>

    <div class="notes-workbench" v-loading="loading">
      <div class="notes-main">
        <div class="notes-header">
          <div>
            <h3>{{ isDraftMode ? '草稿箱' : '我的笔记' }}</h3>
            <p>{{ isDraftMode ? '继续完善未完成的沉淀内容。' : '查看最近编辑、来源收藏项与沉淀结果。' }}</p>
          </div>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="grid">卡片</el-radio-button>
            <el-radio-button label="list">列表</el-radio-button>
          </el-radio-group>
        </div>

        <div v-if="!loading && paginatedNotes.length === 0" class="empty-state">
          <i class="fas fa-file-alt empty-icon"></i>
          <p class="empty-text">{{ isDraftMode ? '暂无草稿' : '暂无笔记' }}</p>
          <p class="empty-desc">{{ isDraftMode ? '从收藏项开始创建笔记，草稿会在这里继续完善。' : '创建你的第一篇笔记，记录学习心得和思考。' }}</p>
          <el-button type="primary" @click="handleCreateNote">新建笔记</el-button>
        </div>

        <el-row v-else-if="viewMode === 'grid'" :gutter="20" class="grid-view">
          <el-col v-for="note in paginatedNotes" :key="note.id" :xs="24" :sm="12" :md="8" :lg="6">
            <div class="note-card" @click="handleViewNote(note)">
              <el-checkbox
                v-if="showBatchMode"
                :value="selectedNotes.includes(note.id)"
                @click.native.stop
                @change="toggleNoteSelection(note.id)"
                class="card-checkbox"
              />
              <div class="card-badges">
                <el-tag v-if="getNoteStage(note) === 'draft'" size="mini" type="warning">草稿</el-tag>
                <el-tag v-else size="mini" type="success">已完成</el-tag>
                <el-tag v-if="note.isPublic" size="mini" type="success">公开</el-tag>
              </div>
              <div class="card-cover">
                <i class="fas fa-file-alt"></i>
                <span class="word-count">{{ note.wordCount || 0 }} 字</span>
              </div>
              <div class="card-body">
                <h3 class="card-title">{{ note.title }}</h3>
                <p class="card-desc">{{ getNoteExcerpt(note) }}</p>
                <div class="source-info">
                  <span class="source-label">来源收藏项</span>
                  <span class="source-value">{{ getSourceReference(note) }}</span>
                </div>
                <div class="card-meta">
                  <span><i class="fas fa-calendar"></i> {{ formatDate(note.updateTime) }}</span>
                  <span><i class="fas fa-layer-group"></i> {{ getTypeLabel(note.type) }}</span>
                </div>
                <div class="card-actions" @click.stop>
                  <el-button type="text" size="mini" @click="handleEditNote(note)">{{ getNoteStage(note) === 'draft' ? '继续编辑' : '编辑' }}</el-button>
                  <el-dropdown @command="handleCommand">
                    <el-button type="text" size="mini">
                      <i class="fas fa-ellipsis-v"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{ action: 'toggle', data: note }">
                        {{ note.isPublic ? '设为私有' : '设为公开' }}
                      </el-dropdown-item>
                      <el-dropdown-item :command="{ action: 'delete', data: note }" divided class="text-danger">
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <el-table v-else :data="paginatedNotes" style="width: 100%" @row-click="handleViewNote">
          <el-table-column v-if="showBatchMode" type="selection" width="55" align="center" />
          <el-table-column prop="title" label="标题" min-width="200">
            <template slot-scope="scope">
              <div class="table-title">
                <span class="type-dot" :class="getTypeClass(scope.row.type)"></span>
                <span>{{ scope.row.title }}</span>
                <el-tag size="mini" :type="getNoteStage(scope.row) === 'draft' ? 'warning' : 'success'" style="margin-left: 8px;">
                  {{ getNoteStage(scope.row) === 'draft' ? '草稿' : '已完成' }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="来源收藏项" min-width="180">
            <template slot-scope="scope">{{ getSourceReference(scope.row) }}</template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getTypeTag(scope.row.type)" size="mini">{{ getTypeLabel(scope.row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="wordCount" label="字数" width="80" align="center" />
          <el-table-column prop="updateTime" label="更新时间" width="160" align="center">
            <template slot-scope="scope">{{ formatDate(scope.row.updateTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="small" @click.stop="handleEditNote(scope.row)">{{ getNoteStage(scope.row) === 'draft' ? '继续编辑' : '编辑' }}</el-button>
              <el-button type="text" size="small" class="text-danger" @click.stop="handleDeleteNote(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper" v-if="filteredNotes.length > pageSize">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="currentPage"
            :page-sizes="[12, 24, 48]"
            :page-size="pageSize"
            :total="filteredNotes.length"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <div class="notes-side">
        <div class="side-card">
          <div class="side-header">
            <h3>沉淀建议</h3>
          </div>
          <div class="side-list">
            <div class="side-item" v-for="tip in sideTips" :key="tip.title">
              <div class="side-title">{{ tip.title }}</div>
              <div class="side-desc">{{ tip.desc }}</div>
            </div>
          </div>
        </div>

        <div class="side-card">
          <div class="side-header">
            <h3>最近关注</h3>
          </div>
          <div class="side-list">
            <div class="side-item" v-for="note in focusNotes" :key="note.id">
              <div class="side-title">{{ note.title }}</div>
              <div class="side-desc">{{ getSourceReference(note) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog :title="isEditing ? '编辑笔记' : '新建笔记'" :visible.sync="showDialog" width="700px" :before-close="handleDialogClose">
      <el-form :model="noteForm" :rules="formRules" ref="noteForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noteForm.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="noteForm.type" placeholder="选择类型" style="width: 100%">
            <el-option label="概念性知识" value="conceptual" />
            <el-option label="程序性知识" value="procedural" />
            <el-option label="事实性知识" value="factual" />
            <el-option label="元认知知识" value="metacognitive" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="noteForm.content" type="textarea" :rows="10" placeholder="请输入笔记内容" />
        </el-form-item>
        <el-form-item label="公开设置">
          <el-switch v-model="noteForm.isPublic" active-text="公开" inactive-text="私有" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="noteForm.tags" multiple filterable allow-create placeholder="添加标签" style="width: 100%">
            <el-option v-for="tag in availableTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleSubmitNote" :loading="submitLoading">
          {{ isEditing ? '更新' : '创建' }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { noteApi } from '@/api/note'

export default {
  name: 'NoteManagement',
  props: {
    initialStatus: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      viewMode: 'grid',
      loading: false,
      submitLoading: false,
      searchKeyword: '',
      filterType: '',
      filterStatus: '',
      currentPage: 1,
      pageSize: 12,
      notes: [],
      availableTags: ['Vue', 'React', '前端', '后端', '知识管理'],
      showDialog: false,
      isEditing: false,
      noteForm: {
        title: '',
        type: 'conceptual',
        content: '',
        isPublic: false,
        tags: []
      },
      formRules: {
        title: [
          { required: true, message: '请输入笔记标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入笔记内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      },
      showBatchMode: false,
      selectedNotes: []
    }
  },
  computed: {
    isDraftMode() {
      return this.initialStatus === 'draft' || this.filterStatus === 'draft'
    },
    normalizedNotes() {
      return this.notes.map(note => ({
        ...note,
        stage: this.getNoteStage(note)
      }))
    },
    filteredNotes() {
      let result = [...this.normalizedNotes]
      const kw = this.searchKeyword?.toLowerCase()
      if (kw) {
        result = result.filter(n =>
          n.title?.toLowerCase().includes(kw) ||
          n.content?.toLowerCase().includes(kw) ||
          n.tags?.some(t => t.toLowerCase().includes(kw))
        )
      }
      if (this.filterType) {
        result = result.filter(n => n.type === this.filterType)
      }
      if (this.filterStatus) {
        if (this.filterStatus === 'draft' || this.filterStatus === 'published') {
          result = result.filter(n => n.stage === this.filterStatus)
        } else {
          const isPublic = this.filterStatus === 'public'
          result = result.filter(n => n.isPublic === isPublic)
        }
      }
      if (this.initialStatus === 'draft' && !this.filterStatus) {
        result = result.filter(n => n.stage === 'draft')
      }
      return result
    },
    paginatedNotes() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredNotes.slice(start, start + this.pageSize)
    },
    overviewMetrics() {
      const total = this.normalizedNotes.length
      const drafts = this.normalizedNotes.filter(note => note.stage === 'draft').length
      const published = this.normalizedNotes.filter(note => note.stage === 'published').length
      const publicCount = this.normalizedNotes.filter(note => note.isPublic).length
      return [
        { key: 'total', label: '全部笔记', value: total, icon: 'fas fa-file-alt', theme: 'primary' },
        { key: 'drafts', label: '草稿', value: drafts, icon: 'fas fa-file-pen', theme: 'warning' },
        { key: 'published', label: '已完成', value: published, icon: 'fas fa-check-circle', theme: 'success' },
        { key: 'public', label: '公开', value: publicCount, icon: 'fas fa-globe', theme: 'info' }
      ]
    },
    sideTips() {
      return [
        { title: '从收藏项继续沉淀', desc: '优先完善已有关联来源的笔记，避免知识链路中断。' },
        { title: '草稿定期回看', desc: '将草稿视为待完成任务，而不是临时文本。' },
        { title: '沉淀后再公开', desc: '优先确保内容结构完整，再决定是否对外公开。' }
      ]
    },
    focusNotes() {
      return this.filteredNotes.slice(0, 4)
    }
  },
  methods: {
    async loadNotes() {
      this.loading = true
      try {
        const params = {
          pageNum: 1,
          pageSize: 100,
          keyword: this.searchKeyword || undefined,
          type: this.filterType || undefined,
          isPublic: this.filterStatus === 'public' ? true : this.filterStatus === 'private' ? false : undefined
        }
        const response = await noteApi.getNoteList(params)
        if (response?.code === 200) {
          this.notes = response.data?.records || []
        }
      } catch (error) {
        console.error('加载笔记失败:', error)
        this.$message.error('加载笔记失败')
      } finally {
        this.loading = false
      }
    },
    handleCreateNote() {
      this.isEditing = false
      this.noteForm = { title: '', type: 'conceptual', content: '', isPublic: false, tags: [] }
      this.showDialog = true
    },
    async handleEditNote(note) {
      this.isEditing = true
      this.noteForm = {
        id: note.id,
        title: note.title,
        type: note.type,
        content: note.content,
        isPublic: note.isPublic || false,
        tags: note.tags || []
      }
      this.showDialog = true
    },
    async handleViewNote(note) {
      try {
        const response = await noteApi.getNoteDetail(note.id)
        if (response?.code === 200) {
          const noteDetail = response.data
          this.isEditing = true
          this.noteForm = {
            id: noteDetail.id,
            title: noteDetail.title,
            type: noteDetail.type,
            content: noteDetail.content,
            isPublic: noteDetail.isPublic || false,
            tags: noteDetail.tags || []
          }
          this.showDialog = true
        }
      } catch (error) {
        console.error('获取笔记详情失败:', error)
        this.$message.error('加载笔记详情失败')
      }
    },
    async handleDeleteNote(note) {
      try {
        await this.$confirm('确定要删除这篇笔记吗？', '确认删除', { type: 'warning' })
        await noteApi.deleteNote(note.id)
        this.$message.success('删除成功')
        this.loadNotes()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除笔记失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    handleCommand(command) {
      const { action, data } = command
      if (action === 'toggle') {
        this.handleTogglePublic(data)
      } else if (action === 'delete') {
        this.handleDeleteNote(data)
      }
    },
    async handleTogglePublic(note) {
      try {
        const newStatus = !note.isPublic
        await noteApi.updateNote(note.id, { isPublic: newStatus })
        note.isPublic = newStatus
        this.$message.success(`${newStatus ? '设为公开' : '设为私有'}成功`)
      } catch (error) {
        console.error('切换状态失败:', error)
        this.$message.error('操作失败')
      }
    },
    handleDialogClose() {
      this.showDialog = false
      this.$refs.noteForm?.resetFields()
    },
    async handleSubmitNote() {
      try {
        await this.$refs.noteForm.validate()
        this.submitLoading = true
        const noteData = {
          title: this.noteForm.title,
          type: this.noteForm.type,
          content: this.noteForm.content,
          isPublic: this.noteForm.isPublic,
          tags: this.noteForm.tags
        }
        if (this.isEditing) {
          await noteApi.updateNote(this.noteForm.id, noteData)
          this.$message.success('更新成功')
        } else {
          await noteApi.createNote(noteData)
          this.$message.success('创建成功')
        }
        this.handleDialogClose()
        this.loadNotes()
      } catch (error) {
        if (!error.errors) {
          console.error('操作失败:', error)
          this.$message.error('操作失败')
        }
      } finally {
        this.submitLoading = false
      }
    },
    handleRefresh() {
      this.loadNotes()
    },
    toggleBatchMode() {
      this.showBatchMode = !this.showBatchMode
      if (!this.showBatchMode) {
        this.selectedNotes = []
      }
    },
    toggleNoteSelection(noteId) {
      const index = this.selectedNotes.indexOf(noteId)
      if (index > -1) {
        this.selectedNotes.splice(index, 1)
      } else {
        this.selectedNotes.push(noteId)
      }
    },
    clearSelection() {
      this.selectedNotes = []
    },
    async batchSetPublic(isPublic) {
      if (this.selectedNotes.length === 0) {
        this.$message.warning('请先选择笔记')
        return
      }
      try {
        await this.$confirm(`确定要将选中的 ${this.selectedNotes.length} 篇笔记${isPublic ? '设为公开' : '设为私有'}吗？`, '批量操作确认', { type: 'warning' })
        await Promise.all(this.selectedNotes.map(noteId => noteApi.updateNote(noteId, { isPublic })))
        this.$message.success('批量操作成功')
        this.selectedNotes = []
        this.loadNotes()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量操作失败:', error)
          this.$message.error('批量操作失败')
        }
      }
    },
    async batchDelete() {
      if (this.selectedNotes.length === 0) {
        this.$message.warning('请先选择笔记')
        return
      }
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedNotes.length} 篇笔记吗？此操作不可恢复！`, '批量删除确认', { type: 'error' })
        await Promise.all(this.selectedNotes.map(noteId => noteApi.deleteNote(noteId)))
        this.$message.success('批量删除成功')
        this.selectedNotes = []
        this.loadNotes()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量删除失败:', error)
          this.$message.error('批量删除失败')
        }
      }
    },
    handleSearch() {
      this.currentPage = 1
    },
    handleFilterChange() {
      this.currentPage = 1
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    getNoteExcerpt(note) {
      if (!note.content) return '暂无内容'
      return note.content.length > 80 ? `${note.content.substring(0, 80)}...` : note.content
    },
    getTypeClass(type) {
      const map = {
        conceptual: 'type-conceptual',
        procedural: 'type-procedural',
        factual: 'type-factual',
        metacognitive: 'type-metacognitive'
      }
      return map[type] || 'type-conceptual'
    },
    getTypeTag(type) {
      const map = {
        conceptual: 'primary',
        procedural: 'success',
        factual: 'warning',
        metacognitive: 'info'
      }
      return map[type] || 'info'
    },
    getTypeLabel(type) {
      const map = {
        conceptual: '概念性',
        procedural: '程序性',
        factual: '事实性',
        metacognitive: '元认知'
      }
      return map[type] || '概念性'
    },
    getNoteStage(note) {
      if (note.status) {
        return note.status === 'draft' ? 'draft' : 'published'
      }
      if (note.isDraft !== undefined) {
        return note.isDraft ? 'draft' : 'published'
      }
      return note.isPublic ? 'published' : 'draft'
    },
    getSourceReference(note) {
      if (note.collectionItemTitle) return note.collectionItemTitle
      if (note.collectionItemId) return `收藏项 #${note.collectionItemId}`
      return '未关联收藏项'
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    }
  },
  watch: {
    initialStatus: {
      immediate: true,
      handler(value) {
        if (value === 'draft') {
          this.filterStatus = 'draft'
        }
      }
    }
  },
  mounted() {
    this.loadNotes()
  }
}
</script>

<style scoped>
.note-management {
  height: 100%;
}

.notes-overview {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.overview-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
}

.overview-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.overview-icon.primary { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.overview-icon.warning { background: linear-gradient(135deg, #f59e0b, #f97316); }
.overview-icon.success { background: linear-gradient(135deg, #10b981, #06b6d4); }
.overview-icon.info { background: linear-gradient(135deg, #3b82f6, #06b6d4); }

.overview-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.overview-label {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-base);
}

.toolbar-left,
.toolbar-right,
.batch-actions {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.batch-operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
  background: linear-gradient(135deg, #fff5f5, #ffe5e5);
  border: 1px solid #ffcccc;
  border-radius: var(--radius-md);
}

.batch-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: #f56c6c;
}

.notes-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(280px, 0.8fr);
  gap: var(--space-5);
}

.notes-main,
.side-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
}

.notes-main {
  padding: var(--space-5);
}

.notes-side {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.side-card {
  padding: var(--space-4);
}

.notes-header,
.side-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.notes-header h3,
.side-header h3 {
  margin: 0 0 var(--space-1);
  color: var(--text-primary);
}

.notes-header p,
.side-desc,
.source-info,
.card-desc,
.card-meta {
  color: var(--text-secondary);
}

.grid-view {
  margin-bottom: var(--space-4);
}

.note-card {
  position: relative;
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-base);
  transition: all var(--transition-normal);
  margin-bottom: var(--space-5);
  cursor: pointer;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.card-checkbox {
  position: absolute;
  top: var(--space-3);
  left: var(--space-3);
  z-index: 20;
}

.card-checkbox :deep(.el-checkbox__label) {
  display: none;
}

.card-badges {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  display: flex;
  gap: var(--space-1);
  z-index: 10;
}

.card-cover {
  height: 100px;
  background: linear-gradient(135deg, var(--bg-canvas), var(--primary-bg));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.card-cover i {
  font-size: 32px;
  margin-bottom: var(--space-2);
}

.word-count {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-secondary);
}

.card-body {
  padding: var(--space-4);
}

.card-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-desc {
  margin: 0 0 var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.source-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-bottom: var(--space-3);
  font-size: var(--font-size-sm);
}

.source-label {
  color: var(--text-placeholder);
}

.source-value {
  color: var(--text-primary);
}

.card-meta,
.card-actions,
.table-title,
.side-item {
  display: flex;
  gap: var(--space-2);
}

.card-meta {
  flex-wrap: wrap;
  font-size: var(--font-size-xs);
  margin-bottom: var(--space-3);
}

.card-actions {
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.note-card:hover .card-actions {
  opacity: 1;
}

.table-title {
  align-items: center;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.type-dot.type-conceptual { background: #6366f1; }
.type-dot.type-procedural { background: #10b981; }
.type-dot.type-factual { background: #f59e0b; }
.type-dot.type-metacognitive { background: #06b6d4; }

.side-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.side-item {
  flex-direction: column;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.side-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.side-title {
  color: var(--text-primary);
  font-weight: 600;
}

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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

.search-input { width: 220px; }
.filter-select { width: 120px; }
.filter-select-sm { width: 120px; }
.text-danger { color: var(--danger-color); }

@media (max-width: 1200px) {
  .notes-overview {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .notes-workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .notes-overview {
    grid-template-columns: 1fr;
  }

  .toolbar,
  .toolbar-left,
  .toolbar-right,
  .batch-operation-bar,
  .batch-actions,
  .notes-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-select,
  .filter-select-sm {
    width: 100%;
  }
}
</style>
