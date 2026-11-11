import { getAccessToken } from '@/utils/auth';

// —— 请求拦截器：为请求附加登录 token ——
// Umi Max 拦截器协议：接收 (url, options)，返回处理后的 { url, options }，
// 必须设置 options.interceptors = true 才会走该协议
export const authHeaderInterceptor = (url, options) => {
  const token = getAccessToken();

  return {
    url,
    options: {
      ...options,
      interceptors: true,
      headers: {
        ...options.headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    },
  };
};
