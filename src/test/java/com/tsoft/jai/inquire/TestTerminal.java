package com.tsoft.jai.inquire;

import lombok.SneakyThrows;
import org.jline.prompt.PrompterFactory;
import org.jline.prompt.impl.DefaultPrompterConfig;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestTerminal {

    private static final PipedInputStream terminalInput = new PipedInputStream();
    private static final PrintStream terminalInputStream = new PrintStream(terminalInputWriterStream(terminalInput));
    private static final OutputStream terminalOutput = new ByteArrayOutputStream();
    private static final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private static final TestTerminalInput input = new TestTerminalInput();

    public static void init() {
        Inquire.terminal = createJUnitTerminal();

        newSession();
    }

    @SneakyThrows
    public static void newSession() {
        // input
        //TestLineReader lineReader = new TestLineReader(Inquire.terminal, input);
        //Inquire.prompter = PrompterFactory.create(lineReader, Inquire.terminal, DefaultPrompterConfig.defaults());
        Inquire.prompter = new TestPrompter(Inquire.terminal, input);
        Inquire.lineReaderBuilder = new TestLineReaderFactory(Inquire.terminal, input);


        // output
        Inquire.writer = new TestWriter(output);
        output.reset();
    }

    public static void setInput(String text) {
        input.add(TestInput.Text(text));
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
