package com.moneynote.book;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyRepository;
import com.moneynote.exception.ResourceDuplicateException;
import com.moneynote.exception.ResourceInvalidException;
import com.moneynote.exception.ResourceNotFoundException;
import com.moneynote.exception.ResourceProtectedException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.user.SessionManager;
import com.moneynote.user.User;
import com.moneynote.user.UserRepository;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CurrencyRepository currencyRepository;
    private final UserRepository userRepository;

    /** 当前账本必须存在且属于当前用户，否则 400；供各业务模块复用，返回当前账本 ID。 */
    @Transactional(readOnly = true)
    public Long getCurrentBookId() {
        Long bookId = SessionManager.getCurrentUser().getCurrentBookId();
        Long userId = CurrentUserHolder.get();
        if (bookId == null || !bookRepository.existsByIdAndUserId(bookId, userId)) {
            throw new ResourceInvalidException("error.book.not.found");
        }
        return bookId;
    }

    /** 当前账本必须存在且属于当前用户，否则 400；返回完整 Book 实体（供取记账币种等）。 */
    @Transactional(readOnly = true)
    public Book getCurrentBook() {
        Long userId = CurrentUserHolder.get();
        Long bookId = SessionManager.getCurrentUser().getCurrentBookId();
        if (bookId == null) {
            throw new ResourceInvalidException("error.book.not.found");
        }
        return bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceInvalidException("error.book.not.found"));
    }

    /** 解析指定账本实体：bookId 为空返回当前账本；传了必须属于当前用户，否则 400。 */
    @Transactional(readOnly = true)
    public Book resolveBook(Long bookId) {
        if (bookId == null) {
            return getCurrentBook();
        }
        Long userId = CurrentUserHolder.get();
        return bookRepository.findByIdAndUserId(bookId, userId)
                .orElseThrow(() -> new ResourceInvalidException("error.book.not.found"));
    }

    /** 解析指定账本 id：bookId 为空返回当前账本 id；传了必须属于当前用户，否则 400。 */
    @Transactional(readOnly = true)
    public Long resolveBookId(Long bookId) {
        if (bookId == null) {
            return getCurrentBookId();
        }
        Long userId = CurrentUserHolder.get();
        if (!bookRepository.existsByIdAndUserId(bookId, userId)) {
            throw new ResourceInvalidException("error.book.not.found");
        }
        return bookId;
    }

    @Transactional
    // 如果没有数据库唯一约束，这里存在TOCTOU（检查后再操作）竞态
    public void create(BookCreateForm form) {
        Long userId = CurrentUserHolder.get();
        if (bookRepository.existsByUserIdAndName(userId, form.getName())) {
            throw new ResourceDuplicateException("resource.book", form.getName());
        }
        // 记账币种必须属于当前用户
        Currency currency = currencyRepository.findByIdAndUserId(form.getCurrencyId(), userId)
                .orElseThrow(() -> new ResourceInvalidException("error.invalid.argument"));
        Book entity = new Book();
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCurrency(currency);
        bookRepository.save(entity);
    }

    @Transactional
    public void update(Long id, BookUpdateForm form) {
        Long userId = CurrentUserHolder.get();
        Book entity = bookRepository.findByIdAndUserId(id, userId).orElse(null);
        if (entity == null) {
            throw new ResourceNotFoundException("resource.book");
        }
        if (form.getName() != null) {
            if (!form.getName().equals(entity.getName())) {
                if (bookRepository.existsByUserIdAndName(userId, form.getName())) {
                    throw new ResourceDuplicateException("resource.book", form.getName());
                }
                entity.setName(form.getName());
            }
        }
        if (form.getNotes() != null) {
            entity.setNotes(form.getNotes());
        }
        // entity 为持久态对象，事务提交时自动 flush，无需显式 save
    }

    @Transactional
    public void delete(Long id) {
        Long userId = CurrentUserHolder.get();
        bookRepository.findByIdAndUserId(id, userId).ifPresent(entity -> {
            // 当前账本被删除后 user.currentBookId 会指向不存在的账本，禁止删除
            if (Objects.equals(id, SessionManager.getCurrentUser().getCurrentBookId())) {
                throw new ResourceProtectedException("error.book.remove.current");
            }
            bookRepository.delete(entity);
        });
    }

    @Transactional
    public void setDefault(Long id) {
        Long userId = CurrentUserHolder.get();
        if (!bookRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("resource.book");
        }
        User user = SessionManager.getCurrentUser();
        user.setCurrentBookId(id);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<BookListItem> list(Pageable page) {
        Long userId = CurrentUserHolder.get();
        Long currentBookId = SessionManager.getCurrentUser().getCurrentBookId();
        Page<Book> entityPage = bookRepository.findByUserId(userId, page);
        return entityPage.map(item -> {
            BookListItem listItem = new BookListItem();
            listItem.setId(item.getId());
            listItem.setName(item.getName());
            listItem.setNotes(item.getNotes());
            listItem.setCurrencyId(item.getCurrency().getId());
            listItem.setCurrencyName(item.getCurrency().getName());
            listItem.setIsCurrent(Objects.equals(item.getId(), currentBookId));
            return listItem;
        });
    }

}
