package com.tsoft.jai.user.terminal;

import com.tsoft.jai.std.Result;
import com.tsoft.jai.user.UserInput;
import org.jline.prompt.*;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.utils.StringUtils.format;

public final class TerminalUtils {

    private static Terminal terminal;
    private static LineReader lineReader;
    private static Prompter prompter;
    private static PrintWriter writer;

    static {
        init();
    }

    public static Result<UserInput> readLine(String prompt) {
        try {
            String message = lineReader.readLine(prompt);
            return Ok(UserInput.Message(message));
        } catch (UserInterruptException ex) {
            return Err("Operation was interrupted by the user");
        } catch (EndOfFileException ex) {
            return Err("Operation was canceled by the user");
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<UserInput> select(String prompt, List<String> values) {
        ListBuilder listBuilder = prompter.newBuilder()
            .createListPrompt()
            .name("select")
            .message(prompt);

        for (String value : values) {
            listBuilder.newItem(value);
        }

        PromptBuilder builder = listBuilder.addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter.prompt(new ArrayList<>(), builder.build());
            String message = results.get("select").getResult();
            return Ok(UserInput.Message(message));
        } catch (UserInterruptException ex) {
            return Err("Operation was interrupted by the user");
        } catch (EndOfFileException ex) {
            return Err("Operation was canceled by the user");
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<UserInput> multiSelect(String prompt, List<String> values) {
        CheckboxBuilder checkboxBuilder = prompter.newBuilder()
            .createCheckboxPrompt()
            .name("multiselect")
            .message(prompt);

        for (String value : values) {
            checkboxBuilder.newItem(value);
        }

        PromptBuilder builder = checkboxBuilder.addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter.prompt(new ArrayList<>(), builder.build());
            CheckboxResult result = (CheckboxResult) results.get("multiselect");
            return Ok(UserInput.List(result.getSelectedIds()));
        } catch (UserInterruptException ex) {
            return Err("Operation was interrupted by the user");
        } catch (EndOfFileException ex) {
            return Err("Operation was canceled by the user");
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<?> print(String msg, Object ... args) {
        msg = format(msg, args);
        if (msg != null) {
            writer.print(msg);
            writer.flush();
        }

        return Ok();
    }

    public static Result<?> println(String msg, Object ... args) {
        msg = format(msg, args);

        if (msg != null) {
            writer.println(msg);
        } else {
            writer.println();
        }
        writer.flush();

        return Ok();
    }

    public static Result<?> println() {
        println(null);

        return Ok();
    }

    private static void init() {
        try {
            terminal = TerminalBuilder.builder().build();
            lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            prompter = PrompterFactory.create(terminal);
            writer = terminal.writer();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private TerminalUtils() { }
}
