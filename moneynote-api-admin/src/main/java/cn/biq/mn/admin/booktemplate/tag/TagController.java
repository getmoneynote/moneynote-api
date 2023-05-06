package cn.biq.mn.admin.booktemplate.tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;


@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(@Valid TagQueryForm form) {
        return new DataResponse<>(tagService.query(form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody TagAddForm form) {
        return new BaseResponse(tagService.add(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id,  @Valid @RequestBody TagUpdateForm form) {
        return new BaseResponse(tagService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(tagService.remove(id));
    }

}
