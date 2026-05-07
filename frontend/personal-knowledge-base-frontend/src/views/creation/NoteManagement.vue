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
                <span class="word-count">{{ (note.content || '').length }} 字</span>
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
                  <span><i class="fas fa-layer-group"></i> {{ getTypeLabel(note.noteType) }}</span>
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
          <el-table-column prop="noteType" label="类型" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="getTypeTag(scope.row.noteType)" size="mini">{{ getTypeLabel(scope.row.noteType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="字数" width="80" align="center">
            <template slot-scope="scope">{{ (scope.row.content || '').length }}</template>
          </el-table-column>
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
      searchKeyword: '',
      filterType: '',
      filterStatus: '',
      currentPage: 1,
      pageSize: 12,
      notes: [],
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
        result = result.filter(n => n.noteType === this.filterType)
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
      this.$router.push({
        path: '/creation/notes/create',
        query: { mode: 'create' }
      })
    },
    handleEditNote(note) {
      if (!note?.id) {
        return
      }
      this.$router.push({
        path: `/creation/notes/${note.id}`,
        query: { mode: 'edit' }
      })
    },
    handleViewNote(note) {
      if (!note?.id) {
        return
      }
      this.$router.push(`/creation/notes/${note.id}`)
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
        const response = await noteApi.updateNote(note.id, { isPublic: newStatus })
        if (response?.code !== 200) {
          throw new Error(response?.message || '操作失败')
        }
        note.isPublic = newStatus
        this.$message.success(`${newStatus ? '设为公开' : '设为私有'}成功`)
      } catch (error) {
        console.error('切换状态失败:', error)
        this.$message.error(error?.message || '操作失败')
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
        await Promise.all(this.selectedNotes.map(id => noteApi.updateNote(id, { isPublic })))
        this.$message.success(`批量设为${isPublic ? '公开' : '私有'}成功`)
        this.clearSelection()
        this.loadNotes()
      } catch (error) {
        console.error('批量操作失败:', error)
        this.$message.error('批量操作失败')
      }
    },
    async batchDelete() {
      if (this.selectedNotes.length === 0) {
        this.$message.warning('请先选择笔记')
        return
      }
      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedNotes.length} 篇笔记吗？`, '确认删除', { type: 'warning' })
        await Promise.all(this.selectedNotes.map(id => noteApi.deleteNote(id)))
        this.$message.success('批量删除成功')
        this.clearSelection()
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
      this.loadNotes()
    },
    handleFilterChange() {
      this.currentPage = 1
      this.loadNotes()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    getNoteStage(note) {
      const status = note.status || note.stage
      if (status === '完成' || status === 'published') {
        return 'published'
      }
      return 'draft'
    },
    getNoteExcerpt(note) {
      const raw = note.description || note.content || ''
      return raw.length > 88 ? `${raw.slice(0, 88)}...` : raw || '暂无内容摘要'
    },
    getSourceReference(note) {
      return note.collectionItemTitle || note.sourceTitle || (note.collectionItemId ? '关联收藏项' : '独立笔记')
    },
    getTypeLabel(type) {
      const map = {
        conceptual: '概念型',
        procedural: '程序型',
        factual: '事实型',
        metacognitive: '元认知型'
      }
      return map[type] || '未分类'
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
    getTypeClass(type) {
      const map = {
        conceptual: 'type-conceptual',
        procedural: 'type-procedural',
        factual: 'type-factual',
        metacognitive: 'type-metacognitive'
      }
      return map[type] || 'type-default'
    },
    formatDate(date) {
      if (!date) return '未知'
      const parsed = new Date(date)
      return Number.isNaN(parsed.getTime()) ? date : parsed.toLocaleDateString('zh-CN')
    }
  },
  created() {
    this.filterStatus = this.initialStatus || ''
    this.loadNotes()
  }
}
</script>

<style scoped>
.note-management {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notes-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.overview-card,
.side-card,
.note-card,
.batch-operation-bar,
.toolbar,
.notes-main,
.notes-side {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.overview-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.overview-icon.primary { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.overview-icon.warning { background: linear-gradient(135deg, #f59e0b, #d97706); }
.overview-icon.success { background: linear-gradient(135deg, #10b981, #059669); }
.overview-icon.info { background: linear-gradient(135deg, #6366f1, #4f46e5); }

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.overview-label {
  color: #64748b;
  font-size: 14px;
}

.toolbar,
.batch-operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}

.toolbar-left,
.toolbar-right,
.batch-actions,
.batch-info,
.notes-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 260px;
}

.filter-select {
  width: 140px;
}

.filter-select-sm {
  width: 120px;
}

.notes-workbench {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 20px;
}

.notes-main,
.notes-side {
  padding: 20px;
}

.notes-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.notes-header {
  justify-content: space-between;
}

.notes-header h3,
.side-header h3,
.card-title,
.side-title {
  margin: 0;
}

.notes-header p,
.side-desc,
.empty-desc,
.card-desc,
.source-label,
.source-value,
.card-meta,
.empty-text {
  color: #64748b;
}

.grid-view {
  margin: 0 !important;
}

.note-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.1);
}

.card-checkbox {
  position: absolute;
  top: 14px;
  left: 14px;
  z-index: 2;
}

.card-badges {
  position: absolute;
  top: 14px;
  right: 14px;
  display: flex;
  gap: 6px;
  z-index: 2;
}

.card-cover {
  height: 110px;
  background: linear-gradient(135deg, #eff6ff, #eef2ff);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #3b82f6;
  gap: 8px;
}

.card-body {
  padding: 18px;
}

.card-title {
  font-size: 16px;
  color: #1f2937;
  margin-bottom: 10px;
}

.card-desc {
  line-height: 1.7;
  min-height: 48px;
  margin-bottom: 14px;
}

.source-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
}

.card-meta,
.card-actions,
.side-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-actions {
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.side-card {
  padding: 18px;
  margin-bottom: 16px;
}

.side-header {
  margin-bottom: 12px;
}

.side-item {
  padding: 12px 0;
  border-top: 1px solid #eef2f7;
}

.side-item:first-child {
  border-top: none;
  padding-top: 0;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 40px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.table-title {
  display: flex;
  align-items: center;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  display: inline-block;
  margin-right: 8px;
}

.type-conceptual { background: #3b82f6; }
.type-procedural { background: #10b981; }
.type-factual { background: #f59e0b; }
.type-metacognitive { background: #8b5cf6; }
.type-default { background: #94a3b8; }

.text-danger {
  color: #ef4444;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1024px) {
  .notes-workbench {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .toolbar,
  .batch-operation-bar,
  .notes-header,
  .toolbar-left,
  .toolbar-right {
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
