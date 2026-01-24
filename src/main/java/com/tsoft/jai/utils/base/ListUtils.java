package com.tsoft.jai.utils.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ListUtils {

    public static <T> List<T> of(T ... values) {
        List<T> list = new ArrayList<>();
        if (values == null) {
            return list;
        }
        list.addAll(Arrays.asList(values));
        return list;
    }

    private ListUtils() { }
}
