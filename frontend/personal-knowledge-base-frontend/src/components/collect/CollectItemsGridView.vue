<template>
  <div class="grid-view">
    <div
      v-for="collection in collections"
      :key="collection.id"
      :class="['collection-card', 'card', { selected: selectedCollections.includes(collection.id), 'selection-mode': showSelectionMode } ]"
      @click="$emit('collection-click', collection)"
      @contextmenu.prevent="handleContextMenu($event, collection)"
    >
      <div class="card-shell">
        <div class="card-header">
          <div class="card-title-group">
            <div class="card-type-badge">
              <i :class="getCollectionTypeIcon(getCollectionType(collection))"></i>
              <span>{{ getCollectionTypeName(getCollectionType(collection)) }}</span>
            </div>
            <h3 class="card-title">{{ collection.title || '未命名内容' }}</h3>
          </div>

          <div class="card-actions">
            <template v-if="activeSidebarItem !== 'recycle'">
              <el-button
                type="text"
                size="mini"
                :icon="isItemStarred(collection) ? 'el-icon-star-on' : 'el-icon-star-off'"
                :class="{ starred: isItemStarred(collection) }"
                @click.stop="$emit('toggle-star', collection)"
                title="星标收藏"
              />
              <el-button
                v-if="canDeleteCollection(collection) && !showSelectionMode"
                type="text"
                size="mini"
                icon="el-icon-delete"
                :disabled="processingRequest"
                :loading="processingRequest"
                @click.stop="$emit('delete-collection', collection)"
                title="删除"
              />
            </template>
            <template v-else>
              <el-button
                type="text"
                size="mini"
                icon="el-icon-check"
                class="recover-btn"
                @click.stop="$emit('recover-collection', collection)"
                title="恢复"
              />
              <el-button
                type="text"
                size="mini"
                icon="el-icon-delete"
                class="permanent-delete-btn"
                @click.stop="$emit('permanent-delete', collection)"
                title="永久删除"
              />
            </template>
          </div>
        </div>

        <div class="card-body">
            <div
              v-if="getCollectionType(collection) === 2 && getCollectionPreviewUrl(collection)"
              class="card-content image-card-content"
            >
              <div
                class="content-preview image-preview-container"
                @click.stop="$emit('preview-image', collection)"
              >
                <img
                  :src="getCollectionPreviewUrl(collection)"
                  :alt="collection.title"
                  class="preview-image"
                  :key="getCollectionPreviewUrl(collection)"
                  @error="handleImageError"
                />
                <div class="image-overlay">
                  <i class="fas fa-expand-arrows-alt"></i>
                </div>
              </div>

              <div class="card-footer">
                <div class="card-tags" v-if="getDisplayTags(collection).length > 0">
                  <el-tag
                    v-for="tag in getDisplayTags(collection).slice(0, 2)"
                    :key="getTagKey(tag)"
                    size="mini"
                    class="tag"
                    @click.stop="$emit('filter-by-tag', tag)"
                  >
                    # {{ getTagName(tag) }}
                  </el-tag>
                  <span
                    v-if="getDisplayTags(collection).length > 2"
                    class="more-tags"
                    @click.stop="$emit('show-all-collection-tags', collection)"
                  >
                    +{{ getDisplayTags(collection).length - 2 }}
                  </span>
                </div>

                <div class="card-location">
                  <span class="meta-label">位置：</span>
                  <span class="meta-value">{{ getLocationDisplay(collection) }}</span>
                </div>

                <div class="card-divider"></div>

                <div class="card-meta primary-meta">
                  <span class="meta-item">
                    <i class="fas fa-clock"></i>
                    {{ formatTime(collection.createdAt || collection.createTime) }}
                  </span>
                  <span class="meta-item" v-if="collection.visitCount > 0">
                    <i class="fas fa-eye"></i>
                    {{ collection.visitCount }}
                  </span>
                </div>

                <div class="card-meta secondary-meta">
                  <span class="source-item">
                    <i :class="getSourceIcon(collection)"></i>
                    {{ getSourceDisplay(collection) }}
                  </span>
                  <span class="source-item shared-item" v-if="collection.isShared || collection.is_shared">
                    <i class="fas fa-user"></i>
                    共享
                  </span>
                </div>
              </div>
            </div>

            <div v-else class="card-content standard-card-content">
              <div
                v-if="getCollectionType(collection) === 3"
                class="content-preview text-preview"
              >
                <p class="preview-text">{{ getTextPreview(collection) }}</p>
              </div>

              <div
                v-else-if="getCollectionType(collection) === 1"
                class="content-preview link-preview"
              >
                <div class="link-icon">
                  <i class="fas fa-link"></i>
                </div>
                <div class="link-info">
                  <p class="link-title">{{ collection.title || '未命名链接' }}</p>
                  <p class="link-url">{{ truncateText(getCollectionPreviewUrl(collection), 64) }}</p>
                  <p class="link-source">{{ getSourceDisplay(collection) }}</p>
                </div>
              </div>

              <div
                v-else-if="getCollectionType(collection) === 4"
                class="content-preview video-preview"
              >
                <template v-if="getCollectionPreviewUrl(collection)">
                  <video
                    v-if="isValidVideoUrl(getCollectionPreviewUrl(collection))"
                    :src="getCollectionPreviewUrl(collection)"
                    class="preview-video"
                    :alt="collection.title"
                    controls
                    preload="metadata"
                    @click.stop
                    @error="handleVideoError($event, collection)"
                    @loadstart="handleVideoLoadStart($event, collection)"
                    @canplay="handleVideoCanPlay($event, collection)"
                  />
                  <div v-else class="invalid-video-placeholder">
                    <i class="fas fa-video-slash"></i>
                    <span class="video-info">无效视频链接</span>
                  </div>
                </template>
                <div v-else class="no-video-placeholder">
                  <i class="fas fa-exclamation-triangle"></i>
                  <span class="video-info">无视频链接</span>
                </div>
              </div>

              <div v-else class="content-preview empty-preview">
                <i class="fas fa-file"></i>
                <span>暂无预览内容</span>
              </div>

              <div class="card-footer">
                <div class="card-tags" v-if="getDisplayTags(collection).length > 0">
                  <el-tag
                    v-for="tag in getDisplayTags(collection).slice(0, 2)"
                    :key="getTagKey(tag)"
                    size="mini"
                    class="tag"
                    @click.stop="$emit('filter-by-tag', tag)"
                  >
                    # {{ getTagName(tag) }}
                  </el-tag>
                  <span
                    v-if="getDisplayTags(collection).length > 2"
                    class="more-tags"
                    @click.stop="$emit('show-all-collection-tags', collection)"
                  >
                    +{{ getDisplayTags(collection).length - 2 }}
                  </span>
                </div>

                <div class="card-location">
                  <span class="meta-label">位置：</span>
                  <span class="meta-value">{{ getLocationDisplay(collection) }}</span>
                </div>

                <div class="card-divider"></div>

                <div class="card-meta primary-meta">
                  <span class="meta-item">
                    <i class="fas fa-clock"></i>
                    {{ formatTime(collection.createdAt || collection.createTime) }}
                  </span>
                  <span class="meta-item" v-if="collection.visitCount > 0">
                    <i class="fas fa-eye"></i>
                    {{ collection.visitCount }}
                  </span>
                </div>

                <div class="card-meta secondary-meta">
                  <span class="source-item">
                    <i :class="getSourceIcon(collection)"></i>
                    {{ getSourceDisplay(collection) }}
                  </span>
                  <span class="source-item shared-item" v-if="collection.isShared || collection.is_shared">
                    <i class="fas fa-user"></i>
                    共享
                  </span>
                </div>
              </div>
            </div>
          </div>
      </div>

      <div
        v-if="showSelectionMode"
        class="selection-indicator-wrap"
      >
        <div
          class="selection-indicator"
          :class="{ selected: selectedCollections.includes(collection.id) }"
          @click.stop="handleToggleSelection(collection.id)"
          title="点击切换选中状态"
        >
          <i :class="selectedCollections.includes(collection.id) ? 'fas fa-check-circle' : 'far fa-circle'"></i>
        </div>
      </div>
    </div>

    <div
      v-show="contextMenuVisible"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      class="context-menu"
      @click.stop
    >
      <div class="context-menu-item" @click="handleMenuCommand('move')">
        <i class="el-icon-folder-opened"></i>
        <span>移动到...</span>
      </div>
      <div class="context-menu-item" @click="handleMenuCommand('star')">
        <i :class="isItemStarred(contextMenuItem) ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
        <span>{{ isItemStarred(contextMenuItem) ? '取消星标' : '星标收藏' }}</span>
      </div>
      <div v-if="activeSidebarItem !== 'recycle'" class="context-menu-divider"></div>
      <div v-if="activeSidebarItem !== 'recycle' && canDeleteCollection(contextMenuItem)" class="context-menu-item danger" @click="handleMenuCommand('delete')">
        <i class="el-icon-delete"></i>
        <span>删除</span>
      </div>
      <template v-if="activeSidebarItem === 'recycle'">
        <div class="context-menu-divider"></div>
        <div class="context-menu-item" @click="handleMenuCommand('recover')">
          <i class="el-icon-refresh-left"></i>
          <span>恢复</span>
        </div>
        <div class="context-menu-item danger" @click="handleMenuCommand('permanent-delete')">
          <i class="el-icon-delete"></i>
          <span>永久删除</span>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CollectItemsGridView',
  props: {
    collections: {
      type: Array,
      required: true
    },
    selectedCollections: {
      type: Array,
      default: () => []
    },
    activeSidebarItem: {
      type: String,
      default: ''
    },
    currentUserId: {
      type: Number,
      default: null
    },
    userCollectedUrls: {
      type: Set,
      default: () => new Set()
    },
    showSelectionMode: {
      type: Boolean,
      default: false
    },
    processingRequest: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      contextMenuVisible: false,
      contextMenuX: 0,
      contextMenuY: 0,
      contextMenuItem: null
    };
  },
  emits: [
    'collection-click',
    'show-context-menu',
    'toggle-star',
    'delete-collection',
    'recover-collection',
    'permanent-delete',
    'preview-image',
    'filter-by-tag',
    'show-all-collection-tags',
    'card-menu',
    'toggle-selection',
    'move-item'
  ],
  mounted() {
    document.addEventListener('click', this.closeContextMenu);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.closeContextMenu);
  },
  methods: {
    handleContextMenu(event, collection) {
      event.preventDefault();
      this.contextMenuItem = collection;
      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;
      this.contextMenuVisible = true;
    },
    closeContextMenu() {
      this.contextMenuVisible = false;
    },
    handleMenuCommand(command) {
      this.closeContextMenu();
      
      switch (command) {
        case 'move':
          this.$emit('move-item', this.contextMenuItem);
          break;
        case 'star':
          this.$emit('toggle-star', this.contextMenuItem);
          break;
        case 'delete':
          this.$emit('delete-collection', this.contextMenuItem);
          break;
        case 'recover':
          this.$emit('recover-collection', this.contextMenuItem);
          break;
        case 'permanent-delete':
          this.$emit('permanent-delete', this.contextMenuItem);
          break;
      }
    },
    handleToggleSelection(collectionId) {
      this.$emit('toggle-selection', collectionId);
    },
    getDisplayTags(collection) {
      if (!collection) return [];

      const tags = collection.tags;
      if (!tags) return [];

      let tagsArray = [];

      if (Array.isArray(tags) && tags.length > 0) {
        if (tags[0] && typeof tags[0] === 'object' && tags[0].id && tags[0].name) {
          tagsArray = tags;
        } else {
          tagsArray = tags.filter(tag => tag);
        }
      } else if (typeof tags === 'string') {
        tagsArray = tags
          .split(',')
          .map(tag => tag.trim())
          .filter(tag => tag);
      } else {
        return [];
      }

      return this.deduplicateTags(tagsArray);
    },

    deduplicateTags(tagsArray) {
      if (!Array.isArray(tagsArray)) return [];

      const seen = new Set();
      const deduplicated = [];

      for (const tag of tagsArray) {
        if (!tag) continue;

        let identifier;
        if (typeof tag === 'object' && tag !== null) {
          identifier = (tag.id != null ? tag.id : (tag.name != null ? tag.name : String(tag)));
        } else {
          identifier = String(tag);
        }

        const normalizedIdentifier = String(identifier || '').toLowerCase();

        if (!seen.has(normalizedIdentifier)) {
          seen.add(normalizedIdentifier);
          deduplicated.push(tag);
        }
      }

      return deduplicated;
    },

    getTagName(tag) {
      if (!tag) return '';

      if (typeof tag === 'object') {
        if (tag.name) {
          return tag.name;
        }
        return tag.title || tag.tagName || String(tag);
      }

      return String(tag);
    },

    getTagKey(tag) {
      if (!tag) return Math.random();

      if (typeof tag === 'object') {
        if (tag.id) {
          return tag.id;
        }
        return tag.name || JSON.stringify(tag);
      }

      return tag;
    },

    canDeleteCollection(collection) {
      if (!collection) return false;
      return collection.userId === this.currentUserId;
    },

    isStarred(collection) {
      if (collection.userId == this.currentUserId) {
        return true;
      }

      if (collection.url && this.userCollectedUrls && this.userCollectedUrls.has(collection.url)) {
        return true;
      }

      return false;
    },
    isItemStarred(collection) {
      if (!collection) {
        return false;
      }
      if (typeof collection.isStar === 'boolean') {
        return collection.isStar;
      }
      if (typeof collection.isStarred === 'boolean') {
        return collection.isStarred;
      }
      if (typeof collection.starred === 'boolean') {
        return collection.starred;
      }
      return false;
    },

    getCollectionType(collection) {
      if (!collection) return 1;
      const typeValue = collection.sourceType || collection.type;
      if (typeValue !== undefined && typeValue !== null) {
        const numericType = parseInt(typeValue);
        return isNaN(numericType) ? 1 : numericType;
      }
      return 1;
    },

    getCollectionTypeIcon(type) {
      const icons = {
        1: 'fas fa-link',
        2: 'fas fa-image',
        3: 'fas fa-file-alt',
        4: 'fas fa-video',
        5: 'fas fa-book-open'
      };
      return icons[type] || 'fas fa-question-circle';
    },

    getCollectionTypeName(type) {
      const names = {
        1: '网页',
        2: '图片',
        3: '文本',
        4: '视频',
        5: '笔记'
      };
      return names[type] || '未知';
    },

    truncateText(text, length) {
      if (!text) return '';
      return text.length > length ? text.substring(0, length) + '...' : text;
    },

    formatTime(timeString) {
      if (!timeString) {
        return '';
      }

      const time = new Date(timeString);
      if (isNaN(time.getTime())) {
        return '';
      }

      const now = new Date();
      const diff = now - time;

      if (time.toDateString() === now.toDateString()) {
        return `今天 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`;
      }

      const yesterday = new Date(now);
      yesterday.setDate(yesterday.getDate() - 1);
      if (time.toDateString() === yesterday.toDateString()) {
        return '昨天';
      }

      if (diff < 3 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000));
        return `${days}天前`;
      }

      return `${time.getFullYear()}-${(time.getMonth() + 1).toString().padStart(2, '0')}-${time.getDate().toString().padStart(2, '0')}`;
    },

    getCollectionPreviewUrl(collection) {
      return collection?.sourceUrl || collection?.url || '';
    },

    getTextPreview(collection) {
      const text = collection?.content || collection?.summary || collection?.description || collection?.coreAbstract || collection?.title || '';
      return this.truncateText(text, this.getCollectionType(collection) === 5 ? 180 : 240);
    },

    getSourceDisplay(collection) {
      if (this.getCollectionType(collection) === 5) {
        if (collection?.noteSourceLabel && String(collection.noteSourceLabel).trim()) {
          return String(collection.noteSourceLabel).trim();
        }

        if (collection?.noteId || collection?.relatedNoteId) {
          return '收藏沉淀';
        }

        return '我的笔记';
      }

      const source = collection?.source || collection?.sourceName;
      if (source && String(source).trim()) {
        return String(source).trim();
      }

      const previewUrl = this.getCollectionPreviewUrl(collection);
      if (previewUrl && this.getCollectionType(collection) === 1) {
        try {
          return new URL(previewUrl).hostname.replace(/^www\./, '');
        } catch (error) {
          return '未知';
        }
      }

      return '未知';
    },

    getSourceIcon(collection) {
      if (this.getCollectionType(collection) === 1) {
        return 'fas fa-globe';
      }
      if (this.getCollectionType(collection) === 2) {
        return 'far fa-image';
      }
      if (this.getCollectionType(collection) === 4) {
        return 'fas fa-play-circle';
      }
      if (this.getCollectionType(collection) === 5) {
        return 'fas fa-book-open';
      }
      return 'far fa-bookmark';
    },

    getLocationDisplay(collection) {
      const directLocation = collection?.collectionName || collection?.collectionTitle || collection?.folderName || collection?.categoryName || collection?.category?.name;
      if (directLocation && String(directLocation).trim()) {
        return String(directLocation).trim();
      }

      const locationMap = {
        all: '我的收藏',
        recycle: '回收站',
        star: '星标收藏',
        starred: '星标收藏',
        recent: '最近收藏',
        unread: '未读内容',
        tags: '标签筛选',
        'my-collections': '我的收藏集'
      };

      return locationMap[this.activeSidebarItem] || '我的收藏';
    },

    handleImageError(event) {
      console.warn(`图像加载失败: ${event.target.src}`);
      event.target.src = 'data:image/svg+xml,%3Csvg%20xmlns=%22http://www.w3.org/2000/svg%22%20width=%22100%22%20height=%22100%22%3E%3Crect%20fill=%22%23f0f0f0%22%20width=%22100%22%20height=%22100%22/%3E%3Ccircle%20cx=%2250%22%20cy=%2240%22%20r=%2215%22%20fill=%22%23ccc%22/%3E%3Cpolygon%20points=%2220,100%2040,60%2060,80%2080,40%20100,100%22%20fill=%22%23ccc%22/%3E%3C/svg%3E';
      event.target.classList.add('image-error');
    },

    isValidVideoUrl(url) {
      if (!url) return false;

      const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.wmv', '.flv', '.mkv'];
      const lowerUrl = url.toLowerCase();
      const hasVideoExtension = videoExtensions.some(ext => lowerUrl.includes(ext));

      const videoDomains = [
        'youtube.com',
        'youtu.be',
        'bilibili.com',
        'vimeo.com',
        'youku.com',
        'tudou.com',
        'iqiyi.com',
        'v.qq.com',
        'mgtv.com',
        'sohu.com',
        '163.com'
      ];

      const hasVideoDomain = videoDomains.some(domain => lowerUrl.includes(domain));

      return hasVideoDomain || hasVideoExtension;
    },

    handleVideoError(event, collection) {
      console.warn(`视频加载失败: ${event.target.src}`);
      this.$set(collection, 'videoLoadFailed', true);
    },

    handleVideoLoadStart(event, collection) {
      console.log(`视频开始加载: ${event.target.src}`);
      this.$set(collection, 'videoLoading', true);
    },

    handleVideoCanPlay(event, collection) {
      console.log(`视频可以播放: ${event.target.src}`);
      this.$set(collection, 'videoLoading', false);
      this.$set(collection, 'videoLoaded', true);
    }
  }
};
</script>

<style scoped>
.grid-view {
  display: grid;
  grid-template-columns: repeat(auto-fill, 350px);
  justify-content: center;
  justify-items: center;
  align-items: stretch;
  gap: 24px 28px;
  width: 100%;
  margin: 0 auto;
  margin-bottom: 0;
  overflow: visible;
  padding: 8px 0 0;
  min-height: 0;
  min-width: 0;
}

.collection-card {
  position: relative;
  cursor: pointer;
  overflow: visible;
  display: flex;
  flex-direction: column;
  align-self: stretch;
  width: 350px;
  min-width: 350px;
  min-height: 0;
  color: var(--text-dark);
  background: transparent;
  border: none;
  box-shadow: none;
}

.card-shell {
  display: flex;
  flex-direction: column;
  width: 350px;
  max-width: 350px;
  height: 100%;
  min-height: 100%;
  background: #ffffff;
  border: 1px solid rgba(99, 102, 241, 0.14);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.collection-card:hover .card-shell {
  transform: translateY(-4px);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.1);
  border-color: rgba(74, 108, 247, 0.3);
}

.collection-card.selected .card-shell {
  border-color: #7c8cff;
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.12), 0 20px 40px rgba(15, 23, 42, 0.12);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 16px 10px;
}

.card-title-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.card-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  max-width: 100%;
  padding: 4px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #5b6df6;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.card-title {
  margin: 0;
  font-size: 16px;
  line-height: 1.5;
  font-weight: 700;
  color: #1f2937;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: calc(1.5em * 2);
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transform: translateY(-4px);
  transition: opacity 0.2s ease, transform 0.2s ease;
  flex-shrink: 0;
}

.collection-card:hover .card-actions,
.collection-card.selected .card-actions,
.collection-card:focus-within .card-actions,
.collection-card.selection-mode .card-actions {
  opacity: 1;
  transform: translateY(0);
}

.card-actions :deep(.el-button) {
  width: 28px;
  height: 28px;
  padding: 0;
  margin: 0;
  border-radius: 50%;
  color: #6b7280 !important;
  background: rgba(15, 23, 42, 0.04);
  transition: all 0.2s ease;
}

.card-actions :deep(.el-button:hover) {
  color: var(--primary-color) !important;
  background: rgba(74, 108, 247, 0.12);
}

.card-content {
  flex: 1;
  display: flex;
  padding: 0 16px;
}

.standard-card-content {
  flex-direction: column;
}

.image-card-content {
  flex-direction: column;
  padding: 0;
}

.content-preview {
  width: 100%;
  height: 210px;
  border-radius: 18px;
  overflow: hidden;
}

.image-preview-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  height: 240px;
  border-radius: 22px 22px 0 0;
  background: linear-gradient(180deg, #edf4ff 0%, #dcecff 100%);
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.04) 0%, rgba(15, 23, 42, 0.42) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 24px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-preview-container:hover .image-overlay {
  opacity: 1;
}

.preview-image,
.preview-video {
  width: 100%;
  height: 100%;
  display: block;
}

.preview-image {
  object-fit: cover;
}

.preview-video {
  object-fit: contain;
  background: #000000;
}

.preview-image.image-error {
  opacity: 0.6;
}

.text-preview {
  padding: 18px 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.preview-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: #344054;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  -webkit-box-orient: vertical;
  white-space: pre-wrap;
}

.link-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  border: 1px solid rgba(74, 108, 247, 0.08);
}

.link-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5b6df6;
  background: #ffffff;
  box-shadow: 0 8px 18px rgba(74, 108, 247, 0.12);
  font-size: 18px;
  flex-shrink: 0;
}

.link-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.link-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.link-url,
.link-source {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: #667085;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #111827;
}

.invalid-video-placeholder,
.no-video-placeholder,
.empty-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  text-align: center;
  color: #667085;
  background: linear-gradient(180deg, #f8fafc 0%, #f2f4f7 100%);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.invalid-video-placeholder i,
.no-video-placeholder i,
.empty-preview i {
  font-size: 24px;
  color: var(--danger-color);
}

.video-info {
  font-size: 14px;
  color: #475467;
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px 16px;
  margin-top: auto;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 24px;
}

.tag {
  cursor: pointer;
  background-color: #ffffff !important;
  border-color: rgba(91, 109, 246, 0.16) !important;
  color: #5b6df6 !important;
  font-size: 12px;
  border-radius: 999px;
  padding: 0 2px;
}

.tag:hover {
  background-color: rgba(74, 108, 247, 0.08) !important;
  border-color: #5b6df6 !important;
}

.tag :deep(.el-tag__close) {
  color: #5b6df6 !important;
}

.more-tags {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 24px;
  font-size: 12px;
  color: #5b6df6;
  cursor: pointer;
  padding: 0 10px;
  border-radius: 999px;
  background-color: rgba(91, 109, 246, 0.08);
  font-weight: 600;
  transition: all 0.2s ease;
}

.more-tags:hover {
  color: #ffffff;
  background-color: #5b6df6;
}

.card-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #475467;
}

.meta-label {
  color: #667085;
}

.meta-value {
  color: #344054;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-divider {
  width: 100%;
  height: 1px;
  background: rgba(15, 23, 42, 0.06);
}

.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.primary-meta,
.secondary-meta {
  gap: 8px;
}

.meta-item,
.source-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 5px 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #667085;
  font-size: 12px;
  line-height: 1;
}

.meta-item i,
.source-item i {
  font-size: 12px;
}

.source-item {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shared-item {
  background: rgba(91, 109, 246, 0.08);
  color: #5b6df6;
}

.selection-indicator-wrap {
  position: absolute;
  top: 12px;
  left: 12px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 12;
  pointer-events: none;
}

.selection-indicator {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
  border: 2px solid rgba(91, 109, 246, 0.18);
  cursor: pointer;
  transition: all 0.2s ease;
  pointer-events: auto;
}

.selection-indicator:not(.selected) {
  opacity: 0.9;
}

.selection-indicator:not(.selected):hover {
  opacity: 1;
  border-color: #5b6df6;
  transform: scale(1.08);
}

.selection-indicator:not(.selected) i {
  color: #94a3b8;
  font-size: 13px;
}

.selection-indicator.selected {
  background-color: #5b6df6;
  opacity: 1;
  border-color: transparent;
}

.selection-indicator.selected i {
  color: #ffffff;
  font-size: 14px;
}

.selection-indicator.selected:hover {
  transform: scale(1.08);
  box-shadow: 0 10px 24px rgba(74, 108, 247, 0.28);
}

.selection-indicator:active {
  transform: scale(0.95);
}

.starred {
  color: var(--warning-color) !important;
}

.recover-btn {
  color: var(--success-color) !important;
}

.recover-btn:hover {
  color: #67c23a !important;
}

.permanent-delete-btn {
  color: var(--danger-color) !important;
}

.permanent-delete-btn:hover {
  color: #ff7875 !important;
}

@media (prefers-color-scheme: dark) {
  .card-shell {
    background: #ffffff !important;
  }

  .card-title,
  .preview-text,
  .meta-value,
  .link-title {
    color: #101828 !important;
  }

  .link-url,
  .link-source,
  .meta-item,
  .source-item,
  .card-location,
  .video-info {
    color: #667085 !important;
  }
}

@media (max-width: 1200px) {
  .grid-view {
    grid-template-columns: repeat(auto-fill, 350px);
    gap: 24px;
  }
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 4px 0;
  min-width: 160px;
}

.context-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
  color: #333;
}

.context-menu-item:hover {
  background-color: #f5f7fa;
}

.context-menu-item.danger {
  color: #f56c6c;
}

.context-menu-item.danger:hover {
  background-color: #fef0f0;
}

.context-menu-item i {
  font-size: 16px;
}

.context-menu-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 4px 0;
}
</style>
