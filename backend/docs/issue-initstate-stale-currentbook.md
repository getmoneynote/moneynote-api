# 问题：切换账本后 initState 返回旧数据

## 现象

用户调用 `PATCH /api/v1/setCurrentBook/{id}` 切换当前账本，接口返回成功。但随后调用 `GET /api/v1/initState` 获取初始状态时，`currentBookId` 仍然是切换前的旧值。只有重新登录后 `initState` 才返回正确的值。

---

## 涉及文件

| 文件 | 角色 |
|---|---|
| `src/main/java/com/moneynote/user/UserController.java` | 定义 `GET /initState` 和 `PATCH /setCurrentBook/{id}` |
| `src/main/java/com/moneynote/user/UserService.java` | `getInitState()` 和 `setCurrentBook()` 的实现 |
| `src/main/java/com/moneynote/user/SessionManager.java` | 会话创建与管理 |
| `src/main/java/com/moneynote/user/SessionInfo.java` | 会话数据容器（UserSessionVo + User 实体） |
| `src/main/java/com/moneynote/user/UserSessionVo.java` | 返回给前端的用户会话 VO，含 `currentBookId` |
| `src/main/java/com/moneynote/user/User.java` | JPA 实体，`currentBookId` 字段在 `t_user` 表 |
| `src/main/java/com/moneynote/user/CurrentUserHolder.java` | ThreadLocal 持有当前请求的 userId |
| `src/main/java/com/moneynote/utils/CacheHolder.java` | 内存缓存：`token → userId` 和 `userId → SessionInfo` |
| `src/main/java/com/moneynote/config/AuthInterceptor.java` | 拦截器，从 token 解析 userId 放入 ThreadLocal |

---

## 关键代码

### 登录时：SessionManager.createSession()

```java
// SessionManager.java:9-21
public static String createSession(User user) {
    SessionInfo sessionInfo = CacheHolder.sessionInfo.computeIfAbsent(
        user.getId(), k -> new SessionInfo());

    UserSessionVo userSessionVo = new UserSessionVo();
    userSessionVo.setUsername(user.getUsername());
    userSessionVo.setPhoto(user.getPhoto());
    userSessionVo.setCurrentBookId(user.getCurrentBookId());  // ① 快照 currentBookId

    sessionInfo.setUserSession(userSessionVo);   // ② 存入 UserSessionVo
    sessionInfo.setCurrentUser(user);            // ③ 存入 JPA 实体

    String token = UUID.randomUUID().toString().replace("-", "");
    CacheHolder.token.put(token, user.getId());
    return token;
}
```

登录时 `SessionInfo` 里有两份数据：
- `SessionInfo.userSession` — `UserSessionVo`，其中 `currentBookId` 取自 `User` 实体的当前值
- `SessionInfo.currentUser` — `User` JPA 实体本身

此时两者一致。

### 切换账本：UserService.setCurrentBook()

```java
// UserService.java:61-64
public void setCurrentBook(Long bookId) {
    User user = SessionManager.getCurrentUser();  // 获取 SessionInfo.currentUser
    user.setCurrentBookId(bookId);                // ✅ 更新 JPA 实体
    userRepository.save(user);                    // ✅ 持久化到 DB
    // ❌ 没有更新 SessionInfo.userSession.currentBookId
}
```

### 获取初始状态：UserService.getInitState()

```java
// UserService.java:52-59
public InitStateResponse getInitState() {
    InitStateResponse response = new InitStateResponse();
    Long currentUserId = CurrentUserHolder.get();
    SessionInfo sessionInfo = SessionManager.getSessionInfo(currentUserId);
    response.setUser(sessionInfo.getUserSession());  // 返回的是 UserSessionVo
    return response;
}
```

### SessionInfo 结构

```java
// SessionInfo.java
public class SessionInfo {
    private UserSessionVo userSession;  // 给前端展示
    private User currentUser;           // 给后端操作
}
```

---

## 根因

```
SessionInfo {
    userSession: UserSessionVo { currentBookId: 1 }   ← setCurrentBook() 没有更新
    currentUser: User           { currentBookId: 2 }   ← setCurrentBook() 更新了
}
```

`setCurrentBook()` 只更新了 `SessionInfo.currentUser`（JPA 实体），没有同步更新 `SessionInfo.userSession`（UserSessionVo）。而 `getInitState()` 返回的是 `userSession`，所以前端始终拿到登录时的旧值。

**本质问题：`SessionInfo` 内部有两份 `currentBookId` 副本，写入路径只覆盖了一份，读取路径恰好读的是没覆盖的那份。**

---

## 候选方案

### 方案 A：setCurrentBook 末尾补同步

```java
// UserService.java
public void setCurrentBook(Long bookId) {
    User user = SessionManager.getCurrentUser();
    user.setCurrentBookId(bookId);
    userRepository.save(user);
    // 新增这一行
    SessionManager.getSessionInfo(user.getId())
        .getUserSession().setCurrentBookId(bookId);
}
```

- **优点**：改动最小，一行代码
- **缺点**：治标不治本。以后任何地方更新 `User` 实体上跟 session 相关的字段（比如将来加了 `photo`、`username` 的运行时修改），都需要记得同步 `UserSessionVo`。`UserSessionVo` 和 `User` 实体的双写问题始终存在。

### 方案 B：getInitState 从 User 实体重建 UserSessionVo

```java
// UserService.java
public InitStateResponse getInitState() {
    InitStateResponse response = new InitStateResponse();
    Long currentUserId = CurrentUserHolder.get();
    SessionInfo sessionInfo = SessionManager.getSessionInfo(currentUserId);
    User user = sessionInfo.getCurrentUser(); // 从 JPA 实体读取最新数据
    UserSessionVo vo = new UserSessionVo();
    vo.setUsername(user.getUsername());
    vo.setPhoto(user.getPhoto());
    vo.setCurrentBookId(user.getCurrentBookId());
    response.setUser(vo);
    return response;
}
```

- **优点**：`UserSessionVo` 不再作为持久快照存在，每次返回都是实时数据。消除双写风险。
- **缺点**：
  - 每次 `initState` 都要从 `User` 实体重新构建 VO（但 `User` 已在内存，非 DB 查询，成本极低）
  - `UserSessionVo` 在 `SessionInfo` 里变成多余字段，或者只用于首次构建后缓存

### 方案 C：SessionInfo 只存 User 实体，去掉 UserSessionVo

删掉 `SessionInfo.userSession` 字段，`getInitState()` 直接从 `currentUser` 构建 VO 返回。`SessionManager.createSession()` 也不再需要创建 `UserSessionVo`。

- **优点**：单一数据源，从根本上消除双写
- **缺点**：
  - `UserSessionVo` 可能在其他地方被使用（需要确认）
  - `SessionManager.getSessionInfo()` 的调用方如果有依赖 `userSession` 的，都需改造

### 方案 D：setCurrentBook 后从 DB 重新查 User 并重建 session

```java
public void setCurrentBook(Long bookId) {
    User user = SessionManager.getCurrentUser();
    user.setCurrentBookId(bookId);
    userRepository.save(user);
    // 重新查库拿到最新 User，重建 SessionInfo
    User refreshed = userRepository.findById(user.getId()).orElseThrow(...);
    SessionManager.createSession(refreshed);  // 重建 UserSessionVo
}
```

- **优点**：`UserSessionVo` 被整体重建，一劳永逸地同步所有字段
- **缺点**：
  - 额外一次 DB 查询
  - `createSession` 会重新生成 token（当前实现的副作用），需要拆出一个不生成 token 的 refresh 方法
  - 如果后续 User 实体有字段变更（如 JPA 关联的 lazy 加载），重建的 VO 可能触发意外查询

---

## 补充上下文

- 缓存是纯内存 `ConcurrentHashMap`，无 Redis/外部缓存
- `AuthInterceptor` 通过 token 查到 `userId` 后放入 `CurrentUserHolder`（ThreadLocal）
- `Book` 实体代表账本，`User.currentBookId` 是「当前正在看的账本 ID」，存储冗余在 `t_user` 表
- 项目结构：Spring Boot 4.1.0 + Java 21 + JPA/Hibernate
