package cn.biq.mn.admin.booktemplate.book;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;

@RestController
@RequestMapping("/book-templates")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody BookAddForm form) {
        return new BaseResponse(bookService.add(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id,  @Valid @RequestBody BookAddForm form) {
        return new BaseResponse(bookService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(Pageable page, BookQueryForm form) {
        return new PageResponse<>(bookService.query(page, form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.remove(id));
    }

}
