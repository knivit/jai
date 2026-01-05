package com.tsoft.jai.client.stream;

import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class SseHandler {

    private final UnboundedSender<SseEvent> sender;
    private final AbortSignal abortSignal;
    private StringBuilder buffer = new StringBuilder();
    private List<ToolCall> toolCalls = new ArrayList<>();

    // pub fn text(&mut self, text: &str) -> Result<()> {
    //    // debug!("HandleText: {}", text);
    //    if text.is_empty() {
    //        return Ok(());
    //    }
    //    self.buffer.push_str(text);
    //    let ret = self
    //        .sender
    //        .send(SseEvent::Text(text.to_string()))
    //        .with_context(|| "Failed to send SseEvent:Text");
    //    if let Err(err) = ret {
    //        if self.abort_signal.aborted() {
    //            return Ok(());
    //        }
    //        return Err(err);
    //    }
    //    Ok(())
    // }
    public void text(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        buffer.append(text);
        sender.send(SseEvent.Text(text));
    }

    // pub fn done(&mut self) {
    //    // debug!("HandleDone");
    //    let ret = self.sender.send(SseEvent::Done);
    //    if ret.is_err() {
    //        if self.abort_signal.aborted() {
    //            return;
    //        }
    //        warn!("Failed to send SseEvent:Done");
    //    }
    // }
    public void done() {
        sender.send(SseEvent.Done());
    }

    // pub fn tool_call(&mut self, call: ToolCall) -> Result<()> {
    //    // debug!("HandleCall: {:?}", call);
    //    self.tool_calls.push(call);
    //    Ok(())
    // }
    public void toolCall(ToolCall call) {
        toolCalls.add(call);
    }
}
