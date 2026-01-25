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
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.reqwest.ClientBuilder;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.ReqwestClient;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import lombok.Getter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.client.mod.Mod.GLOBAL_CONFIG;
import static com.tsoft.jai.client.mod.Mod.REGISTERED_CLIENTS;
import static com.tsoft.jai.tokio.Select.branch;
import static com.tsoft.jai.tokio.Select.select;
import static com.tsoft.jai.utils.AbortSignal.waitAbortSignal;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public abstract class Client {

    public abstract ClientConfig getClientConfig();

    // fn name(&self) -> &str;
    public abstract String getName();

    // fn model(&self) -> &Model;
    public abstract Model getModel();

    @Getter
    private Config config;

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
        RegisteredClient client = REGISTERED_CLIENTS.stream()
            .filter(e -> Objects.equals(e.getName(), model.getClientName()))
            .findAny()
            .orElse(null);
        assert client != null;

        ClientConfig clientConfig = GLOBAL_CONFIG.getClients().stream()
            .filter(e -> Objects.equals(e.getName(), model.getClientName()))
            .findAny()
            .orElse(null);
        assert clientConfig != null;

        return client.getClientSupplier().apply(clientConfig.clone(), model);
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
    public ReqwestClient buildClient() {
        ClientBuilder builder = ReqwestClient.builder();
        ExtraConfig extra = extraConfig();
        int timeout = 10;
        String proxy;
        if (extra != null) {
            if (extra.getConnectTimeout() != null) {
                timeout = extra.getConnectTimeout();
            }
            if (!isBlank(extra.getProxy())) {
                //
            }
        }
        String userAgent = getConfig().getUserAgent();
        if (!isBlank(userAgent)) {
            //
        }
        return builder
            .connectTimeout(Duration.ofSeconds(timeout))
            .build();
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
    public ChatCompletionsOutput chatCompletions(Input input) {
        if (getConfig().isDryRun()) {
            String content = input.echoMessages();
            return new ChatCompletionsOutput().setText(content);
        }
        ReqwestClient client = buildClient();
        ChatCompletionsData data = input.prepareCompletionData(getModel(), false);
        return chatCompletionsInner(client, data);
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
                if (config.isDryRun()) {
                    String content = input.echoMessages();
                    handler.text(content);
                    return Ok();
                }
                ReqwestClient client = buildClient();
                ChatCompletionsData data = input.prepareCompletionData(getModel(), true);
                chatCompletionsStreamingInner(client, handler, data);
                return Ok();
            }), ret -> {
                handler.done();
                return switch (ret.getType()) {
                    case Ok -> ret;
                    case Err -> Err("Failed to call chat-completions api");
                };
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
    public List<List<Float>> embeddings() {
        return Collections.emptyList();
    }

    // async fn rerank(&self, data: &RerankData) -> Result<RerankOutput> {
    //     let client = self.build_client()?;
    //     self.rerank_inner(&client, data)
    //         .await
    //         .context("Failed to call rerank api")
    // }
    public List<RerankResult> rerank(RerankData data) {
        ReqwestClient client = buildClient();
        return rerankInner(client, data);
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
    public ChatCompletionsOutput chatCompletionsInner(ReqwestClient client, ChatCompletionsData data) {
        return null;
    }

    public abstract RequestData prepareChatCompletions(ChatCompletionsData data);

    public abstract void chatCompletionsStreaming(RequestBuilder builder, SseHandler handler, Model model);

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
    public void chatCompletionsStreamingInner(ReqwestClient client, SseHandler handler, ChatCompletionsData data) {
        RequestData requestData = prepareChatCompletions(data);
        RequestBuilder builder = requestBuilder(client, requestData);
        chatCompletionsStreaming(builder, handler, getModel());
    }

    // async fn embeddings_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &EmbeddingsData,
    // ) -> Result<EmbeddingsOutput> {
    //     bail!("The client doesn't support embeddings api")
    // }
    public EmbeddingsOutput embeddingsInner(ReqwestClient client, EmbeddingsData data) {
        bail("The client doesn't support embeddings api");
        return null;
    }

    // async fn rerank_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &RerankData,
    // ) -> Result<RerankOutput> {
    //     bail!("The client doesn't support rerank api")
    // }
    public List<RerankResult> rerankInner(ReqwestClient client, RerankData data) {
        bail("The client doesn't support rerank api");
        return Collections.emptyList();
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
    public Tuple<String, List<ToolResult>> callChatCompletionsStreaming(Input input, Client client, AbortSignal abortSignal) {
        return null;
    }

    // pub async fn call_chat_completions(
    //    input: &Input,
    //    print: bool,
    //    extract_code: bool,
    //    client: &dyn Client,
    //    abort_signal: AbortSignal,
    // ) -> Result<(String, Vec<ToolResult>)> {
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
    public Tuple<String, List<ToolResult>> callChatCompletions(Input input, boolean print, boolean extractCode, Client client, AbortSignal abortSignal) {
        return null;
    }
}
