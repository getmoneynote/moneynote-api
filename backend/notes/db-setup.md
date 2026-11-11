# Spring Boot 首次启动数据库配置向导 —— 完整方案

参考 Jira / Confluence / GitLab 等产品"打开浏览器填数据库信息完成安装"的效果,实现一套支持
MySQL / PostgreSQL / Oracle 等多数据库的首次启动配置向导。

---

## 一、整体架构

### 1.1 核心思路

Spring Boot 应用启动分两种状态运行,通过外部配置文件是否存在来判断:

```
                      ┌─────────────────────┐
                      │   应用启动 main()     │
                      └──────────┬───────────┘
                                 │
                    检查外部配置文件是否存在且有效
                                 │
              ┌──────────────────┴──────────────────┐
              │否(首次启动)                    是(已配置)│
              ▼                                       ▼
   ┌─────────────────────┐              ┌─────────────────────────┐
   │ 排除 DataSource 相关   │              │  正常加载 DataSource      │
   │ 自动配置,只启动最小Web  │              │  JPA/MyBatis 等完整应用    │
   │ 服务 + /setup 向导接口 │              │                          │
   └──────────┬──────────┘              └─────────────────────────┘
              │
     用户在浏览器填写数据库信息
              │
        测试连接 → 保存配置文件
              │
        进程退出(特殊退出码)
              │
     外层脚本/容器捕获退出码 → 重启进程
              │
              └──────────────► 回到顶部,此时已配置,走右边分支
```

### 1.2 目录结构

```
myapp/
├── app.jar                          # 应用本体
├── start.sh                         # 启动脚本(带自动重启逻辑)
└── config/                          # 外部可写目录,不打包进 jar
    └── db-config.yml                # 用户填写后生成的数据库配置(密码加密存储)
```

**关键点**:配置文件必须放在 jar 包外部的可写目录,不能放在 `src/main/resources` 里,
否则容器重启、jar 包替换时配置会丢失。

---

## 二、启动阶段判断:main 方法与自动配置排除

```java
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);

        boolean configured = DbConfigHolder.isConfigured();

        if (!configured) {
            // 首次启动:排除数据源相关自动配置,避免因没有可用数据源导致启动失败
            Map<String, Object> props = new HashMap<>();
            props.put("spring.autoconfigure.exclude", String.join(",",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
                "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
            ));
            props.put("app.db.configured", false);
            builder.properties(props);
        } else {
            builder.properties("app.db.configured", "true");
        }

        ConfigurableApplicationContext ctx = builder.run(args);
    }
}
```

> 也可以用一个 `EnvironmentPostProcessor` 在 `ApplicationContext` 刷新之前把外部 yml
> 文件的内容整体加载进 Spring 的 `Environment`,这样 `DynamicDataSourceConfig` 里就能
> 直接用 `@Value` / `@ConfigurationProperties` 读取,写法更"Spring 原生"。见附录 A。

---

## 三、数据库类型抽象:DbType 枚举

统一维护"数据库类型 → 驱动类名 / URL 模板 / 方言 / 测试 SQL"的映射,新增数据库类型时
只需在这里加一项。

```java
public enum DbType {

    MYSQL(
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai",
        "org.hibernate.dialect.MySQL8Dialect",
        "SELECT 1",
        3306
    ),
    POSTGRESQL(
        "org.postgresql.Driver",
        "jdbc:postgresql://%s:%d/%s",
        "org.hibernate.dialect.PostgreSQLDialect",
        "SELECT 1",
        5432
    ),
    ORACLE_SID(
        "oracle.jdbc.OracleDriver",
        "jdbc:oracle:thin:@%s:%d:%s",          // %s = SID
        "org.hibernate.dialect.OracleDialect",
        "SELECT 1 FROM DUAL",
        1521
    ),
    ORACLE_SERVICE_NAME(
        "oracle.jdbc.OracleDriver",
        "jdbc:oracle:thin:@//%s:%d/%s",        // %s = Service Name
        "org.hibernate.dialect.OracleDialect",
        "SELECT 1 FROM DUAL",
        1521
    );

    private final String driverClassName;
    private final String urlTemplate;
    private final String hibernateDialect;
    private final String testSql;
    private final int defaultPort;

    DbType(String driverClassName, String urlTemplate, String hibernateDialect,
           String testSql, int defaultPort) {
        this.driverClassName = driverClassName;
        this.urlTemplate = urlTemplate;
        this.hibernateDialect = hibernateDialect;
        this.testSql = testSql;
        this.defaultPort = defaultPort;
    }

    public String buildJdbcUrl(String host, int port, String dbName) {
        return String.format(urlTemplate, host, port, dbName);
    }

    // getters ...
}
```

---

## 四、数据库配置对象与持久化:DbConfig / DbConfigHolder

### 4.1 DbConfig

```java
public class DbConfig {
    private DbType dbType;
    private String host;
    private Integer port;
    private String dbName;
    private String username;
    private String password;   // 内存中明文,落盘前加密
    private Map<String, String> extraParams; // 额外连接参数,预留扩展

    public String buildJdbcUrl() {
        int p = (port != null) ? port : dbType.getDefaultPort();
        return dbType.buildJdbcUrl(host, p, dbName);
    }

    // getters/setters ...
}
```

### 4.2 DbConfigHolder —— 配置文件的读写与加密

```java
@Component
public class DbConfigHolder {

    private static final Path CONFIG_PATH = Paths.get(
        System.getProperty("app.config.dir", "./config"), "db-config.yml"
    );

    /** 判断是否已完成配置(文件存在且能被正确解析) */
    public static boolean isConfigured() {
        if (!Files.exists(CONFIG_PATH)) return false;
        try {
            DbConfig cfg = load();
            return cfg != null && cfg.getDbType() != null
                    && StringUtils.hasText(cfg.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    public static DbConfig load() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            Map<String, Object> raw = yaml.load(in);
            DbConfig cfg = mapToDbConfig(raw);
            cfg.setPassword(AesUtil.decrypt(cfg.getPassword())); // 落盘是密文,读出后解密
            return cfg;
        }
    }

    public static void save(DbConfig config) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());

        DbConfig toSave = copy(config);
        toSave.setPassword(AesUtil.encrypt(config.getPassword())); // 加密后落盘

        Yaml yaml = new Yaml();
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            yaml.dump(dbConfigToMap(toSave), writer);
        }

        // 文件权限收紧,仅所有者可读写(Linux/Mac 下有效)
        try {
            Files.setPosixFilePermissions(CONFIG_PATH,
                PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) { /* Windows 下忽略 */ }
    }
}
```

**AES 密钥**建议放在环境变量(如 `APP_SECRET_KEY`)里,不要硬编码在代码或配置文件中,
避免密钥和密文一起泄露。

---

## 五、Setup 向导接口:SetupController

```java
@RestController
@RequestMapping("/setup")
public class SetupController {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    /** 已配置完成后,禁止再访问向导接口,防止生产环境被重新篡改 */
    @ModelAttribute
    public void guard() {
        if (DbConfigHolder.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "系统已完成初始化配置");
        }
    }

    @GetMapping
    public ResponseEntity<Resource> setupPage() {
        // 返回静态的配置向导页面(见第六节前端示例)
        Resource html = new ClassPathResource("setup/index.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    /** 测试连接:绕开 Spring 的 DataSource,用原生 JDBC 直接测 */
    @PostMapping("/test")
    public ResponseEntity<?> testConnection(@RequestBody DbConfig config) {
        try {
            Class.forName(config.getDbType().getDriverClassName());
        } catch (ClassNotFoundException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "驱动未加载: " + e.getMessage()));
        }

        try (Connection conn = DriverManager.getConnection(
                config.buildJdbcUrl(), config.getUsername(), config.getPassword())) {

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(config.getDbType().getTestSql());
            }
            return ResponseEntity.ok(Map.of("success", true, "message", "连接成功"));

        } catch (SQLException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "连接失败: " + e.getMessage()
            ));
        }
    }

    /** 保存配置并触发重启 */
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody DbConfig config) {
        try {
            DbConfigHolder.save(config);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("保存失败: " + e.getMessage());
        }

        // 异步延迟退出,确保这次 HTTP 响应能先返回给浏览器
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            int exitCode = SpringApplication.exit(applicationContext, () -> 88);
            System.exit(exitCode);
        }).start();

        return ResponseEntity.ok(Map.of("message", "配置成功,应用正在重启,请稍后刷新页面"));
    }

    @GetMapping("/db-types")
    public ResponseEntity<?> supportedDbTypes() {
        return ResponseEntity.ok(Arrays.stream(DbType.values())
            .map(t -> Map.of(
                "code", t.name(),
                "defaultPort", t.getDefaultPort()
            ))
            .toList());
    }
}
```

### 为什么不"热切换"数据源?

理论上可以在同一个 JVM 里动态创建新的 `DataSource`、重新初始化 Hibernate 的
`SessionFactory`、刷新 `ApplicationContext` 里跟数据层相关的所有 Bean,但实践中坑非常多:

- Hibernate 的 `EntityManagerFactory` 一旦创建,元数据(实体映射、方言等)基本认为是不可变的,强行重建容易引发内存泄漏或状态不一致。
- 连接池(HikariCP)、事务管理器、`@Transactional` AOP 代理都跟当时的 `DataSource` Bean 绑定,动态替换涉及到大量 Bean 的重新装配。
- 出错后很难回滚到"干净"状态,只能重启进程兜底。

所以**工程上更稳妥的做法是:保存配置后直接退出进程,交给外部机制重启**,让 Spring 走一次完整、干净的启动流程。

---

## 六、进程重启机制

### 6.1 Shell 脚本方式(裸机/虚拟机部署)

```bash
#!/bin/bash
# start.sh
APP_JAR="app.jar"
RESTART_EXIT_CODE=88

while true; do
    java -jar "$APP_JAR"
    EXIT_CODE=$?

    if [ $EXIT_CODE -eq $RESTART_EXIT_CODE ]; then
        echo "检测到配置更新,正在重启应用..."
        sleep 1
        continue
    else
        echo "应用已退出,退出码: $EXIT_CODE"
        break
    fi
done
```

用 `nohup ./start.sh &` 或注册为 systemd 服务(配合 `Restart=on-failure` 也可以,
但更推荐用上面这种"脚本层控制循环"的方式,语义更明确)。

### 6.2 Docker 方式

`Dockerfile` 里的 `ENTRYPOINT` 换成上面的 `start.sh`,或者更简单地:直接在
`docker-compose.yml` 里设置

```yaml
services:
  myapp:
    image: myapp:latest
    restart: always     # 容器退出后 Docker 自动重启,退出码非 0/88 都会重启
    volumes:
      - ./config:/app/config   # 外部配置目录挂载,保证重启后配置文件还在
```

配合应用内 `System.exit(88)`,容器退出后 Docker 自动重启,重启后再次检测到配置文件已存在,直接进入正常启动流程。

### 6.3 Kubernetes 方式

Pod 的重启策略默认 `Restart: Always`,原理相同;需要注意把 `config/` 目录挂载为
`PersistentVolume` 或 `ConfigMap`(如果密码走加密存储,也可以用 `Secret`),否则 Pod
重建后配置会丢失。

---

## 七、已配置状态下的 DataSource 装配

```java
@Configuration
@ConditionalOnProperty(name = "app.db.configured", havingValue = "true")
public class DynamicDataSourceConfig {

    @Bean
    public DataSource dataSource() throws IOException {
        DbConfig config = DbConfigHolder.load();

        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName(config.getDbType().getDriverClassName());
        hikari.setJdbcUrl(config.buildJdbcUrl());
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10_000);

        return new HikariDataSource(hikari);
    }

    // 如果用 JPA,还需要根据 dbType 动态设置 hibernate.dialect
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource)
            throws IOException {
        DbConfig config = DbConfigHolder.load();

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.myapp.domain");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.dialect", config.getDbType().getHibernateDialect());
        jpaProps.put("hibernate.hbm2ddl.auto", "update"); // 首次建表,后续建议改为 validate
        emf.setJpaPropertyMap((Map) jpaProps);

        return emf;
    }
}
```

如果用的是 **MyBatis / MyBatis-Plus**,不需要处理 Hibernate 方言,只需要保证
`DataSource` 装配正确,`SqlSessionFactory` 会自动跟着走;不同数据库的 SQL 语法差异
需要在 Mapper XML 层面自己兼容(或用 MyBatis-Plus 的多方言支持)。

---

## 八、前端向导页面(简化示例)

一个最简单的原生 HTML + JS 页面即可,放在 `resources/setup/index.html`,首次启动时
不依赖任何前端构建工具,保证"最小可用系统"里也能跑起来。

```html
<!DOCTYPE html>
<html lang="zh">
<head><meta charset="UTF-8"><title>系统初始化</title></head>
<body>
  <h2>数据库配置向导</h2>
  <form id="setupForm">
    <select id="dbType">
      <option value="MYSQL">MySQL</option>
      <option value="POSTGRESQL">PostgreSQL</option>
      <option value="ORACLE_SID">Oracle (SID)</option>
      <option value="ORACLE_SERVICE_NAME">Oracle (Service Name)</option>
    </select>
    <input id="host" placeholder="主机地址" value="127.0.0.1" />
    <input id="port" placeholder="端口(留空使用默认)" />
    <input id="dbName" placeholder="数据库名 / SID / Service Name" />
    <input id="username" placeholder="用户名" />
    <input id="password" type="password" placeholder="密码" />
    <button type="button" onclick="testConn()">测试连接</button>
    <button type="button" onclick="save()">保存并启动</button>
  </form>
  <div id="result"></div>

<script>
function collect() {
  return {
    dbType: dbType.value,
    host: host.value,
    port: port.value ? parseInt(port.value) : null,
    dbName: dbName.value,
    username: username.value,
    password: password.value
  };
}

async function testConn() {
  const res = await fetch('/setup/test', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(collect())
  });
  const data = await res.json();
  result.innerText = data.message;
}

async function save() {
  const res = await fetch('/setup/save', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(collect())
  });
  const data = await res.json();
  result.innerText = data.message + '(请等待约 5 秒后刷新页面)';
}
</script>
</body>
</html>
```

---

## 九、Maven 依赖(多数据库驱动全部打包)

```xml
<dependencies>
    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Oracle: Maven Central 上是官方托管的 ojdbc,可直接引入 -->
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc11</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>

    <dependency>
        <groupId>org.yaml</groupId>
        <artifactId>snakeyaml</artifactId>
    </dependency>
</dependencies>
```

> Oracle 的 `ojdbc11` 目前已在 Maven Central 有官方坐标,不需要额外私服;但要注意
> Oracle 官方协议对分发有一定限制,商业分发前建议核实许可证条款。

---

## 十、安全性 checklist

- [ ] 数据库密码落盘前用 AES 加密,密钥放环境变量,不进代码库
- [ ] `db-config.yml` 文件权限收紧为仅所有者可读写
- [ ] `/setup/**` 接口在配置完成后自动返回 403,防止生产环境被重新篡改
- [ ] 测试连接接口(`/setup/test`)要做超时控制(`connectTimeout`),避免恶意/错误地址导致线程长时间挂起
- [ ] 生产环境建议关闭 Hibernate 的 `hbm2ddl.auto=update`,改成 `validate`,首次建表用独立的迁移脚本(Flyway/Liquibase)而不是 JPA 自动建表
- [ ] 日志中不要打印明文密码(包括测试连接失败时的异常堆栈,注意 JDBC URL 中不要拼密码)

---

## 附录 A:用 EnvironmentPostProcessor 替代手动 @Value 读取

如果希望在 `@ConditionalOnProperty` 之外,还能用 `@ConfigurationProperties(prefix = "app.db")`
这种更"Spring 原生"的方式读取配置,可以实现:

```java
public class DbConfigEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                        SpringApplication application) {
        if (!DbConfigHolder.isConfigured()) return;

        try {
            DbConfig config = DbConfigHolder.load();
            Map<String, Object> map = new HashMap<>();
            map.put("app.db.type", config.getDbType().name());
            map.put("app.db.url", config.buildJdbcUrl());
            map.put("app.db.username", config.getUsername());
            map.put("app.db.password", config.getPassword());
            map.put("app.db.configured", true);

            environment.getPropertySources()
                .addFirst(new MapPropertySource("externalDbConfig", map));
        } catch (Exception e) {
            // 加载失败时静默处理,后续会话按"未配置"逻辑走
        }
    }
}
```

注册方式:在 `src/main/resources/META-INF/spring.factories` 里加一行

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
com.example.myapp.config.DbConfigEnvironmentPostProcessor
```

这样 `DynamicDataSourceConfig` 里就可以直接用 `@Value("${app.db.url}")` 等方式取值,
不需要每次手动 `DbConfigHolder.load()`。

---

## 十一、整体验收流程(Checklist)

1. 全新部署,不存在 `config/db-config.yml`
2. 访问任意路径自动跳转 / 提示到 `/setup`
3. 选择数据库类型,填写连接信息,点"测试连接"验证可用性
4. 点"保存并启动",配置写入 `config/db-config.yml`(密码已加密)
5. 应用退出(exit code 88),外层脚本/容器自动重启进程
6. 重启后检测到配置文件存在,正常加载 `DataSource`、JPA/MyBatis,应用进入正常服务状态
7. 再次访问 `/setup` 应返回 403