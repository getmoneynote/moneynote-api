package cn.biq.mn.category;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/categories")
@Validated
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(
            CategoryQueryForm form,
            @PageableDefault(sort = "sort", direction = Sort.Direction.ASC) Pageable page
    ) {
        return new DataResponse<>(categoryService.query(form, page));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(CategoryQueryForm form) {
        return new DataResponse<>(categoryService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody CategoryAddForm request) {
        return new BaseResponse(categoryService.add(request));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(categoryService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody CategoryUpdateForm request) {
        return new BaseResponse(categoryService.update(id, request));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(categoryService.remove(id));
    }

}
