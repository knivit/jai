package com.tsoft.jai.function;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ToolCall {

    private String name;
    private Object arguments;
    private String id;
}
