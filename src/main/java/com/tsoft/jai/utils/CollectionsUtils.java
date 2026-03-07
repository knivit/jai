package com.tsoft.jai.utils;

import java.util.Collection;
import java.util.Map;

public final class CollectionsUtils {

    public static boolean isEmpty(String str) {
        return (str == null) || str.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null) || map.isEmpty();
    }

    public static boolean isEmpty(Collection<?> list) {
        return (list == null) || list.isEmpty();
    }
}
