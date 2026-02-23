package com.tsoft.jai.config.agent;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.config.Role;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.inquire.prompt.Confirm;
import com.tsoft.jai.inquire.prompt.Input;
import com.tsoft.jai.inquire.Inquire;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.serde.Value;
import com.tsoft.jai.serde.serdeyaml.SerdeYaml;
import com.tsoft.jai.utils.AbortSignal;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.function.Functions.runLlmFunction;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.serde.Value.asMap;
import static com.tsoft.jai.serde.serdejson.SerdeJson.json;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.Mod.isUrl;
import static com.tsoft.jai.utils.Mod.normalizeEnvName;
import static com.tsoft.jai.utils.base.StringUtils.format;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;
import static com.tsoft.jai.utils.Variables.interpolateVariables;

@Slf4j
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

    private static final String DEFAULT_AGENT_NAME = "rag";

    //  pub async fn init(
    //     config: &GlobalConfig,
    //     name: &str,
    //     abort_signal: AbortSignal,
    // ) -> Result<Self> {
    //     let functions_dir = Config::agent_functions_dir(name);
    //     let definition_file_path = functions_dir.join("index.yaml");
    //     if !definition_file_path.exists() {
    //         bail!("Unknown agent `{name}`");
    //     }
    //     let functions_file_path = functions_dir.join("functions.json");
    //     let rag_path = Config::agent_rag_file(name, DEFAULT_AGENT_NAME);
    //     let config_path = Config::agent_config_file(name);
    //     let mut agent_config = if config_path.exists() {
    //         AgentConfig::load(&config_path)?
    //     } else {
    //         AgentConfig::new(&config.read())
    //     };
    //     let mut definition = AgentDefinition::load(&definition_file_path)?;
    //     let functions = if functions_file_path.exists() {
    //         Functions::init(&functions_file_path)?
    //     } else {
    //         Functions::default()
    //     };
    //     definition.replace_tools_placeholder(&functions);
    //
    //     agent_config.load_envs(&definition.name);
    //
    //     let model = {
    //         let config = config.read();
    //         match agent_config.model_id.as_ref() {
    //             Some(model_id) => Model::retrieve_model(&config, model_id, ModelType::Chat)?,
    //             None => {
    //                 if agent_config.temperature.is_none() {
    //                     agent_config.temperature = config.temperature;
    //                 }
    //                 if agent_config.top_p.is_none() {
    //                     agent_config.top_p = config.top_p;
    //                 }
    //                 config.current_model().clone()
    //             }
    //         }
    //     };
    //
    //     let rag = if rag_path.exists() {
    //         Some(Arc::new(Rag::load(config, DEFAULT_AGENT_NAME, &rag_path)?))
    //     } else if !definition.documents.is_empty() && !config.read().info_flag {
    //         let mut ans = false;
    //         if *IS_STDOUT_TERMINAL {
    //             ans = Confirm::new("The agent has the documents, init RAG?")
    //                 .with_default(true)
    //                 .prompt()?;
    //         }
    //         if ans {
    //             let mut document_paths = vec![];
    //             for path in &definition.documents {
    //                 if is_url(path) {
    //                     document_paths.push(path.to_string());
    //                 } else {
    //                     let new_path = safe_join_path(&functions_dir, path)
    //                         .ok_or_else(|| anyhow!("Invalid document path: '{path}'"))?;
    //                     document_paths.push(new_path.display().to_string())
    //                 }
    //             }
    //             let rag =
    //                 Rag::init(config, "rag", &rag_path, &document_paths, abort_signal).await?;
    //             Some(Arc::new(rag))
    //         } else {
    //             None
    //         }
    //     } else {
    //         None
    //     };
    //
    //     Ok(Self {
    //         name: name.to_string(),
    //         config: agent_config,
    //         definition,
    //         shared_variables: Default::default(),
    //         session_variables: None,
    //         shared_dynamic_instructions: None,
    //         session_dynamic_instructions: None,
    //         functions,
    //         rag,
    //         model,
    //     })
    // }
    public static Result<Agent> init(Config config, String name, AbortSignal abortSignal) {
        Path functionsDir = config.agentFunctionsDir(name);
        Path definitionFile = functionsDir.resolve("index.yaml");
        if (!Files.exists(definitionFile)) {
            return bail("Unknown agent '{}', name");
        }
        Path functionsFile = functionsDir.resolve("functions.json");
        Path ragFile = config.agentRagFile(name, DEFAULT_AGENT_NAME);
        Path configFile = config.agentConfigFile(name);
        AgentConfig agentConfig;
        if (Files.exists(configFile)) {
            Result<AgentConfig> res = AgentConfig.load(configFile);
            if (isErr(res)) {
                return Err(res);
            }
            agentConfig = res.getValue();
        } else {
            agentConfig = AgentConfig.create(config);
        }
        Result<AgentDefinition> ret = AgentDefinition.load(definitionFile);
        if (isErr(ret)) {
            return Err(ret);
        }
        AgentDefinition definition = ret.getValue();
        Functions functions;
        if (Files.exists(functionsFile)) {
            Result<Functions> res = Functions.init(functionsFile);
            if (isErr(res)) {
                return Err(res);
            }
            functions = res.getValue();
        } else {
            functions = new Functions();
        }
        definition.replaceToolsPlaceholder(functions);

        agentConfig.loadEnvs(definition.getName());

        Model model;
        if (!isBlank(agentConfig.getModelId())) {
            Result<Model> res = Model.retrieveModel(config, agentConfig.getModelId(), ModelType.Chat);
            if (isErr(res)) {
                return Err(res);
            }
            model = res.getValue();
        } else {
            if (agentConfig.getTemperature() == null) {
                agentConfig.setTemperature(config.getTemperature());
            }
            if (agentConfig.getTopP() == null) {
                agentConfig.setTopP(config.getTopP());
            }
            model = config.currentModel();
        }

        Rag rag = null;
        if (Files.exists(ragFile)) {
            Result<Rag> res = Rag.load(config, DEFAULT_AGENT_NAME, ragFile);
            if (isErr(res)) {
                return Err(res);
            }
            rag = res.getValue();
        } else {
            if (!isEmpty(definition.getDocuments()) && !config.isInfoFlag()) {
                boolean ans = false;
                if (Inquire.prompter != null) {
                    Result<Boolean> res = new Confirm("The agent has the documents, init RAG?")
                        .setDefaultValue(true)
                        .prompt();
                    if (isErr(res)) {
                        return Err(res);
                    }
                    ans = res.getValue();
                }
                if (ans) {
                    List<String> documentPaths = new ArrayList<>();
                    for (String path : definition.getDocuments()) {
                        if (isUrl(path)) {
                            documentPaths.add(path);
                        } else {
                            String newPath = functionsDir.resolve(path).toFile().getAbsolutePath();
                            documentPaths.add(newPath);
                        }
                    }
                    Result<Rag> res = Rag.init(config, "rag", ragFile, documentPaths, abortSignal);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    rag = res.getValue();
                }
            }
        }

        return Ok(new Agent()
            .setName(name)
            .setConfig(agentConfig)
            .setDefinition(definition)
            .setSharedVariables(new HashMap<>())
            .setSessionVariables(Collections.emptyMap())
            .setFunctions(functions)
            .setRag(rag)
            .setModel(model));
    }

    // pub fn list_agents() -> Vec<String> {
    //    let agents_file = Config::functions_dir().join("agents.txt");
    //    let contents = match read_to_string(agents_file) {
    //        Ok(v) => v,
    //        Err(_) => return vec![],
    //    };
    //    contents
    //        .split('\n')
    //        .filter_map(|line| {
    //            let line = line.trim();
    //            if line.is_empty() || line.starts_with('#') {
    //                None
    //            } else {
    //                Some(line.to_string())
    //            }
    //        })
    //        .collect()
    // }
    public static List<String> listAgents(Config config) {
        File agentsFile = config.functionsDir().resolve("agents.txt").toFile();

        try {
            return Files.readAllLines(agentsFile.toPath(), StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(e -> !e.isBlank() && !e.startsWith("#"))
                .toList();
        } catch (Exception ex) {
            log.warn("Error reading file {}", agentsFile.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    // pub fn agent_prelude(&self) -> Option<&str> {
    //    self.config.agent_prelude.as_deref()
    // }
    public String agentPrelude() {
        return (config == null) ? null : config.getAgentPrelude();
    }

    // pub fn defined_variables(&self) -> &[AgentVariable] {
    //    &self.definition.variables
    // }
    public List<AgentVariable> definedVariables() {
        return (definition == null) ? null : definition.getVariables();
    }

    // pub fn config_variables(&self) -> &AgentVariables {
    //    &self.config.variables
    // }
    public Map<String, String> configVariables() {
        return (config == null) ? null : config.getVariables();
    }

    // pub fn is_dynamic_instructions(&self) -> bool {
    //    self.definition.dynamic_instructions
    // }
    private boolean isDynamicInstructions() {
        return (definition != null) && definition.isDynamicInstructions();
    }

    // pub fn update_shared_dynamic_instructions(&mut self, force: bool) -> Result<()> {
    //    if self.is_dynamic_instructions() && (force || self.shared_dynamic_instructions.is_none()) {
    //        self.shared_dynamic_instructions = Some(self.run_instructions_fn()?);
    //    }
    //    Ok(())
    // }
    public Result<?> updateSharedDynamicInstructions(boolean force) {
        if (isDynamicInstructions() && (force || isBlank(sharedDynamicInstructions))) {
            Result<String> res = runInstructionsFn();
            if (isErr(res)) {
                return Err(res);
            }
            sharedDynamicInstructions = res.getValue();
        }
        return Ok();
    }

    // fn to_role(&self) -> Role {
    //    let prompt = self.interpolated_instructions();
    //    let mut role = Role::new("", &prompt);
    //    role.sync(self);
    //    role
    // }
    public Role toRole() {
        String prompt = interpolatedInstructions();
        Role role = new Role().setName("").setPrompt(prompt);
        role.sync(this);
        return role;
    }

    // pub fn banner(&self) -> String {
    //    self.definition.banner()
    // }
    public String banner() {
        return (definition == null) ? null : definition.banner();
    }

    // pub fn conversation_starters(&self) -> &[String] {
    //    &self.definition.conversation_starters
    // }
    public List<String> conversationStarters() {
        return (definition == null) ? null : definition.getConversationStarters();
    }

    // pub fn interpolated_instructions(&self) -> String {
    //    let mut output = self
    //        .session_dynamic_instructions
    //        .clone()
    //        .or_else(|| self.shared_dynamic_instructions.clone())
    //        .or_else(|| self.config.instructions.clone())
    //        .unwrap_or_else(|| self.definition.instructions.clone());
    //    for (k, v) in self.variables() {
    //        output = output.replace(&format!("{{{{{k}}}}}"), v)
    //    }
    //    interpolate_variables(&mut output);
    //    output
    // }
    public String interpolatedInstructions() {
        String output = sessionDynamicInstructions;
        if (isBlank(output)) {
            output = sharedDynamicInstructions;
        }
        if (isBlank(output)) {
            output = config.getInstructions();
        }
        if (isBlank(output)) {
            output = definition.getInstructions();
        }
        for (Map.Entry<String, String> entry : variables().entrySet()) {
            output = output.replace(format("{{}}", entry.getKey()), entry.getValue());
        }
        interpolateVariables(output);
        return output;
    }

    // fn run_instructions_fn(&self) -> Result<String> {
    //    let value = run_llm_function(
    //        self.name().to_string(),
    //        vec!["_instructions".into(), "{}".into()],
    //        self.variable_envs(),
    //    )?;
    //    match value {
    //        Some(v) => Ok(v),
    //        _ => bail!("No return value from '_instructions' function"),
    //    }
    // }
    private Result<String> runInstructionsFn() {
        String value = runLlmFunction(name, List.of("_instructions", "{}"), variableEnvs());
        if (isBlank(value)) {
            return bail("No return value from '_instructions' function");
        }
        return Ok(value);
    }

    // pub fn variables(&self) -> &AgentVariables {
    //    match &self.session_variables {
    //        Some(variables) => variables,
    //        None => &self.shared_variables,
    //    }
    // }
    public Map<String, String> variables() {
        if (!isEmpty(sessionVariables)) {
            return sessionVariables;
        }
        return sharedVariables;
    }

    // pub fn export(&self) -> Result<String> {
    //    let mut value = json!({});
    //    value["name"] = json!(self.name());
    //    let variables = self.variables();
    //    if !variables.is_empty() {
    //        value["variables"] = serde_json::to_value(variables)?;
    //    }
    //    value["config"] = json!(self.config);
    //    let mut definition = self.definition.clone();
    //    definition.instructions = self.interpolated_instructions();
    //    value["definition"] = json!(definition);
    //    value["functions_dir"] = Config::agent_functions_dir(&self.name)
    //        .display()
    //        .to_string()
    //        .into();
    //    value["data_dir"] = Config::agent_data_dir(&self.name)
    //        .display()
    //        .to_string()
    //        .into();
    //    value["config_file"] = Config::agent_config_file(&self.name)
    //        .display()
    //        .to_string()
    //        .into();
    //    let data = serde_yaml::to_string(&value)?;
    //    Ok(data)
    // }
    public Result<String> export(Config config) {
        Value value = new Value();
        value.put("name", name);
        Map<String, String> vars = variables();
        if (!isEmpty(vars)) {
            value.put("variables", vars);
        }
        value.put("config", asMap(json(this.config).getValue()));
        value.put("definition", asMap(json(definition).getValue()));
        value.put("functions_dir", config.agentFunctionsDir(name));
        value.put("data_dir", config.agentDataDir(name));
        value.put("config_file", config.agentConfigFile(name));

        Result<String> data = SerdeYaml.toString(value);
        if (isErr(data)) {
            return Err(data);
        }
        return Ok(data.getValue());
    }

    // pub fn variable_envs(&self) -> HashMap<String, String> {
    //    self.variables()
    //        .iter()
    //        .map(|(k, v)| {
    //            (
    //                format!("LLM_AGENT_VAR_{}", normalize_env_name(k)),
    //                v.clone(),
    //            )
    //        })
    //        .collect()
    // }
    private Map<String, String> variableEnvs() {
        Map<String, String> vars = variables();
        if (isEmpty(vars)) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String k = format("LLM_AGENT_VAR_{}", normalizeEnvName(entry.getKey()));
            String v = entry.getValue();
            result.put(k, v);
        }
        return result;
    }

    // pub fn init_agent_variables(
    //     agent_variables: &[AgentVariable],
    //     variables: &AgentVariables,
    //     no_interaction: bool,
    // ) -> Result<AgentVariables> {
    //     let mut output = IndexMap::new();
    //     if agent_variables.is_empty() {
    //         return Ok(output);
    //     }
    //     let mut printed = false;
    //     let mut unset_variables = vec![];
    //     for agent_variable in agent_variables {
    //         let key = agent_variable.name.clone();
    //         match variables.get(&key) {
    //             Some(value) => {
    //                 output.insert(key, value.clone());
    //             }
    //             None => {
    //                 if let Some(value) = agent_variable.default.clone() {
    //                     output.insert(key, value);
    //                     continue;
    //                 }
    //                 if no_interaction {
    //                     continue;
    //                 }
    //                 if *IS_STDOUT_TERMINAL {
    //                     if !printed {
    //                         println!("⚙ Init agent variables...");
    //                         printed = true;
    //                     }
    //                     let value = Text::new(&format!(
    //                         "{} ({}):",
    //                         agent_variable.name, agent_variable.description
    //                     ))
    //                     .with_validator(|input: &str| {
    //                         if input.trim().is_empty() {
    //                             Ok(Validation::Invalid("This field is required".into()))
    //                         } else {
    //                             Ok(Validation::Valid)
    //                         }
    //                     })
    //                     .prompt()?;
    //                     output.insert(key, value);
    //                 } else {
    //                     unset_variables.push(agent_variable)
    //                 }
    //             }
    //         }
    //     }
    //     if !unset_variables.is_empty() {
    //         bail!(
    //             "The following agent variables are required:\n{}",
    //             unset_variables
    //                 .iter()
    //                 .map(|v| format!("  - {}: {}", v.name, v.description))
    //                 .collect::<Vec<_>>()
    //                 .join("\n")
    //         )
    //     }
    //     Ok(output)
    // }
    public static Result<Map<String, String>> initAgentVariables(List<AgentVariable> agentVariables, Map<String, String> variables, boolean noInteraction) {
        Map<String, String> output = new HashMap<>();
        if (isEmpty(agentVariables)) {
            return Ok(output);
        }

        boolean printed = false;
        List<AgentVariable> unsetVariables = new ArrayList<>();
        for (AgentVariable agentVariable : agentVariables) {
            String key = agentVariable.getName();
            String value = variables.get(key);
            if (value != null) {
                output.put(key, value);
            } else {
                value = agentVariable.getDefaultValue();
                if (value != null) {
                    output.put(key, value);
                    continue;
                }
                if (noInteraction) {
                    continue;
                }
                if (Inquire.prompter != null) {
                    if (!printed) {
                        println("⚙ Init agent variables...");
                        printed = true;
                    }
                    value = new Input(format("{} ({}):", agentVariable.getName(), agentVariable.getDescription()))
                        .setValidator(e -> !isBlank(e))
                        .prompt();
                    output.put(key, value);
                } else {
                    unsetVariables.add(agentVariable);
                }
            }
        }

        if (!isEmpty(unsetVariables)) {
            return bail("The following agent variables are required:\n{}",
                String.join("\n", unsetVariables.stream()
                    .map(e -> format("  - {}: {}", e.getName(), e.getDescription())).toList()));
        }

        return Ok(output);
    }
}
