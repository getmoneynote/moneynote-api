package cn.biq.mn.book.tpl;

import cn.biq.mn.tree.TreeNode;
import cn.biq.mn.category.CategoryType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CategoryTemplate extends TreeNode<CategoryTemplate> {

    private String notes;
    private Integer level;
    private CategoryType type;

}
