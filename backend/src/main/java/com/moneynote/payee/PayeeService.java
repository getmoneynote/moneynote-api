package com.moneynote.payee;

import com.moneynote.book.BookRepository;
import com.moneynote.book.BookService;
import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayeeService {

    private final PayeeRepository payeeRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<PayeeListItem> list(PayeeQueryForm form, Pageable page) {
        Long bookId = bookService.resolveBookId(form.getBookId());
        Page<Payee> entityPage = payeeRepository.findAll(buildSpec(bookId, form), page);
        return entityPage.map(item -> {
            PayeeListItem listItem = new PayeeListItem();
            listItem.setId(item.getId());
            listItem.setName(item.getName());
            listItem.setNotes(item.getNotes());
            listItem.setCanExpense(item.getCanExpense());
            listItem.setCanIncome(item.getCanIncome());
            return listItem;
        });
    }

    /** 可选过滤：canExpense/canIncome 仅在为 true 时过滤，互不互斥（"全部" = 都不传）。 */
    private Specification<Payee> buildSpec(Long bookId, PayeeQueryForm form) {
        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("book").get("id"), bookId);
            if (Boolean.TRUE.equals(form.getCanExpense())) {
                predicate = cb.and(predicate, cb.equal(root.get("canExpense"), Boolean.TRUE));
            }
            if (Boolean.TRUE.equals(form.getCanIncome())) {
                predicate = cb.and(predicate, cb.equal(root.get("canIncome"), Boolean.TRUE));
            }
            return predicate;
        };
    }

    @Transactional
    public void create(PayeeCreateForm form) {
        Long bookId = bookService.getCurrentBookId();
        if (payeeRepository.existsByBookIdAndName(bookId, form.getName())) {
            throw new ResourceDuplicateException("resource.payee", form.getName());
        }
        Payee entity = new Payee();
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        entity.setBook(bookRepository.getReferenceById(bookId));
        payeeRepository.save(entity);
    }

    @Transactional
    public void update(Long id, PayeeUpdateForm form) {
        Long bookId = bookService.getCurrentBookId();
        Payee entity = payeeRepository.findByIdAndBookId(id, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.payee"));
        if (form.getName() != null) {
            if (!form.getName().equals(entity.getName())) {
                // 与 AccountService/BookService.update 同模式：只在名字真的变了时判重，
                // 普通 existsBy 即可，不需要 AndIdNot——命中的任何行都不会是自己
                if (payeeRepository.existsByBookIdAndName(bookId, form.getName())) {
                    throw new ResourceDuplicateException("resource.payee", form.getName());
                }
                entity.setName(form.getName());
            }
        }
        if (form.getNotes() != null) {
            entity.setNotes(form.getNotes());
        }
        if (form.getCanExpense() != null) {
            entity.setCanExpense(form.getCanExpense());
        }
        if (form.getCanIncome() != null) {
            entity.setCanIncome(form.getCanIncome());
        }
    }

    @Transactional
    public void delete(Long id) {
        Long bookId = bookService.getCurrentBookId();
        Payee entity = payeeRepository.findByIdAndBookId(id, bookId).orElse(null);
        if (entity != null) {
            payeeRepository.delete(entity);
        }
    }
}
