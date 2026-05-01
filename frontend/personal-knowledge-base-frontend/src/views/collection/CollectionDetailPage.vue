<template>
  <div class="collection-detail-page">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="error" class="error-container">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="loadPageData">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="collection" class="detail-wrapper">
      <div class="page-header card">
        <div class="header-top-row">
          <el-button class="back-button" type="text" icon="el-icon-arrow-left" @click="goBack">
            返回
          </el-button>
          <div class="header-actions" v-if="isOwner">
            <el-button size="small" @click="goToCollectionsList">返回收藏集列表</el-button>
            <el-button type="primary" size="small" @click="showAddItemDialog = true">添加收藏项</el-button>
            <el-button type="primary" size="small" @click="openEditDialog">编辑收藏集</el-button>
            <el-button type="danger" size="small" @click="deleteCollection">删除收藏集</el-button>
          </div>
        </div>

        <div class="header-main">
          <div class="cover-section">
            <img
              v-if="coverImage"
              :src="coverImage"
              :alt="collection.name || '收藏集封面'"
              class="cover-image"
            >
            <div v-else class="cover-placeholder">
              <i class="fas fa-layer-group"></i>
            </div>
          </div>

          <div class="info-section">
            <div class="title-row">
              <h1 class="page-title">{{ collection.name || '未命名收藏集' }}</h1>
              <el-tag :type="isPublic ? 'success' : 'info'" size="small">
                {{ isPublic ? '公开' : '私有' }}
              </el-tag>
            </div>

            <p class="page-description">{{ collection.description || '暂无描述' }}</p>

            <div class="meta-grid">
              <div class="meta-item">
                <span class="meta-label">创建时间</span>
                <span class="meta-value">{{ formatDate(collection.createdAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">更新时间</span>
                <span class="meta-value">{{ formatDate(collection.updatedAt || collection.createdAt) }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">收藏项数量</span>
                <span class="meta-value">{{ totalItems }}</span>
              </div>
              <div class="meta-item">
                <span class="meta-label">状态</span>
                <span class="meta-value">{{ isPublic ? '对外可浏览' : '仅自己可见' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="items-section card">
        <div class="section-header">
          <div>
            <h2 class="section-title">收藏项列表</h2>
            <p class="section-subtitle">浏览该收藏集下的内容项，点击后进入收藏项工作区继续加工整理。</p>
          </div>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏项标题或内容"
            prefix-icon="el-icon-search"
            clearable
            class="items-search"
            @input="handleSearch"
          />
        </div>

        <div v-if="itemsLoading" class="items-loading">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="filteredItems.length === 0" class="empty-items">
          <i class="fas fa-file-alt"></i>
          <p>当前收藏集暂无可展示的收藏项</p>
        </div>

        <div v-else class="items-list">
          <div
            v-for="item in paginatedItems"
            :key="item.id"
            class="item-card"
            @click="goToItemWorkspace(item)"
          >
            <div class="item-main">
              <h3 class="item-title">{{ item.title || '未命名收藏项' }}</h3>
              <p class="item-summary">{{ getItemSummary(item) }}</p>
            </div>
            <div class="item-side">
              <el-tag size="mini" :type="getSourceTagType(item.sourceType)">
                {{ getSourceTypeText(item.sourceType) }}
              </el-tag>
              <span class="item-date">{{ formatDate(item.createdAt || item.createTime) }}</span>
            </div>
          </div>
        </div>

        <div class="items-pagination" v-if="filteredItems.length > itemsPageSize">
          <el-pagination
            background
            layout="prev, pager, next, sizes"
            :total="filteredItems.length"
            :page-size.sync="itemsPageSize"
            :current-page.sync="itemsCurrentPage"
            :page-sizes="[10, 20, 50]"
          />
        </div>
      </div>

      <collection-edit-dialog
        :visible.sync="showEditDialog"
        :collection="collection"
        @success="handleCollectionUpdated"
      />

      <el-dialog title="添加收藏项" :visible.sync="showAddItemDialog" width="560px" @open="resetAddItemForm">
        <el-form :model="addItemForm" label-width="100px" ref="addItemFormRef" :rules="addItemRules">
          <el-form-item label="标题" prop="title">
            <el-input v-model="addItemForm.title" placeholder="请输入收藏项标题" />
          </el-form-item>
          <el-form-item label="来源类型" prop="sourceType">
            <el-select v-model="addItemForm.sourceType" style="width:100%">
              <el-option label="网页" :value="1"></el-option>
              <el-option label="图片" :value="2"></el-option>
              <el-option label="文本" :value="3"></el-option>
              <el-option label="视频" :value="4"></el-option>
              <el-option label="笔记" :value="5"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="来源URL">
            <el-input v-model="addItemForm.sourceUrl" placeholder="https://..." />
          </el-form-item>
          <el-form-item label="来源名称">
            <el-input v-model="addItemForm.source" placeholder="来源网站或作者" />
          </el-form-item>
          <el-form-item label="核心摘要">
            <el-input v-model="addItemForm.coreAbstract" type="textarea" :rows="3" placeholder="简要概括内容" />
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="addItemForm.keywords" placeholder="多个关键词用逗号分隔" />
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="showAddItemDialog = false">取消</el-button>
          <el-button type="primary" :loading="addItemLoading" @click="submitAddItem">确认添加</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { collectionsApi } from '@/api/collections'
import { collectApi } from '@/api/collect'
import CollectionEditDialog from '@/components/collect/CollectionEditDialog.vue'

export default {
  name: 'CollectionDetailPage',
  components: {
    CollectionEditDialog
  },
  data() {
    return {
      loading: true,
      itemsLoading: false,
      error: '',
      collection: null,
      collectionItems: [],
      searchKeyword: '',
      showEditDialog: false,
      showAddItemDialog: false,
      itemsCurrentPage: 1,
      itemsPageSize: 10,
      addItemLoading: false,
      addItemForm: {
        title: '',
        sourceType: 1,
        sourceUrl: '',
        source: '',
        coreAbstract: '',
        keywords: ''
      },
      addItemRules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
        sourceType: [{ required: true, message: '请选择来源类型', trigger: 'change' }]
      }
    }
  },
  computed: {
    ...mapState('user', ['userInfo', 'token']),
    coverImage() {
      return this.collection?.coverImage || this.collection?.coverUrl || ''
    },
    isPublic() {
      return this.collection?.isPublic || this.collection?.isShared || false
    },
    isOwner() {
      return !!this.userInfo?.id && !!this.collection?.userId && this.userInfo.id === this.collection.userId
    },
    filteredItems() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      if (!keyword) {
        return this.collectionItems
      }
      return this.collectionItems.filter(item =>
        String(item?.title || '').toLowerCase().includes(keyword) ||
        String(item?.content || '').toLowerCase().includes(keyword) ||
        String(item?.coreAbstract || '').toLowerCase().includes(keyword)
      )
    },
    totalItems() {
      return this.collection?.itemCount || this.collectionItems.length || 0
    },
    paginatedItems() {
      const start = (this.itemsCurrentPage - 1) * this.itemsPageSize
      return this.filteredItems.slice(start, start + this.itemsPageSize)
    }
  },
  async created() {
    await this.loadPageData()
  },
  watch: {
    '$route.params.id': {
      async handler() {
        await this.loadPageData()
      }
    }
  },
  methods: {
    async loadPageData() {
      this.loading = true
      this.error = ''
      try {
        const response = await collectionsApi.getCollectionDetail(this.$route.params.id)
        const payload = response?.data || {}
        this.collection = payload.data || payload
        await this.loadCollectionItems()
      } catch (error) {
        console.error('加载收藏集详情失败', error)
        this.error = '收藏集详情加载失败'
      } finally {
        this.loading = false
      }
    },
    async loadCollectionItems() {
      if (!this.collection?.id) {
        this.collectionItems = []
        return
      }
      this.itemsLoading = true
      try {
        const response = await collectionsApi.getCollectionItems(this.collection.id, 1, 100)
        const payload = response?.data || {}
        const data = payload.data || payload
        this.collectionItems = Array.isArray(data.records) ? data.records : (Array.isArray(data) ? data : [])
      } catch (error) {
        console.error('加载收藏项失败', error)
        this.collectionItems = []
      } finally {
        this.itemsLoading = false
      }
    },
    handleSearch() {},
    goBack() {
      this.goToCollectionsList()
    },
    goToCollectionsList() {
      this.$router.push('/collections')
    },
    goToItemWorkspace(item) {
      if (!item?.id) {
        return
      }
      this.$router.push(`/creation/collection-items/${item.id}/note/create`)
    },
    openEditDialog() {
      this.showEditDialog = true
    },
    handleCollectionUpdated() {
      this.showEditDialog = false
      this.loadPageData()
    },
    resetAddItemForm() {
      this.addItemForm = {
        title: '',
        sourceType: 1,
        sourceUrl: '',
        source: '',
        coreAbstract: '',
        keywords: ''
      }
    },
    async submitAddItem() {
      try {
        await this.$refs.addItemFormRef.validate()
      } catch {
        return
      }
      this.addItemLoading = true
      try {
        await collectApi.createCollect({
          ...this.addItemForm,
          collectionId: this.collection.id
        })
        this.$message.success('收藏项添加成功')
        this.showAddItemDialog = false
        await this.loadCollectionItems()
      } catch (error) {
        console.error('添加收藏项失败:', error)
        this.$message.error(error?.response?.data?.message || '添加收藏项失败')
      } finally {
        this.addItemLoading = false
      }
    },
    deleteCollection() {
      this.$confirm('确定要删除该收藏集？收藏集内的收藏项将被移至回收站。', '删除收藏集', {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await collectionsApi.deleteCollection(this.collection.id)
          this.$message.success('收藏集已删除')
          this.$router.push('/collections')
        } catch (error) {
          console.error('删除收藏集失败:', error)
          this.$message.error('删除收藏集失败')
        }
      }).catch(() => {})
    },
    formatDate(date) {
      if (!date) return '未知'
      const parsed = new Date(date)
      return Number.isNaN(parsed.getTime()) ? '未知' : parsed.toLocaleDateString('zh-CN')
    },
    getItemSummary(item) {
      return item?.coreAbstract || item?.content || '暂无摘要'
    },
    getSourceTypeText(sourceType) {
      const sourceMap = {
        1: '网页',
        2: '图片',
        3: '文本',
        4: '视频'
      }
      return sourceMap[sourceType] || '内容'
    },
    getSourceTagType(sourceType) {
      const tagMap = {
        1: 'primary',
        2: 'success',
        3: 'info',
        4: 'warning'
      }
      return tagMap[sourceType] || ''
    }
  }
}
</script>

<style scoped>
.collection-detail-page {
  padding: 24px;
}

.items-pagination {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}
</style>
