package com.tsoft.jai.mods.provider.openai.api.v1.chat.rs;

import lombok.Data;

@Data
public class StreamChoiceRs {

    private Integer index;
    private StreamDeltaRs delta;
    private String finishReason;
}
