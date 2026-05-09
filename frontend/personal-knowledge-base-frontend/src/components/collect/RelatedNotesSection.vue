<template>
  <div class="related-notes-section card-panel">
    <div class="section-header">
      <div class="header-left">
        <i class="el-icon-document"></i>
        <h3>关联笔记</h3>
        <el-tag size="small" type="info">{{ relatedNotes.length }}</el-tag>
      </div>
      <div class="header-right">
        <el-button 
          type="primary" 
          size="small" 
          icon="el-icon-plus"
          @click="createNoteFromCollect"
        >
          创建笔记
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="relatedNotes.length === 0" class="empty-state">
      <i class="el-icon-document-copy empty-icon"></i>
      <p class="empty-text">暂无关联笔记</p>
      <p class="empty-hint">将收藏项转化为笔记，开始知识沉淀</p>
      <el-button type="primary" size="small" @click="createNoteFromCollect">
        立即创建笔记
      </el-button>
    </div>

    <div v-else class="notes-list">
      <div 
        v-for="note in relatedNotes" 
        :key="note.id" 
        class="note-card"
        @click="goToNoteDetail(note.id)"
      >
        <div class="note-header">
          <h4 class="note-title">{{ note.title || '未命名笔记' }}</h4>
          <el-tag 
            size="mini" 
            :type="getStatusType(note.status)"
          >
            {{ getStatusText(note.status) }}
          </el-tag>
        </div>

        <p v-if="note.description" class="note-description">
          {{ note.description }}
        </p>

        <div class="note-meta">
          <span class="meta-item">
            <i class="el-icon-time"></i>
            {{ formatDate(note.updateTime || note.createTime) }}
          </span>
          <span v-if="note.noteQualityScore" class="meta-item quality-score">
            <i class="el-icon-star-on"></i>
            质量评分: {{ note.noteQualityScore }}
          </span>
          <span class="meta-item">
            <i class="el-icon-view"></i>
            {{ note.views || 0 }} 次浏览
          </span>
        </div>

        <div class="note-actions" @click.stop>
          <el-button 
            type="text" 
            size="small" 
            icon="el-icon-edit"
            @click="editNote(note.id)"
          >
            编辑
          </el-button>
          <el-button 
            type="text" 
            size="small" 
            icon="el-icon-view"
            @click="goToNoteDetail(note.id)"
          >
            查看
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { collectApi } from '@/api/collect'
  import { formatDistanceToNow } from 'date-fns'
  import { zhCN } from 'date-fns/locale'

export default {
  name: 'RelatedNotesSection',
  props: {
    collectionItemId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      relatedNotes: []
    }
  },
  mounted() {
    this.loadRelatedNotes()
  },
  methods: {
    async loadRelatedNotes() {
      if (!this.collectionItemId) return
      
      this.loading = true
      try {
        const res = await collectApi.getRelatedNotes(this.collectionItemId)
        if (res.code === 200 && res.data) {
          this.relatedNotes = res.data.notes || []
        }
      } catch (error) {
        console.error('加载关联笔记失败:', error)
        this.$message.error('加载关联笔记失败')
      } finally {
        this.loading = false
      }
    },

    createNoteFromCollect() {
      this.$router.push({
        path: '/creation/note',
        query: { 
          collectionItemId: this.collectionItemId,
          mode: 'create'
        }
      })
    },

    goToNoteDetail(noteId) {
      this.$router.push({
        path: `/creation/note/${noteId}`
      })
    },

    editNote(noteId) {
      this.$router.push({
        path: `/creation/note/${noteId}`,
        query: { mode: 'edit' }
      })
    },

    getStatusType(status) {
      const statusMap = {
        '草稿': 'info',
        '完成': 'success',
        '进行中': 'warning'
      }
      return statusMap[status] || 'info'
    },

    getStatusText(status) {
      return status || '草稿'
    },

    formatDate(dateStr) {
      if (!dateStr) return '未知时间'
      try {
        return formatDistanceToNow(new Date(dateStr), { 
          addSuffix: true,
          locale: zhCN 
        })
      } catch (e) {
        return dateStr
      }
    }
  }
}
</script>

<style scoped lang="scss">
.related-notes-section {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e8e8e8;

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;

    i {
      font-size: 20px;
      color: #409eff;
    }

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;

  .empty-icon {
    font-size: 64px;
    color: #dcdfe6;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 16px;
    color: #606266;
    margin: 0 0 8px 0;
  }

  .empty-hint {
    font-size: 14px;
    color: #909399;
    margin: 0 0 24px 0;
  }
}

.notes-list {
  display: grid;
  gap: 16px;
}

.note-card {
  padding: 16px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
    transform: translateY(-2px);
  }

  .note-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .note-title {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .note-description {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    margin: 0 0 12px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .note-meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    margin-bottom: 12px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #909399;

      i {
        font-size: 14px;
      }

      &.quality-score {
        color: #f56c6c;
        font-weight: 500;

        i {
          color: #f56c6c;
        }
      }
    }
  }

  .note-actions {
    display: flex;
    gap: 8px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
}
</style>
