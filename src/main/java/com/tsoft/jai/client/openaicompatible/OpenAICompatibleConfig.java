package com.tsoft.jai.client.openaicompatible;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OpenAICompatibleConfig {

    private String name;
    private String apiBase;
    private String apiKey;
}
