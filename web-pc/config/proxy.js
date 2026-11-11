/**
 * @name 代理的配置
 * @see 在生产环境 代理是无法生效的，所以这里没有生产环境的配置
 * @doc https://umijs.org/docs/guides/proxy
 */
export default {
  /**
   * @name 本地开发代理
   * @description 前端请求带 /api/v1 前缀，代理到本地后端服务。
   */
  dev: {
    '/api/v1': {
      target: 'http://127.0.0.1:4523/m1/8720521-8506928-default',
      pathRewrite: { '^/api/v1': '' },
      changeOrigin: true,
    },
  },
  pre: {
    '/api/v1': {
      target: 'http://localhost:9096',
      changeOrigin: true,
    },
  },
};
