package cn.biq.mn.book.tpl;

import cn.biq.mn.bean.ApplicationScopeBean;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


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
