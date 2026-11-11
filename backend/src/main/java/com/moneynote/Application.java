package com.moneynote;

import com.moneynote.setup.db.DbConfigHolder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        boolean configured = DbConfigHolder.isConfigured();
        Class<?> bootstrapClass = configured
                ? Application.class
                : SetupApplication.class;
        Map<String, Object> props = new HashMap<>();
        props.put("app.db.configured", configured);
        if (!configured) {
            // 只有 setup 模式才排除数据源/JPA 相关自动配置;
            // 正式模式不设置该属性,保证 JPA/Repository 正常注册
            props.put("spring.autoconfigure.exclude", String.join(",",
                    DataSourceAutoConfiguration.class.getName(),
                    HibernateJpaAutoConfiguration.class.getName(),
                    DataSourceTransactionManagerAutoConfiguration.class.getName(),
                    DataJpaRepositoriesAutoConfiguration.class.getName()));
        }
        new SpringApplicationBuilder(bootstrapClass)
                .properties(props)
                .run(args);
    }

}
