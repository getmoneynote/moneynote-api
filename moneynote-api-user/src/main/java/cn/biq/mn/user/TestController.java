package cn.biq.mn.user;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;

import java.util.Date;

@RestController
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "/test1")
    public BaseResponse handleTest1() {
        return new DataResponse<>(81);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test2")
    public BaseResponse handleTest2() {
        throw new RuntimeException(new Date().toString());
    }

    @GetMapping("/test3")
    public BaseResponse getBaseUrl(HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        return new DataResponse<>(baseUrl);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test@killyou765")
    public BaseResponse handleStop() {
        System.exit(0);
        return new DataResponse<>(61);
    }

}
