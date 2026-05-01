<template>
  <el-dialog
    :title="`加工编辑 - ${itemData?.title || '加载中...'}`"
    :visible.sync="dialogVisible"
    width="800px"
    @close="handleClose"
    :before-close="handleBeforeClose"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="loadItemDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>
    
    <!-- 编辑内容 -->
    <div v-else-if="itemData" class="digest-editor">
      <!-- 状态进度显示 -->
      <div class="status-section">
        <div class="status-info">
          <el-tag 
            :type="getStatusTagType(itemData.digestStatus)"
            size="medium"
          >
            {{ getStatusText(itemData.digestStatus) }}
          </el-tag>
          <div class="progress-display">
            <span class="progress-label">学习进度:</span>
            <el-progress 
              :percentage="getProgressPercentage(itemData.studyProgress)"
              :status="getProgressStatus(itemData.digestStatus)"
              :show-text="true"
              :stroke-width="12"
            />
          </div>
        </div>
      </div>
      
      <!-- 分步表单 -->
      <el-tabs v-model="activeTab" type="card" class="editor-tabs">
        <!-- 基础信息标签页 -->
        <el-tab-pane label="基础信息" name="basic">
          <div class="tab-content">
            <el-form :model="formData" label-width="100px" size="small">
              <el-form-item label="标题">
                <el-input 
                  v-model="formData.title" 
                  placeholder="收藏项标题"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="来源类型">
                <el-select v-model="formData.sourceType" placeholder="请选择来源类型">
                  <el-option label="网页" :value="1" />
                  <el-option label="图片" :value="2" />
                  <el-option label="文本" :value="3" />
                  <el-option label="视频" :value="4" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="来源">
                <el-input 
                  v-model="formData.source" 
                  placeholder="来源网站或平台"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="源地址">
                <el-input 
                  v-model="formData.storageUrl" 
                  placeholder="原始链接或文件路径"
                  @blur="handleUrlBlur"
                />
                <div v-if="urlParseError" class="error-tip">{{ urlParseError }}</div>
              </el-form-item>
              
              <div class="tab-actions">
                <el-button @click="saveTab('basic')" type="primary" size="small">
                  保存基础信息
                </el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 核心提炼标签页 -->
        <el-tab-pane label="核心提炼" name="core">
          <div class="tab-content">
            <el-form :model="formData" label-width="100px" size="small">
              <el-form-item label="核心摘要">
                <el-input
                  v-model="formData.coreAbstract"
                  type="textarea"
                  :rows="6"
                  placeholder="提炼收藏项的核心内容（200-500字）"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="关键词">
                <el-input
                  v-model="formData.keywords"
                  placeholder="英文逗号分隔，最多10个关键词"
                  maxlength="200"
                />
                <div class="help-tip">示例：MySQL,索引,B+树</div>
              </el-form-item>
              
              <div class="tab-actions">
                <el-button @click="saveTab('core')" type="primary" size="small">
                  保存核心提炼
                </el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
        
        <!-- 学习规划标签页 -->
        <el-tab-pane label="学习规划" name="study">
          <div class="tab-content">
            <el-form :model="formData" label-width="100px" size="small">
              <el-form-item label="学习目标">
                <el-input
                  v-model="formData.studyGoal"
                  type="textarea"
                  :rows="4"
                  placeholder="明确学习/使用目标（最多256字）"
                  maxlength="256"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="复习周期">
                <el-select 
                  v-model="formData.reviewCycle" 
                  placeholder="选择复习周期"
                  clearable
                >
                  <el-option label="1天" value="1天" />
                  <el-option label="3天" value="3天" />
                  <el-option label="1周" value="1周" />
                  <el-option label="2周" value="2周" />
                  <el-option label="1个月" value="1个月" />
                  <el-option label="3个月" value="3个月" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="分类">
                <el-select 
                  v-model="formData.categoryId" 
                  placeholder="选择分类"
                  clearable
                  filterable
                >
                  <!-- 这里应该从后端获取用户分类列表 -->
                  <el-option label="暂无分类" :value="null" />
                </el-select>
              </el-form-item>
              
              <el-form-item 
                v-if="itemData.digestStatus === 'digesting'" 
                label="学习进度"
              >
                <el-select v-model="formData.studyProgress" placeholder="选择进度">
                  <el-option label="0%" value="0%" />
                  <el-option label="25%" value="25%" />
                  <el-option label="50%" value="50%" />
                  <el-option label="75%" value="75%" />
                </el-select>
              </el-form-item>
              
              <div class="tab-actions">
                <el-button @click="saveTab('study')" type="primary" size="small">
                  保存学习规划
                </el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 状态操作区域 -->
      <div class="status-actions">
        <div class="status-buttons">
          <el-button 
            v-if="canMarkComplete" 
            @click="markComplete"
            type="success"
            size="small"
          >
            标记完成
          </el-button>
          
          <el-button 
            v-if="canMarkAbandoned" 
            @click="markAbandoned"
            type="warning"
            size="small"
          >
            标记放弃
          </el-button>
          
          <el-button 
            v-if="canReset" 
            @click="resetDigest"
            type="info"
            size="small"
          >
            重置加工
          </el-button>
        </div>
        
        <div class="save-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button 
            @click="saveAll" 
            type="primary"
            :loading="saving"
          >
            保存全部
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { collectApi } from '@/api/collect'

export default {
  name: 'CollectionItemDigestEditor',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    itemId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      saving: false,
      error: null,
      itemData: null,
      activeTab: 'basic',
      urlParseError: '',
      formData: {
        title: '',
        sourceType: null,
        source: '',
        storageUrl: '',
        coreAbstract: '',
        keywords: '',
        studyGoal: '',
        reviewCycle: '',
        categoryId: null,
        studyProgress: '0%'
      }
    }
  },
  computed: {
    canMarkComplete() {
      return this.itemData?.digestStatus === 'digesting'
    },
    canMarkAbandoned() {
      return this.itemData?.digestStatus === 'digesting'
    },
    canReset() {
      return ['digested', 'abandoned'].includes(this.itemData?.digestStatus)
    }
  },
  watch: {
    visible(newVal) {
      this.dialogVisible = newVal
      if (newVal) {
        this.loadItemDetail()
      }
    },
    dialogVisible(newVal) {
      this.$emit('update:visible', newVal)
    }
  },
  methods: {
    async loadItemDetail() {
      if (!this.itemId) {
        this.error = '缺少收藏项ID'
        this.loading = false
        return
      }
      
      this.loading = true
      this.error = null
      
      try {
        const response = await collectApi.getCollectById(this.itemId)
        if (response.code === 200) {
          this.itemData = response.data
          this.initFormData()
        } else {
          this.error = response.msg || '加载失败'
        }
      } catch (error) {
        console.error('加载收藏项详情失败', error)
        this.error = error.response?.data?.msg || error.message || '网络错误，请稍后重试'
      } finally {
        this.loading = false
      }
    },
    
    initFormData() {
      const item = this.itemData
      this.formData = {
        title: item.title || '',
        sourceType: item.sourceType,
        source: item.source || '',
        storageUrl: item.storageUrl || '',
        coreAbstract: item.coreAbstract || '',
        keywords: item.keywords || '',
        studyGoal: item.studyGoal || '',
        reviewCycle: item.reviewCycle || '',
        categoryId: item.categoryId,
        studyProgress: item.studyProgress || '0%'
      }
    },
    
    getStatusTagType(status) {
      const statusMap = {
        'undigest': 'danger',
        'digesting': 'warning',
        'digested': 'success',
        'abandoned': 'info'
      }
      return statusMap[status] || 'info'
    },
    
    getStatusText(status) {
      const statusMap = {
        'undigest': '待加工',
        'digesting': '加工中',
        'digested': '已完成',
        'abandoned': '已放弃'
      }
      return statusMap[status] || status
    },
    
    getProgressPercentage(progress) {
      const map = {
        '0%': 0,
        '25%': 25,
        '50%': 50,
        '75%': 75,
        '100%': 100
      }
      return map[progress] || 0
    },
    
    getProgressStatus(status) {
      if (status === 'digested') return 'success'
      if (status === 'abandoned') return 'exception'
      return null
    },
    
    handleUrlBlur() {
      const url = this.formData.storageUrl
      if (url && !this.isValidUrl(url)) {
        this.urlParseError = '请输入有效的URL地址'
      } else {
        this.urlParseError = ''
        this.autoParseUrl()
      }
    },
    
    isValidUrl(string) {
      try {
        new URL(string)
        return true
      } catch (_) {
        return false
      }
    },
    
    autoParseUrl() {
      const url = this.formData.storageUrl
      if (!url) return
      
      try {
        const urlObj = new URL(url)
        // 自动填充来源
        if (!this.formData.source) {
          this.formData.source = this.getDomainFromUrl(urlObj)
        }
      } catch (error) {
        console.warn('URL解析失败:', error)
      }
    },
    
    getDomainFromUrl(urlObj) {
      const domain = urlObj.hostname
      const domainMap = {
        'zhihu.com': '知乎',
        'bilibili.com': 'B站',
        'youtube.com': 'YouTube',
        'cnblogs.com': '博客园'
      }
      
      for (const [key, value] of Object.entries(domainMap)) {
        if (domain.includes(key)) {
          return value
        }
      }
      return domain
    },
    
    async saveTab(tabName) {
      if (!this.itemId) {
        this.$message.error('缺少收藏项ID')
        return
      }
      
      this.saving = true
      try {
        const updateData = this.getTabUpdateData(tabName)
        const response = await collectApi.updateCollectionItemDigest(this.itemId, updateData)
        
        if (response.code === 200) {
          this.itemData = response.data
          this.initFormData() // 更新表单数据以反映最新的更改
          this.$message.success('保存成功')
          this.$emit('updated', response.data)
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
      } finally {
        this.saving = false
      }
    },
    
    getTabUpdateData(tabName) {
      const updateData = {}
      
      switch (tabName) {
        case 'basic':
          updateData.title = this.formData.title
          updateData.sourceType = this.formData.sourceType
          updateData.source = this.formData.source
          updateData.storageUrl = this.formData.storageUrl
          break
        case 'core':
          updateData.coreAbstract = this.formData.coreAbstract
          updateData.keywords = this.formData.keywords
          break
        case 'study':
          updateData.studyGoal = this.formData.studyGoal
          updateData.reviewCycle = this.formData.reviewCycle
          updateData.categoryId = this.formData.categoryId
          updateData.studyProgress = this.formData.studyProgress
          break
      }
      
      return updateData
    },
    
    async saveAll() {
      if (!this.itemId) {
        this.$message.error('缺少收藏项ID')
        return
      }
      
      this.saving = true
      try {
        const response = await collectApi.updateCollectionItemDigest(this.itemId, this.formData)
        
        if (response.code === 200) {
          this.itemData = response.data
          this.initFormData() // 更新表单数据以反映最新的更改
          this.$message.success('保存成功')
          this.$emit('updated', response.data)
          this.handleClose()
        } else {
          this.$message.error(response.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        this.$message.error('保存失败: ' + (error.response?.data?.msg || error.message || '未知错误'))
      } finally {
        this.saving = false
      }
    },
    
    async updateStatus(status) {
      if (!this.itemId) {
        this.$message.error('缺少收藏项ID')
        return
      }
      
      try {
        const response = await collectApi.updateCollectionItemStatus(this.itemId, status)
        
        if (response.code === 200) {
          this.itemData = response.data
          this.initFormData()
          this.$message.success('状态更新成功')
          this.$emit('updated', response.data)
        } else {
          this.$message.error(response.msg || '状态更新失败')
        }
      } catch (error) {
        console.error('状态更新失败', error)
        this.$message.error('状态更新失败 ' + (error.response?.data?.msg || error.message || '未知错误'))
      }
    },
    
    markComplete() {
      this.$confirm('确定要标记为完成吗？完成后学习进度将设为100%。', '确认标记', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.updateStatus('digested')
      })
    },
    
    markAbandoned() {
      this.$confirm('确定要标记为放弃吗？放弃后学习进度将设为0%。', '确认标记', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.updateStatus('abandoned')
      })
    },
    
    resetDigest() {
      this.$confirm('确定要重置加工状态吗？将回到加工中状态，进度设为0%。', '确认重置', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        this.updateStatus('digesting')
      })
    },
    
    handleBeforeClose(done) {
      if (this.saving) {
        this.$message.warning('正在保存中，请稍后。')
        return
      }
      done()
    },
    
    handleClose() {
      this.dialogVisible = false
      this.resetForm()
    },
    
    resetForm() {
      this.itemData = null
      this.error = null
      this.activeTab = 'basic'
      this.urlParseError = ''
      this.formData = {
        title: '',
        sourceType: null,
        source: '',
        storageUrl: '',
        coreAbstract: '',
        keywords: '',
        studyGoal: '',
        reviewCycle: '',
        categoryId: null,
        studyProgress: '0%'
      }
    }
  }
}
</script>

<style scoped>
.digest-editor {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.status-section {
  background: var(--bg-secondary);
  padding: 15px;
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.status-info {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.progress-display {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 200px;
}

.progress-label {
  font-size: 14px;
  color: var(--text-light);
  white-space: nowrap;
}

.editor-tabs {
  flex: 1;
}

.tab-content {
  padding: 10px 0;
}

.tab-actions {
  margin-top: 20px;
  text-align: right;
}

.status-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid var(--border-light);
  flex-wrap: wrap;
  gap: 15px;
}

.status-buttons, .save-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.help-tip {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 5px;
}

.error-tip {
  font-size: 12px;
  color: var(--error-color);
  margin-top: 5px;
}

.loading-container, .error-container {
  padding: 40px 20px;
  text-align: center;
}

/* 响应式设置*/
@media (max-width: 768px) {
  .status-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .progress-display {
    width: 100%;
  }
  
  .status-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .status-buttons, .save-actions {
    justify-content: center;
  }
}
</style>
