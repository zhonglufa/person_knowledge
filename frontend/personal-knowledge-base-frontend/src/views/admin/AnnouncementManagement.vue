<template>
  <div class="announcement-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">公告管理</h2>
        <p class="page-desc">管理系统公告，发布重要通知和信息</p>
      </div>
      <div class="header-right">
        <el-button
          type="warning"
          icon="el-icon-time"
          v-permission="'announcement:publish'"
          @click="handleScheduleAnnouncement"
        >定时发布</el-button>
        <el-button
          type="info"
          icon="el-icon-document-copy"
          v-permission="'announcement:view'"
          @click="showTemplateDialog"
        >模板管理</el-button>
        <el-button
          type="primary"
          v-permission="'announcement:create'"
          @click="handleCreateAnnouncement"
        >
          <i class="el-icon-plus"></i> 发布新公告
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <!-- 公告类型筛选：前端展示，后端暂不支持 -->
        <el-form-item label="公告类型">
          <el-select
            v-model="queryParams.type"
            placeholder="全部类型"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="系统公告" value="system"></el-option>
            <el-option label="活动通知" value="activity"></el-option>
            <el-option label="维护通知" value="maintenance"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="全部" value=""></el-option>
            <el-option label="草稿" :value="0"></el-option>
            <el-option label="已发布" :value="1"></el-option>
            <el-option label="已下架" :value="2"></el-option>
            <el-option label="已过期" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="announcementList"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="title" label="标题" min-width="200" align="center">
          <template slot-scope="scope">
            <span class="title-text">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getTypeColor(scope.row.type)" size="small">
              {{ getTypeText(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getPriorityColor(scope.row.priority)" size="small">
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <!-- 后端状态: 0=草稿, 1=已发布, 2=已下架, 3=已过期 -->
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveAt" label="生效时间" min-width="170" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.effectiveAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="expireAt" label="结束时间" min-width="170" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.expireAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" align="center">
          <template slot-scope="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template slot-scope="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="mini"
                icon="el-icon-edit"
                v-permission="'announcement:update'"
                @click="handleEditAnnouncement(scope.row)"
              >编辑</el-button>
              <el-button
                v-if="scope.row.status === 0"
                type="success"
                size="mini"
                icon="el-icon-top"
                v-permission="'announcement:publish'"
                @click="handlePublishAnnouncement(scope.row)"
              >发布</el-button>
              <el-button
                v-if="scope.row.status === 1"
                type="warning"
                size="mini"
                icon="el-icon-bottom"
                v-permission="'announcement:update'"
                @click="handleTakeDownAnnouncement(scope.row)"
              >下架</el-button>
              <el-button
                v-if="scope.row.status === 2"
                type="success"
                size="mini"
                icon="el-icon-top"
                v-permission="'announcement:publish'"
                @click="handleRepublishAnnouncement(scope.row)"
              >重新发布</el-button>
              <el-button
                type="info"
                size="mini"
                icon="el-icon-data-analysis"
                v-permission="'announcement:view'"
                @click="handleViewStatistics(scope.row)"
              >统计</el-button>
              <el-button
                type="danger"
                size="mini"
                icon="el-icon-delete"
                v-permission="'announcement:delete'"
                @click="handleDeleteAnnouncement(scope.row)"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          background
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑公告对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="640px"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <el-form
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="announcementRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input
            v-model="announcementForm.title"
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="生效时间" prop="effectiveAt">
          <el-date-picker
            v-model="announcementForm.effectiveAt"
            type="datetime"
            placeholder="选择生效时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="失效时间" prop="expireAt">
          <el-date-picker
            v-model="announcementForm.expireAt"
            type="datetime"
            placeholder="选择失效时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="公告类型" prop="type">
          <el-select v-model="announcementForm.type" placeholder="请选择公告类型" style="width: 100%;">
            <el-option label="系统公告" value="system"></el-option>
            <el-option label="活动通知" value="activity"></el-option>
            <el-option label="维护通知" value="maintenance"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-select v-model="announcementForm.priority" placeholder="请选择优先级" style="width: 100%;">
            <el-option label="低" value="low"></el-option>
            <el-option label="中" value="medium"></el-option>
            <el-option label="高" value="high"></el-option>
            <el-option label="紧急" value="urgent"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 公告统计对话框 -->
    <el-dialog
      title="公告触达统计"
      :visible.sync="statisticsDialogVisible"
      width="560px"
    >
      <div v-loading="statisticsLoading" class="statistics-content">
        <div class="stat-item">
          <span class="stat-label">总用户数</span>
          <span class="stat-value">{{ statisticsData.totalUsers || 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已读用户数</span>
          <span class="stat-value success">{{ statisticsData.readUsers || 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">未读用户数</span>
          <span class="stat-value warning">{{ statisticsData.unreadUsers || 0 }}</span>
        </div>
        <div class="stat-item highlight">
          <span class="stat-label">阅读率</span>
          <span class="stat-value primary">{{ statisticsData.readRate || '0%' }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 定时发布对话框 -->
    <el-dialog
      title="定时发布公告"
      :visible.sync="scheduleDialogVisible"
      width="680px"
    >
      <el-tabs v-model="scheduleActiveTab">
        <el-tab-pane label="新建定时任务" name="create">
          <el-form label-width="100px">
            <el-form-item label="选择公告">
              <el-select v-model="scheduleForm.announcementId" placeholder="请选择公告" style="width: 100%;">
                <el-option
                  v-for="item in announcementList.filter(a => a.status === 0)"
                  :key="item.id"
                  :label="item.title"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="发布时间">
              <el-date-picker
                v-model="scheduleForm.scheduledAt"
                type="datetime"
                placeholder="选择发布时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                :picker-options="pickerOptions"
                style="width: 100%;"
              />
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="scheduleDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitSchedule">确 定</el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已有定时任务" name="list">
          <div v-loading="scheduleListLoading" class="schedule-list">
            <div v-if="scheduleList.length === 0" class="empty-schedule">
              暂无定时任务
            </div>
            <div v-for="schedule in scheduleList" :key="schedule.id" class="schedule-item">
              <div class="schedule-info">
                <div class="schedule-title">{{ schedule.announcementTitle }}</div>
                <div class="schedule-time">
                  <el-tag size="mini" type="warning">定时发布</el-tag>
                  <span>{{ schedule.scheduledAt }}</span>
                </div>
              </div>
              <div class="schedule-actions">
                <el-button type="danger" size="mini" @click="handleCancelSchedule(schedule)">取消</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 模板管理对话框 -->
    <el-dialog
      title="公告模板管理"
      :visible.sync="templateDialogVisible"
      width="640px"
    >
      <el-form label-width="100px" class="template-form">
        <el-form-item label="模板名称">
          <el-input v-model="newTemplate.name" placeholder="输入模板名称" />
        </el-form-item>
        <el-form-item label="模板内容">
          <el-input v-model="newTemplate.content" type="textarea" :rows="4" placeholder="输入模板内容" />
        </el-form-item>
        <el-form-item label="模板类型">
          <el-select v-model="newTemplate.type" style="width: 100%;">
            <el-option label="系统公告" value="system"></el-option>
            <el-option label="活动通知" value="activity"></el-option>
            <el-option label="维护通知" value="maintenance"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button v-if="!templateEditMode" type="primary" @click="handleCreateTemplate">创建模板</el-button>
          <el-button v-else type="primary" @click="handleUpdateTemplate">更新模板</el-button>
          <el-button v-if="templateEditMode" @click="cancelEditTemplate">取消编辑</el-button>
        </el-form-item>
      </el-form>

      <div class="template-list">
        <h4>已有模板</h4>
        <div v-if="templates.length === 0" class="empty-template">暂无模板</div>
        <div v-for="tpl in templates" :key="tpl.id" class="template-item">
          <div class="template-info">
            <strong>{{ tpl.name }}</strong>
            <el-tag size="mini" type="info">{{ getTypeText(tpl.type) }}</el-tag>
          </div>
          <p class="template-preview">{{ tpl.content?.substring(0, 50) }}...</p>
          <div class="template-actions">
            <el-button type="primary" size="mini" @click="handleUseTemplate(tpl)">使用模板</el-button>
            <el-button type="info" size="mini" @click="handleEditTemplate(tpl)">编辑</el-button>
            <el-button type="danger" size="mini" @click="handleDeleteTemplate(tpl)">删除</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { announcementApi } from '@/api/admin'

export default {
  name: 'AnnouncementManagement',

  data() {
    return {
      announcementList: [],
      selectedAnnouncements: [],
      loading: false,
      submitLoading: false,

      // 查询参数
      queryParams: {
        type: '',
        status: ''
      },

      // 分页
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },

      // 对话框
      dialogVisible: false,
      dialogTitle: '',
      isCreate: true,

      // 统计对话框
      statisticsDialogVisible: false,
      statisticsLoading: false,
      statisticsData: {},
      currentAnnouncementId: null,

      // 定时发布对话框
      scheduleDialogVisible: false,
      scheduleActiveTab: 'create',
      scheduleForm: {
        announcementId: '',
        scheduledAt: ''
      },
      scheduleList: [],
      scheduleListLoading: false,
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 8.64e7
        }
      },

      // 模板对话框
      templateDialogVisible: false,
      templates: [],
      newTemplate: { name: '', content: '', type: 'system' },
      templateEditMode: false,
      editingTemplateId: null,

      // 表单
      announcementForm: {
        id: '',
        title: '',
        content: '',
        effectiveAt: '',
        expireAt: '',
        type: 'system',
        priority: 'medium'
      },

      // 验证规则
      announcementRules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' },
          { min: 10, message: '公告内容至少10个字符', trigger: 'blur' }
        ],
        expireAt: [
          {
            validator: (rule, value, callback) => {
              if (value && this.announcementForm.effectiveAt && new Date(value) <= new Date(this.announcementForm.effectiveAt)) {
                callback(new Error('失效时间必须晚于生效时间'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
        ]
      }
    }
  },

  mounted() {
    this.loadAnnouncementList()
  },

  methods: {
    /**
     * 查看公告触达统计
     */
    async handleViewStatistics(row) {
      this.currentAnnouncementId = row.id
      this.statisticsDialogVisible = true
      this.statisticsLoading = true
      try {
        const response = await announcementApi.getAnnouncementStatistics(row.id)
        if (response.code === 200) {
          this.statisticsData = response.data || {}
        } else {
          this.$message.error(response.message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('获取统计数据错误:', error)
        this.$message.error('获取统计数据失败')
      } finally {
        this.statisticsLoading = false
      }
    },

    /**
     * 加载公告列表
     */
    async loadAnnouncementList() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        }
        if (this.queryParams.status !== '') {
          params.status = this.queryParams.status
        }
        if (this.queryParams.type !== '') {
          params.type = this.queryParams.type
        }

        const response = await announcementApi.getAnnouncementList(params)

        if (response.code === 200) {
          // MyBatis Plus Page 对象返回的字段是 records 而不是 list
          this.announcementList = response.data?.records || []
          this.pagination.total = response.data?.total || 0
        } else {
          this.$message.error(response.message || '获取公告列表失败')
        }
      } catch (error) {
        console.error('获取公告列表错误:', error)
        this.$message.error('获取公告列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 搜索公告
     */
    handleSearch() {
      this.pagination.currentPage = 1
      this.loadAnnouncementList()
    },

    /**
     * 重置搜索
     */
    handleReset() {
      this.queryParams.type = ''
      this.queryParams.status = ''
      this.pagination.currentPage = 1
      this.loadAnnouncementList()
    },

    /**
     * 选择变化
     */
    handleSelectionChange(selection) {
      this.selectedAnnouncements = selection
    },

    /**
     * 创建公告
     */
    handleCreateAnnouncement() {
      this.isCreate = true
      this.dialogTitle = '创建公告'
      this.announcementForm = {
        id: '',
        title: '',
        content: '',
        effectiveAt: '',
        expireAt: '',
        type: 'system',
        priority: 'medium'
      }
      this.dialogVisible = true
    },

    /**
     * 编辑公告
     */
    handleEditAnnouncement(row) {
      this.isCreate = false
      this.dialogTitle = '编辑公告'
      this.announcementForm = {
        id: row.id,
        title: row.title,
        content: row.content,
        effectiveAt: row.effectiveAt || '',
        expireAt: row.expireAt || '',
        type: row.type || 'system',
        priority: row.priority || 'medium'
      }
      this.dialogVisible = true
    },

    /**
     * 发布公告（草稿 -> 已发布）
     */
    handlePublishAnnouncement(row) {
      this.$confirm(`确定要发布公告 "${row.title}" 吗？发布后将推送给全平台用户。`, '发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.publishAnnouncement(row.id)
          if (response.code === 200) {
            this.$message.success('发布成功')
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '发布失败')
          }
        } catch (error) {
          console.error('发布公告错误:', error)
          this.$message.error('发布失败')
        }
      }).catch(() => {})
    },


    /**
     * 定时发布公告
     */
    handleScheduleAnnouncement() {
      if (this.announcementList.length === 0) {
        this.$message.warning('暂无公告可供定时发布')
        return
      }
      this.scheduleForm = { announcementId: '', scheduledAt: '' }
      this.scheduleActiveTab = 'create'
      this.scheduleDialogVisible = true
      this.loadScheduleList()
    },

    async loadScheduleList() {
      this.scheduleListLoading = true
      try {
        const response = await announcementApi.getScheduleList()
        if (response.code === 200) {
          this.scheduleList = response.data || []
        } else {
          this.$message.error(response.message || '获取定时任务列表失败')
        }
      } catch (error) {
        console.error('获取定时任务列表错误:', error)
      } finally {
        this.scheduleListLoading = false
      }
    },

    submitSchedule() {
      if (!this.scheduleForm.announcementId || !this.scheduleForm.scheduledAt) {
        this.$message.warning('请选择公告和发布时间')
        return
      }
      const scheduledTime = new Date(this.scheduleForm.scheduledAt).getTime()
      const now = Date.now()
      if (scheduledTime <= now) {
        this.$message.warning('发布时间必须晚于当前时间')
        return
      }
      this.$confirm(`确定要在 ${this.scheduleForm.scheduledAt} 发布该公告吗？`, '定时发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const response = await announcementApi.scheduleAnnouncement(this.scheduleForm.announcementId, {
            scheduledAt: this.scheduleForm.scheduledAt
          })
          if (response.code === 200) {
            this.$message.success('定时发布设置成功')
            this.scheduleActiveTab = 'list'
            this.loadScheduleList()
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '定时发布设置失败')
          }
        } catch (error) {
          console.error('定时发布错误:', error)
          this.$message.error('定时发布设置失败')
        }
      }).catch(() => {})
    },

    handleCancelSchedule(schedule) {
      this.$confirm(`确定要取消定时发布任务吗？`, '取消确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.cancelSchedule(schedule.id)
          if (response.code === 200) {
            this.$message.success('已取消定时发布')
            this.loadScheduleList()
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '取消失败')
          }
        } catch (error) {
          console.error('取消定时发布错误:', error)
          this.$message.error('取消失败')
        }
      }).catch(() => {})
    },

    /**
     * 模板管理
     */
    async showTemplateDialog() {
      this.templateDialogVisible = true
      await this.loadTemplates()
    },

    async loadTemplates() {
      try {
        const response = await announcementApi.getAnnouncementTemplates()
        if (response.code === 200) {
          this.templates = response.data || []
        }
      } catch (error) {
        console.error('加载模板错误:', error)
      }
    },

    handleCreateTemplate() {
      if (!this.newTemplate.name || !this.newTemplate.content) {
        this.$message.warning('请填写模板名称和内容')
        return
      }
      this.$confirm(`确定要创建模板 "${this.newTemplate.name}" 吗？`, '创建模板确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const response = await announcementApi.createAnnouncementTemplate(this.newTemplate)
          if (response.code === 200) {
            this.$message.success('模板创建成功')
            this.newTemplate = { name: '', content: '', type: 'system' }
            this.loadTemplates()
          } else {
            this.$message.error(response.message || '创建失败')
          }
        } catch (error) {
          console.error('创建模板错误:', error)
          this.$message.error('创建失败')
        }
      }).catch(() => {})
    },

    handleEditTemplate(template) {
      this.templateEditMode = true
      this.editingTemplateId = template.id
      this.newTemplate = {
        name: template.name,
        content: template.content,
        type: template.type
      }
    },

    handleUpdateTemplate() {
      if (!this.newTemplate.name || !this.newTemplate.content) {
        this.$message.warning('请填写模板名称和内容')
        return
      }
      this.$confirm(`确定要更新模板吗？`, '更新模板确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          const response = await announcementApi.updateAnnouncementTemplate(this.editingTemplateId, this.newTemplate)
          if (response.code === 200) {
            this.$message.success('模板更新成功')
            this.cancelEditTemplate()
            this.loadTemplates()
          } else {
            this.$message.error(response.message || '更新失败')
          }
        } catch (error) {
          console.error('更新模板错误:', error)
          this.$message.error('更新失败')
        }
      }).catch(() => {})
    },

    cancelEditTemplate() {
      this.templateEditMode = false
      this.editingTemplateId = null
      this.newTemplate = { name: '', content: '', type: 'system' }
    },

    handleDeleteTemplate(template) {
      this.$confirm(`确定要删除模板 "${template.name}" 吗？`, '删除模板确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.deleteAnnouncementTemplate(template.id)
          if (response.code === 200) {
            this.$message.success('模板删除成功')
            this.loadTemplates()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除模板错误:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    handleUseTemplate(template) {
      this.templateDialogVisible = false
      this.isCreate = true
      this.dialogTitle = '创建公告（使用模板）'
      this.announcementForm = {
        id: '',
        title: template.name || '',
        content: template.content || '',
        effectiveAt: '',
        expireAt: '',
        type: template.type || 'system',
        priority: 'medium'
      }
      this.dialogVisible = true
      this.$message.success('已应用模板内容')
    },

    /**
     * 下架公告
     */
    handleTakeDownAnnouncement(row) {
      this.$confirm(`确定要下架公告 "${row.title}" 吗？下架后可恢复。`, '下架确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.takeDownAnnouncement(row.id)
          if (response.code === 200) {
            this.$message.success('下架成功')
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '下架失败')
          }
        } catch (error) {
          console.error('下架公告错误:', error)
          this.$message.error('下架失败')
        }
      }).catch(() => {})
    },

    /**
     * 重新发布公告（已下架 -> 已发布）
     */
    handleRepublishAnnouncement(row) {
      this.$confirm(`确定要重新发布公告 "${row.title}" 吗？发布后将推送给全平台用户。`, '重新发布确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.republishAnnouncement(row.id)
          if (response.code === 200) {
            this.$message.success('重新发布成功')
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '重新发布失败')
          }
        } catch (error) {
          console.error('重新发布公告错误:', error)
          this.$message.error('重新发布失败')
        }
      }).catch(() => {})
    },

    /**
     * 删除公告
     */
    handleDeleteAnnouncement(row) {
      this.$confirm(`确定要删除公告 "${row.title}" 吗？此操作不可恢复！`, '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await announcementApi.deleteAnnouncement(row.id)
          if (response.code === 200) {
            this.$message.success('删除成功')

            const isLastItemOnPage = this.announcementList.length <= 1
            if (isLastItemOnPage && this.pagination.currentPage > 1) {
              this.pagination.currentPage -= 1
            }

            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除公告错误:', error)
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    /**
     * 提交表单
     */
    handleSubmit() {
      this.$refs.announcementFormRef.validate(async (valid) => {
        if (!valid) return

        this.submitLoading = true
        try {
          let response
          if (this.isCreate) {
            response = await announcementApi.createAnnouncement(this.announcementForm)
          } else {
            response = await announcementApi.updateAnnouncement(
              this.announcementForm.id,
              this.announcementForm
            )
          }

          if (response.code === 200) {
            this.$message.success(response.message || (this.isCreate ? '发布成功' : '更新成功'))
            this.dialogVisible = false
            this.loadAnnouncementList()
          } else {
            this.$message.error(response.message || (this.isCreate ? '发布失败' : '更新失败'))
          }
        } catch (error) {
          console.error('提交表单错误:', error)
          this.$message.error(this.isCreate ? '发布失败' : '更新失败')
        } finally {
          this.submitLoading = false
        }
      })
    },

    /**
     * 对话框关闭回调
     */
    handleDialogClose() {
      this.$nextTick(() => {
        this.$refs.announcementFormRef && this.$refs.announcementFormRef.resetFields()
      })
    },

    /**
     * 分页大小变化
     */
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.loadAnnouncementList()
    },

    /**
     * 当前页变化
     */
    handleCurrentChange(val) {
      this.pagination.currentPage = val
      this.loadAnnouncementList()
    },

    /**
     * 格式化日期
     */
    indexMethod(index) {
      return (this.pagination.currentPage - 1) * this.pagination.pageSize + index + 1
    },
    formatDate(date) {
      if (!date) return '-'
      return new Date(date).toLocaleString('zh-CN')
    },

    /**
     * 获取类型颜色
     */
    getTypeColor(type) {
      const colorMap = {
        system: '',
        activity: 'success',
        maintenance: 'warning'
      }
      return colorMap[type] || 'info'
    },

    /**
     * 获取类型文本
     */
    getTypeText(type) {
      const textMap = {
        system: '系统公告',
        activity: '活动通知',
        maintenance: '维护通知'
      }
      return textMap[type] || '未知'
    },

    /**
     * 获取优先级颜色
     */
    getPriorityColor(priority) {
      const colorMap = {
        low: 'info',
        medium: '',
        high: 'warning',
        urgent: 'danger'
      }
      return colorMap[priority] || 'info'
    },

    /**
     * 获取优先级文本
     */
    getPriorityText(priority) {
      const textMap = {
        low: '低',
        medium: '中',
        high: '高',
        urgent: '紧急'
      }
      return textMap[priority] || '未知'
    },

    /**
     * 获取状态标签文本
     * 后端状态: 0=草稿, 1=已发布, 2=已下架, 3=已过期, -1=已删除
     */
    getStatusText(status) {
      const textMap = {
        0: '草稿',
        1: '已发布',
        2: '已下架',
        3: '已过期',
        '-1': '已删除'
      }
      return textMap[status] || '未知'
    },

    /**
     * 获取状态标签颜色类型
     */
    getStatusTagType(status) {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning',
        3: 'danger',
        '-1': 'danger'
      }
      return typeMap[status] || 'info'
    }
  }
}
</script>

<style scoped>
.announcement-management {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ========== 页面头部 ========== */
.page-header {
  padding: var(--space-4) var(--space-5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.page-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: var(--space-1) 0 0;
}

/* ========== 筛选卡片 ========== */
.filter-card {
  margin-bottom: var(--space-4);
}

.filter-card :deep(.el-card__body) {
  padding: var(--space-4) var(--space-5);
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form :deep(.el-input__inner) {
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

/* ========== 表格卡片 ========== */
.table-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.table-card :deep(.el-table) {
  font-size: var(--font-size-sm);
}

.table-card :deep(.el-table th) {
  background-color: var(--bg-canvas);
  color: var(--text-primary);
  font-weight: 600;
}

.table-card :deep(.el-table tbody tr) {
  transition: background-color var(--transition-fast);
}

.table-card :deep(.el-button) {
  transition: all var(--transition-fast);
}

.table-card :deep(.el-button:hover) {
  transform: translateY(-1px);
}

/* ========== 操作按钮容器 ========== */
.action-buttons {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 4px;
  justify-content: flex-start;
}

.action-buttons .el-button {
  margin: 0 !important;
}

.title-text {
  display: inline-block;
  max-width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 分页 ========== */
.pagination-wrapper {
  margin-top: var(--space-5);
  display: flex;
  justify-content: flex-end;
}

/* ========== 对话框 ========== */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .filter-form :deep(.el-form-item) {
    margin-bottom: var(--space-3);
  }

  .pagination-wrapper {
    justify-content: center;
  }

  .table-card :deep(.el-table) {
    font-size: var(--font-size-xs);
  }
}

/* ========== 过渡增强 ========== */
.filter-card {
  transition: box-shadow var(--transition-normal);
}

.filter-card:hover {
  box-shadow: var(--shadow-sm);
}

.table-card {
  transition: box-shadow var(--transition-normal);
}

/* ========== 统计对话框 ========== */
.statistics-content {
  padding: var(--space-4) 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-3);
  background: var(--bg-canvas);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-item.highlight {
  background: var(--primary-bg);
  border-color: var(--primary-color);
}

.stat-label {
  font-size: var(--font-size-base);
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-value {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--text-primary);
}

.stat-value.success {
  color: var(--success-color);
}

.stat-value.warning {
  color: var(--warning-color);
}

.stat-value.primary {
  color: var(--primary-color);
}

/* ========== 模板管理样式 ========== */
.template-form {
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.template-list {
  max-height: 300px;
  overflow-y: auto;
}

.template-list h4 {
  margin: 0 0 var(--space-3);
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

.empty-template {
  text-align: center;
  padding: var(--space-6);
  color: var(--text-secondary);
}

.template-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  margin-bottom: var(--space-2);
  background: var(--bg-canvas);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.template-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.template-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.template-preview {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.template-actions {
  display: flex;
  gap: var(--space-2);
  justify-content: flex-end;
}

/* ========== 定时任务列表样式 ========== */
.schedule-list {
  max-height: 400px;
  overflow-y: auto;
  padding: var(--space-2);
}

.empty-schedule {
  text-align: center;
  padding: var(--space-6);
  color: var(--text-secondary);
}

.schedule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  margin-bottom: var(--space-2);
  background: var(--bg-canvas);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.schedule-item:hover {
  border-color: var(--warning-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.schedule-info {
  flex: 1;
  min-width: 0;
}

.schedule-title {
  font-weight: 600;
  font-size: var(--font-size-base);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-time {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.schedule-actions {
  flex-shrink: 0;
  margin-left: var(--space-3);
}
</style>
