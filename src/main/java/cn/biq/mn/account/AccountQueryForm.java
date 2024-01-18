package cn.biq.mn.account;

import cn.biq.mn.group.Group;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountQueryForm {

    private AccountType type;
    private Boolean enable;
    private String name;
    private Double minBalance;
    private Double maxBalance;
    private String no;
    private Boolean include;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransferFrom;
    private Boolean canTransferTo;
    private String currencyCode;

    public Predicate buildPredicate(Group group) {
        QAccount account = QAccount.account;
        BooleanBuilder expression = new BooleanBuilder(account.group.eq(group));
        if (enable != null) {
           expression.and(account.enable.eq(enable));
        }
        if (type != null) {
            expression.and(account.type.eq(type));
        }
        if (name != null) {
            expression.and(account.name.contains(name));
        }
        if (minBalance != null) {
            expression.and(account.balance.goe(minBalance));
        }
        if (maxBalance != null) {
            expression.and(account.balance.loe(maxBalance));
        }
        if (no != null) {
            expression.and(account.no.contains(no));
        }
        if (include != null) {
            expression.and(account.include.eq(include));
        }
        if (canExpense != null) {
            expression.and(account.canExpense.eq(canExpense));
        }
        if (canIncome != null) {
            expression.and(account.canIncome.eq(canIncome));
        }
        if (canTransferFrom != null) {
            expression.and(account.canTransferFrom.eq(canTransferFrom));
        }
        if (canTransferTo != null) {
            expression.and(account.canTransferTo.eq(canTransferTo));
        }
        if (currencyCode != null) {
            expression.and(account.currencyCode.eq(currencyCode));
        }
        return expression;
    }

}
