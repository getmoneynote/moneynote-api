package cn.biq.mn.group;

import cn.biq.mn.base.BaseController;
import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import cn.biq.mn.response.PageResponse;
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

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/inviteUser")
    public BaseResponse handleInviteUser(@PathVariable("id") Integer id, @Valid @RequestBody InviteUserForm form) {
        return new BaseResponse(groupService.inviteUser(id, form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/removeUser")
    public BaseResponse handleRemoveUser(@PathVariable("id") Integer id, @Valid @RequestBody RemoveUserForm form) {
        return new BaseResponse(groupService.removeUser(id, form.getUserId()));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/agree")
    public BaseResponse handleInviteAgree(@PathVariable("id") Integer id) {
        return new BaseResponse(groupService.agreeInvite(id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}/reject")
    public BaseResponse handleInviteReject(@PathVariable("id") Integer id) {
        return new BaseResponse(groupService.rejectInvite(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}/users")
    public BaseResponse handleGetUsers(@PathVariable("id") Integer id) {
        return new DataResponse<>(groupService.getUsers(id));
    }

}
