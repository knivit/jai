package com.tsoft.jai.mods.provider.openai.api.v1.chat.rq;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatMessageRq {

    private String role;
    private String content;
}
