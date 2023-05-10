package cn.biq.mn.user.group;

import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.user.balanceflow.BalanceFlowRepository;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookRepository;
import cn.biq.mn.user.book.BookService;
import cn.biq.mn.user.category.CategoryRepository;
import cn.biq.mn.user.currency.CurrencyService;
import cn.biq.mn.user.payee.PayeeRepository;
import cn.biq.mn.user.tag.TagRepository;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.user.UserGroupRelation;
import cn.biq.mn.user.user.UserGroupRelationRepository;
import cn.biq.mn.user.utils.EnumUtils;
import cn.biq.mn.user.utils.Limitation;
import cn.biq.mn.user.utils.SessionUtil;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final SessionUtil sessionUtil;
    private final GroupRepository groupRepository;
    private final BookRepository bookRepository;
    private final UserGroupRelationRepository userGroupRelationRepository;
    private final EnumUtils enumUtils;
    private final CurrencyService currencyService;
    private final BalanceFlowRepository balanceFlowRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PayeeRepository payeeRepository;

    @Transactional(readOnly = true)
    public Page<GroupDetails> query(Pageable page) {
        User user = sessionUtil.getCurrentUser();
        QGroup qGroup = QGroup.group;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qGroup.relations.any().user.eq(user));
        Page<Group> entityPage = groupRepository.findAll(booleanBuilder, page);
        return entityPage.map(group -> {
            var details = GroupMapper.toDetails(group);
            var relation = userGroupRelationRepository.findByGroupAndUser(group, user).get();
            details.setRole(enumUtils.translateRoleType(relation.getRole()));
            details.setDefault(details.getId().equals(sessionUtil.getCurrentUser().getDefaultGroup().getId()));
            return details;
        });
    }

    public boolean add(GroupAddForm form) {
        User user = sessionUtil.getCurrentUser();
        if (groupRepository.countByCreator(user) >= Limitation.group_max_count) {
            throw new FailureMessageException("group.max.count");
        }
        currencyService.checkCode(form.getDefaultCurrencyCode());
        Group group = new Group();
        group.setName(form.getName());
        group.setNotes(form.getNotes());
        group.setDefaultCurrencyCode(form.getDefaultCurrencyCode());
        group.setCreator(user);
        groupRepository.save(group);
        Book book = new Book();
        book.setName("默认账本");
        book.setDefaultCurrencyCode(group.getDefaultCurrencyCode());
        book.setGroup(group);
        bookRepository.save(book);
        group.setDefaultBook(book);
        groupRepository.save(group);
        UserGroupRelation relation = new UserGroupRelation(user, group, 1);
        userGroupRelationRepository.save(relation);
        return true;
    }

    private void checkRole(Group group) {
        User user = sessionUtil.getCurrentUser();
        var relationOptional = userGroupRelationRepository.findByGroupAndUser(group, user);
        if (relationOptional.isEmpty()) {
            throw new FailureMessageException("group.update.auth.error");
        }
        var relation = relationOptional.get();
        if (relation.getRole() != 1) {
            throw new FailureMessageException("group.update.auth.error");
        }
    }

    public boolean update(Integer id, GroupUpdateForm form) {
        Group entity = groupRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        checkRole(entity);
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        return true;
    }

    public boolean remove(Integer id) {
        Group entity = groupRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        checkRole(entity);
        if (entity.getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new FailureMessageException("group.update.auth.error");
        }
        List<Book> books = bookRepository.findAllByGroup(entity);
        books.forEach(book -> {
            if (balanceFlowRepository.existsByBook(book)) {
                throw new FailureMessageException("group.delete.has.flow");
            }
            categoryRepository.deleteByBook(book);
            payeeRepository.deleteByBook(book);
            tagRepository.deleteByBook(book);
            bookRepository.delete(book);
        });
        userGroupRelationRepository.deleteByGroup(entity);
        groupRepository.delete(entity);
        return true;
    }

}
