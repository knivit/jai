package com.tsoft.jai.core;

import lombok.Getter;

import java.util.function.Supplier;

import static com.tsoft.jai.core.Panic.panic;

public class Option<T> {

    public static final int U32_DEFAULT_VALUE = 0;
    public static final String STR_DEFAULT_VALUE = "";

    public enum OptionEnum {
        Some,
        None
    }

    // immutable
    private static final Option<?> None = new Option<>();

    @Getter
    private final OptionEnum type;
    private T value;

    public static <T> Option<T> None() {
        return (Option<T>) None;
    }

    public static <T> Option<T> Some(T value) {
        return new Option<>(value);
    }

    public Option() {
        type = OptionEnum.None;
    }

    public Option(T value) {
        this.type = (value == null) ? OptionEnum.None : OptionEnum.Some;
        this.value = value;
    }

    public T getValue() {
        return expect();
    }

    public T expect() {
        return expect("");
    }

    public T expect(String msg) {
        return switch (type) {
            case Some -> value;
            case None -> panic(msg);
        };
    }

    public T unwrapOr(T def) {
        return switch (type) {
            case Some -> value;
            case None -> def;
        };
    }

    public T unwrapOrDefault(T def) {
        return unwrapOr(def);
    }

    public T unwrapOrElse(Supplier<T> supplier) {
        return switch (type) {
            case Some -> value;
            case None -> supplier.get();
        };
    }
}
