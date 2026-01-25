package com.tsoft.jai.client.mod;

import com.tsoft.jai.client.openai.OpenAIClient;
import com.tsoft.jai.client.openaicompatible.OpenAICompatibleClient;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Mod {

    // register_client!(
    //    (openai, "openai", OpenAIConfig, OpenAIClient),
    //    (
    //        openai_compatible,
    //        "openai-compatible",
    //        OpenAICompatibleConfig,
    //        OpenAICompatibleClient
    //    ),
    //    (gemini, "gemini", GeminiConfig, GeminiClient),
    //    (claude, "claude", ClaudeConfig, ClaudeClient),
    //    (cohere, "cohere", CohereConfig, CohereClient),
    //    (
    //        azure_openai,
    //        "azure-openai",
    //        AzureOpenAIConfig,
    //        AzureOpenAIClient
    //    ),
    //    (vertexai, "vertexai", VertexAIConfig, VertexAIClient),
    //    (bedrock, "bedrock", BedrockConfig, BedrockClient),
    // );
    public static final List<RegisteredClient> REGISTERED_CLIENTS = List.of(
        new RegisteredClient("openai", "openai", ClientConfig::new, OpenAIClient::new),
        new RegisteredClient("openai_compatible", "openai-compatible", ClientConfig::new, OpenAICompatibleClient::new)
    );

    public static final Map<String, String> OPENAI_COMPATIBLE_PROVIDERS = new HashMap<>() {{
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

    private Mod() { }
}
