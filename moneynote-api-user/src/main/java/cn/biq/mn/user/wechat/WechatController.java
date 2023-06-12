package cn.biq.mn.user.wechat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.DataResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WechatController {

    private final WeChatConfig weChatConfig;
    private final WeChatService weChatService;
    private final HttpServletRequest httpServletRequest;

    /**
     * 拼装微信扫一扫登录url
     */
    @GetMapping(value = "/loginWechat/url")
    public BaseResponse loginUrl() {
        String callbackUrl = URLEncoder.encode(weChatConfig.getOpenRedirectUrl(), StandardCharsets.UTF_8); //进行编码
        String state = UUID.randomUUID().toString().replaceAll("-", "");
        return new DataResponse<>(String.format(WeChatConfig.OPEN_QRCODE_URL, weChatConfig.getOpenAppid(), callbackUrl, state));
    }

    /**
     * 用户授权成功，获取微信回调的code
     */
    @RequestMapping(value = "/loginWechat/callback", method = { RequestMethod.GET, RequestMethod.POST })
    public void wechatUserCallback(@RequestParam(value = "code", required = true) String code, String state, HttpServletResponse response) throws IOException {
        String token = weChatService.saveWeChatUser(code, false);
        httpServletRequest.getSession().setAttribute("accessToken", token);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("<script>window.opener.postMessage('" + token + "', window.location)</script>");
        out.close();
    }

    @RequestMapping(value = "/loginWechat/appCallback", method = { RequestMethod.GET, RequestMethod.POST })
    public BaseResponse wechatUserCallbackApp(@RequestParam(value = "code",required = true) String code, String state) {
        String token = weChatService.saveWeChatUser(code, true);
        return new DataResponse<>(token);
    }

}
