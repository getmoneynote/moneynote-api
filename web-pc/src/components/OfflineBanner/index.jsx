import { getIntl } from '@umijs/max';
import { Alert } from 'antd';
import { useSyncExternalStore } from 'react';

// 本组件挂在 app.jsx 的 rootContainer 层，位于 i18nProvider 外侧，
// useT() 拿不到 intl，与 ErrorBoundary / errorHandler.js 一致走 getIntl()
const t = (id) => getIntl().formatMessage({ id });

const subscribeOnlineStatus = (callback) => {
  window.addEventListener('online', callback);
  window.addEventListener('offline', callback);
  return () => {
    window.removeEventListener('online', callback);
    window.removeEventListener('offline', callback);
  };
};

const getOnlineStatus = () =>
  typeof navigator === 'undefined' ? true : navigator.onLine;

const OfflineBanner = () => {
  const isOnline = useSyncExternalStore(
    subscribeOnlineStatus,
    getOnlineStatus,
    () => true,
  );

  if (isOnline) return null;

  return (
    <Alert
      type="warning"
      showIcon
      closable={false}
      style={{
        position: 'fixed',
        top: 8,
        left: '50%',
        transform: 'translateX(-50%)',
        zIndex: 10,
        maxWidth: 480,
      }}
      title={t('app.network.offline')}
    />
  );
};

export default OfflineBanner;
