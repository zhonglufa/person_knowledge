<template>
  <div class="settings-form">
    <el-form
      ref="settingsFormRef"
      :model="settingsForm"
      label-width="120px"
      label-position="right"
      class="settings-form-inner"
    >
      <!-- 主题设置 -->
      <el-form-item label="主题模式">
        <el-radio-group v-model="settingsForm.theme" @change="handleThemeChange">
          <el-radio-button label="light">
            <i class="fas fa-sun"></i> 浅色
          </el-radio-button>
          <el-radio-button label="dark">
            <i class="fas fa-moon"></i> 深色
          </el-radio-button>
          <el-radio-button label="auto">
            <i class="fas fa-desktop"></i> 跟随系统
          </el-radio-button>
        </el-radio-group>
        <p class="form-tip">选择应用的外观主题，跟随系统将根据操作系统设置自动切换</p>
      </el-form-item>

      <!-- 显示模式 -->
      <el-form-item label="显示模式">
        <el-radio-group v-model="settingsForm.displayMode" @change="handleDisplayModeChange">
          <el-radio-button label="grid">
            <i class="fas fa-th-large"></i> 网格
          </el-radio-button>
          <el-radio-button label="list">
            <i class="fas fa-list"></i> 列表
          </el-radio-button>
        </el-radio-group>
        <p class="form-tip">选择内容展示的布局方式</p>
      </el-form-item>

      <!-- 每页显示数量 -->
      <el-form-item label="每页数量">
        <el-select
          v-model="settingsForm.pageSize"
          placeholder="请选择每页显示数量"
          @change="handlePageSizeChange"
        >
          <el-option label="10条" :value="10"></el-option>
          <el-option label="20条" :value="20"></el-option>
          <el-option label="50条" :value="50"></el-option>
          <el-option label="100条" :value="100"></el-option>
        </el-select>
        <p class="form-tip">设置列表和网格模式下每页显示的内容数量</p>
      </el-form-item>

      <el-divider></el-divider>

      <!-- 通知偏好设置 -->
      <el-form-item label="通知设置">
        <div class="notification-preferences">
          <el-checkbox
            v-model="settingsForm.notifyComment"
            @change="handleNotificationChange"
          >
            <i class="fas fa-comment"></i> 评论通知
            <span class="checkbox-desc">当有人评论我的内容时通知</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.notifyLike"
            @change="handleNotificationChange"
          >
            <i class="fas fa-heart"></i> 点赞通知
            <span class="checkbox-desc">当有人点赞我的内容时通知</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.notifyCollect"
            @change="handleNotificationChange"
          >
            <i class="fas fa-star"></i> 收藏通知
            <span class="checkbox-desc">当有人收藏我的内容时通知</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.notifySystem"
            @change="handleNotificationChange"
          >
            <i class="fas fa-bullhorn"></i> 系统公告
            <span class="checkbox-desc">接收系统公告和重要通知</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.notifyActivity"
            @change="handleNotificationChange"
          >
            <i class="fas fa-gift"></i> 活动通知
            <span class="checkbox-desc">接收活动信息和优惠通知</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.notifyEmail"
            @change="handleNotificationChange"
          >
            <i class="fas fa-envelope"></i> 邮件通知
            <span class="checkbox-desc">同时发送邮件通知</span>
          </el-checkbox>
        </div>
      </el-form-item>

      <el-divider></el-divider>

      <!-- 隐私设置 -->
      <el-form-item label="隐私设置">
        <div class="privacy-preferences">
          <el-checkbox
            v-model="settingsForm.showProfile"
            @change="handlePrivacyChange"
          >
            公开个人主页
            <span class="checkbox-desc">允许其他用户查看我的个人资料</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.showCollections"
            @change="handlePrivacyChange"
          >
            公开收藏集
            <span class="checkbox-desc">允许其他用户查看我的收藏集</span>
          </el-checkbox>

          <el-checkbox
            v-model="settingsForm.showActivity"
            @change="handlePrivacyChange"
          >
            公开动态
            <span class="checkbox-desc">允许其他用户查看我的最近活动</span>
          </el-checkbox>
        </div>
      </el-form-item>
    </el-form>

    <!-- 保存按钮 -->
    <div class="form-footer">
      <el-button
        type="primary"
        :loading="saving"
        @click="saveSettings"
      >
        <i class="el-icon-check"></i> 保存设置
      </el-button>
      <el-button @click="resetSettings">
        <i class="el-icon-refresh"></i> 恢复默认
      </el-button>
    </div>
  </div>
</template>

<script>
import request from '@/api/axios'

export default {
  name: 'SettingsForm',

  data() {
    return {
      saving: false,

      // 设置表单
      settingsForm: {
        // 主题: light / dark / auto
        theme: 'light',
        // 显示模式: grid / list
        displayMode: 'grid',
        // 每页数量
        pageSize: 20,

        // 通知偏好
        notifyComment: true,
        notifyLike: true,
        notifyCollect: true,
        notifySystem: true,
        notifyActivity: false,
        notifyEmail: false,

        // 隐私设置
        showProfile: true,
        showCollections: true,
        showActivity: false
      },

      // 默认设置
      defaultSettings: {
        theme: 'light',
        displayMode: 'grid',
        pageSize: 20,
        notifyComment: true,
        notifyLike: true,
        notifyCollect: true,
        notifySystem: true,
        notifyActivity: false,
        notifyEmail: false,
        showProfile: true,
        showCollections: true,
        showActivity: false
      }
    }
  },

  created() {
    this.loadSettings()
  },

  methods: {
    /**
     * 加载用户设置
     */
    async loadSettings() {
      try {
        // 优先从本地缓存读取
        const cached = localStorage.getItem('userSettings')
        if (cached) {
          const parsed = JSON.parse(cached)
          this.settingsForm = { ...this.settingsForm, ...parsed }
        }

        // 从后端获取最新设置
        const response = await request({
          url: '/user/settings',
          method: 'get'
        })

        if (response.code === 200) {
          this.settingsForm = { ...this.settingsForm, ...response.data }
          // 更新缓存
          localStorage.setItem('userSettings', JSON.stringify(this.settingsForm))
          // 应用主题
          this.applyTheme()
        }
      } catch (error) {
        console.error('加载设置失败:', error)
        // 使用本地缓存或默认值
        if (!localStorage.getItem('userSettings')) {
          this.settingsForm = { ...this.defaultSettings }
        }
      }
    },

    /**
     * 保存设置
     */
    async saveSettings() {
      this.saving = true
      try {
        const response = await request({
          url: '/user/settings',
          method: 'put',
          data: this.settingsForm
        })

        if (response.code === 200) {
          // 保存到本地缓存
          localStorage.setItem('userSettings', JSON.stringify(this.settingsForm))

          // 应用主题
          this.applyTheme()

          // 发送事件通知其他组件
          this.$emit('settings-saved', this.settingsForm)

          this.$message.success('设置保存成功')
        } else {
          this.$message.error(response.message || '保存失败')
        }
      } catch (error) {
        console.error('保存设置失败:', error)
        this.$message.error('保存设置失败，请稍后重试')
      } finally {
        this.saving = false
      }
    },

    /**
     * 恢复默认设置
     */
    resetSettings() {
      this.$confirm('确定要恢复默认设置吗？', '确认', {
        type: 'warning'
      }).then(() => {
        this.settingsForm = { ...this.defaultSettings }
        this.applyTheme()
        this.$message.success('已恢复默认设置')
      }).catch(() => {})
    },

    /**
     * 应用主题
     */
    applyTheme() {
      const theme = this.settingsForm.theme

      if (theme === 'auto') {
        // 跟随系统
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
      } else {
        document.documentElement.setAttribute('data-theme', theme)
      }
    },

    /**
     * 主题变化处理
     */
    handleThemeChange() {
      this.applyTheme()
    },

    /**
     * 显示模式变化处理
     */
    handleDisplayModeChange() {
      this.$emit('display-mode-change', this.settingsForm.displayMode)
    },

    /**
     * 每页数量变化处理
     */
    handlePageSizeChange() {
      this.$emit('page-size-change', this.settingsForm.pageSize)
    },

    /**
     * 通知偏好变化处理
     */
    handleNotificationChange() {
      // 自动保存通知偏好
      this.autoSave()
    },

    /**
     * 隐私设置变化处理
     */
    handlePrivacyChange() {
      this.autoSave()
    },

    /**
     * 自动保存（防抖）
     */
    autoSave() {
      if (this.autoSaveTimer) {
        clearTimeout(this.autoSaveTimer)
      }
      this.autoSaveTimer = setTimeout(() => {
        localStorage.setItem('userSettings', JSON.stringify(this.settingsForm))
      }, 500)
    }
  }
}
</script>

<style scoped>
.settings-form {
  max-width: 720px;
}

.settings-form-inner {
  background-color: var(--bg-primary);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* ========== 表单项 ========== */
.form-tip {
  margin: var(--space-2) 0 0;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  line-height: 1.5;
}

/* ========== 分隔线 ========== */
.el-divider {
  margin: var(--space-6) 0;
}

/* ========== 通知偏好 ========== */
.notification-preferences,
.privacy-preferences {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.notification-preferences :deep(.el-checkbox),
.privacy-preferences :deep(.el-checkbox) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-1);
  margin-right: 0;
}

.notification-preferences :deep(.el-checkbox__label),
.privacy-preferences :deep(.el-checkbox__label) {
  padding-left: var(--space-2);
  font-weight: 500;
  color: var(--text-primary);
}

.checkbox-desc {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  font-weight: normal;
  margin-top: var(--space-1);
}

.notification-preferences :deep(.el-checkbox i),
.privacy-preferences :deep(.el-checkbox i) {
  margin-right: var(--space-2);
  color: var(--primary-color);
}

/* ========== 主题按钮 ========== */
.settings-form :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.settings-form :deep(.el-radio-button__inner i) {
  font-size: var(--font-size-base);
}

/* ========== 底部按钮 ========== */
.form-footer {
  display: flex;
  justify-content: flex-start;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .settings-form-inner {
    padding: var(--space-4);
  }

  .settings-form {
    max-width: 100%;
  }

  .form-footer {
    flex-direction: column;
  }

  .form-footer .el-button {
    width: 100%;
  }
}
</style>
