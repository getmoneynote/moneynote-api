package cn.biq.mn.user;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.utils.WebUtils;
import cn.biq.mn.base.BaseEntityRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.*;
import cn.biq.mn.group.Group;
import cn.biq.mn.group.GroupMapper;
import cn.biq.mn.group.GroupRepository;
import cn.biq.mn.security.JwtUtils;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final WebUtils webUtils;
    private final BaseEntityRepository baseEntityRepository;
    private final SessionUtil sessionUtil;
    private final BookRepository bookRepository;
    private final GroupRepository groupRepository;
    private final BaseService baseService;
    private final BookService bookService;
    private final UserGroupRelationRepository userGroupRelationRepository;

    @Value("${invite_code}")
    private String inviteCode;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginForm form) {
        var response = new LoginResponse();
        User user = userRepository.findOneByUsername(form.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(form.getPassword(), user.getPassword())) {
            throw new FailureMessageException("user.login.wrong.credentials");
        }
        // Filter中设置context，这里不需要
        String jwt = jwtUtils.createToken(user);
        response.setAccessToken(jwt);
        response.setUsername(user.getUsername());
        response.setRemember(form.getRemember());
        sessionUtil.clear();
        return response;
    }

    @Transactional(readOnly = true)
    public boolean logout() {
        return true;
    }

    public boolean register(RegisterForm form) {
        if (StringUtils.hasText(inviteCode) && !inviteCode.equals(form.getInviteCode())) {
            throw new FailureMessageException("user.register.invite.code.error");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new ItemExistsException("user.register.name.exists");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setNickName(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRegisterTime(System.currentTimeMillis());
        user.setRegisterIp(webUtils.getRequestIP());

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
        Book book = bookService.addDefaultTemplate(group);
        user.setDefaultBook(book);
        group.setDefaultBook(book);
        baseEntityRepository.save(user);
        baseEntityRepository.save(group);

        return true;
    }

    public boolean bindUsername(RegisterForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new ItemExistsException("user.register.name.exists");
        }
        User user = sessionUtil.getCurrentUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        baseEntityRepository.save(user);
        return true;
    }

    @Transactional(propagation = Propagation.NEVER, readOnly = true)
    public InitStateResponse getInitState() {
        var initState = new InitStateResponse();
//        initState.setUser(UserMapper.INSTANCE.toSessionVo(sessionUtil.getCurrentUser()));
//        initState.setBook(BookMapper.INSTANCE.toSessionVo(sessionUtil.getCurrentBook()));
//        initState.setGroup(GroupMapper.INSTANCE.toSessionVo(sessionUtil.getCurrentGroup()));
        // 需要最新数据
        User user = sessionUtil.getCurrentUser();
        Book book = null;
        if (user.getDefaultBook() != null) {
            book = bookRepository.findById(user.getDefaultBook().getId()).orElseThrow(ItemNotFoundException::new);
        }
        Group group = groupRepository.findById(user.getDefaultGroup().getId()).orElseThrow(ItemNotFoundException::new);
        initState.setUser(UserMapper.toSessionVo(user));
        initState.setBook(BookMapper.toSessionVo(book));
        initState.setGroup(GroupMapper.toSessionVo(group));
        return initState;
    }

    public boolean changePassword(ChangePasswordRequest form) {
        User user = sessionUtil.getCurrentUser();
        if (passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(form.getNewPassword()));
            baseEntityRepository.save(user);
        } else {
            throw new FailureMessageException("user.old.password.error");
        }
        return true;
    }

    public boolean setDefaultBook(Integer id) {
        User user = sessionUtil.getCurrentUser();
        Group group = sessionUtil.getCurrentGroup();
        Book entity = baseService.findBookById(id);
        if (!entity.getEnable()) {
            throw new ItemNotFoundException();
        }
        group.setDefaultBook(entity);
        user.setDefaultBook(entity);
        groupRepository.save(group);
        userRepository.save(user);
        sessionUtil.setCurrentBook(entity);
        return true;
    }

    public boolean setDefaultGroup(Integer id) {
        Group group = groupRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        User user = sessionUtil.getCurrentUser();
        var relationOptional = userGroupRelationRepository.findByGroupAndUser(group, user);
        if (relationOptional.isEmpty()) {
            throw new FailureMessageException("group.update.auth.error");
        }
        user.setDefaultGroup(group);
        user.setDefaultBook(group.getDefaultBook());
        userRepository.save(user);
        sessionUtil.setCurrentGroup(group);
        sessionUtil.setCurrentBook(group.getDefaultBook());
        return true;
    }

}
