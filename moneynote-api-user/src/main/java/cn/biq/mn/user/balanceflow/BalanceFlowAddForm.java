package cn.biq.mn.user.balanceflow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.validation.TitleField;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.user.categoryrelation.CategoryRelationForm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class BalanceFlowAddForm {

    @NotNull
    private Integer bookId; // 默认为当前账本

    @NotNull
    private FlowType type;

    @TitleField
    private String title;

    @NotNull
    @TimeField
    private Long createTime;

    private Integer accountId;

    private Integer payeeId;

    private Set<Integer> tags;

    @Valid
//    @NotEmpty  //transfer和adjust可以为空
    private List<CategoryRelationForm> categories;

    private Integer toId;

    // 只有transfer和adjust会传
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

    @NotesField
    private String notes;

    @NotNull
    private Boolean confirm;

    @NotNull
    private Boolean include;

    @NotNull
    private Boolean updateBalance = true;

}
