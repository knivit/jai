package com.tsoft.jai.inquire;

import lombok.Setter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

import java.io.IOException;

public class TestLineReader extends LineReaderImpl {

    @Setter
    private String input;

    public TestLineReader(Terminal terminal) throws IOException {
        super(terminal);
    }

    @Override
    public String readLine() throws UserInterruptException, EndOfFileException {
        return input;
    }
}
