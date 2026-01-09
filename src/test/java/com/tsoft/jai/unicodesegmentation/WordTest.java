package com.tsoft.jai.unicodesegmentation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordTest {

    @Test
    void unicodeWords() {
        assertEquals(List.of("The", "quick", "brown", "fox", "can't", "jump", "32.3", "feet", "right"),
            Word.unicodeWords( "The quick (\"brown\") fox can't jump 32.3 feet, right?"));
    }
}
