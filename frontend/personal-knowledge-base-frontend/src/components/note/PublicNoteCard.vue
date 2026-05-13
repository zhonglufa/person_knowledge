<template>
  <div class="public-note-card" @click="handleViewNote">
    <div class="card-header">
      <div class="author-info">
        <el-avatar :size="32" :src="note.userAvatar" icon="el-icon-user-solid"></el-avatar>
        <div class="author-details">
          <span class="author-name">{{ note.userName || '匿名用户' }}</span>
          <span class="publish-time">{{ formatTime(note.createTime) }}</span>
        </div>
      </div>
      <el-tag size="mini" :type="getNoteTypeColor(note.noteType)">
        {{ getNoteTypeLabel(note.noteType) }}
      </el-tag>
    </div>

    <div class="card-body">
      <h3 class="note-title">{{ note.title }}</h3>
      <p class="note-description" v-if="note.description">{{ note.description }}</p>
      <div class="note-preview" v-else>{{ getContentPreview(note.content) }}</div>
    </div>

    <div class="card-footer">
      <div class="note-stats">
        <span class="stat-item">
          <i class="fas fa-eye"></i>
          {{ note.views || 0 }}
        </span>
        <span class="stat-item">
          <i class="fas fa-heart"></i>
          {{ note.likes || 0 }}
        </span>
        <span class="stat-item">
          <i class="fas fa-comment"></i>
          {{ note.comments || 0 }}
        </span>
        <span class="stat-item" v-if="note.noteQualityScore">
          <i class="fas fa-star"></i>
          {{ note.noteQualityScore }}分
        </span>
      </div>
      <div class="card-actions" @click.stop>
        <el-button
          :type="note.isLiked ? 'danger' : 'default'"
          :icon="note.isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
          size="mini"
          circle
          @click="handleLike"
          :loading="likeLoading"
          title="点赞"
        ></el-button>
        <el-button
          :type="note.isCollected ? 'warning' : 'default'"
          :icon="note.isCollected ? 'el-icon-folder' : 'el-icon-folder-add'"
          size="mini"
          circle
          @click="handleCollect"
          :loading="collectLoading"
          title="收藏"
        ></el-button>
        <el-button
          type="default"
          icon="el-icon-chat-dot-round"
          size="mini"
          circle
          @click="handleComment"
          title="评论"
        ></el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { noteApi } from '@/api/note'
import { likeContent, unlikeContent } from '@/api/interaction'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

export default {
  name: 'PublicNoteCard',
  props: {
    note: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      collectLoading: false,
      likeLoading: false
    }
  },
  methods: {
    handleViewNote() {
      this.$router.push(`/creation/notes/${this.note.id}`)
    },
    
    async handleLike() {
      this.likeLoading = true
      try {
        if (this.note.isLiked) {
          await unlikeContent(this.note.id, 'note')
          this.$message.success('取消点赞成功')
          this.note.isLiked = false
          this.note.likes = Math.max(0, (this.note.likes || 0) - 1)
        } else {
          await likeContent(this.note.id, 'note')
          this.$message.success('点赞成功')
          this.note.isLiked = true
          this.note.likes = (this.note.likes || 0) + 1
        }
        this.$emit('like-change', this.note)
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      } finally {
        this.likeLoading = false
      }
    },
    
    async handleCollect() {
      this.collectLoading = true
      try {
        if (this.note.isCollected) {
          await noteApi.uncollectNote(this.note.id)
          this.$message.success('取消收藏成功')
          this.note.isCollected = false
        } else {
          await noteApi.collectNote(this.note.id)
          this.$message.success('收藏成功')
          this.note.isCollected = true
        }
        this.$emit('collect-change', this.note)
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      } finally {
        this.collectLoading = false
      }
    },
    
    handleComment() {
      this.$router.push({
        path: `/creation/notes/${this.note.id}`,
        hash: '#comments'
      })
    },
    
    formatTime(time) {
      if (!time) return ''
      try {
        return formatDistanceToNow(new Date(time), { 
          addSuffix: true,
          locale: zhCN 
        })
      } catch {
        return time
      }
    },
    
    getNoteTypeLabel(type) {
      const typeMap = {
        'conceptual': '概念性',
        'procedural': '程序性',
        'factual': '事实性',
        'metacognitive': '元认知'
      }
      return typeMap[type] || '其他'
    },
    
    getNoteTypeColor(type) {
      const colorMap = {
        'conceptual': 'primary',
        'procedural': 'success',
        'factual': 'warning',
        'metacognitive': 'danger'
      }
      return colorMap[type] || 'info'
    },
    
    getContentPreview(content) {
      if (!content) return '暂无内容'
      const plainText = content.replace(/<[^>]+>/g, '').replace(/[#*`]/g, '')
      return plainText.length > 120 ? plainText.substring(0, 120) + '...' : plainText
    }
  }
}
</script>

<style scoped lang="scss">
.public-note-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
  height: 100%;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
    border-color: #409eff;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .author-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .author-details {
        display: flex;
        flex-direction: column;

        .author-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
        }

        .publish-time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .card-body {
    flex: 1;
    margin-bottom: 12px;

    .note-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .note-description,
    .note-preview {
      font-size: 13px;
      color: #606266;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .note-stats {
      display: flex;
      gap: 16px;

      .stat-item {
        font-size: 12px;
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;

        i {
          font-size: 12px;
        }
      }
    }

    .card-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
