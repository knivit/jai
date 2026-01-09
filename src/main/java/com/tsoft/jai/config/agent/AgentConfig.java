package com.tsoft.jai.config.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.serdejson.SerDe;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AgentConfig {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    @JsonProperty("model")
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

    //  pub fn new(config: &Config) -> Self {
    //     Self {
    //         use_tools: config.use_tools.clone(),
    //         agent_prelude: config.agent_prelude.clone(),
    //         ..Default::default()
    //     }
    // }
    public static AgentConfig create(Config config) {
        return new AgentConfig()
            .setUseTools(config.getUseTools())
            .setAgentPrelude(config.getAgentPrelude());
    }

    // pub fn load(path: &Path) -> Result<Self> {
    //    let contents = read_to_string(path)
    //        .with_context(|| format!("Failed to read agent config file at '{}'", path.display()))?;
    //    let config: Self = serde_yaml::from_str(&contents)
    //        .with_context(|| format!("Failed to load agent config at '{}'", path.display()))?;
    //    Ok(config)
    // }
    public static AgentConfig load(File configFile) {
        return SerDe.readFromYamlFile(configFile, AgentConfig.class);
    }

    public void loadEnvs(String name) {

    }
}
