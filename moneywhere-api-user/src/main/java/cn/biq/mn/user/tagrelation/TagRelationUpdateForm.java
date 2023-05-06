package cn.biq.mn.user.tagrelation;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;

import java.math.BigDecimal;

@Getter @Setter
public class TagRelationUpdateForm {

    @NotNull
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

}
