package cn.biq.mn.user.account;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.TitleField;
import java.math.BigDecimal;

@Getter @Setter
public class AdjustBalanceAddForm {

    @NotNull
    private Integer bookId;

    @NotNull
    @TimeField
    private Long createTime;

    @TitleField
    private String title;

    @NotesField
    private String notes;

    @NotNull
    @AmountField
    private BigDecimal balance;

}
