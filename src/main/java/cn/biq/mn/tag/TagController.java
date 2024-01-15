package cn.biq.mn.tag;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tags")
@Validated
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(TagQueryForm form) {
        return new DataResponse<>(tagService.query(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public BaseResponse handleAll(TagQueryForm form) {
        return new DataResponse<>(tagService.queryAll(form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody TagAddForm request) {
        return new BaseResponse(tagService.add(request));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggle")
    public BaseResponse handleToggle(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.toggle(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanExpense")
    public BaseResponse handleToggleCanExpense(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.toggleCanExpense(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanIncome")
    public BaseResponse handleToggleCanIncome(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.toggleCanIncome(id));
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/{id}/toggleCanTransfer")
    public BaseResponse handleToggleCanTransfer(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.toggleCanTransfer(id));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody TagUpdateForm form) {
        return new BaseResponse(tagService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.remove(id));
    }

}
