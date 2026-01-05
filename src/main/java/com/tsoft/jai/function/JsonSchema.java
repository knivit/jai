package com.tsoft.jai.function;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class JsonSchema {

    // #[serde(rename = "type", skip_serializing_if = "Option::is_none")]
    private String typeValue;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private String description;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Map<String, JsonSchema> properties;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private List<JsonSchema> items;
    // #[serde(rename = "anyOf", skip_serializing_if = "Option::is_none")]
    private List<JsonSchema> anyOf;
    // #[serde(rename = "enum", skip_serializing_if = "Option::is_none")]
    private List<String> enumValue;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private Object defaultValue;
    // #[serde(skip_serializing_if = "Option::is_none")]
    private List<String> required;
}
