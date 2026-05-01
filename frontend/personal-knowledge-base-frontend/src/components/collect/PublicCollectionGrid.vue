<template>
  <div class="public-collection-grid">
    <div
      v-for="collection in collections"
      :key="collection.id"
      class="public-collection-card"
      @click="$emit('collection-click', collection)"
    >
      <div class="card-shell">
        <div class="card-cover">
          <img
            v-if="getCoverImage(collection)"
            :src="getCoverImage(collection)"
            :alt="getCollectionTitle(collection)"
            class="cover-image"
            @error="handleImageError"
          />
          <div v-else class="cover-placeholder">
            <i class="fas fa-layer-group"></i>
          </div>
        </div>

        <div class="card-body">
          <div class="card-title-row">
            <h3 class="card-title">{{ getCollectionTitle(collection) }}</h3>
            <span class="visibility-badge" :class="getVisibilityClass(collection)">
              {{ getVisibilityText(collection) }}
            </span>
          </div>

          <p class="card-description">
            {{ getCollectionDescription(collection) }}
          </p>

          <div class="card-meta-row">
            <span class="meta-item">
              创建时间：{{ formatDate(collection.createdAt || collection.createTime) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PublicCollectionGrid',
  props: {
    collections: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getCollectionTitle(collection) {
      return collection?.name || collection?.title || '未命名收藏集';
    },
    getCollectionDescription(collection) {
      return collection?.description || collection?.content || '暂无描述';
    },
    getCoverImage(collection) {
      return collection?.coverImage || collection?.coverUrl || collection?.url || collection?.sourceUrl || '';
    },
    getVisibilityText(collection) {
      const isPublic = collection?.isPublic !== undefined
        ? collection.isPublic
        : (collection?.isShared !== undefined ? collection.isShared : true);
      return isPublic ? '公开' : '私有';
    },
    getVisibilityClass(collection) {
      const isPublic = collection?.isPublic !== undefined
        ? collection.isPublic
        : (collection?.isShared !== undefined ? collection.isShared : true);
      return isPublic ? 'is-public' : 'is-private';
    },
    formatDate(time) {
      if (!time) {
        return '未知';
      }
      const date = new Date(time);
      if (Number.isNaN(date.getTime())) {
        return '未知';
      }
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    handleImageError(event) {
      event.target.style.display = 'none';
    }
  }
};
</script>

<style scoped>
.public-collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.public-collection-card {
  cursor: pointer;
}

.card-shell {
  background: #ffffff;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(148, 163, 184, 0.16);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.public-collection-card:hover .card-shell {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
  border-color: rgba(99, 102, 241, 0.22);
}

.card-cover {
  height: 220px;
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  font-size: 36px;
  background: linear-gradient(135deg, #e2e8f0 0%, #f8fafc 100%);
}

.card-body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  margin: 0;
  flex: 1;
  font-size: 20px;
  line-height: 1.4;
  color: #0f172a;
  font-weight: 600;
  word-break: break-word;
}

.visibility-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}

.visibility-badge.is-public {
  background: #ecfdf5;
  color: #059669;
}

.visibility-badge.is-private {
  background: #f1f5f9;
  color: #475569;
}

.card-description {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
  min-height: 48px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta-row {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
}

@media (max-width: 768px) {
  .public-collection-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .card-cover {
    height: 200px;
  }
}
</style>
