package com.tsoft.jai.utils.loader;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class LoadedDocument {

    private String path;
    private String contents;
    // #[serde(default)]
    private Map<String, String> metadata;
}
