<template>
  <section class="hero-section">
    <!-- 渐变背景层 -->
    <div class="hero-bg-gradient" aria-hidden="true"></div>

    <!-- 网格装饰层 -->
    <div class="hero-bg-grid" aria-hidden="true"></div>

    <!-- 浮动光斑层 -->
    <div class="hero-bg-orbs" aria-hidden="true">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>

    <div class="container hero-container">
      <div class="hero-content">

        <h1 class="hero-title" data-aos="fade-up">
          结构化知识管理<br>
          <span class="gradient-text">让学习更高效</span>
        </h1>

        <p class="hero-description" data-aos="fade-up" data-aos-delay="100">
          系统化整理、检索和创作知识内容，构建个人知识体系<br class="hidden-mobile">
          提升学习效率与深度理解
        </p>

        <div class="hero-actions" data-aos="fade-up" data-aos-delay="200">
          <!-- 已登录：显示进入工作台 -->
          <template v-if="isLoggedIn">
            <el-button
              type="primary"
              size="large"
              @click="$emit('dashboard')"
              class="hero-btn primary"
            >
              <i class="fas fa-tachometer-alt"></i>
              进入工作台
            </el-button>
          </template>

          <!-- 未登录：显示探索和登录 -->
          <template v-else>
            <el-button
              type="primary"
              size="large"
              @click="$emit('explore')"
              class="hero-btn primary"
            >
              <i class="fas fa-rocket"></i>
              开始探索
            </el-button>

            <el-button
              size="large"
              @click="$emit('login')"
              class="hero-btn secondary"
            >
              <i class="fas fa-sign-in-alt"></i>
              登录体验完整功能
            </el-button>
          </template>
        </div>

        <!-- 统计数据 -->
        <div class="hero-stats" data-aos="fade-up" data-aos-delay="300">
          <div class="stat-item">
            <div class="stat-value">10K+</div>
            <div class="stat-label">知识条目</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">5K+</div>
            <div class="stat-label">活跃用户</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">99.9%</div>
            <div class="stat-label">系统稳定</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'HeroSection',
  props: {
    // 是否已登录
    isLoggedIn: {
      type: Boolean,
      default: false
    }
  },
  emits: ['explore', 'login', 'dashboard']
};
</script>

<style scoped>
.hero-section {
  position: relative;
  min-height: 85vh;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: clamp(60px, 10vw, 100px) 0;
  background: linear-gradient(135deg, #f8f9fc 0%, #e8ecf4 50%, #f0f4ff 100%);
}

.hero-bg-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(74, 108, 247, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(54, 203, 203, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 40% 80%, rgba(255, 107, 107, 0.06) 0%, transparent 50%);
  animation: gradientShift 20s ease-in-out infinite alternate;
}

.hero-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(74, 108, 247, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(74, 108, 247, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
}

.hero-bg-orbs {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 15s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #4a6cf7, #6b8cff);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #36cbcb, #4fd8d8);
  bottom: -50px;
  left: 10%;
  animation-delay: -5s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #ff6b6b, #ffa07a);
  top: 40%;
  right: 20%;
  animation-delay: -10s;
}

@keyframes gradientShift {
  0% { opacity: 0.8; }
  100% { opacity: 1; }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(20px, 10px) scale(1.02); }
}

.hero-container {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px;
}

.hero-content {
  text-align: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(74, 108, 247, 0.08);
  border: 1px solid rgba(74, 108, 247, 0.15);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  color: var(--primary-color);
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.hero-badge i {
  font-size: 14px;
}

.hero-title {
  font-size: clamp(2.5rem, 6vw, 4rem);
  font-weight: 800;
  line-height: 1.15;
  margin-bottom: 24px;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.gradient-text {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: clamp(1rem, 2vw, 1.25rem);
  color: var(--text-regular);
  line-height: 1.7;
  margin-bottom: 48px;
  max-width: 650px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 56px;
}

.hero-btn {
  transition: all var(--transition-base);
  min-width: 180px;
  font-weight: 600;
  font-size: 16px;
  height: 52px;
  border-radius: 12px;
}

.hero-btn i {
  margin-right: 8px;
}

.hero-btn.primary {
  background: var(--gradient-primary);
  border: none;
  color: white;
  box-shadow: 0 8px 24px rgba(74, 108, 247, 0.3);
}

.hero-btn.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(74, 108, 247, 0.4);
}

.hero-btn.secondary {
  background: white;
  border: 2px solid var(--border-base);
  color: var(--text-primary);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.hero-btn.secondary:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(74, 108, 247, 0.15);
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06);
  max-width: 500px;
  margin: 0 auto;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(to bottom, transparent, var(--border-base), transparent);
}

@media (max-width: 768px) {
  .hero-section {
    min-height: 75vh;
    padding: 60px 0;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .hero-btn {
    width: 100%;
    max-width: 320px;
  }

  .hero-stats {
    gap: 20px;
    padding: 20px 24px;
  }

  .stat-value {
    font-size: 24px;
  }

  .orb-1, .orb-2, .orb-3 {
    filter: blur(60px);
    opacity: 0.3;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-description {
    font-size: 1rem;
  }

  .hero-stats {
    flex-direction: column;
    gap: 16px;
  }

  .stat-divider {
    width: 40px;
    height: 1px;
    background: linear-gradient(to right, transparent, var(--border-base), transparent);
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero-bg-gradient,
  .hero-bg-orbs,
  .orb,
  .hero-btn {
    animation: none;
    transition: none;
  }
}
</style>
