<template>
  <el-dialog
    :visible.sync="dialogVisible"
    title="添加收藏项"
    width="600px"
    :before-close="handleClose"
    class="add-collection-item-dialog"
  >
    <el-form
      ref="form"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      @submit.native.prevent="handleSubmit"
    >
      <!-- 标题 -->
      <el-form-item label="标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入收藏项标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 来源类型 -->
      <el-form-item label="来源类型" prop="sourceType">
        <el-select
          v-model="formData.sourceType"
          placeholder="请选择来源类型"
          style="width: 100%"
        >
          <el-option label="网页" :value="1" />
          <el-option label="图片" :value="2" />
          <el-option label="文本" :value="3" />
          <el-option label="视频" :value="4" />
        </el-select>
      </el-form-item>
      
      <!-- URL/文件路径 -->
      <el-form-item 
        v-if="[1, 2, 4].includes(formData.sourceType)" 
        label="链接地址" 
        prop="storageUrl"
      >
        <el-input
          v-model="formData.storageUrl"
          placeholder="请输入网址或文件路径"
          maxlength="500"
        >
          <template #append>
            <el-button 
              icon="el-icon-magic-stick" 
              @click="autoFillFromUrl"
              :disabled="!formData.storageUrl"
            >
              自动填充
            </el-button>
          </template>
        </el-input>
      </el-form-item>
      
      <!-- 文本内容 -->
      <el-form-item 
        v-if="formData.sourceType === 3" 
        label="文本内容" 
        prop="textContent"
      >
        <el-input
          v-model="formData.textContent"
          type="textarea"
          :rows="6"
          placeholder="请输入文本内容"
          maxlength="5000"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 摘要 -->
      <el-form-item label="核心摘要" prop="coreAbstract">
        <el-input
          v-model="formData.coreAbstract"
          type="textarea"
          :rows="4"
          placeholder="请输入核心摘要或笔记"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
      
      <!-- 来源网站 -->
      <el-form-item label="来源网站" prop="source">
        <el-input
          v-model="formData.source"
          placeholder="如：知乎、B站、GitHub"
          maxlength="100"
        />
      </el-form-item>
      
      <!-- 关键词-->
      <el-form-item label="关键词" prop="keywords">
        <el-input
          v-model="formData.keywords"
          placeholder="请输入关键词，用英文逗号分隔"
          maxlength="256"
        />
      </el-form-item>
      
      <!-- 标签选择 -->
      <el-form-item label="标签" prop="tagIds">
        <div class="tags-selector">
          <div class="selected-tags">
            <el-tag
              v-for="tag in selectedTags"
              :key="tag.id"
              :color="tag.color"
              closable
              @close="removeTag(tag)"
              class="selected-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
          
          <el-select
            v-model="newTagInput"
            placeholder="添加标签"
            filterable
            allow-create
            default-first-option
            @change="addTag"
            class="tag-select"
          >
            <el-option
              v-for="tag in availableTags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            >
              <span class="tag-option">
                <span class="tag-color-dot" :style="{ backgroundColor: tag.color }"></span>
                {{ tag.name }}
              </span>
            </el-option>
          </el-select>
        </div>
      </el-form-item>
      
      <!-- 分类选择 -->
      <el-form-item label="分类" prop="categoryId">
        <el-select
          v-model="formData.categoryId"
          placeholder="请选择分类"
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="category in categoryOptions"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          >
            <span>{{ category.name }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      
      <!-- 学习目标 -->
      <el-form-item label="学习目标" prop="studyGoal">
        <el-input
          v-model="formData.studyGoal"
          placeholder="请输入学习目标"
          maxlength="256"
        />
      </el-form-item>
      
      <!-- 复习周期 -->
      <el-form-item label="复习周期" prop="reviewCycle">
        <el-select
          v-model="formData.reviewCycle"
          placeholder="请选择复习周期"
          style="width: 100%"
        >
          <el-option label="1天" value="1天" />
          <el-option label="3天" value="3天" />
          <el-option label="1周" value="1周" />
          <el-option label="2周" value="2周" />
          <el-option label="1个月" value="1个月" />
          <el-option label="3个月" value="3个月" />
          <el-option label="6个月" value="6个月" />
          <el-option label="1年" value="1年" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          确定
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { collectApi } from '@/api/collect'

export default {
  name: 'AddCollectionItemDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    collectionId: {
      type: [Number, String],
      default: null
    }
  },
  data() {
    return {
      submitting: false,
      newTagInput: '',
      selectedTags: [],
      availableTags: [],
      categoryOptions: [], // 添加分类选项
      
      formData: {
        title: '',
        sourceType: 1,
        storageUrl: '',
        textContent: '',
        coreAbstract: '',
        source: '',
        keywords: '',
        tagIds: [],
        categoryId: null, // 添加分类ID字段
        studyGoal: '',
        reviewCycle: ''
      },
      
      formRules: {
        title: [
          { required: true, message: '请输入标题', trigger: 'blur' },
          { max: 200, message: '标题长度不能超过200个字', trigger: 'blur' }
        ],
        sourceType: [
          { required: true, message: '请选择来源类型', trigger: 'change' }
        ],
        storageUrl: [
          { 
            required: true, 
            message: '请输入链接地址', 
            trigger: 'blur',
            validator: (rule, value, callback) => {
              if ([1, 2, 4].includes(this.formData.sourceType) && !value) {
                callback(new Error('请输入链接地址'))
              } else {
                callback()
              }
            }
          }
        ],
        textContent: [
          { 
            required: true, 
            message: '请输入文本内容', 
            trigger: 'blur',
            validator: (rule, value, callback) => {
              if (this.formData.sourceType === 3 && !value) {
                callback(new Error('请输入文本内容'))
              } else {
                callback()
              }
            }
          }
        ]
      }
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.resetForm()
        this.loadTags()
        this.loadCategories() // 添加加载分类
      }
    }
  },
  methods: {
    async loadTags() {
      try {
        const response = await collectApi.getTags()
        if (response.code === 200) {
          this.availableTags = response.data || []
        }
      } catch (error) {
        console.error('加载标签失败:', error)
      }
    },
    
    async loadCategories() {
      try {
        const response = await collectApi.getCategoryList()
        if (response.code === 200) {
          this.categoryOptions = response.data || []
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    
    addTag(tagId) {
      if (!tagId) return
      
      const tag = this.availableTags.find(t => t.id === tagId)
      if (tag && !this.selectedTags.some(t => t.id === tag.id)) {
        this.selectedTags.push(tag)
        this.updateTagIds()
      }
      this.newTagInput = ''
    },
    
    removeTag(tag) {
      const index = this.selectedTags.findIndex(t => t.id === tag.id)
      if (index > -1) {
        this.selectedTags.splice(index, 1)
        this.updateTagIds()
      }
    },
    
    updateTagIds() {
      this.formData.tagIds = this.selectedTags.map(tag => tag.id)
    },
    
    async autoFillFromUrl() {
      if (!this.formData.storageUrl) return
      
      try {
        // 这里可以调用爬虫API来自动获取网页标题等信息
        // 暂时模拟自动填充
        const urlObj = new URL(this.formData.storageUrl)
        this.formData.source = urlObj.hostname.replace('www.', '')
        this.formData.title = this.formData.title || `来自${this.formData.source}的内容`
        
        this.$message.success('自动填充完成')
      } catch (error) {
        console.error('自动填充失败:', error)
        this.$message.warning('URL格式不正确')
      }
    },
    
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
        
        this.submitting = true
        
        // 准备提交数据
        const submitData = {
          ...this.formData,
          collectionId: this.collectionId
        }
        
        // 如果是文本类型，将textContent放入storageUrl
        if (this.formData.sourceType === 3) {
          submitData.storageUrl = this.formData.textContent
        }
        
        const response = await collectApi.createCollect(submitData)
        
        if (response.code === 200) {
          this.$message.success('添加成功')
          this.$emit('success', response.data)
          this.handleClose()
        } else {
          this.$message.error(response.message || '添加失败')
        }
      } catch (error) {
        if (error.field) {
          // 表单验证错误
          return
        }
        console.error('添加收藏项失败:', error)
        this.$message.error('添加失败')
      } finally {
        this.submitting = false
      }
    },
    
    handleClose() {
      this.dialogVisible = false
      this.resetForm()
    },
    
    resetForm() {
      this.$refs.form?.resetFields()
      this.selectedTags = []
      this.newTagInput = ''
      
      // 重置表单数据
      this.formData = {
        title: '',
        sourceType: 1,
        storageUrl: '',
        textContent: '',
        coreAbstract: '',
        source: '',
        keywords: '',
        tagIds: [],
        studyGoal: '',
        reviewCycle: ''
      }
    }
  }
}
</script>

<style scoped>
.add-collection-item-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.tags-selector {
  width: 100%;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  min-height: 32px;
}

.selected-tag {
  margin: 0;
  border: none;
}

.tag-select {
  width: 100%;
}

.tag-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tag-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设置*/
@media (max-width: 768px) {
  .add-collection-item-dialog {
    width: 95% !important;
    max-width: 600px;
  }
}
</style>
