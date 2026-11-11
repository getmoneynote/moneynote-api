import { App } from 'antd';
import { setAppApi } from '@/utils/antdApp';

/**
 * 无渲染桥接组件：必须挂在 <App> 内部，
 * 把 App.useApp() 的实例同步给模块级代码（见 src/utils/antdApp.js）。
 */
export default function AppBridge() {
  setAppApi(App.useApp());
  return null;
}
