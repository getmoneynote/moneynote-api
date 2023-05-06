package cn.biq.mn.user.tag;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeNode;

@Getter
@Setter
public class TagDetails extends TreeNode<TagDetails> {

    private String notes;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;

}
