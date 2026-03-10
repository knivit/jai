package com.tsoft.jai.mods.provider.openai.api.v1.chat.rs;

import lombok.Data;

@Data
public class StreamDeltaRs {

    private String role;
    private String content;
}
