## 编码规范


### 1. 包与模块组织

每个业务领域独立一个包，包内包含该领域的全部类：

```
com.moneynote.<module>/
├── Xxx.java              # 实体
├── XxxRepository.java    # Repository 接口
├── XxxService.java       # Service
├── XxxController.java    # Controller
├── XxxAddForm.java       # 请求 DTO（按需：UpdateForm、QueryForm 等）
└── XxxVo.java            # 响应 DTO（按需）
```

通用基础设施放在独立包中：

| 包 | 职责 |
|---|------|
| `base` | `BaseEntity`、`BaseRepository`、通用 DTO 基类 |
| `config` | Spring 配置类（拦截器、CORS、API 前缀、过滤器） |
| `exception` | `BusinessException`、`GlobalExceptionHandler` |
| `response` | 统一响应封装（`BaseResponse`、`DataResponse` 等） |
| `tree` | 树形结构基类和工具（`TreeEntity`、`TreeUtils`） |
| `utils` | 工具类（`CacheHolder`、`WebUtils`、`MessageSourceUtil`） |
| `db` | 数据库首次启动配置向导 |

### 2. 实体（Entity）

```java
@Entity
@Table(name = "t_xxx")        // 表名统一加 t_ 前缀
@Getter @Setter               // Lombok 生成 getter/setter
public class Xxx extends BaseEntity {
    // 字段定义
}
```

**规则：**
- 所有实体继承 `BaseEntity`，自带 `Long id`（`@GeneratedValue(strategy = GenerationType.IDENTITY)`）
- 表名使用 `t_` 前缀（如 `t_user`、`t_book`、`t_money_flow`）
- 金额字段统一使用 `Long` 类型，以最小货币单位存储（如 ¥10.50 → `1050`）
- 关联关系优先使用 `FetchType.LAZY`；必须立即加载时才用 `EAGER`
- `@Column` 明确标注 `length` 和 `nullable`
- 字符串字段长度参考：用户名 16、名称 64、备注 1024
- 使用 Lombok `@Getter @Setter`，不手写 getter/setter

### 3. Repository

```java
@Repository
public interface XxxRepository extends BaseRepository<Xxx> {
    // Spring Data JPA 方法命名查询
    Optional<Xxx> findOneByXxx(Xxx param);
    boolean existsByXxx(Xxx param);
}
```

**规则：**
- 继承 `BaseRepository<T>`（内部继承 `JpaRepository<T, Long>`）
- 查询方法使用 Spring Data JPA 方法命名约定
- 查找单个用 `Optional<Xxx>` 接收，避免直接返回 `null`
- 存在性检查用 `boolean existsByXxx(...)`

### 4. Service

```java
@Service
@RequiredArgsConstructor
public class XxxService {

    private final XxxRepository xxxRepository;  // 构造器注入

    @Transactional
    public void add(XxxAddForm form) { ... }

    @Transactional(readOnly = true)
    public XxxVo detail(Long id) { ... }
}
```

**规则：**
- 使用 `@RequiredArgsConstructor` + `private final` 字段注入（**禁止** `@Autowired`）
- 写方法标注 `@Transactional`
- 只读方法标注 `@Transactional(readOnly = true)`
- 参数校验在 Controller 层通过 `@Valid` 完成，Service 层负责业务规则校验
- 业务规则违反时直接抛 `BusinessException`，消息使用 i18n key（如 `"book.add.conflict"`）

### 5. Controller

```java
@RestController
@RequiredArgsConstructor
public class XxxController {

    private final XxxService xxxService;

    @RequestMapping(method = RequestMethod.POST, value = "/xxx/add")
    public BaseResponse handleAdd(@Valid @RequestBody XxxAddForm form) {
        xxxService.add(form);
        return new BaseResponse(true);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/xxx/detail")
    public BaseResponse handleDetail(@RequestParam Long id) {
        return new DataResponse<>(xxxService.detail(id));
    }
}
```

**规则：**
- 使用 `@RequestMapping(method = RequestMethod.GET/POST, value = "/path")`（**禁止** `@GetMapping` / `@PostMapping` 简写）
- 所有接口自动添加 `/api/v1` 前缀（由 `ApiPrefixConfig` 配置，无需手动添加）
- `@RequestBody` 参数必须标注 `@Valid`
- 方法命名使用 `handle` 前缀（如 `handleLogin`、`handleRegister`）
- 返回值统一使用 `BaseResponse` 或其子类

### 6. DTO / Form

```java
@Getter @Setter
public class XxxAddForm {

    @NotBlank
    @Size(max = 64)
    private String name;

    private String notes;
}
```

**规则：**
- Form 类用于接收请求参数，Vo 类用于响应数据
- 使用 Lombok `@Getter @Setter`
- 校验注解直接加在字段上
- 对于复用频率高的校验规则，定义组合约束注解（如 `@ValidUsername`、`@ValidPassword`），用 `@Constraint(validatedBy = {})` 声明为组合约束
- 校验消息分工存放：系统标准 key（`jakarta.validation.constraints.*`、`org.hibernate.validator.constraints.*`）覆盖在 `ValidationMessages.properties`；自定义校验 key（`validation.*`）统一放 `messages.properties`。组合约束内部可用 `message="{validation.xxx}"` 引用，但不要在字段上为每个校验重复写 message 文案，避免重复。

### 7. 响应封装

```
BaseResponse { success: boolean }
├── DataResponse<T> { success: true, data: T }
├── SimpleResponse { success, message, showType }
│   └── ValidationErrorResponse { violations: List<Violation> }
```

**规则：**
- 无返回数据时：`return new BaseResponse(true)`
- 有返回数据时：`return new DataResponse<>(data)`（自动设置 `success = true`）
- 错误响应由 `GlobalExceptionHandler` 统一处理，不要在 Controller 中手动构造

### 8. 异常处理

```java
throw new BusinessException("user.register.exists");
```

**规则：**
- 业务异常统一使用 `BusinessException`，消息传 i18n key
- `BusinessException` 禁止堆栈填充（`super(message, null, false, false)`），减少性能开销
- `GlobalExceptionHandler` 统一捕获并转换为 `SimpleResponse`，通过 `MessageSourceUtil.getMessage()` 解析 i18n 消息
- 不要捕获 `Exception` 来吞掉异常

### 9. 认证相关

```java
// 获取当前用户 ID
Long userId = CurrentUserHolder.get();

// 获取当前完整 User 对象
User currentUser = SessionManager.getCurrentUser();
```

**规则：**
- `CurrentUserHolder`（`ThreadLocal<Long>`）存放当前请求的用户 ID
- `SessionManager` 管理会话的创建和查询
- `CacheHolder` 持有两个静态 `ConcurrentHashMap`：`token → userId` 和 `userId → SessionInfo`
- 新增免认证接口需在 `WebConfig.addInterceptors()` 的 `excludePathPatterns` 中注册

### 10. 通用约定

| 项目 | 规范 |
|------|------|
| 依赖注入 | `@RequiredArgsConstructor` + `private final`（禁止 `@Autowired`） |
| Lombok | Entity/DTO/Form/Vo 用 `@Getter @Setter`；Service/Controller 用 `@RequiredArgsConstructor` |
| 注解顺序 | 类注解：`@Entity` → `@Table` → Lombok → 类声明；字段注解：JPA → 校验 → 字段声明 |
| 注释 | 中文注释，行内注释用于解释"为什么"，Javadoc 用于说明"做什么" |
| 空行 | 类声明后、方法间留一个空行，import 块与类声明间留一个空行 |
| 字符串 | 使用 i18n key 而非硬编码中文 |