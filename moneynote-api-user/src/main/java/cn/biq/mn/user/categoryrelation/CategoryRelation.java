package cn.biq.mn.user.categoryrelation;

import cn.biq.mn.user.balanceflow.BalanceFlow;
import cn.biq.mn.user.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "t_user_category_relation")
@Getter @Setter
public class CategoryRelation extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Category category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private BalanceFlow balanceFlow;

    @Column(nullable = false)
    @NotNull
    @AmountField
    private BigDecimal amount; // 金额

    @AmountField
    private BigDecimal convertedAmount; // 金额


}
