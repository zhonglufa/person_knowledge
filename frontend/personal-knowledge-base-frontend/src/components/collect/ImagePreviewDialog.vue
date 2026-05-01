<template>
  <el-dialog
      :visible.sync="localVisible"
      title="图片预览"
      width="85%"
      top="5vh"
      custom-class="image-preview-dialog"
      :destroy-on-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      @close="handleClose"
  >
    <div class="preview-content">
      <img :src="imageUrl" alt="预览图片" class="preview-image" />
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose" class="btn-secondary">关闭</el-button>
      <el-button type="primary" @click="$emit('download')">
        <i class="fas fa-download"></i>
        下载图片
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'ImagePreviewDialog',
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    imageUrl: {
      type: String,
      required: true
    }
  },
  computed: {
    localVisible: {
      get() {
        return this.visible;
      },
      set(value) {
        this.$emit('update:visible', value);
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
/* 图片预览对话框 */
.image-preview-dialog {
  background-color: white;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  transition: all var(--transition-base);
}

.image-preview-dialog :deep(.el-dialog__header) {
  padding: var(--spacing-lg);
  background-color: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
}

.image-preview-dialog :deep(.el-dialog__title) {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-dark);
}

.image-preview-dialog :deep(.el-dialog__body) {
  padding: 0;
  background-color: var(--bg-primary);
}

.image-preview-dialog :deep(.el-dialog__footer) {
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--bg-gray);
  border-top: 1px solid var(--border-light);
}

/* 预览内容区域 */
.preview-content {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 70vh;
  padding: var(--spacing-lg);
  background-color: var(--bg-gray);
  transition: var(--transition-base);
}

/* 预览图片 */
.preview-image {
  max-width: 100%;
  max-height: 65vh;
  object-fit: contain;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-md);
  transition: var(--transition-base);
  background-color: white;
  padding: var(--spacing-lg);
}

/* 底部按钮区域 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .image-preview-dialog {
    width: 95%;
    margin: 0 auto;
  }
  
  .preview-content {
    min-height: 60vh;
    padding: var(--spacing-md);
  }
  
  .preview-image {
    max-height: 50vh;
    padding: var(--spacing-md);
  }
  
  .image-preview-dialog :deep(.el-dialog__header) {
    padding: var(--spacing-md);
  }
  
  .image-preview-dialog :deep(.el-dialog__footer) {
    padding: var(--spacing-sm) var(--spacing-md);
  }
}
</style>