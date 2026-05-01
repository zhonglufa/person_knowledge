// 认证相关的 API 接口

import apiClient from './axios'

// 登录接口
export const login = (credentials) => {
  return apiClient.post('/user/login', credentials)
}

// 注册接口
export const register = (userData) => {
  return apiClient.post('/user/register', userData)
}

// 发送验证码接口
export const sendVerifyCode = (emailData) => {
  return apiClient.post('/user/send-verify-code', emailData)
}

// 退出登录接口
export const logout = () => {
  return apiClient.post('/user/logout')
}

// 获取用户信息接口
export const getUserInfo = () => {
  return apiClient.get('/user/info')
}

// 封装成 authApi 对象导出
export const authApi = {
  login,
  register,
  sendVerifyCode,
  logout,
  getUserInfo
}