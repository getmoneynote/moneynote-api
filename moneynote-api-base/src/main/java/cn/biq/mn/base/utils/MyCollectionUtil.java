package cn.biq.mn.base.utils;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

public final class MyCollectionUtil {

    public static <T> boolean hasDuplicate(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) return false;
        Set<T> set = new HashSet<>();
        for (T element : collection) {
            if (set.contains(element)) return true;
            else set.add(element);
        }
        return false;
    }

    public static <T> List<T> union(List<T> list1, List<T> list2) {
        return Stream.concat(list1.stream(), list2.stream()).toList();
    }

    public static <T> List<T> unionWithoutDuplicates(List<T> list1, List<T> list2) {
        // set 改变了顺序
//        Set<T> set = new HashSet<>();
//        set.addAll(list1);
//        set.addAll(list2);
//        return new ArrayList<>(set);
        List<T> result = new ArrayList<>(list1);
        list2.forEach(e -> {
            if (!result.contains(e)) {
                result.add(e);
            }
        });
        return result;
    }

}
