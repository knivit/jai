package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

@RequiredArgsConstructor
public class TestLineReaderFactory implements Inquire.LineReaderFactory {

    private final Terminal terminal;
    private final String input;

    @SneakyThrows
    @Override
    public LineReader build() {
        TestLineReader lineReader = new TestLineReader(terminal);
        lineReader.setInput(input);
        return lineReader;
    }
}
