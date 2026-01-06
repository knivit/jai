package com.tsoft.jai.config.agent;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AgentDefinition {

    private String name;
    //#[serde(default)]
    private String description;
    //#[serde(default)]
    private String version;
    //#[serde(default)]
    private String instructions;
    //#[serde(default)]
    private boolean dynamicInstructions;
    //#[serde(default)]
    private AgentVariable variables;
    //#[serde(default)]
    private List<String> conversationStarters;
    //#[serde(default)]
    private List<String> documents;
}
