package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import org.jline.prompt.Prompter;
import org.jline.prompt.PrompterFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.tsoft.jai.utils.base.StringUtils.format;

public final class Inquire {

    public static boolean IS_STDOUT_TERMINAL;
    public static final boolean NO_COLOR = false;

    public static Terminal terminal;
    public static Prompter prompter;
    public static LineReaderFactory lineReaderBuilder;
    public static PrintWriter writer;

    static {
        terminal = createTerminal();
        prompter = PrompterFactory.create(terminal);
        lineReaderBuilder = LineReaderFactoryImpl.create(terminal);
        writer = terminal.writer();
    }

    public interface LineReaderFactory {

        LineReader build();
    }

    @RequiredArgsConstructor
    public static class LineReaderFactoryImpl implements LineReaderFactory {

        private final LineReaderBuilder builder;

        public static LineReaderFactory create(Terminal terminal) {
            return new LineReaderFactoryImpl(LineReaderBuilder.builder()
                .terminal(terminal)
                .variable(LineReader.EDITING_MODE, "emacs"));
        }

        public LineReader build() {
            return builder.build();
        }
    }

    private static Terminal createTerminal() {
        try {
            Terminal terminal = TerminalBuilder.builder()
                .encoding(StandardCharsets.UTF_8)
                .system(true)
                .build();

            IS_STDOUT_TERMINAL = true;
            return terminal;
        } catch (Exception ex) {
            IS_STDOUT_TERMINAL = false;
            return null;
        }
    }

    public static void print(String msg, Object ... args) {
        msg = format(msg, args);
        if (msg == null) {
            return;
        }

        writer.print(msg);
        writer.flush();
    }

    public static void println(String msg, Object ... args) {
        msg = format(msg, args);

        if (msg != null) {
            writer.println(msg);
        } else {
            writer.println();
        }
        writer.flush();
    }

    public static void println() {
        println(null);
    }

    public static void enableRawMode() {

    }
}
