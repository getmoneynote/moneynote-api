package com.moneynote.setup;

import com.moneynote.setup.db.DbConfig;
import com.moneynote.setup.db.DbConfigHolder;
import com.moneynote.setup.db.DbType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/setup")
@ConditionalOnProperty(name = "app.db.configured", havingValue = "false")
public class SetupController {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    /**
     * 只对"会改变配置状态"的接口做拦截:test / save。
     * GET /setup 页面本身和 /setup/db-types 不受这个 guard 影响，
     * 因为它们本身不做任何危险操作，交给各自方法自己处理。
     */
    @ModelAttribute
    public void guard(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isMutatingEndpoint = uri.endsWith("/test") || uri.endsWith("/save");

        if (isMutatingEndpoint && DbConfigHolder.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "系统已完成初始化配置，禁止重新配置数据库");
        }
    }

    @GetMapping
    public Object setupPage() {
        if (DbConfigHolder.isConfigured()) {
            // 已配置：不允许再进入配置向导，直接送回首页
            return "redirect:/";
        }
        // 返回静态的配置向导页面(见第六节前端示例)
        Resource html = new ClassPathResource("setup/index.html");
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
//

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
//            System.out.println("[shutdown-debug] 即将调用 SpringApplication.exit ...");
//            int exitCode = SpringApplication.exit(applicationContext, () -> 88);
//            System.out.println("[shutdown-debug] SpringApplication.exit 返回码: {}" + exitCode);
//            System.out.println("[shutdown-debug] 即将 System.exit({})" + exitCode);   // <- 这行
////            System.exit(exitCode);
//            Runtime.getRuntime().halt(exitCode);
            System.exit(88);
        }).start();

        return ResponseEntity.ok(Map.of("message", "配置成功,应用正在重启,请稍后刷新页面"));
    }

    @GetMapping("/db-types")
    public ResponseEntity<?> supportedDbTypes() {
        return ResponseEntity.ok(Arrays.stream(DbType.values())
                .map(t -> Map.of(
                        "code", t.name(),
                        "label", t.getLabel(),
                        "defaultPort", t.getDefaultPort(),
                        "needsHost", t.isNeedsHost(),
                        "needsCred", t.isNeedsCred()
                ))
                .toList());
    }
}
