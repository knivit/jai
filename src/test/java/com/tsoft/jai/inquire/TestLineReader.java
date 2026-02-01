package com.tsoft.jai.inquire;

import lombok.Setter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

import java.io.IOException;
import java.util.function.Supplier;

public class TestLineReader extends LineReaderImpl {

    @Setter
    private Supplier<String> inputs;

    public TestLineReader(Terminal terminal) throws IOException {
        super(terminal);
    }

    @Override
    public String readLine() throws UserInterruptException, EndOfFileException {
        return inputs.get();
    }
}
