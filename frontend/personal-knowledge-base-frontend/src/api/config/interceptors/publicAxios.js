import axios from 'axios'
import { responseInterceptor, responseErrorHandler } from './response'

/**
 * 公开API实例（不携带Token）
 * 用于产品首页等无需登录态的公开接口
 * 
 * 特性：
 * 1. 不自动携带Token
 * 2. 401错误静默处理（不跳转登录页）
 * 3. API失败时返回空数据而非错误
 */
const publicInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器（不添加Token）
publicInstance.interceptors.request.use(
  (config) => {
    // 公开接口不携带Token
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器（静默处理401错误）
publicInstance.interceptors.response.use(
  (response) => {
    return responseInterceptor(response)
  },
  (error) => {
    // 401错误静默处理，不跳转登录页
    if (error.response && error.response.status === 401) {
      console.warn('[PublicAPI] 接口需要认证，返回空数据')
      // 返回空数据结构，避免页面报错
      return Promise.resolve({
        code: 401,
        message: '需要登录',
        data: null
      })
    }
    
    // 其他错误正常处理
    return responseErrorHandler(error)
  }
)

export default publicInstance
