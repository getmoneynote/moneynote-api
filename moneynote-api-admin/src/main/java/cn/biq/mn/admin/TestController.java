package cn.biq.mn.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;

@RestController
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "/test1")
    public BaseResponse handleTest1() {
        return new DataResponse<>(31);
    }

}
