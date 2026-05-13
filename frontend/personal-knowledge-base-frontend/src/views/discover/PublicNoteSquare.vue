<template>
  <div class="public-note-square">
    <div class="square-header">
      <div class="header-content">
        <h1 class="page-title">
          <i class="fas fa-compass"></i>
          发现笔记
        </h1>
        <p class="page-subtitle">探索社区中的优质知识沉淀，与他人交流学习心得</p>
      </div>
    </div>

    <div class="square-toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记标题或内容"
          prefix-icon="el-icon-search"
          clearable
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
          class="search-input"
        />
      </div>
      <div class="toolbar-right">
        <el-select 
          v-model="filterType" 
          placeholder="笔记类型" 
          clearable 
          size="small" 
          @change="handleFilterChange"
          class="filter-select"
        >
          <el-option label="全部类型" value="" />
          <el-option label="概念性知识" value="conceptual" />
          <el-option label="程序性知识" value="procedural" />
          <el-option label="事实性知识" value="factual" />
          <el-option label="元认知知识" value="metacognitive" />
        </el-select>
        <el-select 
          v-model="sortBy" 
          placeholder="排序方式" 
          size="small" 
          @change="handleSortChange"
          class="filter-select"
        >
          <el-option label="最新发布" value="latest" />
          <el-option label="最多浏览" value="views" />
          <el-option label="质量评分" value="quality" />
        </el-select>
      </div>
    </div>

    <div class="square-content" v-loading="loading">
      <div v-if="!loading && publicNotes.length === 0" class="empty-state">
        <i class="fas fa-inbox empty-icon"></i>
        <p class="empty-text">暂无公开笔记</p>
        <p class="empty-desc">成为第一个分享知识的人，将你的笔记设为公开吧！</p>
        <el-button type="primary" @click="goToMyNotes">去我的笔记</el-button>
      </div>

      <el-row v-else :gutter="20" class="notes-grid">
        <el-col 
          v-for="note in publicNotes" 
          :key="note.id" 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="6"
        >
          <PublicNoteCard 
            :note="note" 
            @collect-change="handleCollectChange"
            @like-change="handleLikeChange"
          />
        </el-col>
      </el-row>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[12, 24, 36, 48]"
          :page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </div>
  </div>
</template>

<script>
import PublicNoteCard from '@/components/note/PublicNoteCard.vue'
import { noteApi } from '@/api/note'

export default {
  name: 'PublicNoteSquare',
  components: {
    PublicNoteCard
  },
  data() {
    return {
      loading: false,
      publicNotes: [],
      searchKeyword: '',
      filterType: '',
      sortBy: 'latest',
      pageNum: 1,
      pageSize: 12,
      total: 0
    }
  },
  mounted() {
    this.loadPublicNotes()
  },
  methods: {
    async loadPublicNotes() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.searchKeyword || undefined,
          noteType: this.filterType || undefined
        }
        
        const response = await noteApi.getPublicNotes(params)
        
        if (response && response.data) {
          this.publicNotes = response.data.records || []
          this.total = response.data.total || 0
          
          this.applySorting()
        }
      } catch (error) {
        this.$message.error(error.message || '加载公开笔记失败')
      } finally {
        this.loading = false
      }
    },
    
    applySorting() {
      if (this.sortBy === 'views') {
        this.publicNotes.sort((a, b) => (b.views || 0) - (a.views || 0))
      } else if (this.sortBy === 'quality') {
        this.publicNotes.sort((a, b) => (b.noteQualityScore || 0) - (a.noteQualityScore || 0))
      }
    },
    
    handleSearch() {
      this.pageNum = 1
      this.loadPublicNotes()
    },
    
    handleFilterChange() {
      this.pageNum = 1
      this.loadPublicNotes()
    },
    
    handleSortChange() {
      this.applySorting()
    },
    
    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.loadPublicNotes()
    },
    
    handleCurrentChange(val) {
      this.pageNum = val
      this.loadPublicNotes()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    
    handleCollectChange(note) {
      const index = this.publicNotes.findIndex(n => n.id === note.id)
      if (index !== -1) {
        this.publicNotes[index].isCollected = note.isCollected
      }
    },
    
    handleLikeChange(note) {
      const index = this.publicNotes.findIndex(n => n.id === note.id)
      if (index !== -1) {
        this.publicNotes[index].isLiked = note.isLiked
        this.publicNotes[index].likes = note.likes
      }
    },
    
    goToMyNotes() {
      this.$router.push('/creation/notes')
    }
  }
}
</script>

<style scoped lang="scss">
.public-note-square {
  min-height: 100vh;
  background: #f5f7fa;

  .square-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 48px 24px;
    color: white;
    text-align: center;

    .header-content {
      max-width: 1200px;
      margin: 0 auto;

      .page-title {
        font-size: 36px;
        font-weight: 700;
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 12px;

        i {
          font-size: 32px;
        }
      }

      .page-subtitle {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
      }
    }
  }

  .square-toolbar {
    max-width: 1200px;
    margin: 24px auto;
    padding: 0 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;

    .toolbar-left {
      flex: 1;
      min-width: 200px;

      .search-input {
        max-width: 400px;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 12px;

      .filter-select {
        width: 140px;
      }
    }
  }

  .square-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px 48px;

    .empty-state {
      text-align: center;
      padding: 80px 20px;
      background: white;
      border-radius: 8px;

      .empty-icon {
        font-size: 64px;
        color: #dcdfe6;
        margin-bottom: 16px;
      }

      .empty-text {
        font-size: 18px;
        font-weight: 500;
        color: #606266;
        margin: 0 0 8px 0;
      }

      .empty-desc {
        font-size: 14px;
        color: #909399;
        margin: 0 0 24px 0;
      }
    }

    .notes-grid {
      margin-bottom: 32px;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: center;
      padding: 24px 0;
    }
  }
}

@media (max-width: 768px) {
  .public-note-square {
    .square-header {
      padding: 32px 16px;

      .page-title {
        font-size: 28px;
      }

      .page-subtitle {
        font-size: 14px;
      }
    }

    .square-toolbar {
      flex-direction: column;
      align-items: stretch;

      .toolbar-left {
        .search-input {
          max-width: 100%;
        }
      }

      .toolbar-right {
        justify-content: space-between;

        .filter-select {
          flex: 1;
        }
      }
    }
  }
}
</style>
