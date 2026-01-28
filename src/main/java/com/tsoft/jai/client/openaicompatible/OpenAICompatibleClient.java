package com.tsoft.jai.client.openaicompatible;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.common.ChatCompletionsData;
import com.tsoft.jai.client.common.RequestData;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.base.Triple;
import lombok.Getter;

import static com.tsoft.jai.client.mod.Mod.OPENAI_COMPATIBLE_PROVIDERS;
import static com.tsoft.jai.client.openai.OpenAIClient.buildChatCompletionsBody;
import static com.tsoft.jai.utils.base.StringUtils.format;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Getter
public class OpenAICompatibleClient extends Client {

    private final String name = "openai-compatible";
    private final ClientConfig clientConfig;
    private final Config config;
    private final Model model;

    public OpenAICompatibleClient(Triple<ClientConfig, Config, Model> triple) {
        this.clientConfig = triple.first();
        this.config = triple.second();
        this.model = triple.third();
    }

    // fn prepare_chat_completions(
    //     self_: &OpenAICompatibleClient,
    //     data: ChatCompletionsData,
    // ) -> Result<RequestData> {
    //     let api_key = self_.get_api_key().ok();
    //     let api_base = get_api_base_ext(self_)?;
    //
    //     let url = format!("{api_base}/chat/completions");
    //
    //     let body = openai_build_chat_completions_body(data, &self_.model);
    //
    //     let mut request_data = RequestData::new(url, body);
    //
    //     if let Some(api_key) = api_key {
    //         request_data.bearer_auth(api_key);
    //     }
    //
    //     Ok(request_data)
    // }
    //
    @Override
    public RequestData prepareChatCompletions(ChatCompletionsData data) {
        String apiKey = clientConfig.getApiKey();
        String apiBase = getApiBaseExt();

        String url = format("{}/chat/completions", apiBase);

        Value body = buildChatCompletionsBody(data, model);

        RequestData requestData = new RequestData(url, body);

        if (!isBlank(apiKey)) {
            requestData.bearerAuth(apiKey);
        }

        return requestData;
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
    public void chatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model) {
        identical to OpenAiClient.chatCompletionsStreaming ?
    }

    // fn get_api_base_ext(self_: &OpenAICompatibleClient) -> Result<String> {
    //    let api_base = match self_.get_api_base() {
    //        Ok(v) => v,
    //        Err(err) => {
    //            match OPENAI_COMPATIBLE_PROVIDERS
    //                .into_iter()
    //                .find_map(|(name, api_base)| {
    //                    if name == self_.model.client_name() {
    //                        Some(api_base.to_string())
    //                    } else {
    //                        None
    //                    }
    //                }) {
    //                Some(v) => v,
    //                None => return Err(err),
    //            }
    //        }
    //    };
    //    Ok(api_base.trim_end_matches('/').to_string())
    // }
    private String getApiBaseExt() {
        String apiBase = clientConfig.getApiBase();
        if (isBlank(apiBase)) {
            apiBase = OPENAI_COMPATIBLE_PROVIDERS.get("todo");
        }
        return apiBase;
    }
}
