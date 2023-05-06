package cn.biq.mn.admin.booktemplate.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;


@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(@Valid CategoryQueryForm form) {
        return new DataResponse<>(categoryService.query(form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody CategoryAddForm form) {
        return new BaseResponse(categoryService.add(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id,  @Valid @RequestBody CategoryUpdateForm form) {
        return new BaseResponse(categoryService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(categoryService.remove(id));
    }

}
