package cn.biq.mn.user;

import cn.biq.mn.bean.ApplicationScopeBean;
import cn.biq.mn.book.tpl.BookTemplate;
import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.group.QGroup;
import cn.biq.mn.response.SelectVo;
import cn.biq.mn.utils.CommonUtils;
import cn.biq.mn.utils.MessageSourceUtil;
import cn.biq.mn.utils.WebUtils;
import cn.biq.mn.base.BaseEntityRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.*;
import cn.biq.mn.group.Group;
import cn.biq.mn.group.GroupMapper;
import cn.biq.mn.group.GroupRepository;
import cn.biq.mn.security.JwtUtils;
import cn.biq.mn.utils.SessionUtil;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


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
    private final MessageSourceUtil messageSourceUtil;
    private final ApplicationScopeBean applicationScopeBean;

    @Value("${invite_code:111111}")
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
        group.setName(messageSourceUtil.getMessage("user.register.default.group"));
        String lang = WebUtils.getAcceptLang();
        if ("zh-CN".equals(lang)) {
            group.setDefaultCurrencyCode("CNY");
        } else {
            group.setDefaultCurrencyCode("USD");
        }
        baseEntityRepository.save(group);
        user.setDefaultGroup(group);
        baseEntityRepository.save(user);
        group.setCreator(user);
        baseEntityRepository.save(group);
        UserGroupRelation userGroupRelation = new UserGroupRelation(user, group, 1);
        baseEntityRepository.save(userGroupRelation);
        baseEntityRepository.save(user);

        // 给默认账本
        List<BookTemplate> bookTemplateList = applicationScopeBean.getBookTplList();
        var bookTemplateList2 = bookTemplateList.stream().filter(b -> b.getLang().equals(WebUtils.getAcceptLang())).toList();
        BookTemplate bookTemplate;
        if (bookTemplateList2.size() > 0) {
            bookTemplate = bookTemplateList2.get(0);
        } else {
            bookTemplate = bookTemplateList.get(0);
        }
        Book book = new Book();
        book.setName(bookTemplate.getName());
        bookService.setBookByBookTemplate(bookTemplate, group, book);

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
        User user = userRepository.findById(sessionUtil.getCurrentUser().getId()).orElseThrow(ItemNotFoundException::new);
        Book book = bookRepository.findById(user.getDefaultBook().getId()).orElseThrow(ItemNotFoundException::new);
        Group group = groupRepository.findById(user.getDefaultGroup().getId()).orElseThrow(ItemNotFoundException::new);
        initState.setUser(UserMapper.toSessionVo(user));
        initState.setBook(BookMapper.toSessionVo(book));
        initState.setGroup(GroupMapper.toSessionVo(group));
        sessionUtil.setCurrentBook(book);
        sessionUtil.setCurrentGroup(group);
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
        Book entity = baseService.getBookInGroup(id);
        if (!entity.getEnable()) {
            throw new ItemNotFoundException();
        }

        User user = sessionUtil.getCurrentUser();
//        Group group = sessionUtil.getCurrentGroup();

//        group.setDefaultBook(entity);
        user.setDefaultBook(entity);
//        groupRepository.save(group);
        userRepository.save(user);
        sessionUtil.setCurrentBook(entity);
        return true;
    }

    public boolean setDefaultGroup(Integer id) {
        Group group = groupRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        baseService.checkGroupAuth(group);
        User user = sessionUtil.getCurrentUser();
        user.setDefaultGroup(group);
        user.setDefaultBook(group.getDefaultBook());
        userRepository.save(user);
        sessionUtil.setCurrentGroup(group);
        sessionUtil.setCurrentBook(group.getDefaultBook());
        return true;
    }

    public List<SelectVo> getBooksSelect() {
        List<SelectVo> result = new ArrayList<>();
        User user = sessionUtil.getCurrentUser();
        QGroup qGroup = QGroup.group;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qGroup.relations.any().user.eq(user));
        List<Group> groups = groupRepository.findAll(booleanBuilder);
        groups.forEach(group -> {
            baseService.checkGroupAuth(group);
            List<Book> books = bookRepository.findAllByGroup(group);
            books.forEach(book -> {
                if (book.getEnable()) {
                    SelectVo vo = new SelectVo();
                    vo.setValue(group.getId() + "-" + book.getId());
                    vo.setLabel(group.getName() + "(" + group.getDefaultCurrencyCode() + ")" + " - " + book.getName() + "(" + book.getDefaultCurrencyCode() + ")");
                    result.add(vo);
                }
            });
        });
        return result;
    }

    public boolean setDefaultGroupAndBook(String id) {
        String[] ids = id.split("-");
        if (ids.length < 2) {
            throw new FailureMessageException();
        }
        Integer groupId = Integer.valueOf(ids[0]);
        Integer bookId = Integer.valueOf(ids[1]);
        Group group = groupRepository.findById(groupId).orElseThrow(ItemNotFoundException::new);
        baseService.checkGroupAuth(group);
        Book book = bookRepository.findById(bookId).orElseThrow(ItemNotFoundException::new);
        if (!book.getEnable() || !book.getGroup().getId().equals(groupId)) {
            throw new ItemNotFoundException();
        }
        User user = sessionUtil.getCurrentUser();
        user.setDefaultGroup(group);
        user.setDefaultBook(book);
        userRepository.save(user);
        sessionUtil.setCurrentGroup(group);
        sessionUtil.setCurrentBook(book);
        return true;
    }

}
