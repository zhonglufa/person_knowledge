<template>
  <div class="author-info-card">
    <div class="author-main">
      <el-avatar 
        :size="48" 
        :src="author.avatar || author.userAvatar" 
        icon="el-icon-user-solid"
        class="author-avatar"
      ></el-avatar>
      
      <div class="author-details">
        <div class="author-name-row">
          <span class="author-name">{{ author.userName || author.name || '匿名用户' }}</span>
          <el-tag v-if="author.isVerified" size="mini" type="success" class="verified-badge">
            <i class="el-icon-success"></i> 认证
          </el-tag>
        </div>
        <p class="author-bio" v-if="author.bio || author.description">
          {{ author.bio || author.description }}
        </p>
      </div>
    </div>

    <div class="author-stats">
      <div class="stat-item">
        <span class="stat-value">{{ author.noteCount || 0 }}</span>
        <span class="stat-label">笔记</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-value">{{ author.likesCount || 0 }}</span>
        <span class="stat-label">获赞</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <span class="stat-value">{{ author.followersCount || 0 }}</span>
        <span class="stat-label">粉丝</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AuthorInfoCard',
  props: {
    author: {
      type: Object,
      required: true,
      default: () => ({})
    }
  }
}
</script>

<style scoped>
.author-info-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  transition: all 0.3s ease;
}

.author-info-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.author-main {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

.author-avatar {
  flex-shrink: 0;
  border: 2px solid #f0f2f5;
  transition: all 0.3s ease;
}

.author-avatar:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.author-details {
  flex: 1;
  min-width: 0;
}

.author-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s ease;
}

.author-name:hover {
  color: #409eff;
}

.verified-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.author-bio {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.author-stats {
  display: flex;
  align-items: center;
  gap: 0;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 0 16px;
  min-width: 60px;
}

.stat-item:first-child {
  padding-left: 0;
}

.stat-item:last-child {
  padding-right: 0;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: #e4e7ed;
}

@media (max-width: 768px) {
  .author-info-card {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .author-main {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .author-stats {
    justify-content: center;
    padding-top: 12px;
    border-top: 1px solid #f0f2f5;
  }

  .stat-item {
    padding: 0 12px;
  }
}
</style>
