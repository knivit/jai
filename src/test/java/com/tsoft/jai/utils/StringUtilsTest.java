package com.tsoft.jai.utils;

import org.junit.jupiter.api.Test;

import static com.tsoft.jai.utils.StringUtils.format;
import static com.tsoft.jai.utils.StringUtils.splitOnce;
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
}