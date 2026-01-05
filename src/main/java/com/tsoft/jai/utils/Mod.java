package com.tsoft.jai.utils;

import java.util.regex.Pattern;

public final class Mod {

    private static final Pattern THINK_TAG_RE = Pattern.compile("(?s)^\\s*<think>.*?</think>(\\s*|$)");

    // pub fn strip_think_tag(text: &str) -> Cow<'_, str> {
    //     THINK_TAG_RE.replace_all(text, "")
    // }
    public static String stripThinkTag(String text) {
        return THINK_TAG_RE.matcher(text).replaceAll("");
    }

    private Mod() { }
}
