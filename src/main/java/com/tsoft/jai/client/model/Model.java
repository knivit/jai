package com.tsoft.jai.client.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Model {

    private String clientName;

    @JsonUnwrapped
    private ModelData data;

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
        this.clientName = name;
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

    // pub fn id(&self) -> String {
    //    if self.data.name.is_empty() {
    //        self.client_name.to_string()
    //    } else {
    //        format!("{}:{}", self.client_name, self.data.name)
    //    }
    // }
    public String id() {
        if (data.getName() == null || data.getName().isBlank()) {
            return clientName;
        } else {
            return "%s:%s".formatted(clientName, data.getName());
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
    public Map<String, Object> getPatch() {
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
}
