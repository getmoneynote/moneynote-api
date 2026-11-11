package com.moneynote.setup.db;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class DbConfigHolder {

    private static final Path CONFIG_PATH = Paths.get(
            System.getProperty("app.config.dir", "./config"), "db-config.properties"
    );

    /** 判断是否已完成配置(文件存在且能被正确解析) */
    public static boolean isConfigured() {
        if (!Files.exists(CONFIG_PATH)) {
            return false;
        }
        try {
            DbConfig cfg = load();
            if (cfg == null || cfg.getDbType() == null) {
                return false;
            }
            // H2 文件模式无主机地址,以数据文件名(库名)作为配置完成的判定依据
            if (cfg.getDbType() == DbType.H2) {
                return StringUtils.hasText(cfg.getDbName());
            }
            return StringUtils.hasText(cfg.getHost());
        } catch (Exception e) {
            return false;
        }
    }

    public static DbConfig load() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
            props.load(in);
        }

        DbConfig cfg = new DbConfig();
        cfg.setDbType(DbType.valueOf(props.getProperty("db.type")));
        cfg.setHost(props.getProperty("db.host"));

        String portStr = props.getProperty("db.port");
        cfg.setPort(StringUtils.hasText(portStr) ? Integer.parseInt(portStr) : null);

        cfg.setDbName(props.getProperty("db.name"));
        cfg.setUsername(props.getProperty("db.username"));
        cfg.setPassword(props.getProperty("db.password")); // 读出后解密
        return cfg;
    }

    public static void save(DbConfig config) throws IOException {
        Files.createDirectories(CONFIG_PATH.getParent());

        String content = """
            # MoneyNote 数据库配置
            # 由系统自动生成，请勿手动编辑密码字段

            db.type=%s
            db.host=%s
            db.port=%s
            db.name=%s
            db.username=%s
            db.password=%s
            """.formatted(
                config.getDbType().name(),
                config.getHost(),
                // H2 无端口:port 为空时落盘写成空串,避免 load() 解析 "null" 出错
                config.getPort() == null ? "" : config.getPort(),
                config.getDbName(),
                config.getUsername(),
                config.getPassword()
        );

        Files.writeString(
                CONFIG_PATH,
                content,
                StandardCharsets.UTF_8
        );

    }

}
