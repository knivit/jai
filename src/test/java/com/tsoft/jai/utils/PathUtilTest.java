package com.tsoft.jai.utils;

import org.junit.jupiter.api.Test;

import static com.tsoft.jai.utils.PathUtil.getPatchExtension;
import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void get_patch_extension() {
        assertEquals(null, getPatchExtension(null));
        assertEquals(null, getPatchExtension(""));
        assertEquals(null, getPatchExtension("test"));
        assertEquals("", getPatchExtension("."));
        assertEquals("", getPatchExtension("a."));
        assertEquals("a", getPatchExtension(".a"));
    }
}