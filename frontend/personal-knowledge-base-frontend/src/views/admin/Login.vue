<template>
  <div class="admin-login">
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <!-- 登录头部 -->
        <div class="login-header">
          <div class="login-logo">
            <i class="el-icon-s-grid"></i>
          </div>
          <h1 class="login-title">管理员登录</h1>
          <p class="login-subtitle">个人知识库管理系统 · 后台管理中心</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.native.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入管理员账号"
              prefix-icon="el-icon-user"
              @keyup.enter.native="handleLogin"
              size="medium"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              size="medium"
              show-password
              @keyup.enter.native="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="medium"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 登录底部 -->
        <div class="login-footer">
          <el-link type="primary" @click="$router.push('/login')">
            <i class="el-icon-back"></i> 返回用户登录
          </el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
  name: 'AdminLogin',

  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入管理员账号', trigger: 'blur' },
          { min: 2, max: 30, message: '账号长度在 2 到 30 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
        ]
      },
      showPassword: false,
      loading: false
    }
  },

  methods: {
    handleLogin() {
      if (this.loading) {
        return
      }

      this.$refs.loginFormRef.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          const response = await adminApi.login(this.loginForm)
          if ( response && response.code === 200) {
            localStorage.setItem('adminToken', response.data.token)
            localStorage.setItem('adminInfo', JSON.stringify(response.data.userInfo))

            this.$message.success('登录成功')
            this.$router.replace('/admin/home')
          } else {
            this.$message.error(response.message || '登录失败，请检查账号密码')
          }
        } catch (error) {
          console.error('登录错误:', error)
          this.$message.error('登录失败，请检查网络连接')
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-canvas);
  position: relative;
  overflow: hidden;
}

/* ========== 背景装饰 ========== */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
}

.bg-shape-1 {
  width: 400px;
  height: 400px;
  background: var(--primary-color);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.bg-shape-2 {
  width: 300px;
  height: 300px;
  background: var(--secondary-color);
  bottom: -80px;
  left: -80px;
  animation: float 10s ease-in-out infinite reverse;
}

.bg-shape-3 {
  width: 200px;
  height: 200px;
  background: var(--warning-color);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

/* ========== 登录容器 ========== */
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: var(--space-6);
}

.login-card {
  background: var(--bg-container);
  border-radius: var(--radius-xl);
  padding: var(--space-10) var(--space-8);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  transition: box-shadow var(--transition-normal);
}

/* ========== 登录头部 ========== */
.login-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.login-logo {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--space-4);
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: var(--font-size-4xl);
  box-shadow: var(--shadow-primary);
}

.login-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
}

.login-subtitle {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0;
}

/* ========== 登录表单 ========== */
.login-form {
  margin-top: var(--space-6);
}

.login-form :deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

.login-form :deep(.el-input__inner) {
  height: 44px;
  border-radius: var(--radius-md);
  padding-left: 40px;
}

.login-form :deep(.el-input__prefix) {
  left: 12px;
  color: var(--text-secondary);
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: var(--font-size-base);
  font-weight: 600;
  border-radius: var(--radius-md);
  letter-spacing: 2px;
  transition: all var(--transition-normal);
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-primary);
}

/* ========== 登录底部 ========== */
.login-footer {
  text-align: center;
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.login-footer :deep(.el-link) {
  font-size: var(--font-size-sm);
}

.login-footer :deep(.el-link i) {
  margin-right: var(--space-1);
}

/* ========== 响应式设计 ========== */
@media (max-width: 480px) {
  .login-card {
    padding: var(--space-8) var(--space-6);
  }

  .login-title {
    font-size: var(--font-size-xl);
  }

  .login-logo {
    width: 56px;
    height: 56px;
    font-size: var(--font-size-3xl);
  }
}
</style>
