package com.tsoft.jai.client.openai;

import com.tsoft.jai.client.common.ChatCompletionsData;
import com.tsoft.jai.client.common.ChatCompletionsOutput;
import com.tsoft.jai.client.common.FunctionDeclaration;
import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.client.message.MessageRole;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.Response;
import com.tsoft.jai.reqwest.StatusCode;
import com.tsoft.jai.serdejson.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.utils.Mod.stripThinkTag;

@Slf4j
public class OpenAIClient {

    // pub async fn openai_chat_completions(
    //    builder: RequestBuilder,
    //    _model: &Model,
    // ) -> Result<ChatCompletionsOutput> {
    //    let res = builder.send().await?;
    //    let status = res.status();
    //    let data: Value = res.json().await?;
    //    if !status.is_success() {
    //        catch_error(&data, status.as_u16())?;
    //    }
    //
    //    debug!("non-stream-data: {data}");
    //    openai_extract_chat_completions(&data)
    // }
    public ChatCompletionsOutput openaiChatCompletions(RequestBuilder builder, Model model) {
        Response res = builder.send();
        StatusCode status = res.getStatus();
        Value data = res.getJson();
        if (!status.isSuccess()) {
            log.error("Invalid response, status = {}, data: {}", status, data);
            throw new IllegalStateException("Error");
        }

        log.debug("non-stream-data: {}", data);
        return openaiExtractChatCompletions(data);
    }

    // pub fn openai_build_chat_completions_body(data: ChatCompletionsData, model: &Model) -> Value {
    //    let ChatCompletionsData {
    //        messages,
    //        temperature,
    //        top_p,
    //        functions,
    //        stream,
    //    } = data;
    //
    //    let messages_len = messages.len();
    //    let messages: Vec<Value> = messages
    //        .into_iter()
    //        .enumerate()
    //        .flat_map(|(i, message)| {
    //            let Message { role, content } = message;
    //            match content {
    //                MessageContent::ToolCalls(MessageContentToolCalls {
    //                    tool_results,
    //                    text: _,
    //                    sequence,
    //                }) => {
    //                    if !sequence {
    //                        let tool_calls: Vec<_> = tool_results
    //                            .iter()
    //                            .map(|tool_result| {
    //                                json!({
    //                                    "id": tool_result.call.id,
    //                                    "type": "function",
    //                                    "function": {
    //                                        "name": tool_result.call.name,
    //                                        "arguments": tool_result.call.arguments.to_string(),
    //                                    },
    //                                })
    //                            })
    //                            .collect();
    //                        let mut messages = vec![
    //                            json!({ "role": MessageRole::Assistant, "tool_calls": tool_calls }),
    //                        ];
    //                        for tool_result in tool_results {
    //                            messages.push(json!({
    //                                "role": "tool",
    //                                "content": tool_result.output.to_string(),
    //                                "tool_call_id": tool_result.call.id,
    //                            }));
    //                        }
    //                        messages
    //                    } else {
    //                        tool_results.into_iter().flat_map(|tool_result| {
    //                            vec![
    //                                json!({
    //                                    "role": MessageRole::Assistant,
    //                                    "tool_calls": [
    //                                        {
    //                                            "id": tool_result.call.id,
    //                                            "type": "function",
    //                                            "function": {
    //                                                "name": tool_result.call.name,
    //                                                "arguments": tool_result.call.arguments.to_string(),
    //                                            },
    //                                        }
    //                                    ]
    //                                }),
    //                                json!({
    //                                    "role": "tool",
    //                                    "content": tool_result.output.to_string(),
    //                                    "tool_call_id": tool_result.call.id,
    //                                })
    //                            ]
    //
    //                        }).collect()
    //                    }
    //                }
    //                MessageContent::Text(text) if role.is_assistant() && i != messages_len - 1 => {
    //                    vec![json!({ "role": role, "content": strip_think_tag(&text) }
    //                    )]
    //                }
    //                _ => vec![json!({ "role": role, "content": content })],
    //            }
    //        })
    //        .collect();
    //
    //    let mut body = json!({
    //        "model": &model.real_name(),
    //        "messages": messages,
    //    });
    //
    //    if let Some(v) = model.max_tokens_param() {
    //        if model
    //            .patch()
    //            .and_then(|v| v.get("body").and_then(|v| v.get("max_tokens")))
    //            == Some(&Value::Null)
    //        {
    //            body["max_completion_tokens"] = v.into();
    //        } else {
    //            body["max_tokens"] = v.into();
    //        }
    //    }
    //    if let Some(v) = temperature {
    //        body["temperature"] = v.into();
    //    }
    //    if let Some(v) = top_p {
    //        body["top_p"] = v.into();
    //    }
    //    if stream {
    //        body["stream"] = true.into();
    //    }
    //    if let Some(functions) = functions {
    //        body["tools"] = functions
    //            .iter()
    //            .map(|v| {
    //                json!({
    //                    "type": "function",
    //                    "function": v,
    //                })
    //            })
    //            .collect();
    //    }
    //    body
    // }
    public static Map<String, Object> openaiBuildChatCompletionsBody(ChatCompletionsData data, Model model) {
        List<Map<String, Object>> bodyMessages = new ArrayList<>();

        List<Message> messages = data.getMessages();
        if (messages != null && !messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i ++) {
                Message message = messages.get(i);
                if (message == null) {
                    continue;
                }

                MessageRole role = message.getRole();
                MessageContent content = message.getContent();
                if (role == null || content == null) {
                    continue;
                }

                MessageContentToolCalls toolCalls = content.getToolCalls();
                if (toolCalls != null) {
                    if (!toolCalls.isSequence()) {
                        List<ToolResult> toolResults = toolCalls.getToolResults();

                        if (toolResults != null) {
                            List<Map<String, Object>> bodyToolCalls = new ArrayList<>();

                            for (ToolResult toolResult : toolResults) {
                                bodyToolCalls.add(jsonItem(
                                    "id", toolResult.getCall().getId(),
                                    "type", "function",
                                    "function", jsonItem(
                                        "name", toolResult.getCall().getName(),
                                        "arguments", toolResult.getCall().getArguments()
                                    )));
                            }

                            bodyMessages.add(jsonItem(
                                "role", MessageRole.Assistant,
                                "tool_calls", bodyToolCalls
                            ));

                            for (ToolResult toolResult : toolResults) {
                                bodyMessages.add(jsonItem(
                                    "role", "tool",
                                    "content", toolResult.getOutput(),
                                    "tool_call_id", toolResult.getCall().getId()
                                ));
                            }
                        }
                    } else {
                        List<ToolResult> toolResults = toolCalls.getToolResults();

                        if (toolResults != null) {
                            for (ToolResult toolResult : toolResults) {
                                bodyMessages.add(jsonItem(
                                    "role", MessageRole.Assistant,
                                    "tool_calls", List.of(jsonItem(
                                        "id", toolResult.getCall().getId(),
                                        "type", "function",
                                        "function", jsonItem(
                                            "name", toolResult.getCall().getName(),
                                            "arguments", toolResult.getCall().getArguments()
                                        )))));

                                bodyMessages.add(jsonItem(
                                    "role", "tool",
                                    "content", toolResult.getOutput(),
                                    "tool_call_id", toolResult.getCall().getId()
                                ));
                            }
                        }
                    }

                    continue;
                }

                if (content.getText() != null) {
                    if (MessageRole.Assistant.equals(role) && i != messages.size() - 1) {
                        bodyMessages.add(jsonItem(
                            "role", role,
                            "content", stripThinkTag(content.getText())
                        ));
                    } else {
                        bodyMessages.add(jsonItem(
                            "role", role,
                            "content", content.getText()
                        ));
                    }
                }
            }
        }

        Map<String, Object> body = jsonItem(
            "model", model.getRealName(),
            "messages", bodyMessages
        );

        Integer maxTokensParam = model.getMaxTokensParam();
        if (maxTokensParam != null) {
            Object maxTokens = null;
            Map<String, Object> patch = model.getPatch();
            if (patch != null) {
                Object bodyPatch = patch.get("body");
                if (bodyPatch instanceof Map<?, ?> map) {
                    maxTokens = map.get("max_tokens");
                }
            }

            if (maxTokens == null) {
                body.put("max_completion_tokens", maxTokensParam);
            } else {
                body.put("max_tokens", maxTokensParam);
            }
        }

        Double temperature = data.getTemperature();
        if (temperature != null) {
            body.put("temperature", temperature);
        }

        Double topP = data.getTopP();
        if (topP != null) {
            body.put("top_p", topP);
        }

        if (data.isStream()) {
            body.put("stream", Boolean.TRUE);
        }

        List<FunctionDeclaration> functions = data.getFunctions();
        if (functions != null && !functions.isEmpty()) {
            body.put("tools", functions.stream()
                .map(e -> jsonItem(
                    "type", "function",
                    "function", e))
                .toList());
        }

        return body;
    }

    // pub fn openai_extract_chat_completions(data: &Value) -> Result<ChatCompletionsOutput> {
    //    let text = data["choices"][0]["message"]["content"]
    //        .as_str()
    //        .unwrap_or_default();
    //
    //    let reasoning = data["choices"][0]["message"]["reasoning_content"]
    //        .as_str()
    //        .or_else(|| data["choices"][0]["message"]["reasoning"].as_str())
    //        .unwrap_or_default()
    //        .trim();
    //
    //    let mut tool_calls = vec![];
    //    if let Some(calls) = data["choices"][0]["message"]["tool_calls"].as_array() {
    //        for call in calls {
    //            if let (Some(name), Some(arguments), Some(id)) = (
    //                call["function"]["name"].as_str(),
    //                call["function"]["arguments"].as_str(),
    //                call["id"].as_str(),
    //            ) {
    //                let arguments: Value = arguments.parse().with_context(|| {
    //                    format!("Tool call '{name}' have non-JSON arguments '{arguments}'")
    //                })?;
    //                tool_calls.push(ToolCall::new(
    //                    name.to_string(),
    //                    arguments,
    //                    Some(id.to_string()),
    //                ));
    //            }
    //        }
    //    };
    //
    //    if text.is_empty() && tool_calls.is_empty() {
    //        bail!("Invalid response data: {data}");
    //    }
    //    let text = if !reasoning.is_empty() {
    //        format!("<think>\n{reasoning}\n</think>\n\n{text}")
    //    } else {
    //        text.to_string()
    //    };
    //    let output = ChatCompletionsOutput {
    //        text,
    //        tool_calls,
    //        id: data["id"].as_str().map(|v| v.to_string()),
    //        input_tokens: data["usage"]["prompt_tokens"].as_u64(),
    //        output_tokens: data["usage"]["completion_tokens"].as_u64(),
    //    };
    //    Ok(output)
    // }
    public ChatCompletionsOutput openaiExtractChatCompletions(Value data) {
        String text = data.get("choices", 0, "message", "content").asStr();

        String reasoning = data.get("choices", 0, "message", "reasoning_content").asStr();
        if (reasoning == null) {
            reasoning = data.get("choices", 0, "message", "reasoning").asStr();
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        List<Value> calls = data.get("choices", 0, "message", "tool_calls").asList();
        if (calls != null) {
            for (Value call : calls) {
                String name = call.get("function", "name").asStr();
                String arguments = call.get("function", "arguments").asStr();
                String id = call.get("id").asStr();
                if (name != null && arguments != null && id != null) {
                    toolCalls.add(new ToolCall()
                        .setName(name)
                        .setArguments(arguments)
                        .setId(id));
                }
            }
        }

        if ((text == null || text.isEmpty()) && toolCalls.isEmpty()) {
            throw new IllegalStateException("Invalid response data: " + data);
        }

        if (reasoning != null && !reasoning.isBlank()) {
            text = "<think>\n%s\n</think>\n\n%s".formatted(reasoning.trim(), text);
        }

        return new ChatCompletionsOutput()
            .setText(text)
            .setToolCalls(toolCalls)
            .setId(data.get("id").asStr())
            .setInputTokens(data.get("usage", "prompt_tokens").asInt())
            .setOutputTokens(data.get("usage", "completion_tokens").asInt());
    }

    private static Map<String, Object> jsonItem(Object ... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String)kv[i], kv[i + 1]);
        }
        return map;
    }
}
