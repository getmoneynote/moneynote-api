package cn.biq.mn.balanceflow;

import cn.biq.mn.validation.AmountField;
import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import cn.biq.mn.categoryrelation.CategoryRelationForm;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class BalanceFlowUpdateForm {

    @TitleField
    private String title;

    @TimeField
    private Long createTime;

    private Integer account;

    @Valid
    // 因为Category为非空，所以更新传入空代表不修改。
    private List<CategoryRelationForm> categories;

    private Integer payee;

    private Set<Integer> tags;

    private Integer to;

    // 只有transfer和adjust会传
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

    @NotesField
    private String notes;

    private Boolean include;

}
