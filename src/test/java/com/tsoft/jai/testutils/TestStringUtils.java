package com.tsoft.jai.testutils;

public final class TestStringUtils {

    public static String escapeRegexChars(String text) {
        if (text == null) {
            return text;
        }
        return text.replaceAll("\\\\", "\\\\\\\\");
    }

    public static String normalizeLineSeparators(String text) {
        return text.replace("\r\n", "\n")
            .replace("\r", "\n");
    }

    public static String normalizePathSeparators(String text) {
        return text.replace('\\', '/');
    }

    private TestStringUtils() { }
}
