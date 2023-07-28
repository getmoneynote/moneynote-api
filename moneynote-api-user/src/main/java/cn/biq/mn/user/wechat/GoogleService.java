package cn.biq.mn.user.wechat;

import cn.biq.mn.base.utils.WebUtils;
import cn.biq.mn.user.base.BaseEntityRepository;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookAddByTemplateForm;
import cn.biq.mn.user.book.BookService;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.security.JwtUtils;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.user.UserGroupRelation;
import cn.biq.mn.user.user.UserRepository;
import cn.biq.mn.user.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;


@Service
@Transactional
@RequiredArgsConstructor
public class GoogleService {

    public final static String baseUserInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final WebUtils webUtils;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final SessionUtil sessionUtil;
    private final BaseEntityRepository baseEntityRepository;
    private final BookService bookService;

    public String saveGoogleUser(String accessToken) {
        String userInfoUrl = baseUserInfoUrl + "?access_token=" + accessToken;
        HashMap<String, Object> baseMap = webUtils.get(userInfoUrl);
        String unionId = (String) baseMap.get("id");
        var userOptional = userRepository.findOneByUnionId(unionId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String jwt = jwtUtils.createToken(user);
            sessionUtil.clear();
            return jwt;
        } else {
            User user = new User();
            user.setUnionId(unionId);
            user.setNickName(baseMap.get("name").toString());
            user.setRegisterTime(System.currentTimeMillis());
            user.setRegisterIp(webUtils.getRequestIP());
            user.setHeadimgurl(baseMap.get("picture").toString());
            // TODO 待优化，
            Group group = new Group();
            group.setName("默认组");
            group.setDefaultCurrencyCode("USD");
            baseEntityRepository.save(group);
            user.setDefaultGroup(group);
            baseEntityRepository.save(user);
            group.setCreator(user);
            baseEntityRepository.save(group);
            UserGroupRelation userGroupRelation = new UserGroupRelation(user, group, 1);
            baseEntityRepository.save(userGroupRelation);
            baseEntityRepository.save(user);

            // 给默认账本
            Book book = bookService.addDefaultTemplate(group);
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
