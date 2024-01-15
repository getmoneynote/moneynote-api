package cn.biq.mn.book.tpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

// https://juejin.cn/post/7126843761975853069
@RestController
@RequiredArgsConstructor
public class BookTemplateController {

    @Value("${user_api_base_url}")
    private String userApiBaseUrl;
    private final RestTemplate restTemplate;

    @RequestMapping(value="/book-templates", method = RequestMethod.GET)
    public ResponseEntity<String> handleBookTemplates(HttpServletRequest request) {
        String query = request.getQueryString();
        String target = userApiBaseUrl + "book-templates";
        if (StringUtils.hasText(query)) {
            target = target + "?" + query;
        }
        return restTemplate.getForEntity(target, String.class);
    }

}
