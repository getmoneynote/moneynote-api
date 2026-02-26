package cn.biq.mn;


import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SqlScriptRunner implements ApplicationRunner  {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
//        try {
//            Resource resource = new ClassPathResource("1.sql");
//            String script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
//            // 在某些情况下，数据库可能不支持在单个语句中执行多个 SQL 命令
//            // jdbcTemplate.execute(script);
//            String[] sqlStatements = script.split(";");
//            for (String sql : sqlStatements) {
//                if (!sql.trim().isEmpty()) {
//                    jdbcTemplate.execute(sql);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
