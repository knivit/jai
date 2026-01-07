package com.tsoft.jai.testutils;

public final class TestStringUtils {

    public static String normalizeLineSeparators(String input) {
        return input.replace("\r\n", "\n")
            .replace("\r", "\n");
    }

    private TestStringUtils() { }
}
