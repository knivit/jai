package com.tsoft.jai.client.common;

import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.function.FunctionDeclaration;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ChatCompletionsData {

    private List<Message> messages;
    private Double temperature;
    private Double topP;
    private List<FunctionDeclaration> functions;
    private boolean stream;
}
