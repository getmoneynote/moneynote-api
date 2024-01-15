package cn.biq.mn.tag;

import cn.biq.mn.tree.TreeNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagDetails extends TreeNode<TagDetails> {

    private String notes;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;
    private Integer level;

}
