package com.tsoft.jai.inquire;

import org.jline.prompt.Prompter;
import org.jline.prompt.PrompterFactory;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static com.tsoft.jai.utils.StringUtils.format;

public final class Inquire {

    public static final String JAI_DUMB_TERMINAL_MODE = "JAI_DUMB_TERMINAL_MODE";
    public static final ByteArrayInputStream dumbInput = new ByteArrayInputStream(new byte[0]);
    public static final ByteArrayOutputStream dumbOutput = new ByteArrayOutputStream();

    private static final Inquire INSTANCE = new Inquire();

    // package-private
    private Terminal terminal;
    private final Prompter prompter;

    public static Terminal terminal() {
        return INSTANCE.terminal;
    }

    public static Prompter prompter() {
        return INSTANCE.prompter;
    }

    public static void print(String msg, Object ... args) {
        msg = format(msg, args);
        terminal().writer().print((msg == null) ? "" : msg);
        terminal().flush();
    }

    public static void println(String msg, Object ... args) {
        msg = format(msg, args);
        terminal().writer().println((msg == null) ? "" : msg);
        terminal().flush();
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
                .streams(dumbInput, dumbOutput)
                .encoding(StandardCharsets.UTF_8)
                .size(new Size(80, 1024))
                .dumb(true)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
