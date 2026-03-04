package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;
import org.jline.prompt.Prompt;
import org.jline.prompt.PromptBuilder;
import org.jline.prompt.PromptResult;
import org.jline.prompt.Prompter;
import org.jline.prompt.impl.DefaultInputResult;
import org.jline.prompt.impl.DefaultPrompter;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public class TestPrompter implements Prompter {

    private final Terminal terminal;
    private final TestTerminalInput input;

    @Override
    public PromptBuilder newBuilder() {
        return new DefaultPrompter(terminal).newBuilder();
    }

    @Override
    public Map<String, ? extends PromptResult<? extends Prompt>> prompt(List<AttributedString> header, List<? extends Prompt> prompts) throws IOException, UserInterruptException {
        String name = prompts.get(0).getName();
        String value = input.get();
        DefaultInputResult result = new DefaultInputResult(value, null, null);
        return Map.of(name, result);
    }

    @Override
    public Map<String, ? extends PromptResult<? extends Prompt>> prompt(List<AttributedString> header, Function<Map<String, ? extends PromptResult<? extends Prompt>>, List<? extends Prompt>> promptsProvider) throws IOException {
        return Map.of();
    }

    @Override
    public Terminal getTerminal() {
        return null;
    }

    @Override
    public LineReader getLineReader() {
        return null;
    }
}
