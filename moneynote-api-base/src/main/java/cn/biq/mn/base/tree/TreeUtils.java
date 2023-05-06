package cn.biq.mn.base.tree;

import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TreeUtils {

    //https://stackoverflow.com/questions/15792794/convert-parent-child-array-to-tree
    public static <E extends TreeNode<E>> List<E> buildTree(List<E> flat) {
        List<E> result = new ArrayList<>();
        Map<Integer, E> map = new HashMap<>();
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

    public static <E extends TreeEntity> List<E> getChildren(E node, List<E> flat) {
        List<E> result = new ArrayList<>();
        for (E item : flat) {
            if (item.getParent() != null && node.getId().equals(item.getParent().getId())) {
                result.add(item);
            }
        }
        return result;
    }

    public static <E extends TreeEntity> List<E> getOffspring(E node, List<E> flat) {
        List<E> result = new ArrayList<>();
        List<E> children = getChildren(node, flat);
        if (!CollectionUtils.isEmpty(children)) {
            result.addAll(children);
            for (E item : children) {
                result.addAll(getOffspring(item, flat));
            }
        }
        return result;
    }

}
