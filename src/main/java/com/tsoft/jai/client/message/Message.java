package com.tsoft.jai.client.message;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Message {

    private MessageRole role;
    private MessageContent content;
}
