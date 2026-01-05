package com.tsoft.jai.client.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MessageContent {

    private String text;
    private List<MessageContentPart> array;
    // Note: This type is primarily for convenience and does not exist in OpenAI's API.
    private MessageContentToolCalls toolCalls;
}
