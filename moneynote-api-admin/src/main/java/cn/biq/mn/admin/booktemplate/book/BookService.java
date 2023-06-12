package cn.biq.mn.admin.booktemplate.book;

import cn.biq.mn.admin.booktemplate.category.CategoryMapper;
import cn.biq.mn.admin.booktemplate.payee.PayeeMapper;
import cn.biq.mn.admin.booktemplate.tag.TagMapper;
import cn.biq.mn.admin.entity.admin.Book;
import cn.biq.mn.admin.repository.admin.BookRepository;
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
public class BookService {

    private final BookRepository bookRepository;

    public boolean add(BookAddForm form) {
        if (bookRepository.existsByName(form.getName())) throw new ItemExistsException();
        Book book = new Book();
        book.setName(form.getName());
        book.setNotes(form.getNotes());
        book.setPreviewUrl(form.getPreviewUrl());
        book.setVisible(false);
        bookRepository.save(book);
        return true;
    }

    public Page<BookDetails> query(Pageable page, BookQueryForm form) {
        return bookRepository.findAll(form.buildPredicate(), page).map(BookMapper::toDetails);
    }

    public Page<BookDetails> queryVisible(Pageable page, BookQueryForm form) {
        form.setVisible(true);
        return bookRepository.findAll(form.buildPredicate(), page).map(BookMapper::toDetails);
    }

    public BookTemplateDetails getTemplateDetails(Integer id) {
        var details = new BookTemplateDetails();
        Book entity = bookRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        details.setName(entity.getName());
        details.setNotes(entity.getNotes());

        details.setCategories(entity.getCategories().stream().map(CategoryMapper::toDetails).toList());
        details.setTags(entity.getTags().stream().map(TagMapper::toDetails).toList());
        details.setPayees(entity.getPayees().stream().map(PayeeMapper::toDetails).toList());

        return details;
    }

    public boolean update(Integer id, BookAddForm form) {
        Book entity = bookRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        if (!entity.getName().equals(form.getName())) {
            if (bookRepository.existsByName(form.getName())) throw new ItemExistsException();
        }
        entity.setName(form.getName());
        entity.setNotes(form.getNotes());
        entity.setPreviewUrl(form.getPreviewUrl());
        bookRepository.save(entity);
        return true;
    }

    public boolean toggle(Integer id) {
        Book entity = bookRepository.findById(id).orElseThrow(ItemNotFoundException::new);
        entity.setVisible(!entity.getVisible());
        bookRepository.save(entity);
        return true;
    }

    public boolean remove(Integer id) {
        bookRepository.deleteById(id);
        return true;
    }

}
