package cn.biq.mn.base.utils;

import org.springframework.util.CollectionUtils;
import cn.biq.mn.base.base.BaseEntity;

import java.util.List;

public final class CommonUtils {

    public static <T extends BaseEntity> T findFirstById(List<T> collection, Integer id) {
        if (CollectionUtils.isEmpty(collection)) return null;
        for (T element : collection) {
            if (element.getId().equals(id)) return element;
        }
        return null;
    }

    // https://stackoverflow.com/questions/5223044/is-there-a-java-equivalent-to-null-coalescing-operator-in-c
    // https://stackoverflow.com/questions/56223120/what-does-an-question-mark-and-dot-in-dart-language
    // https://stackoverflow.com/questions/4390141/java-operator-for-checking-null-what-is-it-not-ternary
    public static <T> T firstNonNull(T... params) {
        for (T param : params)
            if (param != null)
                return param;
        return null;
    }

    public static int showType(boolean success) {
        if (success) {
            return Constant.SHOW_TYPE_SUCCESS;
        } else {
            return Constant.SHOW_TYPE_ERROR_MESSAGE;
        }
    }

}
