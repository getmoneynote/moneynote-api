package com.moneynote.setup.db;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Getter @Setter
public class DbConfig {

    private DbType dbType;
    private String host;
    private Integer port;
    private String dbName;
    private String username;
    private String password;   // 内存中明文,落盘前加密
    private Map<String, String> extraParams; // 额外连接参数,预留扩展

    public String buildJdbcUrl() {
        if (dbType == DbType.H2) {
            // H2 文件模式:确保 ./data 目录存在,否则 H2 无法创建数据库文件
            try {
                Files.createDirectories(Paths.get("./data"));
            } catch (IOException ignored) {
                // 目录已存在或权限不足,交由连接时报错
            }
        }
        int p = (port != null) ? port : dbType.getDefaultPort();
        return dbType.buildJdbcUrl(host, p, dbName);
    }

}