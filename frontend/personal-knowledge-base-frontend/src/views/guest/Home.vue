<template>
  <div class="guest-home-container">
    <GuestHeader />

    <UnifiedSkeleton 
      v-if="loading"
      type="stats"
      :count="3"
    />

    <PageError
      v-else-if="loadError"
      :show-error="loadError"
      :title="errorTitle"
      :description="errorMessage"
      @retry="loadHomeData"
    />

    <main v-if="!loadError" v-loading="loading" element-loading-text="加载中...">
      <transition name="fade">
        <HeroSection 
          v-if="!loading"
          :is-logged-in="isAuthenticated"
          @explore="handleExplore"
          @login="goToLogin"
          @dashboard="goToDashboard"
        />
      </transition>

      <transition name="slide-up">
        <CoreValuesSection v-if="!loading" />
      </transition>

      <transition name="slide-up">
        <PopularKnowledgeSection 
          v-if="!loading"
          :knowledge="popularKnowledge"
          :loading="loading"
          @item-click="goToDetail"
          @view-more="goToSearchCenter"
        />
      </transition>

      <transition name="slide-up">
        <CollectionsSection
          v-if="!loading"
          :collections="recommendedCollections"
          :loading="loading"
          @item-click="goToDetail"
          @view-more="goToCollections"
        />
      </transition>
    </main>

    <Footer />
  </div>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
import { debounceSearch } from '@/utils/debounce';
import GuestHeader from '@/components/guest/GuestHeader.vue';
import Footer from '@/components/layout/Footer.vue';
import UnifiedSkeleton from '@/components/common/UnifiedSkeleton.vue';
import PageError from '@/components/common/PageError.vue';
import HeroSection from '@/components/guest/HeroSection.vue';
import CoreValuesSection from '@/components/guest/CoreValuesSection.vue';
import PopularKnowledgeSection from '@/components/guest/PopularKnowledgeSection.vue';
import CollectionsSection from '@/components/guest/CollectionsSection.vue';

export default {
  name: 'GuestHome',
  components: {
    GuestHeader,
    Footer,
    UnifiedSkeleton,
    PageError,
    HeroSection,
    CoreValuesSection,
    PopularKnowledgeSection,
    CollectionsSection,
  },
  data() {
    return {
      searchQuery: '',
      debouncedSearch: null,
      errorTitle: '加载失败',
    };
  },
  computed: {
    ...mapState('home', ['popularKnowledge', 'recommendedCollections', 'loading', 'loadError', 'errorMessage']),
    ...mapGetters('user', ['isAuthenticated'])
  },
  async mounted() {
    this.debouncedSearch = debounceSearch((query) => {
      this.performSearchAction(query);
    }, 500);

    await this.loadHomeData();
    this.preloadCriticalResources();
  },
  methods: {
    ...mapActions('home', ['fetchHomeData', 'fetchPopularKnowledge', 'fetchRecommendedCollections']),

    handleExplore() {
      this.goToLogin();
    },

    goToDashboard() { 
      this.$router.push('/personal/center'); 
    },
    goToLogin() { 
      this.$router.push('/login'); 
    },
    goToSearchCenter() { 
      this.$router.push('/search/center'); 
    },
    goToCollections() { 
      this.$router.push('/collections'); 
    },
    goToDetail() { 
      this.$router.push({
        path: '/login',
        query: { redirect: '/search' }
      });
    },
    goToCollection() { 
      this.$router.push({
        path: '/login',
        query: { redirect: '/collections' }
      });
    },

    performSearch(query) {
      if (this.debouncedSearch) {
        this.debouncedSearch(query);
      }
    },

    performSearchAction(query) {
      if (query?.trim()) {
        this.$router.push({
          path: '/search/center',
          query: { q: query.trim() }
        });
      }
    },

    searchByTag(tagName) {
      this.$router.push({
        path: '/search/center',
        query: { tag: tagName }
      });
    },

    async loadHomeData(retryCount = 0, maxRetries = 2) {
      try {
        await this.fetchHomeData({ retryCount, maxRetries });
      } catch (error) {
        console.error('加载首页数据失败:', error);
      }
    },

    preloadCriticalResources() {
    },

    async getPopularKnowledgeData(limit = 5) {
      try {
        await this.$store.dispatch('home/fetchPopularKnowledge', limit);
        return this.$store.state.home.popularKnowledge || [];
      } catch (error) {
        console.error('获取热门知识库失败', error);
        return [];
      }
    },

    async getCollectionsData(limit = 5) {
      try {
        await this.$store.dispatch('home/fetchRecommendedCollections', limit);
        return this.$store.state.home.recommendedCollections || [];
      } catch (error) {
        console.error('获取收藏集失败', error);
        return [];
      }
    }
  },

  beforeDestroy() {
    if (this.debouncedSearch && this.debouncedSearch.cancel) {
      this.debouncedSearch.cancel();
    }
  }
};
</script>

<style scoped>
.guest-home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

main {
  flex: 1;
  animation: fadeIn var(--transition-slow) ease-out;
}

main > section,
main > div {
  margin-bottom: var(--space-6);
}

main > section:last-child,
main > div:last-child {
  margin-bottom: 0;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.slide-up-enter-active {
  transition: all 0.6s ease-out;
}

.fade-enter,
.slide-up-enter {
  opacity: 0;
  transform: translateY(30px);
}
</style>
