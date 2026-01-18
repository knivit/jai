package com.tsoft.jai.client.message;

import com.tsoft.jai.function.ToolResult;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class MessageContentToolCalls {

    private List<ToolResult> toolResults;
    private String text;
    private boolean sequence;

    // pub fn merge(&mut self, tool_results: Vec<ToolResult>, _text: String) {
    //    self.tool_results.extend(tool_results);
    //    self.text.clear();
    //    self.sequence = true;
    // }
    public void merge(List<ToolResult> toolResults, String _text) {
        if (this.toolResults == null) {
            this.toolResults = new ArrayList<>();
        }
        this.toolResults.addAll(toolResults);
        text = "";
        sequence = true;
    }
}
