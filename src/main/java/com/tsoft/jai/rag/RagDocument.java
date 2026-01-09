package com.tsoft.jai.rag;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class RagDocument {

    private String pageContent;
    private Map<String, String> metadata;
}
