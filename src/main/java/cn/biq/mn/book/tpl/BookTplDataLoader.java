package cn.biq.mn.book.tpl;

import cn.biq.mn.bean.ApplicationScopeBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class BookTplDataLoader implements ApplicationRunner {

    private final ApplicationScopeBean applicationScopeBean;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Resource resource = new ClassPathResource("book_tpl.json");
            ObjectMapper objectMapper = new ObjectMapper();
            BookTemplate[] bookTemplateList = objectMapper.readValue(resource.getInputStream(), BookTemplate[].class);
            applicationScopeBean.setBookTplList(Arrays.asList(bookTemplateList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
