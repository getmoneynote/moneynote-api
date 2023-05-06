package cn.biq.mn.user.balanceflow;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AmountField;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.TitleField;
import cn.biq.mn.user.categoryrelation.CategoryRelationForm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class BalanceFlowUpdateForm {

    @TitleField
    private String title;

    @TimeField
    private Long createTime;

    private Integer accountId;

    private Integer payeeId;

    // 传入null代表不修改，传入空数组[]，代表清空。
    private Set<Integer> tags;

    @Valid
    // 因为Category为非空，所以更新传入空代表不修改。
    private List<CategoryRelationForm> categories;

    private Integer toId;

    // 只有transfer和adjust会传
    @AmountField
    private BigDecimal amount;

    @AmountField
    private BigDecimal convertedAmount;

    @NotesField
    private String notes;

    private Boolean include;

}
