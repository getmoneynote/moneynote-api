package com.moneynote.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TreeUtils {

    //https://stackoverflow.com/questions/15792794/convert-parent-child-array-to-tree
    public static <E extends TreeNode<E>> List<E> buildTree(List<E> flat) {
        List<E> result = new ArrayList<>();
        Map<Long, E> map = new HashMap<>();
        for (E item : flat) {
            map.put(item.getId(), item);
        }
        for (E item : flat) {
            TreeNode<E> parent = map.get(item.getParentId());
            if (parent != null) {
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(item);
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
