/**
 * antd App 实例桥：把 <App> 提供的 message/notification/modal 实例
 * 缓存给无法使用 React context 的模块级代码（如 request 的 errorHandler）。
 * 组件内部不要走这里，直接用 App.useApp()。
 */

let appApi = {};

export const setAppApi = (api) => {
  appApi = api;
};

export const getAppApi = () => appApi;
