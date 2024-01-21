package cn.biq.mn.category;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoryQueryForm {

    private Integer bookId;
    private CategoryType type;
    private String name;
    private Boolean enable;

    public Predicate buildPredicate() {
        QCategory category = QCategory.category;
        BooleanBuilder builder = new BooleanBuilder();
        if (bookId != null) {
            builder.and(category.book.id.eq(bookId));
        }
        if (type != null) {
            builder.and(category.type.eq(type));
        }
        if (enable != null) {
            builder.and(category.enable.eq(enable));
        }
        if (name != null) {
            builder.and(category.name.contains(name));
        }
        return builder;
    }

}
