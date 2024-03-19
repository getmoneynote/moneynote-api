package cn.biq.mn.balanceflow;

import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import cn.biq.mn.categoryrelation.CategoryRelationForm;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class BalanceFlowUpdateForm {

    @TitleField
    private String title;

    @TimeField
    private Long createTime;

    private Integer payee;

    // 传入null代表不修改，传入空数组[]，代表清空。
    private Set<Integer> tags;

    @NotesField
    private String notes;

    private Boolean include;

}
