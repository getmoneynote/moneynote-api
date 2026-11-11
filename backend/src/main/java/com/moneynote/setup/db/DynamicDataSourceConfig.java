package com.moneynote.setup.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "app.db.configured", havingValue = "true")
public class DynamicDataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws IOException {
        DbConfig config = DbConfigHolder.load();

        HikariConfig hikari = new HikariConfig();
        System.out.println(config.getDbType().getDriverClassName());
        hikari.setDriverClassName(config.getDbType().getDriverClassName());
        hikari.setJdbcUrl(config.buildJdbcUrl());
        hikari.setUsername(config.getUsername());
        hikari.setPassword(config.getPassword());
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(2);
        hikari.setConnectionTimeout(10_000);

//        // 启动时测试数据库连接
//        hikari.setConnectionTestQuery("SELECT 1");
//        // 初始化失败立即报错
//        hikari.setInitializationFailTimeout(5000);

        return new HikariDataSource(hikari);
    }

}