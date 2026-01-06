package com.tsoft.jai.config;

import com.tsoft.jai.client.model.Model;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Role {

    private String name;
    // #[serde(default)]
    private String prompt;
    //#[serde(rename(serialize = "model", deserialize = "model"), skip_serializing_if = "Option::is_none")]
    private String modelId;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double temperature;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double topP;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String useTools;

    //#[serde(skip)]
    private Model model;
}
