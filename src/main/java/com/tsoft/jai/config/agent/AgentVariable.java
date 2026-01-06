package com.tsoft.jai.config.agent;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentVariable {

    private String name;
    private String description;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String defaultValue;
    //#[serde(skip_deserializing, default)]
    private String value;
}
