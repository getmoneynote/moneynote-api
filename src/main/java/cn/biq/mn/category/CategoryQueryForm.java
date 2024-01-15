package cn.biq.mn.category;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CategoryQueryForm {

    private Integer bookId;
    private Integer type;
    private String name;
    private Boolean enable;
    //处理修改时，已禁用的情况。
    private Set<Integer> keeps;

    public Predicate buildPredicate() {
        QCategory category = QCategory.category;
        BooleanBuilder builder = new BooleanBuilder();
        if (bookId != null) {
            builder.and(category.book.id.eq(bookId));
        } else {
            builder.and(category.book.id.eq(-1));
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
