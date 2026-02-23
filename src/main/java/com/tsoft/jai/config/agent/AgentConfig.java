package com.tsoft.jai.config.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.serde.serdeyaml.SerdeYaml;
import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Map;

import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.std.Fs.readToString;
import static com.tsoft.jai.utils.base.StringUtils.format;

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
    public static Result<AgentConfig> load(Path path) {
        Result<String> contents = readToString(path)
            .withContext(() -> format("Failed to read agent config file at '{}'", path));
        if (isErr(contents)) {
            return Err(contents);
        }
        Result<AgentConfig> config = SerdeYaml.fromStr(contents.getValue(), AgentConfig.class)
            .withContext(() -> format("Failed to load agent config at '{}'", path));
        if (isErr(config)) {
            return Err(config);
        }
        return Ok(config.getValue());
    }

    public void loadEnvs(String name) {

    }
}
