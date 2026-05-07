<template>
  <div v-loading="pageLoading" class="page-wrapper">
    <!-- 页面主内容 -->
    <main class="page-main">
      <div class="page-container">
        <!-- 页面标题区 -->
        <div class="page-header-section">
          <div class="page-header-content">
            <div class="page-header-left">
              <div class="page-breadcrumb">
                <el-breadcrumb separator="/">
                  <el-breadcrumb-item :to="{ path: '/personal/center' }">个人中心</el-breadcrumb-item>
                  <el-breadcrumb-item>账户设置</el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              <h1 class="page-title">账户设置</h1>
              <p class="page-subtitle">管理您的账户信息和系统偏好设置</p>
            </div>
          </div>
        </div>

        <!-- 设置内容区域 -->
        <div class="settings-layout">
          <!-- 侧边导航 -->
          <div class="sidebar-nav">
            <el-menu
              :default-active="activeSetting"
              class="settings-menu"
              @select="handleSettingSelect"
            >
              <el-menu-item index="account">
                <i class="fas fa-lock"></i>
                <span slot="title">账户安全</span>
              </el-menu-item>
              <el-menu-item index="notification">
                <i class="fas fa-bell"></i>
                <span slot="title">通知设置</span>
              </el-menu-item>
              <el-menu-item index="appearance">
                <i class="fas fa-palette"></i>
                <span slot="title">外观设置</span>
              </el-menu-item>
              <el-menu-item index="privacy">
                <i class="fas fa-shield-alt"></i>
                <span slot="title">隐私设置</span>
              </el-menu-item>
              <el-menu-item index="personal">
                <i class="fas fa-sliders-h"></i>
                <span slot="title">个性化设置</span>
              </el-menu-item>
            </el-menu>
          </div>

          <!-- 设置面板 -->
          <div class="settings-panel">
            <!-- 账户安全设置 -->
            <div v-if="activeSetting === 'account'" class="setting-section content-card">
              <div class="content-card-header">
                <h3 class="content-card-title">
                  <i class="fas fa-lock"></i>
                  账户安全
                </h3>
              </div>
              <div class="content-card-body">
                <el-form label-width="120px" label-position="left">
                  <el-form-item label="当前密码">
                    <el-input
                      v-model="securityForm.currentPassword"
                      type="password"
                      placeholder="请输入当前密码"
                      show-password
                    ></el-input>
                  </el-form-item>

                  <el-form-item label="新密码">
                    <el-input
                      v-model="securityForm.newPassword"
                      type="password"
                      placeholder="请输入新密码（至少8位）"
                      show-password
                    ></el-input>
                  </el-form-item>

                  <el-form-item label="确认密码">
                    <el-input
                      v-model="securityForm.confirmPassword"
                      type="password"
                      placeholder="请再次输入新密码"
                      show-password
                    ></el-input>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="changePassword" :loading="passwordLoading">修改密码</el-button>
                  </el-form-item>
                </el-form>

                <div class="security-info">
                  <h3><i class="fas fa-info-circle"></i> 安全提示</h3>
                  <ul>
                    <li>密码长度至少8位</li>
                    <li>建议包含大小写字母、数字和特殊字符</li>
                    <li>定期更换密码以保证账户安全</li>
                    <li>不要使用与其他网站相同的密码</li>
                  </ul>
                </div>
              </div>
            </div>

            <!-- 通知设置 -->
            <div v-if="activeSetting === 'notification'" class="setting-section content-card">
              <div class="content-card-header">
                <h3 class="content-card-title">
                  <i class="fas fa-bell"></i>
                  通知设置
                </h3>
              </div>
              <div class="content-card-body">
                <el-form label-width="200px" label-position="left">
                  <el-form-item label="系统通知">
                    <el-switch
                      v-model="notificationForm.system"
                      active-text="开启"
                      inactive-text="关闭"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item label="邮件通知">
                    <el-switch
                      v-model="notificationForm.email"
                      active-text="开启"
                      inactive-text="关闭"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item label="收藏更新提醒">
                    <el-switch
                      v-model="notificationForm.collectionUpdate"
                      active-text="开启"
                      inactive-text="关闭"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item label="加工完成提醒">
                    <el-switch
                      v-model="notificationForm.processingComplete"
                      active-text="开启"
                      inactive-text="关闭"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="saveNotifications" :loading="saveLoading">保存设置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>

            <!-- 隐私设置 -->
            <div v-if="activeSetting === 'privacy'" class="setting-section content-card">
              <div class="content-card-header">
                <h3 class="content-card-title">
                  <i class="fas fa-shield-alt"></i>
                  隐私设置
                </h3>
              </div>
              <div class="content-card-body">
                <el-form label-width="200px" label-position="left">
                  <el-form-item label="公开个人资料">
                    <el-switch
                      v-model="privacyForm.publicProfile"
                      active-text="公开"
                      inactive-text="私密"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item label="允许搜索到我的内容">
                    <el-switch
                      v-model="privacyForm.allowSearch"
                      active-text="允许"
                      inactive-text="禁止"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item label="显示收藏统计">
                    <el-switch
                      v-model="privacyForm.showStats"
                      active-text="显示"
                      inactive-text="隐藏"
                    ></el-switch>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="savePrivacy" :loading="saveLoading">保存设置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>

            <!-- 外观设置 -->
            <div v-if="activeSetting === 'appearance'" class="setting-section content-card">
              <div class="content-card-header">
                <h3 class="content-card-title">
                  <i class="fas fa-palette"></i>
                  外观设置
                </h3>
              </div>
              <div class="content-card-body">
                <el-form label-width="120px" label-position="left">
                  <el-form-item label="主题模式">
                    <el-select v-model="appearanceForm.theme" placeholder="选择主题" style="width: 100%">
                      <el-option label="浅色模式" value="light"></el-option>
                      <el-option label="深色模式" value="dark"></el-option>
                      <el-option label="跟随系统" value="auto"></el-option>
                    </el-select>
                  </el-form-item>

                  <el-form-item label="布局密度">
                    <el-radio-group v-model="appearanceForm.density">
                      <el-radio label="compact">紧凑</el-radio>
                      <el-radio label="default">默认</el-radio>
                      <el-radio label="comfortable">宽松</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item label="字体大小">
                    <el-slider
                      v-model="appearanceForm.fontSize"
                      :min="12"
                      :max="20"
                      :step="1"
                      show-stops
                      :format-tooltip="formatFontSize"
                    ></el-slider>
                  </el-form-item>

                  <el-form-item>
                    <el-button type="primary" @click="saveAppearance" :loading="saveLoading">保存设置</el-button>
                    <el-button @click="resetAppearance">恢复默认</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </div>

            <!-- 个性化设置 -->
            <div v-if="activeSetting === 'personal'" class="setting-section content-card">
              <div class="content-card-header">
                <h3 class="content-card-title">
                  <i class="fas fa-sliders-h"></i>
                  个性化设置
                </h3>
              </div>
              <div class="content-card-body">
                <div v-loading="settingsLoading">
                  <settings-form
                    @settings-saved="handlePersonalSettingsSaved"
                    @display-mode-change="handleDisplayModeChange"
                    @page-size-change="handlePageSizeChange"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { changePassword, getUserSettings, updateUserSettings } from '@/api/user'
import SettingsForm from '@/components/common/SettingsForm.vue'

export default {
  name: 'Settings',
  components: {
    SettingsForm
  },
  data() {
    return {
      saveLoading: false,
      passwordLoading: false,
      activeSetting: 'account',
      securityForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      notificationForm: {
        system: true,
        email: true,
        collectionUpdate: true,
        processingComplete: false
      },
      privacyForm: {
        publicProfile: false,
        allowSearch: true,
        showStats: true
      },
      appearanceForm: {
        theme: 'light',
        density: 'default',
        fontSize: 14
      },
      // 个性化设置 - 从后端API获取
      personalSettings: null,
      settingsLoading: false
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo'])
  },
  created() {
    this.loadLocalSettings()
    this.loadPersonalSettings()
  },
  methods: {

    // 加载设置（优先从服务器获取，失败则使用本地缓存）
    async loadLocalSettings() {
      try {
        // 优先从服务器获取设置
        try {
          const response = await getUserSettings()
          if (response?.code === 200 && response.data) {
            const serverSettings = response.data
            if (serverSettings.notification) {
              this.notificationForm = { ...this.notificationForm, ...serverSettings.notification }
            }
            if (serverSettings.privacy) {
              this.privacyForm = { ...this.privacyForm, ...serverSettings.privacy }
            }
            if (serverSettings.appearance) {
              this.appearanceForm = { ...this.appearanceForm, ...serverSettings.appearance }
            }
            // 缓存到本地
            localStorage.setItem('userSettings', JSON.stringify(serverSettings))
          }
        } catch (e) {
          console.warn('从服务器加载设置失败，使用本地缓存:', e)
        }

        // 服务器获取失败，使用本地缓存
        const notificationSettings = localStorage.getItem('notificationSettings')
        if (notificationSettings) {
          this.notificationForm = { ...this.notificationForm, ...JSON.parse(notificationSettings) }
        }

        const privacySettings = localStorage.getItem('privacySettings')
        if (privacySettings) {
          this.privacyForm = { ...this.privacyForm, ...JSON.parse(privacySettings) }
        }

        const appearanceSettings = localStorage.getItem('appearanceSettings')
        if (appearanceSettings) {
          this.appearanceForm = { ...this.appearanceForm, ...JSON.parse(appearanceSettings) }
        }
      } catch (error) {
        console.error('加载设置失败:', error)
      }

      // 应用主题和字体设置
      this.applyTheme()
      this.applyFontSize()
    },

    // ==================== 个性化设置相关 ====================
    /**
     * 从后端API加载个性化设置
     */
    async loadPersonalSettings() {
      this.settingsLoading = true
      try {
        const response = await getUserSettings()
        if (response?.code === 200 && response.data) {
          this.personalSettings = response.data
          // 将后端设置同步到本地缓存
          localStorage.setItem('userSettings', JSON.stringify(this.personalSettings))
        }
      } catch (error) {
        console.error('加载个性化设置失败:', error)
        // 如果后端获取失败，尝试从本地缓存读取
        const cached = localStorage.getItem('userSettings')
        if (cached) {
          this.personalSettings = JSON.parse(cached)
        }
      } finally {
        this.settingsLoading = false
      }
    },

    /**
     * 处理个性化设置保存事件
     */
    handlePersonalSettingsSaved(settings) {
      this.personalSettings = settings
      // 同步更新外观设置中的主题
      if (settings.theme) {
        this.appearanceForm.theme = settings.theme
        this.applyTheme()
      }
    },

    /**
     * 处理显示模式变化
     */
    handleDisplayModeChange(mode) {
      console.log('显示模式变更为:', mode)
      // 可以在这里触发全局显示模式更新
      this.$emit('display-mode-change', mode)
    },

    /**
     * 处理每页数量变化
     */
    handlePageSizeChange(size) {
      console.log('每页数量变更为:', size)
      // 可以在这里触发全局分页设置更新
      this.$emit('page-size-change', size)
    },

    // ==================== 通用处理方法 ====================
    // 处理设置选择
    handleSettingSelect(key) {
      this.activeSetting = key
    },

    // 修改密码
    async changePassword() {
      if (!this.securityForm?.currentPassword) {
        this.$message.warning('请输入当前密码')
        return
      }

      if (!this.securityForm?.newPassword) {
        this.$message.warning('请输入新密码')
        return
      }

      if (this.securityForm.newPassword !== this.securityForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return
      }

      if (this.securityForm.newPassword.length < 8) {
        this.$message.error('密码长度至少8位')
        return
      }

      this.passwordLoading = true
      try {
        await changePassword({
          oldPassword: this.securityForm.currentPassword,
          newPassword: this.securityForm.newPassword
        })
        this.$message.success('密码修改成功，请重新登录')
        this.securityForm = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        await this.$store.dispatch('user/logout')
        this.$router.push('/login')
      } catch (error) {
        console.error('密码修改失败:', error)
        this.$message.error('密码修改失败，请检查当前密码是否正确')
      } finally {
        this.passwordLoading = false
      }
    },

    // 保存通知设置
    async saveNotifications() {
      this.saveLoading = true
      try {
        // 同时保存到服务器和本地
        await updateUserSettings({ notifyPreferences: this.notificationForm })
        localStorage.setItem('notificationSettings', JSON.stringify(this.notificationForm))
        this.$message.success('通知设置保存成功')
      } catch (error) {
        console.error('保存通知设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },

    // 保存隐私设置
    async savePrivacy() {
      this.saveLoading = true
      try {
        // 同时保存到服务器和本地
        await updateUserSettings({ privacy: this.privacyForm })
        localStorage.setItem('privacySettings', JSON.stringify(this.privacyForm))
        this.$message.success('隐私设置保存成功')
      } catch (error) {
        console.error('保存隐私设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },

    // 保存外观设置
    async saveAppearance() {
      this.saveLoading = true
      try {
        // 构建完整的设置数据
        const settingsData = {
          theme: this.appearanceForm.theme,
          displayMode: this.appearanceForm.displayMode || 'grid',
          density: this.appearanceForm.density,
          fontSize: this.appearanceForm.fontSize
        }
        
        // 同时保存到服务器和本地
        await updateUserSettings(settingsData)
        localStorage.setItem('appearanceSettings', JSON.stringify(this.appearanceForm))
        
        // 实时应用主题和字体设置
        this.applyTheme()
        this.applyFontSize()
        
        this.$message.success('外观设置保存成功')
      } catch (error) {
        console.error('保存外观设置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.saveLoading = false
      }
    },

    // 重置外观设置
    resetAppearance() {
      this.appearanceForm = {
        theme: 'light',
        density: 'default',
        fontSize: 14
      }
      this.applyTheme()
      this.applyFontSize()
    },

    // 应用主题
    applyTheme() {
      const html = document.documentElement
      if (this.appearanceForm?.theme === 'dark') {
        html.classList.add('dark')
      } else {
        html.classList.remove('dark')
      }
    },

    // 应用字体大小
    applyFontSize() {
      const root = document.documentElement
      if (this.appearanceForm?.fontSize) {
        root.style.fontSize = `${this.appearanceForm.fontSize}px`
      }
    },

    // 格式化字体大小提示
    formatFontSize(val) {
      return `${val}px`
    }
  }
}
</script>

<style scoped>
/* 页面标题区 */
.page-header-section {
  padding: var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

/* 设置布局 */
.settings-layout {
  display: flex;
  gap: var(--space-6);
}

.sidebar-nav {
  width: 240px;
  flex-shrink: 0;
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-base);
  padding: var(--space-4);
  position: sticky;
  top: 80px;
  height: fit-content;
}

.settings-menu {
  border-right: none;
}

.settings-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: var(--font-size-base);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-1);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.settings-menu .el-menu-item i {
  margin: 0;
  width: 16px;
  text-align: center;
}

.settings-menu .el-menu-item:hover {
  background-color: var(--primary-bg);
}

.settings-menu .el-menu-item.is-active {
  background-color: var(--primary-color);
  color: white;
}

.settings-panel {
  flex: 1;
  min-width: 0;
}

/* 设置区块 */
.setting-section {
  margin-bottom: var(--space-6);
}

.setting-section:last-child {
  margin-bottom: 0;
}

/* 卡片样式 */
.content-card {
  background: var(--bg-canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.content-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-base);
}

/* 卡片标题 */
.content-card-header {
  display: flex;
  align-items: center;
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background-color: var(--bg-canvas);
}

.content-card-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.content-card-title i {
  color: var(--primary-color);
}

.content-card-body {
  padding: var(--space-6);
}

.content-card-body .el-form-item {
  margin-bottom: var(--space-4);
}

.content-card-body .el-form-item:last-child {
  margin-bottom: 0;
}

/* 表单操作按钮区域 */
.form-actions {
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--border-light);
}

/* 安全信息 */
.security-info {
  margin-top: var(--space-8);
  padding: var(--space-5);
  background-color: var(--primary-bg);
  border-radius: var(--radius-lg);
  border-left: 4px solid var(--primary-color);
}

.security-info h3 {
  margin: 0 0 var(--space-3) 0;
  font-size: var(--font-size-base);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.security-info ul {
  margin: 0;
  padding-left: var(--space-5);
}

.security-info li {
  margin-bottom: var(--space-2);
  color: var(--text-regular);
  font-size: var(--font-size-sm);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .settings-layout {
    flex-direction: column;
  }

  .sidebar-nav {
    width: 100%;
    position: static;
  }

  .settings-menu {
    display: flex;
    flex-wrap: wrap;
    border-bottom: 1px solid var(--border-light);
    border-right: none;
  }

  .settings-menu .el-menu-item {
    flex: 1;
    text-align: center;
    min-width: 100px;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .page-header-section {
    padding: var(--space-4) 0;
    margin-bottom: var(--space-4);
  }

  .page-title {
    font-size: var(--font-size-2xl);
  }

  .content-card-body {
    padding: var(--space-4);
  }

  .content-card-body .el-form-item {
    margin-bottom: var(--space-4);
  }
}

@media (max-width: 576px) {
  .page-title {
    font-size: var(--font-size-xl);
  }

  .content-card-title {
    font-size: var(--font-size-base);
  }

  .content-card-body {
    padding: var(--space-3);
  }
}
</style>
