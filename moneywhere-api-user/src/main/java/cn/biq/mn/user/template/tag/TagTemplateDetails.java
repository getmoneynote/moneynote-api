package cn.biq.mn.user.template.tag;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeNode;


@Getter
@Setter
public class TagTemplateDetails extends TreeNode<TagTemplateDetails> {

    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;
    private Integer level;


}
