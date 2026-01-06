package com.tsoft.jai.config;

import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.model.Model;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Session {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    private String modelId;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double temperature;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double topP;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String useTools;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private boolean saveSession;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Integer compressThreshold;

    //#[serde(skip_serializing_if = "Option::is_none")]
    private String roleName;
    //#[serde(default, skip_serializing_if = "IndexMap::is_empty")]
    private Map<String, String> agentVariables;
    //#[serde(default, skip_serializing_if = "String::is_empty")]
    private String agentInstructions;

    //#[serde(default, skip_serializing_if = "Vec::is_empty")]
    private List<Message> compressedMessages;
    private List<Message> messages;
    //#[serde(default, skip_serializing_if = "HashMap::is_empty")]
    private Map<String, String> dataUrls;

    //#[serde(skip)]
    private Model model;
    //#[serde(skip)]
    private String rolePrompt;
    //#[serde(skip)]
    private String name;
    //#[serde(skip)]
    private String path;
    //#[serde(skip)]
    private boolean dirty;
    //#[serde(skip)]
    private boolean saveSessionThisTime;
    //#[serde(skip)]
    private boolean compressing;
    //#[serde(skip)]
    private AutoName autoname;
    //#[serde(skip)]
    private Integer tokens;
}
