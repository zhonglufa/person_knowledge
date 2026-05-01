<template>
  <div class="admin-home">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h2 class="banner-title">欢迎回来，{{ adminName }}</h2>
        <p class="banner-desc">这是您的系统运行概况，请及时关注各项数据变化</p>
      </div>
      <div class="banner-date">
        <i class="el-icon-date"></i>
        {{ currentDate }}
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid" v-loading="loading">
      <div class="stat-card stat-card--users">
        <div class="stat-icon">
          <i class="el-icon-user"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/users')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--today-users">
        <div class="stat-icon">
          <i class="el-icon-user-solid"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayNewUsers || 0 }}</div>
          <div class="stat-label">今日新增</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/users')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--active-users">
        <div class="stat-icon">
          <i class="el-icon-s-operation"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
          <div class="stat-label">7日活跃</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/users')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--total-collections">
        <div class="stat-icon">
          <i class="el-icon-folder"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCollections || 0 }}</div>
          <div class="stat-label">总收藏集数</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/content')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--collections">
        <div class="stat-icon">
          <i class="el-icon-folder-opened"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalCollectionItems || 0 }}</div>
          <div class="stat-label">收藏项总数</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/content')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--notes">
        <div class="stat-icon">
          <i class="el-icon-document"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.noteCount }}</div>
          <div class="stat-label">总笔记数</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/content')">
          <span>查看详情</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--announcements">
        <div class="stat-icon">
          <i class="el-icon-bell"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.announcementCount || 0 }}</div>
          <div class="stat-label">公告数量</div>
        </div>
        <div class="stat-action" @click="$router.push('/admin/announcements')">
          <span>管理公告</span>
          <i class="el-icon-arrow-right"></i>
        </div>
      </div>

      <div class="stat-card stat-card--likes">
        <div class="stat-icon">
          <i class="el-icon-star-on"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalLikes || 0 }}</div>
          <div class="stat-label">点赞总数</div>
        </div>
      </div>

      <div class="stat-card stat-card--comments">
        <div class="stat-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalComments || 0 }}</div>
          <div class="stat-label">评论总数</div>
        </div>
      </div>

      <div class="stat-card stat-card--interactions">
        <div class="stat-icon">
          <i class="el-icon-collection"></i>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalUserCollects || 0 }}</div>
          <div class="stat-label">用户收藏数</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <el-row :gutter="20" class="quick-actions-section">
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="action-card" @click.native="$router.push('/admin/users')">
          <div class="action-content">
            <div class="action-icon action-icon--blue">
              <i class="el-icon-user"></i>
            </div>
            <span class="action-text">用户管理</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="action-card" @click.native="$router.push('/admin/content')">
          <div class="action-content">
            <div class="action-icon action-icon--green">
              <i class="el-icon-document"></i>
            </div>
            <span class="action-text">内容管理</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="action-card" @click.native="$router.push('/admin/announcements')">
          <div class="action-content">
            <div class="action-icon action-icon--orange">
              <i class="el-icon-bell"></i>
            </div>
            <span class="action-text">公告管理</span>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-card shadow="hover" class="action-card" @click.native="$router.push('/admin/content-logs')">
          <div class="action-content">
            <div class="action-icon action-icon--red">
              <i class="el-icon-s-order"></i>
            </div>
            <span class="action-text">操作日志</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态 & 系统信息 -->
    <el-row :gutter="20" class="info-section">
      <el-col :xs="24" :md="12">
        <el-card class="info-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-order"></i> 最近动态</span>
          </div>
          <div class="activity-list">
            <div v-if="recentActivities.length === 0" class="empty-activity">
              <i class="el-icon-info"></i>
              <p>暂无最近动态</p>
            </div>
            <div v-for="(item, index) in recentActivities" :key="index" class="activity-item">
              <div class="activity-dot"></div>
              <div class="activity-content">
                <p class="activity-text">{{ item.text }}</p>
                <span class="activity-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="info-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-monitor"></i> 系统健康状态</span>
          </div>
          <div class="system-info" v-loading="healthLoading">
            <div class="info-item" v-if="healthData.dbStatus">
              <span class="info-label">数据库状态</span>
              <span class="info-value" :class="healthData.dbStatus === 'normal' ? 'status-ok' : 'status-error'">
                <i :class="healthData.dbStatus === 'normal' ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
                {{ healthData.dbStatus === 'normal' ? '正常' : '异常' }}
              </span>
            </div>
            <div class="info-item" v-if="healthData.diskUsage !== undefined && healthData.diskUsage !== 'unknown'">
              <span class="info-label">磁盘使用率</span>
              <span class="info-value">{{ healthData.diskUsage }}% (剩余 {{ healthData.diskFreeGB }} GB)</span>
            </div>
            <div class="info-item" v-if="healthData.uptimeDays !== undefined">
              <span class="info-label">系统运行时长</span>
              <span class="info-value">{{ healthData.uptimeDays }} 天 {{ healthData.uptimeHours }} 小时</span>
            </div>
            <div class="info-item" v-if="healthData.totalUsers">
              <span class="info-label">总用户数</span>
              <span class="info-value">{{ healthData.totalUsers }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表区域 -->
    <el-row :gutter="20" class="trend-section">
      <el-col :span="24">
        <el-card class="trend-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-data-line"></i> 近 30 天趋势</span>
            <el-radio-group v-model="trendDataType" size="small" @change="loadTrendData">
              <el-radio-button label="users">用户增长</el-radio-button>
              <el-radio-button label="content">内容发布</el-radio-button>
            </el-radio-group>
          </div>
          <div class="trend-chart" v-loading="trendLoading">
            <canvas ref="trendCanvas"></canvas>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
  name: 'AdminHome',

  data() {
    return {
      stats: {
        userCount: 0,
        todayNewUsers: 0,
        activeUsers: 0,
        totalCollections: 0,
        totalCollectionItems: 0,
        noteCount: 0,
        announcementCount: 0,
        totalLikes: 0,
        totalComments: 0,
        totalUserCollects: 0
      },
      loading: false,
      recentActivities: [],
      // 趋势数据
      trendDataType: 'users',
      trendData: [],
      trendLoading: false,
      // 健康状态
      healthData: {},
      healthLoading: false,
      // 自动刷新定时器
      refreshTimer: null
    }
  },

  computed: {
    adminName() {
      try {
        const info = JSON.parse(localStorage.getItem('adminInfo') || '{}')
        return info.nickname || info.username || '管理员'
      } catch {
        return '管理员'
      }
    },
    currentDate() {
      const now = new Date()
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
      return now.toLocaleDateString('zh-CN', options)
    }
  },

  created() {
    this.loadStatsData()
    this.loadTrendData()
    this.loadHealthData()
    document.addEventListener('visibilitychange', this.handleVisibilityChange)
  },

  beforeDestroy() {
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
  },

  methods: {
    handleVisibilityChange() {
      if (!document.hidden) {
        this.loadStatsData()
      }
    },

    async loadStatsData() {
      this.loading = true
      try {
        const response = await adminApi.getHomeData()
        if (response.code === 200) {
          const data = response.data
          // 支持扁平结构和嵌套结构两种数据格式
          if (data?.userStats) {
            // 嵌套结构（文档描述格式）
            this.stats.userCount = data.userStats.totalUsers || 0
            this.stats.todayNewUsers = data.userStats.todayNewUsers || 0
            this.stats.activeUsers = data.userStats.activeUsers7d || 0
            this.stats.totalCollectionItems = data.contentStats?.totalItems || 0
            this.stats.totalCollections = data.contentStats?.totalCollections || 0
            this.stats.noteCount = data.contentStats?.totalNotes || 0
            this.stats.totalLikes = data.interactionStats?.totalLikes || 0
            this.stats.totalComments = data.interactionStats?.totalComments || 0
            this.stats.totalUserCollects = data.interactionStats?.totalCollects || 0
          } else {
            // 扁平结构（后端实际返回格式）
            this.stats.userCount = data?.totalUsers || 0
            this.stats.todayNewUsers = data?.todayNewUsers || 0
            this.stats.activeUsers = data?.activeUsers || 0
            this.stats.totalCollections = data?.totalCollections || 0
            this.stats.totalCollectionItems = data?.totalItems || 0
            this.stats.noteCount = data?.totalNotes || 0
            this.stats.announcementCount = data?.activeAnnouncements || 0
            this.stats.totalLikes = data?.totalLikes || 0
            this.stats.totalComments = data?.totalComments || 0
            this.stats.totalUserCollects = data?.totalCollects || 0
          }

          // 如果有最近动态数据则展示
          if (data?.recentActivities && Array.isArray(data.recentActivities)) {
            this.recentActivities = data.recentActivities.slice(0, 5)
          }
        }
      } catch (error) {
        console.error('获取统计数据错误:', error)
        this.$message.error('获取统计数据失败，请检查网络连接')
      } finally {
        this.loading = false
      }
    },

    async loadTrendData() {
      this.trendLoading = true
      try {
        const response = await adminApi.getDashboardTrends({ days: 30, dataType: this.trendDataType })
        if (response.code === 200) {
          const data = response.data
          if (this.trendDataType === 'users' && data.userTrend) {
            this.trendData = data.userTrend
          } else if (this.trendDataType === 'content' && data.contentTrend) {
            this.trendData = data.contentTrend
          }
          this.drawTrendChart()
        }
      } catch (error) {
        console.error('获取趋势数据错误:', error)
        this.$message.error('获取趋势数据失败')
      } finally {
        this.trendLoading = false
      }
    },

    async loadHealthData() {
      this.healthLoading = true
      try {
        const response = await adminApi.getDashboardHealth()
        if (response.code === 200) {
          this.healthData = response.data
        }
      } catch (error) {
        console.error('获取健康状态错误:', error)
      } finally {
        this.healthLoading = false
      }
    },

    drawTrendChart() {
      const canvas = this.$refs.trendCanvas
      if (!canvas || this.trendData.length === 0) return

      const container = canvas.parentElement
      const width = container.clientWidth || 800
      const height = 300
      canvas.width = width
      canvas.height = height

      const ctx = canvas.getContext('2d')
      const padding = 50

      ctx.clearRect(0, 0, width, height)

      // 绘制坐标轴
      ctx.strokeStyle = '#e0e0e0'
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(padding, padding)
      ctx.lineTo(padding, height - padding)
      ctx.lineTo(width - padding, height - padding)
      ctx.stroke()

      // 计算最大值
      const values = this.trendData.map(d => {
        if (this.trendDataType === 'users') return d.newUsers || 0
        return (d.newCollections || 0) + (d.newNotes || 0)
      })
      const maxVal = Math.max(...values, 1)

      // 绘制折线
      ctx.strokeStyle = '#409EFF'
      ctx.lineWidth = 2
      ctx.beginPath()

      const stepX = (width - padding * 2) / (this.trendData.length - 1)

      this.trendData.forEach((d, i) => {
        const x = padding + i * stepX
        const val = this.trendDataType === 'users' ? (d.newUsers || 0) : ((d.newCollections || 0) + (d.newNotes || 0))
        const y = height - padding - (val / maxVal) * (height - padding * 2)

        if (i === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })
      ctx.stroke()

      // 绘制数据点
      ctx.fillStyle = '#409EFF'
      this.trendData.forEach((d, i) => {
        const x = padding + i * stepX
        const val = this.trendDataType === 'users' ? (d.newUsers || 0) : ((d.newCollections || 0) + (d.newNotes || 0))
        const y = height - padding - (val / maxVal) * (height - padding * 2)

        ctx.beginPath()
        ctx.arc(x, y, 3, 0, Math.PI * 2)
        ctx.fill()
      })

      // 绘制 Y 轴标签
      ctx.fillStyle = '#666'
      ctx.font = '12px Arial'
      ctx.textAlign = 'right'
      for (let i = 0; i <= 4; i++) {
        const val = Math.round(maxVal * i / 4)
        const y = height - padding - (i / 4) * (height - padding * 2)
        ctx.fillText(val, padding - 5, y + 4)
      }

      // 绘制 X 轴标签（只显示部分日期）
      ctx.textAlign = 'center'
      const labelInterval = Math.ceil(this.trendData.length / 6)
      this.trendData.forEach((d, i) => {
        if (i % labelInterval === 0 || i === this.trendData.length - 1) {
          const x = padding + i * stepX
          const date = d.date ? d.date.substring(5) : ''
          ctx.fillText(date, x, height - padding + 20)
        }
      })
    }
  }
}
</script>

<style scoped>
.admin-home {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 欢迎横幅 ========== */
.welcome-banner {
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  padding: var(--space-6) var(--space-8);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  box-shadow: var(--shadow-md);
}

.banner-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin: 0 0 var(--space-2);
}

.banner-desc {
  font-size: var(--font-size-sm);
  margin: 0;
  opacity: 0.9;
}

.banner-date {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  opacity: 0.85;
  white-space: nowrap;
}

/* ========== 数据统计卡片 ========== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  background: var(--bg-container);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  border: 1px solid var(--border-light);
  transition: all var(--transition-base);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.stat-card--users::before { background: var(--primary-color); }
.stat-card--today-users::before { background: var(--info-color); }
.stat-card--active-users::before { background: var(--success-color); }
.stat-card--total-collections::before { background: var(--success-color); }
.stat-card--collections::before { background: var(--warning-color); }
.stat-card--notes::before { background: var(--secondary-color); }
.stat-card--announcements::before { background: var(--info-color); }
.stat-card--likes::before { background: var(--warning-color); }
.stat-card--comments::before { background: var(--success-color); }
.stat-card--interactions::before { background: var(--primary-color); }

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl);
  color: #fff;
  margin-bottom: var(--space-4);
}

.stat-card--users .stat-icon { background: var(--gradient-primary); }
.stat-card--today-users .stat-icon { background: linear-gradient(135deg, var(--info-color), var(--info-light)); }
.stat-card--active-users .stat-icon { background: linear-gradient(135deg, var(--success-color), var(--success-light)); }
.stat-card--total-collections .stat-icon { background: linear-gradient(135deg, var(--success-color), var(--success-light)); }
.stat-card--collections .stat-icon { background: linear-gradient(135deg, var(--warning-color), var(--warning-light)); }
.stat-card--notes .stat-icon { background: linear-gradient(135deg, var(--secondary-color), var(--secondary-light)); }
.stat-card--announcements .stat-icon { background: linear-gradient(135deg, var(--info-color), var(--info-light)); }
.stat-card--likes .stat-icon { background: linear-gradient(135deg, var(--warning-color), var(--warning-light)); }
.stat-card--comments .stat-icon { background: linear-gradient(135deg, var(--success-color), var(--success-light)); }
.stat-card--interactions .stat-icon { background: linear-gradient(135deg, var(--primary-color), var(--primary-light)); }

.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

.stat-action {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
  font-size: var(--font-size-sm);
  color: var(--primary-color);
  transition: color var(--transition-fast), transform var(--transition-fast);
}

.stat-action:hover {
  color: var(--primary-hover);
}

.stat-action:hover i {
  transform: translateX(2px);
}

/* ========== 快捷操作 ========== */
.quick-actions-section {
  margin-bottom: var(--space-6);
}

.action-card {
  cursor: pointer;
  transition: all var(--transition-base);
  margin-bottom: var(--space-4);
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.action-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-3xl);
  color: #fff;
}

.action-icon--blue { background: var(--gradient-primary); }
.action-icon--green { background: linear-gradient(135deg, var(--success-color), var(--success-light)); }
.action-icon--orange { background: linear-gradient(135deg, var(--warning-color), var(--warning-light)); }
.action-icon--red { background: linear-gradient(135deg, var(--danger-color), var(--danger-light)); }

.action-text {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--text-primary);
}

/* ========== 信息卡片 ========== */
.info-section {
  margin-bottom: var(--space-6);
}

.info-card :deep(.el-card__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
}

.card-header i {
  color: var(--primary-color);
}

/* 活动列表 */
.activity-list {
  padding: var(--space-2) var(--space-5);
  min-height: 200px;
}

.empty-activity {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 160px;
  color: var(--text-secondary);
}

.empty-activity i {
  font-size: var(--font-size-6xl);
  margin-bottom: var(--space-3);
}

.activity-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px dashed var(--border-light);
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color);
  margin-top: 6px;
  flex-shrink: 0;
}

.activity-text {
  font-size: var(--font-size-sm);
  color: var(--text-regular);
  margin: 0 0 var(--space-1);
}

.activity-time {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

/* 系统信息 */
.system-info {
  padding: var(--space-2) var(--space-5);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  border-bottom: 1px dashed var(--border-light);
  font-size: var(--font-size-sm);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  color: var(--text-secondary);
}

.info-value {
  color: var(--text-primary);
  font-weight: 500;
}

.status-ok {
  color: var(--success-color) !important;
}

.status-ok i {
  margin-right: var(--space-1);
}

.status-error {
  color: var(--danger-color) !important;
}

.status-error i {
  margin-right: var(--space-1);
}

/* 趋势图表区域 */
.trend-section {
  margin-bottom: var(--space-6);
}

.trend-card :deep(.el-card__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.trend-chart {
  padding: var(--space-4);
  display: flex;
  justify-content: center;
}

.trend-chart canvas {
  max-width: 100%;
  height: auto;
}

/* ========== 响应式设计 ========== */
@media (max-width: 1400px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
    padding: var(--space-4) var(--space-5);
  }

  .banner-date {
    align-self: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-value {
    font-size: var(--font-size-2xl);
  }
}
</style>
