package com.tsoft.jai.inquire;

import org.jline.prompt.PrompterFactory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestTerminal {

    private static final PipedInputStream terminalInput = new PipedInputStream();
    private static final PrintStream terminalInputStream = new PrintStream(terminalInputWriterStream(terminalInput));
    private static final OutputStream terminalOutput = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    public static void init() {
        Inquire.terminal = createJUnitTerminal();
    }

    public static void newSession() {
        // input
        Inquire.prompter = PrompterFactory.create(Inquire.terminal);

        // output
        Inquire.writer = new TestWriter(output);
        output.reset();
    }

    public static void prepareInput(String input) {
        prepareInputs(List.of(input));
    }

    public static void prepareInputs(List<String> inputs) {
        Inquire.lineReaderBuilder = new TestLineReaderFactory(Inquire.terminal, inputs);
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
