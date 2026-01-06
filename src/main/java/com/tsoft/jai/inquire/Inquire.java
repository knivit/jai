package com.tsoft.jai.inquire;

import org.jline.prompt.Prompter;
import org.jline.prompt.PrompterFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public final class Inquire {

    private static final Inquire INSTANCE = new Inquire();

    // package-private
    private Terminal terminal;
    private Prompter prompter;
    private LineReader reader;

    public static Terminal terminal() {
        return INSTANCE.terminal;
    }

    public static Prompter prompter() {
        return INSTANCE.prompter;
    }

    public static LineReader reader() {
        return reader();
    }

    private Inquire() {
        init();
    }

    private void init() {
        try {
            terminal = TerminalBuilder.builder()
                .system(true)
                .build();

            prompter = PrompterFactory.create(terminal);

            reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
