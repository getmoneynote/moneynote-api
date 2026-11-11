package com.moneynote.category;

import com.moneynote.tree.TreeNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryTreeNode extends TreeNode<CategoryTreeNode> {

    private String notes;
    private Integer type;
    private Integer level;
    private Long bookId;

}
