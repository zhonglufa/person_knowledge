<template>
  <button
    :class="[
      'custom-button',
      type,
      size,
      {
        'disabled': disabled,
        'loading': loading,
        'block': block
      }
    ]"
    :disabled="disabled || loading"
    @click="$emit('click')"
  >
    <i v-if="loading" class="el-icon-loading"></i>
    <i v-else-if="icon" :class="icon"></i>
    <span v-if="$slots.default">
      <slot></slot>
    </span>
  </button>
</template>

<script>
export default {
  name: 'CustomButton',
  props: {
    type: {
      type: String,
      default: 'default', // default, primary, success, warning, danger, info, text
      validator: (value) => [
        'default', 'primary', 'success', 'warning', 'danger', 'info', 'text'
      ].includes(value)
    },
    size: {
      type: String,
      default: 'medium', // small, medium, large
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    icon: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    block: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
}
</script>

<style scoped>
.custom-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  border: 1px solid transparent;
  border-radius: var(--border-radius-sm);
  font-family: inherit;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  outline: none;
  text-decoration: none;
  white-space: nowrap;
  user-select: none;
}

/* 尺寸变体 */
.custom-button.small {
  padding: var(--space-1) var(--space-2);
  font-size: var(--font-size-sm);
  min-height: 28px;
}

.custom-button.medium {
  padding: var(--space-2) var(--space-3);
  font-size: var(--font-size-base);
  min-height: 36px;
}

.custom-button.large {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-lg);
  min-height: 44px;
}

/* 类型变体 */
.custom-button.default {
  background-color: var(--bg-primary);
  border-color: var(--border-color);
  color: var(--text-dark);
}

.custom-button.default:hover:not(:disabled) {
  background-color: var(--bg-hover);
  border-color: var(--border-color-hover);
}

.custom-button.primary {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

.custom-button.primary:hover:not(:disabled) {
  background-color: var(--primary-color-hover);
  border-color: var(--primary-color-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.custom-button.success {
  background-color: var(--success-color);
  border-color: var(--success-color);
  color: white;
}

.custom-button.success:hover:not(:disabled) {
  background-color: var(--success-color-hover);
  border-color: var(--success-color-hover);
}

.custom-button.warning {
  background-color: var(--warning-color);
  border-color: var(--warning-color);
  color: white;
}

.custom-button.warning:hover:not(:disabled) {
  background-color: var(--warning-color-hover);
  border-color: var(--warning-color-hover);
}

.custom-button.danger {
  background-color: var(--danger-color);
  border-color: var(--danger-color);
  color: white;
}

.custom-button.danger:hover:not(:disabled) {
  background-color: var(--danger-color-hover);
  border-color: var(--danger-color-hover);
}

.custom-button.info {
  background-color: var(--info-color);
  border-color: var(--info-color);
  color: white;
}

.custom-button.info:hover:not(:disabled) {
  background-color: var(--info-color-hover);
  border-color: var(--info-color-hover);
}

.custom-button.text {
  background-color: transparent;
  border-color: transparent;
  color: var(--primary-color);
  padding: var(--space-1) var(--space-2);
}

.custom-button.text:hover:not(:disabled) {
  background-color: var(--bg-hover);
  color: var(--primary-color-hover);
}

/* 状态样式 */
.custom-button:disabled,
.custom-button.disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-button.loading {
  cursor: wait;
}

.custom-button.block {
  display: flex;
  width: 100%;
}

/* 焦点样式 */
.custom-button:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}

/* 图标间距 */
.custom-button i {
  font-size: 1.1em;
}

.custom-button.small i {
  font-size: 1em;
}

.custom-button.large i {
  font-size: 1.2em;
}
</style>
