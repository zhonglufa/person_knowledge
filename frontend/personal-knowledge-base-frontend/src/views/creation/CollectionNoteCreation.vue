<template>
  <div class="collection-note-creation-container">
    <div class="top-operation-bar">
      <div class="left-section">
        <el-button type="text" icon="el-icon-arrow-left" @click="goBack">
          返回
        </el-button>
        <div class="title-group">
          <h2 class="page-title">{{ pageTitle }}</h2>
          <p class="page-desc">{{ pageDescription }}</p>
        </div>
        <el-tag size="small" v-if="collection.category">{{ collection.category }}</el-tag>
        <el-tag size="small" :type="digestStatusTag.type" v-if="digestStatusTag.text">{{ digestStatusTag.text }}</el-tag>
      </div>
      <div class="right-section">
        <el-button @click="goToDrafts">
          草稿箱
        </el-button>
        <el-button type="primary" @click="saveDraft" :loading="savingDraft">
          保存草稿
        </el-button>
        <el-button @click="previewNote">
          预览笔记
        </el-button>
        <el-button type="success" @click="publishNote" :disabled="!canPublish">
          完成沉淀
        </el-button>
      </div>
    </div>

    <div class="context-banner" :class="{ offline: !isOnline }">
      <div class="context-item">
        <span class="context-label">来源收藏项</span>
        <span class="context-value">{{ collection.title || '未命名收藏项' }}</span>
      </div>
      <div class="context-item">
        <span class="context-label">加工目标</span>
        <span class="context-value">{{ collection.studyGoal || '建议补充本次沉淀的学习目标' }}</span>
      </div>
      <div class="context-item">
        <span class="context-label">当前状态</span>
        <span class="context-value">{{ digestStatusTag.text || '待加工' }}</span>
      </div>
      <div class="context-item" v-if="!isOnline">
        <span class="context-label">网络</span>
        <span class="context-value">离线模式</span>
      </div>
    </div>

    <div class="main-content-area" ref="mainContentArea" :style="mainContentAreaStyle">
      <div class="reference-panel workbench-panel" :class="{ 'is-mobile-hidden': isMobileLayout && activeMobilePanel !== 'reference' }">
        <div class="mobile-panel-header">
          <el-button type="text" icon="el-icon-back" @click="activeMobilePanel = 'draft'">笔记草稿</el-button>
        </div>
        <div class="panel-header">
          <h3>收藏项参照</h3>
          <div class="panel-controls">
            <el-button type="text" class="reference-toggle" @click="toggleReferenceInfo">
              <span>{{ referenceExpanded ? '收起信息' : '展开信息' }}</span>
              <i :class="['el-icon-arrow-down', { rotated: referenceExpanded }]" />
            </el-button>
            <el-input
              v-model="searchKeyword"
              placeholder="关键词检索..."
              prefix-icon="el-icon-search"
              size="small"
              @keyup.enter.native="searchInReference"
            />
          </div>
        </div>
        <div class="panel-scroll-area">
          <div class="reference-content">
            <div class="basic-info" v-show="referenceExpanded">
              <div class="info-item">
                <label>标题:</label>
                <span>{{ collection.title }}</span>
              </div>
              <div class="info-item">
                <label>来源:</label>
                <span>{{ collection.source || 'Web' }}</span>
              </div>
              <div class="info-item">
                <label>关键词:</label>
                <el-tag
                  v-for="keyword in collection.keywords ? collection.keywords.split(',') : []"
                  :key="keyword"
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
              <div class="info-item">
                <label>核心摘要:</label>
                <span>{{ collection.coreAbstract || '暂无摘要' }}</span>
              </div>
              <div class="info-item">
                <label>学习目标:</label>
                <span>{{ collection.studyGoal || '尚未设置学习目标' }}</span>
              </div>
              <div class="info-item">
                <label>学习进度:</label>
                <span>{{ collection.studyProgress || 0 }}%</span>
              </div>
            </div>

            <div class="content-body">
              <h4>主体内容</h4>
              <div v-if="parseInt(collection.sourceType) === 1" class="web-content-display">
                <div class="debug-panel">
                  <div class="debug-header" @click="toggleDebugPanel">
                    <span><strong>网页加载信息</strong></span>
                    <i :class="['el-icon-arrow-down', { 'rotated': debugPanelExpanded }]" />
                  </div>
                  <div v-show="debugPanelExpanded" class="debug-content">
                    <p>加载状态: {{ iframeLoading ? '加载中' : '加载完成' }}</p>
                    <p>错误状态: {{ iframeError ? '加载失败' : '正常' }}</p>
                    <p>URL: {{ collection.sourceUrl || collection.url }}</p>
                  </div>
                </div>
                <iframe
                  :key="iframeKey"
                  :src="collection.sourceUrl || collection.url"
                  class="web-iframe"
                  frameborder="0"
                  sandbox="allow-scripts allow-same-origin allow-forms"
                  @load="handleIframeLoad"
                  @error="handleIframeError"
                ></iframe>
                <div v-if="iframeLoading" class="loading-overlay">
                  <i class="el-icon-loading"></i>
                  <span>网页加载中...</span>
                </div>
                <div v-if="iframeError" class="error-overlay">
                  <i class="el-icon-warning"></i>
                  <span>网页加载失败</span>
                  <el-button size="mini" @click="reloadIframe">重新加载</el-button>
                </div>
              </div>
              <div v-else-if="parseInt(collection.sourceType) === 2" class="image-content-display">
                <el-image
                  :src="collection.sourceUrl || collection.url"
                  class="content-image"
                  fit="contain"
                  :preview-src-list="[collection.sourceUrl || collection.url]"
                  @error="handleImageError"
                >
                  <div slot="error" class="image-error">
                    <i class="el-icon-picture-outline"></i>
                    <p>图片加载失败</p>
                  </div>
                </el-image>
              </div>
              <div v-else-if="parseInt(collection.sourceType) === 3" class="text-content-display">
                <div
                  class="text-content"
                  v-html="formatTextContent(collection.content || collection.contentSnapshot || '暂无内容')"
                  @mouseup="handleTextSelection"
                ></div>
              </div>
              <div v-else-if="parseInt(collection.sourceType) === 4" class="video-content-display">
                <video
                  :src="collection.sourceUrl || collection.url"
                  class="content-video"
                  controls
                  preload="metadata"
                  @error="handleVideoError"
                  @loadstart="handleVideoLoadStart"
                  @canplay="handleVideoCanPlay"
                >
                  您的浏览器不支持视频播放。
                </video>
                <div v-if="videoLoading" class="video-loading">
                  <i class="el-icon-loading"></i>
                  <span>视频加载中...</span>
                </div>
                <div v-if="videoError" class="video-error">
                  <i class="el-icon-video-camera"></i>
                  <span>视频加载失败</span>
                </div>
              </div>
              <div v-else class="unknown-content-display">
                <div
                  class="content-display"
                  v-html="formatContent(collection.content || collection.contentSnapshot || '暂无内容')"
                  @mouseup="handleTextSelection"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="divider" ref="dividerRef" :class="{ 'is-mobile-hidden': isMobileLayout }">
        <div class="divider-handle" @mousedown="startResize"></div>
      </div>

      <div class="draft-panel workbench-panel" :class="{ 'is-mobile-hidden': isMobileLayout && activeMobilePanel !== 'draft' }">
        <div class="mobile-panel-header">
          <el-button type="text" icon="el-icon-back" @click="activeMobilePanel = 'reference'">收藏项参照</el-button>
        </div>
        <div class="panel-header">
          <h3>笔记草稿</h3>
          <div class="draft-status-group">
            <el-tag size="small" type="warning" v-if="currentDraftId">草稿已保存</el-tag>
            <el-tag size="small" type="info" v-else>新建沉淀</el-tag>
          </div>
        </div>
        <div class="panel-scroll-area">
          <div class="draft-content">
            <div class="note-config">
              <el-form :model="noteForm" :rules="noteRules" ref="noteForm" label-width="80px">
                <el-form-item label="笔记标题" prop="title">
                  <el-input v-model="noteForm.title" placeholder="请输入笔记标题" @input="markAsModified" />
                </el-form-item>
                <el-form-item label="分类" prop="categoryId">
                  <el-select v-model="noteForm.categoryId" placeholder="请选择分类" @change="markAsModified" style="width: 100%;">
                    <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="笔记类型" prop="noteType">
                  <el-radio-group v-model="noteForm.noteType" @change="handleNoteTypeChange">
                    <el-radio label="conceptual">概念型</el-radio>
                    <el-radio label="procedural">程序型</el-radio>
                    <el-radio label="factual">事实型</el-radio>
                    <el-radio label="metacognitive">元认知型</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="描述">
                  <el-input v-model="noteForm.description" type="textarea" :rows="2" placeholder="简要描述笔记内容..." @input="markAsModified" />
                </el-form-item>
              </el-form>
            </div>

            <div class="editor-section">
              <div class="editor-header">
                <h4>笔记内容</h4>
                <div class="editor-actions">
                  <el-button size="small" @click="insertTemplate">插入模板</el-button>
                  <el-button size="small" @click="copySelectedToNote" :disabled="!selectedText">复制选中内容</el-button>
                  <el-button size="small" @click="goToDrafts">查看草稿箱</el-button>
                </div>
              </div>
              <div class="editor-toolbar">
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
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-operation-bar">
      <div class="left-section">
        <span class="auto-save-status">
          <i class="el-icon-circle-check" v-if="lastSavedTime"></i>
          <i class="el-icon-loading" v-else></i>
          {{ lastSaveStatus }}
        </span>
      </div>
      <div class="right-section">
        <el-button @click="saveDraft" :loading="savingDraft">保存草稿</el-button>
        <el-button type="success" @click="publishNote" :loading="publishing" :disabled="!canPublish">完成沉淀</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { collectApi } from '@/api/collect.js'
import { noteApi } from '@/api/note.js'
import { escapeHtml, stripHtmlAndEscape } from '@/utils/sanitize'

const PANEL_RATIO_STORAGE_KEY = 'creation:collection-note-panel-ratio'

export default {
  name: 'CollectionNoteCreation',
  props: {
    id: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      collection: {},
      searchKeyword: '',
      noteForm: {
        title: '',
        categoryId: '',
        noteType: 'conceptual',
        description: '',
        coverImage: '',
        content: ''
      },
      noteRules: {
        title: [{ required: true, message: '请输入笔记标题', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        noteType: [{ required: true, message: '请选择笔记类型', trigger: 'change' }]
      },
      categories: [],
      selectedText: '',
      savingDraft: false,
      publishing: false,
      lastSavedTime: null,
      lastSaveStatus: '尚未保存',
      modified: false,
      iframeLoading: false,
      iframeError: false,
      iframeKey: 0,
      videoLoading: false,
      videoError: false,
      debugPanelExpanded: false,
      isResizing: false,
      panelSplitRatio: 0.5,
      minPanelWidth: 320,
      currentDraftId: null,
      drafts: [],
      saveInterval: null,
      isOnline: navigator.onLine,
      activeMobilePanel: 'draft',
      windowWidth: typeof window !== 'undefined' ? window.innerWidth : 1024,
      referenceExpanded: false
    }
  },
  computed: {
    ...mapGetters('user', ['getUserInfo']),
    routeCollectionId() {
      return this.id || this.$route.params.id
    },
    pageTitle() {
      return this.collection.title ? `${this.collection.title} - 笔记创作` : '收藏项笔记创作'
    },
    pageDescription() {
      return '从收藏项加工结果继续沉淀，形成可复用的个人知识笔记。'
    },
    digestStatusTag() {
      const status = this.collection.digestStatus
      const map = {
        undigest: { text: '未消化', type: 'info' },
        digesting: { text: '消化中', type: 'warning' },
        digested: { text: '已消化', type: 'success' },
        abandoned: { text: '已放弃', type: 'danger' }
      }
      return map[status] || { text: '', type: 'info' }
    },
    isMobileLayout() {
      return this.windowWidth < 1024
    },
    canPublish() {
      return !!(this.noteForm.title && this.noteForm.categoryId && this.noteForm.noteType && this.noteForm.content)
    },
    wordCountStats() {
      const content = this.noteForm.content || ''
      const totalCharacters = content.length
      const wordCount = content.trim() ? content.trim().split(/\s+/).length : 0
      return { totalCharacters, wordCount }
    },
    mainContentAreaStyle() {
      const baseStyle = {
        gridTemplateColumns: '1fr'
      }

      if (this.isMobileLayout) {
        return baseStyle
      }

      const left = `${(this.panelSplitRatio * 100).toFixed(2)}%`
      const right = `${((1 - this.panelSplitRatio) * 100).toFixed(2)}%`
      return {
        ...baseStyle,
        gridTemplateColumns: `minmax(${this.minPanelWidth}px, ${left}) 10px minmax(${this.minPanelWidth}px, ${right})`
      }
    }
  },
  methods: {
    restorePanelSplitRatio() {
      try {
        const storedRatio = window.localStorage.getItem(PANEL_RATIO_STORAGE_KEY)
        if (!storedRatio) {
          return
        }
        const parsedRatio = Number(storedRatio)
        if (Number.isFinite(parsedRatio) && parsedRatio >= 0.2 && parsedRatio <= 0.8) {
          this.panelSplitRatio = parsedRatio
        }
      } catch (error) {
        console.warn('恢复分栏比例失败:', error)
      }
    },
    persistPanelSplitRatio() {
      try {
        window.localStorage.setItem(PANEL_RATIO_STORAGE_KEY, String(this.panelSplitRatio))
      } catch (error) {
        console.warn('保存分栏比例失败:', error)
      }
    },
    async loadCollection() {
      if (!this.routeCollectionId) {
        this.$message.error('缺少收藏项ID，无法进入笔记创作页')
        this.goBack()
        return
      }
      try {
        const response = await collectApi.getCollectById(this.routeCollectionId)
        const payload = response?.data?.data || response?.data || {}
        this.collection = payload
        if (!this.collection?.id) {
          throw new Error('未找到对应收藏项')
        }
        if (!this.noteForm.title) {
          this.noteForm.title = this.collection.title ? `${this.collection.title} - 学习笔记` : ''
        }
        if (!this.noteForm.description) {
          this.noteForm.description = this.collection.coreAbstract || ''
        }
        if (parseInt(this.collection.sourceType) === 1) {
          this.iframeLoading = true
          this.iframeError = false
          this.iframeKey += 1
        }
      } catch (error) {
        console.error('加载收藏项失败:', error)
        this.$message.error(error?.message || '加载收藏项失败')
        this.goBack()
      }
    },
    async loadCategories() {
      try {
        const response = await collectApi.getCategoryList()
        this.categories = response?.data?.records || response?.data || []
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    async loadDrafts() {
      if (!this.routeCollectionId) {
        return
      }
      try {
        const response = await noteApi.getDraftList({ collectionItemId: this.routeCollectionId, pageNum: 1, pageSize: 20 })
        if (response?.code === 200) {
          this.drafts = response.data?.records || []
          if (this.drafts.length > 0 && !this.currentDraftId) {
            this.loadDraft(this.drafts[0])
          }
        }
      } catch (error) {
        console.error('加载草稿失败:', error)
      }
    },
    loadDraft(draft) {
      this.currentDraftId = draft.id
      this.noteForm = {
        title: draft.title,
        categoryId: draft.categoryId,
        noteType: draft.noteType || draft.type || 'conceptual',
        description: draft.description || '',
        coverImage: draft.coverImage || '',
        content: draft.content || ''
      }
      this.lastSavedTime = draft.updateTime ? new Date(draft.updateTime) : null
      this.lastSaveStatus = this.lastSavedTime ? `已加载草稿 · ${this.lastSavedTime.toLocaleString()}` : '已加载草稿'
      this.modified = false
    },
    markAsModified() {
      this.modified = true
      this.lastSaveStatus = '内容已修改，等待保存'
    },
    searchInReference() {
      if (!this.searchKeyword) return
      this.$message.info(`已定位关键词：${this.searchKeyword}`)
    },
    toggleReferenceInfo() {
      this.referenceExpanded = !this.referenceExpanded
    },
    toggleDebugPanel() {
      this.debugPanelExpanded = !this.debugPanelExpanded
    },
    handleIframeLoad() {
      this.iframeLoading = false
      this.iframeError = false
    },
    handleIframeError() {
      this.iframeLoading = false
      this.iframeError = true
    },
    reloadIframe() {
      this.iframeLoading = true
      this.iframeError = false
      this.iframeKey += 1
    },
    handleImageError() {
      this.$message.warning('图片加载失败')
    },
    handleVideoError() {
      this.videoLoading = false
      this.videoError = true
    },
    handleVideoLoadStart() {
      this.videoLoading = true
      this.videoError = false
    },
    handleVideoCanPlay() {
      this.videoLoading = false
    },
    formatTextContent(content) {
      const plainText = stripHtmlAndEscape(content || '暂无内容')
      return plainText.replace(/\n/g, '<br>')
    },
    formatContent(content) {
      const plainText = stripHtmlAndEscape(content || '暂无内容')
      return plainText.replace(/\n/g, '<br>')
    },
    handleTextSelection() {
      const selection = window.getSelection()?.toString()?.trim()
      this.selectedText = selection || ''
    },
    copySelectedToNote() {
      if (!this.selectedText) return
      const safeText = escapeHtml(this.selectedText)
      this.noteForm.content = `${this.noteForm.content}${this.noteForm.content ? '\n\n' : ''}${safeText}`
      this.markAsModified()
    },
    handleNoteTypeChange() {
      this.markAsModified()
    },
    insertTemplate() {
      const templates = {
        conceptual: '### 核心概念\n\n### 关键理解\n\n### 我的总结',
        procedural: '### 步骤拆解\n\n1. \n2. \n3. \n\n### 注意事项',
        factual: '### 事实记录\n\n### 关键信息\n\n### 结论',
        metacognitive: '### 我的思考\n\n### 问题反思\n\n### 后续行动'
      }
      if (!this.noteForm.content) {
        this.noteForm.content = templates[this.noteForm.noteType] || templates.conceptual
        this.markAsModified()
      }
    },
    async saveDraft() {
      this.savingDraft = true
      try {
        const draftData = {
          collectionItemId: this.collection.id || this.routeCollectionId,
          title: this.noteForm.title,
          categoryId: this.noteForm.categoryId,
          noteType: this.noteForm.noteType,
          description: this.noteForm.description,
          coverImage: this.noteForm.coverImage,
          content: this.noteForm.content
        }
        const response = await noteApi.saveNoteDraft(draftData)
        if (response?.code === 200) {
          this.currentDraftId = response.data?.id || response.data?.data?.id || this.currentDraftId
          this.lastSavedTime = new Date()
          this.lastSaveStatus = `已保存至草稿箱 · ${this.lastSavedTime.toLocaleTimeString()}`
          this.modified = false
          this.$message.success('草稿保存成功')
        }
      } catch (error) {
        console.error('保存草稿失败:', error)
        this.$message.error('保存草稿失败')
      } finally {
        this.savingDraft = false
      }
    },
    async previewNote() {
      if (!this.noteForm.content) {
        this.$message.warning('请先输入笔记内容')
        return
      }
      this.$alert(this.noteForm.content, '笔记预览', {
        confirmButtonText: '关闭',
        dangerouslyUseHTMLString: false
      })
    },
    async publishNote() {
      if (!this.canPublish) {
        this.$message.warning('请填写完整的笔记信息')
        return
      }
      this.publishing = true
      try {
        if (this.modified) {
          await this.saveDraft()
        }
        if (!this.currentDraftId) {
          this.$message.warning('请先保存草稿后再完成沉淀')
          return
        }
        const response = await noteApi.publishNote(this.currentDraftId)
        if (response?.code !== 200) {
          throw new Error(response?.message || '发布失败')
        }
        this.$message.success('笔记沉淀完成')
        this.$router.push('/creation/notes')
      } catch (error) {
        console.error('发布笔记失败:', error)
        this.$message.error(error?.message || '完成沉淀失败')
      } finally {
        this.publishing = false
      }
    },
    startAutoSave() {
      this.saveInterval = setInterval(() => {
        if (this.modified && this.isOnline) {
          this.saveDraft()
        }
      }, 30000)
    },
    stopAutoSave() {
      if (this.saveInterval) {
        clearInterval(this.saveInterval)
        this.saveInterval = null
      }
    },
    startResize(event) {
      if (this.isMobileLayout) {
        return
      }
      event.preventDefault()
      this.isResizing = true
      document.body.classList.add('collection-note-resizing')
      window.addEventListener('mousemove', this.handleResize)
      window.addEventListener('mouseup', this.stopResize)
    },
    handleResize(event) {
      if (this.isMobileLayout) {
        return
      }
      if (!this.isResizing || !this.$refs.mainContentArea) {
        return
      }
      const rect = this.$refs.mainContentArea.getBoundingClientRect()
      const dividerWidth = 10
      const availableWidth = rect.width - dividerWidth
      if (availableWidth <= 0) {
        return
      }
      const rawLeftWidth = event.clientX - rect.left
      const minLeft = this.minPanelWidth
      const maxLeft = availableWidth - this.minPanelWidth
      const clampedLeftWidth = Math.min(Math.max(rawLeftWidth, minLeft), maxLeft)
      this.panelSplitRatio = clampedLeftWidth / availableWidth
    },
    stopResize() {
      if (!this.isResizing) {
        return
      }
      this.isResizing = false
      this.persistPanelSplitRatio()
      document.body.classList.remove('collection-note-resizing')
      window.removeEventListener('mousemove', this.handleResize)
      window.removeEventListener('mouseup', this.stopResize)
    },
    goToDrafts() {
      this.$router.push('/creation/drafts')
    },
    goBack() {
      this.$router.push('/creation')
    },
    handleOnline() {
      this.isOnline = true
      if (this.modified) {
        this.saveDraft()
      }
    },
    handleOffline() {
      this.isOnline = false
      this.lastSaveStatus = '网络已断开，等待恢复后自动保存'
    },
    handleWindowResize() {
      this.windowWidth = window.innerWidth
    }
  },
  async mounted() {
    this.restorePanelSplitRatio()
    await Promise.all([this.loadCollection(), this.loadCategories(), this.loadDrafts()])
    this.activeMobilePanel = 'draft'
    this.startAutoSave()
    window.addEventListener('online', this.handleOnline)
    window.addEventListener('offline', this.handleOffline)
    window.addEventListener('resize', this.handleWindowResize)
  },
  beforeDestroy() {
    this.stopAutoSave()
    this.stopResize()
    window.removeEventListener('online', this.handleOnline)
    window.removeEventListener('offline', this.handleOffline)
    window.removeEventListener('resize', this.handleWindowResize)
  }
}
</script>

<style scoped>
.collection-note-creation-container {
  padding: var(--space-5);
  width: 100%;
  height: 100vh;
  overflow: hidden;
  box-sizing: border-box;
  background: var(--bg-page, #f5f7fa);
}

.top-operation-bar,
.bottom-operation-bar,
.context-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.top-operation-bar,
.bottom-operation-bar {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
}

.context-banner {
  margin: var(--space-4) 0;
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--primary-bg), var(--bg-card));
  border: 1px solid var(--primary-light);
  border-radius: var(--radius-xl);
}

.context-banner.offline {
  background: linear-gradient(135deg, var(--warning-bg), var(--bg-card));
  border-color: var(--warning-color);
}

.left-section,
.right-section,
.context-item,
.panel-controls,
.editor-actions,
.word-count-display,
.draft-status-group {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: var(--font-size-xl);
  color: var(--text-primary);
}

.page-desc,
.context-label,
.info-item label,
.section-label {
  color: var(--text-secondary);
}

.page-desc,
.context-value,
.info-item span {
  margin: 0;
  font-size: var(--font-size-sm);
}

.main-content-area {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) 10px minmax(320px, 1fr);
  gap: var(--space-4);
  height: calc(100vh - 260px);
  min-height: calc(100vh - 260px);
}

.workbench-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.reference-panel,
.draft-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  overflow: hidden;
}

.panel-header,
.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
  flex-shrink: 0;
}

.reference-toggle {
  padding: 0;
}

.basic-info {
  transition: opacity var(--transition-fast);
}

.mobile-panel-header {
  display: none;
  margin-bottom: var(--space-3);
}

.is-mobile-hidden {
  display: none;
}

@media (max-width: 1023px) {
  .collection-note-creation-container {
    height: auto;
    min-height: 100vh;
    overflow: visible;
    padding: var(--space-4);
  }

  .top-operation-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .top-operation-bar .left-section {
    flex-wrap: wrap;
  }

  .top-operation-bar .right-section {
    width: 100%;
    justify-content: flex-end;
    flex-wrap: wrap;
  }

  .context-banner {
    flex-direction: column;
    align-items: stretch;
  }

  .context-item {
    justify-content: space-between;
  }

  .main-content-area {
    height: auto;
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .workbench-panel {
    padding: var(--space-4);
  }

  .divider {
    display: none;
  }

  .mobile-panel-header {
    display: flex;
  }

  .bottom-operation-bar {
    position: sticky;
    bottom: var(--space-4);
    z-index: var(--z-sticky);
  }
}

.panel-scroll-area {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

.basic-info {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.keyword-tag {
  margin-right: 4px;
}

.content-body,
.note-config,
.editor-section {
  margin-top: var(--space-4);
}

.text-content,
.content-display {
  line-height: 1.8;
  color: var(--text-regular);
}

.web-content-display,
.image-content-display,
.video-content-display {
  position: relative;
  min-height: 280px;
  max-height: 100%;
}

.web-iframe,
.content-image,
.content-video {
  width: 100%;
  min-height: 640px;
  max-height: 100%;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.loading-overlay,
.error-overlay,
.video-loading,
.video-error {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  background: rgba(255, 255, 255, 0.9);
}

.debug-panel {
  margin-bottom: var(--space-3);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
}

.debug-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3);
  cursor: pointer;
}

.debug-content {
  padding: 0 var(--space-3) var(--space-3);
}

.rotated {
  transform: rotate(180deg);
}

.divider {
  display: flex;
  justify-content: center;
  align-items: stretch;
}

.divider-handle {
  width: 6px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--primary-light), var(--border-base));
  cursor: col-resize;
  transition: background 0.2s ease;
  height: 100%;
}

.divider-handle:hover {
  background: linear-gradient(180deg, var(--primary-base), var(--primary-light));
}

.image-error,
.unknown-content-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: var(--text-secondary);
}

.auto-save-status {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
}
</style>
