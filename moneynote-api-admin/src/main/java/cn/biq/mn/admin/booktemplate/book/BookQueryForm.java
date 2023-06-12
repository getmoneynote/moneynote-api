package cn.biq.mn.admin.booktemplate.book;

import cn.biq.mn.admin.entity.admin.QBook;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class BookQueryForm {

    private String name;
    private Boolean visible;

    public Predicate buildPredicate() {
        QBook book = QBook.book;
        BooleanBuilder builder = new BooleanBuilder();
        if (name != null) {
            builder.and(book.name.contains(name));
        }
        if (visible != null) {
            builder.and(book.visible.eq(visible));
        }
        return builder;
    }

}
