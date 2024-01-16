package cn.biq.mn.book;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.response.PageResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController extends BaseController {

    private final BookService bookService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(
            BookQueryForm form,
            @PageableDefault(sort = "sort", direction = Sort.Direction.ASC) Pageable page
    ) {
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

    @RequestMapping(method = RequestMethod.POST, value = "/copy")
    public BaseResponse handleAddByBook(@Valid @RequestBody BookAddByBookForm form) {
        return new BaseResponse(bookService.addByBook(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/export")
    public void handleExport(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
        Workbook workbook = bookService.exportFlow(id);
        // 设置 HTTP 响应头
        response.setContentType("application/vnd.ms-excel");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        response.setHeader("Content-disposition", "attachment; filename=users_" + currentDateTime + ".xlsx");
        // 将工作簿写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
