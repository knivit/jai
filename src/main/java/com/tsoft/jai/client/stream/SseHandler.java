package com.tsoft.jai.client.stream;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.anyhow.Result.isErr;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

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
    public Result<?> text(String text) {
        if (isBlank(text)) {
            return Ok();
        }
        buffer.append(text);
        Result<?> ret = sender
            .send(SseEvent.Text(text))
            .withContext(() -> "Failed to send SseEvent:Text");
        if (isErr(ret)) {
            if (abortSignal.aborted()) {
                return Ok();
            }
            return ret;
        }
        return Ok();
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

    // pub fn abort(&self) -> AbortSignal {
    //    self.abort_signal.clone()
    // }
    public AbortSignal abort() {
        return abortSignal;
    }

    // pub fn take(self) -> (String, Vec<ToolCall>) {
    //    let Self {
    //        buffer, tool_calls, ..
    //    } = self;
    //    (buffer, tool_calls)
    // }
    public Tuple<String, List<ToolCall>> take() {
        return new Tuple<>(buffer.toString(), toolCalls);
    }
}
