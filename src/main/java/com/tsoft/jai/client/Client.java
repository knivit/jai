package com.tsoft.jai.client;

import com.tsoft.jai.client.common.*;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.reqwest.RequestBuilder;
import com.tsoft.jai.reqwest.ReqwestClient;

import java.util.List;

public abstract class Client {

    // fn global_config(&self) -> &GlobalConfig;
    public abstract Config globalConfig();

    // fn extra_config(&self) -> Option<&ExtraConfig>;
    public abstract ExtraConfig extraConfig();

    // fn patch_config(&self) -> Option<&RequestPatch>;
    public abstract RequestPatch patchConfig();

    // fn name(&self) -> &str;
    public abstract String name();

    // fn model(&self) -> &Model;
    public abstract Model model();

    // fn model_mut(&mut self) -> &mut Model;
    public abstract Model modelMut();

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
    public ChatCompletionsOutput chatCompletions() {

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
    public void chatCompletionsStreaming() {

    }

    // async fn embeddings(&self, data: &EmbeddingsData) -> Result<Vec<Vec<f32>>> {
    //     let client = self.build_client()?;
    //     self.embeddings_inner(&client, data)
    //         .await
    //         .context("Failed to call embeddings api")
    // }
    public List<List<Float>> embeddings() {

    }

    // async fn rerank(&self, data: &RerankData) -> Result<RerankOutput> {
    //     let client = self.build_client()?;
    //     self.rerank_inner(&client, data)
    //         .await
    //         .context("Failed to call rerank api")
    // }
    public RerankOutput rerank(RerankData data) {

    }

    // async fn chat_completions_inner(
    //     &self,
    //     client: &ReqwestClient,
    //     data: ChatCompletionsData,
    // ) -> Result<ChatCompletionsOutput>;
    public abstract ChatCompletionsOutput chatCompletionsInner(ReqwestClient client, ChatCompletionsData data) {

    }

    // async fn chat_completions_streaming_inner(
    //     &self,
    //     client: &ReqwestClient,
    //     handler: &mut SseHandler,
    //     data: ChatCompletionsData,
    // ) -> Result<()>;
    public void chat_completions_streaming_inner(ReqwestClient client, SseHandler handler, ChatCompletionsData data) {

    }

    // async fn embeddings_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &EmbeddingsData,
    // ) -> Result<EmbeddingsOutput> {
    //     bail!("The client doesn't support embeddings api")
    // }
    public EmbeddingsOutput embeddingsInner(ReqwestClient client, EmbeddingsData data) {

    }

    // async fn rerank_inner(
    //     &self,
    //     _client: &ReqwestClient,
    //     _data: &RerankData,
    // ) -> Result<RerankOutput> {
    //     bail!("The client doesn't support rerank api")
    // }
    public RerankOutput rerankInner(ReqwestClient client, RerankData data) {

    }

    // fn request_builder(
    //     &self,
    //     client: &reqwest::Client,
    //     mut request_data: RequestData,
    // ) -> RequestBuilder {
    //     self.patch_request_data(&mut request_data);
    //     request_data.into_builder(client)
    // }
    public RequestBuilder requestBuilder(ReqwestClient client) {

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

    }
}
