package com.tsoft.jai.client.openai;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.common.ChatCompletionsData;
import com.tsoft.jai.client.common.ChatCompletionsOutput;
import com.tsoft.jai.client.common.RequestData;
import com.tsoft.jai.client.stream.StreamHandler;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.function.FunctionDeclaration;
import com.tsoft.jai.client.message.Message;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.client.message.MessageRole;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.client.stream.SseMessage;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.Response;
import com.tsoft.jai.reqwest.StatusCode;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.base.Triple;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.client.common.Common.catchError;
import static com.tsoft.jai.client.stream.Stream.sseStream;
import static com.tsoft.jai.serdejson.SerDe.json;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.base.StringUtils.format;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Slf4j
@Getter
public class OpenAIClient extends Client {

    private final String name = "openai";
    private final ClientConfig clientConfig;
    private final Config config;
    private final Model model;

    public OpenAIClient(Triple<ClientConfig, Config, Model> triple) {
        this.clientConfig = triple.first();
        this.config = triple.second();
        this.model = triple.third();
    }

    // fn prepare_chat_completions(
    //    self_: &OpenAIClient,
    //    data: ChatCompletionsData,
    // ) -> Result<RequestData> {
    //    let api_key = self_.get_api_key()?;
    //    let api_base = self_
    //        .get_api_base()
    //        .unwrap_or_else(|_| API_BASE.to_string());
    //
    //    let url = format!("{}/chat/completions", api_base.trim_end_matches('/'));
    //
    //    let body = openai_build_chat_completions_body(data, &self_.model);
    //
    //    let mut request_data = RequestData::new(url, body);
    //
    //    request_data.bearer_auth(api_key);
    //    if let Some(organization_id) = &self_.config.organization_id {
    //        request_data.header("OpenAI-Organization", organization_id);
    //    }
    //
    //    Ok(request_data)
    // }
    @Override
    public Result<RequestData> prepareChatCompletions(ChatCompletionsData data) {
        String apiKey = clientConfig.getApiKey();
        return null;
    }

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
    public Result<ChatCompletionsOutput> chatCompletions(RequestBuilder builder, Model model) {
        Result<Response> res = builder.send();
        if (isErr(res)) {
            return Err(res);
        }
        StatusCode status = res.getValue().getStatus();
        Result<Value> resData = res.getValue().getJson();
        if (isErr(resData)) {
            return Err(resData);
        }
        Value data = resData.getValue();
        if (!status.isSuccess()) {
            return catchError(data, status.asInt());
        }

        log.debug("non-stream-data: {}", data);
        return extractChatCompletions(data);
    }

    // pub async fn openai_chat_completions_streaming(
    //    builder: RequestBuilder,
    //    handler: &mut SseHandler,
    //    _model: &Model,
    //) -> Result<()> {
    //    let mut call_id = String::new();
    //    let mut function_name = String::new();
    //    let mut function_arguments = String::new();
    //    let mut function_id = String::new();
    //    let mut reasoning_state = 0;
    //    let handle = |message: SseMmessage| -> Result<bool> {
    //        if message.data == "[DONE]" {
    //            if !function_name.is_empty() {
    //                if function_arguments.is_empty() {
    //                    function_arguments = String::from("{}");
    //                }
    //                let arguments: Value = function_arguments.parse().with_context(|| {
    //                    format!("Tool call '{function_name}' have non-JSON arguments '{function_arguments}'")
    //                })?;
    //                handler.tool_call(ToolCall::new(
    //                    function_name.clone(),
    //                    arguments,
    //                    normalize_function_id(&function_id),
    //                ))?;
    //            }
    //            return Ok(true);
    //        }
    //        let data: Value = serde_json::from_str(&message.data)?;
    //        debug!("stream-data: {data}");
    //        if let Some(text) = data["choices"][0]["delta"]["content"]
    //            .as_str()
    //            .filter(|v| !v.is_empty())
    //        {
    //            if reasoning_state == 1 {
    //                handler.text("\n</think>\n\n")?;
    //                reasoning_state = 0;
    //            }
    //            handler.text(text)?;
    //        } else if let Some(text) = data["choices"][0]["delta"]["reasoning_content"]
    //            .as_str()
    //            .or_else(|| data["choices"][0]["delta"]["reasoning"].as_str())
    //            .filter(|v| !v.is_empty())
    //        {
    //            if reasoning_state == 0 {
    //                handler.text("<think>\n")?;
    //                reasoning_state = 1;
    //            }
    //            handler.text(text)?;
    //        }
    //        if let (Some(function), index, id) = (
    //            data["choices"][0]["delta"]["tool_calls"][0]["function"].as_object(),
    //            data["choices"][0]["delta"]["tool_calls"][0]["index"].as_u64(),
    //            data["choices"][0]["delta"]["tool_calls"][0]["id"]
    //                .as_str()
    //                .filter(|v| !v.is_empty()),
    //        ) {
    //            if reasoning_state == 1 {
    //                handler.text("\n</think>\n\n")?;
    //                reasoning_state = 0;
    //            }
    //            let maybe_call_id = format!("{}/{}", id.unwrap_or_default(), index.unwrap_or_default());
    //            if maybe_call_id != call_id && maybe_call_id.len() >= call_id.len() {
    //                if !function_name.is_empty() {
    //                    if function_arguments.is_empty() {
    //                        function_arguments = String::from("{}");
    //                    }
    //                    let arguments: Value = function_arguments.parse().with_context(|| {
    //                        format!("Tool call '{function_name}' have non-JSON arguments '{function_arguments}'")
    //                    })?;
    //                    handler.tool_call(ToolCall::new(
    //                        function_name.clone(),
    //                        arguments,
    //                        normalize_function_id(&function_id),
    //                    ))?;
    //                }
    //                function_name.clear();
    //                function_arguments.clear();
    //                function_id.clear();
    //                call_id = maybe_call_id;
    //            }
    //            if let Some(name) = function.get("name").and_then(|v| v.as_str()) {
    //                if name.starts_with(&function_name) {
    //                    function_name = name.to_string();
    //                } else {
    //                    function_name.push_str(name);
    //                }
    //            }
    //            if let Some(arguments) = function.get("arguments").and_then(|v| v.as_str()) {
    //                function_arguments.push_str(arguments);
    //            }
    //            if let Some(id) = id {
    //                function_id = id.to_string();
    //            }
    //        }
    //        Ok(false)
    //    };
    //
    //    sse_stream(builder, handle).await
    // }
    @Override
    public Result<?> chatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model) {
        return openaiChatCompletionsStreaming(builder, handler, model);
    }

    public static Result<?> openaiChatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model) {
        class Handler implements StreamHandler {
            private volatile String callId;
            private volatile String functionName;
            private volatile String functionArguments;
            private volatile String functionId;
            private volatile Integer reasoningState = 0;

            public Result<Boolean> handle(SseMessage message) {
                if ("[DONE]".equals(message.getData())) {
                    if (functionArguments != null) {
                        if (functionArguments == null) {
                            functionArguments = "{}";
                        }
                        Result<Value> arguments = SerDe.parseJson(functionArguments).withContext(() ->
                            format("Tool call '{}' have non-JSON arguments '{}'", functionName, functionArguments));
                        if (isErr(arguments)) {
                            return Err(arguments);
                        }
                        handler.toolCall(new ToolCall()
                            .setName(functionName)
                            .setArguments(arguments.getValue())
                            .setId(normalizeFunctionId(functionId)));
                    }
                    return Ok(true);
                }

                Result<Value> res = SerDe.parseJson(message.getData());
                if (isErr(res)) {
                    return Err(res);
                }
                Value data = res.getValue();
                log.debug("stream-data: {}", data);
                if (data == null) {
                    return Ok(false);
                }

                String text = data.get("choices", 0, "delta", "content").asStr();
                if (!isBlank(text)) {
                    if (reasoningState == 1) {
                        handler.text("\n</think>\n\n");
                        reasoningState = 0;
                    }
                    handler.text(text);
                } else {
                    text = data.get("choices", 0, "delta", "reasoning_content").asStr();
                    if (text == null) {
                        text = data.get("choices", 0, "delta", "reasoning").asStr();
                    }
                    if (!isBlank(text)) {
                        if (reasoningState == 0) {
                            handler.text("<think>\n");
                            reasoningState = 1;
                        }
                        handler.text(text);
                    }
                }

                Value function = data.get("choices", 0, "delta", "tool_calls", 0, "function");
                Integer index = data.get("choices", 0, "delta", "tool_calls", 0, "index").asInt();
                String id = data.get("choices", 0, "delta", "tool_calls", 0, "id").asStr();
                if (function != null && !isBlank(id)) {
                    if (reasoningState == 1) {
                        handler.text("\n</think>\n\n");
                        reasoningState = 0;
                    }
                    String maybeCallId = "%s/%s".formatted(id, index);
                    if (!maybeCallId.equals(callId) && maybeCallId.length() >= callId.length()) {
                        if (functionName != null) {
                            if (functionArguments == null) {
                                functionArguments = "{}";
                            }
                            Result<Value> arguments = SerDe.parseJson(functionArguments).withContext(() ->
                                format("Tool call '{}' have non-JSON arguments '{function_arguments}'", functionName, functionArguments));
                            if (isErr(arguments)) {
                                return Err(arguments);
                            }
                            handler.toolCall(new ToolCall()
                                .setName(functionName)
                                .setArguments(arguments.getValue())
                                .setId(normalizeFunctionId(functionId)));
                        }
                        functionName = null;
                        functionArguments = null;
                        functionId = null;
                        callId = maybeCallId;
                    }

                    String name = function.get("name").asStr();
                    if (name != null) {
                        if (name.startsWith(functionName)) {
                            functionName = name;
                        } else {
                            String buf = (functionName == null) ? name : functionName + name;
                            functionName = buf;
                        }
                    }

                    String arguments = function.get("arguments").asStr();
                    if (arguments != null) {
                        String buf = (functionArguments == null) ? arguments : functionArguments + arguments;
                        functionArguments = buf;
                    }

                    if (id != null) {
                        functionId = id;
                    }
                }
                return Ok(false);
            };
        }

        return sseStream(builder, new Handler());
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
    public static Value buildChatCompletionsBody(ChatCompletionsData data, Model model) {
        List<Value> bodyMessages = new ArrayList<>();

        List<Message> messages = data.getMessages();
        if (!isEmpty(messages)) {
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

                        if (!isEmpty(toolResults)) {
                            List<Value> bodyToolCalls = new ArrayList<>();

                            for (ToolResult toolResult : toolResults) {
                                bodyToolCalls.add(json(
                                    "id", toolResult.getCall().getId(),
                                    "type", "function",
                                    "function", json(
                                        "name", toolResult.getCall().getName(),
                                        "arguments", toolResult.getCall().getArguments()
                                    )));
                            }

                            bodyMessages.add(json(
                                "role", MessageRole.Assistant,
                                "tool_calls", bodyToolCalls
                            ));

                            for (ToolResult toolResult : toolResults) {
                                bodyMessages.add(json(
                                    "role", "tool",
                                    "content", toolResult.getOutput(),
                                    "tool_call_id", toolResult.getCall().getId()
                                ));
                            }
                        }
                    } else {
                        List<ToolResult> toolResults = toolCalls.getToolResults();

                        if (!isEmpty(toolResults)) {
                            for (ToolResult toolResult : toolResults) {
                                bodyMessages.add(json(
                                    "role", MessageRole.Assistant,
                                    "tool_calls", List.of(json(
                                        "id", toolResult.getCall().getId(),
                                        "type", "function",
                                        "function", json(
                                            "name", toolResult.getCall().getName(),
                                            "arguments", toolResult.getCall().getArguments()
                                        )))));

                                bodyMessages.add(json(
                                    "role", "tool",
                                    "content", toolResult.getOutput(),
                                    "tool_call_id", toolResult.getCall().getId()
                                ));
                            }
                        }
                    }

                    continue;
                }

                if (!isBlank(content.getText())) {
                    if (MessageRole.Assistant.equals(role) && i != messages.size() - 1) {
                        bodyMessages.add(json(
                            "role", role,
                            "content", stripThinkTag(content.getText())
                        ));
                    } else {
                        bodyMessages.add(json(
                            "role", role,
                            "content", content.getText()
                        ));
                    }
                }
            }
        }

        Value body = json(
            "model", model.getRealName(),
            "messages", bodyMessages
        );

        Integer maxTokensParam = model.getMaxTokensParam();
        if (maxTokensParam != null) {
            Object maxTokens = null;
            Value patch = model.getPatch();
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
        if (!isEmpty(functions)) {
            body.put("tools", functions.stream()
                .map(e -> json(
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
    public Result<ChatCompletionsOutput> extractChatCompletions(Value data) {
        String text = data.get("choices", 0, "message", "content").asStr();

        String reasoning = data.get("choices", 0, "message", "reasoning_content").asStr();
        if (reasoning == null) {
            reasoning = data.get("choices", 0, "message", "reasoning").asStr();
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        List<Value> calls = data.get("choices", 0, "message", "tool_calls").asList();
        if (!isEmpty(calls)) {
            for (Value call : calls) {
                String name = call.get("function", "name").asStr();
                String arguments = call.get("function", "arguments").asStr();
                Result<Value> res = SerDe.parseJson(arguments).withContext(() ->
                    format("Tool call '{}' have non-JSON arguments '{}'", name, arguments));
                if (isErr(res)) {
                    return Err(res);
                }
                String id = call.get("id").asStr();
                if (name != null && arguments != null && id != null) {
                    toolCalls.add(new ToolCall()
                        .setName(name)
                        .setArguments(res.getValue())
                        .setId(id));
                }
            }
        }

        if ((text == null || text.isEmpty()) && toolCalls.isEmpty()) {
            return bail("Invalid response data: {}", data);
        }

        if (!isBlank(reasoning)) {
            text = format("<think>\n{}\n</think>\n\n{}", reasoning.trim(), text);
        }

        ChatCompletionsOutput output = new ChatCompletionsOutput()
            .setText(text)
            .setToolCalls(toolCalls)
            .setId(data.get("id").asStr())
            .setInputTokens(data.get("usage", "prompt_tokens").asInt())
            .setOutputTokens(data.get("usage", "completion_tokens").asInt());
        return Ok(output);
    }

    // fn normalize_function_id(value: &str) -> Option<String> {
    //    if value.is_empty() {
    //        None
    //    } else {
    //        Some(value.to_string())
    //    }
    // }
    public static String normalizeFunctionId(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value;
    }
}
