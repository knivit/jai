package com.tsoft.jai.client;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.common.*;
import com.tsoft.jai.client.mod.RegisteredClient;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.reqwest.ClientBuilder;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.ReqwestClient;
import com.tsoft.jai.serde.Value;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Triple;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.client.mod.Mod.REGISTERED_CLIENTS;
import static com.tsoft.jai.tokio.Select.branch;
import static com.tsoft.jai.tokio.Select.select;
import static com.tsoft.jai.utils.AbortSignal.waitAbortSignal;
import static com.tsoft.jai.utils.Mod.setProxy;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public abstract class Client {

    // fn name(&self) -> &str;
    public abstract String getName();

    public abstract ClientConfig getClientConfig();

    public abstract Config getConfig();

    // fn model(&self) -> &Model;
    public abstract Model getModel();

    // fn extra_config(&self) -> Option<&ExtraConfig>;
    //
    // fn extra_config(&self) -> Option<&$crate::client::ExtraConfig> {
    //     self.config.extra.as_ref()
    // }
    public ExtraConfig extraConfig() {
        return getClientConfig().getExtra();
    }

    // fn patch_config(&self) -> Option<&RequestPatch>;
    public RequestPatch patchConfig() {
        return getClientConfig().getPatch();
    }

    // fn model_mut(&mut self) -> &mut Model;
    //public abstract Model modelMut();

    // pub fn init(global_config: &$crate::config::GlobalConfig, model: &$crate::client::Model) -> Option<Box<dyn Client>> {
    //    let config = global_config.read().clients.iter().find_map(|client_config| {
    //        if let ClientConfig::$config(c) = client_config {
    //            if Self::name(c) == model.client_name() {
    //                return Some(c.clone())
    //            }
    //        }
    //        None
    //    })?;
    //
    //    Some(Box::new(Self {
    //        global_config: global_config.clone(),
    //        config,
    //        model: model.clone(),
    //    }))
    // }
    public static Client init(Config config, Model model) {
        ClientConfig clientConfig = config.getClients().stream()
            .filter(e -> Objects.equals(e.getName(), model.getClientName()))
            .findAny()
            .orElse(null);
        if (clientConfig == null) {
            return null;
        }

        RegisteredClient registeredClient = REGISTERED_CLIENTS.stream()
            .filter(e -> Objects.equals(e.getType(), clientConfig.getType()))
            .findAny()
            .orElse(null);
        if (registeredClient == null) {
            return null;
        }

        Triple<ClientConfig, Config, Model> triple = new Triple<>(clientConfig.clone(), config, model);
        return registeredClient.getClientSupplier().apply(triple);
    }

    // fn build_client(&self) -> Result<ReqwestClient> {
    //    let mut builder = ReqwestClient::builder();
    //    let extra = self.extra_config();
    //    let timeout = extra.and_then(|v| v.connect_timeout).unwrap_or(10);
    //    if let Some(proxy) = extra.and_then(|v| v.proxy.as_deref()) {
    //        builder = set_proxy(builder, proxy)?;
    //    }
    //    if let Some(user_agent) = self.global_config().read().user_agent.as_ref() {
    //        builder = builder.user_agent(user_agent);
    //    }
    //    let client = builder
    //        .connect_timeout(Duration::from_secs(timeout))
    //        .build()
    //        .with_context(|| "Failed to build client")?;
    //    Ok(client)
    // }
    public Result<ReqwestClient> buildClient() {
        ClientBuilder builder = ReqwestClient.builder();
        ExtraConfig extra = extraConfig();
        int timeout = 10;
        if (extra != null && extra.getConnectTimeout() != null) {
            timeout = extra.getConnectTimeout();
        }
        if (extra != null && !isBlank(extra.getProxy())) {
            Result<ClientBuilder> res = setProxy(builder, extra.getProxy());
            if (isErr(res)) {
                return Err(res);
            }
            builder = res.getValue();
        }
        String userAgent = getConfig().getUserAgent();
        if (!isBlank(userAgent)) {
            builder = builder.userAgent(userAgent);
        }
        Result<ReqwestClient> client = builder
            .connectTimeout(Duration.ofSeconds(timeout))
            .build()
            .withContext(() -> "Failed to build client");
        return client;
    }

    // async fn chat_completions(&self, input: Input) -> Result<ChatCompletionsOutput> {
    //     if self.global_config().read().dry_run {
    //         let content = input.echo_messages();
    //         return Ok(ChatCompletionsOutput::new(&content));
    //     }
    //     let client = self.build_client()?;
    //     let data = input.prepare_completion_data(self.model(), false)?;
    //     self.chat_completions_inner(&client, data)
    //         .await
    //         .with_context(|| "Failed to call chat-completions api")
    // }
    public Result<ChatCompletionsOutput> chatCompletions(Input input) {
        if (getConfig().isDryRun()) {
            String content = input.echoMessages();
            return Ok(new ChatCompletionsOutput().setText(content));
        }
        Result<ReqwestClient> res = buildClient();
        if (isErr(res)) {
            return Err(res);
        }
        ReqwestClient client = res.getValue();
        Result<ChatCompletionsData> data = input.prepareCompletionData(getModel(), false);
        if (isErr(data)) {
            return Err(data);
        }
        return chatCompletionsInner(client, data.getValue())
            .withContext(() ->"Failed to call chat-completions api");
    }

    // async fn chat_completions_streaming(
    //     &self,
    //     input: &Input,
    //     handler: &mut SseHandler,
    // ) -> Result<()> {
    //     let abort_signal = handler.abort();
    //     let input = input.clone();
    //     tokio::select! {
    //         ret = async {
    //             if self.global_config().read().dry_run {
    //                 let content = input.echo_messages();
    //                 handler.text(&content)?;
    //                 return Ok(());
    //             }
    //             let client = self.build_client()?;
    //             let data = input.prepare_completion_data(self.model(), true)?;
    //             self.chat_completions_streaming_inner(&client, handler, data).await
    //         } => {
    //             handler.done();
    //             ret.with_context(|| "Failed to call chat-completions api")
    //         }
    //         _ = wait_abort_signal(&abort_signal) => {
    //             handler.done();
    //             Ok(())
    //         },
    //     }
    // }
    public Result<?> chatCompletionsStreaming(Input input, SseHandler handler) {
        AbortSignal abortSignal = handler.getAbortSignal();
        return select(
            branch(supplyAsync(() -> {
                if (getConfig().isDryRun()) {
                    String content = input.echoMessages();
                    Result<?> res = handler.text(content);
                    if (isErr(res)) {
                        return res;
                    }
                    return Ok();
                }
                Result<ReqwestClient> client = buildClient();
                if (isErr(client)) {
                    return client;
                }
                Result<ChatCompletionsData> data = input.prepareCompletionData(getModel(), true);
                if (isErr(data)) {
                    return data;
                }
                return chatCompletionsStreamingInner(client.getValue(), handler, data.getValue());
            }), ret -> {
                handler.done();
                return ret.withContext(() -> "Failed to call chat-completions api");
            }),

            branch(supplyAsync(() -> {
                waitAbortSignal(abortSignal);
                return Ok();
            }), ret -> {
                handler.done();
                return Ok();
            }));
    }

    // async fn embeddings(&self, data: &EmbeddingsData) -> Result<Vec<Vec<f32>>> {
    //     let client = self.build_client()?;
    //     self.embeddings_inner(&client, data)
    //         .await
    //         .context("Failed to call embeddings api")
    // }
    public Result<List<List<Float>>> embeddings(EmbeddingsData data) {
        Result<ReqwestClient> client = buildClient();
        if (isErr(client)) {
            return Err(client);
        }
        embeddingsInner(client.getValue(), data)
            .context("Failed to call embeddings api");
        return Ok(Collections.emptyList());
    }

    // async fn rerank(&self, data: &RerankData) -> Result<RerankOutput> {
    //     let client = self.build_client()?;
    //     self.rerank_inner(&client, data)
    //         .await
    //         .context("Failed to call rerank api")
    // }
    public Result<List<RerankResult>> rerank(RerankData data) {
        Result<ReqwestClient> client = buildClient();
        if (isErr(client)) {
            return Err(client);
        }
        return rerankInner(client.getValue(), data)
            .context("Failed to call rerank api");
    }

    // async fn chat_completions_inner(
    //     &self,
    //     client: &ReqwestClient,
    //     data: ChatCompletionsData,
    // ) -> Result<ChatCompletionsOutput>;
    //
    // async fn chat_completions_inner(
    //     &self,
    //     client: &reqwest::Client,
    //     data: $crate::client::ChatCompletionsData,
    // ) -> anyhow::Result<$crate::client::ChatCompletionsOutput> {
    //     let request_data = $prepare_chat_completions(self, data)?;
    //     let builder = self.request_builder(client, request_data);
    //     $chat_completions(builder, self.model()).await
    // }
    public Result<ChatCompletionsOutput> chatCompletionsInner(ReqwestClient client, ChatCompletionsData data) {
        return Ok();
    }

    public abstract Result<RequestData> prepareChatCompletions(ChatCompletionsData data);

    public abstract Result<?> chatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model);

    // async fn chat_completions_streaming_inner(
    //     &self,
    //     client: &ReqwestClient,
    //     handler: &mut SseHandler,
    //     data: ChatCompletionsData,
    // ) -> Result<()>;

    // async fn chat_completions_streaming_inner(
    //    &self,
    //    client: &reqwest::Client,
    //    handler: &mut $crate::client::SseHandler,
    //    data: $crate::client::ChatCompletionsData,
    // ) -> Result<()> {
    //    let request_data = $prepare_chat_completions(self, data)?;
    //    let builder = self.request_builder(client, request_data);
    //    $chat_completions_streaming(builder, handler, self.model()).await
    // }
    public Result<?> chatCompletionsStreamingInner(ReqwestClient client, SseHandler handler, ChatCompletionsData data) {
        Result<RequestData> res = prepareChatCompletions(data);
        if (isErr(res)) {
            return Err(res);
        }
        RequestData requestData = res.getValue();
        RequestBuilder builder = requestBuilder(client, requestData);
        return chatCompletionsStreaming(builder, handler, getModel());
    }

    // async fn embeddings_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &EmbeddingsData,
    // ) -> Result<EmbeddingsOutput> {
    //     bail!("The client doesn't support embeddings api")
    // }
    public Result<EmbeddingsOutput> embeddingsInner(ReqwestClient client, EmbeddingsData data) {
        return bail("The client doesn't support embeddings api");
    }

    // async fn rerank_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &RerankData,
    // ) -> Result<RerankOutput> {
    //     bail!("The client doesn't support rerank api")
    // }
    public Result<List<RerankResult>> rerankInner(ReqwestClient client, RerankData data) {
        return bail("The client doesn't support rerank api");
    }

    // fn request_builder(
    //     &self,
    //     client: &reqwest::Client,
    //     mut request_data: RequestData,
    // ) -> RequestBuilder {
    //     self.patch_request_data(&mut request_data);
    //     request_data.into_builder(client)
    // }
    public RequestBuilder requestBuilder(ReqwestClient client, RequestData requestData) {
        patchRequestData(requestData);
        return requestData.intoBuilder(client);
    }

    // fn patch_request_data(&self, request_data: &mut RequestData) {
    //     let model_type = self.model().model_type();
    //     if let Some(patch) = self.model().patch() {
    //         request_data.apply_patch(patch.clone());
    //     }
    //
    //     let patch_map = std::env::var(get_env_name(&format!(
    //         "patch_{}_{}",
    //         self.model().client_name(),
    //         model_type.api_name(),
    //     )))
    //     .ok()
    //     .and_then(|v| serde_json::from_str(&v).ok())
    //     .or_else(|| {
    //         self.patch_config()
    //             .and_then(|v| model_type.extract_patch(v))
    //             .cloned()
    //     });
    //     let patch_map = match patch_map {
    //         Some(v) => v,
    //         _ => return,
    //     };
    //     for (key, patch) in patch_map {
    //         let key = ESCAPE_SLASH_RE.replace_all(&key, r"\/");
    //         if let Ok(regex) = Regex::new(&format!("^({key})$")) {
    //             if let Ok(true) = regex.is_match(self.model().name()) {
    //                 request_data.apply_patch(patch);
    //                 return;
    //             }
    //         }
    //     }
    // }
    public void patchRequestData(RequestData requestData) {
        ModelType modelType = getModel().getModelType();
        Value patch = getModel().getPatch();
        if (patch != null) {
            requestData.applyPatch(patch);
        }
    }
}
