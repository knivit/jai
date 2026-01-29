package com.tsoft.jai.client.openaicompatible;

import com.tsoft.jai.anyhow.Result;
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

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.client.mod.Mod.OPENAI_COMPATIBLE_PROVIDERS;
import static com.tsoft.jai.client.openai.OpenAIClient.buildChatCompletionsBody;
import static com.tsoft.jai.client.openai.OpenAIClient.openaiChatCompletionsStreaming;
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
    public Result<RequestData> prepareChatCompletions(ChatCompletionsData data) {
        String apiKey = clientConfig.getApiKey();
        String apiBase = getApiBaseExt();

        String url = format("{}/chat/completions", apiBase);

        Value body = buildChatCompletionsBody(data, model);

        RequestData requestData = new RequestData(url, body);

        if (!isBlank(apiKey)) {
            requestData.bearerAuth(apiKey);
        }

        return Ok(requestData);
    }

    @Override
    public Result<?> chatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model) {
        return openaiChatCompletionsStreaming(builder, handler, model);
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
