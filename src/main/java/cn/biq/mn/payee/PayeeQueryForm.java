package cn.biq.mn.payee;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PayeeQueryForm {

    private Integer bookId;
    private String name;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;

    public Predicate buildPredicate() {
        QPayee payee = QPayee.payee;
        BooleanBuilder builder = new BooleanBuilder();
        if (bookId != null) {
            builder.and(payee.book.id.eq(bookId));
        } else {
            builder.and(payee.book.id.eq(-1));
        }
        if (name != null) {
            builder.and(payee.name.contains(name));
        }
        if (enable != null) {
            builder.and(payee.enable.eq(enable));
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
