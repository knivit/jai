package com.tsoft.jai.inquire;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jline.prompt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.inquire.Inquire.prompter;

@Slf4j
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class Select {

    private final String message;
    private final List<String> values;

    public String prompt() {
        ListBuilder listBuilder = prompter().newBuilder()
            .createListPrompt()
            .name("select")
            .message(message);

        for (String value : values) {
            listBuilder.add(value, value);
        }

        PromptBuilder builder = listBuilder.addPrompt();

        try {
            Map<String, ? extends PromptResult<? extends Prompt>> results = prompter().prompt(new ArrayList<>(), builder.build());
            return results.get("select").getResult();
        } catch (Exception ex) {
            log.warn("Prompt error", ex);
            return null;
        }
    }
}
