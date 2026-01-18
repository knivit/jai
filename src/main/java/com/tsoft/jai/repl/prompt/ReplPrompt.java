package com.tsoft.jai.repl.prompt;

import com.tsoft.jai.config.Config;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReplPrompt {

    private Config config;
}
