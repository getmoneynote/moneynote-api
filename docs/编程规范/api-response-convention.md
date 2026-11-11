# MoneyNote 前后端统一响应约定

> 本文件是前后端**公共契约**：后端按此返回，前端按此消费。
> 与常见 `{ code, message, data }` 三段式不同，MoneyNote **没有 `code` 字段**，
> 采用 **`success` 布尔标志 + HTTP 状态码 + 可选的 `message` / `data`**。

## 1. 核心设计

| 设计点 | 约定 |
|--------|------|
| 成功标志 | `success` 布尔字段，唯一业务成功标志 |
| 错误类别 | **HTTP 状态码**表达（4xx / 5xx），不使用 code |
| 错误文案 | `message` 字符串，**仅在失败时出现**（成功响应无此字段） |
| 业务数据 | `data` 字段，仅在需要时出现 |
| 无 code | 业务码与 HTTP 语义合一，减少一层映射 |

一句话总结：

- **成功**：HTTP `2xx`，`success: true`，无 `message`
- **失败**：HTTP `4xx/5xx`，`success: false`，带 `message`

## 2. 响应类型

后端响应类位于 `backend/.../response/`，全部继承自 `SimpleResponse`：

| 类型 | 结构 | 典型场景 |
|------|------|----------|
| `SimpleResponse` | `{ success }` | 无返回数据的操作（新增、修改、删除、注册） |
| `DataResponse<T>` | `{ success, data }` | 需要返回单对象 / 单值（登录 token、选项列表） |
| `PageResponse<T>` | `{ success, data: { content, current, pageSize, total } }` | 分页列表 |
| `MessageResponse` | `{ success, message }` | 业务失败（由全局异常处理器统一构造） |
| `ValidationErrorResponse` | `{ success, message, violations[] }` | `@Valid` 参数校验失败 |

`violations[]` 中每个元素：

```json
{ "fieldName": "code", "message": "不能为空", "rejectedValue": "" }
```

## 3. HTTP 状态码约定

| HTTP 状态码 | 含义 | 对应异常 |
|-------------|------|----------|
| `200` | 成功 | 正常返回路径 |
| `400` | 参数错误 / 业务失败兜底 | `BusinessException`、`ResourceInvalidException`、`@Valid` 校验失败 |
| `401` | 未认证 | `UnauthorizedException` |
| `403` | 资源受保护，禁止操作 | `ResourceProtectedException` |
| `404` | 资源不存在 / 路由未匹配 | `ResourceNotFoundException`、`NoResourceFoundException` |
| `409` | 资源冲突（如已存在） | `ResourceDuplicateException` |
| `500` | 未知系统异常 | 兜底 `Exception` |

## 4. 实际响应示例

### 4.1 成功 · 无数据（`SimpleResponse`）

`POST /currencies`（新增币种）

```http
HTTP/1.1 200 OK
```

```json
{
  "success": true
}
```

### 4.2 成功 · 带数据（`DataResponse`）

`POST /login`（登录）

```http
HTTP/1.1 200 OK
```

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

### 4.3 成功 · 分页（`PageResponse`）

`GET /currencies?page=0&size=10`

```http
HTTP/1.1 200 OK
```

```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "code": "CNY", "name": "人民币" }
    ],
    "current": 1,
    "pageSize": 10,
    "total": 1
  }
}
```

> 注意：
> - `current` 对外从 **1** 开始（Spring Data 内部 `pageNumber` 从 0 开始，构造时已 +1）。
> - 分页元信息放在 `data` 内部而不是顶层平铺，保证前端「统一取 `data` 字段」的解包约定对所有接口成立。

### 4.4 业务失败（`MessageResponse`）

`POST /login`（密码错误）

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

### 4.5 参数校验失败（`ValidationErrorResponse`）

`POST /currencies`（缺少必填字段）

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "success": false,
  "message": "参数校验失败",
  "violations": [
    {
      "fieldName": "code",
      "message": "不能为空",
      "rejectedValue": ""
    }
  ]
}
```

### 4.6 未认证

```http
HTTP/1.1 401 Unauthorized
```

```json
{
  "success": false,
  "message": "登录已过期，请重新登录"
}
```

## 5. 前端消费约定

前端统一在 `web-pc/src/api/request.js` 收口（基于 Umi 的 request / axios）：

- **2xx 响应**：统一解包，调用方拿到的就是 `data` 字段（业务数据本身），
  `success` 恒为 `true` 无需判断；分页接口拿到的是 `{ content, current, pageSize, total }`
- **非 2xx 响应**：进入全局 `errorHandler`（`web-pc/src/request/errorHandler.js`），
  统一展示后端 `message`；`401` 额外清登录态并跳转 `/login`
- **`skipErrorHandler: true`**：请求失败时跳过全局错误处理，由调用方 `try/catch` 自行处理（如 `userApi.version`）

典型调用：

```js
const { accessToken } = await userApi.login({ username, password });
```

```js
const { data: currencyOptions } = useRequest(currencyApi.options);
```

```js
// 分页：直接传后端契约参数（page 从 0 起）；antd 的 current/pageSize 只在 BaseProTable 边界转换
const page = await currencyApi.list({ page: 0, size: 10 });
// page = { content: [...], current: 1, pageSize: 10, total: 100 }
```

## 6. 注意事项

1. **`success` 默认 `true`**：`SimpleResponse()` 无参构造即成功，失败路径统一由全局异常处理器传 `false`
2. **`message` 国际化**：后端经 `MessageSource` 按请求 `Locale` 渲染，前端无需本地映射文案
3. **失败不携带 `data`**：若需在失败时携带数据，应使用其它约定（当前未启用）
4. **HTTP 状态码是契约的一部分**：前端判定错误类别应优先看状态码，`message` 仅用于展示
5. **`data` 形态**：单对象为对象，列表为数组；分页接口 `data` 为 `{ content, current, pageSize, total }` 对象，分页元信息不顶层平铺
