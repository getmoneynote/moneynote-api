package cn.biq.mn.user.template.category;

import cn.biq.mn.user.category.CategoryType;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.tree.TreeNode;


@Getter
@Setter
public class CategoryTemplateDetails extends TreeNode<CategoryTemplateDetails> {

    private String notes;
    private Integer level;
    private CategoryType type;

}
