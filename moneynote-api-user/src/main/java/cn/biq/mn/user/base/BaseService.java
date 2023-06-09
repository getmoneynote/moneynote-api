package cn.biq.mn.user.base;

import cn.biq.mn.user.tag.Tag;
import cn.biq.mn.user.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.user.account.Account;
import cn.biq.mn.user.account.AccountRepository;
import cn.biq.mn.user.balanceflow.BalanceFlow;
import cn.biq.mn.user.balanceflow.BalanceFlowRepository;
import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookRepository;
import cn.biq.mn.user.category.Category;
import cn.biq.mn.user.category.CategoryRepository;
import cn.biq.mn.user.noteday.NoteDay;
import cn.biq.mn.user.noteday.NoteDayRepository;
import cn.biq.mn.user.payee.Payee;
import cn.biq.mn.user.payee.PayeeRepository;
import cn.biq.mn.user.utils.SessionUtil;

import java.util.ArrayList;
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

    public Book findBookById(Integer id) {
        if (id == null) return null;
        Book book = bookRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!book.getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
            throw new ItemNotFoundException();
        }
        return book;
    }

    // 调用者保证传来的book是currentGroup下面的
    public Category findCategoryByBookAndId(Book book, Integer id) {
        if (id == null) return null;
        Category category = categoryRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!category.getBook().getId().equals(book.getId())) {
            throw new ItemNotFoundException();
        }
        return category;
    }

    // 调用者保证传来的book是currentGroup下面的
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

    public Tag findTagById(Integer id) {
        if (id == null) return null;
        Tag tag = tagRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!tag.getBook().getGroup().getId().equals(sessionUtil.getCurrentGroup().getId())) {
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
}
