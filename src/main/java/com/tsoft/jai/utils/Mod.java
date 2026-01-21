package com.tsoft.jai.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tsoft.jai.unicodesegmentation.Word.isAscii;
import static com.tsoft.jai.unicodesegmentation.Word.unicodeWords;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class Mod {

    private static final Pattern CODE_BLOCK_RE = Pattern.compile("(?ms)```\\w*(.*)```");
    private static final Pattern THINK_TAG_RE = Pattern.compile("(?s)^\\s*<think>.*?</think>(\\s*|$)");

    // pub fn strip_think_tag(text: &str) -> Cow<'_, str> {
    //     THINK_TAG_RE.replace_all(text, "")
    // }
    public static String stripThinkTag(String text) {
        return THINK_TAG_RE.matcher(text).replaceAll("");
    }

    // pub fn extract_code_block(text: &str) -> &str {
    //    CODE_BLOCK_RE
    //        .captures(text)
    //        .ok()
    //        .and_then(|v| v?.get(1).map(|v| v.as_str().trim()))
    //        .unwrap_or(text)
    // }
    public static String extractCodeBlock(String text) {
        Matcher matcher = CODE_BLOCK_RE.matcher(text);
        if (matcher.find()) {
            return text.substring(matcher.start(), matcher.end()).trim();
        }
        return null;
    }

    // pub fn is_url(path: &str) -> bool {
    //    path.starts_with("http://") || path.starts_with("https://")
    // }
    public static boolean isUrl(String path) {
        return !isBlank(path) && (path.startsWith("http://") || path.startsWith("https://"));
    }

    // pub fn normalize_env_name(value: &str) -> String {
    //    value.replace('-', "_").to_ascii_uppercase()
    // }
    public static String normalizeEnvName(String value) {
        return (value == null) ? null : value.replace('-', '_').toUpperCase();
    }

    // pub fn estimate_token_length(text: &str) -> usize {
    //    let words: Vec<&str> = text.unicode_words().collect();
    //    let mut output: f32 = 0.0;
    //    for word in words {
    //        if word.is_ascii() {
    //            output += 1.3;
    //        } else {
    //            let count = word.chars().count();
    //            if count == 1 {
    //                output += 1.0
    //            } else {
    //                output += (count as f32) * 0.5;
    //            }
    //        }
    //    }
    //    output.ceil() as usize
    // }
    public static int estimateTokenLength(String text) {
        List<String> words = unicodeWords(text);
        float output = 0.0f;
        for (String word : words) {
            if (isAscii(word)) {
                output += 1.3f;
            } else {
                int count = word.length();
                if (count == 1) {
                    output += 1.0f;
                } else {
                    output += (float) count * 0.5f;
                }
            }
        }
        return (int)Math.ceil(output);
    }

    private Mod() { }
}
