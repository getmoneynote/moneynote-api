package cn.biq.mn.admin.booktemplate.tag;

import cn.biq.mn.admin.entity.admin.QTag;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TagQueryForm {

    @NotNull
    private Integer book;
    private String name;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;

    public Predicate buildPredicate() {
        QTag tag = QTag.tag;
        BooleanBuilder builder = new BooleanBuilder(tag.book.id.eq(book));
        if (name != null) {
            builder.and(tag.name.contains(name));
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
