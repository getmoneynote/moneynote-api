package cn.biq.mn.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import cn.biq.mn.base.base.BaseRepositoryImpl;


@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = "cn.biq.mn")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class, enableDefaultTransactions = true)
//@EnableTransactionManagement
//https://stackoverflow.com/questions/40724100/enabletransactionmanagement-in-spring-boot
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
