package cn.biq.mn.admin.rbac.admin;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @RequestMapping(method = RequestMethod.POST, value = "")
    public BaseResponse handleAdd(@Valid @RequestBody AdminAddForm form) {
        return new BaseResponse(adminService.add(form));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public BaseResponse handleLogin(@Valid @RequestBody LoginForm form) {
        return new DataResponse<>(adminService.login(form));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/initState")
    public BaseResponse handleInitState() {
        return new DataResponse<>(adminService.getInitState());
    }

}
