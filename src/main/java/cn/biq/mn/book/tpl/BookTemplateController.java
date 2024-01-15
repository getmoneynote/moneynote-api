package cn.biq.mn.book.tpl;

import cn.biq.mn.bean.ApplicationScopeBean;
import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// https://juejin.cn/post/7126843761975853069
@RestController
@RequiredArgsConstructor
public class BookTemplateController {

    private final ApplicationScopeBean applicationScopeBean;

    @RequestMapping(value="/book-templates", method = RequestMethod.GET)
    public BaseResponse handleBookTemplates() {
        return new DataResponse<>(applicationScopeBean.getBookTplList());
    }

}
