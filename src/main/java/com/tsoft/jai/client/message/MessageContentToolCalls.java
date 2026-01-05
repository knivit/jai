package com.tsoft.jai.client.message;

import com.tsoft.jai.function.ToolResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MessageContentToolCalls {

    private List<ToolResult> toolResults;
    private String text;
    private boolean sequence;
}
