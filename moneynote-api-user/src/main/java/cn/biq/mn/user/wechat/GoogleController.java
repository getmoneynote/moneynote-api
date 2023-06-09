package cn.biq.mn.user.wechat;

import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class GoogleController {

    private final GoogleService googleService;
    private final HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/loginGoogle/callback", method = { RequestMethod.GET, RequestMethod.POST })
    public BaseResponse googleUserCallback(@RequestParam(value = "accessToken", required = true) String accessToken) {
        String token = googleService.saveGoogleUser(accessToken);
        httpServletRequest.getSession().setAttribute("accessToken", token);
        return new DataResponse<>(token);
    }

}
