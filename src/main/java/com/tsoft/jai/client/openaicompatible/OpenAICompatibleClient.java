package com.tsoft.jai.client.openaicompatible;

import com.tsoft.jai.client.common.ChatCompletionsData;
import com.tsoft.jai.client.common.RequestData;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.ClientConfig;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.tsoft.jai.client.openai.OpenAIClient.openaiBuildChatCompletionsBody;
import static com.tsoft.jai.utils.StringUtils.isBlank;

@RequiredArgsConstructor
public class OpenAICompatibleClient {

    private static final Map<String, String> OPENAI_COMPATIBLE_PROVIDERS = new HashMap<>() {{
        put("ai21", "https://api.ai21.com/studio/v1");
        put("cloudflare", "https://api.cloudflare.com/client/v4/accounts/{ACCOUNT_ID}/ai/v1");
        put("deepinfra", "https://api.deepinfra.com/v1/openai");
        put("deepseek", "https://api.deepseek.com");
        put("ernie", "https://qianfan.baidubce.com/v2");
        put("github", "https://models.inference.ai.azure.com");
        put("groq", "https://api.groq.com/openai/v1");
        put("hunyuan", "https://api.hunyuan.cloud.tencent.com/v1");
        put("minimax", "https://api.minimax.chat/v1");
        put("mistral", "https://api.mistral.ai/v1");
        put("moonshot", "https://api.moonshot.cn/v1");
        put("openrouter", "https://openrouter.ai/api/v1");
        put("perplexity", "https://api.perplexity.ai");
        put("qianwen", "https://dashscope.aliyuncs.com/compatible-mode/v1");
        put("xai", "https://api.x.ai/v1");
        put("zhipuai", "https://open.bigmodel.cn/api/paas/v4");

        // RAG-dedicate
        put("jina", "https://api.jina.ai/v1");
        put("voyageai", "https://api.voyageai.com/v1");
    }};

    private final ClientConfig config;
    private final Model model;

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
    public RequestData prepareChatCompletions(ChatCompletionsData data) {
        String apiKey = config.getApiKey();
        String apiBase = getApiBaseExt();

        String url = "%s/chat/completions".formatted(apiBase);

        Map<String, Object> body = openaiBuildChatCompletionsBody(data, model);

        RequestData requestData = new RequestData(url, body);

        if (!isBlank(apiKey)) {
            requestData.bearerAuth(apiKey);
        }

        return requestData;
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
        String apiBase = config.getApiBase();
        if (isBlank(apiBase)) {
            apiBase = OPENAI_COMPATIBLE_PROVIDERS.get("todo");
        }
        return apiBase;
    }
}
