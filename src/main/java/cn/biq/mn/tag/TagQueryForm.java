package cn.biq.mn.tag;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class TagQueryForm {

    private Integer bookId;
    private String name;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;

    public Predicate buildPredicate() {
        QTag tag = QTag.tag;
        BooleanBuilder builder = new BooleanBuilder();
        if (bookId != null) {
            builder.and(tag.book.id.eq(bookId));
        }
        if (name != null) {
            builder.and(tag.name.contains(name));
        }
        if (enable != null) {
            builder.and(tag.enable.eq(enable));
        }
        if (canExpense != null) {
            builder.and(tag.canExpense.eq(canExpense));
        }
        if (canIncome != null) {
            builder.and(tag.canIncome.eq(canIncome));
        }
        if (canTransfer != null) {
            builder.and(tag.canTransfer.eq(canTransfer));
        }
        return builder;
    }

}
