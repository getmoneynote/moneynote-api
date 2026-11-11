import React from 'react';
import { history, Link } from '@umijs/max';
import { App } from 'antd';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

import {
  AppBridge,
  ErrorBoundary,
  Footer,
  LangDropdown,
  OfflineBanner,
  UserMenu,
} from '@/components';
import defaultSettings from '@root/config/defaultSettings';

import { requestConfig } from './request';
import { getAccessToken } from './utils/auth';
import { userApi } from './api';

// Initialize dayjs plugins globally
dayjs.extend(relativeTime);

const loginPath = '/login';
// 无需登录即可访问的公开路径
const publicPaths = [loginPath, '/register'];

/**
 * @see https://umijs.org/docs/api/runtime-config#getinitialstate
 * */
export async function getInitialState() {
  const state = { settings: defaultSettings };
  // 用户会话态（本位币、当前账本等）全局只在这里拉一次，
  // 之后由变更接口的响应（如 setBaseCurrency 返回的 UserSessionVo）局部更新
  if (!publicPaths.includes(window.location.pathname)) {
    const initState = await userApi.getInitState();
    state.currentUser = initState?.user;
  }
  return state;
}

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout = () => {
  return {
    menuItemRender: (item, dom) => {
      if (item.path) {
        return (
          <Link to={item.path} prefetch>
            {dom}
          </Link>
        );
      }
      return dom;
    },
    actionsRender: () => {
      return [<LangDropdown key="lang" />, <UserMenu key="user" />].filter(
        Boolean,
      );
    },
    footerRender: () => <Footer />,
    // 去掉页面顶部的面包屑导航
    breadcrumbRender: false,
    onPageChange: () => {
      const { location } = history;
      // 没有登录态（本地无 token）且当前页不是公开页面，重定向到 login
      // 不携带 redirect 参数：登录成功后统一跳 /report/dashboard（见登录页），
      // 避免被带回到已删除的旧路径
      if (!getAccessToken() && !publicPaths.includes(location.pathname)) {
        history.replace(loginPath);
      }
    },
    // Replace ProLayout's default ErrorBoundary with our offline-aware version,
    // so chunk load errors show friendly messages instead of "Something went wrong."
    ErrorBoundary,
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request = {
  baseURL: '/api/v1',
  timeout: 60000,
  ...requestConfig,
};

export function rootContainer(container) {
  return (
    // 自挂一层 <App>：不依赖 umi 插件 provider 的包裹顺序，
    // 保证 AppBridge 及页面内的 App.useApp() 一定能拿到实例
    <App>
      <AppBridge />
      <OfflineBanner />
      <ErrorBoundary>{container}</ErrorBoundary>
    </App>
  );
}
