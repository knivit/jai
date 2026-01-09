package com.tsoft.jai.utils;

import java.util.List;
import java.util.Map;

public final class CollectionsUtils {

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null) || map.isEmpty();
    }

    public static boolean isEmpty(List<?> list) {
        return (list == null) || list.isEmpty();
    }
}
