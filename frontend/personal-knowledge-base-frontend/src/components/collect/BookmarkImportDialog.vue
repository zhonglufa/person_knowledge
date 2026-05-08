<template>
  <el-dialog
    title="导入浏览器收藏夹"
    :visible.sync="dialogVisible"
    width="720px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 步骤条 -->
    <el-steps :active="currentStep" finish-status="success" class="import-steps">
      <el-step title="选择文件" description="上传HTML书签文件"></el-step>
      <el-step title="预览确认" description="查看导入预览"></el-step>
      <el-step title="导入完成" description="查看导入结果"></el-step>
    </el-steps>

    <!-- 步骤1：选择文件 -->
    <div v-show="currentStep === 0" class="step-content">
      <div class="file-upload-area">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          action=""
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".html,.htm"
        >
          <i class="el-icon-upload upload-icon"></i>
          <div class="el-upload__text">
            将浏览器导出的HTML书签文件拖到此处，或<em>点击选择</em>
          </div>
          <div class="el-upload__tip" slot="tip">
            支持 .html / .htm 格式的浏览器书签导出文件
          </div>
        </el-upload>

        <div v-if="selectedFile" class="file-info">
          <el-alert
            :title="`已选择文件: ${selectedFile.name}`"
            type="success"
            :closable="false"
            show-icon
          >
            <template slot="default">
              文件大小: {{ formatFileSize(selectedFile.size) }}
            </template>
          </el-alert>
        </div>
      </div>

      <!-- 选择目标收藏集 -->
      <div class="target-collection-select">
        <h4 class="section-title">选择目标收藏集</h4>
        <el-select
          v-model="targetCollectionId"
          placeholder="请选择目标收藏集"
          style="width: 100%;"
          filterable
        >
          <el-option label="创建新收藏集" value="new"></el-option>
          <el-option
            v-for="collection in collections"
            :key="collection.id"
            :label="collection.name"
            :value="collection.id"
          />
        </el-select>

        <el-input
          v-if="targetCollectionId === 'new'"
          v-model="newCollectionName"
          placeholder="请输入新收藏集名称"
          class="mt-2"
          maxlength="50"
          show-word-limit
        />
      </div>

      <div class="step-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :disabled="!canProceedToPreview"
          @click="handlePreview"
        >下一步：预览</el-button>
      </div>
    </div>

    <!-- 步骤2：预览确认 -->
    <div v-show="currentStep === 1" class="step-content">
      <div v-if="previewLoading" class="preview-loading">
        <i class="el-icon-loading"></i>
        <span>正在解析书签文件...</span>
      </div>

      <div v-else-if="previewResult" class="preview-result">
        <div class="preview-summary">
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-number">{{ previewResult.totalCount }}</div>
                <div class="summary-label">总书签数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item success">
                <div class="summary-number">{{ previewResult.validCount }}</div>
                <div class="summary-label">有效书签</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item warning">
                <div class="summary-number">{{ previewResult.duplicateCount }}</div>
                <div class="summary-label">重复书签</div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 书签预览列表 -->
        <div class="preview-list">
          <h4 class="section-title">书签预览</h4>
          <el-table
            :data="previewResult.items"
            max-height="300"
            border
            size="small"
          >
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag
                  :type="scope.row.status === 'new' ? 'success' : 'warning'"
                  size="mini"
                >
                  {{ scope.row.status === 'new' ? '新增' : '重复' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="150">
              <template slot-scope="scope">
                <span class="truncate-text">{{ scope.row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="url" label="URL" min-width="200">
              <template slot-scope="scope">
                <span class="truncate-text url-text">{{ scope.row.url }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="folder" label="文件夹" width="120" align="center" />
          </el-table>
        </div>
      </div>

      <div class="step-footer">
        <el-button @click="currentStep = 0">上一步</el-button>
        <el-button type="primary" @click="handleImport">开始导入</el-button>
      </div>
    </div>

    <!-- 步骤3：导入结果 -->
    <div v-show="currentStep === 2" class="step-content">
      <div v-if="importing" class="importing-progress">
        <el-progress
          :percentage="importProgress"
          :status="importProgress === 100 ? 'success' : ''"
          :stroke-width="16"
        />
        <p class="progress-text">{{ importProgressText }}</p>
      </div>

      <div v-else-if="importResult" class="import-result">
        <el-result
          :icon="importResult.successCount > 0 ? 'success' : 'warning'"
          :title="importResult.successCount > 0 ? '导入完成' : '导入失败'"
          :sub-title="getResultSubtitle()"
        >
          <template slot="extra">
            <div class="result-stats">
              <el-row :gutter="16">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number success">{{ importResult.successCount }}</div>
                    <div class="stat-label">成功导入</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number warning">{{ importResult.skippedCount }}</div>
                    <div class="stat-label">跳过重复</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-number danger">{{ importResult.failedCount }}</div>
                    <div class="stat-label">导入失败</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </template>
        </el-result>
      </div>

      <div class="step-footer">
        <el-button type="primary" @click="handleClose">完成</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { collectApi } from '@/api/collect'
import { collectionsApi } from '@/api/collections'

export default {
  name: 'BookmarkImportDialog',

  props: {
    // 控制对话框显示
    visible: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      // 当前步骤 (0: 选择文件, 1: 预览, 2: 结果)
      currentStep: 0,

      // 选中的文件
      selectedFile: null,

      // 目标收藏集
      targetCollectionId: '',
      newCollectionName: '',

      // 收藏集列表
      collections: [],

      // 预览相关
      previewLoading: false,
      previewResult: null,
      htmlContent: '',

      // 导入相关
      importing: false,
      importProgress: 0,
      importProgressText: '',
      importResult: null
    }
  },

  computed: {
    /**
     * 对话框显示状态（使用sync修饰符）
     */
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },

    /**
     * 是否可以进入预览步骤
     */
    canProceedToPreview() {
      if (!this.selectedFile) return false
      if (this.targetCollectionId === 'new' && !this.newCollectionName.trim()) return false
      return true
    }
  },

  watch: {
    /**
     * 监听对话框打开，加载收藏集列表
     */
    dialogVisible(val) {
      if (val) {
        this.loadCollections()
      }
    }
  },

  methods: {
    /**
     * 加载收藏集列表
     */
    async loadCollections() {
      try {
        // 调用后端获取收藏集列表API
        const response = await collectionsApi.getUserCollections()
        const payload = response?.data?.data || response?.data || response || []
        this.collections = Array.isArray(payload) ? payload : (payload.list || payload.records || [])
        // 默认选择第一个收藏集
        if (this.collections.length > 0) {
          this.targetCollectionId = this.collections[0].id
        }
      } catch (error) {
        console.error('加载收藏集失败:', error)
        this.$message.error('加载收藏集列表失败')
      }
    },

    /**
     * 文件选择变化
     */
    handleFileChange(file) {
      this.selectedFile = file.raw
    },

    /**
     * 格式化文件大小
     */
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
    },

    /**
     * 进入预览步骤
     */
    async handlePreview() {
      this.currentStep = 1
      this.previewLoading = true

      try {
        const fileContent = await this.readFileAsText(this.selectedFile)
        this.htmlContent = fileContent

        const response = await collectApi.importPreview({ htmlContent: fileContent })
        const payload = response?.data ?? response ?? {}

        this.previewResult = {
          totalCount: payload.totalCount || 0,
          validCount: payload.totalCount || 0,
          duplicateCount: 0,
          items: payload.items || []
        }
      } catch (error) {
        console.error('预览失败:', error)
        this.$message.error('文件解析失败，请检查文件格式')
        this.currentStep = 0
      } finally {
        this.previewLoading = false
      }
    },

    /**
     * 读取文件为文本
     */
    readFileAsText(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = (e) => resolve(e.target.result)
        reader.onerror = reject
        reader.readAsText(file)
      })
    },

    /**
     * 开始导入
     */
    async handleImport() {
      this.importing = true
      this.importProgress = 0
      this.importProgressText = '正在准备导入...'

      try {
        let targetId = this.targetCollectionId

        if (this.targetCollectionId === 'new') {
          this.importProgressText = '正在创建收藏集...'
          try {
            const createResponse = await collectionsApi.createCollection({
              name: this.newCollectionName.trim()
            })
            targetId = createResponse?.data?.id
            if (!targetId) {
              throw new Error('创建收藏集失败')
            }
          } catch (err) {
            console.error('创建收藏集失败:', err)
            this.$message.error('创建收藏集失败')
            return
          }
        }

        this.importProgressText = '正在导入书签...'

        const response = await collectApi.importExecute({
          htmlContent: this.htmlContent,
          targetCollectionId: targetId
        })

        const payload = response?.data ?? response ?? {}
        this.importResult = {
          successCount: payload.successCount || 0,
          skippedCount: 0,
          failedCount: payload.failCount || 0,
          errors: payload.errors || []
        }

        this.currentStep = 2
      } catch (error) {
        console.error('导入失败:', error)
        this.$message.error('导入失败，请稍后重试')
      } finally {
        this.importing = false
      }
    },

    /**
     * 获取导入结果描述
     */
    getResultSubtitle() {
      if (!this.importResult) return ''
      const { successCount, skippedCount, failedCount } = this.importResult
      let subtitle = `成功导入 ${successCount} 条书签`
      if (skippedCount > 0) subtitle += `，跳过 ${skippedCount} 条重复`
      if (failedCount > 0) subtitle += `，${failedCount} 条导入失败`
      return subtitle
    },

    /**
     * 关闭对话框并重置状态
     */
    handleClose() {
      this.currentStep = 0
      this.selectedFile = null
      this.targetCollectionId = ''
      this.newCollectionName = ''
      this.previewResult = null
      this.htmlContent = ''
      this.importResult = null
      this.importProgress = 0
      this.importProgressText = ''
      this.dialogVisible = false
      this.$emit('import-complete')
    }
  }
}
</script>

<style scoped>
/* ========== 步骤条 ========== */
.import-steps {
  margin-bottom: var(--space-6);
}

.step-content {
  min-height: 300px;
}

/* ========== 文件上传区域 ========== */
.file-upload-area {
  margin-bottom: var(--space-5);
}

.upload-icon {
  font-size: 64px;
  color: var(--primary-color);
  margin-bottom: var(--space-3);
}

.file-info {
  margin-top: var(--space-4);
}

/* ========== 目标收藏集选择 ========== */
.target-collection-select {
  margin-top: var(--space-5);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-3) 0;
}

/* ========== 预览区域 ========== */
.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-medium);
}

.preview-loading i {
  font-size: 32px;
  margin-bottom: var(--space-3);
}

/* 预览汇总 */
.preview-summary {
  margin-bottom: var(--space-5);
}

.summary-item {
  text-align: center;
  padding: var(--space-4);
  background-color: var(--bg-canvas);
  border-radius: var(--radius-md);
}

.summary-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.summary-item.success .summary-number {
  color: var(--success-color);
}

.summary-item.warning .summary-number {
  color: var(--warning-color);
}

.summary-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

/* 预览列表 */
.preview-list {
  margin-top: var(--space-4);
}

.truncate-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.url-text {
  color: var(--primary-color);
}

/* ========== 导入进度 ========== */
.importing-progress {
  text-align: center;
  padding: 40px 20px;
}

.progress-text {
  margin-top: var(--space-4);
  color: var(--text-regular);
  font-size: var(--font-size-base);
}

/* ========== 导入结果 ========== */
.import-result {
  padding: var(--space-4) 0;
}

.result-stats {
  margin-top: var(--space-4);
}

.stat-item {
  text-align: center;
  padding: var(--space-3);
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
}

.stat-number.success {
  color: var(--success-color);
}

.stat-number.warning {
  color: var(--warning-color);
}

.stat-number.danger {
  color: var(--danger-color);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

/* ========== 步骤底部按钮 ========== */
.step-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-light);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .summary-item {
    margin-bottom: var(--space-3);
  }

  .preview-list :deep(.el-table) {
    font-size: var(--font-size-xs);
  }
}
</style>
