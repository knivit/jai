package com.tsoft.jai.function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FunctionDeclaration {

    private String name;
    private String description;
    private JsonSchema parameters;

    @JsonIgnore
    private boolean agent;
}
