package cn.biq.mn.book.tpl;

import cn.biq.mn.base.IdAndNameDetails;
import cn.biq.mn.bean.ApplicationScopeBean;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.utils.WebUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


// https://juejin.cn/post/7126843761975853069
@RestController
@RequestMapping("/book-templates")
@RequiredArgsConstructor
public class BookTemplateController {

    private final ApplicationScopeBean applicationScopeBean;

    @RequestMapping(value="", method = RequestMethod.GET)
    public BaseResponse handleBookTemplates() {
        List<BookTemplate> bookTplList = applicationScopeBean.getBookTplList();
        bookTplList = bookTplList.stream().filter(b -> b.getLang().equals(WebUtils.getAcceptLang())).collect(Collectors.toList());
        return new DataResponse<>(bookTplList);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll() {
        List<BookTemplate> bookTplList = applicationScopeBean.getBookTplList();
        bookTplList = bookTplList.stream().filter(b -> b.getLang().equals(WebUtils.getAcceptLang())).collect(Collectors.toList());
        var list = bookTplList.stream().map(i -> new IdAndNameDetails(i.getId(), i.getName())).toList();
        return new DataResponse<>(list);
    }

}
