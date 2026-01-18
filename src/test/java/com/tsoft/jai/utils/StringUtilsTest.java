package com.tsoft.jai.utils;

import org.junit.jupiter.api.Test;

import static com.tsoft.jai.utils.StringUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void split_once() {
        assertEquals(new Tuple<>(null, null), splitOnce(null, ':'));
        assertEquals(new Tuple<>("", null), splitOnce("", ':'));
        assertEquals(new Tuple<>("", ""), splitOnce(":", ':'));
        assertEquals(new Tuple<>("a", null), splitOnce("a", ':'));
        assertEquals(new Tuple<>("a", ""), splitOnce("a:", ':'));
        assertEquals(new Tuple<>("a", "b"), splitOnce("a:b", ':'));
        assertEquals(new Tuple<>("", "b"), splitOnce(":b", ':'));

        assertEquals(new Tuple<>("a", "b c"), splitOnce("a:b c", ':', ' '));
        assertEquals(new Tuple<>("a b", "c"), splitOnce("a b:c", ':', ' '));
    }

    @Test
    void format_str() {
        assertEquals(null, format(null));
        assertEquals(null, format(null, null));
        assertEquals("{}", format("{}"));
        assertEquals("{}", format("{}", null));
        assertEquals(null, format(null, "a"));
        assertEquals("", format("", "a"));
        assertEquals("a", format("{}", "a"));
        assertEquals("a{}", format("{}{}", "a"));
        assertEquals("ab", format("{}{}", "a", "b"));
        assertEquals("a", format("{}{}", "a", null));
    }

    @Test
    void pad_right() {
        assertEquals("          ", padRight(null, 10));
        assertEquals("12345     ", padRight("12345", 10));
        assertEquals("1234567890", padRight("1234567890", 10));
        assertEquals("1234567890", padRight("12345678901", 10));
    }
}