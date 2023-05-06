package cn.biq.mn.admin.booktemplate.payee;

import cn.biq.mn.admin.booktemplate.book.Book;
import cn.biq.mn.admin.booktemplate.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.biq.mn.base.exception.ItemExistsException;
import cn.biq.mn.base.exception.ItemNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class PayeeService {

    private final BookRepository bookRepository;
    private final PayeeRepository payeeRepository;

    public boolean add(PayeeAddForm form) {
        Book book = bookRepository.findById(form.getBookId()).orElseThrow(ItemNotFoundException::new);
        if (payeeRepository.existsByBookAndName(book, form.getName())) throw new ItemExistsException();
        Payee entity = new Payee();
        entity.setBook(book);
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        payeeRepository.save(entity);
        return true;
    }

    public Page<PayeeDetails> query(Pageable page, PayeeQueryForm form) {
        return payeeRepository.findAll(form.buildPredicate(), page).map(PayeeMapper::toDetails);
    }

    public boolean update(Integer id, PayeeUpdateForm form) {
        Payee entity = payeeRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!entity.getName().equals(form.getName())) {
            if (payeeRepository.existsByBookAndName(entity.getBook(), form.getName())) throw new ItemExistsException();
        }
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        payeeRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        payeeRepository.deleteById(id);
        return true;
    }

}
