package com.tsoft.jai.client.common;

import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class RequestPatch {

    private Map<String, Value> chatCompletions;
    private Map<String, Value> embeddings;
    private Map<String, Value> rerank;
}
