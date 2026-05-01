<template>
  <header class="guest-header">
    <div class="guest-header-container">
      <!-- Logo区域 -->
      <div class="guest-logo" @click="scrollToTop">
        <div class="guest-logo-icon">
          <i class="fas fa-book-open"></i>
        </div>
        <div class="guest-logo-text">
          <span class="guest-logo-title">个人结构化知识系统</span>
          <span class="guest-logo-subtitle">Personal Knowledge Base</span>
        </div>
      </div>

      <!-- 右侧操作区：仅登录/注册按钮 -->
      <div class="guest-actions">
        <el-button
          type="text"
          class="guest-login-btn"
          @click="handleLogin"
        >
          <i class="fas fa-sign-in-alt"></i>
          登录
        </el-button>
        <el-button
          type="primary"
          class="guest-register-btn"
          @click="handleRegister"
        >
          <i class="fas fa-user-plus"></i>
          免费注册
        </el-button>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  name: 'GuestHeader',
  methods: {
    // 滚动到页面顶部
    scrollToTop() {
      window.scrollTo({
        top: 0,
        behavior: 'smooth'
      })
    },
    
    // 跳转到登录页
    handleLogin() {
      this.$router.push('/login')
    },
    
    // 跳转到注册页
    handleRegister() {
      this.$router.push('/login?tab=register')
    }
  }
}
</script>

<style scoped>
.guest-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.guest-header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 clamp(20px, 4vw, 48px);
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* Logo样式 */
.guest-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 12px;
}

.guest-logo:hover {
  background: rgba(74, 108, 247, 0.05);
  transform: translateX(2px);
}

.guest-logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4a6cf7, #6b8cff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(74, 108, 247, 0.25);
  transition: all 0.3s ease;
}

.guest-logo:hover .guest-logo-icon {
  transform: scale(1.05) rotate(-5deg);
  box-shadow: 0 6px 16px rgba(74, 108, 247, 0.35);
}

.guest-logo-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.guest-logo-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary, #1a1a1a);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.guest-logo-subtitle {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary, #666);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* 右侧操作区 */
.guest-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.guest-login-btn {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary, #1a1a1a);
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.guest-login-btn i {
  margin-right: 6px;
  font-size: 14px;
}

.guest-login-btn:hover {
  color: var(--primary-color, #4a6cf7);
  background: rgba(74, 108, 247, 0.08);
}

.guest-register-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 10px 24px;
  border-radius: 10px;
  height: auto;
  background: linear-gradient(135deg, #4a6cf7, #6b8cff);
  border: none;
  box-shadow: 0 4px 12px rgba(74, 108, 247, 0.25);
  transition: all 0.3s ease;
}

.guest-register-btn i {
  margin-right: 6px;
  font-size: 14px;
}

.guest-register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 108, 247, 0.35);
}

.guest-register-btn:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .guest-header-container {
    height: 64px;
    padding: 0 16px;
  }

  .guest-logo-subtitle {
    display: none;
  }

  .guest-logo-title {
    font-size: 16px;
  }

  .guest-logo-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  .guest-login-btn span {
    display: none;
  }

  .guest-login-btn {
    padding: 8px;
  }

  .guest-register-btn {
    padding: 8px 16px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .guest-logo-text {
    display: none;
  }

  .guest-actions {
    gap: 8px;
  }
}

/* 滚动时header样式优化 */
@media (prefers-reduced-motion: reduce) {
  .guest-header,
  .guest-logo,
  .guest-logo-icon,
  .guest-login-btn,
  .guest-register-btn {
    transition: none;
  }
}
</style>
