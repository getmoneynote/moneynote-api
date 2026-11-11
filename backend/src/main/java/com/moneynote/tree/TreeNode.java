package com.moneynote.tree;

import com.moneynote.base.IdAndNameDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TreeNode<T> extends IdAndNameDetails {

    private Long parentId;
    private List<T> children;

    private IdAndNameDetails parent;

}
