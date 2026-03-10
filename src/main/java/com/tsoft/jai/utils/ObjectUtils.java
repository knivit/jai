package com.tsoft.jai.utils;

public final class ObjectUtils {

    public static <T> T nvl(T a, T b) {
        return (a == null) ? b : a;
    }

    private ObjectUtils() { }
}
