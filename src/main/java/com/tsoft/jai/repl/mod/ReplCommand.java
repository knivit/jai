package com.tsoft.jai.repl.mod;

import com.tsoft.jai.config.AssertState;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class ReplCommand {

    private final String name;
    private final String description;
    private final AssertState state;
}
