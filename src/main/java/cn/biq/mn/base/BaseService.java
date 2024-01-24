package cn.biq.mn.base;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.account.Account;
import cn.biq.mn.account.AccountRepository;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.balanceflow.BalanceFlowRepository;
import cn.biq.mn.book.Book;
import cn.biq.mn.book.BookRepository;
import cn.biq.mn.category.Category;
import cn.biq.mn.category.CategoryRepository;
import cn.biq.mn.group.Group;
import cn.biq.mn.noteday.NoteDay;
import cn.biq.mn.noteday.NoteDayRepository;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.payee.PayeeRepository;
import cn.biq.mn.tag.Tag;
import cn.biq.mn.tag.TagRepository;
import cn.biq.mn.user.User;
import cn.biq.mn.user.UserGroupRelationRepository;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BaseService {

    private final SessionUtil sessionUtil;
    private final AccountRepository accountRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final PayeeRepository payeeRepository;
    private final TagRepository tagRepository;
    private final BalanceFlowRepository balanceFlowRepository;
    private final NoteDayRepository noteDayRepository;
    private final UserGroupRelationRepository userGroupRelationRepository;


    public BalanceFlow findFlowById(Integer id) {
        if (id == null) return null;
        BalanceFlow balanceFlow = balanceFlowRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!balanceFlow.getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return balanceFlow;
    }

    public Account findAccountById(Integer id) {
        if (id == null) return null;
        Account account = accountRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!account.getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return account;
    }

    /**
     * 默认的组里面找账本
     * @param id
     * @return
     */
    public Book getBookInGroup(Integer id) {
        if (id == null) return null;
        Book book = bookRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!book.getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return book;
    }

    public Category findCategoryByBookAndId(Book book, Integer id) {
        if (id == null) return null;
        Category category = categoryRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!category.getBook().getId().equals(book.getId())) {
            throw new ItemNotFoundException();
        }
        return category;
    }

    public List<Category> findCategoriesByBookAndIds(Book book, Set<Integer> ids) {
        List<Category> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        List<Category> categories = categoryRepository.findAllById(ids);
        categories.forEach(e -> {
            if (e.getBook().getId().equals(book.getId())) {
                result.add(e);
            }
        });
        return result;
    }

    public Category findCategoryById(Integer id) {
        if (id == null) return null;
        Category category = categoryRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!category.getBook().getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return category;
    }

    public Category findCurrentBookCategoryById(Integer id) {
        if (id == null) return null;
        Category category = categoryRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!category.getBook().getId().equals(sessionUtil.getCurrentBook().getId())) {
            throw new ItemNotFoundException();
        }
        return category;
    }

    public Tag findTagById(Integer id) {
        if (id == null) return null;
        Tag tag = tagRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!tag.getBook().getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return tag;
    }

    public Tag findCurrentBookTagById(Integer id) {
        if (id == null) return null;
        Tag tag = tagRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!tag.getBook().getId().equals(sessionUtil.getCurrentBook().getId())) {
            throw new ItemNotFoundException();
        }
        return tag;
    }

    // 调用者保证传来的book是currentGroup下面的
    public List<Tag> findTagsByBookAndIds(Book book, Set<Integer> ids) {
        List<Tag> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        List<Tag> tags = tagRepository.findAllById(ids);
        tags.forEach(e -> {
            if (e.getBook().getId().equals(book.getId())) {
                result.add(e);
            }
        });
        return result;
    }

    public Payee findPayeeById(Integer id) {
        if (id == null) return null;
        Payee payee = payeeRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!payee.getBook().getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return payee;
    }

    public NoteDay findNoteDayById(Integer id) {
        if (id == null) return null;
        NoteDay noteDay = noteDayRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!noteDay.getUser().getId().equals(sessionUtil.getCurrentUser().getId())) {
            throw new ItemNotFoundException();
        }
        return noteDay;
    }

    public void checkGroupAuth(Group group) {
        if (!group.getEnable()) {
            throw new FailureMessageException("group.update.auth.error");
        }
        User user = sessionUtil.getCurrentUser();
        var relationOptional = userGroupRelationRepository.findByGroupAndUser(group, user);
        if (relationOptional.isEmpty()) {
            throw new FailureMessageException("group.update.auth.error");
        }
        var relation = relationOptional.get();
        // 检查权限
        if (!Arrays.asList(1, 2, 3).contains(relation.getRole())) {
            throw new FailureMessageException("group.update.auth.error");
        }
    }

}
