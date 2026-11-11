# MoneyNote UI

整个项目的前端界面

## 技术栈

整个项目是在Ant Design Pro v6的基础上开发的，将TypeScript代码转换为了纯JavaScript

- 框架：React / Ant Design Pro v6（https://beta-pro.ant.design/docs/overview-cn）
- 语言：JavaScript（纯 JS，无 TypeScript）
- 命令：见 `package.json` 的 `scripts` 字段


### 前端：手写 API 请求
- 后端驱动开发，前端在 `src/api/` 下手写请求函数
- 请求函数放在 `src/api/<feature>.js`，导出 `<feature>Api` 对象

## 服务端状态管理
- 统一使用 Umi `useRequest()`（来自 `@umijs/max`）管理服务端状态
- 数据流：`src/api/<feature>.js` → `useRequest()` → 页面

## 样式规范
- Ant Design：负责 UI 组件
- antd-style（`createStyles`）：负责组件级动态样式，围绕 antd token 定制
- 不使用其他 CSS 框架 / utility 体系

## 编码规范
- 新增/编辑弹窗统一参考 `docs/弹窗最佳实践.md`（现有样板见 `src/pages/currency/`）
- 表格列的对齐 / 列宽 / 数字展示统一参考 `docs/表格规范.md`
