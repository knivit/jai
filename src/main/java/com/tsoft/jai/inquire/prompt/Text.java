package com.tsoft.jai.inquire.prompt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jline.prompt.Prompt;
import org.jline.prompt.PromptBuilder;
import org.jline.prompt.PromptResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import static com.tsoft.jai.inquire.Inquire.prompter;

@Slf4j
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class Text {

    private final String message;
    private String defaultValue;
    private Function<String, Boolean> validator;

    public String prompt() {
        PromptBuilder promptBuilder = prompter().newBuilder()
            .createInputPrompt()
            .name("text")
            .message(message)
            .defaultValue(defaultValue)
            .validator(validator)
            .addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter().prompt(new ArrayList<>(), promptBuilder.build());
            return results.get("input").getResult();
        } catch (Exception ex) {
            log.warn("Prompt error", ex);
            return null;
        }
    }
}
