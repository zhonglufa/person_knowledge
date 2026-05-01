// 导入拦截器
import { requestInterceptor, requestErrorHandler } from './request'
import { responseInterceptor, responseErrorHandler } from './response'

// 配置拦截器
export const setupInterceptors = (axiosInstance) => {
  // 请求拦截器
  axiosInstance.interceptors.request.use(
    requestInterceptor,
    requestErrorHandler
  )

  // 响应拦截器
  axiosInstance.interceptors.response.use(
    responseInterceptor,
    responseErrorHandler
  )

  return axiosInstance
}

// 导出所有拦截器
export {
  requestInterceptor,
  requestErrorHandler,
  responseInterceptor,
  responseErrorHandler
}
