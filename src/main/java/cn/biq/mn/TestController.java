package cn.biq.mn;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


@RestController
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "/version")
    public BaseResponse handleVersion() {
        return new DataResponse<>(93.3);
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
