<template>
  <div v-loading="pageLoading" class="dashboard-page">
    <!-- 页面头部 -->
    <div class="page-header card">
      <div class="header-content">
        <div class="header-left">
          <div class="breadcrumb">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
              <el-breadcrumb-item>统计看板</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <h1 class="page-title">统计看板</h1>
          <p class="page-subtitle">用于查看更完整的收藏、笔记、分类与学习进度分析，避免与个人中心首页重复堆叠。</p>
        </div>
        <div class="header-right">
          <el-button type="text" @click="$router.push('/personal/center')">
            返回个人中心 <i class="fas fa-arrow-right"></i>
          </el-button>
          <el-date-picker
        v-model="dateRange"
        type="daterange"
        id="dateRange"
        name="dateRange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        size="small"
        @change="handleDateRangeChange"
      />
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 数据概览 -->
      <div class="overview-section">
        <div class="overview-cards">
          <div class="overview-card card">
            <div class="card-icon bg-primary-gradient">
              <i class="fas fa-bookmark"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.totalCollections || 0 }}</div>
              <div class="card-label">总收藏数</div>
              <div class="card-trend" :class="getTrendClass(overviewStats?.collectionTrend)">
                <i :class="overviewStats?.collectionTrend > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
                {{ Math.abs(overviewStats?.collectionTrend || 0) }}%
              </div>
            </div>
          </div>

          <div class="overview-card card">
            <div class="card-icon bg-success-gradient">
              <i class="fas fa-file-alt"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.totalNotes || 0 }}</div>
              <div class="card-label">总笔记数</div>
              <div class="card-trend" :class="getTrendClass(overviewStats?.noteTrend)">
                <i :class="overviewStats?.noteTrend > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
                {{ Math.abs(overviewStats?.noteTrend || 0) }}%
              </div>
            </div>
          </div>

          <div class="overview-card card">
            <div class="card-icon bg-info-gradient">
              <i class="fas fa-tags"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.totalTags || 0 }}</div>
              <div class="card-label">标签总数</div>
              <div class="card-trend neutral">
                <i class="fas fa-minus"></i>
                0%
              </div>
            </div>
          </div>

          <div class="overview-card card">
            <div class="card-icon bg-warning-gradient">
              <i class="fas fa-fire"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.processingItems || 0 }}</div>
              <div class="card-label">加工中项目</div>
              <div class="card-trend" :class="getTrendClass(overviewStats?.processingTrend)">
                <i :class="overviewStats?.processingTrend > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
                {{ Math.abs(overviewStats?.processingTrend || 0) }}%
              </div>
            </div>
          </div>

          <!-- 新增：分类统计卡片 -->
          <div class="overview-card card" @click="activeTab = 'categories'" style="cursor: pointer;">
            <div class="card-icon bg-purple-gradient">
              <i class="fas fa-folder-open"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.totalCategories || 0 }}</div>
              <div class="card-label">总分类数</div>
              <div class="card-trend neutral">
                <i class="fas fa-folder-tree"></i>
                分类管理
              </div>
            </div>
          </div>

          <!-- 新增：学习进度卡片 -->
          <div class="overview-card card" @click="activeTab = 'learning'" style="cursor: pointer;">
            <div class="card-icon bg-cyan-gradient">
              <i class="fas fa-graduation-cap"></i>
            </div>
            <div class="card-content">
              <div class="card-value">{{ overviewStats?.learningProgress || 0 }}%</div>
              <div class="card-label">学习完成率</div>
              <div class="card-trend" :class="getTrendClass(overviewStats?.learningTrend)">
                <i :class="overviewStats?.learningTrend > 0 ? 'fas fa-arrow-up' : 'fas fa-arrow-down'"></i>
                {{ Math.abs(overviewStats?.learningTrend || 0) }}%
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-section">
        <div class="chart-row">
          <!-- 收藏趋势图 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>收藏趋势</h3>
              <el-select v-model="collectionChartType" id="collectionChartType" name="collectionChartType" size="small" @change="updateCollectionChart">
              <el-option label="日" value="daily"></el-option>
              <el-option label="周" value="weekly"></el-option>
              <el-option label="月" value="monthly"></el-option>
            </el-select>
            </div>
            <div class="chart-container">
              <canvas ref="collectionChart"></canvas>
            </div>
          </div>

          <!-- 类型分布图 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>内容类型分布</h3>
            </div>
            <div class="chart-container">
              <canvas ref="typeChart"></canvas>
            </div>
          </div>
        </div>

        <div class="chart-row">
          <!-- 加工状态分布 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>加工状态分布</h3>
            </div>
            <div class="chart-container">
              <canvas ref="processingChart"></canvas>
            </div>
          </div>

          <!-- 分类统计分布图 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>分类分布统计</h3>
              <el-button type="text" size="small" icon="el-icon-refresh" @click="loadCategoryStatistics"></el-button>
            </div>
            <div v-if="categoryStats.length > 0" class="chart-container">
              <canvas ref="categoryChart"></canvas>
            </div>
            <div v-else class="chart-empty">
              <i class="el-icon-folder-opened"></i>
              <span>暂无分类数据</span>
            </div>
          </div>
        </div>

        <div class="chart-row">
          <!-- 学习进度环形图 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>学习进度总览</h3>
              <el-select v-model="progressViewType" id="progressViewType" name="progressViewType" size="small" @change="updateProgressChart">
              <el-option label="消化状态" value="digest"></el-option>
              <el-option label="笔记类型" value="note"></el-option>
            </el-select>
            </div>
            <div class="chart-container">
              <canvas ref="progressChart"></canvas>
            </div>
            <div class="progress-legend">
              <div v-for="item in progressLegend" :key="item.label" class="legend-item">
                <span class="legend-color" :style="{ backgroundColor: item.color }"></span>
                <span class="legend-label">{{ item.label }}</span>
                <span class="legend-value">{{ item.value }} ({{ item.percent }}%)</span>
              </div>
            </div>
          </div>

          <!-- 标签云 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>标签云</h3>
              <el-button type="text" size="small" icon="el-icon-refresh" @click="loadTagCloud"></el-button>
            </div>
            <div v-if="tagCloud.length > 0" class="tag-cloud-container">
              <span
                v-for="(tag, index) in tagCloud"
                :key="tag.name || index"
                class="tag-cloud-item"
                :style="getTagCloudStyle(tag)"
                :title="`${tag.name}: ${tag.count} 项`"
              >
                {{ tag.name || '' }}
              </span>
            </div>
            <div v-else class="chart-empty">
              <i class="el-icon-collection-tag"></i>
              <span>暂无标签数据</span>
            </div>
          </div>
        </div>
        
        <div class="chart-row">
          <!-- 每周学习活动热力图 -->
          <div class="chart-card card full-width">
            <div class="chart-header">
              <h3>每周学习活动</h3>
              <div class="chart-controls">
              <el-radio-group v-model="activityTimeRange" id="activityTimeRange" name="activityTimeRange" size="small" @change="loadActivityHeatmap">
                <el-radio-button label="week" id="activityWeek" name="activityWeek">本周</el-radio-button>
                <el-radio-button label="month" id="activityMonth" name="activityMonth">本月</el-radio-button>
                <el-radio-button label="quarter" id="activityQuarter" name="activityQuarter">近三月</el-radio-button>
              </el-radio-group>
            </div>
            </div>
            <div class="heatmap-container">
              <div class="heatmap-grid">
                <div v-for="(day, index) in heatmapData" :key="index" class="heatmap-cell">
                  <el-tooltip :content="`${day.date}: ${day.count} 次活动`" placement="top">
                    <div 
                      class="heatmap-day" 
                      :class="getHeatmapClass(day.count)"
                      @click="showActivityDetail(day)"
                    >
                    </div>
                  </el-tooltip>
                </div>
              </div>
              <div class="heatmap-legend">
                <span>少</span>
                <div class="heatmap-scale">
                  <div class="heatmap-level level-0"></div>
                  <div class="heatmap-level level-1"></div>
                  <div class="heatmap-level level-2"></div>
                  <div class="heatmap-level level-3"></div>
                  <div class="heatmap-level level-4"></div>
                </div>
                <span>多</span>
              </div>
            </div>
          </div>
        </div>

        <div class="chart-row">

          <!-- 热门标签 Top 10 -->
          <div class="chart-card card">
            <div class="chart-header">
              <h3>热门标签 Top 10</h3>
            </div>
            <div v-if="topTags && topTags.length > 0" class="tags-container">
              <div
                v-for="(tag, index) in topTags"
                :key="tag.name || index"
                class="tag-item"
              >
                <div class="tag-rank">#{{ index + 1 }}</div>
                <div class="tag-content">
                  <span class="tag-name">{{ tag.name || '' }}</span>
                  <span class="tag-count">{{ tag.count || 0 }}</span>
                </div>
                <div class="tag-bar">
                  <div
                    class="tag-progress"
                    :style="{ width: `${getTagProgress(tag)}%` }"
                  ></div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无标签数据" :image-size="60" />
          </div>
        </div>
      </div>

      <!-- 详细统计 -->
      <div class="details-section card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="收藏集统计" name="collections">
            <div class="stats-table">
              <el-table
                v-loading="tableLoading"
                :data="collectionStats"
                style="width: 100%"
                empty-text="暂无收藏集数据"
              >
                <el-table-column prop="name" label="收藏集名称" width="200"></el-table-column>
                <el-table-column prop="itemCount" label="项目数" width="100">
                  <template slot-scope="scope">{{ scope.row?.itemCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="processedCount" label="已加工" width="100">
                  <template slot-scope="scope">{{ scope.row?.processedCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="processingRate" label="加工率">
                  <template slot-scope="scope">
                    <el-progress
                      :percentage="scope.row?.processingRate ?? 0"
                      :status="getProgressStatus(scope.row?.processingRate)"
                    ></el-progress>
                  </template>
                </el-table-column>
                <el-table-column prop="lastUpdated" label="最后更新">
                  <template slot-scope="scope">
                    {{ formatDate(scope.row?.lastUpdated) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="笔记统计" name="notes">
            <div class="stats-table">
              <el-table
                v-loading="tableLoading"
                :data="noteStats"
                style="width: 100%"
                empty-text="暂无笔记数据"
              >
                <el-table-column prop="title" label="笔记标题" width="250">
                  <template slot-scope="scope">
                    <span class="text-truncate" :title="scope.row?.title || ''">
                      {{ scope.row?.title || '' }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="wordCount" label="字数" width="100">
                  <template slot-scope="scope">{{ scope.row?.wordCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="tagCount" label="标签数" width="100">
                  <template slot-scope="scope">{{ scope.row?.tagCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="views" label="浏览量" width="100">
                  <template slot-scope="scope">{{ scope.row?.views ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间">
                  <template slot-scope="scope">
                    {{ formatDate(scope.row?.createTime) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="分类统计" name="categories">
            <div class="stats-table">
              <el-table
                v-loading="tableLoading"
                :data="categoryStats"
                style="width: 100%"
                empty-text="暂无分类数据"
              >
                <el-table-column prop="name" label="分类名称" width="200"></el-table-column>
                <el-table-column prop="itemCount" label="项目数" width="100">
                  <template slot-scope="scope">{{ scope.row?.itemCount ?? 0 }}</template>
                </el-table-column>
                <el-table-column prop="percentage" label="占比">
                  <template slot-scope="scope">
                    <div class="percentage-bar">
                      <div class="percentage-fill" :style="{ width: `${scope.row?.percentage ?? 0}%` }"></div>
                      <span class="percentage-text">{{ scope.row?.percentage ?? 0 }}%</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="lastUpdated" label="最近更新">
                  <template slot-scope="scope">
                    {{ formatDate(scope.row?.lastUpdated) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="学习进度" name="learning">
            <div class="learning-stats">
              <div class="learning-overview">
                <div class="learning-circle">
                  <el-progress
                    type="circle"
                    :percentage="overviewStats?.learningProgress || 0"
                    :width="180"
                    :stroke-width="12"
                    color="#409EFF"
                  ></el-progress>
                  <div class="learning-circle-label">
                    <span class="learning-title">总体进度</span>
                    <span class="learning-value">{{ overviewStats?.learningProgress || 0 }}%</span>
                  </div>
                </div>
                <div class="learning-cards">
                  <div class="learning-card">
                    <i class="el-icon-document-checked"></i>
                    <div class="learning-card-content">
                      <span class="learning-card-value">{{ learningStats?.completed || 0 }}</span>
                      <span class="learning-card-label">已完成项目</span>
                    </div>
                  </div>
                  <div class="learning-card">
                    <i class="el-icon-loading"></i>
                    <div class="learning-card-content">
                      <span class="learning-card-value">{{ learningStats?.inProgress || 0 }}</span>
                      <span class="learning-card-label">进行中项目</span>
                    </div>
                  </div>
                  <div class="learning-card">
                    <i class="el-icon-alarm-clock"></i>
                    <div class="learning-card-content">
                      <span class="learning-card-value">{{ learningStats?.overdue || 0 }}</span>
                      <span class="learning-card-label">逾期项目</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="learning-progress-list">
                <h4>学习进度明细</h4>
                <el-table :data="learningProgressList" style="width: 100%">
                  <el-table-column prop="title" label="项目名称" min-width="200"></el-table-column>
                  <el-table-column prop="progress" label="进度" width="200">
                    <template slot-scope="scope">
                      <el-progress :percentage="scope.row?.progress ?? 0" :stroke-width="8"></el-progress>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template slot-scope="scope">
                      <el-tag :type="getStatusType(scope.row?.status)">{{ getStatusText(scope.row?.status) }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="活跃度分析" name="activity">
            <div class="activity-stats">
              <div class="activity-cards">
                <div class="activity-card card">
                  <h4>本周活跃度</h4>
                  <div class="activity-score">{{ activityStats?.weeklyScore || 0 }}%</div>
                  <div class="activity-desc">
                    较上周 {{ (activityStats?.weeklyChange || 0) > 0 ? '增加' : '减少' }}
                    {{ Math.abs(activityStats?.weeklyChange || 0) }}%
                  </div>
                </div>

                <div class="activity-card card">
                  <h4>月度贡献</h4>
                  <div class="activity-score">{{ activityStats?.monthlyCount || 0 }}</div>
                  <div class="activity-desc">
                    新增 {{ activityStats?.monthlyNew || 0 }} 项内容
                  </div>
                </div>

                <div class="activity-card card">
                  <h4>最长连续天数</h4>
                  <div class="activity-score">{{ activityStats?.streakDays || 0 }}</div>
                  <div class="activity-desc">保持每日更新的习惯</div>
                </div>
              </div>

              <div class="activity-calendar">
                <h4>近30天活跃度日历</h4>
                <div class="calendar-grid">
                  <div
                    v-for="day in activityCalendar"
                    :key="day.date"
                    class="calendar-day"
                    :class="getCalendarClass(day.count)"
                    :title="`${day.date}: ${day.count || 0} 项活动`"
                  >
                    <span class="day-number">{{ day.day || '' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getUserStatistics } from '@/api/user'
import { getCategoryStatistics } from '@/api/category'
import { getTagCloud } from '@/api/tag'

let Chart = null

export default {
  name: 'Dashboard',
  data() {
    return {
      pageLoading: false,
      tableLoading: false,
      dateRange: [new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()],
      activeTab: 'collections',
      collectionChartType: 'weekly',
      collectionChart: null,
      typeChart: null,
      processingChart: null,
      categoryChart: null,
      progressChart: null, // 学习进度图表
      
      // 学习进度相关
      progressViewType: 'digest', // digest: 消化状态, note: 笔记类型
      progressLegend: [],
      
      // 活动热力图相关
      activityTimeRange: 'week',
      heatmapData: [],

      overviewStats: {
        totalCollections: 0,
        totalNotes: 0,
        totalTags: 0,
        processingItems: 0,
        collectionTrend: 0,
        noteTrend: 0,
        processingTrend: 0,
        totalCategories: 0,
        learningProgress: 0,
        learningTrend: 0
      },

      collectionData: {
        labels: [],
        datasets: [{
          label: '收藏数量',
          data: [],
          borderColor: 'var(--primary-color)',
          backgroundColor: 'var(--primary-bg)',
          tension: 0.4,
          fill: true
        }]
      },

      typeData: {
        labels: ['网页', '文档', '图片', '视频', '音频'],
        datasets: [{
          data: [35, 25, 20, 15, 5],
          backgroundColor: [
            'var(--primary-color)',
            'var(--success-color)',
            'var(--warning-color)',
            'var(--danger-color)',
            'var(--secondary-color)'
          ]
        }]
      },

      processingData: {
        labels: ['未加工', '加工中', '已加工', '已放弃'],
        datasets: [{
          data: [40, 25, 30, 5],
          backgroundColor: [
            'var(--danger-color)',
            'var(--warning-color)',
            'var(--success-color)',
            'var(--text-placeholder)'
          ]
        }]
      },

      categoryData: {
        labels: [],
        datasets: [{
          data: [],
          backgroundColor: [
            '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
            '#8E71D9', '#3DB39E', '#E97771', '#6B8EE6', '#D39E5B'
          ]
        }]
      },

      topTags: [],
      tagCloud: [],
      categoryStats: [],

      collectionStats: [],
      noteStats: [],

      learningStats: {
        completed: 0,
        inProgress: 0,
        overdue: 0
      },
      learningProgressList: [],

      activityStats: {
        weeklyScore: 0,
        weeklyChange: 0,
        monthlyCount: 0,
        monthlyNew: 0,
        streakDays: 0
      },

      activityCalendar: []
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo'])
  },
  async mounted() {
    await this.loadDashboardData()
    await this.loadCategoryStatistics()
    await this.loadTagCloud()
    await this.loadChartLibrary()
    this.$nextTick(() => {
      this.initCharts()
    })
  },
  beforeDestroy() {
    this.destroyCharts()
  },
  methods: {
    // 初始化学习进度图表
    initProgressChart() {
      if (this.progressChart) {
        this.progressChart.destroy();
      }
      
      const ctx = this.$refs.progressChart;
      if (!ctx) return;
      
      const isDigest = this.progressViewType === 'digest';
      
      this.progressChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: isDigest 
            ? ['未消化', '消化中', '已消化', '已放弃']
            : ['概念性', '程序性', '事实性', '元认知'],
          datasets: [{
            data: isDigest
              ? [this.processingData.datasets[0].data[0] || 40, 
                 this.processingData.datasets[0].data[1] || 25,
                 this.processingData.datasets[0].data[2] || 30,
                 this.processingData.datasets[0].data[3] || 5]
              : [35, 40, 25], // 模拟笔记类型数据
            backgroundColor: isDigest
              ? ['var(--danger-color)', 'var(--warning-color)', 'var(--success-color)', 'var(--text-placeholder)']
              : ['var(--success-color)', 'var(--warning-color)', 'var(--info-color)'],
            borderWidth: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '65%',
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  const label = context.label || '';
                  const value = context.raw || 0;
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = Math.round((value / total) * 100);
                  return `${label}: ${value} (${percentage}%)`;
                }
              }
            }
          }
        }
      });
      
      // 更新图例
      this.updateProgressLegend();
    },
    
    // 更新学习进度图表
    updateProgressChart() {
      this.initProgressChart();
    },
    
    // 更新图例
    updateProgressLegend() {
      const isDigest = this.progressViewType === 'digest';
      const data = isDigest
        ? this.processingData.datasets[0].data
        : [35, 40, 25];
      
      const labels = isDigest
        ? ['未消化', '消化中', '已消化', '已放弃']
        : ['概念性', '程序性', '事实性', '元认知'];
      
      const colors = isDigest
        ? ['var(--danger-color)', 'var(--warning-color)', 'var(--success-color)', 'var(--text-placeholder)']
        : ['var(--primary-color)', 'var(--success-color)', 'var(--warning-color)', 'var(--info-color)'];
      
      const total = data.reduce((a, b) => a + b, 0);
      
      this.progressLegend = labels.map((label, index) => ({
        label,
        value: data[index] || 0,
        percent: total > 0 ? Math.round(((data[index] || 0) / total) * 100) : 0,
        color: colors[index]
      }));
    },
    
    // 生成热力图数据
    generateHeatmapData(days = 7) {
      const data = [];
      const today = new Date();
      
      for (let i = days - 1; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        
        data.push({
          date: date.toISOString().split('T')[0],
          count: Math.floor(Math.random() * 15) // 模拟数据：0-14次活动
        });
      }
      
      return data;
    },
    
    // 加载活动热力图
    loadActivityHeatmap() {
      let days = 7;
      if (this.activityTimeRange === 'month') {
        days = 30;
      } else if (this.activityTimeRange === 'quarter') {
        days = 90;
      }
      
      this.heatmapData = this.generateHeatmapData(days);
    },
    
    // 获取热力图颜色类
    getHeatmapClass(count) {
      if (count === 0) return 'level-0';
      if (count <= 3) return 'level-1';
      if (count <= 6) return 'level-2';
      if (count <= 10) return 'level-3';
      return 'level-4';
    },
    
    // 显示活动详情
    showActivityDetail(day) {
      this.$message.info(`${day.date}: ${day.count} 次活动`);
    },
    
    // 加载 Chart.js 库
    async loadChartLibrary() {
      try {
        const chartModule = await import('chart.js/auto')
        Chart = chartModule.default || chartModule
      } catch (error) {
        console.error('加载图表库失败:', error)
      }
    },

    // 加载分类统计数据
    async loadCategoryStatistics() {
      try {
        const res = await getCategoryStatistics()
        const data = res?.data || res || {}
        this.categoryStats = Array.isArray(data) ? data : data?.list || []
        this.overviewStats.totalCategories = this.categoryStats.length

        // 更新分类图表数据
        if (this.categoryStats.length > 0) {
          this.categoryData.labels = this.categoryStats.slice(0, 10).map(c => c.name)
          this.categoryData.datasets[0].data = this.categoryStats.slice(0, 10).map(c => c.itemCount || 0)
          this.initCategoryChart()
        }
      } catch (error) {
        console.error('加载分类统计失败:', error)
        this.useFallbackCategoryData()
      }
    },

    // 加载标签云数据
    async loadTagCloud() {
      try {
        const res = await getTagCloud()
        const data = res?.data || res || {}
        this.tagCloud = Array.isArray(data) ? data : data?.list || []
      } catch (error) {
        console.error('加载标签云失败:', error)
        this.useFallbackTagCloud()
      }
    },

    // 使用备用分类数据
    useFallbackCategoryData() {
      this.categoryStats = [
        { name: '前端开发', itemCount: 45, percentage: 35, lastUpdated: '2024-01-20T10:30:00Z' },
        { name: '后端技术', itemCount: 32, percentage: 25, lastUpdated: '2024-01-19T14:20:00Z' },
        { name: '移动开发', itemCount: 18, percentage: 14, lastUpdated: '2024-01-18T09:15:00Z' },
        { name: 'DevOps', itemCount: 15, percentage: 12, lastUpdated: '2024-01-17T16:30:00Z' },
        { name: '人工智能', itemCount: 18, percentage: 14, lastUpdated: '2024-01-16T11:45:00Z' }
      ]
      this.overviewStats.totalCategories = this.categoryStats.length
      this.categoryData.labels = this.categoryStats.map(c => c.name)
      this.categoryData.datasets[0].data = this.categoryStats.map(c => c.itemCount || 0)
    },

    // 使用备用标签云数据
    useFallbackTagCloud() {
      this.tagCloud = [
        { name: 'Vue.js', count: 45 }, { name: 'React', count: 38 }, { name: 'JavaScript', count: 35 },
        { name: 'Node.js', count: 28 }, { name: 'TypeScript', count: 25 }, { name: 'CSS', count: 22 },
        { name: 'Python', count: 20 }, { name: 'Docker', count: 18 }, { name: 'Git', count: 15 },
        { name: 'Kubernetes', count: 12 }, { name: 'GraphQL', count: 10 }, { name: 'MongoDB', count: 8 }
      ]
    },

    // 初始化分类图表
    initCategoryChart() {
      if (!Chart || !this.$refs.categoryChart) return
      const ctx = this.$refs.categoryChart.getContext('2d')
      if (ctx) {
        if (this.categoryChart) this.categoryChart.destroy()
        this.categoryChart = new Chart(ctx, {
          type: 'doughnut',
          data: this.categoryData,
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
          }
        })
      }
    },

    // 加载仪表板数据
    async loadDashboardData() {
      this.pageLoading = true
      try {
        const res = await getUserStatistics()
        const data = res?.data || res || {}
        this.parseDashboardData(data)
      } catch (error) {
        console.error('加载仪表板数据失败:', error)
        this.$message.error('加载数据失败')
        this.useFallbackDashboardData()
      } finally {
        this.pageLoading = false
      }
    },

    // 解析仪表板数据
    parseDashboardData(data) {
      this.overviewStats = {
        totalCollections: data?.totalCollections ?? this.overviewStats.totalCollections,
        totalNotes: data?.totalNotes ?? this.overviewStats.totalNotes,
        totalTags: data?.totalTags ?? this.overviewStats.totalTags,
        processingItems: data?.processingItems ?? this.overviewStats.processingItems,
        collectionTrend: data?.collectionTrend ?? this.overviewStats.collectionTrend,
        noteTrend: data?.noteTrend ?? this.overviewStats.noteTrend,
        processingTrend: data?.processingTrend ?? this.overviewStats.processingTrend
      }

      this.topTags = Array.isArray(data?.topTags) ? data.topTags : this.getFallbackTopTags()
      this.collectionStats = Array.isArray(data?.collectionStats) ? data.collectionStats : this.getFallbackCollectionStats()
      this.noteStats = Array.isArray(data?.noteStats) ? data.noteStats : this.getFallbackNoteStats()

      this.activityStats = {
        weeklyScore: data?.weeklyScore ?? this.activityStats.weeklyScore,
        weeklyChange: data?.weeklyChange ?? this.activityStats.weeklyChange,
        monthlyCount: data?.monthlyCount ?? this.activityStats.monthlyCount,
        monthlyNew: data?.monthlyNew ?? this.activityStats.monthlyNew,
        streakDays: data?.streakDays ?? this.activityStats.streakDays
      }

      this.generateChartData()
      this.generateActivityCalendar()
    },

    // 使用备用数据
    useFallbackDashboardData() {
      this.overviewStats = {
        totalCollections: 128,
        totalNotes: 67,
        totalTags: 89,
        processingItems: 34,
        collectionTrend: 12.5,
        noteTrend: 8.3,
        processingTrend: -5.2
      }

      this.topTags = this.getFallbackTopTags()
      this.collectionStats = this.getFallbackCollectionStats()
      this.noteStats = this.getFallbackNoteStats()

      this.activityStats = {
        weeklyScore: 78,
        weeklyChange: 15,
        monthlyCount: 45,
        monthlyNew: 12,
        streakDays: 7
      }

      this.generateChartData()
      this.generateActivityCalendar()
    },

    // 获取备用热门标签
    getFallbackTopTags() {
      return [
        { name: '前端开发', count: 25 },
        { name: 'Vue.js', count: 22 },
        { name: 'JavaScript', count: 18 },
        { name: '学习笔记', count: 15 },
        { name: '技术文章', count: 12 },
        { name: 'React', count: 10 },
        { name: 'Node.js', count: 8 },
        { name: 'CSS', count: 7 },
        { name: '算法', count: 6 },
        { name: '数据库', count: 5 }
      ]
    },

    // 获取备用收藏统计
    getFallbackCollectionStats() {
      return [
        { name: '前端学习资料', itemCount: 25, processedCount: 20, processingRate: 80, lastUpdated: '2024-01-20T10:30:00Z' },
        { name: '后端技术文档', itemCount: 18, processedCount: 12, processingRate: 67, lastUpdated: '2024-01-19T14:20:00Z' },
        { name: '设计资源', itemCount: 15, processedCount: 15, processingRate: 100, lastUpdated: '2024-01-18T09:15:00Z' }
      ]
    },

    // 获取备用笔记统计
    getFallbackNoteStats() {
      return [
        { title: 'Vue3 Composition API 使用指南', wordCount: 1200, tagCount: 5, views: 1234, createTime: '2024-01-15T10:30:00Z' },
        { title: 'React Hooks 最佳实践', wordCount: 980, tagCount: 4, views: 856, createTime: '2024-01-12T16:45:00Z' },
        { title: 'JavaScript 异步编程详解', wordCount: 1500, tagCount: 6, views: 2103, createTime: '2024-01-10T09:20:00Z' }
      ]
    },

    // 生成图表数据
    generateChartData() {
      const now = new Date()
      const dataPoints = this.collectionChartType === 'daily' ? 7 :
                        this.collectionChartType === 'weekly' ? 12 : 6

      this.collectionData.labels = []
      this.collectionData.datasets[0].data = []

      for (let i = dataPoints - 1; i >= 0; i--) {
        const date = new Date(now)
        if (this.collectionChartType === 'daily') {
          date.setDate(date.getDate() - i)
        } else if (this.collectionChartType === 'weekly') {
          date.setDate(date.getDate() - i * 7)
        } else {
          date.setMonth(date.getMonth() - i)
        }

        this.collectionData.labels.push(
          date.toLocaleDateString('zh-CN', {
            month: 'short',
            day: 'numeric',
            ...(this.collectionChartType === 'monthly' && { year: 'numeric' })
          })
        )

        this.collectionData.datasets[0].data.push(
          Math.floor(Math.random() * 20) + 5
        )
      }

      if (this.collectionChart) {
        this.collectionChart.data = this.collectionData
        this.collectionChart.update()
      }
    },

    // 生成活跃度日历
    generateActivityCalendar() {
      const today = new Date()
      this.activityCalendar = []

      for (let i = 29; i >= 0; i--) {
        const date = new Date(today)
        date.setDate(date.getDate() - i)

        this.activityCalendar.push({
          date: date.toISOString().split('T')[0],
          day: date.getDate(),
          count: Math.floor(Math.random() * 8)
        })
      }
    },

    // 初始化图表
    initCharts() {
      if (!Chart) return

      this.destroyCharts()

      // 收藏趋势图
      const collectionCtx = this.$refs.collectionChart?.getContext('2d')
      if (collectionCtx) {
        this.collectionChart = new Chart(collectionCtx, {
          type: 'line',
          data: this.collectionData,
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
              y: {
                beginAtZero: true,
                grid: { color: 'rgba(0, 0, 0, 0.05)' }
              },
              x: { grid: { display: false } }
            }
          }
        })
      }

      // 类型分布图
      const typeCtx = this.$refs.typeChart?.getContext('2d')
      if (typeCtx) {
        this.typeChart = new Chart(typeCtx, {
          type: 'doughnut',
          data: this.typeData,
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'right' } }
          }
        })
      }

      // 加工状态图
      const processingCtx = this.$refs.processingChart?.getContext('2d')
      if (processingCtx) {
        this.processingChart = new Chart(processingCtx, {
          type: 'pie',
          data: this.processingData,
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom' } }
          }
        })
      }

      this.generateChartData()
      
      // 初始化学习进度图表
      this.initProgressChart();
      
      // 初始化活动热力图
      this.loadActivityHeatmap();
    },

    // 销毁图表
    destroyCharts() {
      if (this.collectionChart) { this.collectionChart.destroy(); this.collectionChart = null }
      if (this.typeChart) { this.typeChart.destroy(); this.typeChart = null }
      if (this.processingChart) { this.processingChart.destroy(); this.processingChart = null }
      if (this.progressChart) { this.progressChart.destroy(); this.progressChart = null }
    },

    // 更新收藏图表
    updateCollectionChart() {
      this.generateChartData()
    },

    // 处理日期范围变化
    handleDateRangeChange() {
      this.loadDashboardData()
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      if (isNaN(date.getTime())) return dateString
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },

    // 获取趋势样式类
    getTrendClass(value) {
      if (value > 0) return 'positive'
      if (value < 0) return 'negative'
      return 'neutral'
    },

    // 获取进度条状态
    getProgressStatus(rate) {
      if (rate >= 80) return 'success'
      if (rate >= 50) return 'warning'
      return 'exception'
    },

    // 获取标签进度百分比
    getTagProgress(tag) {
      if (!this.topTags || this.topTags.length === 0) return 0
      const maxCount = this.topTags[0]?.count || 1
      return ((tag?.count || 0) / maxCount) * 100
    },

    // 获取日历样式类
    getCalendarClass(count) {
      if (count > 5) return 'high'
      if (count > 2) return 'medium'
      if (count > 0) return 'low'
      return ''
    },

    // 获取标签云样式
    getTagCloudStyle(tag) {
      if (!this.tagCloud || this.tagCloud.length === 0) return {}
      const maxCount = Math.max(...this.tagCloud.map(t => t.count || 0))
      const minCount = Math.min(...this.tagCloud.map(t => t.count || 0))
      const ratio = maxCount === minCount ? 1 : ((tag.count || 0) - minCount) / (maxCount - minCount)
      const fontSize = 12 + ratio * 16 // 12px 到 28px
      const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#8E71D9']
      const colorIndex = Math.floor(ratio * (colors.length - 1))
      return {
        fontSize: `${fontSize}px`,
        color: colors[colorIndex],
        fontWeight: ratio > 0.5 ? 'bold' : 'normal',
        opacity: 0.7 + ratio * 0.3
      }
    },

    // 获取学习状态类型
    getStatusType(status) {
      const statusMap = { 'completed': 'success', 'in_progress': 'warning', 'overdue': 'danger' }
      return statusMap[status] || 'info'
    },

    // 获取学习状态文本
    getStatusText(status) {
      const textMap = { 'completed': '已完成', 'in_progress': '进行中', 'overdue': '已逾期' }
      return textMap[status] || '未知'
    }
  }
}
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-page);
}

.page-header {
  margin: 0 0 var(--space-6);
  border: none;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}

.header-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.breadcrumb {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.page-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 var(--space-6) var(--space-6);
}

/* 概览卡片 */
.overview-section {
  margin-bottom: var(--space-6);
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--space-4);
}

.overview-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  cursor: default;
  transition: all var(--transition-normal);
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.bg-primary-gradient { background: var(--gradient-primary); }
.bg-success-gradient { background: var(--gradient-secondary); }
.bg-info-gradient { background: var(--gradient-cool); }
.bg-warning-gradient { background: var(--gradient-warm); }
.bg-purple-gradient { background: linear-gradient(135deg, #8E71D9, #6B5B95); }
.bg-cyan-gradient { background: linear-gradient(135deg, #3DB39E, #26A69A); }

.card-content {
  flex: 1;
  min-width: 0;
}

.card-value {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.card-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: var(--space-1) 0;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-xs);
  font-weight: 500;
}

.card-trend.positive { color: var(--success-color); }
.card-trend.negative { color: var(--danger-color); }
.card-trend.neutral { color: var(--text-secondary); }

/* 图表区域 */
.charts-section {
  margin-bottom: var(--space-6);
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  margin-bottom: var(--space-5);
}

.chart-card {
  padding: var(--space-5);
}

.chart-card:hover {
  transform: none;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.chart-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.chart-container {
  height: 300px;
  position: relative;
}

/* 图表空状态 */
.chart-empty {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-placeholder);
  gap: var(--space-2);
}

.chart-empty i {
  font-size: 48px;
  opacity: 0.5;
}

/* 标签云 */
.tag-cloud-container {
  min-height: 300px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-4);
}

.tag-cloud-item {
  cursor: pointer;
  transition: all var(--transition-normal);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
}

.tag-cloud-item:hover {
  transform: scale(1.1);
  background-color: var(--primary-bg);
}

/* 百分比条 */
.percentage-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
}

.percentage-fill {
  height: 8px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width var(--transition-normal);
}

.percentage-text {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  min-width: 40px;
}

/* 标签列表 */
.tags-container {
  max-height: 300px;
  overflow-y: auto;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}

.tag-item:last-child {
  border-bottom: none;
}

.tag-rank {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-secondary);
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}

.tag-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
}

.tag-name {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-count {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  background-color: var(--bg-canvas);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.tag-bar {
  width: 80px;
  height: 6px;
  background-color: var(--bg-canvas);
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
}

.tag-progress {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width var(--transition-normal);
}

/* 详细统计 */
.details-section {
  padding: var(--space-6);
}

.stats-table {
  margin-top: var(--space-4);
}

/* 活跃度 */
.activity-stats {
  margin-top: var(--space-4);
}

.activity-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.activity-card {
  text-align: center;
  padding: var(--space-5);
  cursor: default;
}

.activity-card:hover {
  transform: none;
}

.activity-card h4 {
  margin: 0 0 var(--space-3) 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  font-weight: 500;
}

.activity-score {
  font-size: var(--font-size-4xl);
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: var(--space-2);
}

.activity-desc {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.activity-calendar h4 {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: var(--space-1);
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background-color: var(--bg-canvas);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  transition: all var(--transition-normal);
}

.calendar-day.low {
  background-color: var(--primary-bg);
  color: var(--primary-color);
}

.calendar-day.medium {
  background-color: var(--primary-light);
  color: white;
}

.calendar-day.high {
  background-color: var(--primary-color);
  color: white;
}

/* 学习进度 */
.learning-stats {
  margin-top: var(--space-4);
}

.learning-overview {
  display: flex;
  gap: var(--space-8);
  margin-bottom: var(--space-6);
  padding: var(--space-5);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-lg);
}

.learning-circle {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.learning-circle-label {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.learning-title {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.learning-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.learning-cards {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  align-items: center;
}

.learning-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background-color: var(--bg-page);
  border-radius: var(--radius-md);
}

.learning-card i {
  font-size: 28px;
  color: var(--primary-color);
}

.learning-card-content {
  display: flex;
  flex-direction: column;
}

.learning-card-value {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
}

.learning-card-label {
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.learning-progress-list h4 {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 992px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: var(--space-4);
  }

  .overview-cards {
    grid-template-columns: 1fr;
  }

  .activity-cards {
    grid-template-columns: 1fr;
  }

  .main-content {
    padding: 0 var(--space-4) var(--space-4);
  }
}

@media (max-width: 576px) {
  .page-header {
    padding: var(--space-4);
  }

  .chart-card,
  .details-section {
    padding: var(--space-4);
  }

  .page-title {
    font-size: var(--font-size-xl);
  }

  .calendar-grid {
    gap: 2px;
  }
}

/* 学习进度图表 */
.progress-legend {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-base);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  color: var(--text-secondary);
}

.legend-value {
  font-weight: 600;
  color: var(--text-primary);
}

/* 活动热力图 */
.heatmap-container {
  padding: var(--space-4) 0;
}

.heatmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 16px);
  gap: 4px;
  justify-content: center;
}

.heatmap-cell {
  width: 16px;
  height: 16px;
}

.heatmap-day {
  width: 100%;
  height: 100%;
  border-radius: 3px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.heatmap-day:hover {
  transform: scale(1.3);
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
}

.heatmap-level {
  border-radius: 3px;
}

.level-0 { background: #ebedf0; }
.level-1 { background: #9be9a8; }
.level-2 { background: #40c463; }
.level-3 { background: #30a14e; }
.level-4 { background: #216e39; }

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-4);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.heatmap-scale {
  display: flex;
  gap: 3px;
}

.heatmap-scale .heatmap-level {
  width: 12px;
  height: 12px;
}

.chart-controls {
  display: flex;
  gap: var(--space-2);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}
</style>
