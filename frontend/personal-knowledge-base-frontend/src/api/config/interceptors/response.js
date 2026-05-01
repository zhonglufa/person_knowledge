import router from '@/router'
import { Message } from 'element-ui'

// 错误消息去重（防止短时间内重复提示）
const errorMessages = new Set()
let errorTimer = null

/**
 * 显示错误消息（带防抖和去重）
 * @param {string} message - 错误消息
 */
function showError(message) {
  // 清除之前的定时器
  if (errorTimer) {
    clearTimeout(errorTimer);
  }
  
  // 检查是否是重复的错误消息
  if (errorMessages.has(message)) {
    return;
  }
  
  // 添加到集合中
  errorMessages.add(message);
  
  // 显示错误消息
  Message.error({
    message,
    duration: 3000,
    showClose: true
  });
  
  // 3秒后清除该消息，允许再次显示
  errorTimer = setTimeout(() => {
    errorMessages.delete(message);
  }, 3000);
}

/**
 * 响应拦截器配置
 * 自动解包后端 R 格式响应 {code, message, data}
 *
 * 处理后的数据结构：
 * - response.code: 状态码 (200, 400, 403, 500等)
 * - response.message: 消息文本
 * - response.data: 实际数据 (token, userInfo等)
 *
 * 不再需要 response.data.code 这种嵌套访问
 */
export const responseInterceptor = (response) => {
  // 获取原始响应数据
  const responseData = response.data

  // 如果是标准的 R 格式响应 {code, message, data}
  // 将 data 直接挂到响应对象上，方便组件直接访问
  if (responseData && typeof responseData === 'object' && 'code' in responseData) {
    // 业务错误码处理（HTTP 200但业务失败）
    if (responseData.code !== 200) {
      // 根据业务错误码显示不同的提示
      const businessErrorMessages = {
        400: '请求参数错误',
        401: '登录已过期，请重新登录',
        403: '权限不足，无法执行此操作',
        404: '请求的资源不存在',
        500: '服务器错误，请稍后重试'
      };
      
      const errorMsg = responseData.message || businessErrorMessages[responseData.code] || '操作失败';
      showError(errorMsg);
    }
    
    // 创建解包后的响应对象
    return {
      ...responseData,  // code, message 直接提升到顶层
      data: responseData.data,  // data 保持原样
      originalResponse: response  // 保存原始响应以备不时之需
    }
  }

  // 兼容其他格式（如文件下载、HTML等非JSON响应）
  return responseData || response
}

/**
 * 响应错误处理
 * 处理认证失败、权限不足等错误情况
 */
export const responseErrorHandler = (error) => {
  console.error('Response error:', error)

  // 提取错误信息
  let errorMessage = '请求失败，请稍后重试'
  let errorCode = null
  let shouldShowError = true // 是否显示错误提示

  // 尝试从响应中提取错误信息
  if (error.response) {
    const { status, data } = error.response

    // 根据HTTP状态码处理
    switch (status) {
      case 400:
        // 请求参数错误
        errorMessage = data?.message || '请求参数错误，请检查输入'
        errorCode = 400
        break
      case 401:
        // 认证失败（Token过期、无效等）
        errorMessage = '登录已过期，请重新登录'
        errorCode = 401
        shouldShowError = false // 由路由跳转处理，不重复提示
        break
      case 403:
        // 权限不足
        errorMessage = data?.message || '权限不足，无法访问该资源'
        errorCode = 403
        break
      case 404:
        // 资源不存在
        errorMessage = '请求的资源不存在'
        errorCode = 404
        break
      case 408:
        // 请求超时
        errorMessage = '请求超时，请检查网络连接'
        errorCode = 408
        break
      case 500:
        // 服务器错误
        errorMessage = '服务器错误，请稍后重试'
        errorCode = 500
        break
      case 502:
        // 网关错误
        errorMessage = '服务器网关错误，请稍后重试'
        errorCode = 502
        break
      case 503:
        // 服务不可用
        errorMessage = '服务暂时不可用，请稍后重试'
        errorCode = 503
        break
      case 504:
        // 网关超时
        errorMessage = '网关超时，请稍后重试'
        errorCode = 504
        break
      default:
        // 从后端响应中提取错误信息
        errorMessage = data?.message || error.message || '网络请求失败'
    }

    // 清除认证信息并重定向
    handleAuthError(status, data)
  } else if (error.request) {
    // 网络错误（无响应）
    errorMessage = '网络连接失败，请检查网络设置'
    errorCode = 'NETWORK_ERROR'
  } else {
    // 请求配置错误
    errorMessage = error.message || '请求配置错误'
    errorCode = 'CONFIG_ERROR'
  }

  // 显示错误提示（如果不需要静默处理）
  if (shouldShowError) {
    showError(errorMessage);
  }

  // 创建标准化的错误对象
  const normalizedError = new Error(errorMessage)
  normalizedError.code = errorCode
  normalizedError.status = error.response?.status
  normalizedError.responseData = error.response?.data
  normalizedError.shouldShowError = shouldShowError

  return Promise.reject(normalizedError)
}

/**
 * 处理认证错误
 * 根据当前上下文区分管理员和普通用户的认证失败处理
 */
function handleAuthError(status, data) {
  // 检查是否是管理员上下文
  const isAdminContext = window.location.pathname.startsWith('/admin')
  const adminToken = localStorage.getItem('adminToken')
  const userToken = localStorage.getItem('token')

  // 避免在登录页重复跳转
  const isLoginPage = window.location.pathname.includes('/login') ||
                      window.location.pathname.includes('/admin/login')

  if (isLoginPage) {
    return // 在登录页时不跳转
  }

  // 清除对应的认证信息
  if (status === 401) {
    // Token过期或无效
    if (isAdminContext && adminToken) {
      // 管理员Token过期
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminInfo')
      // 保存重定向路径
      localStorage.setItem('redirectAfterAdminLogin', window.location.pathname)
      // 跳转到管理员登录页
      router.push('/admin/login')
    } else if (userToken) {
      // 普通用户Token过期
      localStorage.removeItem('token')
      // 保存重定向路径
      localStorage.setItem('redirectAfterLogin', window.location.pathname)
      // 跳转到用户登录页
      router.push('/login')
    }
  } else if (status === 403) {
    // 权限不足（非管理员访问管理后台）
    if (isAdminContext && !adminToken) {
      router.push('/admin/login')
    }
  }
}