package com.tsoft.jai.function;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionDeclaration {

    private String name;
    private String description;
    private JsonSchema parameters;
    private boolean agent;
}
