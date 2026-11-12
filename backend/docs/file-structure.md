# 项目结构

```
backend/
├── pom.xml                       # Maven 配置（Java 21, Spring Boot 4.1.0）
├── mvnw / mvnw.cmd / .mvn/       # Maven Wrapper
├── config/db-config.properties   # 安装向导生成的数据库配置（运行时读写）
├── data/                         # H2 本地开发数据文件
├── specs/                        # 产品/架构/API 规格（PRD / SPEC / ARCHITECTURE / API）
├── docs/                         # 开发规范文档
├── notes/                        # 零散笔记（db-setup / restful最佳实践 / todo）
└── src/main/
    ├── java/com/moneynote/       # 见下文
    └── resources/                # 见下文
```

Docker 相关文件（`Dockerfile` / `docker-compose*.yml` / `entrypoint.sh` /
`.dockerignore`）统一放在**仓库根目录**，构建上下文也是仓库根目录。

## Java 源码 `src/main/java/com/moneynote/`

```
├── Application.java              # 启动类：按数据库是否已配置选择正式 / Setup 模式
├── SetupApplication.java         # Setup 模式启动类（仅扫描 setup 包，排除数据源/JPA 自动配置）
├── TestController.java
│
├── account/                      # 账户模块
│   ├── Account.java              # 实体
│   ├── AccountAddForm.java       # 创建 DTO
│   ├── AccountQueryForm.java     # 查询 DTO
│   ├── AccountDetails.java       # 详情 DTO（继承 IdAndNameDetails）
│   ├── AccountController.java
│   ├── AccountService.java
│   └── AccountRepository.java
│
├── moneyflow/                    # 收支流水模块
│   └── MoneyFlow.java            # 实体
│
├── base/                         # 基础抽象层
│   ├── BaseEntity.java           # 实体基类
│   ├── BaseRepository.java       # Repository 基类
│   ├── BaseDetails.java          # 详情 DTO 基类
│   ├── IdAndNameDetails.java     # 通用 id+name DTO
│   └── SelectOption.java         # 下拉选项 VO
│
├── book/                         # 账本模块
│   ├── Book.java                 # 实体
│   ├── BookCreateForm.java       # 创建 DTO
│   ├── BookUpdateForm.java       # 更新 DTO
│   ├── BookListItem.java         # 列表项 DTO
│   ├── BookController.java
│   ├── BookService.java
│   └── BookRepository.java
│
├── category/                     # 分类模块（树形结构）
│   └── Category.java             # 实体（继承 TreeEntity）
│
├── config/                       # 配置
│   ├── ApiPrefixConfig.java      # 自动为 RestController 添加 /api/v1 前缀
│   ├── AuthInterceptor.java      # 认证拦截器（Bearer Token 校验）
│   ├── SpaForwardFilter.java     # SPA 路由支持（正式模式）
│   └── WebConfig.java            # 拦截器注册 + CORS 配置
│
├── currency/                     # 货币模块
│   ├── Currency.java             # 实体
│   ├── CurrencyCreateForm.java   # 创建 DTO
│   ├── CurrencyUpdateForm.java   # 更新 DTO
│   ├── CurrencyQueryForm.java    # 查询 DTO
│   ├── CurrencyListItem.java     # 列表项 DTO
│   ├── CurrencyController.java
│   ├── CurrencyService.java
│   └── CurrencyRepository.java
│
├── exception/                    # 异常处理
│   ├── BusinessException.java    # 业务异常基类（禁止堆栈填充）
│   ├── ResourceNotFoundException.java   # 404 路径级主资源缺失
│   ├── ResourceInvalidException.java    # 400 引用的辅助资源无效
│   ├── ResourceDuplicateException.java  # 资源重复
│   ├── ResourceProtectedException.java  # 受保护资源不可操作
│   ├── ForbiddenException.java   # 403
│   ├── UnauthorizedException.java # 401
│   └── GlobalExceptionHandler.java # 全局异常处理器
│
├── payee/                        # 收款人/付款人模块
│   └── Payee.java                # 实体
│
├── response/                     # 响应封装
│   ├── SimpleResponse.java       # 响应基类（success 标志）
│   ├── DataResponse.java         # 携带业务数据（继承 SimpleResponse）
│   ├── MessageResponse.java      # 携带提示文案（继承 SimpleResponse）
│   ├── PageResponse.java         # 分页数据响应（继承 DataResponse）
│   ├── ValidationErrorResponse.java # 校验失败响应（继承 MessageResponse）
│   └── Violation.java            # 校验违规详情
│
├── setup/                        # 首次启动安装向导
│   ├── SetupController.java      # /setup 向导接口（仅未配置数据库时注册）
│   ├── SetupRedirectFilter.java  # setup 模式下所有请求 302 到 /setup
│   └── db/
│       ├── DbConfig.java         # 配置项 POJO
│       ├── DbConfigHolder.java   # 读写 config/db-config.properties
│       ├── DbType.java           # 数据库类型枚举
│       └── DynamicDataSourceConfig.java # 动态 DataSource（@ConditionalOnProperty）
│
├── tag/                          # 标签模块
│   ├── Tag.java                  # 实体
│   ├── TagForm.java              # 创建/更新 DTO
│   ├── TagController.java
│   ├── TagService.java
│   └── TagRepository.java
│
├── tagrelation/                  # 标签关联（流水 ↔ 标签）
│   └── TagRelation.java          # 实体
│
├── tree/                         # 树形结构工具
│   ├── TreeEntity.java           # 树形实体基类（parent/level/name）
│   ├── TreeNode.java             # 树节点 DTO
│   └── TreeUtils.java            # 扁平列表 → 树形结构转换
│
├── user/                         # 用户模块
│   ├── User.java                 # 实体
│   ├── UserRepository.java
│   ├── UserService.java          # 注册/登录
│   ├── UserController.java
│   ├── UserConverter.java        # 实体 ↔ VO 转换
│   ├── LoginForm.java            # 登录 DTO
│   ├── RegisterForm.java         # 注册 DTO
│   ├── LoginSuccessVo.java       # 登录成功响应（携带 accessToken）
│   ├── InitStateResponse.java    # 初始化状态响应
│   ├── UserSessionVo.java        # 用户会话 VO
│   ├── SessionInfo.java          # 会话信息
│   ├── SessionManager.java       # 会话管理（Token 生成/查询）
│   ├── CurrentUserHolder.java    # ThreadLocal 当前用户 ID
│   ├── InvalidCredentialsException.java # 登录失败业务异常
│   ├── UserExceptionHandler.java # user 模块专属异常处理（仅作用于 UserController）
│   ├── ValidUsername.java        # 用户名校验注解
│   └── ValidPassword.java        # 密码校验注解
│
├── utils/                        # 工具类
│   ├── CacheHolder.java          # 内存缓存（token→userId, userId→SessionInfo）
│   ├── MessageResolver.java      # 统一国际化消息解析器
│   └── WebUtils.java             # Web 工具（解析 Bearer Token）
│
└── validate/                     # 组合校验注解
    ├── ValidName.java            # 非空 + 长度 ≤ 64
    └── ValidNotes.java           # 长度 ≤ 1024
```

## 资源文件 `src/main/resources/`

```
├── application.properties        # 主配置（端口 9095, JPA, Hibernate）
├── messages.properties           # 业务消息 i18n（默认 / en_US / zh_CN）
├── messages_en_US.properties
├── messages_zh_CN.properties
├── ValidationMessages.properties # 校验消息 i18n（默认 / en_US / zh_CN）
├── ValidationMessages_en_US.properties
├── ValidationMessages_zh_CN.properties
├── public/                       # 前端构建产物
│   └── index.html
└── setup/                        # 首次启动安装向导页面
    ├── index.html
    └── logo.svg
```
