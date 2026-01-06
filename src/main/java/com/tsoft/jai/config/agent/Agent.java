package com.tsoft.jai.config.agent;

import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.rag.Rag;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class Agent {

    private String name;
    private AgentConfig config;
    private AgentDefinition definition;
    private Map<String, String> sharedVariables;
    private Map<String, String> sessionVariables;
    private String sharedDynamicInstructions;
    private String sessionDynamicInstructions;
    private Functions functions;
    private Rag rag;
    private Model model;
}
