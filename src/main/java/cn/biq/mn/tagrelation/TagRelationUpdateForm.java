package cn.biq.mn.tagrelation;

import cn.biq.mn.validation.AmountField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class TagRelationUpdateForm {

    @NotNull
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

}
