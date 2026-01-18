package com.tsoft.jai.client.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentPart;
import com.tsoft.jai.client.message.MessageRole;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.client.macros.Macros.listAllModels;
import static com.tsoft.jai.client.macros.Macros.listClientNames;
import static com.tsoft.jai.client.model.ModelType.canCreateFromName;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.Mod.estimateTokenLength;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.StringUtils.*;

@Data
@Accessors(chain = true)
public class Model {

    private String clientName;

    @JsonUnwrapped
    private ModelData data;

    private static final int PER_MESSAGES_TOKENS = 5;
    private static final int  BASIS_TOKENS = 2;

    // fn default() -> Self {
    //    Model::new("", "")
    // }
    public Model() {
        this("", "");
    }

    // pub fn new(client_name: &str, name: &str) -> Self {
    //    Self {
    //        client_name: client_name.into(),
    //        data: ModelData::new(name),
    //    }
    // }
    public Model(String clientName, String name) {
        this.clientName = clientName;
        this.data = new ModelData(name);
    }

    // pub fn from_config(client_name: &str, models: &[ModelData]) -> Vec<Self> {
    //    models
    //        .iter()
    //        .map(|v| Model {
    //            client_name: client_name.to_string(),
    //            data: v.clone(),
    //        })
    //        .collect()
    // }
    public static List<Model> fromConfig(String clientName, List<ModelData> models) {
        return models.stream()
            .map(e -> new Model()
                .setClientName(clientName)
                .setData(e))
            .toList();
    }

    // pub fn retrieve_model(config: &Config, model_id: &str, model_type: ModelType) -> Result<Self> {
    //    let models = list_all_models(config);
    //    let (client_name, model_name) = match model_id.split_once(':') {
    //        Some((client_name, model_name)) => {
    //            if model_name.is_empty() {
    //                (client_name, None)
    //            } else {
    //                (client_name, Some(model_name))
    //            }
    //        }
    //        None => (model_id, None),
    //    };
    //    match model_name {
    //        Some(model_name) => {
    //            if let Some(model) = models.iter().find(|v| v.id() == model_id) {
    //                if model.model_type() == model_type {
    //                    return Ok((*model).clone());
    //                } else {
    //                    bail!("Model '{model_id}' is not a {model_type} model")
    //                }
    //            }
    //            if list_client_names(config)
    //                .into_iter()
    //                .any(|v| *v == client_name)
    //                && model_type.can_create_from_name()
    //            {
    //                let mut new_model = Self::new(client_name, model_name);
    //                new_model.data.model_type = model_type.to_string();
    //                return Ok(new_model);
    //            }
    //        }
    //        None => {
    //            if let Some(found) = models
    //                .iter()
    //                .find(|v| v.client_name == client_name && v.model_type() == model_type)
    //            {
    //                return Ok((*found).clone());
    //            }
    //        }
    //    };
    //    bail!("Unknown {model_type} model '{model_id}'")
    // }
    public static Model retrieveModel(Config config, String modelId, ModelType modelType) {
        List<Model> models = listAllModels(config);
        Tuple<String, String> tuple = splitOnce(modelId, ':');
        String clientName = isBlank(tuple.first()) ? modelId : tuple.first();
        String modelName = tuple.second();

        if (!isBlank(modelName)) {
            Model model = models.stream()
                .filter(e -> modelId.equals(e.id()))
                .findAny()
                .orElse(null);
            if (model != null) {
                if (Objects.equals(model.getModelType(), modelType)) {
                    return model;
                } else {
                    bail("Model '{}' is not a {} model", modelId, modelType);
                }
            }

            List<String> clientNames = listClientNames(config);
            if (clientNames.stream().anyMatch(e -> e.equals(clientName)) && canCreateFromName(modelType)) {
                Model newModel = new Model(clientName, modelName);
                newModel.data.setModelType(modelType.name());
                return newModel;
            }
        } else {
            Optional<Model> found = models.stream()
                .filter(e -> Objects.equals(e.getClientName(), clientName) && Objects.equals(e.getModelType(), modelType))
                .findAny();
            if (found.isPresent()) {
                return found.get();
            }
        }

        bail("Unknown {} model '{}'", modelType, modelId);
        return null;
    }

    // pub fn id(&self) -> String {
    //    if self.data.name.is_empty() {
    //        self.client_name.to_string()
    //    } else {
    //        format!("{}:{}", self.client_name, self.data.name)
    //    }
    // }
    public String id() {
        if (isBlank(data.getName())) {
            return clientName;
        } else {
            return format("{}:{}", clientName, data.getName());
        }
    }

    // pub fn real_name(&self) -> &str {
    //     self.data.real_name.as_deref().unwrap_or(&self.data.name)
    // }
    public String getRealName() {
        String result = null;
        if (data != null) {
            result = data.getRealName();
            if (result == null) {
                result = data.getName();
            }
        }
        return result;
    }

    // pub fn model_type(&self) -> ModelType {
    //    if self.data.model_type.starts_with("embed") {
    //        ModelType::Embedding
    //    } else if self.data.model_type.starts_with("rerank") {
    //        ModelType::Reranker
    //    } else {
    //        ModelType::Chat
    //    }
    // }
    public ModelType getModelType() {
        if (data.getModelType().startsWith("embed")) {
            return ModelType.Embedding;
        } else if (data.getModelType().startsWith("rerank")) {
            return ModelType.Reranker;
        } else {
            return ModelType.Chat;
        }
    }

    // pub fn patch(&self) -> Option<&Value> {
    //     self.data.patch.as_ref()
    // }
    public Value getPatch() {
        return (data == null) ? null : data.getPatch();
    }

    // pub fn max_tokens_param(&self) -> Option<isize> {
    //    if self.data.require_max_tokens {
    //        self.data.max_output_tokens
    //    } else {
    //        None
    //    }
    // }
    public Integer getMaxTokensParam() {
        if (data != null) {
            if (data.isRequireMaxTokens()) {
                return data.getMaxOutputTokens();
            }
        }
        return null;
    }

    // pub fn messages_tokens(&self, messages: &[Message]) -> usize {
    //    let messages_len = messages.len();
    //    messages
    //        .iter()
    //        .enumerate()
    //        .map(|(i, v)| match &v.content {
    //            MessageContent::Text(text) => {
    //                if v.role.is_assistant() && i != messages_len - 1 {
    //                    estimate_token_length(&strip_think_tag(text))
    //                } else {
    //                    estimate_token_length(text)
    //                }
    //            }
    //            MessageContent::Array(list) => list
    //                .iter()
    //                .map(|v| match v {
    //                    MessageContentPart::Text { text } => estimate_token_length(text),
    //                    MessageContentPart::ImageUrl { .. } => 0,
    //                })
    //                .sum(),
    //            MessageContent::ToolCalls(MessageContentToolCalls {
    //                tool_results, text, ..
    //            }) => {
    //                estimate_token_length(text)
    //                    + tool_results
    //                        .iter()
    //                        .map(|v| {
    //                            serde_json::to_string(v)
    //                                .map(|v| estimate_token_length(&v))
    //                                .unwrap_or_default()
    //                        })
    //                        .sum::<usize>()
    //            }
    //        })
    //        .sum()
    // }
    private int messagesTokens(List<Message> messages) {
        int messagesLen = messages.size();

        int total = 0;
        for (int i = 0; i < messages.size(); i ++) {
            Message v = messages.get(i);
            MessageContent c = v.getContent();
            total += switch (c.getType()) {
                case Text -> {
                    String text = c.getText();
                    if (MessageRole.isAssistant(v.getRole()) && i != messagesLen) {
                        yield estimateTokenLength(stripThinkTag(text));
                    } else {
                        yield estimateTokenLength(text);
                    }
                }
                case Array -> {
                    List<MessageContentPart> list = c.getArray();
                    int sum = 0;
                    for (MessageContentPart part : list) {
                        sum += switch (part.getType()) {
                            case Text -> estimateTokenLength(part.getText());
                            case ImageUrl -> 0;
                        };
                    }
                    yield sum;
                }
                case ToolCalls -> {
                    List<ToolResult> toolResults = c.getToolCalls().getToolResults();
                    String text = c.getToolCalls().getText();
                    int sum = estimateTokenLength(text);
                    for (ToolResult toolResult : toolResults) {
                        String str = SerDe.toJsonString(toolResult);
                        sum += estimateTokenLength(str);
                    }
                    yield sum;
                }
            };
        }
        return total;
    }

    // pub fn max_input_tokens(&self) -> Option<usize> {
    //    self.data.max_input_tokens
    // }
    public Integer maxInputTokens() {
        return (data == null) ? null : data.getMaxInputTokens();
    }

    // pub fn total_tokens(&self, messages: &[Message]) -> usize {
    //     if messages.is_empty() {
    //         return 0;
    //     }
    //     let num_messages = messages.len();
    //     let message_tokens = self.messages_tokens(messages);
    //     if messages[num_messages - 1].role.is_user() {
    //         num_messages * PER_MESSAGES_TOKENS + message_tokens
    //     } else {
    //         (num_messages - 1) * PER_MESSAGES_TOKENS + message_tokens
    //     }
    // }
    public Integer totalTokens(List<Message> messages) {
        if (isEmpty(messages)) {
            return 0;
        }

        int numMessages = messages.size();
        int messageTokens = messagesTokens(messages);
        if (MessageRole.isUser(messages.get(numMessages - 1).getRole())) {
            return numMessages * PER_MESSAGES_TOKENS + messageTokens;
        } else {
            return (numMessages - 1) * PER_MESSAGES_TOKENS + messageTokens;
        }
    }

    // pub fn no_stream(&self) -> bool {
    //    self.data.no_stream
    // }
    public boolean noStream() {
        return (data != null) && data.isNoStream();
    }
}
