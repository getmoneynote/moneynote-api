package cn.biq.mn.user.balanceflow;

import cn.biq.mn.user.group.Group;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.utils.CalendarUtil;

import java.util.Set;

@Getter @Setter
public class BalanceFlowQueryForm {

    private Integer book;
    private FlowType type;
    private String title;
    private Double minAmount;
    private Double maxAmount;
    private Long minTime;
    private Long maxTime;
    private Integer account;
    private Set<Integer> payees;
    private Set<Integer> categories;
    private Set<Integer> tags;
    private Boolean confirm;
    private Boolean include;
    private Integer toId;
    private String notes;

    public Predicate buildPredicate(Group group) {
        QBalanceFlow balanceFlow = QBalanceFlow.balanceFlow;
        BooleanBuilder booleanBuilder = new BooleanBuilder(balanceFlow.group.eq(group));
        if (book != null) {
            booleanBuilder.and(balanceFlow.book.id.eq(book));
        }
        if (type != null) {
            booleanBuilder.and(balanceFlow.type.eq(type));
        }
        if (title != null) {
            booleanBuilder.and(balanceFlow.title.contains(title));
        }
        if (minAmount != null) {
            booleanBuilder.and(balanceFlow.amount.goe(minAmount));
        }
        if (maxAmount != null) {
            booleanBuilder.and(balanceFlow.amount.loe(maxAmount));
        }
        if (minTime != null) {
            // 时间设置为一天的开始
//            minTime = CalendarUtil.startOfDay(minTime);
            booleanBuilder.and(balanceFlow.createTime.goe(minTime));
        }
        if (maxTime != null) {
            // 时间设置为一天的结束
//            maxTime = CalendarUtil.endOfDay(maxTime);
            booleanBuilder.and(balanceFlow.createTime.loe(maxTime));
        }
        if (account != null) {
            BooleanBuilder builder1 = new BooleanBuilder(balanceFlow.account.id.eq(account));
            BooleanBuilder builder2 = new BooleanBuilder(balanceFlow.to.id.eq(account));
            booleanBuilder.and(builder1.or(builder2));
        }
        if (payees != null) {
            booleanBuilder.and(balanceFlow.payee.id.in(payees));
        }
        if (confirm != null) {
            booleanBuilder.and(balanceFlow.confirm.eq(confirm));
        }
        if (include != null) {
            booleanBuilder.and(balanceFlow.include.eq(include));
        }
        if (categories != null) {
            // https://stackoverflow.com/questions/21637636/spring-data-jpa-with-querydslpredicateexecutor-and-joining-into-a-collection
            booleanBuilder.and(balanceFlow.categories.any().category.id.in(categories));
        }
        if (tags != null) {
            booleanBuilder.and(balanceFlow.tags.any().tag.id.in(tags));
        }
        if (notes != null) {
            booleanBuilder.and(balanceFlow.notes.contains(notes));
        }
        return booleanBuilder;
    }

    public Predicate buildExpensePredicate(Group group) {
        QBalanceFlow balanceFlow = QBalanceFlow.balanceFlow;
        BooleanBuilder booleanBuilder = new BooleanBuilder(buildPredicate(group));
        return booleanBuilder.and(balanceFlow.type.eq(FlowType.EXPENSE));
    }

    public Predicate buildIncomePredicate(Group group) {
        QBalanceFlow balanceFlow = QBalanceFlow.balanceFlow;
        BooleanBuilder booleanBuilder = new BooleanBuilder(buildPredicate(group));
        return booleanBuilder.and(balanceFlow.type.eq(FlowType.INCOME));
    }

}
