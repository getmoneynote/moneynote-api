package cn.biq.mn.user.book;

import cn.biq.mn.user.base.BaseService;
import cn.biq.mn.user.tag.Tag;
import cn.biq.mn.user.tag.TagMapper;
import cn.biq.mn.user.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemExistsException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.tree.TreeUtils;
import cn.biq.mn.user.balanceflow.BalanceFlowRepository;
import cn.biq.mn.user.category.Category;
import cn.biq.mn.user.category.CategoryMapper;
import cn.biq.mn.user.category.CategoryRepository;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.payee.Payee;
import cn.biq.mn.user.payee.PayeeRepository;
import cn.biq.mn.user.template.book.BookTemplate;
import cn.biq.mn.user.template.book.BookTemplateRepository;
import cn.biq.mn.user.template.category.CategoryTemplateDetails;
import cn.biq.mn.user.template.category.CategoryTemplateMapper;
import cn.biq.mn.user.template.tag.TagTemplateDetails;
import cn.biq.mn.user.template.tag.TagTemplateMapper;
import cn.biq.mn.user.utils.SessionUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final SessionUtil sessionUtil;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BalanceFlowRepository balanceFlowRepository;
    private final BookTemplateRepository bookTemplateRepository;
    private final TagRepository tagRepository;
    private final PayeeRepository payeeRepository;
    private final BookMapper bookMapper;
    private final BaseService baseService;

    @Transactional(readOnly = true)
    public Page<BookDetails> query(BookQueryForm form, Pageable page) {
        Group group = sessionUtil.getCurrentGroup();
        Page<Book> entityPage = bookRepository.findAll(form.buildPredicate(group), page);
        return entityPage.map(book -> {
            var details = BookMapper.toDetails(book);
            if (sessionUtil.getCurrentUser().getDefaultBook() != null) {
                details.setDefault(details.getId().equals(sessionUtil.getCurrentUser().getDefaultBook().getId()));
            }
            return details;
        });
    }

    @Transactional(readOnly = true)
    public List<BookDetails> queryAll(BookQueryForm form) {
        form.setEnable(true);
        Group group = sessionUtil.getCurrentGroup();
        List<Book> entityList = bookRepository.findAll(form.buildPredicate(group));
        Book keep = baseService.findBookById(form.getKeep());
        if (keep != null && !entityList.contains(keep)) {
            entityList.add(0, keep);
        }
        return entityList.stream().map(BookMapper::toDetails).toList();
    }

    @Transactional(readOnly = true)
    public BookDetails get(Integer id) {
        Book entity = baseService.findBookById(id);
        var details = BookMapper.toDetails(entity);
        details.setDefault(details.getId().equals(sessionUtil.getCurrentUser().getDefaultBook().getId()));
        return details;
    }

    public boolean add(BookAddForm form) {
        Group group = sessionUtil.getCurrentGroup();
        if (bookRepository.existsByGroupAndName(group, form.getName())) {
            throw new ItemExistsException();
        }
        bookRepository.save(bookMapper.toEntity(form));
        return true;
    }

    public boolean update(Integer id, BookUpdateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        Book entity = baseService.findBookById(id);
        if (!Objects.equals(entity.getName(), form.getName())) {
            if (StringUtils.hasText(form.getName())) {
                if (bookRepository.existsByGroupAndName(group, form.getName())) {
                    throw new ItemExistsException();
                }
            }
        }
        bookMapper.updateEntity(form, entity);
        bookRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        Group group = sessionUtil.getCurrentGroup();
        if (group.getDefaultBook().getId().equals(id)) {
            return false;
        }
        Book entity = baseService.findBookById(id);
        if (balanceFlowRepository.existsByBook(entity)) {
            throw new FailureMessageException("book.delete.has.flow");
        }
        categoryRepository.deleteByBook(entity);
        payeeRepository.deleteByBook(entity);
        tagRepository.deleteByBook(entity);
        bookRepository.delete(entity);
        return true;
    }

    public boolean toggle(Integer id) {
        Group group = sessionUtil.getCurrentGroup();
        if (group.getDefaultBook().getId().equals(id)) {
            return false;
        }
        Book entity = baseService.findBookById(id);
        entity.setEnable(!entity.getEnable());
        bookRepository.save(entity);
        return true;
    }

    public boolean addByTemplate(BookAddByTemplateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        addByTemplate(form, group);
        return true;
    }

    public Book addByTemplate(BookAddByTemplateForm form, Group group) {
        Book book = new Book();
        BookTemplate bookTemplate = bookTemplateRepository.findById(form.getTemplateId()).orElseThrow(ItemNotFoundException::new);
        String bookName = null;
        if (StringUtils.hasText(form.getBookName())) {
            bookName = form.getBookName();
        } else {
            bookName = bookTemplate.getName();
        }
        if (bookRepository.existsByGroupAndName(group, bookName)) {
            throw new ItemExistsException();
        }
        book.setName(bookName);
        book.setNotes(bookTemplate.getNotes());
        book.setDefaultCurrencyCode(group.getDefaultCurrencyCode());
        book.setGroup(group);
        bookRepository.save(book);

        List<TagTemplateDetails> tagTemplateDetails = TreeUtils.buildTree(bookTemplate.getTags().stream().map(TagTemplateMapper::toDetails).toList());
        saveTag(tagTemplateDetails, book);

        List<CategoryTemplateDetails> categoryTemplateDetails = TreeUtils.buildTree(bookTemplate.getCategories().stream().map(CategoryTemplateMapper::toDetails).toList());
        saveCategory(categoryTemplateDetails, book);

        List<Payee> payeesToSave = new ArrayList<>();
        bookTemplate.getPayees().forEach(i -> {
            Payee payee = new Payee();
            payee.setName(i.getName());
            payee.setNotes(i.getNotes());
            payee.setCanExpense(i.getCanExpense());
            payee.setCanIncome(i.getCanIncome());
            payee.setBook(book);
            payeesToSave.add(payee);
        });
        payeeRepository.saveAll(payeesToSave);

        return book;
    }

    private void saveTag(List<TagTemplateDetails> detailsList, Book book) {
        Queue<TagTemplateDetails> queue = new LinkedList<>();
        for(TagTemplateDetails item : detailsList) {
            queue.add(item);
            Tag parent = null;
            while (!queue.isEmpty()) {
                TagTemplateDetails details = queue.poll();
                Tag tag = TagMapper.toEntity(details);
                tag.setBook(book);
                tag.setParent(parent);
                tagRepository.save(tag);
                if (details.getChildren() != null) {
                    parent = tag;
                    queue.addAll(details.getChildren());
                }
            }
        }
    }

    private void saveCategory(List<CategoryTemplateDetails> detailsList, Book book) {
        Queue<CategoryTemplateDetails> queue = new LinkedList<>();
        for(CategoryTemplateDetails item : detailsList) {
            queue.add(item);
            Category parent = null;
            while (!queue.isEmpty()) {
                CategoryTemplateDetails details = queue.poll();
                Category category = CategoryMapper.toEntity(details);
                category.setBook(book);
                category.setParent(parent);
                categoryRepository.save(category);
                if (details.getChildren() != null) {
                    parent = category;
                    queue.addAll(details.getChildren());
                }
            }
        }
    }

}
