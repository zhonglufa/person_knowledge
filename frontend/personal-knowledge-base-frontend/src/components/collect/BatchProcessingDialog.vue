<template>
  <el-dialog
    :title="`批量加工操作 - ${selectedItems.length} 个收藏项`"
    :visible.sync="dialogVisible"
    width="600px"
    @close="handleClose"
  >
    <div class="batch-processing-dialog">
      <!-- 选择的操作类�?-->
      <div class="operation-selection">
        <el-form :model="form" label-width="120px">
          <el-form-item label="操作类型">
            <el-select 
              v-model="form.operation" 
              placeholder="请选择批量操作"
              @change="handleOperationChange"
            >
              <el-option
                v-for="option in operationOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
                :disabled="option.disabled"
              />
            </el-select>
          </el-form-item>
          
          <!-- 状态选择（仅在状态变更时显示）-->
          <el-form-item 
            v-if="form.operation === 'status'" 
            label="目标状态"
            :rules="[{ required: true, message: '请选择目标状态', trigger: 'change' }]"
          >
            <el-select 
              v-model="form.targetStatus" 
              placeholder="请选择目标状�?
            >
              <el-option
                v-for="status in validStatusOptions"
                :key="status.value"
                :label="status.label"
                :value="status.value"
              />
            </el-select>
            <div class="status-info" v-if="form.targetStatus">
              <el-alert
                :title="getStatusInfo(form.targetStatus)"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </el-form-item>
          
          <!-- 进度选择（仅在设置进度时显示�?-->
          <el-form-item 
            v-if="form.operation === 'progress'" 
            label="学习进度"
            :rules="[{ required: true, message: '请选择学习进度', trigger: 'change' }]"
          >
            <el-select 
              v-model="form.studyProgress" 
              placeholder="请选择学习进度"
            >
              <el-option label="0%" value="0%" />
              <el-option label="25%" value="25%" />
              <el-option label="50%" value="50%" />
              <el-option label="75%" value="75%" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 选中项预览-->
      <div class="selected-items-preview">
        <h4>选中的收藏项 ({{ selectedItems.length }}个)</h4>
        <div class="items-list">
          <div 
            v-for="item in previewItems" 
            :key="item.id"
            class="item-preview"
          >
            <div class="item-title">{{ item.title }}</div>
            <el-tag 
              :type="getDigestStatusType(item.digestStatus)"
              size="mini"
            >
              {{ getDigestStatusText(item.digestStatus) }}
            </el-tag>
          </div>
          <div v-if="selectedItems.length > 5" class="more-items">
            ... 还有 {{ selectedItems.length - 5 }} 个收藏项
          </div>
        </div>
      </div>
      
      <!-- 操作确认信息 -->
      <div class="operation-confirm" v-if="form.operation">
        <el-alert
          :title="getOperationConfirmText()"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
    </div>
    
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button 
        type="primary" 
        @click="handleConfirm"
        :loading="processing"
        :disabled="!isFormValid"
      >
        确认执行
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { collectApi } from '@/api/collect'

export default {
  name: 'BatchProcessingDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    selectedItems: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      dialogVisible: false,
      processing: false,
      form: {
        operation: '',
        targetStatus: '',
        studyProgress: ''
      },
      operationOptions: [
        { 
          value: 'status', 
          label: '批量状态变更',
          disabled: false
        },
        { 
          value: 'progress', 
          label: '批量设置进度',
          disabled: false
        },
        { 
          value: 'reset', 
          label: '批量重置加工',
          disabled: false
        }
      ]
    }
  },
  computed: {
    previewItems() {
      return this.selectedItems.slice(0, 5)
    },
    
    validStatusOptions() {
      // 根据选中项的状态动态计算可选的目标状�?
      const currentStatuses = [...new Set(this.selectedItems.map(item => item.digestStatus))]
      
      // 只有当所有选中项都处于相同状态时，才限制状态流�?
      if (currentStatuses.length === 1) {
        const currentStatus = currentStatuses[0]
        return this.getAllowedStatusTransitions(currentStatus)
      }
      
      // 如果选中项状态不同，只显示通用的合法状�?
      return [
        { value: 'digesting', label: '消化中' },
        { value: 'digested', label: '已消化' },
        { value: 'abandoned', label: '已放弃' }
      ]
    },
    
    isFormValid() {
      if (!this.form.operation) return false
      
      if (this.form.operation === 'status' && !this.form.targetStatus) {
        return false
      }
      
      if (this.form.operation === 'progress' && !this.form.studyProgress) {
        return false
      }
      
      return true
    }
  },
  watch: {
    visible(newVal) {
      this.dialogVisible = newVal
      if (newVal) {
        this.resetForm()
      }
    },
    
    dialogVisible(newVal) {
      this.$emit('update:visible', newVal)
    }
  },
  methods: {
    resetForm() {
      this.form = {
        operation: '',
        targetStatus: '',
        studyProgress: ''
      }
    },
    
    handleClose() {
      this.dialogVisible = false
    },
    
    handleOperationChange() {
      // 重置相关字段
      this.form.targetStatus = ''
      this.form.studyProgress = ''
    },
    
    getAllowedStatusTransitions(currentStatus) {
      const transitions = {
        'undigest': [
          { value: 'digesting', label: '消化中' }
        ],
        'digesting': [
          { value: 'digested', label: '已消化' },
          { value: 'abandoned', label: '已放弃' }
        ],
        'digested': [
          { value: 'digesting', label: '消化中' }
        ],
        'abandoned': [
          { value: 'digesting', label: '消化中' }
        ]
      }
      
      return transitions[currentStatus] || []
    },
    
    getDigestStatusType(status) {
      const statusMap = {
        'undigest': 'info',
        'digesting': 'warning',
        'digested': 'success',
        'abandoned': 'danger'
      }
      return statusMap[status] || 'info'
    },
    
    getDigestStatusText(status) {
      const statusMap = {
        'undigest': '未消化',
        'digesting': '消化中',
        'digested': '已消化',
        'abandoned': '已放弃'
      }
      return statusMap[status] || status
    },
    
    getStatusInfo(status) {
      const infoMap = {
        'digesting': '将选中项标记为消化中状态，可以继续加工',
        'digested': '将选中项标记为已完成状态，学习进度将设为100%',
        'abandoned': '将选中项标记为已放弃状态，学习进度将设为0%'
      }
      return infoMap[status] || ''
    },
    
    getOperationConfirmText() {
      const texts = {
        'status': `将${this.selectedItems.length} 个收藏项的状态批量变更为 "${this.getDigestStatusText(this.form.targetStatus)}"`,
        'progress': `将${this.selectedItems.length} 个收藏项的学习进度批量设置为 "${this.form.studyProgress}"`,
        'reset': `将${this.selectedItems.length} 个收藏项的加工状态重置为"消化中"，进度重置为0%`
      }
      return texts[this.form.operation] || ''
    },
    
    async handleConfirm() {
      if (!this.isFormValid) {
        this.$message.warning('请完善表单信息')
        return
      }
      
      this.processing = true
      
      try {
        const itemIds = this.selectedItems.map(item => item.id)
        let response
        
        switch (this.form.operation) {
          case 'status':
            response = await collectApi.batchUpdateCollectionItemStatus({
              itemIds,
              digestStatus: this.form.targetStatus
            })
            break
            
          case 'progress':
            // 批量设置进度需要逐个更新
            const updatePromises = itemIds.map(id => 
              collectApi.updateCollectionItemDigest(id, {
                studyProgress: this.form.studyProgress
              })
            )
            response = await Promise.all(updatePromises)
            break
            
          case 'reset':
            response = await collectApi.batchUpdateCollectionItemStatus({
              itemIds,
              digestStatus: 'digesting'
            })
            break
        }
        
        if (response) {
          this.$message.success(`批量操作成功完成 (${this.selectedItems.length} 个收藏项)`)
          this.$emit('success')
          this.handleClose()
        }
      } catch (error) {
        console.error('批量操作失败:', error)
        this.$message.error('批量操作失败: ' + (error.message || '未知错误'))
      } finally {
        this.processing = false
      }
    }
  }
}
</script>

<style scoped>
.batch-processing-dialog {
  padding: 20px 0;
}

.operation-selection {
  margin-bottom: 20px;
}

.status-info {
  margin-top: 10px;
}

.selected-items-preview {
  margin: 20px 0;
  padding: 15px;
  background: var(--bg-gray);
  border-radius: 8px;
}

.selected-items-preview h4 {
  margin: 0 0 15px 0;
  color: var(--text-dark);
}

.items-list {
  max-height: 200px;
  overflow-y: auto;
}

.item-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  margin-bottom: 8px;
}

.item-title {
  flex: 1;
  font-size: 14px;
  color: var(--text-dark);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
}

.more-items {
  text-align: center;
  padding: 10px;
  color: var(--text-light);
  font-size: 13px;
}

.operation-confirm {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}

@media (max-width: 768px) {
  .batch-processing-dialog {
    padding: 10px 0;
  }
  
  .item-preview {
    padding: 6px 10px;
  }
  
  .item-title {
    font-size: 13px;
  }
}
</style>
