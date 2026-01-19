package com.tsoft.jai.config;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.message.ImageUrl;
import com.tsoft.jai.client.message.MessageContent;
import com.tsoft.jai.client.message.MessageContentPart;
import com.tsoft.jai.client.message.MessageContentToolCalls;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.utils.*;
import com.tsoft.jai.utils.command.Command;
import com.tsoft.jai.utils.command.Shell;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.Function;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.client.macros.Macros.initClient;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.utils.Mod.isUrl;
import static com.tsoft.jai.utils.PathUtil.*;
import static com.tsoft.jai.utils.StringUtils.isBlank;
import static com.tsoft.jai.utils.StringUtils.stripSuffix;
import static com.tsoft.jai.utils.loader.Loader.isLoaderProtocol;

@Data
@Accessors(chain = true)
public class Input implements Cloneable {

    private Config config;
    private String text;
    private Tuple<String, List<String>> raw;
    private String patchedText;
    private String lastReply;
    private String continueOutput;
    private boolean regenerate;
    private List<String> medias;
    private Map<String, String> dataUrls;
    private MessageContentToolCalls toolCalls;
    private Role role;
    private String ragName;
    private boolean withSession;
    private boolean withAgent;

    private static final List<String> IMAGE_EXTS = List.of("png", "jpeg", "jpg", "webp", "gif");

    // pub fn from_str(config: &GlobalConfig, text: &str, role: Option<Role>) -> Self {
    //    let (role, with_session, with_agent) = resolve_role(&config.read(), role);
    //    Self {
    //        config: config.clone(),
    //        text: text.to_string(),
    //        raw: (text.to_string(), vec![]),
    //        patched_text: None,
    //        last_reply: None,
    //        continue_output: None,
    //        regenerate: false,
    //        medias: Default::default(),
    //        data_urls: Default::default(),
    //        tool_calls: None,
    //        role,
    //        rag_name: None,
    //        with_session,
    //        with_agent,
    //    }
    // }
    public static Input fromStr(Config config, String text, Role role) {
        Triple<Role, Boolean, Boolean> triple = resolveRole(config, role);
        role = triple.first();
        boolean withSession = triple.second();
        boolean withAgent = triple.third();
        return new Input()
            .setConfig(config)
            .setText(text)
            .setRaw(new Tuple<>(text, new ArrayList<>()))
            .setRole(role)
            .setWithSession(withSession)
            .setWithAgent(withAgent);
    }

    //  pub async fn from_files(
    //     config: &GlobalConfig,
    //     raw_text: &str,
    //     paths: Vec<String>,
    //     role: Option<Role>,
    // ) -> Result<Self> {
    //     let loaders = config.read().document_loaders.clone();
    //     let (raw_paths, local_paths, remote_urls, external_cmds, protocol_paths, with_last_reply) =
    //         resolve_paths(&loaders, paths)?;
    //     let mut last_reply = None;
    //     let (documents, medias, data_urls) = load_documents(
    //         &loaders,
    //         local_paths,
    //         remote_urls,
    //         external_cmds,
    //         protocol_paths,
    //     )
    //     .await
    //     .context("Failed to load files")?;
    //     let mut texts = vec![];
    //     if !raw_text.is_empty() {
    //         texts.push(raw_text.to_string());
    //     };
    //     if with_last_reply {
    //         if let Some(LastMessage { input, output, .. }) = config.read().last_message.as_ref() {
    //             if !output.is_empty() {
    //                 last_reply = Some(output.clone())
    //             } else if let Some(v) = input.last_reply.as_ref() {
    //                 last_reply = Some(v.clone());
    //             }
    //             if let Some(v) = last_reply.clone() {
    //                 texts.push(format!("\n{v}"));
    //             }
    //         }
    //         if last_reply.is_none() && documents.is_empty() && medias.is_empty() {
    //             bail!("No last reply found");
    //         }
    //     }
    //     let documents_len = documents.len();
    //     for (kind, path, contents) in documents {
    //         if documents_len == 1 && raw_text.is_empty() {
    //             texts.push(format!("\n{contents}"));
    //         } else {
    //             texts.push(format!(
    //                 "\n============ {kind}: {path} ============\n{contents}"
    //             ));
    //         }
    //     }
    //     let (role, with_session, with_agent) = resolve_role(&config.read(), role);
    //     Ok(Self {
    //         config: config.clone(),
    //         text: texts.join("\n"),
    //         raw: (raw_text.to_string(), raw_paths),
    //         patched_text: None,
    //         last_reply,
    //         continue_output: None,
    //         regenerate: false,
    //         medias,
    //         data_urls,
    //         tool_calls: Default::default(),
    //         role,
    //         rag_name: None,
    //         with_session,
    //         with_agent,
    //     })
    // }
    public static Input fromFiles(Config config, String rawText, List<String> paths, Role role) {
        Map<String, String> loaders = config.getDocumentLoaders();
        TupleN tuple = resolvePaths(loaders, paths);
        Set<String> rawPaths = tuple.get("raw_paths");
        Set<String> localPaths = tuple.get("local_paths");
        Set<String> remoteUrls = tuple.get("remote_urls");
        Set<String> externalCmds = tuple.get("external_cmds");
        Set<String> protocolPaths = tuple.get("protocol_paths");
        boolean withLastReply = tuple.get("with_last_reply");
        Object lastReply = null;
        tuple = loadDocuments(loaders, localPaths, remoteUrls, externalCmds, protocolPaths);
        if (tuple == null) {
            println("Failed to load files");
            return null;
        }
        Object documents = tuple.get("documents");
        Object medias = tuple.get("medias");
        Object dataUrls = tuple.get("data_urls");
        List<String> texts = new ArrayList<>();
        if (!isBlank(rawText)) {
            texts.add(rawText);
        }
        if (withLastReply) {

        }
    }

    // pub async fn from_files_with_spinner(
    //     config: &GlobalConfig,
    //     raw_text: &str,
    //     paths: Vec<String>,
    //     role: Option<Role>,
    //     abort_signal: AbortSignal,
    // ) -> Result<Self> {
    //     abortable_run_with_spinner(
    //         Input::from_files(config, raw_text, paths, role),
    //         "Loading files",
    //         abort_signal,
    //     )
    //     .await
    // }
    public static Input fromFilesWithSpinner(Config config, String rawText, List<String> paths, Role role, AbortSignal abortSignal) {
        return abortableRunWithSpinner((e) -> Input.fromFiles(config, rawText, paths, role), "Loading files", abortSignal);
    }

    // pub async fn abortable_run_with_spinner<F, T>(
    //    task: F,
    //    message: &str,
    //    abort_signal: AbortSignal,
    // ) -> Result<T>
    // where
    //    F: Future<Output = Result<T>>,
    // {
    //    let (_, spinner_rx) = Spinner::create(message);
    //    abortable_run_with_spinner_rx(task, spinner_rx, abort_signal).await
    public static <T, R> Input abortableRunWithSpinner(Function<T, R> task, String message, AbortSignal abortSignal) {
        return null;
    }

    // pub fn message_content(&self) -> MessageContent {
    //    if self.medias.is_empty() {
    //        MessageContent::Text(self.text())
    //    } else {
    //        let mut list: Vec<MessageContentPart> = self
    //            .medias
    //            .iter()
    //            .cloned()
    //            .map(|url| MessageContentPart::ImageUrl {
    //                image_url: ImageUrl { url },
    //            })
    //            .collect();
    //        if !self.text.is_empty() {
    //            list.insert(0, MessageContentPart::Text { text: self.text() });
    //        }
    //        MessageContent::Array(list)
    //    }
    // }
    public MessageContent messageContent() {
        if (CollectionsUtils.isEmpty(medias)) {
            return MessageContent.Text(text);
        } else {
            List<MessageContentPart> list = new ArrayList<>();
            for (String url : medias) {
                list.add(MessageContentPart.ImageUrl(new ImageUrl().setUrl(url)));
            }
            if (!isBlank(text)) {
                list.addFirst(MessageContentPart.Text(text));
            }
            return MessageContent.Array(list);
        }
    }

    // pub async fn use_embeddings(&mut self, abort_signal: AbortSignal) -> Result<()> {
    //    if self.text.is_empty() {
    //        return Ok(());
    //    }
    //    let rag = self.config.read().rag.clone();
    //    if let Some(rag) = rag {
    //        let result = Config::search_rag(&self.config, &rag, &self.text, abort_signal).await?;
    //        self.patched_text = Some(result);
    //        self.rag_name = Some(rag.name().to_string());
    //    }
    //    Ok(())
    // }
    public void useEmbeddings(AbortSignal abortSignal) {
        if (isBlank(text)) {
            return;
        }
        Rag rag = config.getRag();
        if (rag != null) {
            patchedText = Config.searchRag(config, rag, text, abortSignal);
            ragName = rag.getName();
        }
    }

    // pub fn merge_tool_results(mut self, output: String, tool_results: Vec<ToolResult>) -> Self {
    //    match self.tool_calls.as_mut() {
    //        Some(exist_tool_results) => {
    //            exist_tool_results.merge(tool_results, output);
    //        }
    //        None => self.tool_calls = Some(MessageContentToolCalls::new(tool_results, output)),
    //    }
    //    self
    // }
    public Input mergeToolResults(String output, List<ToolResult> toolResults) {
        if (toolCalls != null) {
            toolCalls.merge(toolResults, output);
        } else {
            toolCalls = new MessageContentToolCalls().setToolResults(toolResults).setText(output);
        }
        return this;
    }

    // pub fn create_client(&self) -> Result<Box<dyn Client>> {
    //    init_client(&self.config, Some(self.role().model().clone()))
    // }
    public Client createClient() {
        return initClient(config, role.getModel());
    }

    // pub fn is_empty(&self) -> bool {
    //    self.text.is_empty() && self.medias.is_empty()
    // }
    public boolean isEmpty() {
        return isBlank(text) && CollectionsUtils.isEmpty(medias);
    }

    // pub fn stream(&self) -> bool {
    //    self.config.read().stream && !self.role().model().no_stream()
    // }
    public boolean stream() {
        return config.isStream() && (role != null && role.getModel() != null && !role.getModel().noStream());
    }

    // fn resolve_paths(
    //    loaders: &HashMap<String, String>,
    //    paths: Vec<String>,
    //) -> Result<ResolvePathsOutput> {
    //    let mut raw_paths = IndexSet::new();
    //    let mut local_paths = IndexSet::new();
    //    let mut remote_urls = IndexSet::new();
    //    let mut external_cmds = IndexSet::new();
    //    let mut protocol_paths = IndexSet::new();
    //    let mut with_last_reply = false;
    //    for path in paths {
    //        if path == "%%" {
    //            with_last_reply = true;
    //            raw_paths.insert(path);
    //        } else if path.starts_with('`') && path.len() > 2 && path.ends_with('`') {
    //            external_cmds.insert(path[1..path.len() - 1].to_string());
    //            raw_paths.insert(path);
    //        } else if is_url(&path) {
    //            if path.strip_suffix("**").is_some() {
    //                bail!("Invalid website '{path}'");
    //            }
    //            remote_urls.insert(path.clone());
    //            raw_paths.insert(path);
    //        } else if is_loader_protocol(loaders, &path) {
    //            protocol_paths.insert(path.clone());
    //            raw_paths.insert(path);
    //        } else {
    //            let resolved_path = resolve_home_dir(&path);
    //            let absolute_path = to_absolute_path(&resolved_path)
    //                .with_context(|| format!("Invalid path '{path}'"))?;
    //            local_paths.insert(resolved_path);
    //            raw_paths.insert(absolute_path);
    //        }
    //    }
    //    Ok((
    //        raw_paths.into_iter().collect(),
    //        local_paths.into_iter().collect(),
    //        remote_urls.into_iter().collect(),
    //        external_cmds.into_iter().collect(),
    //        protocol_paths.into_iter().collect(),
    //        with_last_reply,
    //    ))
    // }
    private static TupleN resolvePaths(Map<String, String> loaders, List<String> paths) {
        Set<String> rawPaths = new LinkedHashSet<>();
        Set<String> localPaths = new LinkedHashSet<>();
        Set<String> remoteUrls = new LinkedHashSet<>();
        Set<String> externalCmds = new LinkedHashSet<>();
        Set<String> protocolPaths = new LinkedHashSet<>();
        boolean withLastReply = false;
        for (String path : paths) {
            if ("%%".equals(path)) {
                withLastReply = true;
                rawPaths.add(path);
            } else if (path.startsWith("`") && path.length() > 2 && path.endsWith("`'")) {
                externalCmds.add(path.substring(1, path.length() - 1));
                rawPaths.add(path);
            } else if (isUrl(path)) {
                if (!isBlank(stripSuffix(path, "**"))) {
                    bail("Invalid website '{}'", path);
                }
                remoteUrls.add(path);
                rawPaths.add(path);
            } else if (isLoaderProtocol(loaders, path)) {
                protocolPaths.add(path);
                rawPaths.add(path);
            } else {
                String resolvedPath = resolveHomeDir(path);
                String absolutePath = toAbsolutePath(resolvedPath);
                if (absolutePath == null) {
                    println("Invalid path '{}'", path);
                }
                localPaths.add(resolvedPath);
                rawPaths.add(absolutePath);
            }
        }

        return TupleN.asMap(
            "raw_paths", rawPaths,
            "local_paths", localPaths,
            "remote_urls", remoteUrls,
            "external_cmds", externalCmds,
            "protocol_paths", protocolPaths
        );
    }

    // async fn load_documents(
    //    loaders: &HashMap<String, String>,
    //    local_paths: Vec<String>,
    //    remote_urls: Vec<String>,
    //    external_cmds: Vec<String>,
    //    protocol_paths: Vec<String>,
    //) -> Result<(
    //    Vec<(&'static str, String, String)>,
    //    Vec<String>,
    //    HashMap<String, String>,
    //)> {
    //    let mut files = vec![];
    //    let mut medias = vec![];
    //    let mut data_urls = HashMap::new();
    //
    //    for cmd in external_cmds {
    //        let output = duct::cmd(&SHELL.cmd, &[&SHELL.arg, &cmd])
    //            .stderr_to_stdout()
    //            .unchecked()
    //            .read()
    //            .unwrap_or_else(|err| err.to_string());
    //        files.push(("CMD", cmd, output));
    //    }
    //
    //    let local_files = expand_glob_paths(&local_paths, true).await?;
    //    for file_path in local_files {
    //        if is_image(&file_path) {
    //            let contents = read_media_to_data_url(&file_path)
    //                .with_context(|| format!("Unable to read media '{file_path}'"))?;
    //            data_urls.insert(sha256(&contents), file_path);
    //            medias.push(contents)
    //        } else {
    //            let document = load_file(loaders, &file_path)
    //                .await
    //                .with_context(|| format!("Unable to read file '{file_path}'"))?;
    //            files.push(("FILE", file_path, document.contents));
    //        }
    //    }
    //
    //    for file_url in remote_urls {
    //        let (contents, extension) = fetch_with_loaders(loaders, &file_url, true)
    //            .await
    //            .with_context(|| format!("Failed to load url '{file_url}'"))?;
    //        if extension == MEDIA_URL_EXTENSION {
    //            data_urls.insert(sha256(&contents), file_url);
    //            medias.push(contents)
    //        } else {
    //            files.push(("URL", file_url, contents));
    //        }
    //    }
    //
    //    for protocol_path in protocol_paths {
    //        let documents = load_protocol_path(loaders, &protocol_path)
    //            .with_context(|| format!("Failed to load from '{protocol_path}'"))?;
    //        files.extend(
    //            documents
    //                .into_iter()
    //                .map(|document| ("FROM", document.path, document.contents)),
    //        );
    //    }
    //
    //    Ok((files, medias, data_urls))
    // }
    public static TupleN loadDocuments(Map<String, String> loaders, Set<String> localPaths, Set<String> remoteUrls, Set<String> externalCmds, Set<String> protocolPaths) {
        List<Triple<String, String, String>> files = new ArrayList<>();
        List<String> medias = new ArrayList<>();
        Map<String, String> dataUrls = new HashMap<>();

        for (String cmd : externalCmds) {
            String output = Command.execute(cmd).stderrToStdout();
            files.add(new Triple<>("CMD", cmd, output));
        }

        Set<String> localFiles = expandGlobPaths(localPaths, true);
        for (String filePath : localFiles) {
            if (isImage(filePath)) {

            }
        }
    }

    // fn is_image(path: &str) -> bool {
    //    get_patch_extension(path)
    //        .map(|v| IMAGE_EXTS.contains(&v.as_str()))
    //        .unwrap_or_default()
    // }
    public static boolean isImage(String path) {
        return IMAGE_EXTS.contains(getPatchExtension(path));
    }

    // fn resolve_role(config: &Config, role: Option<Role>) -> (Role, bool, bool) {
    //    match role {
    //        Some(v) => (v, false, false),
    //        None => (
    //            config.extract_role(),
    //            config.session.is_some(),
    //            config.agent.is_some(),
    //        ),
    //    }
    // }
    private static Triple<Role, Boolean, Boolean> resolveRole(Config config, Role role) {
        if (role != null) {
            return new Triple<>(role, false, false);
        } else {
            return new Triple<>(config.extractRole(), config.getSession() != null, config.getAgent() != null);
        }
    }

    @Override
    public Input clone() {
        try {
            Input clone = (Input) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
