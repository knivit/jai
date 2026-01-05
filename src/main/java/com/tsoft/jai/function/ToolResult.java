package com.tsoft.jai.function;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ToolResult {

    private ToolCall call;
    private Object output;
}
