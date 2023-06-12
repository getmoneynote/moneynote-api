package cn.biq.mn.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import cn.biq.mn.base.base.BaseRepositoryImpl;

import javax.sql.DataSource;

@SpringBootApplication
@ComponentScan(basePackages = "cn.biq.mn")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
