package com.tsoft.jai.inquire;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

import java.io.IOException;

public class TestLineReader extends LineReaderImpl {

    private final TestTerminalInput terminalInput;

    public TestLineReader(Terminal terminal, TestTerminalInput input) throws IOException {
        super(terminal);
        terminalInput = input;
    }

    @Override
    public String readLine(String prompt) throws UserInterruptException, EndOfFileException {
        return readLine();
    }

    @Override
    public String readLine(String prompt, String rightPrompt, Character mask, String buffer)  throws UserInterruptException, EndOfFileException {
        return readLine();
    }

    @Override
    public String readLine() throws UserInterruptException, EndOfFileException {
        TestInput input = terminalInput.get();
        if (TestInput.is(input, TestInput.TextInputEnum.CtrlD)) {
            throw new EndOfFileException("");
        }

        if (TestInput.is(input, TestInput.TextInputEnum.CtrlC)) {
            throw new UserInterruptException("");
        }

        return input.getValue();
    }
}
