package com.moneynote.tag;

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
public class TagService {

    private final TagRepository tagRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<TagListItem> list(TagQueryForm form, Pageable page) {
        Long bookId = bookService.resolveBookId(form.getBookId());
        Page<Tag> entityPage = tagRepository.findAll(buildSpec(bookId, form), page);
        return entityPage.map(item -> {
            TagListItem listItem = new TagListItem();
            listItem.setId(item.getId());
            listItem.setName(item.getName());
            listItem.setNotes(item.getNotes());
            listItem.setCanExpense(item.getCanExpense());
            listItem.setCanIncome(item.getCanIncome());
            listItem.setCanTransfer(item.getCanTransfer());
            return listItem;
        });
    }

    /**
     * 组装列表过滤条件：账本必须匹配，可选的 canExpense/canIncome/canTransfer
     * 三者只在此处按 true 过滤，互不互斥、可任意组合（即"全部"= 三者都不传）。
     */
    private Specification<Tag> buildSpec(Long bookId, TagQueryForm form) {
        return (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("book").get("id"), bookId);
            if (Boolean.TRUE.equals(form.getCanExpense())) {
                predicate = cb.and(predicate, cb.equal(root.get("canExpense"), Boolean.TRUE));
            }
            if (Boolean.TRUE.equals(form.getCanIncome())) {
                predicate = cb.and(predicate, cb.equal(root.get("canIncome"), Boolean.TRUE));
            }
            if (Boolean.TRUE.equals(form.getCanTransfer())) {
                predicate = cb.and(predicate, cb.equal(root.get("canTransfer"), Boolean.TRUE));
            }
            return predicate;
        };
    }

    @Transactional
    public void create(TagCreateForm form) {
        Long bookId = bookService.getCurrentBookId();
        if (tagRepository.existsByBookIdAndName(bookId, form.getName())) {
            throw new ResourceDuplicateException("resource.tag", form.getName());
        }
        Tag entity = new Tag();
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setCanExpense(form.getCanExpense());
        entity.setCanIncome(form.getCanIncome());
        entity.setCanTransfer(form.getCanTransfer());
        entity.setBook(bookRepository.getReferenceById(bookId));
        tagRepository.save(entity);
    }

    @Transactional
    public void update(Long id, TagUpdateForm form) {
        Long bookId = bookService.getCurrentBookId();
        Tag entity = tagRepository.findByIdAndBookId(id, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.tag"));
        if (form.getName() != null) {
            if (!form.getName().equals(entity.getName())) {
                // 与 AccountService/BookService.update 同模式：只在名字真的变了时判重，
                // 普通 existsBy 即可，不需要 AndIdNot——命中的任何行都不会是自己
                if (tagRepository.existsByBookIdAndName(bookId, form.getName())) {
                    throw new ResourceDuplicateException("resource.tag", form.getName());
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
        if (form.getCanTransfer() != null) {
            entity.setCanTransfer(form.getCanTransfer());
        }
    }

    @Transactional
    public void delete(Long id) {
        Long bookId = bookService.getCurrentBookId();
        Tag entity = tagRepository.findByIdAndBookId(id, bookId).orElse(null);
        if (entity != null) {
            tagRepository.delete(entity);
        }
    }
}
