package com.tsoft.jai.inquire;

import org.jline.prompt.Prompter;
import org.jline.prompt.PrompterFactory;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class Inquire {

    public static final String JAI_DUMB_TERMINAL_MODE = "JAI_DUMB_TERMINAL_MODE";

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

    public static void println(String message, Object ... args) {
        if (message == null) {
            terminal().writer().println();
            terminal().flush();
            return;
        }

        if (args == null || args.length == 0) {
            terminal().writer().println(message);
            terminal().flush();
            return;
        }

        for (int i = 0; i < args.length; i ++) {
            int n = message.indexOf("{}");
            if (n < 0) {
                break;
            }
            String arg = (args[i] == null) ? "null" : args[i].toString();
            message = message.substring(0, n) + arg + message.substring(n + 2);
        }

        terminal().writer().println(message);
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
                .streams(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream())
                .encoding(StandardCharsets.UTF_8)
                .size(new Size(80, 1024))
                .dumb(true)
                .build();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
