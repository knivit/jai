package com.tsoft.jai.inquire.prompt;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.inquire.validator.Validation;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jline.prompt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.inquire.Inquire.prompter;

@Accessors(chain = true)
@RequiredArgsConstructor
public class MultiSelect {

    private final String message;
    private final List<String> values;

    @Setter()
    private Function<List<String>, Result<Validation>> validator;

    public Result<List<String>> prompt() {
        CheckboxBuilder checkboxBuilder = prompter.newBuilder()
            .createCheckboxPrompt()
            .name("multiselect")
            .message(message);

        for (String value : values) {
            checkboxBuilder.newItem(value);
        }

        PromptBuilder builder = checkboxBuilder.addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter.prompt(new ArrayList<>(), builder.build());
            CheckboxResult result = (CheckboxResult) results.get("multiselect");
            return Ok(new ArrayList<>(result.getSelectedIds()));
        } catch (Exception ex) {
            return Err(ex);
        }
    }
}
