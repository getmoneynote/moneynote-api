package cn.biq.mn.user.book;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cn.biq.mn.user.group.Group;

@Getter
@Setter
@NoArgsConstructor
public class BookQueryForm {

    private Boolean enable;
    private String name;
    private Integer keep;

    public Predicate buildPredicate(Group group) {
        QBook book = QBook.book;
        BooleanBuilder expression = new BooleanBuilder(book.group.eq(group));
        if (enable != null) {
           expression.and(book.enable.eq(enable));
        }
        if (name != null) {
            expression.and(book.name.contains(name));
        }
        return expression;
    }

}
