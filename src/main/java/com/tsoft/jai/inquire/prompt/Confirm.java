package com.tsoft.jai.inquire.prompt;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jline.prompt.ConfirmResult;
import org.jline.prompt.Prompt;
import org.jline.prompt.PromptBuilder;
import org.jline.prompt.PromptResult;

import java.util.ArrayList;
import java.util.Map;

import static com.tsoft.jai.inquire.Inquire.prompter;

@Slf4j
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class Confirm {

    private final String message;
    private boolean defaultValue;

    public boolean prompt() {
        PromptBuilder builder = prompter.newBuilder()
            .createConfirmPrompt()
            .name("confirm")
            .message(message)
            .defaultValue(defaultValue)
            .addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter.prompt(new ArrayList<>(), builder.build());
            ConfirmResult confirm = (ConfirmResult) results.get("confirm");
            return confirm.isConfirmed();
        } catch (Exception ex) {
            log.warn("Prompt error", ex);
            return false;
        }
    }
}
