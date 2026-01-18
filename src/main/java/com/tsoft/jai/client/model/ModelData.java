package com.tsoft.jai.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ModelData {

    private String name;
    // #[serde(default = "default_model_type", rename = "type")]
    @JsonProperty("type")
    private String modelType = defaultModelType();
    // #[serde(skip_serializing_if = "Option::is_none")]
    private String realName;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer maxInputTokens;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private BigDecimal inputPrice;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private BigDecimal outputPrice;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Value patch;

    // chat-only properties
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer maxOutputTokens;
    // #[serde(default, skip_serializing_if = "std::ops::Not::not")]
    private boolean requireMaxTokens;
    // #[serde(default, skip_serializing_if = "std::ops::Not::not")]
    private boolean supportsVision;
    // #[serde(default, skip_serializing_if = "std::ops::Not::not")]
    private boolean supportsFunctionCalling;
    // #[serde(default, skip_serializing_if = "std::ops::Not::not")]
    private boolean noStream;
    // #[serde(default, skip_serializing_if = "std::ops::Not::not")]
    private boolean noSystemMessage;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private String systemPromptPrefix;

    // embedding-only properties
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer maxTokensPerChunk;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer defaultChunkSize;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer maxBatchSize;

    // pub fn new(name: &str) -> Self {
    //    Self {
    //        name: name.to_string(),
    //        model_type: default_model_type(),
    //        ..Default::default()
    //    }
    // }
    public ModelData(String name) {
        this.name = name;
        this.modelType = defaultModelType();
    }

    // fn default_model_type() -> String {
    //    "chat".into()
    // }
    private static String defaultModelType() {
        return "chat";
    }
}
