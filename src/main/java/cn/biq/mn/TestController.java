package cn.biq.mn;

import cn.biq.mn.response.BaseResponse;
import cn.biq.mn.response.DataResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;


@RestController
public class TestController {

    @RequestMapping(method = RequestMethod.GET, value = "/version")
    public BaseResponse handleVersion() {
        return new DataResponse<>("93.45");
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

//    @RequestMapping(method = RequestMethod.GET, value = "/test@killyou765")
//    public BaseResponse handleStop() {
//        System.exit(0);
//        return new DataResponse<>(61);
//    }

    public static void main(String[] args) {
        Date ss = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        ZoneId zoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(8));
        TimeZone timeZone = TimeZone.getTimeZone(zoneId);
        sf.setTimeZone(timeZone);
        System.out.println(sf.format(ss));
    }

}
