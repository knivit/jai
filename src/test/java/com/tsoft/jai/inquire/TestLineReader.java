package com.tsoft.jai.inquire;

import lombok.Setter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

import java.io.IOException;

public class TestLineReader extends LineReaderImpl {

    static final String USER_INTERRUPTION = "Ctrl+C";
    static final String END_OF_FILE = "Ctrl+D";

    @Setter
    private String input;

    public TestLineReader(Terminal terminal) throws IOException {
        super(terminal);
    }

    @Override
    public String readLine(String prompt) throws UserInterruptException, EndOfFileException {
        return readLine();
    }

    @Override
    public String readLine() throws UserInterruptException, EndOfFileException {
        if (USER_INTERRUPTION.equals(input)) {
            throw new UserInterruptException("");
        }

        if (END_OF_FILE.equals(input)) {
            throw new EndOfFileException("");
        }

        return input;
    }
}
