package cn.biq.mn.user.wechat;

import cn.biq.mn.user.base.BaseEntityRepository;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookAddByTemplateForm;
import cn.biq.mn.user.book.BookService;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.user.UserGroupRelation;
import cn.biq.mn.user.user.UserRepository;
import cn.biq.mn.user.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.base.utils.WebUtils;
import cn.biq.mn.user.security.JwtUtils;

import java.util.HashMap;

@Service
@Transactional
@RequiredArgsConstructor
public class WeChatService {

    private final WeChatConfig weChatConfig;
    private final WebUtils webUtils;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final SessionUtil sessionUtil;
    private final BaseEntityRepository baseEntityRepository;
    private final BookService bookService;

    public String saveWeChatUser(String code, boolean isApp) {
        String accessTokenUrl = String.format(WeChatConfig.OPEN_ACCESS_TOKEN_URL, weChatConfig.getOpenAppid(), weChatConfig.getOpenAppsecret(), code);
        if (isApp) {
            accessTokenUrl = String.format(WeChatConfig.OPEN_ACCESS_TOKEN_URL, weChatConfig.getOpenAppidApp(), weChatConfig.getOpenAppsecretApp(), code);
        }
        HashMap<String, Object> baseMap = webUtils.get(accessTokenUrl);
        String unionId = (String) baseMap.get("unionid");
        var userOptional = userRepository.findOneByUnionId(unionId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String jwt = jwtUtils.createToken(user);
            sessionUtil.clear();
            return jwt;
        } else {
            String accessToken = (String) baseMap.get("access_token");
            String openId = (String) baseMap.get("openid");

            String userInfoUrl = String.format(WeChatConfig.OPEN_USER_INFO_URL, accessToken, openId);
            //5、通过URL再去调微信接口获取用户基本信息
            HashMap<String, Object> baseUserMap = webUtils.get(userInfoUrl);

            User user = new User();
            user.setUnionId(unionId);
            user.setOpenId(openId);
            user.setNickName(baseUserMap.get("nickname").toString());
            user.setRegisterTime(System.currentTimeMillis());
            user.setRegisterIp(webUtils.getRequestIP());
            user.setHeadimgurl(baseUserMap.get("headimgurl").toString());
            // TODO 待优化，
            Group group = new Group();
            group.setName("默认组");
            group.setDefaultCurrencyCode("CNY");
            baseEntityRepository.save(group);
            user.setDefaultGroup(group);
            baseEntityRepository.save(user);
            group.setCreator(user);
            baseEntityRepository.save(group);
            UserGroupRelation userGroupRelation = new UserGroupRelation(user, group, 1);
            baseEntityRepository.save(userGroupRelation);
            baseEntityRepository.save(user);

            // 给默认账本
            var templateForm = new BookAddByTemplateForm();
            templateForm.setTemplateId(1);
            Book book = bookService.addByTemplate(templateForm, group);
            user.setDefaultBook(book);
            group.setDefaultBook(book);
            baseEntityRepository.save(user);
            baseEntityRepository.save(group);

            String jwt = jwtUtils.createToken(user);
            sessionUtil.clear();
            return jwt;

        }

    }


}
