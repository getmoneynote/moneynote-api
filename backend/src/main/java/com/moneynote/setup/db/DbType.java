package com.moneynote.setup.db;

import lombok.Getter;

public enum DbType {

    MYSQL(
            "MySQL",
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false",
            "SELECT 1",
            3306,
            true,
            true
    ),
    POSTGRESQL(
            "PostgreSQL",
            "org.postgresql.Driver",
            "jdbc:postgresql://%s:%d/%s",
            "SELECT 1",
            5432,
            true,
            true
    ),
    H2(
            "H2 (内嵌)",
            "org.h2.Driver",
            "jdbc:h2:file:./data/%s;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE;TRACE_LEVEL_FILE=0",
            "SELECT 1",
            0,
            false,
            false
    );

    @Getter
    private final String label;

    @Getter
    private final String driverClassName;

    @Getter
    private final String urlTemplate;

    @Getter
    private final String testSql;

    @Getter
    private final int defaultPort;

    /** 是否需要主机地址+端口(如 MySQL/PG;H2 内嵌模式不需要) */
    @Getter
    private final boolean needsHost;

    /** 是否需要用户填写数据库账号密码(H2 用固定默认账号,不需要) */
    @Getter
    private final boolean needsCred;

    DbType(String label, String driverClassName, String urlTemplate, String testSql, int defaultPort,
           boolean needsHost, boolean needsCred) {
        this.label = label;
        this.driverClassName = driverClassName;
        this.urlTemplate = urlTemplate;
        this.testSql = testSql;
        this.defaultPort = defaultPort;
        this.needsHost = needsHost;
        this.needsCred = needsCred;
    }

    public String buildJdbcUrl(String host, int port, String dbName) {
        if (this == H2) {
            // H2 文件模式:host/port 无意义,dbName 即数据文件名(位于 ./data 目录下)
            return String.format(urlTemplate, dbName);
        }
        return String.format(urlTemplate, host, port, dbName);
    }

}
