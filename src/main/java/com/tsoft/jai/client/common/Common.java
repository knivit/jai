package com.tsoft.jai.client.common;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.Tuple;

import java.util.List;

import static com.tsoft.jai.config.Input.abortableRunWithSpinner;
import static com.tsoft.jai.function.Functions.evalToolCalls;
import static com.tsoft.jai.utils.Mod.extractCodeBlock;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.StringUtils.isBlank;

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
}
