package com.tsoft.jai.client.common;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import com.tsoft.jai.utils.base.TupleN;

import java.util.List;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.function.Functions.evalToolCalls;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.inquire.prompt.Spinner.abortableRunWithSpinner;
import static com.tsoft.jai.render.Mod.renderStream;
import static com.tsoft.jai.tokio.Join.join;
import static com.tsoft.jai.utils.Mod.extractCodeBlock;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;
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
    public static Result<Tuple<String, List<ToolResult>>> callChatCompletions(Input input, boolean print, boolean exractCode, Client client, AbortSignal abortSignal) {
        Result<ChatCompletionsOutput> ret = abortableRunWithSpinner(() -> client.chatCompletions(input), "Generating", abortSignal);
        return switch (ret.getType()) {
            case Ok -> {
                String text = ret.getValue().getText();
                List<ToolCall> toolCalls = ret.getValue().getToolCalls();
                if (!isBlank(text)) {
                    if (exractCode) {
                        text = extractCodeBlock(stripThinkTag(text));
                    }
                    if (print) {
                        client.getConfig().printMarkdown(text);
                    }
                }
                yield Ok(new Tuple<>(text, evalToolCalls(client.getConfig(), toolCalls)));
            }
            case Err -> Err(ret);
        };
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
    public static Result<Tuple<String, List<ToolResult>>> callChatCompletionsStreaming(Input input, Client client, AbortSignal abortSignal) {
        UnboundedSender<SseEvent> tx = new UnboundedSender<>();
        UnboundedReceiver<SseEvent> rx = new UnboundedReceiver<>();
        SseHandler handler = new SseHandler(tx, abortSignal);

        TupleN tupleN = join(
            supplyAsync(() -> client.chatCompletionsStreaming(input, handler)),
            supplyAsync(() -> renderStream(rx, client.getConfig(), abortSignal)));
        Result<?> sendRet = tupleN.next();
        Result<?> renderRet = tupleN.next();

        if (handler.abort().aborted()) {
            return bail("Aborted.");
        }

        if (isErr(renderRet)) {
            return Err(renderRet);
        }

        Tuple<String, List<ToolCall>> tuple = handler.take();
        String text = tuple.first();
        List<ToolCall> toolCalls = tuple.second();
        return switch (getType(sendRet)) {
            case Ok -> {
                if (!isBlank(text) && !text.endsWith("\n")) {
                    println();
                }
                yield Ok(new Tuple<>(text, evalToolCalls(client.getConfig(), toolCalls)));
            }
            case Err -> {
                if (!isBlank(text)) {
                    println();
                }
                yield Err(sendRet);
            }
        };
    }

    // pub fn catch_error(data: &Value, status: u16) -> Result<()> {
    //    if (200..300).contains(&status) {
    //        return Ok(());
    //    }
    //    debug!("Invalid response, status: {status}, data: {data}");
    //    if let Some(error) = data["error"].as_object() {
    //        if let (Some(typ), Some(message)) = (
    //            json_str_from_map(error, "type"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (type: {typ})");
    //        } else if let (Some(typ), Some(message)) = (
    //            json_str_from_map(error, "code"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (code: {typ})");
    //        }
    //    } else if let Some(error) = data["errors"][0].as_object() {
    //        if let (Some(code), Some(message)) = (
    //            error.get("code").and_then(|v| v.as_u64()),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (status: {code})")
    //        }
    //    } else if let Some(error) = data[0]["error"].as_object() {
    //        if let (Some(status), Some(message)) = (
    //            json_str_from_map(error, "status"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (status: {status})")
    //        }
    //    } else if let (Some(detail), Some(status)) = (data["detail"].as_str(), data["status"].as_i64())
    //    {
    //        bail!("{detail} (status: {status})");
    //    } else if let Some(error) = data["error"].as_str() {
    //        bail!("{error}");
    //    } else if let Some(message) = data["message"].as_str() {
    //        bail!("{message}");
    //    }
    //    bail!("Invalid response data: {data} (status: {status})");
    // }
    public static <T> Result<T> catchError(Value data, int status) {
        return bail("Invalid response data: {} (status: {})", data, status);
    }
}
