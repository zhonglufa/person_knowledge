<template>
  <div class="content-header-wrapper" :class="{ 'without-action-button': !shouldShowAddCollectionButton }">
    <div class="content-header">
      <div class="header-main">
        <div class="header-left">
          <h2 class="content-title">{{ currentContentTitle }}</h2>
          <p v-if="currentContentSubtitle" class="content-subtitle">{{ currentContentSubtitle }}</p>
        </div>

        <div class="filter-section">
          <div class="filter-row">
            <div class="filter-group">
              <el-checkbox
                v-model="selectAll"
                :disabled="disableSelection"
                @change="handleSelectAllChange"
                :indeterminate="isIndeterminate"
              >
                全选
              </el-checkbox>

              <template v-if="selectedCount > 0">
                <el-button
                  v-if="activeSidebarItem !== 'recycle'"
                  size="small"
                  type="warning"
                  icon="el-icon-delete"
                  @click="handleBatchDelete"
                >
                  批量删除 ({{ selectedCount }})
                </el-button>

                <template v-if="activeSidebarItem === 'recycle'">
                  <el-button
                    size="small"
                    type="success"
                    icon="el-icon-refresh-left"
                    @click="handleBatchRecover"
                  >
                    批量恢复 ({{ selectedCount }})
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    icon="el-icon-delete"
                    @click="handleBatchPermanentDelete"
                  >
                    永久删除 ({{ selectedCount }})
                  </el-button>
                </template>
              </template>
            </div>

            <div v-if="shouldShowTypeFilter" class="filter-group type-filter-group">
              <span class="filter-label">类型筛选：</span>
              <el-select
                v-model="selectedType"
                placeholder="请选择类型"
                size="small"
                clearable
                @change="handleTypeChange"
                class="filter-select"
              >
                <el-option label="全部类型" value="0"></el-option>
                <el-option label="网页" value="1"></el-option>
                <el-option label="图片" value="2"></el-option>
                <el-option label="文本" value="3"></el-option>
                <el-option label="视频" value="4"></el-option>
              </el-select>
            </div>

            <div v-if="shouldShowDateFilter" class="filter-group date-filter-group">
              <span class="filter-label">时间筛选：</span>
              <el-date-picker
                v-model="selectedDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                size="small"
                value-format="yyyy-MM-dd"
                unlink-panels
                class="date-filter"
                @change="handleDateRangeChange"
              />
              <el-button
                v-if="hasSelectedDateRange"
                type="text"
                size="small"
                class="reset-date-btn"
                @click="handleResetDateRange"
              >
                清除时间
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="shouldShowAddCollectionButton" class="header-right">
        <el-button-group class="action-buttons">
          <el-button
            type="primary"
            size="small"
            icon="el-icon-plus"
            @click="handleAddCollection"
          >
            添加收藏
          </el-button>
        </el-button-group>
      </div>
    </div>

    <AddCollectionModal
      :visible.sync="showAddCollectionModalFlag"
      @add="handleAddCollectionSubmit"
    />
  </div>
</template>

<script>
import AddCollectionModal from './AddCollectionModal.vue';
import { collectApi } from '@/api/collect';

export default {
  name: 'ContentHeader',
  components: {
    AddCollectionModal,
  },
  props: {
    sidebarItems: {
      type: Array,
      default: () => []
    },
    activeSidebarItem: String,
    isMobile: Boolean,
    selectedCount: {
      type: Number,
      default: 0
    },
    filterParams: {
      type: Object,
      default: () => ({})
    },
    disableSelection: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showAddCollectionModalFlag: false,
      showImportModalFlag: false,
      importFormat: 'json',
      selectedType: '0',
      selectedDateRange: [],
      selectAll: false,
      isIndeterminate: false,
      exportLoading: false
    };
  },
  computed: {
    currentContentTitle() {
      const items = Array.isArray(this.sidebarItems) ? this.sidebarItems : [];
      const item = items.find(item => item.id === this.activeSidebarItem);
      return item ? item.text : '全部收藏';
    },
    currentContentSubtitle() {
      const items = Array.isArray(this.sidebarItems) ? this.sidebarItems : [];
      const item = items.find(item => item.id === this.activeSidebarItem);
      return item?.subtext || null;
    },
    // 添加收藏入口只在“全部收藏”场景展示，避免标签页、回收站等非主收藏流转场景出现错误操作入口。
    shouldShowAddCollectionButton() {
      return this.activeSidebarItem === 'all';
    },
    // “发现”展示的是收藏集而不是收藏项来源类型，因此该场景不展示类型筛选。
    shouldShowTypeFilter() {
      return this.activeSidebarItem !== 'discover';
    },
    shouldShowDateFilter() {
      return true;
    },
    hasSelectedDateRange() {
      return Array.isArray(this.selectedDateRange) && this.selectedDateRange.length === 2;
    }
  },
  methods: {
    handleAddCollection() {
      this.showAddCollectionModalFlag = true;
    },

    async handleAddCollectionSubmit(data) {
      try {
        await this.addCollection(data);
        this.showAddCollectionModalFlag = false;
      } catch {
      }
    },

    async addCollection(data) {
      if (!this.isValidUrl(data.url)) {
        this.$message.error('请输入有效的URL');
        return;
      }

      const checkResponse = await collectApi.checkUrlExists(data.url);
      if (checkResponse.data && checkResponse.data.exists) {
        this.$message.error('该链接已被收藏');
        return;
      }

      const response = await collectApi.createCollect({
        sourceType: 1,
        sourceUrl: data.url,
        title: data.title || undefined,
        source: new URL(data.url).hostname || '未知来源'
      });

      this.$message.success('收藏添加成功');
      this.$emit('add-collection-success', response.data);
    },

    isValidUrl(url) {
      try {
        new URL(url);
        return /^https?:\/\/.+/.test(url);
      } catch (e) {
        return false;
      }
    },

    handleTypeChange() {
      const filterParams = {
        ...this.filterParams,
        type: this.selectedType !== '0' ? parseInt(this.selectedType) : undefined
      };
      this.$emit('apply-filters', filterParams);
    },

    handleDateRangeChange() {
      const [startDate, endDate] = Array.isArray(this.selectedDateRange) ? this.selectedDateRange : [];
      const filterParams = {
        ...this.filterParams,
        startDate: startDate || null,
        endDate: endDate || null
      };
      this.$emit('apply-filters', filterParams);
    },

    handleResetDateRange() {
      this.selectedDateRange = [];
      this.handleDateRangeChange();
    },

    handleSelectAllChange(val) {
      this.isIndeterminate = false;
      this.$emit('select-all-change', val);
      this.$emit('selection-mode-change', val);
    },

    handleBatchDelete() {
      this.$emit('batch-delete');
    },

    handleBatchRecover() {
      this.$emit('batch-recover');
    },

    handleBatchPermanentDelete() {
      this.$emit('batch-permanent-delete');
    },

    syncFilterState() {
      this.selectedType = this.filterParams?.type !== undefined && this.filterParams?.type !== null
        ? String(this.filterParams.type)
        : '0';
      this.selectedDateRange = this.filterParams?.startDate && this.filterParams?.endDate
        ? [this.filterParams.startDate, this.filterParams.endDate]
        : [];
    }
  },

  watch: {
    activeSidebarItem() {
      this.selectAll = false;
      this.isIndeterminate = false;
      this.syncFilterState();
    },
    disableSelection(disabled) {
      if (disabled) {
        this.selectAll = false;
        this.isIndeterminate = false;
      }
    },
    selectedCount(newVal) {
      if (newVal === 0) {
        this.selectAll = false;
        this.isIndeterminate = false;
        this.$emit('selection-mode-change', false);
      }
    },
    filterParams: {
      handler() {
        this.syncFilterState();
      },
      deep: true,
      immediate: true
    }
  }
}
</script>

<style scoped>
.content-header-wrapper {
  background-color: var(--bg-primary);
  border-radius: var(--border-radius-lg) var(--border-radius-lg) 0 0;
  transition: var(--transition-base);
}

.content-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-lg);
}

.header-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.header-left {
  min-width: 0;
}

.content-title {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--text-dark);
}

.content-subtitle {
  margin: var(--spacing-xs) 0 0;
  font-size: var(--font-size-sm);
  color: var(--text-light);
}

.filter-section {
  min-width: 0;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-md);
}

.filter-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-sm);
}

.type-filter-group,
.date-filter-group {
  min-width: 0;
}

.filter-label {
  font-size: var(--font-size-sm);
  color: var(--text-medium);
  white-space: nowrap;
}

.filter-select {
  width: 160px;
}

.date-filter {
  width: 260px;
}

.reset-date-btn {
  padding: 0;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
}

.action-buttons {
  display: inline-flex;
}

/* 当“添加收藏”按钮隐藏时，头部内容仍保持左对齐，避免主内容区出现不必要的右侧留白。 */
.without-action-button .content-header {
  justify-content: flex-start;
}

@media (max-width: 768px) {
  .content-header {
    flex-direction: column;
  }

  .header-right {
    width: 100%;
  }

  .filter-select,
  .date-filter {
    width: 100%;
  }
}
</style>
