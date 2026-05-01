<template>
  <div class="collection-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <el-button type="primary" @click="loadCollectionDetail">重新加载</el-button>
        </template>
      </el-result>
    </div>
    
    <!-- 正常内容 -->
    <div v-else-if="collection" class="detail-wrapper">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/collections' }">收藏集</el-breadcrumb-item>
                <el-breadcrumb-item>{{ collection.name }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <h1 class="page-title">{{ collection.name }}</h1>
            <p v-if="collection.description" class="page-subtitle">
              {{ collection.description }}
            </p>
          </div>
          
          <div class="header-right">
            <div class="header-stats">
              <div class="stat-item">
                <i class="fas fa-file-alt text-primary"></i>
                <span class="stat-number">{{ collection.itemCount || 0 }}</span>
                <span class="stat-label">收藏项</span>
              </div>
              <div class="stat-item">
                <i class="fas fa-layer-group text-primary"></i>
                <span class="stat-number">{{ collection.tagCount || 0 }}</span>
                <span class="stat-label">标签</span>
              </div>
              <!-- 点赞数显示 -->
              <div class="stat-item">
                <like-button
                  :target-id="collection.id"
                  target-type="collection"
                  :initial-count="collection.likeCount || 0"
                  :show-text="false"
                  @like="handleLike"
                  @unlike="handleUnlike"
                />
              </div>
              <!-- 收藏数显示 -->
              <div class="stat-item">
                <i class="fas fa-star text-warning"></i>
                <span class="stat-number">{{ collection.collectCount || 0 }}</span>
                <span class="stat-label">收藏</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 左侧信息面板 -->
        <div class="info-panel">
          <div class="cover-card card">
            <div class="cover-wrapper">
              <img 
                v-if="collection.coverImage" 
                :src="collection.coverImage" 
                :alt="collection.name"
                class="cover-image"
                @error="handleCoverError"
              />
              <div v-else class="cover-placeholder" :style="{ backgroundColor: coverColor }">
                <i class="fas fa-layer-group"></i>
              </div>
            </div>
            
            <div class="cover-meta">
              <div class="meta-tags">
                <el-tag v-if="collection.isDefault" type="warning" size="small">默认</el-tag>
                <el-tag v-if="collection.isShared" type="success" size="small">已共享</el-tag>
                <el-tag v-if="collection.isPrivate" type="info" size="small">私有</el-tag>
              </div>
              
              <div class="meta-info">
                <div class="meta-row">
                  <i class="fas fa-calendar-plus"></i>
                  <span>创建于 {{ formatDate(collection.createdAt) }}</span>
                </div>
                <div v-if="collection.updatedAt" class="meta-row">
                  <i class="fas fa-history"></i>
                  <span>更新于 {{ formatDate(collection.updatedAt) }}</span>
                </div>
                <div class="meta-row">
                  <i class="fas fa-user"></i>
                  <span>创建者 ID: {{ collection.userId }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮栏 -->
          <div class="action-panel card">
            <h3 class="panel-title">操作</h3>
            <div class="action-buttons">
              <el-button 
                type="primary" 
                icon="el-icon-plus"
                class="action-btn"
                @click="showAddItemDialog = true"
              >
                添加收藏项
              </el-button>
              <el-button 
                icon="el-icon-edit"
                class="action-btn"
                @click="showEditDialog = true"
              >
                编辑收藏集
              </el-button>
              <el-button 
                icon="el-icon-document"
                class="action-btn"
                @click="handleProcessCollection"
              >
                加工编辑
              </el-button>
              
              <!-- 消化状态快捷操作 -->
              <div class="digest-status-section">
                <h4 class="section-subtitle">消化状态筛选</h4>
                <div class="digest-filter-buttons">
                  <el-button
                    :type="filterDigest === '' ? 'primary' : ''"
                    size="small"
                    @click="filterDigest = ''"
                  >
                    全部
                  </el-button>
                  <el-button
                    :type="filterDigest === 'undigest' ? 'danger' : ''"
                    size="small"
                    @click="filterDigest = 'undigest'"
                  >
                    <i class="fas fa-circle"></i> 未消化
                  </el-button>
                  <el-button
                    :type="filterDigest === 'digesting' ? 'warning' : ''"
                    size="small"
                    @click="filterDigest = 'digesting'"
                  >
                    <i class="fas fa-spinner"></i> 消化中
                  </el-button>
                  <el-button
                    :type="filterDigest === 'digested' ? 'success' : ''"
                    size="small"
                    @click="filterDigest = 'digested'"
                  >
                    <i class="fas fa-check-circle"></i> 已消化
                  </el-button>
                  <el-button
                    :type="filterDigest === 'abandoned' ? 'info' : ''"
                    size="small"
                    @click="filterDigest = 'abandoned'"
                  >
                    <i class="fas fa-times-circle"></i> 已放弃
                  </el-button>
                </div>
              </div>
              
              <el-dropdown @command="handleMoreAction" class="more-dropdown">
                <el-button class="action-btn">
                  更多操作 <i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="set-default" v-if="!collection.isDefault">
                      <i class="fas fa-star"></i>
                      设为默认
                    </el-dropdown-item>
                    <el-dropdown-item command="share" v-if="!collection.isShared">
                      <i class="fas fa-share-alt"></i>
                      分享
                    </el-dropdown-item>
                    <el-dropdown-item command="export">
                      <i class="fas fa-download"></i>
                      导出
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <i class="fas fa-trash-alt text-danger"></i>
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
        
        <!-- 关联笔记区域 -->
        <div class="related-notes-panel card">
          <div class="panel-header">
            <h3 class="panel-title">
              <i class="fas fa-link"></i>
              关联笔记
            </h3>
            <el-button 
              type="text" 
              size="small"
              @click="loadRelatedNotes"
            >
              <i class="fas fa-sync-alt"></i> 刷新
            </el-button>
          </div>
          
          <div v-loading="relatedNotesLoading" class="related-notes-content">
            <!-- 空状态 -->
            <div v-if="!relatedNotesLoading && relatedNotes.length === 0" class="empty-related">
              <i class="fas fa-file-alt"></i>
              <p>暂无关联笔记</p>
              <p class="empty-hint">创建笔记时可以添加此收藏集的标签来建立关联</p>
            </div>
            
            <!-- 笔记列表 -->
            <div v-else class="related-notes-list">
              <div
                v-for="note in relatedNotes"
                :key="note.id"
                class="related-note-item"
                @click="viewNote(note)"
              >
                <div class="note-icon">
                  <i class="fas fa-file-alt"></i>
                </div>
                <div class="note-content">
                  <h4 class="note-title">{{ note.title }}</h4>
                  <p class="note-excerpt">{{ getNoteExcerpt(note) }}</p>
                  <div class="note-meta">
                    <span><i class="fas fa-calendar"></i> {{ formatDate(note.updateTime) }}</span>
                    <span><i class="fas fa-font"></i> {{ note.wordCount || 0 }} 字</span>
                    <el-tag size="mini" :type="getNoteTypeTag(note.type)">
                      {{ getNoteTypeLabel(note.type) }}
                    </el-tag>
                  </div>
                </div>
                <div class="note-arrow">
                  <i class="fas fa-chevron-right"></i>
                </div>
              </div>
              
              <!-- 查看更多 -->
              <div v-if="relatedNotes.length >= 5" class="view-more" @click="viewAllRelatedNotes">
                <span>查看全部 {{ relatedTotalCount }} 条关联笔记</span>
                <i class="fas fa-arrow-right"></i>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧内容区域 -->
        <div class="content-area">
          <!-- 批量操作提示 -->
          <div v-if="selectedItems.length > 0" class="batch-operation-bar">
            <div class="batch-info">
              <i class="fas fa-check-circle text-success"></i>
              <span>已选择 {{ selectedItems.length }} 个收藏项</span>
            </div>
            <div class="batch-actions">
              <el-button 
                type="success"
                icon="el-icon-check"
                @click="showBatchDialog = true"
              >
                批量操作
              </el-button>
              <el-button 
                icon="el-icon-close"
                @click="selectedItems = []"
              >
                取消选择
              </el-button>
            </div>
          </div>
          
          <!-- 工具栏 -->
          <div class="content-toolbar card">
            <div class="toolbar-left">
              <div class="search-wrapper">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索收藏项..."
                  prefix-icon="el-icon-search"
                  clearable
                  class="search-input"
                  @input="handleSearch"
                />
              </div>
              
              <div class="filter-group">
                <el-select
                  v-model="selectedTag"
                  placeholder="按标签筛选"
                  clearable
                  class="filter-select"
                  @change="handleTagFilter"
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
                
                <el-select
                  v-model="sourceType"
                  placeholder="按来源筛选"
                  clearable
                  class="filter-select"
                  @change="handleSourceFilter"
                >
                  <el-option label="网页" :value="1" />
                  <el-option label="图片" :value="2" />
                  <el-option label="文本" :value="3" />
                  <el-option label="视频" :value="4" />
                </el-select>
              </div>
            </div>
            
            <div class="toolbar-right">
              <div class="view-toggle">
                <el-button-group>
                  <el-button 
                    :type="viewMode === 'grid' ? 'primary' : ''"
                    icon="el-icon-menu"
                    @click="viewMode = 'grid'"
                    title="网格视图"
                  />
                  <el-button 
                    :type="viewMode === 'list' ? 'primary' : ''"
                    icon="el-icon-tickets"
                    @click="viewMode = 'list'"
                    title="列表视图"
                  />
                </el-button-group>
              </div>
              
              <div class="result-info">
                <span class="total-count">共 {{ totalItems }} 个项</span>
              </div>
            </div>
          </div>
          
          <!-- 收藏项列表 -->
          <div class="items-section">
            <div v-if="itemsLoading" class="items-loading">
              <el-skeleton :rows="6" animated />
            </div>
            
            <div v-else-if="collectionItems.length === 0" class="empty-state">
              <div class="empty-content">
                <i class="fas fa-inbox empty-icon"></i>
                <h3 class="empty-title">暂无收藏项</h3>
                <p class="empty-description">这个收藏集还没有添加任何收藏项</p>
                <el-button type="primary" @click="showAddItemDialog = true">
                  <i class="el-icon-plus"></i>
                  添加第一个收藏项
                </el-button>
              </div>
            </div>
            
            <div v-else class="items-container" :class="`view-${viewMode}`">
              <el-checkbox-group 
                v-model="selectedItems" 
                v-if="showBatchSelection"
                class="batch-selection-wrapper"
              >
                <div
                  v-for="item in filteredCollectionItems"
                  :key="item.id"
                  class="item-wrapper"
                >
                  <el-checkbox 
                    :label="item.id" 
                    :value="item"
                    class="item-checkbox"
                  />
                  <component
                    :is="itemComponent"
                    :item="item"
                    :collection-id="collection.id"
                    @edit="handleEditItem"
                    @delete="handleDeleteItem"
                    @process="handleProcessItem"
                    @click="handleItemClick"
                  />
                </div>
              </el-checkbox-group>
              
              <component
                :is="itemComponent"
                v-for="item in filteredCollectionItems"
                v-else
                :key="item.id"
                :item="item"
                :collection-id="collection.id"
                @edit="handleEditItem"
                @delete="handleDeleteItem"
                @process="handleProcessItem"
                @click="handleItemClick"
                @contextmenu="showItemContextMenu"
              />
            </div>
            
            <!-- 分页 -->
            <div v-if="totalItems > 0" class="pagination-container">
              <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :total="totalItems"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
              />
            </div>

            <!-- 评论区 -->
            <div class="comment-section-wrapper card">
              <comment-section
                :target-id="collection.id"
                target-type="collection"
                :current-user-id="currentUserId"
                :current-user-name="currentUserName"
                :current-user-avatar="currentUserAvatar"
                @comment-submitted="handleCommentSubmitted"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加收藏项对话框 -->
    <add-collection-item-dialog
      v-if="collection && collection.id"
      :visible.sync="showAddItemDialog"
      :collection-id="collection.id"
      @success="handleItemAdded"
    />
    
    <!-- 编辑收藏项对话框 -->
    <edit-collection-item-dialog
      :visible.sync="showEditItemDialog"
      :item="editingItem"
      @success="handleItemUpdated"
    />
    
    <!-- 编辑收藏集对话框 -->
    <collection-edit-dialog
      :visible.sync="showEditDialog"
      :collection="collection"
      @success="handleCollectionUpdated"
    />
    
    <!-- 加工编辑对话框 -->
    <collection-item-digest-editor
      :visible.sync="showProcessDialog"
      :item-id="processingItem?.id"
      @updated="handleItemProcessed"
    />
    
    <!-- 批量操作对话框 -->
    <batch-processing-dialog
      :visible.sync="showBatchDialog"
      :selected-items="selectedCollectionItems"
      @success="handleBatchSuccess"
    />
  </div>
</template>

<script>
import { collectionsApi } from '@/api/collections'
import { collectApi } from '@/api/collect'
import AddCollectionItemDialog from '@/components/collect/AddCollectionItemDialog.vue'
import EditCollectionItemDialog from '@/components/collect/EditCollectionItemDialog.vue'
import CollectionEditDialog from '@/components/collect/CollectionEditDialog.vue'
import CollectionItemCard from '@/components/collect/CollectionItemCard.vue'
import CollectionItemDigestEditor from '@/components/collect/CollectionItemDigestEditor.vue'
import BatchProcessingDialog from '@/components/collect/BatchProcessingDialog.vue'
// 互动组件 - 点赞和评论
import LikeButton from '@/components/interaction/LikeButton.vue'
import CommentSection from '@/components/interaction/CommentSection.vue'

export default {
  name: 'CollectionDetailView',
  components: {
    AddCollectionItemDialog,
    EditCollectionItemDialog,
    CollectionEditDialog,
    CollectionItemCard,
    CollectionItemDigestEditor,
    BatchProcessingDialog,
    // 互动组件
    LikeButton,
    CommentSection
  },
  props: {
    id: {
      type: [String, Number],
      required: false
    }
  },
  data() {
    return {
      loading: true,
      error: null,
      collection: null,
      collectionId: null,
      collectionItems: [],
      itemsLoading: false,
      
      // 分页相关
      currentPage: 1,
      pageSize: 20,
      totalItems: 0,
      
      // 搜索筛选
      searchKeyword: '',
      selectedTag: '',
      sourceType: '',
      filterDigest: '', // 消化状态筛选
      searchTimer: null,
      
      // 视图模式
      viewMode: 'grid',
      
      // 对话框控制
      showAddItemDialog: false,
      showEditItemDialog: false,
      showEditDialog: false,
      showProcessDialog: false,
      showBatchDialog: false,
      editingItem: null,
      processingItem: null,
      selectedItems: [],
      
      // 可用标签
      availableTags: [],
      
      // 关联笔记相关
      relatedNotes: [],
      relatedNotesLoading: false,
      relatedTotalCount: 0
    }
  },
  computed: {
    itemComponent() {
      return this.viewMode === 'grid' ? 'CollectionItemCard' : 'CollectionItemListItem'
    },
    
    // 批量选择相关
    showBatchSelection() {
      return this.selectedItems.length > 0
    },
    
    selectedCollectionItems() {
      return this.collectionItems.filter(item => 
        this.selectedItems.includes(item.id)
      )
    },
    coverColor() {
      // 生成基于收藏集名称的颜色
      if (!this.collection?.name) return '#4ECDC4'
      let hash = 0
      for (let i = 0; i < this.collection.name.length; i++) {
        hash = this.collection.name.charCodeAt(i) + ((hash << 5) - hash)
      }
      const color = Math.abs(hash).toString(16).substring(0, 6)
      return `#${color.padEnd(6, '0')}`
    },

    // ==================== 互动功能相关 ====================
    /**
     * 获取当前用户ID
     */
    currentUserId() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        return userInfo.id || userInfo.userId || null
      } catch {
        return null
      }
    },

    /**
     * 获取当前用户名
     */
    currentUserName() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        return userInfo.nickname || userInfo.username || '用户'
      } catch {
        return '用户'
      }
    },

    /**
     * 获取当前用户头像
     */
    currentUserAvatar() {
      try {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        return userInfo.avatar || ''
      } catch {
        return ''
      }
    },
    
    /**
     * 根据消化状态过滤收藏项
     */
    filteredCollectionItems() {
      if (!this.filterDigest) {
        return this.collectionItems
      }
      return this.collectionItems.filter(item => item.digestStatus === this.filterDigest)
    }
  },
  async created() {
    // 优先使用路由参数，如果没有则使用props
    const collectionId = this.$route.params.id || this.id;
    console.log('获取到的收藏集ID:', collectionId);
    
    if (!collectionId) {
      this.error = '缺少收藏集ID参数';
      this.loading = false;
      return;
    }
    
    // 将ID转换为数字类型
    this.collectionId = parseInt(collectionId);
    
    await this.loadCollectionDetail()
    await this.loadCollectionItems()
    await this.loadAvailableTags()
    await this.loadRelatedNotes() // 加载关联笔记
  },
  methods: {
    // 加载关联笔记
    async loadRelatedNotes() {
      if (!this.collection?.id) return;
      
      this.relatedNotesLoading = true;
      try {
        // TODO: 调用后端API获取关联笔记
        // const response = await collectionsApi.getCollectionRelatedNotes(this.collection.id)
        
        // 模拟数据
        this.relatedNotes = this.getMockRelatedNotes();
        this.relatedTotalCount = this.relatedNotes.length;
      } catch (error) {
        console.error('加载关联笔记失败:', error);
      } finally {
        this.relatedNotesLoading = false;
      }
    },
    
    // 查看笔记详情
    viewNote(note) {
      this.$router.push(`/creation/notes/${note.id}`);
    },
    
    // 查看全部关联笔记
    viewAllRelatedNotes() {
      // 可以跳转到专门的关联笔记列表页
      this.$router.push({
        path: '/creation/notes',
        query: { collectionId: this.collection.id }
      });
    },
    
    // 获取笔记摘要
    getNoteExcerpt(note) {
      if (!note.content) return '暂无内容';
      const text = note.content.replace(/[#*`_\[\]()>]/g, '').trim();
      return text.length > 80 ? text.substring(0, 80) + '...' : text;
    },
    
    // 获取笔记类型标签
    getNoteTypeTag(type) {
      const tags = {
        original: 'success',
        summary: 'warning',
        normal: ''
      };
      return tags[type] || '';
    },
    
    // 获取笔记类型标签文本
    getNoteTypeLabel(type) {
      const labels = {
        original: '原创',
        summary: '总结',
        normal: '普通'
      };
      return labels[type] || type;
    },
    
    // 模拟关联笔记数据
    getMockRelatedNotes() {
      if (!this.collection?.name) return [];
      
      return [
        {
          id: 1,
          title: `关于《${this.collection.name}》的学习笔记`,
          content: `这是关于${this.collection.name}的学习笔记，记录了重要的知识点和实践心得...`,
          wordCount: 1247,
          type: 'original',
          updateTime: '2024-01-15'
        },
        {
          id: 2,
          title: `${this.collection.name} 实践总结`,
          content: `通过实际项目应用，总结了最佳实践和常见问题解决方案...`,
          wordCount: 2156,
          type: 'summary',
          updateTime: '2024-01-14'
        },
        {
          id: 3,
          title: `${this.collection.name} 相关资料整理`,
          content: `整理了相关的优质资源、教程和文档链接...`,
          wordCount: 856,
          type: 'normal',
          updateTime: '2024-01-13'
        }
      ];
    },
    
    // 加载收藏集详情
    async loadCollectionDetail() {
      this.loading = true
      this.error = null
      
      try {
        console.log('正在加载收藏集详情，ID:', this.collectionId);
        const response = await collectionsApi.getCollectionDetail(this.collectionId)
        console.log('收藏集详情响应:', response);
        
        // 检查响应结果
        if (response) {
          if (response.code === 200) {
            this.collection = response.data
            console.log('收藏集数据:', this.collection);
          } else {
            this.error = response.message || response.msg || '加载失败'
            console.error('加载失败:', this.error);
          }
        } else {
          this.error = '服务器响应格式错误'
          console.error('服务器响应为空或格式不正确:', response);
        }
      } catch (error) {
        console.error('加载收藏集详情失败:', error)
        // 提供更详细的错误信息
        if (error.response) {
          // 服务器返回了错误响应
          this.error = `服务器错误(${error.response.status}): ${error.response.data?.message || error.response.data?.msg || '未知错误'}`
        } else if (error.request) {
          // 请求已发出但没有收到响应
          this.error = '网络连接失败，请检查网络连接'
        } else {
          // 其他错误
          this.error = '请求失败: ' + (error.message || '未知错误')
        }
      } finally {
        this.loading = false
      }
    },
    
    // 加载收藏项列表
    async loadCollectionItems() {
      if (!this.collection) return
      
      this.itemsLoading = true
      
      try {
        const params = {
          page: this.currentPage,
          pageSize: this.pageSize,
          keyword: this.searchKeyword,
          tagId: this.selectedTag,
          sourceType: this.sourceType
        }
        
        console.log('加载收藏项，收藏集ID:', this.collection.id, '参数:', params);
        const response = await collectionsApi.getCollectionItems(this.collection.id, params)
        console.log('收藏项列表响应:', response);
        
        if (response.data.code === 200) {
          this.collectionItems = response.data.data.records || []
          this.totalItems = response.data.data.total || 0
          console.log('收藏项数据:', this.collectionItems, '总数:', this.totalItems);
        } else {
          console.error('加载收藏项失败:', response.data.msg);
          this.$message.error(response.data.msg || '加载收藏项失败')
        }
      } catch (error) {
        console.error('加载收藏项失败:', error)
        this.$message.error('加载收藏项失败 ' + (error.message || '未知错误'))
      } finally {
        this.itemsLoading = false
      }
    },
    
    // 加载可用标签
    async loadAvailableTags() {
      try {
        const response = await collectApi.getTags()
        if (response.data.code === 200) {
          this.availableTags = response.data.data || []
        }
      } catch (error) {
        console.error('加载标签失败:', error)
      }
    },
    
    // 搜索处理
    handleSearch() {
      clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => {
        this.currentPage = 1
        this.loadCollectionItems()
      }, 500)
    },
    
    // 标签筛选
    handleTagFilter() {
      this.currentPage = 1
      this.loadCollectionItems()
    },
    
    // 来源筛选
    handleSourceFilter() {
      this.currentPage = 1
      this.loadCollectionItems()
    },
    
    // 分页处理
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadCollectionItems()
    },
    
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadCollectionItems()
    },
    
    // 更多操作
    async handleMoreAction(command) {
      console.log('执行菜单操作:', command);
      switch (command) {
        case 'set-default':
          await this.setDefaultCollection()
          break
        case 'share':
          await this.shareCollection()
          break
        case 'export':
          this.exportCollection()
          break
        case 'delete':
          this.deleteCollection()
          break
        default:
          console.warn('未知的操作命令:', command);
      }
    },
    
    // 设置默认收藏集
    async setDefaultCollection() {
      try {
        console.log('设置默认收藏集，ID:', this.collection.id);
        const response = await collectionsApi.setDefaultCollection(this.collection.id)
        console.log('设置默认收藏集响应:', response);
        
        if (response.data.code === 200) {
          this.$message.success('设置默认收藏集成功')
          this.collection.isDefault = true
          // 更新本地存储或其他相关状态
        } else {
          this.$message.error(response.data.msg || '设置失败')
        }
      } catch (error) {
        console.error('设置默认收藏集失败:', error)
        this.$message.error('设置失败: ' + (error.message || '未知错误'))
      }
    },
    
    // 分享收藏集
    async shareCollection() {
      try {
        console.log('分享收藏集，ID:', this.collection.id);
        // 这里应该调用分享API
        this.$message.info('分享功能开发中...')
        // 模拟分享操作
        setTimeout(() => {
          this.collection.isShared = true;
          this.$message.success('分享成功');
        }, 1000);
      } catch (error) {
        console.error('分享失败:', error)
        this.$message.error('分享失败: ' + (error.message || '未知错误'))
      }
    },
    
    // 导出收藏集
    exportCollection() {
      // 导出逻辑
      this.$message.info('导出功能开发中...')
    },
    
    // 删除收藏集
    deleteCollection() {
      this.$confirm('确定要删除这个收藏集吗？此操作不可恢复？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('删除收藏集，ID:', this.collection.id);
          const response = await collectionsApi.deleteCollection(this.collection.id)
          console.log('删除收藏集响应:', response);
          
          if (response.data.code === 200) {
            this.$message.success('删除成功')
            // 返回上一页或跳转到收藏集列表
            this.$router.push('/collections')
          } else {
            this.$message.error(response.data.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 用户取消删除
        console.log('用户取消删除操作');
      })
    },
    
    // 收藏项相关操作
    handleEditItem(item) {
      this.editingItem = item
      this.showEditItemDialog = true
    },
    
    handleDeleteItem(item) {
      this.$confirm('确定要删除这个收藏项吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await collectApi.deleteCollect(item.id)
          if (response.data.code === 200) {
            this.$message.success('删除成功')
            this.loadCollectionItems()
          } else {
            this.$message.error(response.data.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      })
    },
    
    handleItemClick(item) {
      // 处理收藏项点击事件
      console.log('点击收藏项:', item)
    },
    
    // 显示收藏项右键菜单
    showItemContextMenu(event, item) {
      // 创建自定义右键菜单
      const menu = document.createElement('div');
      menu.className = 'custom-context-menu';
      menu.style.position = 'fixed';
      menu.style.left = event.clientX + 'px';
      menu.style.top = event.clientY + 'px';
      menu.style.backgroundColor = 'white';
      menu.style.border = '1px solid #dcdfe6';
      menu.style.borderRadius = '4px';
      menu.style.boxShadow = '0 2px 12px 0 rgba(0, 0, 0, 0.1)';
      menu.style.padding = '4px 0';
      menu.style.zIndex = '9999';
      menu.style.minWidth = '120px';
      
      // 添加菜单项
      const menuItemStyles = `
        display: block;
        padding: 8px 16px;
        text-decoration: none;
        color: #606266;
        cursor: pointer;
        font-size: 14px;
      `;
      
      const processItem = document.createElement('div');
      processItem.innerHTML = '加工编辑';
      processItem.style = menuItemStyles;
      processItem.onclick = () => {
        this.handleProcessItem(item);
        document.body.removeChild(menu);
      };
      
      const editItem = document.createElement('div');
      editItem.innerHTML = '编辑';
      editItem.style = menuItemStyles;
      editItem.onclick = () => {
        this.handleProcessItem(item); // 编辑和加工是同一功能
        document.body.removeChild(menu);
      };
      
      const deleteItem = document.createElement('div');
      deleteItem.innerHTML = '删除';
      deleteItem.style = `
        ${menuItemStyles}
        border-top: 1px solid #ebeef5;
        color: #f56c6c;
      `;
      deleteItem.onclick = () => {
        this.handleDeleteItem(item);
        document.body.removeChild(menu);
      };
      
      menu.appendChild(processItem);
      menu.appendChild(editItem);
      menu.appendChild(deleteItem);
      
      // 点击其他地方关闭菜单
      const closeMenu = () => {
        if (document.body.contains(menu)) {
          document.body.removeChild(menu);
        }
        document.removeEventListener('click', closeMenu);
      };
      
      setTimeout(() => {
        document.addEventListener('click', closeMenu);
      }, 0);
      
      document.body.appendChild(menu);
    },
    
    // 处理加工编辑
    handleProcessItem(item) {
      this.processingItem = item
      this.showProcessDialog = true
    },
    
    // 处理加工完成
    handleItemProcessed() {
      this.showProcessDialog = false
      this.processingItem = null
      // 重新加载收藏项列表以更新状态
      this.loadCollectionItems()
      this.$message.success('加工信息保存成功')
    },
    
    // 处理收藏集加工编辑（批量操作）
    handleProcessCollection() {
      if (!this.collectionItems || this.collectionItems.length === 0) {
        this.$message.warning('当前收藏集没有收藏项可以加工')
        return
      }
      
      // 启用批量选择模式
      if (this.selectedItems.length === 0) {
        this.$message.info('请先选择要批量操作的收藏项')
        // 这里可以添加UI提示引导用户选择
        return
      }
      
      this.showBatchDialog = true
    },
    
    // 处理批量操作成功
    handleBatchSuccess() {
      this.showBatchDialog = false
      this.selectedItems = []
      this.loadCollectionItems()
      this.$message.success('批量操作执行成功')
    },
    
    handleItemAdded() {
      this.showAddItemDialog = false
      this.loadCollectionItems()
    },
    
    handleItemUpdated() {
      this.showEditItemDialog = false
      this.loadCollectionItems()
    },
    
    handleCollectionUpdated(updatedCollection) {
      this.showEditDialog = false
      this.collection = { ...this.collection, ...updatedCollection }
      this.$message.success('更新成功')
    },
    
    // 封面图片加载失败处理
    handleCoverError(event) {
      event.target.style.display = 'none'
      event.target.parentElement.classList.add('image-error')
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },

    // ==================== 互动功能事件处理 ====================
    /**
     * 处理点赞事件
     */
    handleLike() {
      console.log('点赞成功')
      // 更新收藏集点赞数
      if (this.collection) {
        this.collection.likeCount = (this.collection.likeCount || 0) + 1
      }
    },

    /**
     * 处理取消点赞事件
     */
    handleUnlike() {
      console.log('取消点赞')
      // 更新收藏集点赞数
      if (this.collection) {
        this.collection.likeCount = Math.max(0, (this.collection.likeCount || 0) - 1)
      }
    },

    /**
     * 处理评论提交事件
     */
    handleCommentSubmitted() {
      console.log('评论提交成功')
      // 可以在这里更新评论数或其他相关逻辑
      if (this.collection) {
        this.collection.commentCount = (this.collection.commentCount || 0) + 1
      }
    }
  }
}
</script>

<style scoped>
/* 页面基础样式 */
.collection-detail-view {
  padding: 0;
  max-width: 100%;
  margin: 0;
  min-height: calc(100vh - 60px);
  background-color: var(--bg-secondary);
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  padding: 80px 20px;
  text-align: center;
  background: var(--bg-primary);
  min-height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 主内容 */
.detail-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 页面头部 */
.page-header {
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-light);
  padding: var(--space-6) 0;
  box-shadow: var(--shadow-sm);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-6);
}

.header-left {
  flex: 1;
}

.breadcrumb {
  margin-bottom: var(--space-3);
}

.page-title {
  font-size: var(--font-size-xxxxl);
  font-weight: var(--font-weight-bold);
  color: var(--text-dark);
  margin: 0 0 var(--space-3) 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: var(--font-size-lg);
  color: var(--text-light);
  margin: 0;
  line-height: 1.5;
  max-width: 600px;
}

.header-right {
  flex-shrink: 0;
}

.header-stats {
  display: flex;
  gap: var(--space-5);
  background: var(--bg-secondary);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-light);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  text-align: center;
  min-width: 80px;
}

.stat-item i {
  font-size: 20px;
  margin-bottom: var(--space-1);
}

.stat-number {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-bold);
  color: var(--text-dark);
  line-height: 1;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-light);
  text-transform: uppercase;
  letter-spacing: var(--letter-spacing-wide);
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: var(--space-6);
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  width: 100%;
}

/* 信息面板（左侧） */
.info-panel {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.cover-card {
  padding: 0;
  overflow: hidden;
}

.cover-wrapper {
  padding: var(--space-4);
}

.cover-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-md);
  transition: var(--transition-base);
}

.cover-placeholder {
  width: 100%;
  height: 200px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  box-shadow: var(--shadow-md);
}

.cover-meta {
  padding: 0 var(--space-4) var(--space-4);
}

.meta-tags {
  margin-bottom: var(--space-4);
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--text-light);
}

.meta-row i {
  width: 16px;
  text-align: center;
  color: var(--primary-color);
}

/* 操作面板 */
.action-panel {
  padding: var(--space-4);
}

.panel-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-dark);
  margin: 0 0 var(--space-4) 0;
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-light);
}

.section-subtitle {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--text-secondary);
  margin: var(--space-4) 0 var(--space-3) 0;
}

.digest-filter-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.digest-filter-buttons .el-button {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.digest-filter-buttons .el-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.action-btn {
  width: 100%;
  justify-content: flex-start;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--border-radius-md);
  transition: var(--transition-base);
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.more-dropdown {
  width: 100%;
}

.more-dropdown :deep(.el-button) {
  width: 100%;
  justify-content: space-between;
}

/* 内容区域（右侧） */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-width: 0;
}

/* 批量操作栏 */
.batch-operation-bar {
  background: var(--bg-primary);
  border: 1px solid var(--success-color);
  border-radius: var(--border-radius-md);
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
  transition: border-color var(--transition-fast);
}

.batch-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--text-dark);
}

.batch-info i {
  font-size: 18px;
}

.batch-actions {
  display: flex;
  gap: var(--space-2);
}

/* 内容工具栏 */
.content-toolbar {
  padding: var(--space-4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex: 1;
  min-width: 300px;
}

.search-wrapper {
  flex: 1;
  max-width: 300px;
}

.filter-group {
  display: flex;
  gap: var(--space-2);
}

.filter-select {
  width: 140px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.view-toggle {
  display: flex;
  align-items: center;
}

.result-info {
  font-size: var(--font-size-sm);
  color: var(--text-light);
}

.total-count {
  font-weight: var(--font-weight-medium);
}

/* 收藏项区域 */
.items-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.items-loading {
  padding: var(--space-6);
  background: var(--bg-primary);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-4);
  background: var(--bg-primary);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
}

.empty-content {
  text-align: center;
  max-width: 400px;
}

.empty-icon {
  font-size: 64px;
  color: var(--text-light);
  margin-bottom: var(--space-4);
}

.empty-title {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-dark);
  margin: 0 0 var(--space-2) 0;
}

.empty-description {
  font-size: var(--font-size-base);
  color: var(--text-light);
  margin: 0 0 var(--space-5) 0;
  line-height: 1.5;
}

.items-container {
  display: grid;
  gap: var(--space-4);
  grid-auto-rows: max-content;
}

.items-container.view-grid {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.items-container.view-list {
  grid-template-columns: 1fr;
  gap: var(--space-2);
}

/* 批量选择样式 */
.batch-selection-wrapper {
  display: grid;
  gap: var(--space-4);
}

.view-grid .batch-selection-wrapper {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.view-list .batch-selection-wrapper {
  grid-template-columns: 1fr;
}

.item-wrapper {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.view-list .item-wrapper {
  align-items: center;
}

.item-checkbox {
  margin-top: var(--space-2);
  flex-shrink: 0;
}

.view-list .item-checkbox {
  margin-top: 0;
}

.item-checkbox :deep(.el-checkbox__input) {
  width: 18px;
  height: 18px;
}

.item-checkbox :deep(.el-checkbox__inner) {
  width: 18px;
  height: 18px;
  border-radius: 4px;
}

.item-checkbox :deep(.el-checkbox__inner::after) {
  height: 10px;
  left: 6px;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: var(--space-4) 0;
  background: var(--bg-primary);
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-light);
}

/* 评论区容器 */
.comment-section-wrapper {
  margin-top: var(--space-5);
  padding: var(--space-4);
}

/* 文字颜色工具类 */
.text-warning {
  color: var(--warning-color) !important;
}

/* 响应式设置 */
@media (max-width: 1200px) {
  .main-content {
    padding: var(--space-5);
  }
  
  .info-panel {
    width: 250px;
  }
  
  .items-container.view-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

@media (max-width: 992px) {
  .main-content {
    flex-direction: column;
    padding: var(--space-4);
  }
  
  .info-panel {
    width: 100%;
    flex-direction: row;
  }
  
  .cover-card {
    width: 250px;
    flex-shrink: 0;
  }
  
  .action-panel {
    flex: 1;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-4);
  }
  
  .header-stats {
    align-self: flex-start;
  }
  
  .toolbar-left {
    flex-wrap: wrap;
  }
  
  .search-wrapper {
    max-width: none;
    flex: 1;
  }
  
  .filter-group {
    width: 100%;
    margin-top: var(--space-2);
  }
  
  .filter-select {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: var(--space-5) 0;
  }
  
  .header-content {
    padding: 0 var(--space-4);
  }
  
  .page-title {
    font-size: var(--font-size-xxxl);
  }
  
  .header-stats {
    width: 100%;
    justify-content: space-around;
  }
  
  .stat-item {
    min-width: auto;
  }
  
  .main-content {
    padding: var(--space-3);
  }
  
  .info-panel {
    flex-direction: column;
  }
  
  .cover-card {
    width: 100%;
  }
  
  .cover-image,
  .cover-placeholder {
    height: 160px;
    font-size: 36px;
  }
  
  .items-container.view-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
  
  .batch-operation-bar {
    flex-direction: column;
    gap: var(--space-3);
    text-align: center;
  }
  
  .content-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .header-content {
    padding: 0 var(--space-3);
  }
  
  .page-title {
    font-size: var(--font-size-xxl);
  }
  
  .page-subtitle {
    font-size: var(--font-size-base);
  }
  
  .main-content {
    padding: var(--space-2);
  }
  
  .cover-image,
  .cover-placeholder {
    height: 140px;
    font-size: 28px;
  }
  
  .items-container.view-grid {
    grid-template-columns: 1fr;
  }
  
  .item-wrapper {
    gap: var(--space-2);
  }
  
  .item-checkbox {
    margin-top: var(--space-1);
  }
}

/* 关联笔记面板 */
.related-notes-panel {
  margin-bottom: var(--space-6);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-base);
}

.panel-header .panel-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.related-notes-content {
  min-height: 100px;
}

.empty-related {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-4);
  color: var(--text-secondary);
  text-align: center;
}

.empty-related i {
  font-size: 48px;
  color: var(--text-placeholder);
  margin-bottom: var(--space-3);
}

.empty-related p {
  font-size: var(--font-size-md);
  margin: 0 0 var(--space-2);
}

.empty-hint {
  font-size: var(--font-size-sm) !important;
  color: var(--text-secondary) !important;
}

.related-notes-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.related-note-item {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-canvas, #f5f7fa);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-base);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.related-note-item:hover {
  background: var(--bg-container, #ffffff);
  border-color: var(--primary-color, #409eff);
  box-shadow: var(--shadow-sm);
  transform: translateX(4px);
}

.note-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--success-bg, #f0f9ff);
  color: var(--success-color, #67c23a);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.note-content {
  flex: 1;
  min-width: 0;
}

.note-title {
  font-size: var(--font-size-md);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.note-excerpt {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.note-meta span {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.note-arrow {
  display: flex;
  align-items: center;
  color: var(--text-placeholder);
  transition: all var(--transition-normal);
}

.related-note-item:hover .note-arrow {
  color: var(--primary-color, #409eff);
  transform: translateX(4px);
}

.view-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3);
  margin-top: var(--space-3);
  background: var(--primary-bg, #ecf5ff);
  border-radius: var(--radius-md);
  color: var(--primary-color, #409eff);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: var(--font-size-sm);
}

.view-more:hover {
  background: var(--primary-color, #409eff);
  color: white;
}

.view-more i {
  transition: transform var(--transition-normal);
}

.view-more:hover i {
  transform: translateX(4px);
}
</style>
