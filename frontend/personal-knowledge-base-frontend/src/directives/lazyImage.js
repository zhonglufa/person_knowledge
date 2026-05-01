/**
 * 图片懒加载指令
 * 使用方式：v-lazy="imageUrl"
 * 支持选项：v-lazy="{ src: imageUrl, loading: 'loading.jpg', error: 'error.jpg' }"
 */

// 默认占位图（base64灰色背景）
const DEFAULT_LOADING = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuWbvueJh+WKoOi9veWksei0pTwvdGV4dD48L3N2Zz4='

const DEFAULT_ERROR = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZmZmMmYyIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIxNCIgZmlsbD0iI2U1MzUzNSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuWbvueJh+ivt+WPpzwvdGV4dD48L3N2Zz4='

// 观察器实例
let observer = null

// 初始化IntersectionObserver
function initObserver() {
  if ('IntersectionObserver' in window) {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const img = entry.target
          const src = img.dataset.src
          
          if (src) {
            img.src = src
            img.removeAttribute('data-src')
          }
          
          observer.unobserve(img)
        }
      })
    }, {
      rootMargin: '50px 0px', // 提前50px加载
      threshold: 0.01
    })
  }
}

// 懒加载指令
export default {
  inserted(el, binding) {
    // 初始化观察器
    if (!observer) {
      initObserver()
    }
    
    const img = el.tagName === 'IMG' ? el : el.querySelector('img')
    if (!img) return
    
    // 获取图片URL
    let src = ''
    let loadingSrc = DEFAULT_LOADING
    let errorSrc = DEFAULT_ERROR
    
    if (typeof binding.value === 'string') {
      src = binding.value
    } else if (typeof binding.value === 'object') {
      src = binding.value.src
      loadingSrc = binding.value.loading || DEFAULT_LOADING
      errorSrc = binding.value.error || DEFAULT_ERROR
    }
    
    if (!src) return
    
    // 设置loading状态
    img.src = loadingSrc
    img.dataset.src = src
    
    // 监听错误
    img.onerror = () => {
      img.src = errorSrc
    }
    
    // 使用IntersectionObserver
    if (observer) {
      observer.observe(img)
    } else {
      // 降级方案：直接加载
      img.src = src
    }
  },
  
  unbind(el) {
    // 清理观察
    const img = el.tagName === 'IMG' ? el : el.querySelector('img')
    if (img && observer) {
      observer.unobserve(img)
    }
  }
}
