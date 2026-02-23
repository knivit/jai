package com.tsoft.jai.client.common;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.model.ProviderModels;
import com.tsoft.jai.client.openaicompatible.OpenAICompatibleClient;
import com.tsoft.jai.client.stream.SseEvent;
import com.tsoft.jai.client.stream.SseHandler;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.config.Input;
import com.tsoft.jai.env.Env;
import com.tsoft.jai.function.ToolCall;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.inquire.prompt.MultiSelect;
import com.tsoft.jai.inquire.prompt.Select;
import com.tsoft.jai.inquire.prompt.Text;
import com.tsoft.jai.inquire.validator.Validation;
import com.tsoft.jai.inquire.validator.Validator;
import com.tsoft.jai.serde.Value;
import com.tsoft.jai.serde.serdeyaml.SerdeYaml;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedReceiver;
import com.tsoft.jai.tokio.sync.mpsc.UnboundedSender;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import com.tsoft.jai.utils.base.TupleN;
import tools.jackson.core.type.TypeReference;

import java.util.*;
import java.util.regex.Pattern;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.client.mod.Mod.OPENAI_COMPATIBLE_PROVIDERS;
import static com.tsoft.jai.core.macros.BuiltIn.includeStr;
import static com.tsoft.jai.function.Functions.evalToolCalls;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.inquire.spinner.Spinner.abortableRunWithSpinner;
import static com.tsoft.jai.render.Mod.renderStream;
import static com.tsoft.jai.serde.Value.json;
import static com.tsoft.jai.tokio.Join.join;
import static com.tsoft.jai.tokio.sync.mpsc.Unbounded.unboundedChannel;
import static com.tsoft.jai.utils.AbortSignal.createAbortSignal;
import static com.tsoft.jai.utils.Mod.extractCodeBlock;
import static com.tsoft.jai.utils.Mod.stripThinkTag;
import static com.tsoft.jai.utils.Request.fetchModels;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.*;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Common {

    // const MODELS_YAML: &str = include_str!("../../models.yaml");
    private static final String MODELS_YAML = includeStr("/models.yaml");

    // pub static ALL_PROVIDER_MODELS: LazyLock<Vec<ProviderModels>> = LazyLock::new(|| {
    //    Config::loal_models_override()
    //        .ok()
    //        .unwrap_or_else(|| serde_yaml::from_str(MODELS_YAML).unwrap())
    // });
    public static final List<ProviderModels> ALL_PROVIDER_MODELS =
        Config.loadModelsOverride()
            .ok()
            .unwrapOrElse(() -> SerdeYaml.fromStr(MODELS_YAML, new TypeReference<List<ProviderModels>>() { }).unwrap());

    // static EMBEDDING_MODEL_RE: LazyLock<Regex> = LazyLock::new(|| {
    //    Regex::new(r"((^|/)(bge-|e5-|uae-|gte-|text-)|embed|multilingual|minilm)").unwrap()
    // });
    private static final Pattern EMBEDDING_MODEL_RE = Pattern.compile("((^|/)(bge-|e5-|uae-|gte-|text-)|embed|multilingual|minilm)");

    // pub async fn create_openai_compatible_client_config(
    //    client: &str,
    // ) -> Result<Option<(String, Value)>> {
    //    let api_base = super::OPENAI_COMPATIBLE_PROVIDERS
    //        .into_iter()
    //        .find(|(name, _)| client == *name)
    //        .map(|(_, api_base)| api_base)
    //        .unwrap_or("http(s)://{API_ADDR}/v1");
    //
    //    let name = if client == OpenAICompatibleClient::NAME {
    //        let value = prompt_input_string("Provider Name", true, None)?;
    //        value.replace(' ', "-")
    //    } else {
    //        client.to_string()
    //    };
    //
    //    let mut config = json!({
    //        "type": OpenAICompatibleClient::NAME,
    //        "name": &name,
    //    });
    //
    //    let api_base = if api_base.contains('{') {
    //        prompt_input_string("API Base", true, Some(&format!("e.g. {api_base}")))?
    //    } else {
    //        api_base.to_string()
    //    };
    //    config["api_base"] = api_base.into();
    //
    //    let api_key = prompt_input_string("API Key", false, None)?;
    //    if !api_key.is_empty() {
    //        config["api_key"] = api_key.into();
    //    }
    //
    //    let model = set_client_models_config(&mut config, &name).await?;
    //    let clients = json!(vec![config]);
    //    Ok(Some((model, clients)))
    // }
    public static Result<Tuple<String, Value>> createOpenaiCompatibleClientConfig(String client) {
        String apiBase = OPENAI_COMPATIBLE_PROVIDERS.entrySet().stream()
            .filter(e -> e.getKey().equals(client))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse("http(s)://{API_ADDR}/v1");

        String name;
        if (OpenAICompatibleClient.NAME.equals(client)) {
            Result<String> res = promptInputString("provider name", true, null);
            if (isErr(res)) {
                return Err(res);
            }
            String value = res.getValue();
            name = replace(value, ' ', '-');
        } else {
            name = client;
        }

        Value config = json(
            "type", OpenAICompatibleClient.NAME,
            "name", name
        );

        if (apiBase.contains("{")) {
            Result<String> res = promptInputString("API Base", true, format("e.g. {}", apiBase));
            if (isErr(res)) {
                return Err(res);
            }
            apiBase = res.getValue();
        }

        config.put("api_base", apiBase);

        Result<String> res = promptInputString("API key", false, null);
        if (isErr(res)) {
            return Err(res);
        }
        String apiKey = res.getValue();
        if (!isBlank(apiKey)) {
            config.put("api_key", apiKey);
        }

        res = setClientModelsConfig(config, name);
        if (isErr(res)) {
            return Err(res);
        }
        String model = res.getValue();
        Value clients = new Value(List.of(config));
        return Ok(new Tuple<>(model, clients));
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
    public static Result<Tuple<String, List<ToolResult>>> callChatCompletions(Input input, boolean print, boolean extractCode, Client client, AbortSignal abortSignal) {
        Result<ChatCompletionsOutput> ret = abortableRunWithSpinner(() -> client.chatCompletions(input), "Generating", abortSignal);
        return switch (ret.getType()) {
            case Ok -> {
                String text = ret.getValue().getText();
                List<ToolCall> toolCalls = ret.getValue().getToolCalls();
                if (!isBlank(text)) {
                    if (extractCode) {
                        text = extractCodeBlock(stripThinkTag(text));
                    }
                    if (print) {
                        client.getConfig().printMarkdown(text);
                    }
                }
                Result<List<ToolResult>> res = evalToolCalls(client.getConfig(), toolCalls);
                if (isErr(res)) {
                    yield Err(res);
                }
                yield Ok(new Tuple<>(text, res.getValue()));
            }
            case Err -> Err(ret);
        };
    }

    // pub async fn call_chat_completions_streaming(
    //    input: &Input,
    //    client: &dyn Client,
    //    abort_signal: AbortSignal,
    // ) -> Result<(String, Vec<ToolResult>)> {
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
    public static Result<Tuple<String, List<ToolResult>>> callChatCompletionsStreaming(Input input, Client client, AbortSignal abortSignal) {
        Tuple<UnboundedSender<SseEvent>, UnboundedReceiver<SseEvent>> utuple = unboundedChannel();
        UnboundedSender<SseEvent> tx = utuple.first();
        UnboundedReceiver<SseEvent> rx = utuple.second();
        SseHandler handler = new SseHandler(tx, abortSignal);

        TupleN tupleN = join(
            supplyAsync(() -> client.chatCompletionsStreaming(input, handler)),
            supplyAsync(() -> renderStream(rx, client.getConfig(), abortSignal)));
        Result<?> sendRet = tupleN.next();
        Result<?> renderRet = tupleN.next();

        if (handler.abort().aborted()) {
            return bail("Aborted.");
        }

        if (isErr(renderRet)) {
            return Err(renderRet);
        }

        Tuple<String, List<ToolCall>> tuple = handler.take();
        String text = tuple.first();
        List<ToolCall> toolCalls = tuple.second();
        return switch (getType(sendRet)) {
            case Ok -> {
                if (!isBlank(text) && !text.endsWith("\n")) {
                    println();
                }
                Result<List<ToolResult>> res = evalToolCalls(client.getConfig(), toolCalls);
                if (isErr(res)) {
                    yield Err(res);
                }
                yield Ok(new Tuple<>(text, res.getValue()));
            }
            case Err -> {
                if (!isBlank(text)) {
                    println();
                }
                yield Err(sendRet);
            }
        };
    }

    // pub fn catch_error(data: &Value, status: u16) -> Result<()> {
    //    if (200..300).contains(&status) {
    //        return Ok(());
    //    }
    //    debug!("Invalid response, status: {status}, data: {data}");
    //    if let Some(error) = data["error"].as_object() {
    //        if let (Some(typ), Some(message)) = (
    //            json_str_from_map(error, "type"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (type: {typ})");
    //        } else if let (Some(typ), Some(message)) = (
    //            json_str_from_map(error, "code"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (code: {typ})");
    //        }
    //    } else if let Some(error) = data["errors"][0].as_object() {
    //        if let (Some(code), Some(message)) = (
    //            error.get("code").and_then(|v| v.as_u64()),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (status: {code})")
    //        }
    //    } else if let Some(error) = data[0]["error"].as_object() {
    //        if let (Some(status), Some(message)) = (
    //            json_str_from_map(error, "status"),
    //            json_str_from_map(error, "message"),
    //        ) {
    //            bail!("{message} (status: {status})")
    //        }
    //    } else if let (Some(detail), Some(status)) = (data["detail"].as_str(), data["status"].as_i64())
    //    {
    //        bail!("{detail} (status: {status})");
    //    } else if let Some(error) = data["error"].as_str() {
    //        bail!("{error}");
    //    } else if let Some(message) = data["message"].as_str() {
    //        bail!("{message}");
    //    }
    //    bail!("Invalid response data: {data} (status: {status})");
    // }
    public static <T> Result<T> catchError(Value data, int status) {
        return bail("Invalid response data: {} (status: {})", data, status);
    }

    // async fn set_client_models_config(client_config: &mut Value, client: &str) -> Result<String> {
    //    if let Some(provider) = ALL_PROVIDER_MODELS.iter().find(|v| v.provider == client) {
    //        let models: Vec<String> = provider
    //            .models
    //            .iter()
    //            .filter(|v| v.model_type == "chat")
    //            .map(|v| v.name.clone())
    //            .collect();
    //        let model_name = select_model(models)?;
    //        return Ok(format!("{client}:{model_name}"));
    //    }
    //    let mut model_names = vec![];
    //    if let (Some(true), Some(api_base), api_key) = (
    //        client_config["type"]
    //            .as_str()
    //            .map(|v| v == OpenAICompatibleClient::NAME),
    //        client_config["api_base"].as_str(),
    //        client_config["api_key"]
    //            .as_str()
    //            .map(|v| v.to_string())
    //            .or_else(|| {
    //                let env_name = format!("{client}_api_key").to_ascii_uppercase();
    //                std::env::var(&env_name).ok()
    //            }),
    //    ) {
    //        match abortable_run_with_spinner(
    //            fetch_models(api_base, api_key.as_deref()),
    //            "Fetching models",
    //            create_abort_signal(),
    //        )
    //        .await
    //        {
    //            Ok(fetched_models) => {
    //                model_names = MultiSelect::new("LLMs to include (required):", fetched_models)
    //                    .with_validator(|list: &[ListOption<&String>]| {
    //                        if list.is_empty() {
    //                            Ok(Validation::Invalid(
    //                                "At least one item must be selected".into(),
    //                            ))
    //                        } else {
    //                            Ok(Validation::Valid)
    //                        }
    //                    })
    //                    .prompt()?;
    //            }
    //            Err(err) => {
    //                eprintln!("✗ Fetch models failed: {err}");
    //            }
    //        }
    //    }
    //    if model_names.is_empty() {
    //        model_names = prompt_input_string(
    //            "LLMs to add",
    //            true,
    //            Some("Separated by commas, e.g. llama3.3,qwen2.5"),
    //        )?
    //        .split(',')
    //        .filter_map(|v| {
    //            let v = v.trim();
    //            if v.is_empty() {
    //                None
    //            } else {
    //                Some(v.to_string())
    //            }
    //        })
    //        .collect::<Vec<_>>();
    //    }
    //    if model_names.is_empty() {
    //        bail!("No models");
    //    }
    //    let models: Vec<Value> = model_names
    //        .iter()
    //        .map(|v| {
    //            let l = v.to_lowercase();
    //            if l.contains("rank") {
    //                json!({
    //                    "name": v,
    //                    "type": "reranker",
    //                })
    //            } else if let Ok(true) = EMBEDDING_MODEL_RE.is_match(&l) {
    //                json!({
    //                    "name": v,
    //                    "type": "embedding",
    //                    "default_chunk_size": 1000,
    //                    "max_batch_size": 100
    //                })
    //            } else if v.contains("vision") {
    //                json!({
    //                    "name": v,
    //                    "supports_vision": true
    //                })
    //            } else {
    //                json!({
    //                    "name": v,
    //                })
    //            }
    //        })
    //        .collect();
    //    client_config["models"] = models.into();
    //    let model_name = select_model(model_names)?;
    //    Ok(format!("{client}:{model_name}"))
    // }
    public static Result<String> setClientModelsConfig(Value clientConfig, String client) {
        ProviderModels provider = ALL_PROVIDER_MODELS.stream().filter(v -> Objects.equals(v.getProvider(), client)).findAny().orElse(null);
        if (provider != null) {
            List<String> models = provider
                .getModels()
                .stream()
                .filter(v -> Objects.equals(v.getModelType(), "chat"))
                .map(v -> v.getName())
                .toList();
            Result<String> res = selectModel(models);
            if (isErr(res)) {
                return Err(res);
            }
            String modelName = res.getValue();
            return Ok(format("{}:{}", client, modelName));
        }
        List<String> modelNames = new ArrayList<>();
        boolean isOpenAICompatible = Objects.equals(OpenAICompatibleClient.NAME, clientConfig.get("type").asStr());
        String apiBase = clientConfig.get("api_base").asStr();
        String apiKeyStr = clientConfig.get("api_key").asStr();
        if (apiKeyStr == null) {
            String envName = format("{}_api_key", client).toUpperCase();
            apiKeyStr = Env.var(envName).ok().unwrapOr(null);
        }
        final String apiKey = apiKeyStr;
        if (isOpenAICompatible && !isBlank(apiBase) && !isBlank(apiKey)) {
            Result<List<String>> res = abortableRunWithSpinner(() -> fetchModels(apiBase, apiKey), "Fetching models", createAbortSignal());
            switch (res.getType()) {
                case Ok -> {
                    List<String> fetchedModels = res.getValue();
                    Result<List<String>> ret = new MultiSelect("LLMs to include (required):", fetchedModels)
                        .setValidator(list -> {
                            if (isEmpty(list)) {
                                return Ok(Validation.Invalid("At least one item must be selected"));
                            } else {
                                return Ok(Validation.Valid());
                            }
                        })
                        .prompt();
                    if (isErr(ret)) {
                        return Err(ret);
                    }
                    modelNames = ret.getValue();
                }
                case Err -> println("✗ Fetch models failed: {}", res.getErr());
            }
        }
        if (isEmpty(modelNames)) {
            Result<String> res = promptInputString("LLMs to add",
                true, "Separated by commas, e.g. llama3.3,qwen2.5");
            if (isErr(res)) {
                return Err(res);
            }
            modelNames = Arrays.stream(res.getValue()
                .split(","))
                .map(v -> {
                    v = v.trim();
                    if (isEmpty(v)) {
                        return null;
                    } else {
                        return v;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        }
        if (isEmpty(modelNames)) {
            return bail("No models");
        }
        List<Value> models = modelNames
            .stream()
            .map(v -> {
                String l = v.toLowerCase();
                if (l.contains("rank")) {
                    return json(
                        "name", v,
                        "type", "reranker"
                    );
                } else if (EMBEDDING_MODEL_RE.matcher(l).hasMatch()) {
                    return json(
                        "name", v,
                        "type", "embedding",
                        "default_chunk_size", 1000,
                        "max_batch_size", 100
                    );
                } else if (v.contains(("vision"))) {
                    return json(
                        "name", v,
                        "supports_vision", true
                    );
                } else {
                    return json(
                        "name", v
                    );
                }
            })
            .toList();
        clientConfig.put("models", models);
        Result<String> res = selectModel(modelNames);
        if (isErr(res)) {
            return Err(res);
        }
        String modelName = res.getValue();
        return Ok(format("{}:{}", client, modelName));
    }

    // fn select_model(model_names: Vec<String>) -> Result<String> {
    //    if model_names.is_empty() {
    //        bail!("No models");
    //    }
    //    let model = if model_names.len() == 1 {
    //        model_names[0].clone()
    //    } else {
    //        Select::new("Default Model (required):", model_names).prompt()?
    //    };
    //    Ok(model)
    // }
    public static Result<String> selectModel(List<String> modelNames) {
        if (isEmpty(modelNames)) {
            return bail("No models");
        }
        String model;
        if (modelNames.size() == 1) {
            model = modelNames.get(0);
        } else {
            Result<String> res = new Select("Default Model (required):", modelNames).prompt();
            if (isErr(res)) {
                return Err(res);
            }
            model = res.getValue();
        }
        return Ok(model);
    }

    // fn prompt_input_string(
    //    desc: &str,
    //    required: bool,
    //    help_message: Option<&str>,
    // ) -> anyhow::Result<String> {
    //    let desc = if required {
    //        format!("{desc} (required):")
    //    } else {
    //        format!("{desc} (optional):")
    //    };
    //    let mut text = Text::new(&desc);
    //    if required {
    //        text = text.with_validator(required!("This field is required"))
    //    }
    //    if let Some(help_message) = help_message {
    //        text = text.with_help_message(help_message);
    //    }
    //    let text = text.prompt()?;
    //    Ok(text)
    // }
    public static Result<String> promptInputString(String desc, boolean required, String helpMessage) {
        if (required) {
            desc = format("{} (required):", desc);
        } else {
            desc = format("{} (optional):", desc);
        }
        Text text = new Text(desc);
        if (required) {
            text.setValidator(Validator.required("This field is required"));
        }
        Result<String> res = text.prompt();
        if (isErr(res)) {
            return Err(res);
        }
        String value = res.getValue();
        return Ok(value);
    }
}
