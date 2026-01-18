package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.client.message.MessageRole;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.CollectionsUtils;
import com.tsoft.jai.utils.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.config.Config.TEMP_SESSION_NAME;
import static com.tsoft.jai.serdejson.SerDe.toJson;
import static com.tsoft.jai.utils.StringUtils.*;

@Data
@Accessors(chain = true)
public class Session {

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
    @JsonIgnore
    private Model model;
    //#[serde(skip)]
    @JsonIgnore
    private String rolePrompt;
    //#[serde(skip)]
    @JsonIgnore
    private String name;
    //#[serde(skip)]
    @JsonIgnore
    private String path;
    //#[serde(skip)]
    @JsonIgnore
    private boolean dirty;
    //#[serde(skip)]
    @JsonIgnore
    private boolean saveSessionThisTime;
    //#[serde(skip)]
    @JsonIgnore
    private boolean compressing;
    //#[serde(skip)]
    @JsonIgnore
    private AutoName autoname;
    //#[serde(skip)]
    @JsonIgnore
    private Integer tokens;

    private static final Pattern RE_AUTONAME_PREFIX = Pattern.compile("\\d{8}T\\d{6}-");

    // pub fn new(config: &Config, name: &str) -> Self {
    //    let role = config.extract_role();
    //    let mut session = Self {
    //        name: name.to_string(),
    //        save_session: config.save_session,
    //        ..Default::default()
    //    };
    //    session.set_role(role);
    //    session.dirty = false;
    //    session
    // }
    public static Session create(Config config, String name) {
        Role role = config.extractRole();
        Session session = new Session()
            .setName(name)
            .setSaveSession(config.isSaveSession());
        session.setRole(role);
        session.setDirty(false);
        return session;
    }

    // pub fn load(config: &Config, name: &str, path: &Path) -> Result<Self> {
    //    let content = read_to_string(path)
    //        .with_context(|| format!("Failed to load session {} at {}", name, path.display()))?;
    //    let mut session: Self =
    //        serde_yaml::from_str(&content).with_context(|| format!("Invalid session {name}"))?;
    //
    //    session.model = Model::retrieve_model(config, &session.model_id, ModelType::Chat)?;
    //
    //    if let Some(autoname) = name.strip_prefix("_/") {
    //        session.name = TEMP_SESSION_NAME.to_string();
    //        session.path = None;
    //        if let Ok(true) = RE_AUTONAME_PREFIX.is_match(autoname) {
    //            session.autoname = Some(AutoName::new(autoname[16..].to_string()));
    //        }
    //    } else {
    //        session.name = name.to_string();
    //        session.path = Some(path.display().to_string());
    //    }
    //
    //    if let Some(role_name) = &session.role_name {
    //        if let Ok(role) = config.retrieve_role(role_name) {
    //            session.role_prompt = role.prompt().to_string();
    //        }
    //    }
    //
    //    session.update_tokens();
    //
    //    Ok(session)
    // }
    public static Session load(Config config, String name, File path) {
        Session session = SerDe.readFromYamlFile(path, Session.class);

        session.setModel(Model.retrieveModel(config, session.modelId, ModelType.Chat));

        Tuple<String, String> tuple = splitOnce(name, '/');
        String autoname = tuple.second();
        if (!isBlank(autoname)) {
            session.name = TEMP_SESSION_NAME;
            session.path = null;
            if (RE_AUTONAME_PREFIX.matcher(autoname).hasMatch()) {
                session.autoname = new AutoName().setName(autoname.substring(16));
            }
        } else {
            session.name = name;
            session.path = path.getAbsolutePath();
        }

        if (!isBlank(session.roleName)) {
            Role role = config.retrieveRole(session.roleName);
            if (role != null) {
                session.rolePrompt = role.getPrompt();
            }
        }

        session.updateTokens();

        return session;
    }

    // pub fn set_role(&mut self, role: Role) {
    //    self.model_id = role.model().id();
    //    self.temperature = role.temperature();
    //    self.top_p = role.top_p();
    //    self.use_tools = role.use_tools();
    //    self.model = role.model().clone();
    //    self.role_name = convert_option_string(role.name());
    //    self.role_prompt = role.prompt().to_string();
    //    self.dirty = true;
    //    self.update_tokens();
    // }
    public void setRole(Role role) {
        modelId = role.getModel().id();
        temperature = role.getTemperature();
        topP = role.getTopP();
        useTools = role.getUseTools();
        model = role.getModel();
        roleName = role.getName();
        rolePrompt = role.getPrompt();
        dirty = true;
        updateTokens();
    }

    // pub fn sync_agent(&mut self, agent: &Agent) {
    //    self.role_name = None;
    //    self.role_prompt = agent.interpolated_instructions();
    //    self.agent_variables = agent.variables().clone();
    //    self.agent_instructions = self.role_prompt.clone();
    // }
    public void syncAgent(Agent agent) {
        roleName = null;
        rolePrompt = agent.interpolatedInstructions();
        agentVariables = agent.variables();
        agentInstructions = rolePrompt;
    }

    // pub fn clear_messages(&mut self) {
    //    self.messages.clear();
    //    self.compressed_messages.clear();
    //    self.data_urls.clear();
    //    self.autoname = None;
    //    self.dirty = true;
    //    self.update_tokens();
    // }
    public void clearMessages() {
        messages.clear();
        compressedMessages.clear();
        dataUrls.clear();
        autoname = null;
        dirty = true;
        updateTokens();
    }

    // pub fn add_message(&mut self, input: &Input, output: &str) -> Result<()> {
    //    if input.continue_output().is_some() {
    //        if let Some(message) = self.messages.last_mut() {
    //            if let MessageContent::Text(text) = &mut message.content {
    //                *text = format!("{text}{output}");
    //            }
    //        }
    //    } else if input.regenerate() {
    //        if let Some(message) = self.messages.last_mut() {
    //            if let MessageContent::Text(text) = &mut message.content {
    //                *text = output.to_string();
    //            }
    //        }
    //    } else {
    //        if self.messages.is_empty() {
    //            if self.name == TEMP_SESSION_NAME && self.save_session == Some(true) {
    //                let raw_input = input.raw();
    //                let chat_history = format!("USER: {raw_input}\nASSISTANT: {output}\n");
    //                self.autoname = Some(AutoName::new_from_chat_history(chat_history));
    //            }
    //            self.messages.extend(input.role().build_messages(input));
    //        } else {
    //            self.messages
    //                .push(Message::new(MessageRole::User, input.message_content()));
    //        }
    //        self.data_urls.extend(input.data_urls());
    //        if let Some(tool_calls) = input.tool_calls() {
    //            self.messages.push(Message::new(
    //                MessageRole::Tool,
    //                MessageContent::ToolCalls(tool_calls.clone()),
    //            ))
    //        }
    //        self.messages.push(Message::new(
    //            MessageRole::Assistant,
    //            MessageContent::Text(output.to_string()),
    //        ));
    //    }
    //    self.dirty = true;
    //    self.update_tokens();
    //    Ok(())
    // }
    public void addMessage(Input input, String output) {
        if (!isBlank(input.getContinueOutput())) {
            if (!CollectionsUtils.isEmpty(messages)) {
                Message message = messages.getLast();
                if (message.getContent() != null) {
                    message.getContent().setText(format("{}{}", message.getContent().getText(), output));
                }
            }
        } else if (input.isRegenerate()) {
            Message message = messages.getLast();
            if (message.getContent() != null) {
                message.getContent().setText(output);
            }
        } else {
            if (CollectionsUtils.isEmpty(messages)) {
                if (TEMP_SESSION_NAME.equals(name) && saveSession) {
                    Tuple<String, List<String>> rawInput = input.getRaw();
                    String chatHistory = format("USER: {}\nASSISTANT: {}\n", rawInput, output);
                    autoname = AutoName.newFromChatHistory(chatHistory);
                }
                messages = input.getRole().buildMessages(input);
            } else {
                messages.add(new Message().setRole(MessageRole.User).setContent(input.messageContent()));
            }

            dataUrls.putAll(input.getDataUrls());
            MessageContentToolCalls toolCalls = input.getToolCalls();
            if (toolCalls != null) {
                messages.add(new Message().setRole(MessageRole.Tool).setContent(MessageContent.ToolCalls(toolCalls)));
            }

            messages.add(new Message().setRole(MessageRole.Assistant).setContent(MessageContent.Text(output)));
        }

        dirty = true;
        updateTokens();
    }

    // fn to_role(&self) -> Role {
    //    let role_name = self.role_name.as_deref().unwrap_or_default();
    //    let mut role = Role::new(role_name, &self.role_prompt);
    //    role.sync(self);
    //    role
    // }
    public Role toRole() {
        Role role = new Role().setName(roleName).setPrompt(rolePrompt);
        role.sync(this);
        return role;
    }

    // pub fn guard_empty(&self) -> Result<()> {
    //    if !self.is_empty() {
    //        bail!("Cannot perform this operation because the session has messages, please `.empty session` first.");
    //    }
    //    Ok(())
    // }
    public void guardEmpty() {
        if (!isEmpty()) {
            bail("Cannot perform this operation because the session has messages, please `.empty session` first.");
        }
    }

    // pub fn export(&self) -> Result<String> {
    //    let mut data = json!({
    //        "path": self.path,
    //        "model": self.model().id(),
    //    });
    //    if let Some(temperature) = self.temperature() {
    //        data["temperature"] = temperature.into();
    //    }
    //    if let Some(top_p) = self.top_p() {
    //        data["top_p"] = top_p.into();
    //    }
    //    if let Some(use_tools) = self.use_tools() {
    //        data["use_tools"] = use_tools.into();
    //    }
    //    if let Some(save_session) = self.save_session() {
    //        data["save_session"] = save_session.into();
    //    }
    //    let (tokens, percent) = self.tokens_usage();
    //    data["total_tokens"] = tokens.into();
    //    if let Some(max_input_tokens) = self.model().max_input_tokens() {
    //        data["max_input_tokens"] = max_input_tokens.into();
    //    }
    //    if percent != 0.0 {
    //        data["total/max"] = format!("{percent}%").into();
    //    }
    //    data["messages"] = json!(self.messages);
    //
    //    let output = serde_yaml::to_string(&data)
    //        .with_context(|| format!("Unable to show info about session '{}'", &self.name))?;
    //    Ok(output)
    // }
    public String export() {
        Value data = new Value();
        data.put("path", path);
        data.put("model", model.id());
        if (temperature != null) {
            data.put("temperature", temperature);
        }
        if (topP != null) {
            data.put("top_p", topP);
        }
        if (useTools != null) {
            data.put("use_tools", useTools);
        }
        data.put("save_session", saveSession);
        Tuple<Integer, Float> tuple = tokensUsage();
        Integer tokens = tuple.first();
        Float percent = tuple.second();
        data.put("total_tokens", tokens);
        Integer maxInputTokens = model.maxInputTokens();
        if (maxInputTokens != null) {
            data.put("max_input_tokens", maxInputTokens);
        }
        if (percent != 0.0f) {
            data.put("total/max", format("{}%", percent));
        }
        data.put("messages", toJson(messages).asMap());

        return SerDe.toYamlString(data);
    }

    // pub fn tokens_usage(&self) -> (usize, f32) {
    //    let tokens = self.tokens();
    //    let max_input_tokens = self.model().max_input_tokens().unwrap_or_default();
    //    let percent = if max_input_tokens == 0 {
    //        0.0
    //    } else {
    //        let percent = tokens as f32 / max_input_tokens as f32 * 100.0;
    //        (percent * 100.0).round() / 100.0
    //    };
    //    (tokens, percent)
    // }
    private Tuple<Integer, Float> tokensUsage() {
        Integer maxInputTokens = model.maxInputTokens();
        float percent;
        if (tokens == null || maxInputTokens == null || maxInputTokens == 0) {
            percent = 0.0f;
        } else {
            percent = (float) tokens / (float) maxInputTokens * 100.0f;
            percent = Math.round(percent * 100.0f) / 100.0f;
        }
        return new Tuple<>(tokens, percent);
    }

    // pub fn update_tokens(&mut self) {
    //    self.tokens = self.model().total_tokens(&self.messages);
    // }
    private void updateTokens() {
        tokens = model.totalTokens(messages);
    }

    // pub fn is_empty(&self) -> bool {
    //    self.messages.is_empty() && self.compressed_messages.is_empty()
    // }
    public boolean isEmpty() {
        return CollectionsUtils.isEmpty(messages) && CollectionsUtils.isEmpty(compressedMessages);
    }
}
