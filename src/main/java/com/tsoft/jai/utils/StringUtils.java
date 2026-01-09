package com.tsoft.jai.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public final class StringUtils {

    public static boolean isBlank(String str) {
        return (str == null) || str.isBlank();
    }

    public static Tuple<String, String> splitOnce(String str, char ch) {
        if (str == null) {
            return new Tuple<>(null, null);
        }
        int n = str.indexOf(ch);
        if (n == -1) {
            return new Tuple<>(str, null);
        }
        return new Tuple<>(str.substring(0, n), str.substring(n + 1));
    }

    public static String format(String text, Object ... args) {
        if (text == null || args == null) {
            return text;
        }

        for (int i = 0; i < args.length; i ++) {
            int n = text.indexOf("{}");
            if (n < 0) {
                break;
            }
            String arg = (args[i] == null) ? "" : args[i].toString();
            text = text.substring(0, n) + arg + text.substring(n + 2);
        }

        return text;
    }

    public static char getFirstChar(String text) {
        for (int i = 0; i < text.length(); i ++) {
            char ch = text.charAt(i);
            if (ch > ' ') {
                return ch;
            }
        }
        return '\0';
    }

    public static String readFile(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (Exception ex) {
            log.warn("Error reading file '{}'", file.getAbsolutePath(), ex);
            return null;
        }
    }

    private StringUtils() { }
}
