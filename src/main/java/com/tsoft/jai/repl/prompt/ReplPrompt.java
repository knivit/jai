package com.tsoft.jai.repl.prompt;

import com.tsoft.jai.config.Config;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class ReplPrompt {

    private final Config config;
}
