package com.tsoft.jai.repl.mod;

import com.tsoft.jai.utils.base.Tuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModTest {

    @Test
    void parse_command() {
        Tuple<String, String> tuple;

        tuple = Mod.parseCommand(".agent");
        assertEquals(".agent", tuple.first());
        assertEquals(null, tuple.second());

        tuple = Mod.parseCommand(".agent temp x=1");
        assertEquals(".agent", tuple.first());
        assertEquals("temp x=1", tuple.second());
    }
}