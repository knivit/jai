package com.tsoft.jai.client.common;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.Tuple;
import com.tsoft.jai.utils.TupleN;

import java.util.List;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.config.Input.abortableRunWithSpinner;
import static com.tsoft.jai.function.Functions.evalToolCalls;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.render.Mod.renderStream;
import static com.tsoft.jai.tokio.Join.join;
import static com.tsoft.jai.utils.Mod.extractCodeBlock;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.StringUtils.isBlank;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Common {

    // pub async fn call_chat_completions(
    //    input: &Input,
    //    print: bool,
    //    extract_code: bool,
    //    client: &dyn Client,
    //    abort_signal: AbortSignal,
    //) -> Result<(String, Vec<ToolResult>)> {
    //    let ret = abortable_run_with_spinner(
    //        client.chat_completions(input.clone()),
    //        "Generating",
    //        abort_signal,
    //    )
    //    .await;
    //
    //    match ret {
    //        Ok(ret) => {
    //            let ChatCompletionsOutput {
    //                mut text,
    //                tool_calls,
    //                ..
    //            } = ret;
    //            if !text.is_empty() {
    //                if extract_code {
    //                    text = extract_code_block(&strip_think_tag(&text)).to_string();
    //                }
    //                if print {
    //                    client.global_config().read().print_markdown(&text)?;
    //                }
    //            }
    //            Ok((text, eval_tool_calls(client.global_config(), tool_calls)?))
    //        }
    //        Err(err) => Err(err),
    //    }
    // }
    public static Tuple<String, List<ToolResult>> callChatCompletions(Input input, boolean print, boolean exractCode, Client client, AbortSignal abortSignal) {
        ChatCompletionsOutput ret = abortableRunWithSpinner(() -> client.chatCompletions(input), "Generating", abortSignal);
        if (ret != null) {
            String text = ret.getText();
            List<ToolCall> toolCalls = ret.getToolCalls();
            if (!isBlank(text)) {
                if (exractCode) {
                    text = extractCodeBlock(stripThinkTag(text));
                }
                if (print) {
                    client.getConfig().printMarkdown(text);
                }
            }
            return new Tuple<>(text, evalToolCalls(client.getConfig(), toolCalls));
        }
        return null;
    }

    // pub async fn call_chat_completions_streaming(
    //    input: &Input,
    //    client: &dyn Client,
    //    abort_signal: AbortSignal,
    //) -> Result<(String, Vec<ToolResult>)> {
    //    let (tx, rx) = unbounded_channel();
    //    let mut handler = SseHandler::new(tx, abort_signal.clone());
    //
    //    let (send_ret, render_ret) = tokio::join!(
    //        client.chat_completions_streaming(input, &mut handler),
    //        render_stream(rx, client.global_config(), abort_signal.clone()),
    //    );
    //
    //    if handler.abort().aborted() {
    //        bail!("Aborted.");
    //    }
    //
    //    render_ret?;
    //
    //    let (text, tool_calls) = handler.take();
    //    match send_ret {
    //        Ok(_) => {
    //            if !text.is_empty() && !text.ends_with('\n') {
    //                println!();
    //            }
    //            Ok((text, eval_tool_calls(client.global_config(), tool_calls)?))
    //        }
    //        Err(err) => {
    //            if !text.is_empty() {
    //                println!();
    //            }
    //            Err(err)
    //        }
    //    }
    // }
    public static Tuple<String, List<ToolResult>> callChatCompletionsStreaming(Input input, Client client, AbortSignal abortSignal) {
        UnboundedSender<SseEvent> tx = new UnboundedSender<>();
        UnboundedReceiver<SseEvent> rx = new UnboundedReceiver<>();
        SseHandler handler = new SseHandler(tx, abortSignal);

        TupleN tupleN = join(
            supplyAsync(() -> client.chatCompletionsStreaming(input, handler)),
            supplyAsync(() -> renderStream(rx, client.getConfig(), abortSignal)));
        Result<?> sendRet = tupleN.next();
        Result<?> renderRet = tupleN.next();

        if (handler.abort().aborted()) {
            bail("Aborted.");
        }

        Tuple<String, List<ToolCall>> tuple = handler.take();
        String text = tuple.first();
        List<ToolCall> toolCalls = tuple.second();
        return switch (getType(sendRet)) {
            case Ok -> {
                if (!isBlank(text) && !text.endsWith("\n")) {
                    println();
                }
                yield new Tuple<>(text, evalToolCalls(client.getConfig(), toolCalls));
            }
            case Err -> {
                if (!isBlank(text)) {
                    println();
                }
                yield null;
            }
        };
    }
}
