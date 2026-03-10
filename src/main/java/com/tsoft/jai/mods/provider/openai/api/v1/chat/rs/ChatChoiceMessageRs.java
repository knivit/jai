package com.tsoft.jai.mods.provider.openai.api.v1.chat.rs;

import lombok.Data;

import java.util.List;

@Data
public class ChatChoiceMessageRs {

    private String role;
    private String content;
    private List<ChatChoiceMessageToolCallRs> toolCalls;
}
