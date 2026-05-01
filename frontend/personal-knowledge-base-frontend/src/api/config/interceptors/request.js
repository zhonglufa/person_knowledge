
// 请求拦截器配置
export const requestInterceptor = (config) => {
  // 根据URL路径判断使用哪个token
  const isAdminRequest = config.url && config.url.startsWith('/admin')
  const token = localStorage.getItem(isAdminRequest ? 'adminToken' : 'token')

  // 如果有token，则在请求头中添加认证信息
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
};

// 请求错误处理
export const requestErrorHandler = (error) => {
  console.error('Request error:', error);
  return Promise.reject(error);
};
