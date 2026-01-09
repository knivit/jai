package com.tsoft.jai.unicodesegmentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class Word {

    public static boolean isAscii(String word) {
        if (isBlank(word)) {
            return false;
        }
        for (int i = 0; i < word.length(); i ++) {
            char ch = word.charAt(i);
            if (!Character.isLetter(ch)) {
                return false;
            }
        }
        return true;
    }

    public static List<String> unicodeWords(String text) {
        if (isBlank(text)) {
            return Collections.emptyList();
        }
        int n = 0;
        int state = 0;      // 0 - Nothing, 1 - a word, 2 - a number
        List<String> result = new ArrayList<>();
        for (int i = 0; i < text.length(); i ++) {
            char ch = text.charAt(i);
            if (isDelimiter(ch)) {
                if (state == 0) {
                    continue;
                }

                result.add(text.substring(n, i));
                state = 0;
                continue;
            }

            if (state == 0) {
                if ((ch >= '0' && ch <= '9') || ch == '.') {
                    state = 2;
                } else {
                    state = 1;
                }
                n = i;
            }
        }
        return result;
    }

    private static boolean isDelimiter(char ch) {
        return ch <= ' ' || ch == '"' || ch == '?' || ch == '(' || ch == ')' || ch == ':' || ch == ',' || ch == '+' || ch == '-';
    }

    private Word() { }
}
