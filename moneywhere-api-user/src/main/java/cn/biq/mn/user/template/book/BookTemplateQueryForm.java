package cn.biq.mn.user.template.book;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class BookTemplateQueryForm {

    private String name;

    public Predicate buildPredicate() {
        QBookTemplate book = QBookTemplate.bookTemplate;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(book.visible.eq(true));
        if (name != null) {
            builder.and(book.name.contains(name));
        }
        return builder;
    }

}
