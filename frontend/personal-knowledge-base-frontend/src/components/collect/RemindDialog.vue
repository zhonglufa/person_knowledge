<template>
  <el-dialog
    title="设置学习提醒"
    :visible.sync="dialogVisible"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="remind-form">
      <el-form :model="remindForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="提醒时间" prop="remindAt">
          <el-date-picker
            v-model="remindForm.remindAt"
            type="datetime"
            placeholder="选择提醒时间"
            style="width: 100%"
            :picker-options="pickerOptions"
            format="yyyy-MM-dd HH:mm"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="提醒方式">
          <el-checkbox-group v-model="remindForm.remindWays">
            <el-checkbox label="system">系统通知</el-checkbox>
            <el-checkbox label="email">邮件通知</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="备注" prop="note">
          <el-input
            v-model="remindForm.note"
            type="textarea"
            :rows="3"
            placeholder="添加备注信息（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 现有提醒信息 -->
      <div v-if="existingRemind" class="existing-remind">
        <el-alert
          title="已有提醒设置"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <div class="existing-info">
              <p><strong>原提醒时间：</strong>{{ formatDate(existingRemind.remindAt) }}</p>
              <p v-if="existingRemind.note"><strong>备注：</strong>{{ existingRemind.note }}</p>
            </div>
          </template>
        </el-alert>
      </div>
    </div>

    <template slot="footer">
      <el-button v-if="existingRemind" type="danger" @click="handleCancelRemind">取消提醒</el-button>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { collectApi } from '@/api/collect'

export default {
  name: 'RemindDialog',

  props: {
    // 控制对话框显示
    visible: {
      type: Boolean,
      default: false
    },
    // 收藏项ID
    itemId: {
      type: [Number, String],
      required: true
    },
    // 现有提醒信息（可选）
    existingRemind: {
      type: Object,
      default: null
    }
  },

  data() {
    // 验证提醒时间
    const validateRemindTime = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请选择提醒时间'))
      } else {
        const remindTime = new Date(value)
        const now = new Date()
        if (remindTime <= now) {
          callback(new Error('提醒时间必须晚于当前时间'))
        } else {
          callback()
        }
      }
    }

    return {
      loading: false,
      remindForm: {
        remindAt: '',
        remindWays: ['system'],
        note: ''
      },
      rules: {
        remindAt: [
          { required: true, message: '请选择提醒时间', trigger: 'change' },
          { validator: validateRemindTime, trigger: 'change' }
        ]
      },
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8 * 60 * 60 * 1000 // 不能选择过去的时间
        }
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
    // 当对话框打开且有现有提醒时，填充表单
    visible(val) {
      if (val && this.existingRemind) {
        this.remindForm.remindAt = this.existingRemind.remindAt
        this.remindForm.note = this.existingRemind.note || ''
        this.remindForm.remindWays = this.existingRemind.remindWays || ['system']
      } else if (val) {
        // 重置表单
        this.remindForm = {
          remindAt: '',
          remindWays: ['system'],
          note: ''
        }
      }
    }
  },

  methods: {
    /**
     * 格式化日期
     */
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    /**
     * 保存提醒设置
     */
    handleSave() {
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          await collectApi.setRemind(this.itemId, this.remindForm.remindAt)
          this.$message.success('学习提醒设置成功')
          this.$emit('remind-saved', {
            remindAt: this.remindForm.remindAt,
            remindWays: this.remindForm.remindWays,
            note: this.remindForm.note
          })
          this.handleClose()
        } catch (error) {
          console.error('设置提醒失败:', error)
          this.$message.error('设置提醒失败，请稍后重试')
        } finally {
          this.loading = false
        }
      })
    },

    /**
     * 取消提醒
     */
    async handleCancelRemind() {
      try {
        await this.$confirm('确定要取消这个学习提醒吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.loading = true
        await collectApi.cancelRemind(this.itemId)
        this.$message.success('学习提醒已取消')
        this.$emit('remind-cancelled')
        this.handleClose()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消提醒失败:', error)
          this.$message.error('取消提醒失败')
        }
      } finally {
        this.loading = false
      }
    },

    /**
     * 关闭对话框
     */
    handleClose() {
      this.$refs.formRef?.resetFields()
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
.remind-form {
  padding: 10px 0;
}

.existing-remind {
  margin-top: 20px;
}

.existing-info {
  font-size: var(--font-size-sm);
}

.existing-info p {
  margin: 4px 0;
}
</style>