package cn.biq.mn.admin.booktemplate.payee;

import cn.biq.mn.admin.entity.admin.QPayee;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PayeeQueryForm {

    @NotNull
    private Integer book;
    private String name;
    private Boolean canExpense;
    private Boolean canIncome;

    public Predicate buildPredicate() {
        QPayee payee = QPayee.payee;
        BooleanBuilder builder = new BooleanBuilder(payee.book.id.eq(book));
        if (name != null) {
            builder.and(payee.name.contains(name));
        }
        if (canExpense != null) {
            builder.and(payee.canExpense.eq(canExpense));
        }
        if (canIncome != null) {
            builder.and(payee.canIncome.eq(canIncome));
        }
        return builder;
    }

}
