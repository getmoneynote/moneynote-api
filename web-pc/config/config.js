// https://umijs.org/config/

import { join, posix } from 'node:path';
import { defineConfig } from '@umijs/max';
import defaultSettings from './defaultSettings';
import proxy from './proxy';

import routes from './routes';

const { UMI_ENV = 'dev' } = process.env;

/**
 * @name 使用公共路径
 * @description 部署时的路径，如果部署在非根目录下，需要配置这个变量
 * @doc https://umijs.org/docs/api/config#publicpath
 */
const PUBLIC_PATH = '/';

const config = defineConfig({
  alias: {
    '@root': join(__dirname, '..'),
  },
  /**
   * @name 开启 hash 模式
   * @description 让 build 之后的产物包含 hash 后缀。通常用于增量发布和避免浏览器加载缓存。
   * @doc https://umijs.org/docs/api/config#hash
   */
  hash: true,

  /**
   * @name esbuild 压缩时的 IIFE 辅助函数隔离
   * @description 解决多 chunk 之间 esbuild helper 变量名冲突的问题
   * @doc https://umijs.org/docs/api/config#esbuildminifyiife
   */
  esbuildMinifyIIFE: true,

  publicPath: PUBLIC_PATH,

  /**
   * @name 兼容性设置
   * @description 设置 ie11 不一定完美兼容，需要检查自己使用的所有依赖
   * @doc https://umijs.org/docs/api/config#targets
   */
  // targets: {
  //   ie: 11,
  // },
  /**
   * @name 路由的配置，不在路由中引入的文件不会编译
   * @description 只支持 path，component，routes，redirect，wrappers，title 的配置
   * @doc https://umijs.org/docs/guides/routes
   */
  // umi routes: https://umijs.org/docs/routing
  routes,
  /**
   * @name 主题的配置
   * @description 虽然叫主题，但是其实只是 less 的变量设置
   * @doc antd的主题设置 https://ant.design/docs/react/customize-theme-cn
   * @doc umi 的 theme 配置 https://umijs.org/docs/api/config#theme
   */
  // theme: { '@primary-color': '#1DA57A' }
  /**
   * @name 代理配置
   * @description 可以让你的本地服务器代理到你的服务器上，这样你就可以访问服务器的数据了
   * @see 要注意以下 代理只能在本地开发时使用，build 之后就无法使用了。
   * @doc 代理介绍 https://umijs.org/docs/guides/proxy
   * @doc 代理配置 https://umijs.org/docs/api/config#proxy
   */
  proxy: proxy[UMI_ENV],
  /**
   * @name 快速热更新配置
   * @description 一个不错的热更新组件，更新时可以保留 state
   */
  fastRefresh: true,
  /**
   * 单包构建：不需要按需加载，因此不开路由预加载与 manifest。
   * 这两项只为「按路由拆出的 async chunk」服务，开着只会多产出
   * preload_helper.*.js 和 asset-manifest.json 两个无用文件。
   * 如以后恢复分包，可重新加回 routePrefetch: {} / manifest: {}。
   */
  // routePrefetch: {},
  // manifest: {},
  //============== 以下都是max的插件配置 ===============
  /**
   * @name 数据流插件
   * @@doc https://umijs.org/docs/max/data-flow
   */
  model: {},
  /**
   * 一个全局的初始数据流，可以用它在插件之间共享数据
   * @description 可以用来存放一些全局的数据，比如用户信息，或者一些全局的状态，全局初始状态在整个 Umi 项目的最开始创建。
   * @doc https://umijs.org/docs/max/data-flow#%E5%85%A8%E5%B1%80%E5%88%9D%E5%A7%8B%E7%8A%B6%E6%80%81
   */
  initialState: {},
  /**
   * @name layout 插件
   * @doc https://umijs.org/docs/max/layout-menu
   */
  title: 'MoneyNote',
  layout: {
    locale: true,
    ...defaultSettings,
  },
  /**
   * @name 国际化插件
   * @doc https://umijs.org/docs/max/i18n
   */
  locale: {
    // default zh-CN
    default: 'zh-CN',
    antd: true,
    // default true, when it is true, will use `navigator.language` overwrite default
    baseNavigator: true,
  },
  /**
   * @name antd 插件
   * @description 内置了 babel import 插件
   * @doc https://umijs.org/docs/max/antd#antd
   */
  antd: {
    appConfig: {},
    configProvider: {
      variant: 'filled',
      theme: {
        token: {
          fontFamily: 'AlibabaSans, sans-serif',
        },
      },
    },
  },
  /**
   * @name 网络请求配置
   * @description 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
   * @doc https://umijs.org/docs/max/request
   *
   * dataField: '' —— 让 useRequest 的 formatResult 变成恒等函数，不再自动解包响应体。
   * 解包统一在 src/api/request.js 里做（返回 body.data），
   * 这样 useRequest 和直接 await 拿到的数据结构一致。
   * 注意：这是构建期配置，改动后需重启 dev server 才会重新生成 .umi 临时文件。
   */
  request: {
    dataField: '',
  },
  /**
   * @name <head> 中额外的 script
   * @description 配置 <head> 中额外的 script
   */
  headScripts: [
    // 解决首次加载时白屏的问题
    // 用 posix.join 避免 Windows 下拼出反斜杠路径（\scripts\loading.js）
    { src: posix.join(PUBLIC_PATH, 'scripts/loading.js'), async: true },
  ],

  /**
   * 单包构建：不做按需加载，全部页面和依赖打进同一个 umi.*.js。
   *
   * Umi 4 的路由天生是 React.lazy + import()，@ant-design/x 的 CodeHighlighter
   * 又用模板字符串动态 import prism 语言包（webpack 会为每种语言生成一个 chunk，
   * 本项目约 300 个）。单靠 codeSplitting.jsStrategy 只能改 splitChunks，
   * 消不掉 import() 产生的 chunk，所以这里用 webpack 的 LimitChunkCountPlugin
   * 强制把所有 chunk 合并成一个，同时关掉 splitChunks 避免重复拆分。
   * 产物只剩 index.html + 1 个 js + 1 个 css + public/ 静态资源。
   *
   * 注意：仅作用于生产构建，dev 仍走原来的增量编译。
   */
  chainWebpack(memo, { env, webpack }) {
    if (env === 'production') {
      memo.optimization.splitChunks(false);
      memo
        .plugin('limit-chunk-count')
        .use(webpack.optimize.LimitChunkCountPlugin, [{ maxChunks: 1 }]);
    }
    return memo;
  },

  /**
   * 不生成每条路由一个目录的同内容 index.html。
   * 后端的 SpaForwardFilter 会把非 /api、非静态文件的 GET 请求转发到 /index.html，
   * 深链刷新已能正常工作；若改为纯静态托管（nginx / 对象存储），
   * 需配置 try_files $uri /index.html，或把下面这行放开。
   */
  // exportStatic: {},
  define: {
    __APP_VERSION__: require('./../package.json').version,
  },
});

export default config;
