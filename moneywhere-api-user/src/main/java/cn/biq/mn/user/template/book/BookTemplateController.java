package cn.biq.mn.user.template.book;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;

@RestController
@RequestMapping("/book-templates")
@RequiredArgsConstructor
public class BookTemplateController {

    private final BookTemplateService bookService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(Pageable page, BookTemplateQueryForm form) {
        return new PageResponse<>(bookService.query(page, form));
    }

}
