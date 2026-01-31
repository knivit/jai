package com.tsoft.jai.utils;

import com.tsoft.jai.anyhow.Error;
import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.reqwest.ClientBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.inquire.Inquire.NO_COLOR;
import static com.tsoft.jai.unicodesegmentation.Word.isAscii;
import static com.tsoft.jai.unicodesegmentation.Word.unicodeWords;
import static com.tsoft.jai.utils.base.StringUtils.format;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

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

    // pub fn pretty_error(err: &anyhow::Error) -> String {
    //    let mut output = vec![];
    //    output.push(format!("Error: {err}"));
    //    let causes: Vec<_> = err.chain().skip(1).collect();
    //    let causes_len = causes.len();
    //    if causes_len > 0 {
    //        output.push("\nCaused by:".to_string());
    //        if causes_len == 1 {
    //            output.push(format!("    {}", indent_text(causes[0], 4).trim()));
    //        } else {
    //            for (i, cause) in causes.into_iter().enumerate() {
    //                output.push(format!("{i:5}: {}", indent_text(cause, 7).trim()));
    //            }
    //        }
    //    }
    //    output.join("\n")
    // }
    public static String prettyError(Error<?> err) {
        return format("Error: {}", err);
    }

    // pub fn error_text(input: &str) -> String {
    //    color_text(input, nu_ansi_term::Color::Red)
    // }
    public static String errorText(String input) {
        return colorText(input, AttributedStyle.RED);
    }

    // pub fn warning_text(input: &str) -> String {
    //    color_text(input, nu_ansi_term::Color::Yellow)
    // }
    public static String warningText(String input) {
        return colorText(input, AttributedStyle.YELLOW);
    }

    // pub fn color_text(input: &str, color: nu_ansi_term::Color) -> String {
    //    if *NO_COLOR {
    //        return input.to_string();
    //    }
    //    nu_ansi_term::Style::new()
    //        .fg(color)
    //        .paint(input)
    //        .to_string()
    // }
    public static String colorText(String input, int color) {
        if (NO_COLOR) {
            return input;
        }
        return new AttributedString(input, AttributedStyle.DEFAULT.foreground(color)).toString();
    }

    // pub fn dimmed_text(input: &str) -> String {
    //    if *NO_COLOR {
    //        return input.to_string();
    //    }
    //    nu_ansi_term::Style::new().dimmed().paint(input).to_string()
    // }
    public static String dimmedText(String input) {
        if (NO_COLOR) {
            return input;
        }
        return new AttributedString(input, AttributedStyle.DEFAULT.faint()).toString();
    }

    // pub fn set_proxy(
    //    mut builder: reqwest::ClientBuilder,
    //    proxy: &str,
    // ) -> Result<reqwest::ClientBuilder> {
    //    builder = builder.no_proxy();
    //    if !proxy.is_empty() && proxy != "-" {
    //        builder = builder
    //            .proxy(reqwest::Proxy::all(proxy).with_context(|| format!("Invalid proxy `{proxy}`"))?);
    //    };
    //    Ok(builder)
    // }
    public static Result<ClientBuilder> setProxy(ClientBuilder builder, String proxy) {
        builder = builder.noProxy();
        if (!isBlank(proxy) && !"-".equals(proxy)) {
            //
        }
        return Ok(builder);
    }

    private Mod() { }
}
