<template>
  <BaseModal
    :visible="visible"
    title="添加收藏"
    size="small"
    @close="handleClose"
    @confirm="handleConfirm"
    @cancel="handleClose"
    :loading="loading"
  >
    <el-form 
      ref="form" 
      :model="formData" 
      :rules="rules" 
      label-position="top"
      @submit.native.prevent
    >
      <el-form-item label="链接地址" prop="url">
        <el-input
          v-model="formData.url"
          placeholder="请输入完整的网址，以 http(s)://开头"
          clearable
          :loading="parseLoading"
        />
        <div v-if="detectedType" class="type-hint">
          <i class="fas fa-check-circle"></i>
          检测类型: {{ detectedType }}
        </div>
      </el-form-item>
      
      <el-form-item label="标题（可选）" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="自动获取网页标题，也可手动输入"
          clearable
        />
      </el-form-item>

      <el-form-item label="摘要（可选）" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="自动获取网页摘要，也可手动补充"
          clearable
        />
      </el-form-item>
      
      <div v-if="detectedThumbnail && formData.type === 2" class="thumbnail-preview">
        <label>缩略图预覧:</label>
        <img 
          :src="detectedThumbnail" 
          alt="缩略图"
          class="thumbnail-image"
          @error="handleThumbnailError"
        />
      </div>
    </el-form>
    
    <div slot="footer" class="footer-buttons">
      <el-button type="primary" @click="handleConfirm" :loading="loading">确定</el-button>
      <el-button @click="handleClose">取消</el-button>
    </div>
  </BaseModal>
</template>

<script>
import BaseModal from '@/components/common/Modal.vue';
import { collectApi } from '@/api/collect';

export default {
  name: 'AddCollectionModal',
  components: {
    BaseModal
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      parseLoading: false,
      lastParsedUrl: '',
      formData: {
        url: '',
        title: '',
        type: 1,
        description: ''
      },
      detectedType: null,
      detectedThumbnail: null,
      rules: {
        url: [
          { 
            required: true, 
            message: '请输入链接地址', 
            trigger: 'blur' 
          },
          { 
            pattern: /^https?:\/\/.+/, 
            message: '请输入有效的网址，以 http(s):// 开头', 
            trigger: 'blur' 
          }
        ]
      }
    };
  },
  watch: {
    visible(val) {
      if (!val) {
        this.resetFormState();
      }
    },
    'formData.url': function(val) {
      if (!val) {
        this.lastParsedUrl = '';
        this.detectedType = null;
        this.detectedThumbnail = null;
        return;
      }
      if (/^https?:\/\/.+/.test(val)) {
        this.autoParseUrl(val);
      }
    }
  },
  methods: {
    resetFormState() {
      this.formData = {
        url: '',
        title: '',
        type: 1,
        description: ''
      };
      this.detectedType = null;
      this.detectedThumbnail = null;
      this.lastParsedUrl = '';
      this.$refs.form?.clearValidate();
    },

    async autoParseUrl(url) {
      const currentUrl = url.trim();
      if (!currentUrl || currentUrl === this.lastParsedUrl) {
        return;
      }
      try {
        this.parseLoading = true;
        const response = await collectApi.parseUrlMetadata(currentUrl);
        const metadata = response?.data;
        if (!metadata) {
          return;
        }

        this.lastParsedUrl = currentUrl;
        this.formData.url = metadata.normalizedUrl || currentUrl;
        if (!this.formData.title || this.formData.title === this.extractFallbackTitle(currentUrl)) {
          this.formData.title = metadata.title || this.extractFallbackTitle(this.formData.url);
        }
        if (!this.formData.description) {
          this.formData.description = metadata.description || '';
        }
        this.applyDetectedMetadata(metadata);
      } catch (error) {
        this.detectContentType(currentUrl);
        if (!this.formData.title) {
          this.formData.title = this.extractFallbackTitle(currentUrl);
        }
      } finally {
        this.parseLoading = false;
      }
    },

    applyDetectedMetadata(metadata) {
      const sourceType = metadata?.sourceType || 1;
      this.formData.type = sourceType;
      const typeMap = {
        1: '网页',
        2: '图片',
        3: '文本',
        4: '视频'
      };
      this.detectedType = typeMap[sourceType] || '网页';
      this.detectedThumbnail = sourceType === 2 ? (metadata.thumbnail || metadata.normalizedUrl || metadata.url) : (metadata.thumbnail || null);
    },

    detectContentType(url) {
      try {
        const urlObj = new URL(url);
        const pathname = urlObj.pathname.toLowerCase();
        if (/\.(jpg|jpeg|png|gif|webp|svg|bmp)$/i.test(pathname)) {
          this.formData.type = 2;
          this.detectedType = '图片';
          this.detectedThumbnail = url;
          return;
        }
        if (/\.(mp4|avi|mov|mkv|flv|webm|m3u8)$/i.test(pathname)) {
          this.formData.type = 4;
          this.detectedType = '视频';
          this.detectedThumbnail = null;
          return;
        }
        if (/\.(txt|md|doc|docx|pdf)$/i.test(pathname)) {
          this.formData.type = 3;
          this.detectedType = '文本';
          this.detectedThumbnail = null;
          return;
        }
        this.formData.type = 1;
        this.detectedType = '网页';
        this.detectedThumbnail = null;
      } catch (error) {
        this.formData.type = 1;
        this.detectedType = '网页';
        this.detectedThumbnail = null;
      }
    },

    extractFallbackTitle(url) {
      try {
        const urlObj = new URL(url);
        return urlObj.hostname + (urlObj.pathname && urlObj.pathname !== '/' ? urlObj.pathname : '');
      } catch (error) {
        return url;
      }
    },
    
    handleThumbnailError() {
      this.detectedThumbnail = null;
      this.$message.warning('缩略图加载失败');
    },
    
    handleClose() {
      this.$emit('update:visible', false);
    },
    
    async handleConfirm() {
      try {
        await this.$refs.form.validate();
        this.loading = true;
        try {
          const checkResponse = await collectApi.checkUrlExists(this.formData.url);
          if (checkResponse?.data?.exists) {
            this.$message.error('该链接已被收藏');
            return;
          }
        } catch (error) {
          this.$message.warning('无法检查URL是否已存在，将继续添加');
        }

        const collectionData = {
          url: this.formData.url,
          title: this.formData.title,
          type: this.formData.type,
          description: this.formData.description,
          thumbnail: this.detectedThumbnail
        };
        this.$emit('add', collectionData);
      } catch (error) {
        this.$message.error('请检查表单填写是否正确');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
:deep(.modal-content) {
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-lg);
  transition: var(--transition-base);
}

:deep(.modal-header) {
  border-bottom: 1px solid var(--border-color);
  padding: var(--spacing-lg);
  background-color: var(--bg-primary);
  transition: var(--transition-base);
}

:deep(.modal-title) {
  color: var(--text-dark);
  font-size: var(--font-size-lg);
  font-weight: 600;
  transition: var(--transition-base);
}

:deep(.modal-body) {
  padding: var(--spacing-lg);
  background-color: var(--bg-primary);
  transition: var(--transition-base);
}

:deep(.modal-footer) {
  border-top: 1px solid var(--border-color);
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--bg-primary);
  transition: var(--transition-base);
}

:deep(.el-form) {
  background-color: var(--bg-primary);
  transition: var(--transition-base);
}

:deep(.el-form-item) {
  margin-bottom: var(--spacing-md);
  transition: var(--transition-base);
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-dark);
  font-size: var(--font-size-sm);
  margin-bottom: var(--spacing-xs);
  transition: var(--transition-base);
}

:deep(.el-input__inner) {
  border-radius: var(--border-radius-sm);
  border: 1px solid var(--border-color);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-base);
  background-color: var(--bg-primary);
  color: var(--text-dark);
  transition: var(--transition-base);
}
</style>
