package cn.biq.mn.user.book;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.base.BaseController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import cn.biq.mn.base.response.DataResponse;


@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController extends BaseController {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(BookQueryForm form, Pageable page) {
        return new PageResponse<>(bookService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public BaseResponse handleGet(@PathVariable("id") Integer id) {
        return new DataResponse<>(bookService.get(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody BookAddForm form) {
        return new BaseResponse(bookService.add(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(BookQueryForm form) {
        return new DataResponse<>(bookService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody BookUpdateForm form) {
        return new BaseResponse(bookService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.remove(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(bookService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/template")
    public BaseResponse handleAddByTemplate(@Valid @RequestBody BookAddByTemplateForm form) {
        return new BaseResponse(bookService.addByTemplate(form));
    }

}
