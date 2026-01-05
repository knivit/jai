package com.tsoft.jai.client.common;

import com.tsoft.jai.function.ToolCall;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ChatCompletionsOutput {

    private String text;
    private List<ToolCall> toolCalls;
    private String id;
    private Integer inputTokens;
    private Integer outputTokens;
}
