<template>
  <el-dialog
    title="编辑收藏集"
    :visible.sync="dialogVisible"
    width="500px"
    @close="handleClose"
  >
    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="6" animated />

    <!-- 编辑表单 -->
    <el-form v-else ref="editForm" :model="form" :rules="rules" label-width="80px">
      <!-- 名称 -->
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="form.name"
          placeholder="请输入收藏集名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <!-- 描述 -->
      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入收藏集描述"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 封面图片 -->
      <el-form-item label="封面" prop="coverImage">
        <el-input
          v-model="form.coverImage"
          placeholder="请输入封面图片URL"
          maxlength="300"
        />
        <div v-if="form.coverImage" class="image-preview">
          <img :src="form.coverImage" alt="预览" @error="handleImageError" />
        </div>
      </el-form-item>

      <!-- 是否共享 -->
      <el-form-item label="共享" prop="isShared">
        <el-switch
          v-model="form.isShared"
          active-text="公开共享"
          inactive-text="私有"
        />
      </el-form-item>
    </el-form>

    <!-- 底部操作 -->
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        保存
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { collectionsApi } from '@/api/collections'

export default {
  name: 'CollectionEditDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    collection: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        description: '',
        coverImage: '',
        isShared: false
      },
      rules: {
        name: [
          { required: true, message: '请输入收藏集名称', trigger: 'blur' },
          { min: 1, max: 50, message: '长度在1 �?50 个字符之间', trigger: 'blur' }
        ],
        description: [
          { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
        ]
      },
      loading: false,
      submitting: false
    }
  },
  watch: {
    visible(newVal) {
      this.dialogVisible = newVal
      if (newVal) {
        this.initForm()
      }
    },
    dialogVisible(newVal) {
      this.$emit('update:visible', newVal)
    }
  },
  methods: {
    // 初始化表单
    initForm() {
      if (this.collection && this.collection.id) {
        this.form = {
          id: this.collection.id,
          name: this.collection.name || '',
          description: this.collection.description || '',
          coverImage: this.collection.coverImage || '',
          isShared: this.collection.isShared || false
        }
      }
    },

    // 图片加载错误处理
    handleImageError(event) {
      event.target.style.display = 'none'
    },

    // 提交表单
    async handleSubmit() {
      this.$refs.editForm.validate(async (valid) => {
        if (!valid) {
          return
        }

        this.submitting = true
        try {
          const updateData = {
            name: this.form.name,
            description: this.form.description,
            coverImage: this.form.coverImage,
            isShared: this.form.isShared
          }

          const response = await collectionsApi.updateCollection(this.form.id, updateData)
          
          if (response.code === 200) {
            this.$message.success('收藏集更新成功')
            this.$emit('success', response.data)
            this.handleClose()
          } else {
            this.$message.error(response.message || '更新失败')
          }
        } catch (error) {
          console.error('更新收藏集失败', error)
          this.$message.error('更新失败: ' + (error.response?.data?.message || '未知错误'))
        } finally {
          this.submitting = false
        }
      })
    },

    // 关闭对话框
    handleClose() {
      this.dialogVisible = false
      this.$refs.editForm.resetFields()
    }
  }
}
</script>

<style scoped>
.image-preview {
  margin-top: 10px;
}

.image-preview img {
  max-width: 100%;
  max-height: 150px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
