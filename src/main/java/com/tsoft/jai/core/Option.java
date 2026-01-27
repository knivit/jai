package com.tsoft.jai.core;

public final class Option {

    public static <T> T unwrapOr(T value, T def) {
        return (value == null) ? def : value;
    }

    private Option() { }
}
