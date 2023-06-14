package cn.biq.mn.user.wechat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// https://github.com/yudiandemingzi/spring-boot-wechat-login
@Component
@Getter
@Setter
public class WeChatConfig {

    /**
     * 开放平台appid
     */
    @Value("${wxopen_appid:''}")
    private String openAppid;

    /**
     * 开放平台appsecret
     */
    @Value("${wxopen_appsecret:''}")
    private String openAppsecret;


    /**
     * 开放平台回调url
     */
    @Value("${wxopen_redirect_url:''}")
    private String openRedirectUrl;

    @Value("${wxopen_appid_app:''}")
    private String openAppidApp;

    @Value("${wxopen_appsecret_app:''}")
    private String openAppsecretApp;


    /**
     * 微信开放平台二维码连接
     */
    public final static String OPEN_QRCODE_URL= "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect";


    /**
     * 开放平台获取access_token地址
     */
    public final static String OPEN_ACCESS_TOKEN_URL="https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";


    /**
     * 获取用户信息
     */
    public final static String OPEN_USER_INFO_URL ="https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

}
