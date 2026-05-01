import axios from 'axios'
import { setupInterceptors } from './config/interceptors'

// 创建 Axios 实例
const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 设置拦截器
setupInterceptors(instance)

// 导出实例
export default instance
