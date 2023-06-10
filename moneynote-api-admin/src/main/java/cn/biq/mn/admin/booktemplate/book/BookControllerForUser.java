package cn.biq.mn.admin.booktemplate.book;


import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-api/book-templates")
@RequiredArgsConstructor
public class BookControllerForUser {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQueryVisible(Pageable page, BookQueryForm form) {
        return new PageResponse<>(bookService.queryVisible(page, form));
    }

}
