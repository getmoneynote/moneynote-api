package cn.biq.mn.book;

import cn.biq.mn.bean.ApplicationScopeBean;
import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.payee.PayeeMapper;
import cn.biq.mn.tree.TreeUtils;
import cn.biq.mn.balanceflow.BalanceFlow;
import cn.biq.mn.balanceflow.BalanceFlowDetails;
import cn.biq.mn.balanceflow.BalanceFlowMapper;
import cn.biq.mn.balanceflow.BalanceFlowRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.tpl.BookTemplate;
import cn.biq.mn.book.tpl.CategoryTemplate;
import cn.biq.mn.book.tpl.TagTemplate;
import cn.biq.mn.category.Category;
import cn.biq.mn.category.CategoryDetails;
import cn.biq.mn.category.CategoryMapper;
import cn.biq.mn.category.CategoryRepository;
import cn.biq.mn.currency.CurrencyService;
import cn.biq.mn.group.Group;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.payee.PayeeRepository;
import cn.biq.mn.tag.Tag;
import cn.biq.mn.tag.TagDetails;
import cn.biq.mn.tag.TagMapper;
import cn.biq.mn.tag.TagRepository;
import cn.biq.mn.user.User;
import cn.biq.mn.user.UserRepository;
import cn.biq.mn.utils.Limitation;
import cn.biq.mn.utils.MessageSourceUtil;
import cn.biq.mn.utils.SessionUtil;
import cn.biq.mn.utils.WebUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final SessionUtil sessionUtil;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BalanceFlowRepository balanceFlowRepository;
    private final TagRepository tagRepository;
    private final PayeeRepository payeeRepository;
    private final BookMapper bookMapper;
    private final BalanceFlowMapper balanceFlowMapper;
    private final BaseService baseService;
    private final CurrencyService currencyService;
    private final ApplicationScopeBean applicationScopeBean;
    private final MessageSourceUtil messageSourceUtil;

    @Transactional(readOnly = true)
    public Page<BookDetails> query(BookQueryForm form, Pageable page) {
        Group group = sessionUtil.getCurrentGroup();
        Page<Book> entityPage = bookRepository.findAll(form.buildPredicate(group), page);
        return entityPage.map(book -> {
            var details = BookMapper.toDetails(book);
            // 是否是默认账本
            details.setCurrent(details.getId().equals(sessionUtil.getCurrentBook().getId()));
            details.setGroupDefault(details.getId().equals(book.getGroup().getDefaultBook().getId()));
            return details;
        });
    }

    // select下拉数据使用
    @Transactional(readOnly = true)
    public List<BookDetails> queryAll(BookQueryForm form) {
        form.setEnable(true);
        Group group = sessionUtil.getCurrentGroup();
        List<Book> entityList = bookRepository.findAll(form.buildPredicate(group));
        return entityList.stream().map(BookMapper::toDetails).toList();
    }

    @Transactional(readOnly = true)
    public BookDetails get(Integer id) {
        Book entity = baseService.getBookInGroup(id);
        var details = BookMapper.toDetails(entity);
        details.setCurrent(details.getId().equals(sessionUtil.getCurrentUser().getDefaultBook().getId()));
        return details;
    }

    public boolean add(BookAddForm form) {
        Group group = sessionUtil.getCurrentGroup();
        if (bookRepository.countByGroup(group) >= Limitation.book_max_count) {
            throw new FailureMessageException("book.max.count");
        }
        if (bookRepository.existsByGroupAndName(group, form.getName())) {
            throw new ItemExistsException();
        }
        currencyService.checkCode(form.getDefaultCurrencyCode());
        bookRepository.save(bookMapper.toEntity(form));
        return true;
    }

    public boolean update(Integer id, BookUpdateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        Book entity = baseService.getBookInGroup(id);
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
        // 默认的账本不能操作，前端按钮禁用
//        Group group = sessionUtil.getCurrentGroup();
//        // 组的默认账本不能删除，保证一个组必须有一个账本
//        if (group.getDefaultBook().getId().equals(id)) {
//            // 前端会禁止操作
//            throw new FailureMessageException();
//        }
        Book entity = baseService.getBookInGroup(id);
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
            throw new FailureMessageException();
        }
        Book entity = baseService.getBookInGroup(id);
        // 移入回收站的操作
        if (entity.getEnable()) {
            //将所有用户默认账本是该账本的用户的默认账本，回退到组的默认账本
            List<User> users = userRepository.findByDefaultBook(entity);
            users.forEach(user -> {
                user.setDefaultBook(group.getDefaultBook());
                userRepository.save(user);
            });
            // TODO 用户是登录状态，正在操作怎么处理
        }
        entity.setEnable(!entity.getEnable());
        bookRepository.save(entity);
        return true;
    }

    // 账本模板列表，复制功能使用
    public boolean addByTemplate(BookAddByTemplateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        List<BookTemplate> bookTplList = applicationScopeBean.getBookTplList();
        Optional<BookTemplate> bookTemplateOptional = bookTplList.stream()
                .filter(bookTemplate -> bookTemplate.getId().equals(form.getTemplateId()))
                .findFirst();
        if (bookTemplateOptional.isPresent()) {
            BookTemplate bookTemplate = bookTemplateOptional.get();
            Book book = new Book();
            String bookName;
            if (StringUtils.hasText(form.getBookName())) {
                bookName = form.getBookName();
            } else {
                bookName = bookTemplate.getName();
            }
            book.setName(bookName);
            setBookByBookTemplate(bookTemplate, group, book);
        } else {
            throw new ItemNotFoundException();
        }
        return true;
    }

    // 复制账本
    public boolean addByBook(BookAddByBookForm form) {
        Group group = sessionUtil.getCurrentGroup();
        Book bookSrc = baseService.getBookInGroup(form.getBookId());
        Book book = new Book();
        String bookName;
        if (StringUtils.hasText(form.getBookName())) {
            bookName = form.getBookName();
        } else {
            bookName = bookSrc.getName();
        }
        book.setName(bookName);
        book.setNotes(bookSrc.getNotes());
        book.setDefaultCurrencyCode(group.getDefaultCurrencyCode());
        book.setGroup(group);
        bookRepository.save(book);

        List<TagDetails> tagDetailList = bookSrc.getTags().stream().map(TagMapper::toDetails).toList();
        saveTag1(TreeUtils.buildTree(tagDetailList), book);

        List<CategoryDetails> categoryDetailList = bookSrc.getCategories().stream().map(CategoryMapper::toDetails).toList();
        saveCategory1(TreeUtils.buildTree(categoryDetailList), book);

        List<Payee> payeesToSave = new ArrayList<>();
        bookSrc.getPayees().forEach(i -> {
            Payee payee = PayeeMapper.toEntity(i);
            payee.setBook(book);
            payeesToSave.add(payee);
        });
        payeeRepository.saveAll(payeesToSave);

        return true;
    }

    public void setBookByBookTemplate(BookTemplate bookTemplate, Group group, Book book) {
        if (bookRepository.existsByGroupAndName(group, book.getName())) {
            throw new ItemExistsException();
        }

        book.setNotes(bookTemplate.getNotes());
        book.setDefaultCurrencyCode(group.getDefaultCurrencyCode());
        book.setGroup(group);
        bookRepository.save(book);

        saveTag(TreeUtils.buildTree(bookTemplate.getTags()), book);
        saveCategory(TreeUtils.buildTree(bookTemplate.getCategories()), book);
        List<Payee> payeesToSave = new ArrayList<>();
        bookTemplate.getPayees().forEach(i -> {
            Payee payee = PayeeMapper.toEntity(i);
            payee.setBook(book);
            payeesToSave.add(payee);
        });
        payeeRepository.saveAll(payeesToSave);
    }

    private void saveTag(List<TagTemplate> detailsList, Book book) {
        Queue<TagTemplate> queue = new LinkedList<>();
        for(TagTemplate item : detailsList) {
            queue.add(item);
            Tag parent = null;
            while (!queue.isEmpty()) {
                var details = queue.poll();
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

    private void saveTag1(List<TagDetails> detailsList, Book book) {
        Queue<TagDetails> queue = new LinkedList<>();
        for(TagDetails item : detailsList) {
            queue.add(item);
            Tag parent = null;
            while (!queue.isEmpty()) {
                var details = queue.poll();
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

    private void saveCategory(List<CategoryTemplate> detailsList, Book book) {
        Queue<CategoryTemplate> queue = new LinkedList<>();
        for(var item : detailsList) {
            queue.add(item);
            Category parent = null;
            while (!queue.isEmpty()) {
                var details = queue.poll();
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

    private void saveCategory1(List<CategoryDetails> detailsList, Book book) {
        Queue<CategoryDetails> queue = new LinkedList<>();
        for(var item : detailsList) {
            queue.add(item);
            Category parent = null;
            while (!queue.isEmpty()) {
                var details = queue.poll();
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

    public Workbook exportFlow(Integer id, Integer timeZoneOffset) {
        Book book = baseService.getBookInGroup(id);
        // 24小时内只能导出一次
//        if (CalendarUtil.inLastDay(book.getExportAt())) {
//            throw new FailureMessageException("book.export.limit.fail");
//        }
        // 创建一个新的工作簿
        Workbook workbook = new SXSSFWorkbook();
        // 创建一个新的工作表
        Sheet sheet = workbook.createSheet("Book");
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            messageSourceUtil.getMessage("book.export.title"),
            messageSourceUtil.getMessage("book.export.type"),
            messageSourceUtil.getMessage("book.export.amount"),
            messageSourceUtil.getMessage("book.export.time"),
            messageSourceUtil.getMessage("book.export.account"),
            messageSourceUtil.getMessage("book.export.category"),
            messageSourceUtil.getMessage("book.export.tag"),
            messageSourceUtil.getMessage("book.export.payee"),
            messageSourceUtil.getMessage("book.export.note"),
            messageSourceUtil.getMessage("book.export.confirm"),
            messageSourceUtil.getMessage("book.export.include"),
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        List<BalanceFlow> balanceFlows = balanceFlowRepository.findAllByBookOrderByCreateTimeDesc(book);
        List<BalanceFlowDetails> balanceFlowDetailsList = balanceFlows.stream().map(balanceFlowMapper::toDetails).toList();
        int rowNum = 1;
        for (BalanceFlowDetails item : balanceFlowDetailsList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getTitle());
            row.createCell(1).setCellValue(item.getTypeName());
            row.createCell(2).setCellValue(item.getAmount().toString());

            Date createDate = new Date(item.getCreateTime());
            String lang = WebUtils.getAcceptLang();
            String dateFormat;
            if ("zh-CN".equals(lang)) {
                dateFormat = "yyyy-MM-dd HH:mm:ss";
            } else {
                dateFormat = "MM/dd/yyyy HH:mm";
            }
            SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
            ZoneId zoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(timeZoneOffset));
            TimeZone timeZone = TimeZone.getTimeZone(zoneId);
            sf.setTimeZone(timeZone);
            row.createCell(3).setCellValue(sf.format(createDate));

            row.createCell(4).setCellValue(item.getAccountName());
            row.createCell(5).setCellValue(item.getCategoryName());
            row.createCell(6).setCellValue(item.getTagsName());

            if (item.getPayee() != null) {
                row.createCell(7).setCellValue(item.getPayee().getName());
            } else {
                row.createCell(7).setCellValue("");
            }

            row.createCell(8).setCellValue(item.getNotes());
            row.createCell(9).setCellValue(item.getConfirm() ? messageSourceUtil.getMessage("yes") : messageSourceUtil.getMessage("no"));
            if (item.getInclude() != null) {
                row.createCell(10).setCellValue(item.getInclude() ? messageSourceUtil.getMessage("yes") : messageSourceUtil.getMessage("no"));
            }
        }
        sheet.setColumnWidth(3, 19*256);
//        sheet.autoSizeColumn(3);
        book.setExportAt(System.currentTimeMillis());
        bookRepository.save(book);
        return workbook;
    }

}
