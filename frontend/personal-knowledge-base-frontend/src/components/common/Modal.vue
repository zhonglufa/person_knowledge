<template>
  <transition name="base-modal">
    <div v-if="visible" class="base-modal-wrapper" @click.self="handleClose">
      <div 
        :class="[
          'base-modal',
          {
            'base-modal--fullscreen': fullscreen,
            'base-modal--center': center,
            'base-modal--small': size === 'small',
            'base-modal--large': size === 'large',
            'base-modal--medium': size === 'medium',
          }
        ]"
        :style="modalStyle"
        @click.stop
      >
        <div v-if="title || showClose" class="base-modal__header">
          <div v-if="title" class="base-modal__title">{{ title }}</div>
          <button 
            v-if="showClose" 
            type="button" 
            class="base-modal__close" 
            @click="handleClose"
            :disabled="loading"
          >
            <i class="base-icon-close"></i>
          </button>
        </div>
        <div v-if="!fullscreen" class="base-modal__body">
          <slot></slot>
        </div>
        <div v-if="fullscreen" class="base-modal__body--fullscreen">
          <slot></slot>
        </div>
        <div v-if="showFooter" class="base-modal__footer">
          <slot name="footer">
            <base-button v-if="showCancelButton" @click="handleCancel" :loading="loading">{{ cancelText }}</base-button>
            <base-button v-if="showConfirmButton" type="primary" @click="handleConfirm" :loading="loading">{{ confirmText }}</base-button>
          </slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script>
import BaseButton from './Button/CustomButton.vue';

export default {
  name: 'BaseModal',
  components: {
    BaseButton
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    width: {
      type: [String, Number],
      default: 500
    },
    height: {
      type: [String, Number],
      default: null
    },
    fullscreen: {
      type: Boolean,
      default: false
    },
    top: {
      type: [String, Number],
      default: 100
    },
    modalStyle: {
      type: Object,
      default: () => ({})
    },
    showClose: {
      type: Boolean,
      default: true
    },
    closeOnClickModal: {
      type: Boolean,
      default: true
    },
    closeOnPressEscape: {
      type: Boolean,
      default: true
    },
    showFooter: {
      type: Boolean,
      default: true
    },
    showCancelButton: {
      type: Boolean,
      default: true
    },
    showConfirmButton: {
      type: Boolean,
      default: true
    },
    cancelText: {
      type: String,
      default: '取消'
    },
    confirmText: {
      type: String,
      default: '确定'
    },
    loading: {
      type: Boolean,
      default: false
    },
    center: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => {
        return ['small', 'medium', 'large', 'fullscreen'].includes(value);
      }
    }
  },
  data() {
    return {
      key: 0
    };
  },
  watch: {
    visible(val) {
      if (val) {
        this.$nextTick(() => {
          document.body.style.overflow = 'hidden';
        });
        this.addEscKeydownListener();
      } else {
        document.body.style.overflow = '';
        this.removeEscKeydownListener();
      }
      this.$emit('update:visible', val);
    }
  },
  beforeDestroy() {
    document.body.style.overflow = '';
    this.removeEscKeydownListener();
  },
  methods: {
    handleClose() {
      if (this.loading) return;
      this.$emit('update:visible', false);
      this.$emit('close');
    },
    handleCancel() {
      if (this.loading) return;
      this.$emit('cancel');
      this.handleClose();
    },
    handleConfirm() {
      if (this.loading) return;
      this.$emit('confirm');
    },
    addEscKeydownListener() {
      if (this.closeOnPressEscape) {
        document.addEventListener('keydown', this.handleEscKeydown);
      }
    },
    removeEscKeydownListener() {
      document.removeEventListener('keydown', this.handleEscKeydown);
    },
    handleEscKeydown(e) {
      if (e.key === 'Escape' && this.visible) {
        this.handleClose();
      }
    }
  }
};
</script>

<style scoped>
.base-modal-wrapper {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: auto;
  margin: 0;
  z-index: var(--z-modal);
  background-color: var(--bg-overlay);
}

.base-modal {
  position: relative;
  margin: var(--space-20) auto;
  background-color: var(--bg-container);
  border-radius: var(--radius-lg);
  outline: none;
  box-shadow: var(--shadow-xl);
  box-sizing: border-box;
  width: 500px;
  transition: transform var(--transition-normal), opacity var(--transition-normal);
}

.base-modal--small {
  width: 400px;
}

.base-modal--large {
  width: 900px;
}

.base-modal--medium {
  width: 500px;
}

.base-modal--fullscreen {
  width: 100%;
  height: 100%;
  margin: 0;
  max-width: 100%;
  max-height: 100%;
  border-radius: 0;
}

.base-modal--center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  margin: 0;
}

.base-modal__header {
  position: relative;
  padding: var(--space-5) var(--space-5) var(--space-4);
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.base-modal__title {
  font-size: var(--font-size-lg);
  line-height: 1;
  color: var(--text-primary);
  font-weight: 500;
}

.base-modal__close {
  position: relative;
  z-index: 1;
  top: -2px;
  right: -2px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin: 0;
  padding: 0;
  color: var(--text-secondary);
  font-weight: 400;
  font-size: var(--font-size-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
  background: transparent;
  border: none;
  outline: none;
  border-radius: var(--radius-sm);
}

.base-modal__close:hover {
  color: var(--primary-color);
  background-color: var(--primary-bg);
}

.base-modal__body {
  padding: var(--space-5);
  max-height: calc(100vh - 130px);
  overflow-y: auto;
}

.base-modal__body--fullscreen {
  position: absolute;
  top: 60px;
  right: 0;
  bottom: 60px;
  left: 0;
  padding: var(--space-5);
  max-height: none;
  overflow-y: auto;
}

.base-modal__footer {
  padding: var(--space-4) var(--space-5);
  background-color: var(--bg-canvas);
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.base-modal__footer button {
  margin-left: var(--space-3);
}

/* 过渡动画 */
.base-modal-enter-active,
.base-modal-leave-active {
  transition: opacity var(--transition-slow);
}

.base-modal-enter-from,
.base-modal-leave-to {
  opacity: 0;
}

.base-modal-enter-active .base-modal,
.base-modal-leave-active .base-modal {
  transition: transform var(--transition-slow);
}

.base-modal-enter-from .base-modal,
.base-modal-leave-to .base-modal {
  transform: translateY(-20px);
}

.base-modal-enter-active .base-modal--center,
.base-modal-leave-active .base-modal--center {
  transition: transform var(--transition-slow);
}

.base-modal-enter-from .base-modal--center,
.base-modal-leave-to .base-modal--center {
  transform: scale(0.9);
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .base-modal {
    width: 90% !important;
    margin: var(--space-5) auto;
  }
  
  .base-modal__header {
    padding: var(--space-4) var(--space-4) var(--space-3);
  }
  
  .base-modal__body {
    padding: var(--space-4);
  }
  
  .base-modal__footer {
    padding: var(--space-3) var(--space-4);
  }
}
</style>