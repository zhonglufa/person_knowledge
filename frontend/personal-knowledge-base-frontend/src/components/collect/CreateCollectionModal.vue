<template>
  <el-dialog
    title="创建新收藏集"
    :visible.sync="dialogVisible"
    width="540px"
    :close-on-click-modal="false"
    @close="handleClose"
    @open="handleOpen"
  >
    <el-form 
      ref="form" 
      :model="formData" 
      :rules="rules" 
      label-position="top"
      @submit.native.prevent="handleConfirm"
    >
      <el-form-item label="收藏集名称" prop="name" class="required-field">
        <el-input
          ref="nameInput"
          v-model="formData.name"
          placeholder="例如：前端学习资料、旅行计划..."
          clearable
          maxlength="100"
          show-word-limit
          @keyup.enter.native="handleConfirm"
        />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          placeholder="简单描述这个收藏集的用途（可选）"
          type="textarea"
          :rows="2"
          maxlength="200"
          show-word-limit
          clearable
        />
      </el-form-item>

      <el-form-item label="可见性" prop="isPublic" class="visibility-field">
        <el-radio-group v-model="formData.isPublic" size="small">
          <el-radio-button :label="0">
            <i class="fas fa-lock"></i> 私有
          </el-radio-button>
          <el-radio-button :label="1">
            <i class="fas fa-globe"></i> 公开
          </el-radio-button>
        </el-radio-group>
        <div class="field-hint">
          <i class="fas fa-info-circle"></i>
          {{ formData.isPublic === 1 ? '其他人可以查看此收藏集' : '仅你自己可见' }}
        </div>
      </el-form-item>

      <el-collapse v-model="activeCollapse" class="advanced-options">
         <el-collapse-item title="高级选项（可选）" name="advanced">
           <el-form-item label="封面图片" prop="coverImage">
             <div class="upload-section">
               <el-upload
                 class="image-uploader"
                 :action="uploadAction"
                 :headers="uploadHeaders"
                 :show-file-list="false"
                 :before-upload="beforeImageUpload"
                 :on-success="handleUploadSuccess"
                 :on-error="handleUploadError"
                 accept="image/*"
               >
                 <div v-if="formData.coverImage" class="image-preview">
                   <img :src="formData.coverImage" alt="封面预览" />
                   <div class="image-overlay">
                     <i class="el-icon-edit"></i>
                     <span>更换图片</span>
                   </div>
                 </div>
                 <div v-else class="upload-placeholder">
                   <i class="el-icon-plus"></i>
                   <div class="upload-text">点击上传封面图片</div>
                   <div class="upload-hint">支持 JPG、PNG、GIF，不超过 5MB</div>
                 </div>
               </el-upload>
               
               <div v-if="formData.coverImage" class="image-actions">
                 <el-button size="small" @click.stop="handleRemoveImage">
                   <i class="el-icon-delete"></i> 移除图片
                 </el-button>
               </div>
             </div>
             
             <div class="divider-text">
               <span>或</span>
             </div>
             
             <el-input
               v-model="imageUrl"
               placeholder="输入图片URL"
               clearable
               @blur="handleUrlInput"
             >
               <el-button 
                 slot="append" 
                 @click="handleUrlInput"
                 :disabled="!imageUrl"
               >
                 使用
               </el-button>
             </el-input>
           </el-form-item>

          <el-form-item label="背景色" prop="coverColor">
            <div class="color-selector">
              <el-color-picker
                v-model="formData.coverColor"
                show-alpha
                :predefine="predefineColors"
              />
              <span class="color-value">{{ formData.coverColor }}</span>
              <span class="color-hint">无封面图片时使用</span>
            </div>
            <div class="color-preview-mini">
              <div 
                class="preview-box-mini"
                :style="{ backgroundColor: formData.coverColor }"
              >
                <i class="fas fa-layer-group"></i>
              </div>
            </div>
          </el-form-item>
        </el-collapse-item>
      </el-collapse>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose" :disabled="loading">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm" 
        :loading="loading"
        :disabled="!formData.name"
      >
        <i class="fas fa-check"></i> 创建
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
    return {
      dialogVisible: this.visible,
      loading: false,
      uploadLoading: null,
      activeCollapse: [],
      imageUrl: '',
      uploadAction: process.env.VUE_APP_API_BASE_URL ? `${process.env.VUE_APP_API_BASE_URL}/api/collections/upload-cover` : '/api/collections/upload-cover',
      uploadHeaders: {},
      predefineColors: [
        '#4ECDC4',
        '#FF6B6B',
        '#4A6CF7',
        '#95E1D3',
        '#F38181',
        '#AA96DA',
        '#FCBAD3',
        '#FFFFD2'
      ],
      formData: {
        name: '',
        description: '',
        coverImage: '',
        coverColor: '#4ECDC4',
        isPublic: 0
      },
      rules: {
        name: [
          { required: true, message: '请输入收藏集名称', trigger: 'blur' },
          { max: 100, message: '收藏集名称不能超过100个字符', trigger: 'blur' }
        ],
        coverImage: [
          { 
            pattern: /^(https?:\/\/.+)?$/, 
            message: '请输入有效的 URL，以 http(s):// 开头', 
            trigger: 'blur' 
          }
        ]
      }
    };
  },
  mounted() {
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
    handleOpen() {
      this.$nextTick(() => {
        if (this.$refs.nameInput) {
          this.$refs.nameInput.focus();
        }
      });
    },

    async handleConfirm() {
      if (!this.formData.name || !this.formData.name.trim()) {
        this.$message.warning('请输入收藏集名称');
        return;
      }

      try {
        await this.$refs.form.validate();
        
        this.loading = true;

        const response = await collectionsApi.createCollection({
          name: this.formData.name.trim(),
          description: this.formData.description?.trim() || null,
          coverImage: this.formData.coverImage || null,
          coverColor: this.formData.coverColor,
          isPublic: this.formData.isPublic
        });

        this.$message.success('收藏集创建成功');
        
        this.$emit('success', response.data);
        
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
      if (this.uploadLoading) {
        this.uploadLoading.close();
        this.uploadLoading = null;
      }
      if (this.$refs.form) {
        this.$refs.form.resetFields();
      }
      Object.assign(this.formData, {
        name: '',
        description: '',
        coverImage: '',
        coverColor: '#4ECDC4',
        isPublic: 0
      });
      this.imageUrl = '';
      this.activeCollapse = [];
      this.dialogVisible = false;
      this.$emit('update:visible', false);
    },

    initUploadHeaders() {
      const token = localStorage.getItem('token');
      if (token) {
        this.uploadHeaders = {
          'Authorization': `Bearer ${token}`
        };
      }
    },

    beforeImageUpload(file) {
      const isImage = /^image\/(jpeg|jpg|png|gif)$/.test(file.type);
      if (!isImage) {
        this.$message.error('只支持 JPG、PNG、GIF 格式的图片');
        return false;
      }

      const isLt5M = file.size / 1024 / 1024 < 5;
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB');
        return false;
      }

      const loading = this.$loading({
        lock: true,
        text: '图片上传中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      this.uploadLoading = loading;
      return true;
    },

    handleUploadSuccess(response, file) {
      if (this.uploadLoading) {
        this.uploadLoading.close();
        this.uploadLoading = null;
      }

      if (response && response.code === 200 && response.data) {
        this.formData.coverImage = response.data.url || response.data;
        this.$message.success('图片上传成功');
      } else {
        this.$message.error(response?.message || '图片上传失败');
      }
    },

    handleUploadError(error) {
      if (this.uploadLoading) {
        this.uploadLoading.close();
        this.uploadLoading = null;
      }
      
      console.error('图片上传失败:', error);
      this.$message.error('图片上传失败，请重试');
    },

    handleRemoveImage() {
      this.formData.coverImage = '';
      this.imageUrl = '';
    },

    handleUrlInput() {
      if (this.imageUrl && this.imageUrl.trim()) {
        const urlRegex = /^https?:\/\/.+/;
        if (urlRegex.test(this.imageUrl.trim())) {
          this.formData.coverImage = this.imageUrl.trim();
          this.$message.success('图片URL已设置');
        } else {
          this.$message.warning('请输入有效的图片URL（以http://或https://开头）');
        }
      }
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
      if (!event || !event.target) return;
      
      console.warn('图片加载失败:', event.target.src);
      event.target.style.display = 'none';
      
      this.$message.warning('图片加载失败，请检查URL是否正确');
      this.formData.coverImage = '';
    }
  }
};
</script>

<style scoped>
:deep(.el-dialog) {
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
}

:deep(.el-dialog__header) {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(to bottom, var(--bg-container), var(--bg-page));
}

:deep(.el-dialog__title) {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.el-dialog__body) {
  padding: var(--space-6);
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.el-form) {
  padding: 0;
}

:deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  font-size: var(--font-size-base);
}

.required-field :deep(.el-form-item__label)::before {
  content: '* ';
  color: var(--danger-color);
  margin-right: 4px;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  border: 1px solid var(--border-base);
  background-color: var(--bg-container);
  color: var(--text-primary);
  transition: all var(--transition-base);
}

:deep(.el-input__inner:focus),
:deep(.el-textarea__inner:focus) {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-bg);
}

:deep(.el-input__inner:hover),
:deep(.el-textarea__inner:hover) {
  border-color: var(--primary-light);
}

.visibility-field :deep(.el-radio-group) {
  display: flex;
  gap: var(--space-2);
}

.visibility-field :deep(.el-radio-button__inner) {
  padding: 8px 20px;
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.visibility-field :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.field-hint {
  margin-top: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.field-hint i {
  color: var(--info-color);
}

.advanced-options {
  margin-top: var(--space-4);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background-color: var(--bg-canvas);
}

.advanced-options :deep(.el-collapse-item__header) {
  padding: var(--space-3) var(--space-4);
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-secondary);
  background-color: transparent;
  border: none;
  transition: all var(--transition-base);
}

.advanced-options :deep(.el-collapse-item__header:hover) {
  color: var(--primary-color);
}

.advanced-options :deep(.el-collapse-item__wrap) {
  border: none;
  background-color: transparent;
}

.advanced-options :deep(.el-collapse-item__content) {
  padding: var(--space-4);
  padding-top: 0;
}

.image-preview-wrapper {
  margin-top: var(--space-3);
  padding: var(--space-3);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
  text-align: center;
  border: 1px solid var(--border-light);
}

.preview-image {
  max-width: 100%;
  max-height: 150px;
  border-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.color-selector {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

:deep(.el-color-picker__trigger) {
  border-radius: var(--radius-md);
  border: 1px solid var(--border-base);
  width: 48px;
  height: 48px;
  transition: all var(--transition-base);
}

:deep(.el-color-picker__trigger:hover) {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.color-value {
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  background-color: var(--bg-canvas);
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-light);
}

.color-hint {
  font-size: var(--font-size-xs);
  color: var(--text-placeholder);
}

.color-preview-mini {
  margin-top: var(--space-3);
  display: flex;
  justify-content: center;
}

.preview-box-mini {
  width: 120px;
  height: 60px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}

.upload-section {
  margin-bottom: var(--space-4);
}

.image-uploader {
  width: 100%;
}

:deep(.el-upload) {
  width: 100%;
  display: block;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--border-base);
  transition: all var(--transition-base);
}

.image-preview:hover {
  border-color: var(--primary-color);
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity var(--transition-base);
}

.image-preview:hover .image-overlay {
  opacity: 1;
}

.image-overlay i {
  font-size: 32px;
  margin-bottom: var(--space-2);
}

.image-overlay span {
  font-size: var(--font-size-base);
  font-weight: 500;
}

.upload-placeholder {
  width: 100%;
  height: 200px;
  border: 2px dashed var(--border-base);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-base);
  background-color: var(--bg-canvas);
}

.upload-placeholder:hover {
  border-color: var(--primary-color);
  background-color: var(--primary-bg);
}

.upload-placeholder i {
  font-size: 48px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-3);
}

.upload-text {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
  font-weight: 500;
}

.upload-hint {
  font-size: var(--font-size-xs);
  color: var(--text-placeholder);
}

.image-actions {
  margin-top: var(--space-3);
  display: flex;
  justify-content: center;
}

.divider-text {
  position: relative;
  text-align: center;
  margin: var(--space-4) 0;
}

.divider-text::before,
.divider-text::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 45%;
  height: 1px;
  background-color: var(--border-light);
}

.divider-text::before {
  left: 0;
}

.divider-text::after {
  right: 0;
}

.divider-text span {
  display: inline-block;
  padding: 0 var(--space-2);
  background-color: var(--bg-container);
  color: var(--text-placeholder);
  font-size: var(--font-size-sm);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-light);
  background-color: var(--bg-canvas);
}

.dialog-footer :deep(.el-button) {
  border-radius: var(--radius-md);
  padding: 10px 24px;
  font-weight: 500;
  transition: all var(--transition-base);
}

.dialog-footer :deep(.el-button--primary) {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.dialog-footer :deep(.el-button--primary:hover) {
  background-color: var(--primary-hover);
  border-color: var(--primary-hover);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.dialog-footer :deep(.el-button--primary:active) {
  transform: translateY(0);
}

.dialog-footer :deep(.el-button--primary:disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }

  :deep(.el-dialog__header) {
    padding: var(--space-4);
  }

  :deep(.el-dialog__body) {
    padding: var(--space-4);
    max-height: 60vh;
  }

  .dialog-footer {
    padding: var(--space-3) var(--space-4);
    flex-direction: column-reverse;
  }

  .dialog-footer :deep(.el-button) {
    width: 100%;
  }

  .visibility-field :deep(.el-radio-group) {
    width: 100%;
  }

  .visibility-field :deep(.el-radio-button) {
    flex: 1;
  }

  .color-selector {
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  :deep(.el-dialog__title) {
    font-size: var(--font-size-lg);
  }

  :deep(.el-form-item) {
    margin-bottom: var(--space-4);
  }

  .preview-box-mini {
    width: 100%;
    height: 50px;
  }
}
</style>
