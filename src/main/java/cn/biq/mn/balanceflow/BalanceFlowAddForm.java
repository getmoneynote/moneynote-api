package cn.biq.mn.balanceflow;

import cn.biq.mn.validation.AmountField;
import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import cn.biq.mn.categoryrelation.CategoryRelationForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class BalanceFlowAddForm {

    @NotNull
    private Integer book; // 默认为当前账本

    @NotNull
    private FlowType type;

    @TitleField
    private String title;

    @NotNull
    @TimeField
    private Long createTime;

    private Integer account;

    private Integer payee;

    private Set<Integer> tags;

    @Valid
//    @NotEmpty  //transfer和adjust可以为空
    private List<CategoryRelationForm> categories;

    private Integer to;

    // 只有transfer和adjust会传
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

    @NotesField
    private String notes;

    @NotNull
    private Boolean confirm; // 是否确认

    @NotNull
    private Boolean include; // 是否包含统计

}
