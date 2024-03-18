package cn.biq.mn.balanceflow;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemNotFoundException;
import cn.biq.mn.utils.CalendarUtil;
import cn.biq.mn.utils.MyCollectionUtil;
import cn.biq.mn.account.Account;
import cn.biq.mn.account.AccountRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.categoryrelation.CategoryRelation;
import cn.biq.mn.categoryrelation.CategoryRelationForm;
import cn.biq.mn.categoryrelation.CategoryRelationService;
import cn.biq.mn.flowfile.FlowFile;
import cn.biq.mn.flowfile.FlowFileDetails;
import cn.biq.mn.flowfile.FlowFileMapper;
import cn.biq.mn.flowfile.FlowFileRepository;
import cn.biq.mn.group.Group;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.payee.PayeeRepository;
import cn.biq.mn.tagrelation.TagRelation;
import cn.biq.mn.tagrelation.TagRelationService;
import cn.biq.mn.user.User;
import cn.biq.mn.utils.Limitation;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class BalanceFlowService {

    private final BalanceFlowRepository balanceFlowRepository;
    private final SessionUtil sessionUtil;
    private final AccountRepository accountRepository;
    private final BaseService baseService;
    private final PayeeRepository payeeRepository;
    private final CategoryRelationService categoryRelationService;
    private final TagRelationService tagRelationService;
    private final BalanceFlowMapper balanceFlowMapper;
    private final FlowFileRepository flowFileRepository;

    private void checkBeforeAdd(BalanceFlowAddForm form, Book book, User user) {
        // 对用户添加账单的操作进行限流。
        Long[] day = CalendarUtil.getIn1Day();
        if (balanceFlowRepository.countByCreatorAndInsertAtBetween(user, day[0], day[1]) > Limitation.flow_max_count_daily) {
            throw new FailureMessageException("add.flow.daily.overflow");
        }
        Account account = null;
        if (form.getAccount() != null) {
            account = baseService.findAccountById(form.getAccount());
        }
        if (form.getType() == FlowType.EXPENSE || form.getType() == FlowType.INCOME) {
            // 支出和收入的分类不能为空，因为transfer可以为空，所以只能在这里验证
            if (CollectionUtils.isEmpty(form.getCategories())) {
                throw new FailureMessageException("add.flow.category.empty");
            }
            // 检查Category不能重复
            if (MyCollectionUtil.hasDuplicate(form.getCategories())) {
                throw new FailureMessageException("add.flow.category.duplicated");
            }
            // 限制每个账单的分类数目
            if (form.getCategories().size() > Limitation.flow_max_category_count) {
                throw new FailureMessageException("add.flow.category.overflow");
            }
            // 外币账户必须输入convertedAmount
            if (account != null) {
                if (!account.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                    form.getCategories().forEach(i -> {
                        if (i.getConvertedAmount() == null) throw new FailureMessageException("valid.fail");
                    });
                }
            }
        }
        if (form.getType() == FlowType.TRANSFER) {
            // 转账必须有account, to id和amount
            if (account == null || form.getTo() == null || form.getAmount() == null) {
                throw new FailureMessageException("valid.fail");
            }
            // 转账的两个账户的币种不同，必须输入convertedAmount
            Account toAccount = baseService.findAccountById(form.getTo());
            if (!account.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                if (form.getConvertedAmount() == null) {
                    throw new FailureMessageException("valid.fail");
                }
            }
        }
    }

    private void checkBeforeUpdate(BalanceFlowUpdateForm form, BalanceFlow entity, Book book) {
        Account account = entity.getAccount();
        if(!Objects.equals(entity.getAccount() != null ? entity.getAccount().getId() : null, form.getAccountId())) {
            account = baseService.findAccountById(form.getAccountId());
        }
        if (entity.getType() == FlowType.EXPENSE || entity.getType() == FlowType.INCOME) {
            // 因为Category为非空，所以更新传入空代表不修改。
            // 检查Category不能重复
            if (MyCollectionUtil.hasDuplicate(form.getCategories())) {
                throw new FailureMessageException("add.flow.category.duplicated");
            }
            // 限制每个账单的分类数目
            if (form.getCategories() != null && form.getCategories().size() > Limitation.flow_max_category_count) {
                throw new FailureMessageException("add.flow.category.overflow");
            }
            // 外币账户必须输入convertedAmount
            // account修改了才需要判断，account没有修改的，也需要判断是不是外币账户。
            if (account != null) {
                if (!account.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                    // 不传代表不修改，所以需要判断非空
                    if (!CollectionUtils.isEmpty(form.getCategories())) {
                        form.getCategories().forEach(i -> {
                            if (i.getConvertedAmount() == null) throw new FailureMessageException("valid.fail");
                        });
                    }
                }
            }
        }
        if (entity.getType() == FlowType.TRANSFER) {
            // 转账必须有to id和amount
//            if (form.getToId() == null || form.getAmount() == null) {
//                throw new FailureMessageException("valid.fail");
//            }
            // 转账的两个账户的币种不同，必须输入convertedAmount
            // 账户有修改才需要判断，账户没有修改，但是之前两个账户的currencyCode不同也需要convertedAmount
            Account toAccount = entity.getTo();
            if (form.getToId() != null && !form.getToId().equals(entity.getTo().getId())) {
                toAccount = baseService.findAccountById(form.getToId());
            }
            if (!account.getCurrencyCode().equals(toAccount.getCurrencyCode())) {
                if (form.getConvertedAmount() == null) {
                    throw new FailureMessageException("valid.fail");
                }
            }
        }
    }

    public boolean add(BalanceFlowAddForm form) {
        User user = sessionUtil.getCurrentUser();
        Group group = sessionUtil.getCurrentGroup();
        Book book = baseService.getBookInGroup(form.getBook());
        checkBeforeAdd(form, book, user);
        BalanceFlow entity = BalanceFlowMapper.toEntity(form);
        entity.setGroup(group);
        entity.setBook(book);
        entity.setCreator(user);
        if (form.getAccount() != null) {
            entity.setAccount(baseService.findAccountById(form.getAccount()));
        }
        if (form.getType() == FlowType.EXPENSE || form.getType() == FlowType.INCOME) {
            BigDecimal amount = form.getCategories().stream().map(CategoryRelationForm::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            entity.setAmount(amount);
            if (entity.getAccount() == null || entity.getAccount().getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                entity.setConvertedAmount(amount);
            } else {
                BigDecimal convertedAmount = form.getCategories().stream().map(CategoryRelationForm::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                entity.setConvertedAmount(convertedAmount);
            }
            categoryRelationService.addRelation(form.getCategories(), entity, book, entity.getAccount());
        }
        if (form.getType() == FlowType.TRANSFER) {
            Account toAccount = baseService.findAccountById(form.getTo());
            entity.setTo(toAccount);
            if (entity.getAccount().getCurrencyCode().equals(entity.getTo().getCurrencyCode())) {
                entity.setConvertedAmount(entity.getAmount());
            } else {
                entity.setConvertedAmount(form.getConvertedAmount());
            }
        }
        if (form.getTags() != null) {
            tagRelationService.addRelation(form.getTags(), entity, book, entity.getAccount());
        }
        if (form.getPayee() != null) {
            Payee payee = payeeRepository.findOneByBookAndId(book, form.getPayee()).orElseThrow(ItemNotFoundException::new);
            entity.setPayee(payee);
        }
        balanceFlowRepository.save(entity);
        if (form.getConfirm()) {
            confirmBalance(entity);
        }
        return true;
    }

    // 余额确认，包括账户扣款。
    private void confirmBalance(BalanceFlow flow) {
        if (flow.getConfirm() && flow.getAccount() != null) {
            Account account = flow.getAccount();
            if (flow.getType() == FlowType.EXPENSE) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            } else if (flow.getType() == FlowType.INCOME) {
                account.setBalance(account.getBalance().add(flow.getAmount()));
            } else if (flow.getType() == FlowType.TRANSFER) {
                Account toAccount = flow.getTo();
                BigDecimal amount = flow.getAmount();
                BigDecimal convertedAmount = flow.getConvertedAmount();
                account.setBalance(account.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(convertedAmount));
                accountRepository.save(toAccount);
            }
            accountRepository.save(account);
        }
    }

    private void refundBalance(BalanceFlow flow) {
        if (flow.getConfirm() && flow.getAccount() != null) {
            Account account = flow.getAccount();
            if (flow.getType() == FlowType.EXPENSE) {
                account.setBalance(account.getBalance().add(flow.getAmount()));
            } else if (flow.getType() == FlowType.INCOME) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            } else if (flow.getType() == FlowType.TRANSFER) {
                Account toAccount = flow.getTo();
                BigDecimal amount = flow.getAmount();
                BigDecimal convertedAmount = flow.getConvertedAmount();
                account.setBalance(account.getBalance().add(amount));
                toAccount.setBalance(toAccount.getBalance().subtract(convertedAmount));
                accountRepository.save(toAccount);
            } else if (flow.getType() == FlowType.ADJUST) {
                account.setBalance(account.getBalance().subtract(flow.getAmount()));
            }
            accountRepository.save(account);
        }
    }

    @Transactional(readOnly = true)
    // 不加transaction会报错
    // org.hibernate.LazyInitializationException: could not initialize proxy [tech.jiukuai.bookkeeping.user.account.Account#1] - no Session
    public Page<BalanceFlowDetails> query(BalanceFlowQueryForm request, Pageable page) {
        Group group = sessionUtil.getCurrentGroup();
        Page<BalanceFlow> entityPage = balanceFlowRepository.findAll(request.buildPredicate(group), page);
        return entityPage.map(balanceFlowMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public BalanceFlowDetails get(int id) {
        return balanceFlowMapper.toDetails(baseService.findFlowById(id));
    }

    @Transactional(readOnly = true)
    public BigDecimal[] statistics(BalanceFlowQueryForm request) {
        var result = new BigDecimal[3];
        Group group = sessionUtil.getCurrentGroup();
        QBalanceFlow balanceFlow = QBalanceFlow.balanceFlow;
        result[0] = balanceFlowRepository.calcSum(request.buildExpensePredicate(group), balanceFlow.convertedAmount, balanceFlow);
        result[1] = balanceFlowRepository.calcSum(request.buildIncomePredicate(group), balanceFlow.convertedAmount, balanceFlow);
        result[2] = result[1].subtract(result[0]);
        return result;
    }

    public boolean remove(int id) {
        BalanceFlow entity = baseService.findFlowById(id);
        refundBalance(entity);
        // 要先删除文件
        flowFileRepository.deleteByFlow(entity);
        balanceFlowRepository.delete(entity);
        return true;
    }

    private boolean categoryEquals(Set<CategoryRelation> categories1, List<CategoryRelationForm> categories2) {
        if (categories1.size() != categories2.size()) {
            return false;
        }
        for (var i : categories1) {
            boolean found = false;
            for (var j : categories2) {
                if (j.equals(i)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private boolean tagEquals(Set<Integer> tags1, Set<TagRelation> tags2) {
        if (tags1.size() != tags2.size()) {
            return false;
        }
        for (var i : tags2) {
            boolean found = false;
            for (var j : tags1) {
                if (j.equals(i.getTag().getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public boolean update(Integer id, BalanceFlowUpdateForm form) {
        // checkBeforeUpdate
        BalanceFlow entity = baseService.findFlowById(id);
        Book book = entity.getBook();
        checkBeforeUpdate(form, entity, book);
        BalanceFlowMapper.updateEntity(form, entity);
        boolean refundFlag = false;
        Account newAccount = entity.getAccount();
        BigDecimal newAmount = entity.getAmount();
        BigDecimal newConvertedAmount = entity.getConvertedAmount();
        Account newTo = entity.getTo();
        // 判断账户是否更新
        if(!Objects.equals(entity.getAccount() != null ? entity.getAccount().getId() : null, form.getAccountId())) {
            newAccount = baseService.findAccountById(form.getAccountId());
            refundFlag = true;
        }
        if (entity.getType() == FlowType.EXPENSE || entity.getType() == FlowType.INCOME) {
            // 传入的categories为空，代表不修改。
            if (!CollectionUtils.isEmpty(form.getCategories()) && !categoryEquals(entity.getCategories(), form.getCategories())) {
                newAmount = form.getCategories().stream().map(CategoryRelationForm::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (entity.getAmount().compareTo(newAmount) != 0) {
                    refundFlag = true;
                }
                if (newAccount == null || newAccount.getCurrencyCode().equals(book.getDefaultCurrencyCode())) {
                    newConvertedAmount = newAmount;
                } else {
                    newConvertedAmount = form.getCategories().stream().map(CategoryRelationForm::getConvertedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                if (entity.getAmount().compareTo(newAmount) != 0) {
                    // 不然更新金额，标签对应的金额不更新
                    // 只有更新金额才自动更新标签的金额，更新分类不更新标签的金额
                    for (TagRelation i : entity.getTags()) {
                        i.setAmount(newAmount);
                        i.setConvertedAmount(newConvertedAmount);
                    }
                }
                entity.getCategories().clear();
                categoryRelationService.addRelation(form.getCategories(), entity, book, newAccount);
            }
        } else if (entity.getType() == FlowType.TRANSFER) {
            if (form.getToId() != null && !form.getToId().equals(entity.getTo().getId())) {
                newTo = baseService.findAccountById(form.getToId());
                refundFlag = true;
            }
            if (form.getAmount() != null && form.getAmount().compareTo(entity.getAmount()) != 0) {
                newAmount = form.getAmount();
                refundFlag = true;
            }
            if (newAccount.getCurrencyCode().equals(newTo.getCurrencyCode())) {
                newConvertedAmount = newAmount;
            } else {
                // 这种情况form.getConvertedAmount()一定不为空
                if (form.getConvertedAmount().compareTo(entity.getConvertedAmount()) != 0) {
                    newConvertedAmount = form.getConvertedAmount();
                    refundFlag = true;
                }
            }
        }
        if (refundFlag && entity.getConfirm()) {
            refundBalance(entity);
        }
        entity.setAccount(newAccount);
        entity.setAmount(newAmount);
        entity.setTo(newTo);
        entity.setConvertedAmount(newConvertedAmount);
        if (form.getPayeeId() != null) {
            if (entity.getPayee() == null || !form.getPayeeId().equals(entity.getPayee().getId())) {
                Payee payee = payeeRepository.findOneByBookAndId(book, form.getPayeeId()).orElseThrow(ItemNotFoundException::new);
                entity.setPayee(payee);
            }
        } else {
            entity.setPayee(null);
        }
        // 传入null代表不修改，传入空数组[]，代表清空。
        if (form.getTags() != null && !tagEquals(form.getTags(), entity.getTags())) {
            entity.getTags().clear();
            tagRelationService.addRelation(form.getTags(), entity, book, newAccount);
        }
        balanceFlowRepository.save(entity);
        if (refundFlag && entity.getConfirm()) {
            confirmBalance(entity);
        }
        return true;
    }

    public boolean confirm(Integer id) {
        BalanceFlow entity = baseService.findFlowById(id);
        entity.setConfirm(true);
        confirmBalance(entity);
        balanceFlowRepository.save(entity);
        return true;
    }

    public FlowFileDetails addFile(Integer id, MultipartFile file) {
        FlowFile flowFile = new FlowFile();
        try {
            flowFile.setData(file.getBytes());
        } catch (IOException e) {
            throw new FailureMessageException("add.flow.file.fail");
        }
        flowFile.setCreator(sessionUtil.getCurrentUser());
        flowFile.setFlow(baseService.findFlowById(id));
        flowFile.setCreateTime(System.currentTimeMillis());
        flowFile.setSize(file.getSize());
        flowFile.setContentType(file.getContentType());
        flowFile.setOriginalName(file.getOriginalFilename());
        flowFileRepository.save(flowFile);
        return FlowFileMapper.toDetails(flowFile);
    }

    public List<FlowFileDetails> getFiles(Integer id) {
        BalanceFlow flow = baseService.findFlowById(id);
        List<FlowFile> flowFiles = flowFileRepository.findByFlow(flow);
        return flowFiles.stream().map(FlowFileMapper::toDetails).toList();
    }

}
