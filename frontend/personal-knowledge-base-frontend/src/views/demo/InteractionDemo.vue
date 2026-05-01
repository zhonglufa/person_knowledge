<template>
  <div class="interaction-demo">
    <h2>内容互动功能演示</h2>
    
    <!-- 互动操作区域 -->
    <div class="interaction-actions">
      <div class="action-group">
        <h3>互动操作</h3>
        <div class="action-buttons">
          <button 
            @click="handleLike" 
            :class="{ active: interactionStatus.isLiked }"
            class="btn-like"
          >
            👍 点赞 ({{ stats.likeCount }})
          </button>
          
          <button 
            @click="handleCollect" 
            :class="{ active: interactionStatus.isCollected }"
            class="btn-collect"
          >
            ⭐ 收藏 ({{ stats.collectCount }})
          </button>
          
          <button @click="showCommentDialog = true" class="btn-comment">
            💬 评论 ({{ stats.commentCount }})
          </button>
        </div>
      </div>
      
      <!-- 评论输入框 -->
      <div v-if="showCommentDialog" class="comment-dialog">
        <div class="comment-input">
          <textarea 
            v-model="commentContent" 
            placeholder="请输入您的评论..."
            rows="3"
          ></textarea>
          <div class="comment-actions">
            <button @click="handleComment" class="btn-submit">发表评论</button>
            <button @click="showCommentDialog = false" class="btn-cancel">取消</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 互动统计信息 -->
    <div class="interaction-stats">
      <h3>互动统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-label">点赞数:</span>
          <span class="stat-value">{{ stats.likeCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">评论数:</span>
          <span class="stat-value">{{ stats.commentCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">收藏数:</span>
          <span class="stat-value">{{ stats.collectCount }}</span>
        </div>
      </div>
    </div>
    
    <!-- 评论列表 -->
    <div class="comments-section">
      <h3>评论列表</h3>
      <div v-if="comments.length === 0" class="no-comments">
        暂无评论，快来发表第一条评论吧！
      </div>
      <div v-else class="comments-list">
        <div 
          v-for="comment in comments" 
          :key="comment.id" 
          class="comment-item"
        >
          <div class="comment-header">
            <span class="comment-user">用户{{ comment.userId }}</span>
            <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
        </div>
      </div>
    </div>
    
    <!-- API调用示例 -->
    <div class="api-examples">
      <h3>API调用示例</h3>
      <div class="example-item">
        <h4>点赞操作</h4>
        <pre><code>POST /api/demo/interaction/like?targetId=1&targetType=collection</code></pre>
      </div>
      <div class="example-item">
        <h4>获取统计</h4>
        <pre><code>GET /api/demo/interaction/stats?targetId=1&targetType=collection</code></pre>
      </div>
      <div class="example-item">
        <h4>获取评论</h4>
        <pre><code>GET /api/demo/interaction/comments?targetId=1&targetType=collection</code></pre>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'InteractionDemo',
  setup() {
    // 响应式数据
    const stats = ref({
      likeCount: 0,
      commentCount: 0,
      collectCount: 0
    })
    
    const interactionStatus = ref({
      isLiked: false,
      isCollected: false
    })
    
    const comments = ref([])
    const showCommentDialog = ref(false)
    const commentContent = ref('')
    const targetId = 1 // 演示用的目标ID
    const targetType = 'collection' // 演示用的目标类型
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    }
    
    // 获取互动统计
    const fetchStats = async () => {
      try {
        const response = await axios.get('/api/demo/interaction/stats', {
          params: { targetId, targetType }
        })
        if (response.data.code === 200) {
          stats.value = response.data.data.stats
        }
      } catch (error) {
        console.error('获取统计失败:', error)
      }
    }
    
    // 获取用户互动状态
    const fetchStatus = async () => {
      try {
        const response = await axios.get('/api/demo/interaction/status', {
          params: { targetId, targetType }
        })
        if (response.data.code === 200) {
          interactionStatus.value = response.data.data.status
        }
      } catch (error) {
        console.error('获取状态失败:', error)
      }
    }
    
    // 获取评论列表
    const fetchComments = async () => {
      try {
        const response = await axios.get('/api/demo/interaction/comments', {
          params: { targetId, targetType, page: 1, size: 10 }
        })
        if (response.data.code === 200) {
          comments.value = response.data.data.list
        }
      } catch (error) {
        console.error('获取评论失败:', error)
      }
    }
    
    // 点赞操作
    const handleLike = async () => {
      try {
        const url = interactionStatus.value.isLiked 
          ? '/api/demo/interaction/like' 
          : '/api/demo/interaction/like'
          
        const method = interactionStatus.value.isLiked ? 'delete' : 'post'
        
        const response = await axios({
          method,
          url,
          params: { targetId, targetType }
        })
        
        if (response.data.code === 200) {
          // 更新状态
          interactionStatus.value.isLiked = !interactionStatus.value.isLiked
          // 重新获取统计
          await fetchStats()
          alert(interactionStatus.value.isLiked ? '点赞成功' : '取消点赞成功')
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        alert('操作失败')
      }
    }
    
    // 收藏操作
    const handleCollect = async () => {
      try {
        const url = '/api/demo/interaction/collect'
        const response = await axios.post(url, null, {
          params: { targetId, targetType }
        })
        
        if (response.data.code === 200) {
          // 更新状态
          interactionStatus.value.isCollected = !interactionStatus.value.isCollected
          // 重新获取统计
          await fetchStats()
          alert('收藏成功')
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        alert('操作失败')
      }
    }
    
    // 发表评论
    const handleComment = async () => {
      if (!commentContent.value.trim()) {
        alert('请输入评论内容')
        return
      }
      
      try {
        const response = await axios.post('/api/demo/interaction/comment', null, {
          params: { 
            targetId, 
            targetType, 
            content: commentContent.value 
          }
        })
        
        if (response.data.code === 200) {
          commentContent.value = ''
          showCommentDialog.value = false
          // 重新获取评论和统计
          await fetchComments()
          await fetchStats()
          alert('评论发表成功')
        }
      } catch (error) {
        console.error('发表评论失败:', error)
        alert('评论发表失败')
      }
    }
    
    // 初始化数据
    onMounted(async () => {
      await Promise.all([
        fetchStats(),
        fetchStatus(),
        fetchComments()
      ])
    })
    
    return {
      stats,
      interactionStatus,
      comments,
      showCommentDialog,
      commentContent,
      formatTime,
      handleLike,
      handleCollect,
      handleComment
    }
  }
}
</script>

<style scoped>
.interaction-demo {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-5);
  font-family: Arial, sans-serif;
}

.interaction-demo h2 {
  color: var(--text-primary);
  text-align: center;
  margin-bottom: var(--space-8);
}

.interaction-actions {
  background: var(--bg-canvas);
  padding: var(--space-5);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-5);
}

.action-group h3 {
  margin-top: 0;
  color: var(--text-regular);
}

.action-buttons {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.action-buttons button {
  padding: var(--space-3) var(--space-5);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  transition: all var(--transition-normal);
}

.btn-like {
  background: var(--info-bg);
  color: var(--info-color);
}

.btn-like.active {
  background: var(--info-color);
  color: white;
}

.btn-collect {
  background: var(--warning-bg);
  color: var(--warning-dark);
}

.btn-collect.active {
  background: var(--warning-dark);
  color: white;
}

.btn-comment {
  background: var(--success-bg);
  color: var(--success-dark);
}

.action-buttons button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.comment-dialog {
  margin-top: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-container);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.comment-input textarea {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  resize: vertical;
  margin-bottom: var(--space-3);
  transition: border-color var(--transition-fast);
}

.comment-input textarea:focus {
  border-color: var(--primary-color);
  outline: none;
}

.comment-actions {
  display: flex;
  gap: var(--space-3);
}

.btn-submit {
  background: var(--success-color);
  color: white;
  padding: var(--space-2) var(--space-4);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-submit:hover {
  background: var(--success-dark);
}

.btn-cancel {
  background: var(--danger-color);
  color: white;
  padding: var(--space-2) var(--space-4);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-cancel:hover {
  background: var(--danger-dark);
}

.interaction-stats {
  background: var(--bg-container);
  padding: var(--space-5);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-5);
  box-shadow: var(--shadow-sm);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--space-4);
}

.stat-item {
  text-align: center;
  padding: var(--space-4);
  background: var(--bg-canvas);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.stat-item:hover {
  background: var(--primary-bg);
}

.stat-label {
  display: block;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

.comments-section {
  background: var(--bg-container);
  padding: var(--space-5);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-5);
  box-shadow: var(--shadow-sm);
}

.no-comments {
  text-align: center;
  color: var(--text-placeholder);
  padding: var(--space-8);
}

.comments-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
  transition: background-color var(--transition-fast);
}

.comment-item:hover {
  background-color: var(--bg-canvas);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-3);
  font-size: 14px;
}

.comment-user {
  font-weight: bold;
  color: var(--text-primary);
}

.comment-time {
  color: var(--text-placeholder);
}

.comment-content {
  color: var(--text-regular);
  line-height: 1.5;
}

.api-examples {
  background: #2d2d2d;
  color: #f8f8f2;
  padding: var(--space-5);
  border-radius: var(--radius-md);
  font-family: 'Courier New', monospace;
}

.api-examples h3 {
  color: #f8f8f2;
  margin-top: 0;
}

.example-item {
  margin-bottom: var(--space-4);
}

.example-item h4 {
  color: #a6e22e;
  margin: var(--space-3) 0 var(--space-1) 0;
}

.example-item pre {
  background: #1e1e1e;
  padding: var(--space-3);
  border-radius: var(--radius-xs);
  overflow-x: auto;
}

.example-item code {
  color: #f8f8f2;
}
</style>