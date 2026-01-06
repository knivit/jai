package com.tsoft.jai.config.agent;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AgentConfig {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    private String modelId;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double temperature;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double topP;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String useTools;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String agentPrelude;
    //#[serde(default, skip_serializing_if = "Option::is_none")]
    private String instructions;
    //#[serde(default, skip_serializing_if = "IndexMap::is_empty")]
    private Map<String, String> variables;
}
