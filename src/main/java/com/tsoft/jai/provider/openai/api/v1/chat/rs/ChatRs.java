package com.tsoft.jai.provider.openai.api.v1.chat.rs;

import lombok.Data;

import java.util.List;

@Data
public class ChatRs {

    private List<ChatChoiceRs> choices;
}
