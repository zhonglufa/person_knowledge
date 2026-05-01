<template>
  <el-dialog
    title="创建新收藏集"
    :visible.sync="dialogVisible"
    width="500px"
    @close="handleClose"
  >
    <el-form 
      ref="form" 
      :model="formData" 
      :rules="rules" 
      label-position="top"
    >
      <!-- 收藏集名称 -->
      <el-form-item label="收藏集名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入收藏集名称"
          clearable
        />
      </el-form-item>

      <!-- 收藏集描述 -->
      <el-form-item label="描述（可选）" prop="description">
        <el-input
          v-model="formData.description"
          placeholder="请输入收藏集描述，不超过200个字符"
          type="textarea"
          :rows="3"
          maxlength="200"
          show-word-limit
          clearable
        />
      </el-form-item>

      <!-- 封面图片 URL 或本地上传 -->
      <el-form-item label="封面图片" prop="coverImage">
        <el-tabs value="tab-upload" class="image-upload-tabs">
          <!-- 标签1: 本地上传 -->
          <el-tab-pane label="本地上传" name="tab-upload">
            <div class="upload-area">
              <el-upload
                class="upload-demo"
                drag
                action="/collections/upload-cover"
                :headers="uploadHeaders"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                :multiple="false"
                accept="image/*"
              >
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击选择</em>
                </div>
                <div class="el-upload__tip">只能上传jpg/png/jpeg/gif文件，且不超过10MB</div>
              </el-upload>
            </div>
          </el-tab-pane>
          
          <!-- 标签2: URL输入 -->
          <el-tab-pane label="URL输入" name="tab-url">
            <el-input
              v-model="formData.coverImage"
              placeholder="请输入图片 URL，如：https://example.com/image.jpg"
              clearable
            >
              <el-button slot="append" @click="showImagePreview">预览</el-button>
            </el-input>
          </el-tab-pane>
        </el-tabs>
        
        <!-- 图片预览 -->
        <div v-if="formData.coverImage" class="image-preview-wrapper">
          <img 
            :src="formData.coverImage" 
            alt="预览"
            class="preview-image"
            @error="handleImageError"
          />
        </div>
      </el-form-item>

      <!-- 封面背景色 -->
      <el-form-item label="背景色（当无图片时使用）" prop="coverColor">
        <div class="color-selector">
          <el-color-picker
            v-model="formData.coverColor"
            show-alpha
            color-format="hex"
          />
          <span class="color-value">{{ formData.coverColor }}</span>
        </div>
        <div class="color-preview">
          <div 
            class="preview-box"
            :style="{ backgroundColor: formData.coverColor }"
          >
            <i class="fas fa-layer-group"></i>
          </div>
        </div>
      </el-form-item>

      <!-- 是否共享 -->
      <el-form-item label="是否共享" prop="isShared">
        <el-switch
          v-model="formData.isShared"
          active-color="#13ce66"
          inactive-color="#ff4949"
        />
        <span class="switch-label">
          {{ formData.isShared ? '是，公开共享此收藏集' : '否，仅我可见' }}
        </span>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">
        创建
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { sanitizeHtml } from '@/utils/sanitize'
import { collectionsApi } from '@/api/collections';

export default {
  name: 'CreateCollectionModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    const validateName = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入收藏集名称'));
      } else if (value.length > 100) {
        callback(new Error('收藏集名称不能超过100个字符'));
      } else {
        callback();
      }
    };

    const validateCoverImage = (rule, value, callback) => {
      if (value) {
        // 简单的 URL 验证
        const urlRegex = /^https?:\/\/.+/;
        if (!urlRegex.test(value)) {
          callback(new Error('请输入有效的 URL，以 http(s):// 开头'));
        } else {
          callback();
        }
      } else {
        callback();
      }
    };

    return {
      dialogVisible: this.visible,
      loading: false,
      uploadHeaders: {},
      formData: {
        name: '',
        description: '',
        coverImage: '',
        coverColor: '#4ECDC4',
        isShared: false
      },
      rules: {
        name: [{ validator: validateName, trigger: 'blur' }],
        coverImage: [{ validator: validateCoverImage, trigger: 'blur' }]
      }
    };
  },
  mounted() {
    // 初始化上传请求头
    this.initUploadHeaders();
  },
  watch: {
    visible(val) {
      this.dialogVisible = val;
    },
    dialogVisible(val) {
      this.$emit('update:visible', val);
    }
  },
  methods: {
    async handleConfirm() {
      try {
        // 验证表单
        await this.$refs.form.validate();
        
        this.loading = true;

        // 调用 API 创建收藏集
        const response = await collectionsApi.createCollection({
          name: this.formData.name,
          description: this.formData.description || null,
          coverImage: this.formData.coverImage || null,
          coverColor: this.formData.coverColor,
          isShared: this.formData.isShared
        });

        this.$message.success('收藏集创建成功');
        
        // 发送事件给父组件
        this.$emit('create-success', response.data);
        
        // 关闭对话框
        this.handleClose();
      } catch (error) {
        console.error('创建收藏集失败:', error);
        const errorMsg = error.response?.data?.message || error.message || '创建失败';
        this.$message.error('创建失败: ' + errorMsg);
      } finally {
        this.loading = false;
      }
    },

    handleClose() {
      // 重置表单
      this.$refs.form.resetFields();
      this.formData = {
        name: '',
        description: '',
        coverImage: '',
        coverColor: '#4ECDC4',
        isShared: false
      };
      this.dialogVisible = false;
    },

    showImagePreview() {
      if (this.formData.coverImage) {
        const safeImageSrc = sanitizeHtml(this.formData.coverImage);
        this.$alert(
          `<img src="${safeImageSrc}" style="max-width: 100%; max-height: 400px;" alt="预览" />`,
          '图片预览',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '关闭'
          }
        ).catch(() => {});
      } else {
        this.$message.warning('请先输入图片 URL');
      }
    },

    handleImageError(event) {
      console.warn('图片加载失败:', event.target.src);
      event.target.style.display = 'none';
    },

    beforeUpload(file) {
      // 验证文件类型
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/jpg';
      if (!isImage) {
        this.$message.error('只支持jpg/png/jpeg/gif格式的图片');
        return false;
      }
      // 验证文件大小（10MB）
      const isLt10M = file.size / 1024 / 1024 < 10;
      if (!isLt10M) {
        this.$message.error('图片大小不能超过10MB');
        return false;
      }
      return true;
    },

    handleUploadSuccess(response, file, fileList) {
      if (response && response.data && response.data.url) {
        // 设置上传的图片URL
        this.formData.coverImage = response.data.url;
        this.$message.success('图片上传成功');
      } else {
        this.$message.error('上传成功但获取URL失败');
      }
    },

    handleUploadError(error, file, fileList) {
      console.error('上传失败:', error);
      this.$message.error('图片上传失败，请重试');
    },

    initUploadHeaders() {
      // 从localStorage获取认证令牌
      const token = localStorage.getItem('token');
      if (token) {
        this.uploadHeaders = {
          'Authorization': `Bearer ${token}`
        };
      }
    }
  }
};
</script>

<style scoped>
/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: var(--border-radius-lg);
}

:deep(.el-dialog__title) {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-dark);
}

/* 表单样式 */
:deep(.el-form) {
  padding: var(--spacing-md) 0;
}

:deep(.el-form-item) {
  margin-bottom: var(--spacing-lg);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-dark);
  margin-bottom: var(--spacing-sm);
}

/* 输入框样式 */
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-primary);
  color: var(--text-dark);
}

:deep(.el-input__inner:focus),
:deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(74, 108, 247, 0.1);
}

/* 图片预览 */
.image-preview-wrapper {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--bg-gray);
  border-radius: var(--border-radius-md);
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  border-radius: var(--border-radius-sm);
}

/* 颜色选择器 */
.color-selector {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

:deep(.el-color-picker__trigger) {
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--border-color);
}

.color-value {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: var(--font-size-sm);
  color: var(--text-medium);
}

/* 颜色预览 */
.color-preview {
  margin-top: var(--spacing-md);
  padding: var(--spacing-md);
  background-color: var(--bg-gray);
  border-radius: var(--border-radius-md);
  text-align: center;
}

.preview-box {
  width: 100%;
  height: 100px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 开关 */
:deep(.el-switch) {
  margin-right: var(--spacing-md);
}

.switch-label {
  color: var(--text-medium);
  font-size: var(--font-size-sm);
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: var(--border-radius-sm);
}

:deep(.el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

:deep(.el-button--primary:hover) {
  background-color: #3a5ce7;
  border-color: #3a5ce7;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

/* 响应式设计 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 90% !important;
  }
}

/* 上传标签栏样式 */
.image-upload-tabs {
  margin-bottom: var(--spacing-md);
}

:deep(.image-upload-tabs .el-tabs__header) {
  margin-bottom: var(--spacing-md);
}

:deep(.image-upload-tabs .el-tabs__nav-wrap) {
  border-bottom: 1px solid var(--border-color);
}

/* 上传区域样式 */
.upload-area {
  padding: var(--spacing-md) 0;
}

:deep(.upload-demo.el-upload-dragger) {
  border: 2px dashed var(--primary-color);
  border-radius: var(--border-radius-md);
  background-color: var(--bg-gray);
  transition: all 0.3s ease;
}

:deep(.upload-demo.el-upload-dragger:hover) {
  border-color: var(--primary-color);
  background-color: rgba(74, 108, 247, 0.05);
}

:deep(.upload-demo.el-upload-dragger .el-icon-upload) {
  color: var(--primary-color);
  font-size: 48px;
  margin: var(--spacing-md) 0;
}

:deep(.upload-demo.el-upload-dragger .el-upload__text) {
  color: var(--text-dark);
  font-size: var(--font-size-base);
  margin: var(--spacing-sm) 0;
}

:deep(.upload-demo.el-upload-dragger .el-upload__text em) {
  color: var(--primary-color);
  font-style: normal;
  font-weight: 500;
}

:deep(.upload-demo.el-upload-dragger .el-upload__tip) {
  color: var(--text-light);
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-sm);
}

:deep(.el-upload-list__item) {
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  background-color: var(--bg-gray);
}

:deep(.el-upload-list__item:hover) {
  background-color: var(--bg-hover);
}
</style>
