<template>
  <div class="note-detail-page">
    <div v-if="loading" class="loading-container card-panel">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error-container card-panel">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadPageData">重新加载</el-button>
          <el-button @click="goBack">返回我的笔记</el-button>
        </template>
      </el-result>
    </div>

    <div v-else class="page-content">
      <div class="top-operation-bar card-panel">
        <div class="left-section">
          <el-button type="text" icon="el-icon-arrow-left" @click="goBack">
            返回
          </el-button>
          <div class="title-group">
            <h2 class="page-title">{{ noteForm.title || pageTitle }}</h2>
            <p class="page-desc">{{ pageDescription }}</p>
          </div>
          <el-tag size="small" :type="statusTag.type">{{ statusTag.text }}</el-tag>
          <el-tag size="small" :type="noteForm.isPublic ? 'success' : 'info'">{{ noteForm.isPublic ? '公开' : '私有' }}</el-tag>
        </div>
        <div class="right-section">
          <template v-if="isEditMode">
            <el-button v-if="!isCreateMode" @click="goBackToReading">返回阅读</el-button>
            <el-button @click="cancelEdit">取消编辑</el-button>
            <el-button type="primary" @click="saveDraft" :loading="savingDraft">{{ isCreateMode ? '创建笔记' : '保存' }}</el-button>
            <el-button @click="previewNote">预览笔记</el-button>
            <el-button v-if="!isCreateMode" type="success" @click="publishCurrentNote" :disabled="!canPublish">完成沉淀</el-button>
          </template>
          <template v-else-if="!isCreateMode">
            <el-button @click="enterEditMode">进入编辑</el-button>
          </template>
        </div>
      </div>

      <div class="context-banner card-panel">
        <div class="context-item">
          <span class="context-label">笔记类型</span>
          <span class="context-value">{{ getTypeLabel(noteForm.noteType) }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">最后保存</span>
          <span class="context-value">{{ lastSaveStatus }}</span>
        </div>
        <div class="context-item">
          <span class="context-label">更新时间</span>
          <span class="context-value">{{ formatDate(note.updateTime || note.updatedAt || note.createTime) }}</span>
        </div>
        <div class="context-item" v-if="sourceItem.id">
          <span class="context-label">来源收藏项</span>
          <span class="context-value link" @click="goToSourceItem">{{ sourceItem.title || `收藏项 #${sourceItem.id}` }}</span>
        </div>
      </div>

      <div class="main-content-area">
        <div class="reference-panel card-panel">
          <div class="panel-header">
            <h3>来源参照</h3>
            <div class="panel-controls">
              <el-input
                v-if="sourceItem.id"
                v-model="sourceKeyword"
                placeholder="筛选来源片段..."
                prefix-icon="el-icon-search"
                size="small"
                clearable
              />
              <div v-else class="empty-source-tip">当前笔记暂无关联来源</div>
            </div>
          </div>
          <div class="reference-content note-reference-content">
            <div class="reading-summary-block">
              <div class="summary-header">
                <span class="summary-kicker">阅读摘要</span>
                <h4>{{ noteForm.title || pageTitle }}</h4>
                <p>{{ noteForm.description || defaultDescription }}</p>
              </div>
              <div class="summary-metrics">
                <div class="summary-metric">
                  <span class="metric-label">字数规模</span>
                  <strong>{{ wordCountStats.totalCharacters }}</strong>
                </div>
                <div class="summary-metric">
                  <span class="metric-label">单词数</span>
                  <strong>{{ wordCountStats.wordCount }}</strong>
                </div>
                <div class="summary-metric">
                  <span class="metric-label">互动热度</span>
                  <strong>{{ interactionStats.likes + interactionStats.comments }}</strong>
                </div>
              </div>
            </div>

            <div class="basic-info note-basic-info">
              <div class="info-group">
                <h4>基础属性</h4>
                <div class="info-item">
                  <label>状态</label>
                  <span>{{ statusTag.text }}</span>
                </div>
                <div class="info-item">
                  <label>公开设置</label>
                  <span>{{ noteForm.isPublic ? '公开' : '私有' }}</span>
                </div>
                <div class="info-item">
                  <label>笔记类型</label>
                  <span>{{ getTypeLabel(noteForm.noteType) }}</span>
                </div>
              </div>

              <div class="info-group source-context-group" v-if="sourceItem.id">
                <div class="group-heading-row">
                  <h4>来源上下文</h4>
                  <el-button type="text" size="mini" @click="goToSourceItem">查看来源</el-button>
                </div>
                <div class="info-item">
                  <label>来源标题</label>
                  <span>{{ sourceItem.title || `收藏项 #${sourceItem.id}` }}</span>
                </div>
                <div class="info-item">
                  <label>来源类型</label>
                  <span>{{ sourceTypeLabel }}</span>
                </div>
                <div class="info-item" v-if="sourceItem.source">
                  <label>来源渠道</label>
                  <span>{{ sourceItem.source }}</span>
                </div>
                <div class="info-item">
                  <label>学习目标</label>
                  <span>{{ sourceItem.studyGoal || '尚未设置学习目标' }}</span>
                </div>
                <div class="info-item">
                  <label>核心摘要</label>
                  <span>{{ sourceItem.coreAbstract || '暂无核心摘要' }}</span>
                </div>
                <div class="keyword-block" v-if="sourceKeywords.length > 0">
                  <span class="keyword-label">关键词</span>
                  <div class="keyword-list">
                    <el-tag v-for="keyword in sourceKeywords" :key="keyword" size="small" effect="plain" class="keyword-tag">
                      {{ keyword }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="info-group source-content-group" v-if="sourceItem.id && filteredSourceSnippets.length > 0">
              <div class="group-heading-row">
                <h4>来源素材片段</h4>
                <el-button size="mini" @click="copyPrimarySourceSnippet">插入主片段</el-button>
              </div>
              <div class="source-snippet-list">
                <div v-for="(snippet, index) in filteredSourceSnippets" :key="`${index}-${snippet.title}`" class="source-snippet-card">
                  <div class="snippet-header">
                    <div>
                      <strong>{{ snippet.title }}</strong>
                      <p>{{ snippet.description }}</p>
                    </div>
                    <el-button size="mini" type="text" @click="insertSnippetToNote(snippet)">引用到笔记</el-button>
                  </div>
                  <div class="snippet-body">{{ snippet.content }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="divider">
          <div class="divider-handle"></div>
        </div>

        <div class="draft-panel card-panel">
          <div class="panel-header">
            <h3>{{ isEditMode ? '笔记编辑器' : '阅读视图' }}</h3>
            <div class="draft-status-group">
              <el-tag size="small" type="warning" v-if="modified">内容已修改</el-tag>
              <el-tag size="small" type="warning" v-else-if="!isCreateMode && noteForm.id">草稿已保存</el-tag>
              <el-tag size="small" type="info" v-else-if="isCreateMode">新建沉淀</el-tag>
              <el-tag size="small" type="success" v-else>内容已同步</el-tag>
            </div>
          </div>
          <div class="draft-content">
            <template v-if="isEditMode">
              <div class="editor-workbench-banner">
                <div class="workbench-main">
                  <span class="workbench-kicker">编辑工作台</span>
                  <h4>{{ isCreateMode ? '开始沉淀新的知识笔记' : '继续完善当前知识笔记' }}</h4>
                  <p>{{ sourceItem.id ? `当前关联来源：${sourceItem.title || `收藏项 #${sourceItem.id}`}` : '当前为独立笔记，可直接沉淀核心内容与个人总结。' }}</p>
                </div>
                <div class="workbench-status">
                  <div class="status-card">
                    <span class="status-label">保存状态</span>
                    <strong>{{ modified ? '待保存' : '已同步' }}</strong>
                  </div>
                  <div class="status-card">
                    <span class="status-label">最近动作</span>
                    <strong>{{ lastSavedTime ? formatTimeOnly(lastSavedTime) : '尚未保存' }}</strong>
                  </div>
                </div>
              </div>

              <div class="note-config">
                <el-form :model="noteForm" :rules="noteRules" ref="noteForm" label-width="80px">
                  <el-form-item label="笔记标题" prop="title">
                    <el-input v-model="noteForm.title" placeholder="请输入笔记标题" @input="markAsModified" />
                  </el-form-item>
                  <el-form-item label="笔记类型" prop="noteType">
                    <el-radio-group v-model="noteForm.noteType" @change="markAsModified">
                      <el-radio label="conceptual">概念型</el-radio>
                      <el-radio label="procedural">程序型</el-radio>
                      <el-radio label="factual">事实型</el-radio>
                      <el-radio label="metacognitive">元认知型</el-radio>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item label="描述">
                    <el-input v-model="noteForm.description" type="textarea" :rows="2" placeholder="简要描述笔记内容..." @input="markAsModified" />
                  </el-form-item>
                  <el-form-item label="公开设置">
                    <el-switch v-model="noteForm.isPublic" active-text="公开" inactive-text="私有" @change="markAsModified" />
                  </el-form-item>
                </el-form>
              </div>

              <div class="editor-section">
                <div class="editor-header">
                  <h4>笔记内容</h4>
                  <div class="editor-actions">
                    <el-button size="small" @click="insertTemplate">插入模板</el-button>
                    <el-button size="small" @click="copySourceAbstract" :disabled="!sourceSnippet">引用来源摘要</el-button>
                    <el-button size="small" @click="copyPrimarySourceSnippet" :disabled="filteredSourceSnippets.length === 0">引用来源片段</el-button>
                    <el-button size="small" v-if="!isCreateMode" @click="goBackToReading">查看阅读态</el-button>
                  </div>
                </div>
                <div class="editor-toolbar enhanced-editor-toolbar">
                  <div class="word-count-display">
                    <span class="word-count-item">
                      <i class="el-icon-document"></i>
                      字符数: {{ wordCountStats.totalCharacters }}
                    </span>
                    <span class="word-count-item">
                      <i class="el-icon-edit"></i>
                      单词数: {{ wordCountStats.wordCount }}
                    </span>
                  </div>
                  <div class="toolbar-hints">
                    <span class="hint-item">建议：先补标题，再整理摘要，最后完善正文结构</span>
                  </div>
                </div>
                <div class="rich-editor">
                  <el-input
                    v-model="noteForm.content"
                    type="textarea"
                    :rows="15"
                    placeholder="开始创作您的笔记..."
                    @input="markAsModified"
                  />
                </div>
              </div>
            </template>

            <template v-else>
              <div class="reading-panel enhanced-reading-panel">
                <div class="reading-meta reading-meta-grid">
                  <div class="reading-item featured">
                    <span class="reading-label">阅读字数</span>
                    <span class="reading-value">{{ wordCountStats.totalCharacters }}</span>
                  </div>
                  <div class="reading-item">
                    <span class="reading-label">单词数</span>
                    <span class="reading-value">{{ wordCountStats.wordCount }}</span>
                  </div>
                  <div class="reading-item">
                    <span class="reading-label">更新时间</span>
                    <span class="reading-value">{{ formatDate(note.updateTime || note.updatedAt || note.createTime) }}</span>
                  </div>
                </div>

                <div class="reading-article-shell">
                  <div class="article-cover">
                    <div class="article-cover-content">
                      <span class="article-cover-kicker">知识沉淀</span>
                      <h1>{{ noteForm.title || pageTitle }}</h1>
                      <p>{{ noteForm.description || defaultDescription }}</p>
                    </div>
                  </div>

                  <div class="reading-outline" v-if="contentSections.length > 0">
                    <span class="outline-label">内容结构</span>
                    <div class="outline-tags">
                      <el-tag v-for="(section, index) in contentSections" :key="`${section}-${index}`" size="small" effect="plain">
                        {{ section }}
                      </el-tag>
                    </div>
                  </div>

                  <div class="reading-content prose-content" v-html="formattedNoteContent"></div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div v-if="isEditMode" class="bottom-operation-bar card-panel">
        <div class="bottom-left-section">
          <span class="auto-save-status">
            <i :class="modified ? 'el-icon-time' : 'el-icon-circle-check'"></i>
            {{ modified ? '当前内容已修改，建议及时保存' : lastSaveStatus }}
          </span>
        </div>
        <div class="bottom-right-section">
          <el-button size="small" @click="previewNote">预览笔记</el-button>
          <el-button size="small" type="primary" @click="saveDraft" :loading="savingDraft">{{ isCreateMode ? '创建笔记' : '保存草稿' }}</el-button>
        </div>
      </div>

      <div ref="commentsSection" class="bottom-interaction-panel card-panel">
        <div class="panel-header compact">
          <div>
            <h3>互动场地</h3>
            <p>阅读完成后，可在下方完成点赞、收藏和评论互动。</p>
          </div>
        </div>
        <div class="interaction-stats">
          <div class="interaction-stat">
            <strong>{{ interactionStats.likes }}</strong>
            <span>点赞</span>
          </div>
          <div class="interaction-stat">
            <strong>{{ interactionStats.collects }}</strong>
            <span>收藏</span>
          </div>
          <div class="interaction-stat">
            <strong>{{ interactionStats.comments }}</strong>
            <span>评论</span>
          </div>
        </div>
        <div class="interaction-actions">
          <el-button size="small" :type="interactionState.isLiked ? 'danger' : 'default'" @click="toggleLike" :disabled="isCreateMode">
            {{ interactionState.isLiked ? '取消点赞' : '点赞' }}
          </el-button>
          <el-button size="small" :type="interactionState.isCollected ? 'warning' : 'default'" @click="toggleCollect" :disabled="isCreateMode">
            {{ interactionState.isCollected ? '取消收藏' : '收藏' }}
          </el-button>
        </div>
        <div class="comment-editor">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            :disabled="isCreateMode"
            placeholder="留下你的互动评论..."
          />
          <div class="comment-submit">
            <el-button size="small" type="primary" @click="submitComment" :disabled="isCreateMode">发表评论</el-button>
          </div>
        </div>
        <div class="comment-list">
          <div v-if="isCreateMode" class="empty-panel">创建后可在这里承接点赞、收藏和评论互动。</div>
          <div v-else-if="comments.length === 0" class="empty-panel">暂无评论，后续可在这里承接讨论。</div>
          <div v-else v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-meta">{{ comment.userName || `用户${comment.userId || ''}` }} · {{ formatDate(comment.createdAt || comment.createTime) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { noteApi } from '@/api/note'
import { collectApi } from '@/api/collect'
import {
  likeContent,
  unlikeContent,
  collectContent,
  uncollectContent,
  commentContent,
  getLikeCount,
  getCollectCount,
  getComments,
  checkLikeStatus,
  checkCollectStatus
} from '@/api/interaction'

const escapeHtml = (text) => String(text || '')
  .replace(/&/g, '&amp;')
  .replace(/</g, '&lt;')
  .replace(/>/g, '&gt;')
  .replace(/"/g, '&quot;')
  .replace(/'/g, '&#39;')

const createEmptyNoteForm = () => ({
  id: null,
  title: '',
  noteType: 'conceptual',
  description: '',
  content: '',
  isPublic: false
})

const formatNoteContentToHtml = (content, fallback) => {
  const safeText = escapeHtml(content || fallback)
  return safeText
    .replace(/^(### )(.+)$/gm, '<h3>$2</h3>')
    .replace(/^(## )(.+)$/gm, '<h2>$2</h2>')
    .replace(/^(# )(.+)$/gm, '<h1>$2</h1>')
    .replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
    .replace(/\n\n+/g, '</p><p>')
    .replace(/\n/g, '<br>')
    .replace(/^/, '<p>')
    .replace(/$/, '</p>')
    .replace(/<p><\/p>/g, '')
    .replace(/<p>(<h[1-3]>)/g, '$1')
    .replace(/(<\/h[1-3]>)<\/p>/g, '$1')
    .replace(/<p>(<blockquote>)/g, '$1')
    .replace(/(<\/blockquote>)<\/p>/g, '$1')
    .replace(/<p><ul>/g, '<ul>')
    .replace(/<\/ul><\/p>/g, '</ul>')
}

export default {
  name: 'NoteDetailPage',
  data() {
    return {
      loading: true,
      error: '',
      savingDraft: false,
      isEditMode: false,
      note: {},
      sourceItem: {},
      comments: [],
      sourceKeyword: '',
      interactionStats: {
        likes: 0,
        collects: 0,
        comments: 0
      },
      interactionState: {
        isLiked: false,
        isCollected: false
      },
      commentForm: {
        content: ''
      },
      noteForm: createEmptyNoteForm(),
      originalNoteForm: null,
      lastSavedTime: null,
      lastSaveStatus: '尚未保存',
      modified: false,
      noteRules: {
        title: [
          { required: true, message: '请输入笔记标题', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入笔记内容', trigger: 'blur' },
          { min: 10, message: '内容至少10个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    noteId() {
      return this.$route.params.id
    },
    routeMode() {
      return this.$route.query.mode
    },
    isCreateMode() {
      return this.$route.name === 'CreationNoteCreate' || this.routeMode === 'create'
    },
    canPublish() {
      return !!(this.noteForm.title && this.noteForm.noteType && this.noteForm.content)
    },
    statusTag() {
      if (this.isCreateMode) {
        return { text: '新建中', type: 'info' }
      }
      const status = this.note.status || this.note.stage
      if (status === '完成' || status === 'published') {
        return { text: '已完成', type: 'success' }
      }
      return { text: '草稿', type: 'warning' }
    },
    wordCountStats() {
      const content = this.noteForm.content || ''
      const totalCharacters = content.length
      const wordCount = content.trim() ? content.trim().split(/\s+/).length : 0
      return {
        totalCharacters,
        wordCount
      }
    },
    pageTitle() {
      if (this.isCreateMode) {
        return '新建知识笔记'
      }
      return this.note.title || '知识笔记详情'
    },
    pageDescription() {
      if (this.isCreateMode) {
        return '从来源内容中沉淀结构化知识，形成可复用的个人笔记。'
      }
      return this.note.description || '查看、完善并管理当前知识笔记。'
    },
    defaultDescription() {
      return '围绕当前主题进行知识沉淀，保留关键信息、观点与行动建议。'
    },
    formattedNoteContent() {
      return formatNoteContentToHtml(this.noteForm.content, '当前暂无正文内容，可切换到编辑模式继续完善。')
    },
    sourceKeywords() {
      const raw = this.sourceItem.keywords || this.sourceItem.tags || []
      if (Array.isArray(raw)) {
        return raw.filter(Boolean)
      }
      return String(raw || '')
        .split(/[，,、\s]+/)
        .map(item => item.trim())
        .filter(Boolean)
    },
    sourceTypeLabel() {
      const type = this.sourceItem.type || this.sourceItem.sourceType
      if (!type) {
        return '未知类型'
      }
      const map = {
        article: '文章',
        video: '视频',
        book: '书籍',
        course: '课程',
        web: '网页',
        note: '笔记'
      }
      return map[type] || type
    },
    sourceSnippet() {
      return this.filteredSourceSnippets[0] || null
    },
    sourceSnippets() {
      const snippets = []
      const pushSnippet = (title, description, content) => {
        if (!content) {
          return
        }
        snippets.push({ title, description, content })
      }
      pushSnippet('核心摘要', '适合作为笔记概览使用', this.sourceItem.coreAbstract)
      pushSnippet('学习目标', '用于明确本次沉淀的目标', this.sourceItem.studyGoal)
      pushSnippet('关键内容', '来源正文中的主要信息', this.sourceItem.content || this.sourceItem.description)
      return snippets
    },
    contentSections() {
      return (this.noteForm.content || '')
        .split('\n')
        .map(line => line.trim())
        .filter(line => /^#{1,3}\s+/.test(line))
        .map(line => line.replace(/^#{1,3}\s+/, ''))
    },
    filteredSourceSnippets() {
      const keyword = this.sourceKeyword.trim().toLowerCase()
      if (!keyword) {
        return this.sourceSnippets
      }
      return this.sourceSnippets.filter(snippet =>
        [snippet.title, snippet.description, snippet.content].some(field => String(field).toLowerCase().includes(keyword))
      )
    }
  },
  async created() {
    await this.loadPageData()
    this.syncModeFromRoute()
  },
  watch: {
    '$route.params.id': {
      async handler() {
        this.isEditMode = false
        await this.loadPageData()
        this.syncModeFromRoute()
        this.scrollToCommentsIfNeeded()
      }
    },
    '$route.query.mode'() {
      this.syncModeFromRoute()
    },
    '$route.name': {
      async handler() {
        this.isEditMode = false
        await this.loadPageData()
        this.syncModeFromRoute()
        this.scrollToCommentsIfNeeded()
      }
    },
    '$route.hash'() {
      this.scrollToCommentsIfNeeded()
    }
  },
  methods: {
    syncModeFromRoute() {
      if (this.isCreateMode) {
        this.isEditMode = true
        return
      }
      this.isEditMode = this.routeMode === 'edit'
    },
    async loadPageData() {
      this.loading = true
      this.error = ''
      try {
        if (this.isCreateMode) {
          this.note = {}
          this.sourceItem = {}
          this.comments = []
          this.sourceKeyword = ''
          this.interactionStats = { likes: 0, collects: 0, comments: 0 }
          this.interactionState = { isLiked: false, isCollected: false }
          this.noteForm = createEmptyNoteForm()
          this.originalNoteForm = createEmptyNoteForm()
          this.lastSavedTime = null
          this.lastSaveStatus = '尚未保存'
          this.modified = false
          return
        }

        const response = await noteApi.getNoteDetail(this.noteId)
        if (response?.code !== 200 || !response.data) {
          throw new Error('笔记详情不存在')
        }
        const detail = response.data
        this.note = detail
        const normalizedForm = {
          id: detail.id,
          title: detail.title || '',
          noteType: detail.noteType || detail.type || 'conceptual',
          description: detail.description || '',
          content: detail.content || '',
          isPublic: detail.isPublic || false
        }
        this.noteForm = { ...normalizedForm }
        this.originalNoteForm = { ...normalizedForm }
        this.lastSavedTime = detail.updateTime ? new Date(detail.updateTime) : null
        this.lastSaveStatus = this.lastSavedTime ? `最近更新 ${this.lastSavedTime.toLocaleString()}` : '尚未保存'
        this.modified = false
        this.sourceKeyword = ''

        await Promise.all([
          this.loadSourceItem(detail.collectionItemId),
          this.loadInteractionData()
        ])
      } catch (error) {
        console.error('加载笔记详情失败:', error)
        this.error = error?.message || '笔记详情加载失败'
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToCommentsIfNeeded()
        })
      }
    },
    async loadSourceItem(collectionItemId) {
      if (!collectionItemId) {
        this.sourceItem = {}
        return
      }
      try {
        const response = await collectApi.getCollectById(collectionItemId)
        this.sourceItem = response?.data?.data || response?.data || {}
      } catch (error) {
        console.error('加载来源收藏项失败:', error)
        this.sourceItem = {}
      }
    },
    async loadInteractionData() {
      if (this.isCreateMode) {
        this.comments = []
        this.interactionStats = { likes: 0, collects: 0, comments: 0 }
        this.interactionState = { isLiked: false, isCollected: false }
        return
      }
      const targetType = 'note'
      try {
        const [likeRes, collectRes, commentRes, likeStatusRes, collectStatusRes] = await Promise.all([
          getLikeCount(this.noteId, targetType),
          getCollectCount(this.noteId, targetType),
          getComments(this.noteId, targetType, 1, 10),
          checkLikeStatus(this.noteId, targetType),
          checkCollectStatus(this.noteId, targetType)
        ])

        this.interactionStats.likes = likeRes?.data?.count || likeRes?.data || this.note.likes || 0
        this.interactionStats.collects = collectRes?.data?.count || collectRes?.data || 0
        const commentData = commentRes?.data?.records || commentRes?.data?.list || commentRes?.data || []
        this.comments = Array.isArray(commentData) ? commentData : []
        this.interactionStats.comments = this.comments.length
        this.interactionState.isLiked = !!(likeStatusRes?.data?.liked ?? likeStatusRes?.data)
        this.interactionState.isCollected = !!(collectStatusRes?.data?.collected ?? collectStatusRes?.data)
      } catch (error) {
        console.error('加载互动数据失败:', error)
        this.comments = []
        this.interactionStats.likes = this.note.likes || 0
        this.interactionStats.collects = 0
        this.interactionStats.comments = 0
      }
    },
    scrollToCommentsIfNeeded() {
      if (this.$route.hash !== '#comments') {
        return
      }
      const target = this.$refs.commentsSection
      if (target && typeof target.scrollIntoView === 'function') {
        target.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    },
    enterEditMode() {
      this.isEditMode = true
      this.$router.replace({ query: { ...this.$route.query, mode: 'edit' } })
    },
    goBackToReading() {
      const query = { ...this.$route.query }
      delete query.mode
      this.isEditMode = false
      this.$router.replace({ query })
    },
    cancelEdit() {
      if (this.originalNoteForm) {
        this.noteForm = { ...this.originalNoteForm }
      }
      this.modified = false
      this.goBackToReading()
    },
    markAsModified() {
      this.modified = true
    },
    insertTemplate() {
      const template = ['# 核心观点', '', '## 关键知识点', '', '## 我的理解', '', '## 行动计划'].join('\n')
      this.noteForm.content = this.noteForm.content ? `${this.noteForm.content}\n\n${template}` : template
      this.markAsModified()
    },
    copySourceAbstract() {
      if (!this.sourceSnippet) {
        return
      }
      this.insertSnippetToNote(this.sourceSnippet)
    },
    copyPrimarySourceSnippet() {
      if (!this.filteredSourceSnippets.length) {
        return
      }
      this.insertSnippetToNote(this.filteredSourceSnippets[0])
    },
    insertSnippetToNote(snippet) {
      const text = snippet?.content || ''
      if (!text) {
        return
      }
      this.noteForm.content = this.noteForm.content ? `${this.noteForm.content}\n\n${text}` : text
      this.markAsModified()
    },
    async saveDraft() {
      const formRef = this.$refs.noteForm
      if (formRef?.validate) {
        const valid = await new Promise(resolve => formRef.validate(resolve))
        if (!valid) {
          return
        }
      }
      this.savingDraft = true
      try {
        const payload = {
          ...this.noteForm,
          collectionItemId: this.sourceItem.id || this.note.collectionItemId || null
        }
        let response
        if (this.isCreateMode) {
          response = await noteApi.createNote(payload)
        } else {
          response = await noteApi.updateNote(this.noteId, payload)
        }
        if (response?.code !== 200) {
          throw new Error(response?.message || '保存失败')
        }
        this.$message.success(this.isCreateMode ? '创建成功' : '保存成功')
        this.modified = false
        if (this.isCreateMode && response?.data?.id) {
          await this.$router.replace({ name: 'CreationNoteDetail', params: { id: response.data.id } })
          return
        }
        await this.loadPageData()
      } catch (error) {
        console.error('保存笔记失败:', error)
        this.$message.error(error?.message || '保存失败')
      } finally {
        this.savingDraft = false
      }
    },
    previewNote() {
      this.goBackToReading()
    },
    async publishCurrentNote() {
      this.$message.success('当前版本已完成沉淀')
    },
    async toggleLike() {
      if (this.isCreateMode) {
        return
      }
      try {
        if (this.interactionState.isLiked) {
          await unlikeContent(this.noteId, 'note')
          this.$message.success('已取消点赞')
        } else {
          await likeContent(this.noteId, 'note')
          this.$message.success('点赞成功')
        }
        await this.loadInteractionData()
      } catch (error) {
        console.error('点赞操作失败:', error)
        this.$message.error(error?.message || '操作失败')
      }
    },
    async toggleCollect() {
      if (this.isCreateMode) {
        return
      }
      try {
        if (this.interactionState.isCollected) {
          await uncollectContent(this.noteId, 'note')
          this.$message.success('已取消收藏')
        } else {
          await collectContent(this.noteId, 'note')
          this.$message.success('收藏成功')
        }
        await this.loadInteractionData()
      } catch (error) {
        console.error('收藏操作失败:', error)
        this.$message.error(error?.message || '操作失败')
      }
    },
    async submitComment() {
      if (this.isCreateMode) {
        return
      }
      const content = this.commentForm.content.trim()
      if (!content) {
        this.$message.warning('请输入评论内容')
        return
      }
      try {
        await commentContent(this.noteId, 'note', content)
        this.$message.success('评论成功')
        this.commentForm.content = ''
        await this.loadInteractionData()
        this.$nextTick(() => {
          this.scrollToCommentsIfNeeded()
        })
      } catch (error) {
        console.error('发表评论失败:', error)
        this.$message.error(error?.message || '评论失败')
      }
    },
    goBack() {
      this.$router.push('/creation/notes')
    },
    goToSourceItem() {
      if (!this.sourceItem.id) {
        return
      }
      this.$router.push(`/collections/${this.sourceItem.id}/workspace`)
    },
    getTypeLabel(type) {
      const map = {
        conceptual: '概念型',
        procedural: '程序型',
        factual: '事实型',
        metacognitive: '元认知型'
      }
      return map[type] || type || '未设置'
    },
    formatDate(value) {
      if (!value) {
        return '--'
      }
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) {
        return value
      }
      const yyyy = date.getFullYear()
      const mm = String(date.getMonth() + 1).padStart(2, '0')
      const dd = String(date.getDate()).padStart(2, '0')
      const hh = String(date.getHours()).padStart(2, '0')
      const mi = String(date.getMinutes()).padStart(2, '0')
      return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
    },
    formatTimeOnly(value) {
      if (!value) {
        return '--'
      }
      const date = value instanceof Date ? value : new Date(value)
      if (Number.isNaN(date.getTime())) {
        return '--'
      }
      const hh = String(date.getHours()).padStart(2, '0')
      const mi = String(date.getMinutes()).padStart(2, '0')
      return `${hh}:${mi}`
    }
  }
}
</script>

<style scoped>
.note-detail-page {
  padding: 24px;
}

.page-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-panel {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}

.top-operation-bar,
.context-banner,
.bottom-operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.left-section,
.right-section,
.bottom-left-section,
.bottom-right-section {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-group h2,
.panel-header h3,
.page-title {
  margin: 0;
}

.page-desc,
.empty-source-tip,
.panel-header p {
  margin: 6px 0 0;
  color: #909399;
}

.context-banner {
  flex-wrap: wrap;
}

.context-item {
  min-width: 180px;
}

.context-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.context-value {
  color: #303133;
  font-weight: 500;
}

.context-value.link {
  color: #409eff;
  cursor: pointer;
}

.main-content-area {
  display: grid;
  grid-template-columns: minmax(320px, 420px) 16px minmax(0, 1fr);
  gap: 20px;
}

.reference-panel,
.draft-panel,
.bottom-interaction-panel {
  min-width: 0;
}

.panel-header,
.group-heading-row,
.editor-header,
.summary-header,
.snippet-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-header.compact {
  align-items: center;
}

.panel-controls {
  min-width: 240px;
}

.reading-summary-block,
.basic-info,
.info-group,
.source-snippet-list,
.editor-section,
.reading-panel,
.reading-article-shell,
.bottom-interaction-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-metrics,
.interaction-stats,
.reading-meta-grid,
.workbench-status {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.summary-metric,
.interaction-stat,
.reading-item,
.status-card,
.source-snippet-card {
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 14px;
}

.metric-label,
.reading-label,
.status-label {
  display: block;
  color: #909399;
  font-size: 12px;
  margin-bottom: 6px;
}

.group-heading-row h4,
.info-group h4,
.editor-header h4,
.summary-header h4,
.article-cover-content h1 {
  margin: 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label,
.keyword-label,
.summary-kicker,
.article-cover-kicker,
.workbench-kicker {
  color: #909399;
  font-size: 12px;
}

.keyword-list,
.outline-tags,
.editor-actions,
.draft-status-group,
.word-count-display,
.toolbar-hints,
.interaction-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.source-snippet-card strong,
.interaction-stat strong,
.reading-value,
.status-card strong {
  color: #303133;
}

.snippet-body,
.comment-content,
.reading-content,
.item-content {
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
}

.divider {
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.divider-handle {
  width: 6px;
  border-radius: 999px;
  background: linear-gradient(180deg, #dbeafe 0%, #bfdbfe 100%);
}

.editor-workbench-banner,
.article-cover {
  padding: 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.enhanced-editor-toolbar,
.comment-submit {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.rich-editor,
.comment-editor,
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reading-content :deep(h1),
.reading-content :deep(h2),
.reading-content :deep(h3) {
  margin-top: 1.2em;
  margin-bottom: 0.6em;
}

.reading-content :deep(ul) {
  padding-left: 20px;
}

.empty-panel {
  padding: 18px;
  border-radius: 14px;
  background: #f8fafc;
  color: #909399;
}

.comment-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 14px;
}

.comment-meta {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.loading-container,
.error-container {
  min-height: 240px;
}

@media (max-width: 1200px) {
  .main-content-area {
    grid-template-columns: 1fr;
  }

  .divider {
    display: none;
  }
}

@media (max-width: 768px) {
  .note-detail-page {
    padding: 16px;
  }

  .card-panel {
    padding: 18px;
  }

  .top-operation-bar,
  .context-banner,
  .bottom-operation-bar,
  .panel-header,
  .group-heading-row,
  .editor-header,
  .enhanced-editor-toolbar,
  .comment-submit {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-controls {
    min-width: 0;
    width: 100%;
  }
}
</style>
