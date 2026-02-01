package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class TestLineReaderFactory implements Inquire.LineReaderFactory {

    private final Terminal terminal;
    private final Supplier<String> inputs;

    @SneakyThrows
    @Override
    public LineReader build() {
        TestLineReader lineReader = new TestLineReader(terminal);
        lineReader.setInputs(inputs);
        return lineReader;
    }
}
