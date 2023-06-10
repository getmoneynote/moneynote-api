package cn.biq.mn.user.book.tpl;

import cn.biq.mn.base.tree.TreeNode;
import cn.biq.mn.user.category.CategoryType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoryTemplate extends TreeNode<CategoryTemplate> {

    private String notes;
    private Integer level;
    private CategoryType type;

}
