package cn.biq.mn.payee;

import cn.biq.mn.exception.FailureMessageException;
import cn.biq.mn.exception.ItemExistsException;
import cn.biq.mn.balanceflow.BalanceFlowRepository;
import cn.biq.mn.base.BaseService;
import cn.biq.mn.book.Book;
import cn.biq.mn.utils.Limitation;
import cn.biq.mn.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PayeeService {

    private final PayeeRepository payeeRepository;
    private final BaseService baseService;
    private final BalanceFlowRepository balanceFlowRepository;
    private final SessionUtil sessionUtil;

    public PayeeDetails add(PayeeAddForm form) {
        Book book = baseService.findBookById(form.getBookId());
        // 限制每个账本的交易对象数量
        if (payeeRepository.countByBook(book) >= Limitation.payee_max_count) {
            throw new FailureMessageException("payee.max.count");
        }
        // 不能重复
        if (payeeRepository.existsByBookAndName(book, form.getName())) {
            throw new ItemExistsException();
        }
        Payee entity = PayeeMapper.toEntity(form);
        entity.setBook(book);
        payeeRepository.save(entity);
        return PayeeMapper.toDetails(entity);
    }

    @Transactional(readOnly = true)
    public Page<PayeeDetails> query(PayeeQueryForm form, Pageable page) {
        // 确保传入的bookId是自己组里面的。
        if (form.getBookId() != null) {
            baseService.findBookById(form.getBookId());
        } else {
            form.setBookId(sessionUtil.getCurrentBook().getId());
        }
        Page<Payee> entityPage = payeeRepository.findAll(form.buildPredicate(), page);
        return entityPage.map(PayeeMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public List<PayeeDetails> queryAll(PayeeQueryForm form) {
        // TODO 重构
        if (form.getBookId() == null) {
            return new ArrayList<>();
        }
        // 确保传入的bookId是自己组里面的。
        baseService.findBookById(form.getBookId());
        form.setEnable(true);
        List<Payee> entityList = payeeRepository.findAll(form.buildPredicate(), Sort.by(Sort.Direction.ASC, "sort"));
        return entityList.stream().map(PayeeMapper::toDetails).toList();
    }

    public boolean toggle(Integer id) {
        Payee entity = baseService.findPayeeById(id);
        entity.setEnable(!entity.getEnable());
        payeeRepository.save(entity);
        return true;
    }

    public boolean toggleCanExpense(Integer id) {
        Payee entity = baseService.findPayeeById(id);
        entity.setCanExpense(!entity.getCanExpense());
        payeeRepository.save(entity);
        return true;
    }

    public boolean toggleCanIncome(Integer id) {
        Payee entity = baseService.findPayeeById(id);
        entity.setCanIncome(!entity.getCanIncome());
        payeeRepository.save(entity);
        return true;
    }

    public boolean update(Integer id, PayeeUpdateForm form) {
        Payee entity = baseService.findPayeeById(id);
        Book book = entity.getBook();
        if (!entity.getName().equals(form.getName())) {
            if (StringUtils.hasText(form.getName())) {
                if (payeeRepository.existsByBookAndName(book, form.getName())) {
                    throw new ItemExistsException();
                }
            }
        }
        PayeeMapper.updateEntity(form, entity);
        payeeRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        Payee entity = baseService.findPayeeById(id);
        // 有账单关联
        if (balanceFlowRepository.existsByPayee(entity)) {
            throw new FailureMessageException("payee.delete.has.flow");
        }
        payeeRepository.delete(entity);
        return true;
    }

}
