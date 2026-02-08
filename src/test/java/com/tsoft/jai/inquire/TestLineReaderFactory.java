package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.List;

import static com.tsoft.jai.inquire.TestLineReader.END_OF_FILE;

@RequiredArgsConstructor
public class TestLineReaderFactory implements Inquire.LineReaderFactory {

    private final Terminal terminal;
    private final List<String> inputs;

    private int index;

    @SneakyThrows
    @Override
    public LineReader build() {
        TestLineReader lineReader = new TestLineReader(terminal);

        String input;
        if ((inputs == null) || (index >= inputs.size())) {
            input = END_OF_FILE;
        } else {
            input = inputs.get(index);
            index ++;
        }

        lineReader.setInput(input);
        return lineReader;
    }
}
