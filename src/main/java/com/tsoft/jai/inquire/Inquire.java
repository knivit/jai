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

    public static final String JUNIT_TERMINAL_MODE = "JUNIT_TERMINAL_MODE";

    private static final PipedInputStream terminalInput = new PipedInputStream();
    public static final PrintStream terminalInputStream = new PrintStream(terminalInputWriterStream(terminalInput));

    private static final OutputStream terminalOutput = new ByteArrayOutputStream();
    public static final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private static final PrintWriter junitWriter = new PrintWriter(output);

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
        if (msg == null) {
            return;
        }

        PrintWriter writer = isJUnitTerminalMode() ? junitWriter : terminal().writer();
        writer.print(msg);
        writer.flush();

        // Additionally to a console in JUnit mode
        if (isJUnitTerminalMode()) {
            System.out.print(msg);
            System.out.flush();
        }
    }

    public static void println(String msg, Object ... args) {
        msg = format(msg, args);

        PrintWriter writer = isJUnitTerminalMode() ? junitWriter : terminal().writer();
        if (msg != null) {
            writer.println(msg);
        } else {
            writer.println();
        }
        writer.flush();

        // Additionally to a console in JUnit mode
        if (isJUnitTerminalMode()) {
            if (msg != null) {
                System.out.println(msg);
            } else {
                System.out.println();
            }
        }
    }

    public static void println() {
        println(null);
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
        if (isJUnitTerminalMode()) {
            initJUnitTerminal();
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

    private void initJUnitTerminal() {
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

    private static boolean isJUnitTerminalMode() {
        return "ON".equals(System.getProperty(JUNIT_TERMINAL_MODE));
    }
}
