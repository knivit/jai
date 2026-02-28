package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

@RequiredArgsConstructor
public class TestLineReaderFactory implements Inquire.LineReaderFactory {

    private final Terminal terminal;
    private final TestTerminalInput input;;

    @SneakyThrows
    @Override
    public LineReader build() {
        return new TestLineReader(terminal, input);
    }
}
