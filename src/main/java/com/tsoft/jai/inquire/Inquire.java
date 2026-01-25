package com.tsoft.jai.inquire;

import org.jline.prompt.Prompter;
import org.jline.prompt.PrompterFactory;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.tsoft.jai.utils.base.StringUtils.format;

public final class Inquire {

    public static final String JAI_DUMB_TERMINAL_MODE = "JAI_DUMB_TERMINAL_MODE";

    public static final PipedInputStream terminalInput = new PipedInputStream();
    public static final PrintStream terminalInputStream = new PrintStream(terminalInputWriterStream(terminalInput));

    public static final OutputStream terminalOutput = new ByteArrayOutputStream();
    public static final ByteArrayOutputStream output = new ByteArrayOutputStream();
    public static final PrintStream outputStream = new PrintStream(output);

    private static final Inquire INSTANCE = new Inquire();

    // package-private
    private Terminal terminal;
    private final Prompter prompter;

    public static final boolean IS_STDOUT_TERMINAL = INSTANCE.terminal != null;

    public static final boolean NO_COLOR = false;

    public static Terminal terminal() {
        return INSTANCE.terminal;
    }

    public static Prompter prompter() {
        return INSTANCE.prompter;
    }

    public static void print(String msg, Object ... args) {
        msg = format(msg, args);
        outputStream.print((msg == null) ? "" : msg);
        outputStream.flush();
    }

    public static void println() {
        outputStream.println();
        outputStream.flush();
    }

    public static void println(String msg, Object ... args) {
        msg = format(msg, args);
        outputStream.println((msg == null) ? "" : msg);
        outputStream.flush();
    }

    public static void enableRawMode() {

    }

    private static OutputStream terminalInputWriterStream(PipedInputStream in) {
        try {
            return new PipedOutputStream(in);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Inquire() {
        if ("ON".equals(System.getProperty(JAI_DUMB_TERMINAL_MODE))) {
            initDumbTerminal();
        } else {
            initTerminal();
        }

        prompter = PrompterFactory.create(terminal);
    }

    private void initTerminal() {
        try {
            terminal = TerminalBuilder.builder()
                .encoding(StandardCharsets.UTF_8)
                .system(true)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void initDumbTerminal() {
        try {
            terminal = TerminalBuilder.builder()
                .streams(terminalInput, terminalOutput)
                .encoding(StandardCharsets.UTF_8)
                .size(new Size(80, 1024))
                .dumb(true)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
