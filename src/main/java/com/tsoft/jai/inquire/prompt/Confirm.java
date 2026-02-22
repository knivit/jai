package com.tsoft.jai.inquire.prompt;

import com.tsoft.jai.anyhow.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jline.prompt.ConfirmResult;
import org.jline.prompt.Prompt;
import org.jline.prompt.PromptBuilder;
import org.jline.prompt.PromptResult;

import java.util.ArrayList;
import java.util.Map;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.inquire.Inquire.prompter;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class Confirm {

    private final String message;
    private boolean defaultValue;

    public Result<Boolean> prompt() {
        PromptBuilder builder = prompter.newBuilder()
            .createConfirmPrompt()
            .name("confirm")
            .message(message)
            .defaultValue(defaultValue)
            .addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter.prompt(new ArrayList<>(), builder.build());
            ConfirmResult confirm = (ConfirmResult) results.get("confirm");
            return Ok(confirm.isConfirmed());
        } catch (Exception ex) {
            return Err(ex);
        }
    }
}
