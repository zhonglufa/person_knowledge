<template>
  <div class="creation-workspace-page">
    <div class="creation-center">
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item>创作中心</el-breadcrumb-item>
                <el-breadcrumb-item>创作工作台</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <h1 class="page-title">创作工作台</h1>
            <p class="page-subtitle">围绕收藏项加工与知识沉淀组织你的今日任务。</p>
          </div>
          <div class="header-right">
            <div class="header-stats">
              <div class="stat-item" v-for="stat in currentStats" :key="stat.key">
                <i :class="stat.icon" :style="{ color: stat.color }"></i>
                <span class="stat-number">{{ stat.value }}</span>
                <span class="stat-label">{{ stat.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="workspace-panels">
        <el-row :gutter="16">
          <el-col :xs="24" :lg="16">
            <div class="workspace-card">
              <div class="card-header">
                <h3>今日待加工</h3>
                <el-button type="primary" text @click="goToRoute('/creation/processing')">查看全部</el-button>
              </div>
              <div class="task-list">
                <div class="task-item" v-for="task in workspaceTasks" :key="task.title">
                  <div class="task-main">
                    <div class="task-title">{{ task.title }}</div>
                    <div class="task-meta">{{ task.meta }}</div>
                  </div>
                  <el-tag size="small" :type="task.type">{{ task.status }}</el-tag>
                </div>
              </div>
            </div>

            <div class="workspace-card">
              <div class="card-header">
                <h3>继续沉淀</h3>
                <el-button type="primary" text @click="goToRoute('/creation/notes')">我的笔记</el-button>
              </div>
              <div class="task-list">
                <div class="task-item" v-for="note in recentNotes" :key="note.title">
                  <div class="task-main">
                    <div class="task-title">{{ note.title }}</div>
                    <div class="task-meta">{{ note.meta }}</div>
                  </div>
                  <el-tag size="small" :type="note.type">{{ note.status }}</el-tag>
                </div>
              </div>
            </div>
          </el-col>

          <el-col :xs="24" :lg="8">
            <div class="workspace-card compact">
              <div class="card-header">
                <h3>快捷动作</h3>
              </div>
              <div class="quick-grid">
                <div class="quick-item" @click="goToRoute('/creation/processing')">
                  <i class="fas fa-seedling"></i>
                  <span>开始加工</span>
                </div>
                <div class="quick-item" @click="goToRoute('/creation/notes')">
                  <i class="fas fa-note-sticky"></i>
                  <span>查看笔记</span>
                </div>
                <div class="quick-item" @click="goToRoute('/creation/drafts')">
                  <i class="fas fa-file-alt"></i>
                  <span>继续草稿</span>
                </div>
                <div class="quick-item" @click="handleQuickAction('quick-capture')">
                  <i class="fas fa-bolt"></i>
                  <span>快速收藏</span>
                </div>
              </div>
            </div>

            <div class="workspace-card compact">
              <div class="card-header">
                <h3>今日提醒</h3>
              </div>
              <div class="reminder-list">
                <div class="reminder-item" v-for="reminder in reminders" :key="reminder.title">
                  <div class="task-title">{{ reminder.title }}</div>
                  <div class="task-meta">{{ reminder.meta }}</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CreationWorkspace',
  data() {
    return {
      workspaceTasks: [
        { title: '整理这周的前端架构文章', meta: '3 条内容待处理', status: '待加工', type: 'warning' },
        { title: '补充 Vue 路由设计笔记', meta: '已有摘要待沉淀', status: '加工中', type: 'success' },
        { title: '复盘组件拆分方案', meta: '提醒今天完成', status: '今日提醒', type: 'danger' }
      ],
      recentNotes: [
        { title: '路由结构重组方案', meta: '30 分钟前更新', status: '草稿', type: 'info' },
        { title: '收藏项加工流程设计', meta: '今天创建', status: '待完善', type: 'warning' }
      ],
      reminders: [
        { title: '完成 2 条待加工内容', meta: '今天 18:00 前' },
        { title: '继续补全笔记草稿', meta: '今天 21:00 前' }
      ],
      currentStats: [
        { key: 'processing', icon: 'fas fa-seedling', label: '待加工', value: 12, color: 'var(--warning-color)' },
        { key: 'draft', icon: 'fas fa-file-alt', label: '草稿', value: 3, color: 'var(--primary-color)' }
      ]
    }
  },
  methods: {
    goToRoute(route) {
      this.$router.push(route)
    },
    handleQuickAction(action) {
      const messages = {
        'quick-capture': '快速收藏功能'
      }
      this.$message.info(messages[action] || '功能开发中')
    }
  }
}
</script>

<style scoped>
.creation-center {
  padding: var(--space-6);
  height: 100%;
  overflow-y: auto;
}
.page-header {
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-base);
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}
.header-left { flex: 1; }
.breadcrumb { margin-bottom: var(--space-2); }
.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}
.page-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0;
}
.header-stats {
  display: flex;
  gap: var(--space-4);
}
.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
}
.stat-number { font-weight: 700; color: var(--text-primary); }
.stat-label { color: var(--text-secondary); font-size: var(--font-size-sm); }
.workspace-panels { margin-top: var(--space-4); }
.workspace-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--space-4);
}
.workspace-card.compact { padding: var(--space-4); }
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}
.card-header h3 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}
.task-list,
.reminder-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.task-item,
.reminder-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-light);
}
.task-item:last-child,
.reminder-item:last-child { border-bottom: none; }
.task-main { flex: 1; }
.task-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}
.task-meta {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
}
.quick-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.quick-item i {
  font-size: var(--font-size-xl);
  color: var(--primary-color);
}
@media (max-width: 992px) {
  .header-content { flex-direction: column; }
  .header-stats { flex-wrap: wrap; }
}
</style>
