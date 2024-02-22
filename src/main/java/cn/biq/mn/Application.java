package cn.biq.mn;

import cn.biq.mn.base.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
//@ComponentScan(basePackages = "cn.biq.mn")
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class, enableDefaultTransactions = true)
//@EnableTransactionManagement
//https://stackoverflow.com/questions/40724100/enabletransactionmanagement-in-spring-boot
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
