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
                  <el-breadcrumb-item>个人资料</el-breadcrumb-item>
                </el-breadcrumb>
              </div>
              <h1 class="page-title">个人资料</h1>
              <p class="page-subtitle">管理您的个人信息和账户设置</p>
            </div>
          </div>
        </div>

        <div class="profile-content">
          <!-- 个人信息卡片 -->
          <div class="content-card">
            <div class="content-card-header">
              <h3 class="content-card-title">
                <i class="fas fa-user"></i>
                基本信息
              </h3>
            </div>
            <div class="content-card-body">
              <el-form
                :model="profileForm"
                :rules="profileRules"
                ref="profileForm"
                label-width="120px"
                class="profile-form"
              >
                <el-row :gutter="20">
                  <el-col :span="8">
                    <el-form-item label="头像">
                      <div class="avatar-upload">
                        <div class="avatar-preview" @click="triggerAvatarUpload">
                          <img :src="profileForm?.avatar || defaultAvatar" :alt="profileForm?.nickname || ''" />
                          <div class="avatar-overlay">
                            <i class="fas fa-camera"></i>
                            <span>更换头像</span>
                          </div>
                        </div>
                        <el-upload
                          ref="avatarUpload"
                          class="avatar-uploader"
                          :http-request="uploadAvatar"
                          :show-file-list="false"
                          :before-upload="beforeAvatarUpload"
                          style="display: none"
                        >
                        </el-upload>
                      </div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="用户名" prop="username">
                      <el-input v-model="profileForm.username" disabled placeholder="用户名不可修改" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="昵称" prop="nickname">
                      <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="邮箱" prop="email">
                      <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="phone">
                      <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-select v-model="profileForm.gender" placeholder="请选择性别" style="width: 100%">
                        <el-option label="男" value="male"></el-option>
                        <el-option label="女" value="female"></el-option>
                        <el-option label="保密" value="secret"></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="专业领域">
                      <el-select
                        v-model="profileForm.expertise"
                        multiple
                        filterable
                        allow-create
                        placeholder="请选择或添加专业领域"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="field in expertiseOptions"
                          :key="field"
                          :label="field"
                          :value="field"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="个人简介">
                  <el-input
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="3"
                    placeholder="请简要介绍自己..."
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>

                <el-form-item class="form-actions">
                  <el-button
                    type="primary"
                    @click="submitProfile"
                    :loading="submitLoading"
                  >
                    保存资料
                  </el-button>
                  <el-button @click="resetProfile">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>

          <!-- 账户安全卡片 -->
          <div class="content-card">
            <div class="content-card-header">
              <h3 class="content-card-title">
                <i class="fas fa-shield-alt"></i>
                账户安全
              </h3>
            </div>
            <div class="content-card-body">
              <div class="security-section">
                <div class="security-item">
                  <div class="security-info">
                    <div class="security-icon gradient-primary">
                      <i class="fas fa-lock"></i>
                    </div>
                    <div>
                      <h4>密码安全</h4>
                      <p>定期更换密码可以提高账户安全性</p>
                    </div>
                  </div>
                  <el-button type="primary" plain size="small" @click="showChangePassword = true">修改密码</el-button>
                </div>

                <div class="security-item">
                  <div class="security-info">
                    <div class="security-icon gradient-secondary">
                      <i class="fas fa-shield-alt"></i>
                    </div>
                    <div>
                      <h4>双重验证</h4>
                      <p>开启双重验证可以进一步保护您的账户</p>
                    </div>
                  </div>
                  <el-switch
                    v-model="securitySettings.twoFactorAuth"
                    @change="handleTwoFactorChange"
                    active-text="已开启"
                    inactive-text="已关闭"
                  />
                </div>

                <div class="security-item">
                  <div class="security-info">
                    <div class="security-icon gradient-cool">
                      <i class="fas fa-history"></i>
                    </div>
                    <div>
                      <h4>登录历史</h4>
                      <p>查看最近的登录记录和设备信息</p>
                    </div>
                  </div>
                  <el-button plain size="small" @click="viewLoginHistory">查看记录</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 偏好设置卡片 -->
          <div class="content-card">
            <div class="content-card-header">
              <h3 class="content-card-title">
                <i class="fas fa-sliders-h"></i>
                偏好设置
              </h3>
            </div>
            <div class="content-card-body">
              <el-form
                :model="preferenceForm"
                label-width="150px"
                class="preference-form"
              >
                <el-form-item label="默认视图模式">
                  <el-radio-group v-model="preferenceForm.defaultView">
                    <el-radio label="grid">网格视图</el-radio>
                    <el-radio label="list">列表视图</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="收藏项排序">
                  <el-select v-model="preferenceForm.collectionSort" style="width: 100%">
                    <el-option label="按创建时间" value="createTime"></el-option>
                    <el-option label="按更新时间" value="updateTime"></el-option>
                    <el-option label="按标题" value="title"></el-option>
                    <el-option label="按收藏集" value="collection"></el-option>
                  </el-select>
                </el-form-item>

                <el-form-item label="加工状态显示">
                  <el-switch
                    v-model="preferenceForm.showStatusBadges"
                    active-text="显示"
                    inactive-text="隐藏"
                    @change="savePreferences"
                  />
                </el-form-item>

                <el-form-item label="通知设置">
                  <div class="notification-settings">
                    <el-checkbox
                      v-model="preferenceForm.notifications.email"
                      @change="savePreferences"
                    >
                      邮件通知
                    </el-checkbox>
                    <el-checkbox
                      v-model="preferenceForm.notifications.browser"
                      @change="savePreferences"
                    >
                      浏览器通知
                    </el-checkbox>
                    <el-checkbox
                      v-model="preferenceForm.notifications.desktop"
                      @change="savePreferences"
                    >
                      桌面通知
                    </el-checkbox>
                  </div>
                </el-form-item>

                <el-form-item label="数据管理">
                  <div class="data-management">
                    <el-button type="primary" plain @click="exportData">
                      <i class="fas fa-download"></i> 导出个人数据
                    </el-button>
                    <el-button type="danger" plain @click="showDeleteAccount = true">
                      <i class="fas fa-trash-alt"></i> 删除账户
                    </el-button>
                  </div>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      :visible.sync="showChangePassword"
      width="500px"
    >
      <el-form
        :model="passwordForm"
        :rules="passwordRules"
        ref="passwordForm"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            type="password"
            show-password
            placeholder="请输入当前密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（至少6位）"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showChangePassword = false">取消</el-button>
        <el-button
          type="primary"
          @click="changePassword"
          :loading="passwordLoading"
        >
          确认修改
        </el-button>
      </span>
    </el-dialog>

    <!-- 删除账户对话框 -->
    <el-dialog
      title="删除账户"
      :visible.sync="showDeleteAccount"
      width="500px"
      class="danger-dialog"
    >
      <div class="delete-warning">
        <div class="warning-icon">
          <i class="fas fa-exclamation-triangle"></i>
        </div>
        <h3>警告：此操作不可逆转</h3>
        <p>删除账户将永久清除您的所有数据，包括：</p>
        <ul>
          <li>所有收藏项和收藏集</li>
          <li>所有笔记和个人资料</li>
          <li>所有加工进度和状态</li>
          <li>账户设置和偏好</li>
        </ul>
        <p><strong>请谨慎操作！</strong></p>
        <el-input
          v-model="deleteConfirmText"
          placeholder="请输入 DELETE CONFIRM 确认删除"
        />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showDeleteAccount = false">取消</el-button>
        <el-button
          type="danger"
          @click="deleteAccount"
          :disabled="deleteConfirmText !== 'DELETE CONFIRM'"
          :loading="deleteLoading"
        >
          永久删除账户
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { getUserProfile, updateUserProfile, uploadAvatar, changePassword } from '@/api/user'

export default {
  name: 'Profile',
  data() {
    // 验证密码一致性
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      pageLoading: false,
      submitLoading: false,
      passwordLoading: false,
      deleteLoading: false,
      defaultAvatar: 'https://assets.mockplus.cn/ai/newImages/pexels/357.jpg',

      // 对话框显示状态
      showChangePassword: false,
      showDeleteAccount: false,
      deleteConfirmText: '',

      // 安全设置
      securitySettings: {
        twoFactorAuth: false
      },

      // 专业领域选项
      expertiseOptions: [
        '前端开发', '后端开发', '移动开发', '人工智能',
        '数据科学', '产品设计', '项目管理', '用户体验'
      ],

      // 个人资料表单
      profileForm: {
        username: '',
        nickname: '',
        email: '',
        phone: '',
        avatar: '',
        gender: 'secret',
        bio: '',
        expertise: []
      },

      // 偏好设置表单
      preferenceForm: {
        defaultView: 'grid',
        collectionSort: 'updateTime',
        showStatusBadges: true,
        notifications: {
          email: true,
          browser: true,
          desktop: false
        }
      },

      // 密码表单
      passwordForm: {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      },

      // 验证规则
      profileRules: {
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
        ]
      },

      passwordRules: {
        currentPassword: [
          { required: true, message: '请输入当前密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },

  computed: {
    ...mapGetters('user', ['getUserInfo', 'getAvatar', 'getNickname'])
  },

  created() {
    this.fetchUserProfile()
    this.loadPreferences()
  },

  methods: {
    // 获取用户资料
    async fetchUserProfile() {
      this.pageLoading = true
      try {
        const res = await getUserProfile()
        const data = res?.data || res || {}
        this.profileForm = {
          username: data?.username || this.getUserInfo?.username || '',
          nickname: data?.nickname || this.getNickname || '',
          email: data?.email || '',
          phone: data?.phone || '',
          avatar: data?.avatar || this.getAvatar || this.defaultAvatar,
          gender: data?.gender || 'secret',
          bio: data?.bio || '',
          expertise: Array.isArray(data?.expertise) ? data.expertise : []
        }
      } catch (error) {
        console.error('获取用户资料失败:', error)
        this.$message.error('获取用户资料失败')
        this.initFromVuex()
      } finally {
        this.pageLoading = false
      }
    },

    // 从 Vuex 初始化
    initFromVuex() {
      const info = this.getUserInfo || {}
      this.profileForm = {
        username: info?.username || '',
        nickname: this.getNickname || '',
        email: info?.email || '',
        phone: info?.phone || '',
        avatar: this.getAvatar || this.defaultAvatar,
        gender: info?.gender || 'secret',
        bio: info?.bio || '',
        expertise: Array.isArray(info?.expertise) ? info.expertise : []
      }
    },

    // 加载偏好设置
    loadPreferences() {
      try {
        const saved = localStorage.getItem('userPreferences')
        if (saved) {
          const prefs = JSON.parse(saved)
          this.preferenceForm = { ...this.preferenceForm, ...prefs }
        }
      } catch (error) {
        console.error('加载偏好设置失败:', error)
      }
    },

    // 保存偏好设置
    savePreferences() {
      try {
        localStorage.setItem('userPreferences', JSON.stringify(this.preferenceForm))
        this.$message.success('偏好设置已保存')
      } catch (error) {
        console.error('保存偏好设置失败:', error)
        this.$message.error('保存偏好设置失败')
      }
    },

    // 触发头像上传
    triggerAvatarUpload() {
      this.$refs.avatarUpload?.$el?.querySelector('input')?.click()
    },

    // 上传前验证
    beforeAvatarUpload(file) {
      const isImage = file?.type?.startsWith('image/')
      const isLt2M = file?.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('头像图片只能是图片格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('头像图片大小不能超过 2MB!')
        return false
      }
      return true
    },

    // 自定义头像上传
    async uploadAvatar(option) {
      if (!option?.file) return

      const formData = new FormData()
      formData.append('file', option.file)

      try {
        const response = await uploadAvatar(formData)

        if (response && typeof response === 'object' && 'code' in response && response.code !== 200) {
          throw new Error(response?.message || '头像上传失败')
        }

        const avatarUrl = typeof response?.data === 'string'
          ? response.data
          : response?.data?.avatarUrl || response?.data?.avatar || response?.data?.url || response?.avatarUrl || response?.avatar || response?.url || ''

        if (!avatarUrl) {
          throw new Error('头像上传失败')
        }

        this.profileForm.avatar = avatarUrl
        this.$message.success('头像上传成功')

        const userInfo = { ...(this.getUserInfo || {}) }
        userInfo.avatar = avatarUrl
        this.$store.commit('user/SET_USER_INFO', userInfo)
      } catch (error) {
        console.error('头像上传失败:', error)
        this.$message.error(error?.message || '头像上传失败')
      }
    },

    // 提交个人资料
    async submitProfile() {
      try {
        await this.$refs.profileForm?.validate()
        this.submitLoading = true

        const submitData = {
          nickname: this.profileForm?.nickname || '',
          email: this.profileForm?.email || '',
          phone: this.profileForm?.phone || '',
          avatar: this.profileForm?.avatar || '',
          gender: this.profileForm?.gender || 'secret',
          bio: this.profileForm?.bio || '',
          expertise: this.profileForm?.expertise || []
        }

        const res = await updateUserProfile(submitData)
        if (res && typeof res === 'object' && 'code' in res && res.code !== 200) {
          return
        }
        this.$message.success('资料保存成功')

        // 更新 Vuex 中的用户信息
        const userInfo = { ...(this.getUserInfo || {}), ...submitData }
        this.$store.commit('user/SET_USER_INFO', userInfo)
      } catch (error) {
        if (error?.errors) {
          return // 表单验证错误
        }
        console.error('保存失败:', error)
        this.$message.error('保存失败，请稍后重试')
      } finally {
        this.submitLoading = false
      }
    },

    // 重置个人资料
    resetProfile() {
      this.fetchUserProfile()
      this.$nextTick(() => {
        this.$refs.profileForm?.resetFields()
      })
    },

    // 修改密码
    async changePassword() {
      try {
        await this.$refs.passwordForm?.validate()
        this.passwordLoading = true

        await changePassword({
          oldPassword: this.passwordForm?.currentPassword || '',
          newPassword: this.passwordForm?.newPassword || ''
        })

        this.$message.success('密码修改成功')
        this.showChangePassword = false
        this.passwordForm = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        this.$refs.passwordForm?.resetFields()
      } catch (error) {
        if (error?.errors) {
          return // 表单验证错误
        }
        console.error('密码修改失败:', error)
        this.$message.error('密码修改失败，请检查当前密码是否正确')
      } finally {
        this.passwordLoading = false
      }
    },

    // 处理双重验证变更
    async handleTwoFactorChange(value) {
      try {
        // TODO: 调用 API 更新双重验证状态
        this.$message.success(value ? '双重验证已开启' : '双重验证已关闭')
      } catch (error) {
        console.error('更新双重验证状态失败:', error)
        this.securitySettings.twoFactorAuth = !value
        this.$message.error('操作失败')
      }
    },

    // 查看登录历史
    viewLoginHistory() {
      this.$message.info('查看登录历史功能开发中...')
    },

    // 导出数据
    async exportData() {
      try {
        // TODO: 调用数据导出 API
        this.$message.success('数据导出请求已提交，请稍后查看邮箱')
      } catch (error) {
        console.error('数据导出失败:', error)
        this.$message.error('数据导出失败')
      }
    },

    // 删除账户
    async deleteAccount() {
      if (this.deleteConfirmText !== 'DELETE CONFIRM') {
        this.$message.warning('请正确输入确认文本')
        return
      }

      this.deleteLoading = true
      try {
        // TODO: 调用删除账户 API
        this.$message.success('账户删除请求已提交')
        this.showDeleteAccount = false

        // 清除本地状态并跳转登录页
        await this.$store.dispatch('user/logout')
        this.$router.push('/login')
      } catch (error) {
        console.error('账户删除失败:', error)
        this.$message.error('账户删除失败')
      } finally {
        this.deleteLoading = false
        this.deleteConfirmText = ''
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  padding: var(--space-6);
  height: 100%;
  overflow-y: auto;
  background-color: var(--bg-page);
}

/* 页面标题 */
.page-header {
  margin-bottom: var(--space-6);
  border: none;
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
}

.page-subtitle {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin: 0;
}

/* 页面标题区 */
.page-header-section {
  padding: var(--space-6) 0;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: var(--space-6);
}

/* 内容区域 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
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

.profile-card {
  padding: 0;
  overflow: hidden;
}

.profile-card:hover {
  transform: none;
}

.card-header-inner {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-canvas);
}

.inner-card-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.card-content-inner {
  padding: var(--space-6);
}

/* 卡片内边距调整 */
.content-card-body {
  padding: var(--space-6);
}

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

/* 表单样式 */
.profile-form,
.preference-form {
  max-width: 800px;
}

:deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.form-actions {
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--border-light);
}

/* 头像上传 */
.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: var(--radius-full);
  overflow: hidden;
  border: 3px solid var(--border-base);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.avatar-preview:hover {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: var(--bg-mask);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
  font-size: var(--font-size-xs);
  text-align: center;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay i {
  font-size: var(--font-size-xl);
  margin-bottom: var(--space-1);
}

/* 安全设置 */
.security-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: var(--bg-canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  transition: all var(--transition-normal);
}

.security-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.security-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.security-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: var(--font-size-lg);
  flex-shrink: 0;
}

.bg-primary-gradient { background: var(--gradient-primary); }
.bg-success-gradient { background: var(--gradient-secondary); }
.bg-info-gradient { background: var(--gradient-cool); }

.security-info h4 {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1) 0;
}

.security-info p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}

/* 通知设置 */
.notification-settings {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

:deep(.el-checkbox) {
  margin-right: 0;
  margin-bottom: var(--space-2);
}

/* 数据管理 */
.data-management {
  display: flex;
  gap: var(--space-4);
}

/* 删除警告 */
.delete-warning {
  text-align: center;
  padding: var(--space-5);
}

.warning-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background: var(--danger-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-4);
}

.warning-icon i {
  font-size: 32px;
  color: var(--danger-color);
}

.delete-warning h3 {
  font-size: var(--font-size-xl);
  color: var(--text-primary);
  margin: 0 0 var(--space-4) 0;
}

.delete-warning p {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-3) 0;
  line-height: 1.6;
}

.delete-warning ul {
  text-align: left;
  margin: var(--space-4) 0;
  padding-left: var(--space-5);
}

.delete-warning li {
  margin: var(--space-2) 0;
  color: var(--text-regular);
}

.delete-warning .el-input {
  margin-top: var(--space-5);
}

:deep(.danger-dialog .el-dialog__title) {
  color: var(--danger-color);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: var(--font-size-2xl);
  }

  .content-card-body {
    padding: var(--space-4);
  }

  .profile-form :deep(.el-row) {
    flex-direction: column;
    gap: 0;
  }

  .profile-form :deep(.el-col) {
    width: 100%;
    margin-bottom: var(--space-3);
  }

  .profile-form :deep(.el-col:first-child) {
    margin-bottom: var(--space-4);
  }

  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-4);
  }

  .avatar-preview {
    width: 100px;
    height: 100px;
  }

  .data-management {
    flex-direction: column;
  }

  .form-actions {
    margin-top: var(--space-4);
    padding-top: var(--space-3);
  }

  .notification-settings {
    flex-direction: row;
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  :deep(.el-checkbox) {
    margin-right: var(--space-4);
    margin-bottom: 0;
  }
}

@media (max-width: 576px) {
  .page-header-section {
    padding: var(--space-4) 0;
  }

  .page-title {
    font-size: var(--font-size-xl);
  }

  .content-card-header {
    padding: var(--space-3) var(--space-4);
  }

  .content-card-title {
    font-size: var(--font-size-base);
  }

  .content-card-body {
    padding: var(--space-3);
  }

  .security-item {
    padding: var(--space-3);
  }

  .security-info {
    gap: var(--space-3);
  }

  .security-icon {
    width: 36px;
    height: 36px;
    font-size: var(--font-size-base);
  }

  .security-info h4 {
    font-size: var(--font-size-sm);
  }
}
</style>
