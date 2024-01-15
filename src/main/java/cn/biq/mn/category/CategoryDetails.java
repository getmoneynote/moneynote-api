package cn.biq.mn.category;

import cn.biq.mn.tree.TreeNode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryDetails extends TreeNode<CategoryDetails> {

    private CategoryType type;
    private String notes;
    private Boolean enable;
    private Boolean canExpense;
    private Boolean canIncome;
    private Integer level;

}
