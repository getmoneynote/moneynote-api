package cn.biq.mn.account;

import cn.biq.mn.validation.AmountField;
import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class AdjustBalanceAddForm {

    @NotNull
    private Integer book;

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
