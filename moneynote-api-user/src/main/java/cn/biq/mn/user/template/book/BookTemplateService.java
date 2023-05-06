package cn.biq.mn.user.template.book;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookTemplateService {

    private final BookTemplateRepository bookTemplateRepository;

    public Page<BookTemplateDetails> query(Pageable page, BookTemplateQueryForm form) {
        return bookTemplateRepository.findAll(form.buildPredicate(), page).map(BookTemplateMapper::toDetails);
    }

}
