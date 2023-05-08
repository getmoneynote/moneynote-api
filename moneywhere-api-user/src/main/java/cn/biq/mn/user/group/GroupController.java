package cn.biq.mn.user.group;

import cn.biq.mn.base.base.BaseController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController extends BaseController {

    private final GroupService groupService;

    @RequestMapping(method = RequestMethod.GET, value = "")
    public BaseResponse handleQuery(Pageable page) {
        return new PageResponse<>(groupService.query(page));
    }

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody GroupAddForm form) {
        return new BaseResponse(groupService.add(form));
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{id}")
    public BaseResponse handleUpdate(@PathVariable("id") Integer id, @Valid @RequestBody GroupUpdateForm form) {
        return new BaseResponse(groupService.update(id, form));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public BaseResponse handleDelete(@PathVariable("id") Integer id) {
        return new BaseResponse(groupService.remove(id));
    }

}
