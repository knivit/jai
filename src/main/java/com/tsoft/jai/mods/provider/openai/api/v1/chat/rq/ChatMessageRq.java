package com.tsoft.jai.mods.provider.openai.api.v1.chat.rq;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageRq {

    private String role;
    private String content;
}
