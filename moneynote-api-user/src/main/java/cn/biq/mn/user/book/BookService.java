package cn.biq.mn.user.book;

import cn.biq.mn.base.utils.CalendarUtil;
import cn.biq.mn.user.balanceflow.BalanceFlow;
import cn.biq.mn.user.balanceflow.BalanceFlowDetails;
import cn.biq.mn.user.balanceflow.BalanceFlowMapper;
import cn.biq.mn.user.base.BaseService;
import cn.biq.mn.user.book.tpl.BookTemplate;
import cn.biq.mn.user.book.tpl.BookTemplateResponse;
import cn.biq.mn.user.book.tpl.CategoryTemplate;
import cn.biq.mn.user.book.tpl.TagTemplate;
import cn.biq.mn.user.currency.CurrencyService;
import cn.biq.mn.user.payee.Payee;
import cn.biq.mn.user.tag.Tag;
import cn.biq.mn.user.tag.TagMapper;
import cn.biq.mn.user.tag.TagRepository;
import cn.biq.mn.user.utils.Limitation;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
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
import cn.biq.mn.user.payee.PayeeRepository;
import cn.biq.mn.user.utils.SessionUtil;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final SessionUtil sessionUtil;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BalanceFlowRepository balanceFlowRepository;
    private final TagRepository tagRepository;
    private final PayeeRepository payeeRepository;
    private final BookMapper bookMapper;
    private final BalanceFlowMapper balanceFlowMapper;
    private final BaseService baseService;
    private final CurrencyService currencyService;
    private final RestTemplate restTemplate;
    @Value("${user_api_base_url}")
    private String userApiBaseUrl;

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
        // 默认的账本不能操作，前端按钮禁用
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
        // 默认的账本不能操作，前端按钮禁用
        Group group = sessionUtil.getCurrentGroup();
        if (group.getDefaultBook().getId().equals(id)) {
            return false;
        }
        Book entity = baseService.findBookById(id);
        entity.setEnable(!entity.getEnable());
        bookRepository.save(entity);
        return true;
    }

    // 账本模板列表，复制功能使用
    public boolean addByTemplate(BookAddByTemplateForm form) {
        Group group = sessionUtil.getCurrentGroup();
        var response =  restTemplate.getForObject(userApiBaseUrl + "/book-templates/" + form.getTemplateId(), BookTemplateResponse.class);
        if (!response.isSuccess()) {
            throw new ItemNotFoundException();
        }
        Book book = new Book();
        var bookTemplate = response.getData();
        String bookName;
        if (StringUtils.hasText(form.getBookName())) {
            bookName = form.getBookName();
        } else {
            bookName = bookTemplate.getName();
        }
        book.setName(bookName);
        setBookByBookTemplate(bookTemplate, group, book);
        return true;
    }

    // 注册功能使用。
    public Book addDefaultTemplate(Group group) {
        var bookTemplate = restTemplate.getForObject("http://tpl.moneywhere.com/default.json", BookTemplate.class);
        Book book = new Book();
        book.setName(bookTemplate.getName());
        setBookByBookTemplate(bookTemplate, group, book);
        return book;
    }

    private void setBookByBookTemplate(BookTemplate bookTemplate, Group group, Book book) {
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
            Payee payee = new Payee();
            payee.setName(i.getName());
            payee.setNotes(i.getNotes());
            payee.setCanExpense(i.getCanExpense());
            payee.setCanIncome(i.getCanIncome());
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

    public Workbook exportFlow(Integer id) {
        Book book = baseService.findBookById(id);
        // 24小时内只能导出一次
        if (CalendarUtil.inLastDay(book.getExportAt())) {
            throw new FailureMessageException("book.export.limit.fail");
        }
        // 创建一个新的工作簿
        Workbook workbook = new SXSSFWorkbook();
        // 创建一个新的工作表
        Sheet sheet = workbook.createSheet("Data");
        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "标题", "交易类型", "金额", "时间", "账户", "分类",
                "标签", "交易对象", "备注", "是否确认", "是否统计"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 写入数据
        List<BalanceFlow> balanceFlows = balanceFlowRepository.findAllByBook(book);
        List<BalanceFlowDetails> balanceFlowDetailsList = balanceFlows.stream().map(balanceFlowMapper::toDetails).toList();
        int rowNum = 1;
        for (BalanceFlowDetails item : balanceFlowDetailsList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getTitle());
            row.createCell(1).setCellValue(item.getTypeName());
            row.createCell(2).setCellValue(item.getAmount().toString());

            CellStyle cellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm"));
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            Cell cell = row.createCell(3);
            cell.setCellValue(new Date(item.getCreateTime()));
            cell.setCellStyle(cellStyle);

            row.createCell(4).setCellValue(item.getAccountName());
            row.createCell(5).setCellValue(item.getCategoryName());
            row.createCell(6).setCellValue(item.getTagsName());

            if (item.getPayee() != null) {
                row.createCell(7).setCellValue(item.getPayee().getName());
            } else {
                row.createCell(7).setCellValue("");
            }

            row.createCell(8).setCellValue(item.getNotes());
            row.createCell(9).setCellValue(item.getConfirm());
            if (item.getInclude() != null) {
                row.createCell(10).setCellValue(item.getInclude());
            }

        }
        sheet.setColumnWidth(3, 19*256);
//        sheet.autoSizeColumn(3);
        book.setExportAt(System.currentTimeMillis());
        bookRepository.save(book);
        return workbook;
    }

}
