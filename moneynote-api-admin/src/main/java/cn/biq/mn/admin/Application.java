package cn.biq.mn.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import cn.biq.mn.base.base.BaseRepositoryImpl;

@SpringBootApplication
@ComponentScan(basePackages = "cn.biq.mn")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class, enableDefaultTransactions = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
