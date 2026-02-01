package com.tsoft.jai.inquire;

import org.jline.prompt.PrompterFactory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestInquire {

    private static final PipedInputStream terminalInput = new PipedInputStream();
    private static final PrintStream terminalInputStream = new PrintStream(terminalInputWriterStream(terminalInput));
    private static final OutputStream terminalOutput = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public static void init() {
        Inquire.terminal = createJUnitTerminal();
    }

    public static void newSession() {
        newSession(null);
    }

    public static void newSession(String input) {
        // input
        Inquire.prompter = PrompterFactory.create(Inquire.terminal);
        Inquire.lineReaderBuilder = new TestLineReaderFactory(Inquire.terminal, input);

        // output
        Inquire.writer = new TestWriter(output);
        output.reset();
    }

    public static String getOutput() {
        return output.toString();
    }

    private static Terminal createJUnitTerminal() {
        try {
            return TerminalBuilder.builder()
                .streams(terminalInput, terminalOutput)
                .encoding(StandardCharsets.UTF_8)
                .dumb(true)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static OutputStream terminalInputWriterStream(PipedInputStream in) {
        try {
            return new PipedOutputStream(in);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
