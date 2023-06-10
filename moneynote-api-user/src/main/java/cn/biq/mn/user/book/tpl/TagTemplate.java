package cn.biq.mn.user.book.tpl;

import cn.biq.mn.base.tree.TreeNode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TagTemplate extends TreeNode<TagTemplate> {

    private String notes;
    private Boolean canExpense;
    private Boolean canIncome;
    private Boolean canTransfer;
    private Integer level;

}
