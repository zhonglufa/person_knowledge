<template>
  <div class="public-item-detail">
    <div class="detail-header">
      <el-button type="text" icon="el-icon-arrow-left" @click="goBack">返回</el-button>
      <div class="header-actions">
        <el-button v-if="!isLoggedIn" type="primary" @click="goToLogin">登录收藏</el-button>
        <el-button v-else type="primary" icon="el-icon-star-off" @click="handleCollect">收藏</el-button>
      </div>
    </div>

    <div class="detail-container" v-loading="loading">
      <div class="item-header">
        <h1 class="item-title">{{ item.title || '未命名收藏项' }}</h1>
        <div class="item-meta">
          <el-tag size="small" type="info">{{ getSourceTypeText(item.sourceType) }}</el-tag>
          <el-tag size="small" v-if="item.source">{{ item.source }}</el-tag>
          <span class="meta-item">
            <i class="el-icon-view"></i> {{ item.visitCount || 0 }} 次访问
          </span>
          <span class="meta-item">
            <i class="el-icon-time"></i> {{ formatDate(item.createdAt) }}
          </span>
        </div>
      </div>

      <div class="item-content">
        <div class="content-section" v-if="item.coreAbstract">
          <h3>核心摘要</h3>
          <p class="abstract">{{ item.coreAbstract }}</p>
        </div>

        <div class="content-section" v-if="item.keywords">
          <h3>关键词</h3>
          <div class="keywords">
            <el-tag
              v-for="keyword in item.keywords.split(',')"
              :key="keyword"
              size="small"
              class="keyword-tag"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </div>

        <div class="content-section" v-if="item.url">
          <h3>原始链接</h3>
          <a :href="item.url" target="_blank" class="source-link">
            {{ item.url }}
            <i class="el-icon-top-right"></i>
          </a>
        </div>

        <div class="content-section" v-if="item.content">
          <h3>内容详情</h3>
          <div class="content-body" v-html="formatContent(item.content)"></div>
        </div>

        <div class="content-section" v-if="item.imageUrl">
          <h3>图片</h3>
          <img :src="item.imageUrl" :alt="item.title" class="content-image" @click="previewImage" />
        </div>
      </div>

      <div class="item-footer">
        <p class="footer-tip">
          <i class="el-icon-info"></i>
          这是一个公开分享的收藏项，{{ isLoggedIn ? '你可以收藏到自己的知识库' : '登录后可以收藏到你的知识库' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { getPublicCollectionItem } from '@/api/collect'

export default {
  name: 'PublicCollectionItemDetail',
  data() {
    return {
      loading: false,
      item: {}
    }
  },
  computed: {
    isLoggedIn() {
      return !!this.$store.state.user.token
    }
  },
  mounted() {
    this.loadItemDetail()
  },
  methods: {
    async loadItemDetail() {
      const id = this.$route.params.id
      if (!id) {
        this.$message.error('收藏项ID不存在')
        this.goBack()
        return
      }

      this.loading = true
      try {
        const response = await getPublicCollectionItem(id)
        if (response.code === 200) {
          this.item = response.data
        } else {
          this.$message.error(response.message || '获取收藏项详情失败')
          this.goBack()
        }
      } catch (error) {
        console.error('获取公开收藏项详情失败:', error)
        this.$message.error('该收藏项不存在或未公开')
        this.goBack()
      } finally {
        this.loading = false
      }
    },
    goBack() {
      this.$router.go(-1)
    },
    goToLogin() {
      this.$router.push({
        path: '/login',
        query: { redirect: this.$route.fullPath }
      })
    },
    async handleCollect() {
      this.$message.info('收藏功能开发中...')
    },
    getSourceTypeText(type) {
      const map = {
        1: '网页',
        2: '图片',
        3: '文本',
        4: '视频',
        5: '笔记'
      }
      return map[type] || '其他'
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },
    formatContent(content) {
      if (!content) return ''
      return content.replace(/\n/g, '<br>')
    },
    previewImage() {
      if (this.item.imageUrl) {
        window.open(this.item.imageUrl, '_blank')
      }
    }
  }
}
</script>

<style scoped>
.public-item-detail {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.detail-container {
  max-width: 900px;
  margin: 24px auto;
  padding: 0 24px;
}

.item-header {
  background-color: #fff;
  padding: 32px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.item-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.item-content {
  background-color: #fff;
  padding: 32px;
  border-radius: 8px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.content-section {
  margin-bottom: 32px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.abstract {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  cursor: default;
}

.source-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
  word-break: break-all;
}

.source-link:hover {
  text-decoration: underline;
}

.content-body {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
}

.content-image {
  max-width: 100%;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.content-image:hover {
  transform: scale(1.02);
}

.item-footer {
  background-color: #fff;
  padding: 24px 32px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.footer-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  margin: 0;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 0 16px;
  }

  .item-header,
  .item-content,
  .item-footer {
    padding: 20px;
  }

  .item-title {
    font-size: 22px;
  }
}
</style>
