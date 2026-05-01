<template>
  <article 
    class="value-card"
    @click="$emit('click')"
    @keydown.enter="$emit('click')"
    tabindex="0"
    role="button"
    :aria-label="`了解更多关于${value.title}的信息`"
  >
    <div class="card-content">
      <div class="card-icon">
        <i :class="value.icon"></i>
        <div class="icon-bg" :style="iconGradient"></div>
      </div>
      
      <h3 class="card-title">
        {{ value.title }}
      </h3>
      
      <p class="card-description">
        {{ value.description }}
      </p>
    </div>
    
    <div class="card-decoration">
      <span class="card-number">{{ cardNumber }}</span>
    </div>
  </article>
</template>

<script>
export default {
  name: 'ValueCard',
  props: {
    value: {
      type: Object,
      required: true
    }
  },
  computed: {
    cardNumber() {
      return String(this.value.id).padStart(2, '0');
    },
    iconGradient() {
      const gradients = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
      ];
      return {
        background: gradients[(this.value.id - 1) % gradients.length]
      };
    }
  },
  emits: ['click']
};
</script>

<style scoped>
.value-card {
  position: relative;
  background: white;
  border-radius: 20px;
  padding: 40px 32px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  isolation: isolate;
}

.value-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  opacity: 0;
  transition: opacity 0.4s ease;
  z-index: 1;
}

.value-card:hover,
.value-card:focus {
  transform: translateY(-10px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.value-card:hover::before,
.value-card:focus::before {
  opacity: 1;
}

.value-card:active {
  transform: translateY(-5px);
}

.card-content {
  position: relative;
  z-index: 2;
}

.card-icon {
  position: relative;
  width: 70px;
  height: 70px;
  margin-bottom: 24px;
}

.card-icon i {
  position: relative;
  z-index: 2;
  font-size: 28px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.icon-bg {
  position: absolute;
  inset: 0;
  border-radius: 18px;
  z-index: 1;
  transform: rotate(45deg);
  transition: transform 0.4s ease;
}

.value-card:hover .icon-bg {
  transform: rotate(225deg);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-dark, #1a1a1a);
  margin-bottom: 16px;
  line-height: 1.3;
}

.card-description {
  color: var(--text-medium, #666);
  line-height: 1.6;
  margin: 0;
  font-size: 1rem;
}

.card-decoration {
  position: absolute;
  top: 24px;
  right: 24px;
  opacity: 0.05;
  font-size: 120px;
  font-weight: 900;
  color: var(--primary-color, #4a6cf7);
  pointer-events: none;
  user-select: none;
  transition: opacity 0.4s ease;
}

.value-card:hover .card-decoration {
  opacity: 0.1;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .value-card {
    background: #1e1e2e;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }
  
  .card-title {
    color: #e2e8f0;
  }
  
  .card-description {
    color: #94a3b8;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .value-card,
  .icon-bg,
  .card-decoration {
    transition: none;
    animation: none;
  }
}
</style>
