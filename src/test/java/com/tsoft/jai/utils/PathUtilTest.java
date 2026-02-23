package com.tsoft.jai.utils;

import org.junit.jupiter.api.Test;

import static com.tsoft.jai.core.Option.OptionEnum.None;
import static com.tsoft.jai.utils.PathUtil.getPatchExtension;
import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void get_patch_extension() {
        assertEquals(None, getPatchExtension(null).getType());
        assertEquals(None, getPatchExtension("").getType());
        assertEquals(None, getPatchExtension("test").getType());
        assertEquals("", getPatchExtension(".").getValue());
        assertEquals("", getPatchExtension("a.").getValue());
        assertEquals("a", getPatchExtension(".a").getValue());
    }
}