package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageRole;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.Asset;
import com.tsoft.jai.utils.base.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.*;
import static com.tsoft.jai.utils.Variables.interpolateVariables;

@Data
@Accessors(chain = true)
public class Role {

    private String name;
    // #[serde(default)]
    private String prompt;
    //#[serde(rename(serialize = "model", deserialize = "model"), skip_serializing_if = "Option::is_none")]
    @JsonProperty("model")
    private String modelId;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double temperature;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private Double topP;
    //#[serde(skip_serializing_if = "Option::is_none")]
    private String useTools;

    //#[serde(skip)]
    @JsonIgnore
    private Model model;

    public static final String SHELL_ROLE = "%shell%";
    public static final String EXPLAIN_SHELL_ROLE = "%explain-shell%";
    public static final String CODE_ROLE = "%code%";
    public static final String CREATE_TITLE_ROLE = "%create-title%";
    public static final String INPUT_PLACEHOLDER = "__INPUT__";

    private static final Pattern RE_METADATA = Pattern.compile("(?s)-{3,}\\s*(.*?)\\s*-{3,}\\s*(.*)");

    // pub fn new(name: &str, content: &str) -> Self {
    //    let mut metadata = "";
    //    let mut prompt = content.trim();
    //    if let Ok(Some(caps)) = RE_METADATA.captures(content) {
    //        if let (Some(metadata_value), Some(prompt_value)) = (caps.get(1), caps.get(2)) {
    //            metadata = metadata_value.as_str().trim();
    //            prompt = prompt_value.as_str().trim();
    //        }
    //    }
    //    let mut prompt = prompt.to_string();
    //    interpolate_variables(&mut prompt);
    //    let mut role = Self {
    //        name: name.to_string(),
    //        prompt,
    //        ..Default::default()
    //    };
    //    if !metadata.is_empty() {
    //        if let Ok(value) = serde_yaml::from_str::<Value>(metadata) {
    //            if let Some(value) = value.as_object() {
    //                for (key, value) in value {
    //                    match key.as_str() {
    //                        "model" => role.model_id = value.as_str().map(|v| v.to_string()),
    //                        "temperature" => role.temperature = value.as_f64(),
    //                        "top_p" => role.top_p = value.as_f64(),
    //                        "use_tools" => role.use_tools = value.as_str().map(|v| v.to_string()),
    //                        _ => (),
    //                    }
    //                }
    //            }
    //        }
    //    }
    //    role
    // }
    public static Role create(String name, String content) {
        String metadata = "";
        String prompt = isBlank(content) ? "" : content.trim();
        if (RE_METADATA.matcher(content).hasMatch()) {
            String[] caps = RE_METADATA.split(content);
            String metadataValue = (caps.length <= 1) ? null : caps[1];
            String promptValue = (caps.length <= 2) ? null : caps[2];
            if (!isBlank(metadataValue) && !isBlank(promptValue)) {
                metadata = metadataValue.trim();
                prompt = promptValue.trim();
            }
        }
        prompt = interpolateVariables(prompt);
        Role role = new Role().setName(name).setPrompt(prompt);
        if (!isBlank(metadata)) {
            Result<Value> res = SerDe.parseYaml(metadata);
            if (isOk(res)) {
                Value value = res.getValue();
                for (Map.Entry<String, String> entry : value.asMap().entrySet()){
                    switch (entry.getKey()) {
                        case "model" -> role.modelId = entry.getValue();
                        case "temperature" -> role.temperature = Double.parseDouble(entry.getValue());
                        case "top_p" -> role.topP = Double.parseDouble(entry.getValue());
                        case "use_tools" -> role.useTools = entry.getValue();
                    }
                }
            }
        }
        return role;
    }

    // pub fn is_empty_prompt(&self) -> bool {
    //    self.prompt.is_empty()
    // }
    public boolean isEmptyPrompt() {
        return isBlank(prompt);
    }

    private static final String ROLES_ASSET = "assets/roles";

    // pub fn builtin(name: &str) -> Result<Self> {
    //    let content = RolesAsset::get(&format!("{name}.md"))
    //        .ok_or_else(|| anyhow!("Unknown role `{name}`"))?;
    //    let content = unsafe { std::str::from_utf8_unchecked(&content.data) };
    //    Ok(Role::new(name, content))
    // }
    public static Result<Role> builtin(String name) {
        Result<File> res = Asset.file(format("{}/{}.md", ROLES_ASSET, name))
            .withContext(() -> format("Unknown role `{}`", name));
        if (isErr(res)) {
            return Err(res);
        }
        File file = res.getValue();
        String content = readFile(file);
        return Ok(Role.create(name, content));
    }

    // pub fn list_builtin_role_names() -> Vec<String> {
    //    RolesAsset::iter()
    //        .filter_map(|v| v.strip_suffix(".md").map(|v| v.to_string()))
    //        .collect()
    // }
    public static List<String> listBuiltinRoleNames() {
        return Asset.files(ROLES_ASSET).stream()
            .map(File::getName)
            .filter(e -> e.endsWith(".md"))
            .map(e -> e.substring(0, e.length() - 3))
            .toList();
    }

    // pub fn echo_messages(&self, input: &Input) -> String {
    //    let input_markdown = input.render();
    //    if self.is_empty_prompt() {
    //        input_markdown
    //    } else if self.is_embedded_prompt() {
    //        self.prompt.replace(INPUT_PLACEHOLDER, &input_markdown)
    //    } else {
    //        format!("{}\n\n{}", self.prompt, input_markdown)
    //    }
    // }
    public String echoMessages(Input input) {
        String inputMarkdown = input.render();
        if (isEmptyPrompt()) {
            return inputMarkdown;
        } else if (isEmbeddedPrompt()) {
            return prompt.replace(INPUT_PLACEHOLDER, inputMarkdown);
        } else {
            return format("{}\n\n{}", prompt, inputMarkdown);
        }
    }

    // pub fn build_messages(&self, input: &Input) -> Vec<Message> {
    //    let mut content = input.message_content();
    //    let mut messages = if self.is_empty_prompt() {
    //        vec![Message::new(MessageRole::User, content)]
    //    } else if self.is_embedded_prompt() {
    //        content.merge_prompt(|v: &str| self.prompt.replace(INPUT_PLACEHOLDER, v));
    //        vec![Message::new(MessageRole::User, content)]
    //    } else {
    //        let mut messages = vec![];
    //        let (system, cases) = parse_structure_prompt(&self.prompt);
    //        if !system.is_empty() {
    //            messages.push(Message::new(
    //                MessageRole::System,
    //                MessageContent::Text(system.to_string()),
    //            ));
    //        }
    //        if !cases.is_empty() {
    //            messages.extend(cases.into_iter().flat_map(|(i, o)| {
    //                vec![
    //                    Message::new(MessageRole::User, MessageContent::Text(i.to_string())),
    //                    Message::new(MessageRole::Assistant, MessageContent::Text(o.to_string())),
    //                ]
    //            }));
    //        }
    //        messages.push(Message::new(MessageRole::User, content));
    //        messages
    //    };
    //    if let Some(text) = input.continue_output() {
    //        messages.push(Message::new(
    //            MessageRole::Assistant,
    //            MessageContent::Text(text.into()),
    //        ));
    //    }
    //    messages
    // }
    public List<Message> buildMessages(Input input) {
        MessageContent content = input.messageContent();
        List<Message> messages = new ArrayList<>();
        if (isBlank(prompt)) {
            messages.add(new Message().setRole(MessageRole.User).setContent(content));
        } else if (isEmbeddedPrompt()) {
            content.mergePrompt(e -> e.replace(INPUT_PLACEHOLDER, e));
            messages.add(new Message().setRole(MessageRole.User).setContent(content));
        } else {
            Tuple<String, List<Tuple<String, String>>> tuple = parseStructurePrompt(prompt);
            String system = tuple.first();
            List<Tuple<String, String>> cases = tuple.second();
            if (!isBlank(system)) {
                messages.add(new Message().setRole(MessageRole.System).setContent(MessageContent.Text(system)));
            }
            if (!isEmpty(cases)) {
                for (Tuple<String, String> it : cases) {
                    messages.add(new Message().setRole(MessageRole.User).setContent(MessageContent.Text(it.first())));
                    messages.add(new Message().setRole(MessageRole.Assistant).setContent(MessageContent.Text(it.second())));
                }
            }
            messages.add(new Message().setRole(MessageRole.User).setContent(content));
        }

        String text = input.getContinueOutput();
        if (!isBlank(text)) {
            messages.add(new Message().setRole(MessageRole.Assistant).setContent(MessageContent.Text(text)));
        }
        return messages;
    }

    // pub fn is_embedded_prompt(&self) -> bool {
    //    self.prompt.contains(INPUT_PLACEHOLDER)
    // }
    public boolean isEmbeddedPrompt() {
        return !isBlank(prompt) && prompt.contains(INPUT_PLACEHOLDER);
    }

    // fn parse_structure_prompt(prompt: &str) -> (&str, Vec<(&str, &str)>) {
    //    let mut text = prompt;
    //    let mut search_input = true;
    //    let mut system = None;
    //    let mut parts = vec![];
    //    loop {
    //        let search = if search_input {
    //            "### INPUT:"
    //        } else {
    //            "### OUTPUT:"
    //        };
    //        match text.find(search) {
    //            Some(idx) => {
    //                if system.is_none() {
    //                    system = Some(&text[..idx])
    //                } else {
    //                    parts.push(&text[..idx])
    //                }
    //                search_input = !search_input;
    //                text = &text[(idx + search.len())..];
    //            }
    //            None => {
    //                if !text.is_empty() {
    //                    if system.is_none() {
    //                        system = Some(text)
    //                    } else {
    //                        parts.push(text)
    //                    }
    //                }
    //                break;
    //            }
    //        }
    //    }
    //    let parts_len = parts.len();
    //    if parts_len > 0 && parts_len % 2 == 0 {
    //        let cases: Vec<(&str, &str)> = parts
    //            .iter()
    //            .step_by(2)
    //            .zip(parts.iter().skip(1).step_by(2))
    //            .map(|(i, o)| (i.trim(), o.trim()))
    //            .collect();
    //        let system = system.map(|v| v.trim()).unwrap_or_default();
    //        return (system, cases);
    //    }
    //
    //    (prompt, vec![])
    // }
    private Tuple<String, List<Tuple<String, String>>> parseStructurePrompt(String prompt) {
        String text = prompt;
        boolean searchInput = true;
        String system = null;
        List<String> parts = new ArrayList<>();
        while (true) {
            String search = searchInput ? "### INPUT:" : "### OUTPUT:";
            int idx = text.indexOf(search);
            if (idx != -1) {
                if (system == null) {
                    system = text.substring(0, idx);
                } else {
                    parts.add(text.substring(0, idx));
                }
                searchInput = !searchInput;
                text = text.substring(idx + search.length());
            } else {
                if (!isBlank(text)) {
                    if (system == null) {
                        system = text;
                    } else {
                        parts.add(text);
                    }
                }
                break;
            }
        }

        int partsLen = parts.size();
        if (partsLen > 0 && (partsLen % 2) == 0) {
            List<Tuple<String, String>> cases = new ArrayList<>();
            for (int i = 0; i < parts.size(); i += 2) {
                Tuple<String, String> tuple = new Tuple<>(parts.get(i).trim(), parts.get(i + 1).trim());
                cases.add(tuple);
            }
            system = system.trim();
            return new Tuple<>(system, cases);
        }

        return new Tuple<>(prompt, Collections.emptyList());
    }

    // pub fn sync<T: RoleLike>(&mut self, role_like: &T) {
    //    let model = role_like.model();
    //    let temperature = role_like.temperature();
    //    let top_p = role_like.top_p();
    //    let use_tools = role_like.use_tools();
    //    self.batch_set(model, temperature, top_p, use_tools);
    // }
    public void sync(Session session) {
        Model model = session.getModel();
        Double temperature = session.getTemperature();
        Double topP = session.getTopP();
        String useTools = session.getUseTools();
        batchSet(model, temperature, topP, useTools);
    }

    public void sync(Agent agent) {
        Model model = agent.getModel();
        Double temperature = agent.getConfig().getTemperature();
        Double topP = agent.getConfig().getTopP();
        String useTools = agent.getConfig().getUseTools();
        batchSet(model, temperature, topP, useTools);
    }

    // pub fn batch_set(
    //     &mut self,
    //     model: &Model,
    //     temperature: Option<f64>,
    //     top_p: Option<f64>,
    //     use_tools: Option<String>,
    // ) {
    //     self.set_model(model.clone());
    //     if temperature.is_some() {
    //         self.set_temperature(temperature);
    //     }
    //     if top_p.is_some() {
    //         self.set_top_p(top_p);
    //     }
    //     if use_tools.is_some() {
    //         self.set_use_tools(use_tools);
    //     }
    // }
    public void batchSet(Model model, Double temperature, Double topP, String useTools) {
        this.model = model;
        if (temperature != null) {
            this.temperature = temperature;
        }
        if (topP != null) {
            this.topP = topP;
        }
        if (!isBlank(useTools)) {
            this.useTools = useTools;
        }
    }

    // pub fn export(&self) -> String {
    //    let mut metadata = vec![];
    //    if let Some(model) = self.model_id() {
    //        metadata.push(format!("model: {model}"));
    //    }
    //    if let Some(temperature) = self.temperature() {
    //        metadata.push(format!("temperature: {temperature}"));
    //    }
    //    if let Some(top_p) = self.top_p() {
    //        metadata.push(format!("top_p: {top_p}"));
    //    }
    //    if let Some(use_tools) = self.use_tools() {
    //        metadata.push(format!("use_tools: {use_tools}"));
    //    }
    //    if metadata.is_empty() {
    //        format!("{}\n", self.prompt)
    //    } else if self.prompt.is_empty() {
    //        format!("---\n{}\n---\n", metadata.join("\n"))
    //    } else {
    //        format!("---\n{}\n---\n\n{}\n", metadata.join("\n"), self.prompt)
    //    }
    // }
    public String export() {
        List<String> metadata = new ArrayList<>();
        if (modelId != null) {
            metadata.add(format("model: {}", modelId));
        }
        if (temperature != null) {
            metadata.add(format("temperature: {}", temperature));
        }
        if (topP != null) {
            metadata.add(format("top_p: {}", topP));
        }
        if (useTools != null) {
            metadata.add(format("use_tools: {}", useTools));
        }
        if (isEmpty(metadata)) {
            return format("{}\n", prompt);
        } else if (isBlank(prompt)) {
            return format("---\n{}\n---\n", String.join("\n", metadata));
        } else {
            return format("---\n{}\n---\n\n{}\n", String.join("\n", metadata), prompt);
        }
    }
}
