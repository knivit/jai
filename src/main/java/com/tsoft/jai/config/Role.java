package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.utils.Asset;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;

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

    // pub fn list_builtin_role_names() -> Vec<String> {
    //    RolesAsset::iter()
    //        .filter_map(|v| v.strip_suffix(".md").map(|v| v.to_string()))
    //        .collect()
    // }
    public static List<String> listBuiltinRoleNames() {
        return Asset.get("assets/roles").stream()
            .map(File::getName)
            .filter(e -> e.endsWith(".md"))
            .map(e -> e.substring(0, e.length() - 3))
            .toList();
    }
}
