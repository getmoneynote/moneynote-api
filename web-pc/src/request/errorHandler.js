import { getIntl } from '@umijs/max';
import { message as staticMessage } from 'antd';
import { getAppApi } from '@/utils/antdApp';
import { clearAccessToken } from '@/utils/auth';

/**
 * 与后端统一响应契约对齐（见根目录 docs/api-response-convention.md）：
 * - 成功：HTTP 2xx，success 恒为 true
 * - 失败：HTTP 4xx/5xx，body 为 { success:false, message }（message 已按 Locale 国际化）
 */

const t = (id) => getIntl().formatMessage({ id });

// 本模块是 request 拦截器（非组件），拿不到 React context，
// 走 AppBridge 缓存的 <App> 实例，避免 antd 静态方法警告；App 未挂载前兜底用静态方法
const showError = (content) => {
  const { message } = getAppApi();
  if (message?.error) {
    message.error(content);
  } else {
    staticMessage.error(content);
  }
};

// 401 跳转防抖：并发请求同时 401 时只清一次登录态、跳一次登录页
let isRedirecting401 = false;

// 请求成功（2xx）但业务失败（success=false）时的兜底。
// 当前后端 2xx 恒 success=true，此分支仅防御性保留；抛出的错误由 errorHandler 统一展示
export const errorThrower = (res) => {
  const { success, message: msg } = res;
  if (!success) {
    const error = new Error(msg);
    error.name = 'BizError';
    throw error;
  }
};

const isNetworkError = (error) => {
  return (
    error.code === 'NETWORK_ERROR' ||
    error.type === 'TypeError' ||
    !error.response ||
    error.response.status === 0
  );
};

const isTimeoutError = (error) => {
  return (
    error.code === 'ECONNABORTED' ||
    error.message?.toLowerCase().includes('timeout')
  );
};

const handle401 = () => {
  if (isRedirecting401) return;
  isRedirecting401 = true;
  clearAccessToken();
  setTimeout(() => {
    window.location.href = '/login';
    setTimeout(() => {
      isRedirecting401 = false;
    }, 1000);
  }, 500);
};

export const errorHandler = (error, opts) => {
  // 允许调用方自己处理
  if (opts?.skipErrorHandler) {
    throw error;
  }

  // 业务异常：请求成功（2xx）但业务失败（success=false）
  if (error.name === 'BizError') {
    showError(error.message || t('operation.failed'));
    return;
  }

  // 超时
  if (isTimeoutError(error)) {
    showError(t('app.request.timeout'));
    return;
  }

  // 网络类错误：断网 / CORS / 服务不可达
  if (isNetworkError(error)) {
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      showError(t('app.request.offline'));
      return;
    }
    showError(t('app.request.network_error'));
    return;
  }

  // HTTP 非 2xx
  const { status, data } = error.response;

  if (status === 401) {
    showError(data?.message || t('app.request.unauthorized'));
    handle401();
    return;
  }

  showError(data?.message || t('app.request.failed'));
};
