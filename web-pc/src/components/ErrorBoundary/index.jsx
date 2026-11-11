import { getIntl } from '@umijs/max';
import { Button, Card, Result } from 'antd';
import React from 'react';

// class 组件与模块级渲染函数用不了 useT()，与 errorHandler.js 一致走 getIntl()
const t = (id) => getIntl().formatMessage({ id });

function isChunkLoadError(error) {
  return (
    error.name === 'ChunkLoadError' ||
    /(?:loading|failed to load) (?:css )?chunk/i.test(error.message) ||
    /Failed to fetch dynamically imported module/i.test(error.message)
  );
}

function renderErrorFallback(error, isOnline, onRetry, onReload) {
  const isOffline = !isOnline;
  const isChunkError = isChunkLoadError(error);

  return (
    <Card variant="borderless" style={{ margin: 24 }}>
      <Result
        status="error"
        title={t(isChunkError ? 'app.error.chunk.title' : 'app.error.render.title')}
        subTitle={t(
          isChunkError
            ? isOffline
              ? 'app.error.chunk.description.offline'
              : 'app.error.chunk.description.online'
            : 'app.error.render.description',
        )}
        extra={[
          isChunkError && (
            <Button type="primary" key="retry" onClick={onRetry}>
              {t('app.error.retry')}
            </Button>
          ),
          <Button
            type={isChunkError ? 'default' : 'primary'}
            key="reload"
            onClick={onReload}
          >
            {t('app.error.reload')}
          </Button>,
          <Button href="/" key="home">
            {t('app.error.home')}
          </Button>,
        ].filter(Boolean)}
      />
    </Card>
  );
}

export default class ErrorBoundary extends React.Component {
  state = {
    hasError: false,
    error: null,
    isOnline: typeof navigator !== 'undefined' ? navigator.onLine : true,
    retryCount: 0,
  };

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidMount() {
    window.addEventListener('online', this.handleOnline);
    window.addEventListener('offline', this.handleOffline);
  }

  componentWillUnmount() {
    window.removeEventListener('online', this.handleOnline);
    window.removeEventListener('offline', this.handleOffline);
  }

  handleOnline = () => {
    this.setState({ isOnline: true });
    if (
      this.state.hasError &&
      this.state.error &&
      isChunkLoadError(this.state.error)
    ) {
      window.location.reload();
    }
  };

  handleOffline = () => {
    this.setState({ isOnline: false });
  };

  componentDidCatch(error, info) {
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  handleRetry = () => {
    // Incrementing retryCount changes the key on the children fragment,
    // forcing React to unmount and remount all lazy components.
    // This causes React.lazy to re-execute import() for the failed chunk.
    this.setState((prev) => ({
      hasError: false,
      error: null,
      retryCount: prev.retryCount + 1,
    }));
  };

  handleReload = () => {
    window.location.reload();
  };

  render() {
    if (!this.state.hasError || !this.state.error) {
      return (
        <React.Fragment key={this.state.retryCount}>
          {this.props.children}
        </React.Fragment>
      );
    }
    return renderErrorFallback(
      this.state.error,
      this.state.isOnline,
      this.handleRetry,
      this.handleReload,
    );
  }
}
