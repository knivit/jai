package com.tsoft.jai.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ModelData {

    private String name;
    // #[serde(default = "default_model_type", rename = "type")]
    private String modelType;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private String realName;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Integer maxInputTokens;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private BigDecimal inputPrice;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private BigDecimal outputPrice;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Map<String, Object> patch;

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
}
