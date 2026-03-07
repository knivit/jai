package com.tsoft.jai.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public final class NumberUtils {

    public static Integer parseInt(String value, Consumer<String> errHandler) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            if (errHandler == null) {
                log.warn("Error parsing '{}' as int, returning null", value);
            } else {
                errHandler.accept(value);
            }
            return null;
        }
    }

    private NumberUtils() { }
}
