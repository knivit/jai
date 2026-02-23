package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.client.model.ProviderModels;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.config.role.RoleLike;
import com.tsoft.jai.core.Option;
import com.tsoft.jai.dirs.Dirs;
import com.tsoft.jai.env.Env;
import com.tsoft.jai.function.FunctionDeclaration;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.function.ToolResult;
import com.tsoft.jai.inquire.prompt.Confirm;
import com.tsoft.jai.inquire.prompt.Select;
import com.tsoft.jai.rag.DocumentId;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.render.markdown.RenderOptions;
import com.tsoft.jai.serde.serdejson.SerdeJson;
import com.tsoft.jai.serde.Value;
import com.tsoft.jai.serde.serdeyaml.SerdeYaml;
import com.tsoft.jai.std.Fs;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.base.Tuple;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.tsoft.jai.Main.CARGO_CRATE_NAME;
import static com.tsoft.jai.Main.CARGO_PKG_VERSION;
import static com.tsoft.jai.anyhow.Macros.anyhow;
import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.client.macros.Macros.createClientConfig;
import static com.tsoft.jai.client.macros.Macros.listModels;
import static com.tsoft.jai.client.mod.Mod.OPENAI_COMPATIBLE_PROVIDERS;
import static com.tsoft.jai.config.Mod.RAG_TEMPLATE;
import static com.tsoft.jai.inquire.Inquire.IS_STDOUT_TERMINAL;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.serde.Value.json;
import static com.tsoft.jai.std.Fs.*;
import static com.tsoft.jai.utils.Mod.getEnvName;
import static com.tsoft.jai.utils.Mod.normalizeEnvName;
import static com.tsoft.jai.utils.PathUtil.listFileNames;
import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.NumberUtils.parseInt;
import static com.tsoft.jai.utils.base.StringUtils.*;
import static com.tsoft.jai.utils.command.Command.editFile;

@Data
@Accessors(chain = true)
public class Config {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    //#[serde(default)]
    @JsonProperty("model")
    private String modelId;
    private Double temperature;
    private Double topP;

    private boolean dryRun = false;
    private boolean stream = true;
    private boolean save = false;
    private String keyBindings = "emacs";
    private String editor;
    private String wrap;
    private boolean wrapCode = false;

    private boolean functionCalling = true;
    private Map<String, String> mappingTools;
    private String useTools;

    private String replPrelude;
    private String cmdPrelude;
    private String agentPrelude;

    private boolean saveSession;
    private Integer compressThreshold = 4000;
    private String summarizePrompt;
    private String summaryPrompt;

    private String ragEmbeddingModel;
    private String ragRerankerModel;
    private Integer ragTopK = 5;
    private Integer ragChunkSize;
    private Integer ragChunkOverlap;
    private String ragTemplate;

    //#[serde(default)]
    private Map<String, String> documentLoaders;

    private boolean highlight = true;
    private String theme;
    private String leftPrompt;
    private String rightPrompt;

    private String serveAddr;
    private String userAgent;
    private boolean saveShellHistory = true;
    private String syncModelsUrl;

    private List<ClientConfig> clients = new ArrayList<>();

    //#[serde(skip)]
    @JsonIgnore
    private boolean macroFlag = false;
    //#[serde(skip)]
    @JsonIgnore
    private boolean infoFlag = false;
    //#[serde(skip)]
    @JsonIgnore
    private Map<String, String> agentVariables;

    //#[serde(skip)]
    @JsonIgnore
    private Model model;
    //#[serde(skip)]
    @JsonIgnore
    private Functions functions;
    //#[serde(skip)]
    @JsonIgnore
    private WorkingMode workingMode = WorkingMode.Cmd;
    //#[serde(skip)]
    @JsonIgnore
    @ToString.Exclude                   // a recursion through the Input field
    private LastMessage lastMessage;

    //#[serde(skip)]
    @JsonIgnore
    private Role role;
    //#[serde(skip)]
    @JsonIgnore
    private Session session;
    //#[serde(skip)]
    @JsonIgnore
    private Rag rag;
    //#[serde(skip)]
    @JsonIgnore
    private Agent agent;

    private static final String CLIENTS_FIELD = "clients";

    public static final String TEMP_ROLE_NAME = "%%";
    public static final String TEMP_RAG_NAME = "temp";
    public static final String TEMP_SESSION_NAME = "temp";

    private static final String CONFIG_FILE_NAME = "config.yaml";
    private static final String ROLES_DIR_NAME = "roles";
    private static final String MACROS_DIR_NAME = "macros";
    private static final String ENV_FILE_NAME = ".env";
    private static final String MESSAGES_FILE_NAME = "messages.md";
    private static final String SESSIONS_DIR_NAME = "sessions";
    private static final String RAGS_DIR_NAME = "rags";
    private static final String FUNCTIONS_DIR_NAME = "functions";
    private static final String FUNCTIONS_FILE_NAME = "functions.json";
    private static final String FUNCTIONS_BIN_DIR_NAME = "bin";
    private static final String AGENTS_DIR_NAME = "agents";

    // pub async fn init(working_mode: WorkingMode, info_flag: bool) -> Result<Self> {
    //    let config_path = Self::config_file();
    //    let mut config = if !config_path.exists() {
    //        match env::var(get_env_name("provider"))
    //            .ok()
    //            .or_else(|| env::var(get_env_name("platform")).ok())
    //        {
    //            Some(v) => Self::load_dynamic(&v)?,
    //            None => {
    //                if *IS_STDOUT_TERMINAL {
    //                    create_config_file(&config_path).await?;
    //                }
    //                Self::load_from_file(&config_path)?
    //            }
    //        }
    //    } else {
    //        Self::load_from_file(&config_path)?
    //    };
    //
    //    config.working_mode = working_mode;
    //    config.info_flag = info_flag;
    //
    //    let setup = |config: &mut Self| -> Result<()> {
    //        config.load_envs();
    //
    //        if let Some(wrap) = config.wrap.clone() {
    //            config.set_wrap(&wrap)?;
    //        }
    //
    //        config.load_functions()?;
    //
    //        config.setup_model()?;
    //        config.setup_document_loaders();
    //        config.setup_user_agent();
    //        Ok(())
    //    };
    //    let ret = setup(&mut config);
    //    if !info_flag {
    //        ret?;
    //    }
    //    Ok(config)
    // }
    public static Result<Config> init(WorkingMode workingMode, boolean infoFlag) {
        Path configPath = configFile();

        Config config = null;
        if (!Files.exists(configPath)) {
            Option<String> var = Env.var(getEnvName("provider"))
                .ok()
                .orElse(() -> Env.var(getEnvName("platform")).ok());

            switch (var.getType()) {
                case Some -> {
                    Result<Config> res = loadDynamic(var.getValue());
                    if (isErr(res)) {
                        return Err(res);
                    }
                    config = res.getValue();
                }
                case None -> {
                    if (IS_STDOUT_TERMINAL) {
                        Result<?> res = createConfigFile(configPath);
                        if (isErr(res)) {
                            return Err(res);
                        }
                    }
                    Result<Config> ret = loadFromFile(configPath);
                    if (isErr(ret)) {
                        return Err(ret);
                    }
                    config = ret.getValue();
                }
            }
        } else {
            Result<Config> ret = loadFromFile(configPath);
            if (isErr(ret)) {
                return Err(ret);
            }
            config = ret.getValue();
        }

        config.setWorkingMode(workingMode);
        config.setInfoFlag(infoFlag);

        config.loadEnvs();

        if (!isBlank(config.getWrap())) {
            config.initWrap(config.getWrap());
        }

        config.loadFunctions();

        Result<?> res = config.setupModel();
        if (isErr(res)) {
            return Err(res);
        }
        config.setupDocumentLoaders();
        config.setupUserAgent();

        if (!infoFlag) {
            return Ok(config);
        }

        return Ok(config);
    }

    // pub fn config_dir() -> PathBuf {
    //    if let Ok(v) = env::var(get_env_name("config_dir")) {
    //        PathBuf::from(v)
    //    } else if let Ok(v) = env::var("XDG_CONFIG_HOME") {
    //        PathBuf::from(v).join(env!("CARGO_CRATE_NAME"))
    //    } else {
    //        let dir = dirs::config_dir().expect("No user's config directory");
    //        dir.join(env!("CARGO_CRATE_NAME"))
    //    }
    // }
    public static Path configDir() {
        Result<String> var = Env.var(getEnvName("config_dir"));
        if (isOk(var)) {
            return Path.of(var.getValue());
        } else {
            Path dir = Dirs.configDir().expect("No user's config directory");
            return dir.resolve(CARGO_CRATE_NAME);
        }
    }

    // pub fn local_path(name: &str) -> PathBuf {
    //    Self::config_dir().join(name)
    // }
    public static Path localPath(String name) {
        return configDir().resolve(name);
    }

    // pub fn config_file() -> PathBuf {
    //    match env::var(get_env_name("config_file")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(CONFIG_FILE_NAME),
    //    }
    // }
    private static Path configFile() {
        Result<String> var = Env.var(getEnvName("config_file"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(CONFIG_FILE_NAME);
        };
    }

    // pub async fn search_rag(
    //     config: &GlobalConfig,
    //     rag: &Rag,
    //     text: &str,
    //     abort_signal: AbortSignal,
    // ) -> Result<String> {
    //     let (reranker_model, top_k) = rag.get_config();
    //     let (embeddings, ids) = rag
    //         .search(text, top_k, reranker_model.as_deref(), abort_signal)
    //         .await?;
    //     let text = config.read().rag_template(&embeddings, text);
    //     rag.set_last_sources(&ids);
    //     Ok(text)
    // }
    public static Result<String> searchRag(Config config, Rag rag, String text, AbortSignal abortSignal) {
        Tuple<String, Integer> tuple = rag.getConfig();
        String rerankerModel = tuple.first();
        Integer topK = tuple.second();
        Result<Tuple<String, List<DocumentId>>> res = rag.search(text, topK, rerankerModel, abortSignal);
        if (isErr(res)) {
            return Err(res);
        }
        String embeddings = res.getValue().first();
        List<DocumentId> ids = res.getValue().second();
        text = config.ragTemplate(embeddings, text);
        rag.setLastSources(ids);
        return Ok(text);
    }

    // pub fn maybe_autoname_session(config: GlobalConfig) {
    //    let mut need_autoname = false;
    //    if let Some(session) = config.write().session.as_mut() {
    //        if session.need_autoname() {
    //            session.set_autonaming(true);
    //            need_autoname = true;
    //        }
    //    }
    //    if !need_autoname {
    //        return;
    //    }
    //    let color = if config.read().light_theme() {
    //        nu_ansi_term::Color::LightGray
    //    } else {
    //        nu_ansi_term::Color::DarkGray
    //    };
    //    print!("\n📢 {}\n", color.italic().paint("Autonaming the session."),);
    //    tokio::spawn(async move {
    //        if let Err(err) = Config::autoname_session(&config).await {
    //            warn!("Failed to autonaming the session: {err}");
    //        }
    //        if let Some(session) = config.write().session.as_mut() {
    //            session.set_autonaming(false);
    //        }
    //    });
    // }
    public static void maybeAutonameSession(Config config) {

    }

    // pub fn maybe_compress_session(config: GlobalConfig) {
    //    let mut need_compress = false;
    //    {
    //        let mut config = config.write();
    //        let compress_threshold = config.compress_threshold;
    //        if let Some(session) = config.session.as_mut() {
    //            if session.need_compress(compress_threshold) {
    //                session.set_compressing(true);
    //                need_compress = true;
    //            }
    //        }
    //    };
    //    if !need_compress {
    //        return;
    //    }
    //    let color = if config.read().light_theme() {
    //        nu_ansi_term::Color::LightGray
    //    } else {
    //        nu_ansi_term::Color::DarkGray
    //    };
    //    print!(
    //        "\n📢 {}\n",
    //        color.italic().paint("Compressing the session."),
    //    );
    //    tokio::spawn(async move {
    //        if let Err(err) = Config::compress_session(&config).await {
    //            warn!("Failed to compress the session: {err}");
    //        }
    //        if let Some(session) = config.write().session.as_mut() {
    //            session.set_compressing(false);
    //        }
    //    });
    // }
    public static void maybeCompressSession(Config config) {

    }

    // pub fn list_roles(with_builtin: bool) -> Vec<String> {
    //    let mut names = HashSet::new();
    //    if let Ok(rd) = read_dir(Self::roles_dir()) {
    //        for entry in rd.flatten() {
    //            if let Some(name) = entry
    //                .file_name()
    //                .to_str()
    //                .and_then(|v| v.strip_suffix(".md"))
    //            {
    //                names.insert(name.to_string());
    //            }
    //        }
    //    }
    //    if with_builtin {
    //        names.extend(Role::list_builtin_role_names());
    //    }
    //    let mut names: Vec<_> = names.into_iter().collect();
    //    names.sort_unstable();
    //    names
    // }
    public List<String> listRoles(boolean withBuiltin) {
        Set<String> names = new HashSet<>();
        Result<List<File>> rd = readDir(rolesDir());
        if (isOk(rd)) {
            rd.getValue().stream()
                .map(File::getName)
                .filter(e -> e.endsWith(".md"))
                .map(e -> e.substring(0, e.length() - 3))
                .forEach(names::add);
        }

        if (withBuiltin) {
            names.addAll(Role.listBuiltinRoleNames());
        }
        List<String> roles = new ArrayList<>(names);
        roles.sort(String::compareToIgnoreCase);
        return roles;
    }

    // pub fn has_role(name: &str) -> bool {
    //    let names = Self::list_roles(true);
    //    names.contains(&name.to_string())
    // }
    public boolean hasRole(String name) {
        List<String> names = listRoles(true);
        return names.contains(name);
    }

    // pub fn new_role(&mut self, name: &str) -> Result<()> {
    //    if self.macro_flag {
    //        bail!("No role");
    //    }
    //    let ans = Confirm::new("Create a new role?")
    //        .with_default(true)
    //        .prompt()?;
    //    if ans {
    //        self.upsert_role(name)?;
    //    } else {
    //        bail!("No role");
    //    }
    //    Ok(())
    // }
    public Result<?> newRole(String name) {
        if (macroFlag) {
            return bail("No role");
        }
        Result<Boolean> res = new Confirm("Create a new role?").setDefaultValue(true).prompt();
        if (isErr(res)) {
            return Err(res);
        }
        boolean ans = res.getValue();
        if (ans) {
            upsertRole(name);
        } else {
            return bail("No role");
        }
        return Ok();
    }

    // pub fn upsert_role(&mut self, name: &str) -> Result<()> {
    //    let role_path = Self::role_file(name);
    //    ensure_parent_exists(&role_path)?;
    //    let editor = self.editor()?;
    //    edit_file(&editor, &role_path)?;
    //    if self.working_mode.is_repl() {
    //        println!("✓ Saved the role to '{}'.", role_path.display());
    //    }
    //    Ok(())
    // }
    public Result<?> upsertRole(String name) {
        Path rolePath = roleFile(name);
        Result<?> res = ensureParentExists(rolePath);
        if (isErr(res)) {
            return Err(res);
        }
        Result<?> ree = editFile(editor, rolePath);
        if (isErr(ree)) {
            return Err(ree);
        }
        if (WorkingMode.Repl.equals(workingMode)) {
            println("✓ Saved the role to '{}'.", rolePath);
        }
        return Ok();
    }

    // pub fn roles_dir() -> PathBuf {
    //    match env::var(get_env_name("roles_dir")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(ROLES_DIR_NAME),
    //    }
    // }
    public Path rolesDir() {
        Result<String> var = Env.var(getEnvName("roles_dir"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(ROLES_DIR_NAME);
        };
    }

    // pub fn macros_dir() -> PathBuf {
    //    match env::var(get_env_name("macros_dir")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(MACROS_DIR_NAME),
    //    }
    // }
    public Path macrosDir() {
        Result<String> var = Env.var(getEnvName("macros_dir"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(MACROS_DIR_NAME);
        };
    }

    // pub fn functions_dir() -> PathBuf {
    //    match env::var(get_env_name("functions_dir")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(FUNCTIONS_DIR_NAME),
    //    }
    // }
    public Path functionsDir() {
        Result<String> var = Env.var(getEnvName("functions_dir"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(FUNCTIONS_DIR_NAME);
        };
    }

    // pub fn agents_data_dir() -> PathBuf {
    //    Self::local_path(AGENTS_DIR_NAME)
    // }
    public Path agentsDataDir() {
        return localPath(AGENTS_DIR_NAME);
    }

    // pub fn agent_data_dir(name: &str) -> PathBuf {
    //    match env::var(format!("{}_DATA_DIR", normalize_env_name(name))) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::agents_data_dir().join(name),
    //    }
    // }
    public Path agentDataDir(String name) {
        Result<String> var = Env.var(format("{}_DATA_DIR", normalizeEnvName(name)));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> agentsDataDir().resolve(name);
        };
    }

    // pub fn agents_functions_dir() -> PathBuf {
    //    Self::functions_dir().join(AGENTS_DIR_NAME)
    // }
    public Path agentsFunctionsDir() {
        return functionsDir().resolve(AGENTS_DIR_NAME);
    }

    // pub fn agent_functions_dir(name: &str) -> PathBuf {
    //    match env::var(format!("{}_FUNCTIONS_DIR", normalize_env_name(name))) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::agents_functions_dir().join(name),
    //    }
    // }
    public Path agentFunctionsDir(String name) {
        Result<String> var = Env.var(format("{}_FUNCTIONS_DIR", normalizeEnvName(name)));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> agentsFunctionsDir().resolve(name);
        };
    }

    // pub fn models_override_file() -> PathBuf {
    //    Self::local_path("models-override.yaml")
    // }
    public static Path modelsOverrideFile() {
        return localPath("models-override.yaml");
    }

    // pub fn agent_rag_file(agent_name: &str, rag_name: &str) -> PathBuf {
    //    Self::agent_data_dir(agent_name).join(format!("{rag_name}.yaml"))
    // }
    public Path agentRagFile(String agentName, String ragName) {
        return agentDataDir(agentName).resolve(format("{}.yaml", ragName));
    }

    //  pub fn agent_config_file(name: &str) -> PathBuf {
    //     match env::var(format!("{}_CONFIG_FILE", normalize_env_name(name))) {
    //         Ok(value) => PathBuf::from(value),
    //         Err(_) => Self::agent_data_dir(name).join(CONFIG_FILE_NAME),
    //     }
    // }
    public Path agentConfigFile(String name) {
        Result<String> var = Env.var(format("{}_CONFIG_FILE", normalizeEnvName(name)));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> agentDataDir(name).resolve(name);
        };
    }

    // pub fn rags_dir() -> PathBuf {
    //    match env::var(get_env_name("rags_dir")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(RAGS_DIR_NAME),
    //    }
    // }
    public Path ragsDir() {
        Result<String> var = Env.var(getEnvName("rags_dir"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(RAGS_DIR_NAME);
        };
    }

    // pub fn sessions_dir(&self) -> PathBuf {
    //    match &self.agent {
    //        None => match env::var(get_env_name("sessions_dir")) {
    //            Ok(value) => PathBuf::from(value),
    //            Err(_) => Self::local_path(SESSIONS_DIR_NAME),
    //        },
    //        Some(agent) => Self::agent_data_dir(agent.name()).join(SESSIONS_DIR_NAME),
    //    }
    // }
    public Path sessionsDir() {
        if (agent == null) {
            Result<String> var = Env.var(getEnvName("sessions_dir"));
            return switch (var.getType()) {
                case Ok -> Paths.get(var.getValue());
                case Err -> localPath(SESSIONS_DIR_NAME);
            };
        } else {
            return agentDataDir(agent.getName()).resolve(SESSIONS_DIR_NAME);
        }
    }

    // pub fn session_file(&self, name: &str) -> PathBuf {
    //    match name.split_once("/") {
    //        Some((dir, name)) => self.sessions_dir().join(dir).join(format!("{name}.yaml")),
    //        None => self.sessions_dir().join(format!("{name}.yaml")),
    //    }
    // }
    public Path sessionFile(String sessionName) {
        Tuple<String, String> tuple = splitOnce(sessionName, '/');
        String dir = tuple.first();
        String name = tuple.second();
        if (!isEmpty(dir) && !isEmpty(name)) {
            return sessionsDir().resolve(dir).resolve(format("{}.yaml", name));
        } else {
            return sessionsDir().resolve(format("{}.yaml", sessionName));
        }
    }

    // pub fn role_file(name: &str) -> PathBuf {
    //    Self::roles_dir().join(format!("{name}.md"))
    // }
    public Path roleFile(String name) {
        return rolesDir().resolve(format("{}.md", name));
    }

    // pub fn env_file() -> PathBuf {
    //    match env::var(get_env_name("env_file")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(ENV_FILE_NAME),
    //    }
    // }
    public Path envFile() {
        Result<String> var = Env.var(getEnvName("env_file"));
        return switch (var.getType()) {
            case Ok -> Paths.get(var.getValue());
            case Err -> localPath(ENV_FILE_NAME);
        };
    }

    // pub fn messages_file(&self) -> PathBuf {
    //    match &self.agent {
    //        None => match env::var(get_env_name("messages_file")) {
    //            Ok(value) => PathBuf::from(value),
    //            Err(_) => Self::local_path(MESSAGES_FILE_NAME),
    //        },
    //        Some(agent) => Self::agent_data_dir(agent.name()).join(MESSAGES_FILE_NAME),
    //    }
    // }
    public Path messagesFile() {
        if (agent == null) {
            Result<String> var = Env.var(getEnvName("messages_file"));
            return switch (var.getType()) {
                case Ok -> Paths.get(var.getValue());
                case Err -> localPath(MESSAGES_FILE_NAME);
            };
        } else {
            return agentDataDir(agent.getName()).resolve(MESSAGES_FILE_NAME);
        }
    }

    // pub fn rag_file(&self, name: &str) -> PathBuf {
    //    match &self.agent {
    //        Some(agent) => Self::agent_rag_file(agent.name(), name),
    //        None => Self::rags_dir().join(format!("{name}.yaml")),
    //    }
    // }
    public Path ragFile(String name) {
        if (agent != null) {
            return agentRagFile(agent.getName(), name);
        } else {
            return ragsDir().resolve(format("{}.yaml", name));
        }
    }

    // pub fn list_rags() -> Vec<String> {
    //    match read_dir(Self::rags_dir()) {
    //        Ok(rd) => {
    //            let mut names = vec![];
    //            for entry in rd.flatten() {
    //                let name = entry.file_name();
    //                if let Some(name) = name.to_string_lossy().strip_suffix(".yaml") {
    //                    names.push(name.to_string());
    //                }
    //            }
    //            names.sort_unstable();
    //            names
    //        }
    //        Err(_) => vec![],
    //    }
    // }
    public List<String> listRags() {
        Result<List<File>> res = readDir(ragsDir());
        return switch (res.getType()) {
            case Ok -> {
                List<String> names = new ArrayList<>(
                    res.getValue().stream()
                        .map(File::getName)
                        .filter(e -> e.endsWith(".yaml"))
                        .map(e -> e.substring(0, e.length() - 5))
                        .toList());
                names.sort(String::compareToIgnoreCase);
                yield names;
            }
            case Err -> Collections.emptyList();
        };
    }

    // pub fn list_macros() -> Vec<String> {
    //    list_file_names(Self::macros_dir(), ".yaml")
    // }
    public List<String> listMacros() {
        return listFileNames(macrosDir(), ".yaml");
    }

    // pub fn list_sessions(&self) -> Vec<String> {
    //    list_file_names(self.sessions_dir(), ".yaml")
    // }
    public List<String> listSessions() {
        return listFileNames(sessionsDir(), ".yaml");
    }

    //  pub async fn use_agent(
    //     config: &GlobalConfig,
    //     agent_name: &str,
    //     session_name: Option<&str>,
    //     abort_signal: AbortSignal,
    // ) -> Result<()> {
    //     if !config.read().function_calling {
    //         bail!("Please enable function calling before using the agent.");
    //     }
    //     if config.read().agent.is_some() {
    //         bail!("Already in a agent, please run '.exit agent' first to exit the current agent.");
    //     }
    //     let agent = Agent::init(config, agent_name, abort_signal).await?;
    //     let session = session_name.map(|v| v.to_string()).or_else(|| {
    //         if config.read().macro_flag {
    //             None
    //         } else {
    //             agent.agent_prelude().map(|v| v.to_string())
    //         }
    //     });
    //     config.write().rag = agent.rag();
    //     config.write().agent = Some(agent);
    //     if let Some(session) = session {
    //         config.write().use_session(Some(&session))?;
    //     } else {
    //         config.write().init_agent_shared_variables()?;
    //     }
    //     Ok(())
    // }
    public Result<?> useAgent(String agentName, String sessionName, AbortSignal abortSignal) {
        if (!isFunctionCalling()) {
            return bail("Please enable function calling before using the agent.");
        }
        if (agent != null) {
            return bail("Already in a agent, please run '.exit agent' first to exit the current agent.");
        }

        Result<Agent> res = Agent.init(this, agentName, abortSignal);
        if (isErr(res)) {
            return Err(res);
        }
        Agent agent = res.getValue();

        String session = sessionName;
        if (isBlank(session)) {
            if (!isMacroFlag()) {
                session = agent.agentPrelude();
            }
        }

        rag = agent.getRag();
        this.agent = agent;
        if (!isBlank(session)) {
            Result<?> ret = useSession(session);
            if (isErr(ret)) {
                return Err(ret);
            }
        } else {
            Result<?> ret = initAgentSharedVariables();
            if (isErr(ret)) {
                return Err(ret);
            }
        }

        return Ok();
    }

    // pub fn current_model(&self) -> &Model {
    //    if let Some(session) = self.session.as_ref() {
    //        session.model()
    //    } else if let Some(agent) = self.agent.as_ref() {
    //        agent.model()
    //    } else if let Some(role) = self.role.as_ref() {
    //        role.model()
    //    } else {
    //        &self.model
    //    }
    // }
    public Model currentModel() {
        if (session != null) {
            return session.getModel();
        }
        if (agent != null) {
            return agent.getModel();
        }
        if (role != null) {
            return role.getModel();
        }
        return model;
    }

    // pub fn role_like_mut(&mut self) -> Option<&mut dyn RoleLike> {
    //    if let Some(session) = self.session.as_mut() {
    //        Some(session)
    //    } else if let Some(agent) = self.agent.as_mut() {
    //        Some(agent)
    //    } else if let Some(role) = self.role.as_mut() {
    //        Some(role)
    //    } else {
    //        None
    //    }
    // }
    public RoleLike roleLikeMut() {
        if (session != null) {
            return new RoleLike(session);
        } else if (agent != null) {
            return new RoleLike(agent);
        } else if (role != null) {
            return new RoleLike(role);
        }
        return null;
    }

    // pub fn extract_role(&self) -> Role {
    //    if let Some(session) = self.session.as_ref() {
    //        session.to_role()
    //    } else if let Some(agent) = self.agent.as_ref() {
    //        agent.to_role()
    //    } else if let Some(role) = self.role.as_ref() {
    //        role.clone()
    //    } else {
    //        let mut role = Role::default();
    //        role.batch_set(
    //            &self.model,
    //            self.temperature,
    //            self.top_p,
    //            self.use_tools.clone(),
    //        );
    //        role
    //    }
    // }
    public Role extractRole() {
        if (session != null) {
            return session.toRole();
        }
        if (agent != null) {
            return agent.toRole();
        }
        if (role != null) {
            return role;
        }

        Role role = new Role();
        role.batchSet(model, temperature, topP, useTools);
        return role;
    }

    // pub fn use_prompt(&mut self, prompt: &str) -> Result<()> {
    //    let mut role = Role::new(TEMP_ROLE_NAME, prompt);
    //    role.set_model(self.current_model().clone());
    //    self.use_role_obj(role)
    // }
    public Result<?> usePrompt(String prompt) {
        Role role = Role.create(TEMP_ROLE_NAME, prompt);
        role.setModel(currentModel());
        return useRoleObj(role);
    }

    // pub fn use_role(&mut self, name: &str) -> Result<()> {
    //    let role = self.retrieve_role(name)?;
    //    self.use_role_obj(role)
    // }
    public Result<?> useRole(String name) {
        Result<Role> role = retrieveRole(name);
        if (isErr(role)) {
            return Err(role);
        }
        return useRoleObj(role.getValue());
    }

    // pub fn use_role_obj(&mut self, role: Role) -> Result<()> {
    //    if self.agent.is_some() {
    //        bail!("Cannot perform this operation because you are using a agent")
    //    }
    //    if let Some(session) = self.session.as_mut() {
    //        session.guard_empty()?;
    //        session.set_role(role);
    //    } else {
    //        self.role = Some(role);
    //    }
    //    Ok(())
    // }
    private Result<?> useRoleObj(Role role) {
        if (agent != null) {
            return bail("Cannot perform this operation because you are using a agent");
        }
        if (session != null) {
            session.guardEmpty();
            session.setRole(role);
        } else {
            this.role = role;
        }
        return Ok();
    }

    // pub fn role_info(&self) -> Result<String> {
    //    if let Some(session) = &self.session {
    //        if session.role_name().is_some() {
    //            let role = session.to_role();
    //            Ok(role.export())
    //        } else {
    //            bail!("No session role")
    //        }
    //    } else if let Some(role) = &self.role {
    //        Ok(role.export())
    //    } else {
    //        bail!("No role")
    //    }
    // }
    public Result<String> roleInfo() {
        if (session != null) {
            if (!isBlank(session.getRoleName())) {
                Role role = session.toRole();
                return Ok(role.export());
            } else {
                return bail("No session role");
            }
        } else if (role != null) {
            return Ok(role.export());
        } else {
            return bail("No role");
        }
    }

    // pub fn save_role(&mut self, name: Option<&str>) -> Result<()> {
    //    let mut role_name = match &self.role {
    //        Some(role) => {
    //            if role.has_args() {
    //                bail!("Unable to save the role with arguments (whose name contains '#')")
    //            }
    //            match name {
    //                Some(v) => v.to_string(),
    //                None => role.name().to_string(),
    //            }
    //        }
    //        None => bail!("No role"),
    //    };
    //    if role_name == TEMP_ROLE_NAME {
    //        role_name = Text::new("Role name:")
    //            .with_validator(|input: &str| {
    //                let input = input.trim();
    //                if input.is_empty() {
    //                    Ok(Validation::Invalid("This name is required".into()))
    //                } else if input == TEMP_ROLE_NAME {
    //                    Ok(Validation::Invalid("This name is reserved".into()))
    //                } else {
    //                    Ok(Validation::Valid)
    //                }
    //            })
    //            .prompt()?;
    //    }
    //    let role_path = Self::role_file(&role_name);
    //    if let Some(role) = self.role.as_mut() {
    //        role.save(&role_name, &role_path, self.working_mode.is_repl())?;
    //    }
    //
    //    Ok(())
    // }
    public Result<?> saveRole(String name) {
        String roleName;

        return Ok();
    }

    // pub fn use_session(&mut self, session_name: Option<&str>) -> Result<()> {
    //    if self.session.is_some() {
    //        bail!(
    //            "Already in a session, please run '.exit session' first to exit the current session."
    //        );
    //    }
    //    let mut session;
    //    match session_name {
    //        None | Some(TEMP_SESSION_NAME) => {
    //            let session_file = self.session_file(TEMP_SESSION_NAME);
    //            if session_file.exists() {
    //                remove_file(session_file).with_context(|| {
    //                    format!("Failed to cleanup previous '{TEMP_SESSION_NAME}' session")
    //                })?;
    //            }
    //            session = Some(Session::new(self, TEMP_SESSION_NAME));
    //        }
    //        Some(name) => {
    //            let session_path = self.session_file(name);
    //            if !session_path.exists() {
    //                session = Some(Session::new(self, name));
    //            } else {
    //                session = Some(Session::load(self, name, &session_path)?);
    //            }
    //        }
    //    }
    //    let mut new_session = false;
    //    if let Some(session) = session.as_mut() {
    //        if session.is_empty() {
    //            new_session = true;
    //            if let Some(LastMessage {
    //                input,
    //                output,
    //                continuous,
    //            }) = &self.last_message
    //            {
    //                if (*continuous && !output.is_empty())
    //                    && self.agent.is_some() == input.with_agent()
    //                {
    //                    let ans = Confirm::new(
    //                        "Start a session that incorporates the last question and answer?",
    //                    )
    //                    .with_default(false)
    //                    .prompt()?;
    //                    if ans {
    //                        session.add_message(input, output)?;
    //                    }
    //                }
    //            }
    //        }
    //    }
    //    self.session = session;
    //    self.init_agent_session_variables(new_session)?;
    //    Ok(())
    // }
    public Result<?> useSession(String sessionName) {
        if (session != null) {
            return bail("Already in a session, please run '.exit session' first to exit the current session.");
        }

        if (isBlank(sessionName) || TEMP_SESSION_NAME.equals(sessionName)) {
            Path sessionFile = sessionFile(TEMP_SESSION_NAME);
            if (Files.exists(sessionFile)) {
                Result<?> res = removeFile(sessionFile).withContext(() -> format("Failed to cleanup previous '{}' session", TEMP_SESSION_NAME));
                if (isErr(res)) {
                    return Err(res);
                }
            }
            session = Session.create(this, TEMP_SESSION_NAME);
        } else {
            Path sessionFile = sessionFile(sessionName);
            if (!Files.exists(sessionFile)) {
                session = Session.create(this, sessionName);
            } else {
                Result<Session> res = Session.load(this, sessionName, sessionFile);
                if (isErr(res)) {
                    return Err(res);
                }
                session = res.getValue();
            }
        }

        boolean newSession = false;
        if (session.isEmpty()) {
            newSession = true;
            if (lastMessage != null) {
                Input input = lastMessage.getInput();
                String output = lastMessage.getOutput();
                boolean continuous = lastMessage.isContinuous();
                if (continuous && !isBlank(output)
                    && ((agent != null && input.isWithAgent()) || (agent == null && !input.isWithAgent()))) {
                    Result<Boolean> res = new Confirm("Start a session that incorporates the last question and answer?")
                        .setDefaultValue(false)
                        .prompt();
                    if (isErr(res)) {
                        return Err(res);
                    }
                    boolean ans = res.getValue();
                    if (ans) {
                        Result<?> ret = session.addMessage(input, output);
                        if (isErr(ret)) {
                            return Err(ret);
                        }
                    }
                }
            }
        }

        Result<?> res = initAgentSessionVariables(newSession);
        if (isErr(res)) {
            return Err(res);
        }
        return Ok();
    }

    // pub fn session_info(&self) -> Result<String> {
    //    if let Some(session) = &self.session {
    //        let render_options = self.render_options()?;
    //        let mut markdown_render = MarkdownRender::init(render_options)?;
    //        let agent_info: Option<(String, Vec<String>)> = self.agent.as_ref().map(|agent| {
    //            let functions = agent
    //                .functions()
    //                .declarations()
    //                .iter()
    //                .filter_map(|v| if v.agent { Some(v.name.clone()) } else { None })
    //                .collect();
    //            (agent.name().to_string(), functions)
    //        });
    //        session.render(&mut markdown_render, &agent_info)
    //    } else {
    //        bail!("No session")
    //    }
    // }
    public Result<String> sessionInfo() {
        if (session != null) {
            RenderOptions renderOptions = renderOptions();
            return null;
        } else {
            return bail("No session");
        }
    }

    // pub fn save_session(&mut self, name: Option<&str>) -> Result<()> {
    //    let session_name = match &self.session {
    //        Some(session) => match name {
    //            Some(v) => v.to_string(),
    //            None => session
    //                .autoname()
    //                .unwrap_or_else(|| session.name())
    //                .to_string(),
    //        },
    //        None => bail!("No session"),
    //    };
    //    let session_path = self.session_file(&session_name);
    //    if let Some(session) = self.session.as_mut() {
    //        session.save(&session_name, &session_path, self.working_mode.is_repl())?;
    //    }
    //    Ok(())
    // }
    public Result<?> saveSession(String name) {
        String sessionName;
        if (session != null) {
            if (!isBlank(name)) {
                sessionName = name;
            } else {
                sessionName = session.autoname();
                if (isBlank(sessionName)) {
                    sessionName = session.getName();
                }
            }
        } else {
            return bail("No session");
        }
        Path sessionPath = sessionFile(sessionName);
        if (session != null) {
            Result<?> res = session.save(sessionName, sessionPath, WorkingMode.isRepl(workingMode));
            if (isErr(res)) {
                return Err(res);
            }
        }
        return Ok();
    }

    // pub async fn use_rag(
    //    config: &GlobalConfig,
    //    rag: Option<&str>,
    //    abort_signal: AbortSignal,
    // ) -> Result<()> {
    //    if config.read().agent.is_some() {
    //        bail!("Cannot perform this operation because you are using a agent")
    //    }
    //    let rag = match rag {
    //        None => {
    //            let rag_path = config.read().rag_file(TEMP_RAG_NAME);
    //            if rag_path.exists() {
    //                remove_file(&rag_path).with_context(|| {
    //                    format!("Failed to cleanup previous '{TEMP_RAG_NAME}' rag")
    //                })?;
    //            }
    //            Rag::init(config, TEMP_RAG_NAME, &rag_path, &[], abort_signal).await?
    //        }
    //        Some(name) => {
    //            let rag_path = config.read().rag_file(name);
    //            if !rag_path.exists() {
    //                if config.read().working_mode.is_cmd() {
    //                    bail!("Unknown RAG '{name}'")
    //                }
    //                Rag::init(config, name, &rag_path, &[], abort_signal).await?
    //            } else {
    //                Rag::load(config, name, &rag_path)?
    //            }
    //        }
    //    };
    //    config.write().rag = Some(Arc::new(rag));
    //    Ok(())
    // }
    public Result<?> useRag(String name, AbortSignal abortSignal) {
        if (agent != null) {
            return bail("Cannot perform this operation because you are using a agent");
        }

        Rag rag;
        if (isBlank(name)) {
            Path ragPath = ragFile(TEMP_RAG_NAME);
            if (Files.exists(ragPath)) {
                Result<?> res = removeFile(ragPath).withContext(() -> format("Failed to cleanup previous '{}' rag", TEMP_RAG_NAME));
                if (isErr(res)) {
                    return Err(res);
                }
            }
            Result<Rag> res = Rag.init(this, TEMP_RAG_NAME, ragPath, Collections.emptyList(), abortSignal);
            if (isErr(res)) {
                return Err(res);
            }
            rag = res.getValue();
        } else {
            Path ragPath = ragFile(name);
            if (!Files.exists(ragPath)) {
                if (WorkingMode.isCmd(workingMode)) {
                    return bail("Unknown RAG '{}'", name);
                }
                Result<Rag> res = Rag.init(this, name, ragPath, Collections.emptyList(), abortSignal);
                if (isErr(res)) {
                    return Err(res);
                }
                rag = res.getValue();
            } else {
                Result<Rag> res = Rag.load(this, name, ragPath);
                if (isErr(res)) {
                    return Err(res);
                }
                rag = res.getValue();
            }
        }

        this.rag = rag;
        return Ok();
    }

    // pub fn rag_template(&self, embeddings: &str, text: &str) -> String {
    //    if embeddings.is_empty() {
    //        return text.to_string();
    //    }
    //    self.rag_template
    //        .as_deref()
    //        .unwrap_or(RAG_TEMPLATE)
    //        .replace("__CONTEXT__", embeddings)
    //        .replace("__INPUT__", text)
    // }
    public String ragTemplate(String embeddings, String text) {
        if (isBlank(embeddings)) {
            return text;
        }
        return new Option<>(ragTemplate)
            .unwrapOr(RAG_TEMPLATE)
            .replace("__CONTEXT__", embeddings)
            .replace("__INPUT__", text);
    }

    // pub fn rag_info(&self) -> Result<String> {
    //    if let Some(rag) = &self.rag {
    //        rag.export()
    //    } else {
    //        bail!("No RAG")
    //    }
    // }
    public Result<String> ragInfo() {
        if (rag != null) {
            return rag.export();
        } else {
            return bail("No RAG");
        }
    }

    // pub fn state(&self) -> StateFlags {
    //    let mut flags = StateFlags::empty();
    //    if let Some(session) = &self.session {
    //        if session.is_empty() {
    //            flags |= StateFlags::SESSION_EMPTY;
    //        } else {
    //            flags |= StateFlags::SESSION;
    //        }
    //        if session.role_name().is_some() {
    //            flags |= StateFlags::ROLE;
    //        }
    //    } else if self.role.is_some() {
    //        flags |= StateFlags::ROLE;
    //    }
    //    if self.agent.is_some() {
    //        flags |= StateFlags::AGENT;
    //    }
    //    if self.rag.is_some() {
    //        flags |= StateFlags::RAG;
    //    }
    //    flags
    // }
    public int state() {
        int flags = StateFlags.EMPTY;
        if (session != null) {
            if (session.isEmpty()) {
                flags |= StateFlags.SESSION_EMPTY;
            } else {
                flags |= StateFlags.SESSION;
            }
            if (!isBlank(session.getRoleName())) {
                flags |= StateFlags.ROLE;
            }
        } else if (role != null) {
            flags |= StateFlags.ROLE;
        }
        if (agent != null) {
            flags |= StateFlags.AGENT;
        }
        if (rag != null) {
            flags |= StateFlags.RAG;
        }
        return flags;
    }

    // fn init_agent_shared_variables(&mut self) -> Result<()> {
    //    let agent = match self.agent.as_mut() {
    //        Some(v) => v,
    //        None => return Ok(()),
    //    };
    //    if !agent.defined_variables().is_empty() && agent.shared_variables().is_empty() {
    //        let mut config_variables = agent.config_variables().clone();
    //        if let Some(v) = &self.agent_variables {
    //            config_variables.extend(v.clone());
    //        }
    //        let new_variables = Agent::init_agent_variables(
    //            agent.defined_variables(),
    //            &config_variables,
    //            self.info_flag,
    //        )?;
    //        agent.set_shared_variables(new_variables);
    //    }
    //    if !self.info_flag {
    //        agent.update_shared_dynamic_instructions(false)?;
    //    }
    //    Ok(())
    // }
    private Result<?> initAgentSharedVariables() {
        if (agent == null) {
            return Ok();
        }

        if (!isEmpty(agent.definedVariables()) && isEmpty(agent.getSharedVariables())) {
            Map<String, String> configVariables = new HashMap<>(agent.configVariables());
            if (!isEmpty(agentVariables)) {
                configVariables.putAll(agentVariables);
            }

            Result<Map<String, String>> res = Agent.initAgentVariables(agent.definedVariables(), configVariables, infoFlag);
            if (isErr(res)) {
                return Err(res);
            }
            Map<String, String> newVariables = res.getValue();
            agent.setSharedVariables(newVariables);
        }
        if (!infoFlag) {
            Result<?> res = agent.updateSharedDynamicInstructions(false);
            if (isErr(res)) {
                return Err(res);
            }
        }
        return Ok();
    }

    // pub fn set_model(&mut self, model_id: &str) -> Result<()> {
    //    let model = Model::retrieve_model(self, model_id, ModelType::Chat)?;
    //    match self.role_like_mut() {
    //        Some(role_like) => role_like.set_model(model),
    //        None => {
    //            self.model = model;
    //        }
    //    }
    //    Ok(())
    // }
    public Result<?> setModel(String modelId) {
        Result<Model> res = Model.retrieveModel(this, modelId, ModelType.Chat);
        if (isErr(res)) {
            return Err(res);
        }
        Model model = res.getValue();
        RoleLike roleLike = roleLikeMut();
        if (roleLike != null) {
            roleLike.setModel(model);
        } else{
            this.model = model;
        }
        return Ok();
    }

    // fn init_agent_session_variables(&mut self, new_session: bool) -> Result<()> {
    //    let (agent, session) = match (self.agent.as_mut(), self.session.as_mut()) {
    //        (Some(agent), Some(session)) => (agent, session),
    //        _ => return Ok(()),
    //    };
    //    if new_session {
    //        let shared_variables = agent.shared_variables().clone();
    //        let session_variables =
    //            if !agent.defined_variables().is_empty() && shared_variables.is_empty() {
    //                let mut config_variables = agent.config_variables().clone();
    //                if let Some(v) = &self.agent_variables {
    //                    config_variables.extend(v.clone());
    //                }
    //                let new_variables = Agent::init_agent_variables(
    //                    agent.defined_variables(),
    //                    &config_variables,
    //                    self.info_flag,
    //                )?;
    //                agent.set_shared_variables(new_variables.clone());
    //                new_variables
    //            } else {
    //                shared_variables
    //            };
    //        agent.set_session_variables(session_variables);
    //        if !self.info_flag {
    //            agent.update_session_dynamic_instructions(None)?;
    //        }
    //        session.sync_agent(agent);
    //    } else {
    //        let variables = session.agent_variables();
    //        agent.set_session_variables(variables.clone());
    //        agent.update_session_dynamic_instructions(Some(
    //            session.agent_instructions().to_string(),
    //        ))?;
    //    }
    //    Ok(())
    // }
    private Result<?> initAgentSessionVariables(boolean newSession) {
        return Ok();
    }

    // pub fn agent_info(&self) -> Result<String> {
    //    if let Some(agent) = &self.agent {
    //        agent.export()
    //    } else {
    //        bail!("No agent")
    //    }
    // }
    public Result<String> agentInfo() {
        if (agent != null) {
            return agent.export(this);
        } else {
            return bail("No agent");
        }
    }

    // pub fn retrieve_role(&self, name: &str) -> Result<Role> {
    //    let names = Self::list_roles(false);
    //    let mut role = if names.contains(&name.to_string()) {
    //        let path = Self::role_file(name);
    //        let content = read_to_string(&path)?;
    //        Role::new(name, &content)
    //    } else {
    //        Role::builtin(name)?
    //    };
    //    let current_model = self.current_model().clone();
    //    match role.model_id() {
    //        Some(model_id) => {
    //            if current_model.id() != model_id {
    //                let model = Model::retrieve_model(self, model_id, ModelType::Chat)?;
    //                role.set_model(model);
    //            } else {
    //                role.set_model(current_model);
    //            }
    //        }
    //        None => {
    //            role.set_model(current_model);
    //            if role.temperature().is_none() {
    //                role.set_temperature(self.temperature);
    //            }
    //            if role.top_p().is_none() {
    //                role.set_top_p(self.top_p);
    //            }
    //        }
    //    }
    //    Ok(role)
    // }
    public Result<Role> retrieveRole(String name) {
        List<String> names = listRoles(false);

        Role role;
        if (names.contains(name)) {
            Path path = roleFile(name);
            Result<String> res = readToString(path);
            if (isErr(res)) {
                return Err(res);
            }
            String content = res.getValue();
            role = Role.create(name, content);
        } else {
            Result<Role> res = Role.builtin(name);
            if (isErr(res)) {
                return Err(res);
            }
            role = res.getValue();
        }
        Model currentModel = currentModel();
        if (!isBlank(role.getModelId())) {
            String modelId = role.getModelId();
            if (!Objects.equals(currentModel.id(), modelId)) {
                Result<Model> res = Model.retrieveModel(this, modelId, ModelType.Chat);
                if (isErr(res)) {
                    return Err(res);
                }
                role.setModel(res.getValue());
            } else {
                role.setModel(currentModel);
            }
        } else {
            role.setModel(currentModel);
            if (role.getTemperature() == null) {
                role.setTemperature(temperature);
            }
            if (role.getTopP() == null) {
                role.setTopP(topP);
            }
        }
        return Ok(role);
    }

    // pub fn empty_session(&mut self) -> Result<()> {
    //    if let Some(session) = self.session.as_mut() {
    //        if let Some(agent) = self.agent.as_ref() {
    //            session.sync_agent(agent);
    //        }
    //        session.clear_messages();
    //    } else {
    //        bail!("No session")
    //    }
    //    self.discontinuous_last_message();
    //    Ok(())
    // }
    public Result<?> emptySession() {
        if (session != null) {
            if (agent != null) {
                session.syncAgent(agent);
            }
            session.clearMessages();
        } else {
            return bail("No session");
        }
        discontinuousLastMessage();
        return Ok();
    }

    // pub fn info(&self) -> Result<String> {
    //    if let Some(agent) = &self.agent {
    //        let output = agent.export()?;
    //        if let Some(session) = &self.session {
    //            let session = session
    //                .export()?
    //                .split('\n')
    //                .map(|v| format!("  {v}"))
    //                .collect::<Vec<_>>()
    //                .join("\n");
    //            Ok(format!("{output}session:\n{session}"))
    //        } else {
    //            Ok(output)
    //        }
    //    } else if let Some(session) = &self.session {
    //        session.export()
    //    } else if let Some(role) = &self.role {
    //        Ok(role.export())
    //    } else if let Some(rag) = &self.rag {
    //        rag.export()
    //    } else {
    //        self.sysinfo()
    //    }
    // }
    public Result<String> info() {
        if (agent != null) {
            Result<String> res = agent.export(this);
            if (isErr(res)) {
                return Err(res);
            }
            String output = res.getValue();
            if (session != null) {
                Result<String> resSession = session.export();
                if (isErr(resSession)) {
                    return Err(resSession);
                }
                String info = String.join("\n", Arrays.stream(resSession.getValue().split("\n"))
                    .map(e -> format("  {}", e))
                    .toList());
                return Ok(format("{}session:\n{}", output, info));
            } else {
                return Ok(output);
            }
        } else if (session != null) {
            return session.export();
        } else if (role != null) {
            return Ok(role.export());
        } else if (rag != null) {
            return rag.export();
        } else {
            return sysinfo();
        }
    }

    // pub fn set_wrap(&mut self, value: &str) -> Result<()> {
    //    if value == "no" {
    //        self.wrap = None;
    //    } else if value == "auto" {
    //        self.wrap = Some(value.into());
    //    } else {
    //        value
    //            .parse::<u16>()
    //            .map_err(|_| anyhow!("Invalid wrap value"))?;
    //        self.wrap = Some(value.into())
    //    }
    //    Ok(())
    // }
    public void initWrap(String value) {
        if ("no".equalsIgnoreCase(value)) {
            wrap = null;
        } else if ("auto".equalsIgnoreCase(value)) {
            wrap = "auto";
        } else {
            parseInt(value, __ -> anyhow("Invalid wrap value"));
            wrap = value;
        }
    }

    // pub async fn rebuild_rag(config: &GlobalConfig, abort_signal: AbortSignal) -> Result<()> {
    //    let mut rag = match config.read().rag.clone() {
    //        Some(v) => v.as_ref().clone(),
    //        None => bail!("No RAG"),
    //    };
    //    let document_paths = rag.document_paths().to_vec();
    //    rag.refresh_document_paths(&document_paths, true, config, abort_signal)
    //        .await?;
    //    config.write().rag = Some(Arc::new(rag));
    //    Ok(())
    // }
    public static Result<?> rebuildRag(Config config, AbortSignal abortSignal) {
        Rag rag = config.getRag();
        if (rag == null) {
            return bail("No RAG");
        }
        List<String> documentPaths = rag.documentPaths();
        rag.refreshDocumentPaths(documentPaths, true, config, abortSignal);
        return Ok();
    }

    // #[async_recursion::async_recursion]
    // pub async fn macro_execute(
    //    config: &GlobalConfig,
    //    name: &str,
    //    args: Option<&str>,
    //    abort_signal: AbortSignal,
    // ) -> Result<()> {
    //    let macro_value = Config::load_macro(name)?;
    //    let (mut new_args, text) = split_args_text(args.unwrap_or_default(), cfg!(windows));
    //    if !text.is_empty() {
    //        new_args.push(text.to_string());
    //    }
    //    let variables = macro_value
    //        .resolve_variables(&new_args)
    //        .map_err(|err| anyhow!("{err}. Usage: {}", macro_value.usage(name)))?;
    //    let role = config.read().extract_role();
    //    let mut config = config.read().clone();
    //    config.temperature = role.temperature();
    //    config.top_p = role.top_p();
    //    config.use_tools = role.use_tools().clone();
    //    config.macro_flag = true;
    //    config.model = role.model().clone();
    //    config.role = None;
    //    config.session = None;
    //    config.rag = None;
    //    config.agent = None;
    //    config.discontinuous_last_message();
    //    let config = Arc::new(RwLock::new(config));
    //    config.write().macro_flag = true;
    //    for step in &macro_value.steps {
    //        let command = Macro::interpolate_command(step, &variables);
    //        println!(">> {}", multiline_text(&command));
    //        run_repl_command(&config, abort_signal.clone(), &command).await?;
    //    }
    //    Ok(())
    // }
    public static void macroExecute(Config config, String macroName, String text, AbortSignal abortSignal) {

    }

    // pub fn before_chat_completion(&mut self, input: &Input) -> Result<()> {
    //    self.last_message = Some(LastMessage::new(input.clone(), String::new()));
    //    Ok(())
    // }
    public void beforeChatCompletion(Input input) {
        lastMessage = new LastMessage().setInput(input);
    }

    // pub fn is_compressing_session(&self) -> bool {
    //    self.session
    //        .as_ref()
    //        .map(|v| v.compressing())
    //        .unwrap_or_default()
    // }
    public boolean isCompressingSession() {
        return (session != null) && session.isCompressing();
    }

    // pub fn after_chat_completion(
    //    &mut self,
    //    input: &Input,
    //    output: &str,
    //    tool_results: &[ToolResult],
    // ) -> Result<()> {
    //    if !tool_results.is_empty() {
    //        return Ok(());
    //    }
    //    self.last_message = Some(LastMessage::new(input.clone(), output.to_string()));
    //    if !self.dry_run {
    //        self.save_message(input, output)?;
    //    }
    //    Ok(())
    // }
    public void afterChatCompletion(Input input, String output, List<ToolResult> toolResults) {
        if (!isEmpty(toolResults)) {
            return;
        }
        lastMessage = new LastMessage().setInput(input).setOutput(output);
        if (!dryRun) {
            saveMessage(input, output);
        }
    }

    // pub fn agent_banner(&self) -> Result<String> {
    //    if let Some(agent) = &self.agent {
    //        Ok(agent.banner())
    //    } else {
    //        bail!("No agent")
    //    }
    // }
    public Result<String> agentBanner() {
        if (agent != null) {
            return Ok(agent.banner());
        } else {
            return bail("No agent");
        }
    }

    // fn save_message(&mut self, input: &Input, output: &str) -> Result<()> {
    //    let mut input = input.clone();
    //    input.clear_patch();
    //    if let Some(session) = input.session_mut(&mut self.session) {
    //        session.add_message(&input, output)?;
    //        return Ok(());
    //    }
    //
    //    if !self.save {
    //        return Ok(());
    //    }
    //    let mut file = self.open_message_file()?;
    //    if output.is_empty() && input.tool_calls().is_none() {
    //        return Ok(());
    //    }
    //    let now = now();
    //    let summary = input.summary();
    //    let raw_input = input.raw();
    //    let scope = if self.agent.is_none() {
    //        let role_name = if input.role().is_derived() {
    //            None
    //        } else {
    //            Some(input.role().name())
    //        };
    //        match (role_name, input.rag_name()) {
    //            (Some(role), Some(rag_name)) => format!(" ({role}#{rag_name})"),
    //            (Some(role), _) => format!(" ({role})"),
    //            (None, Some(rag_name)) => format!(" (#{rag_name})"),
    //            _ => String::new(),
    //        }
    //    } else {
    //        String::new()
    //    };
    //    let tool_calls = match input.tool_calls() {
    //        Some(MessageContentToolCalls {
    //            tool_results, text, ..
    //        }) => {
    //            let mut lines = vec!["<tool_calls>".to_string()];
    //            if !text.is_empty() {
    //                lines.push(text.clone());
    //            }
    //            lines.push(serde_json::to_string(&tool_results).unwrap_or_default());
    //            lines.push("</tool_calls>\n".to_string());
    //            lines.join("\n")
    //        }
    //        None => String::new(),
    //    };
    //    let output = format!(
    //        "# CHAT: {summary} [{now}]{scope}\n{raw_input}\n--------\n{tool_calls}{output}\n--------\n\n",
    //    );
    //    file.write_all(output.as_bytes())
    //        .with_context(|| "Failed to save message")
    // }
    public void saveMessage(Input input, String output) {
        input = input.clone();
    }

    // pub fn exit_session(&mut self) -> Result<()> {
    //    if let Some(mut session) = self.session.take() {
    //        let sessions_dir = self.sessions_dir();
    //        session.exit(&sessions_dir, self.working_mode.is_repl())?;
    //        self.discontinuous_last_message();
    //    }
    //    Ok(())
    // }
    public Result<?> exitSession() {
        if (session != null) {
            Path sessionsDir = sessionsDir();
            session.exit(sessionsDir, WorkingMode.Repl.equals(workingMode));
            discontinuousLastMessage();
        }
        return Ok();
    }

    // pub fn render_options(&self) -> Result<RenderOptions> {
    //    let theme = if self.highlight {
    //        let theme_mode = if self.light_theme() { "light" } else { "dark" };
    //        let theme_filename = format!("{theme_mode}.tmTheme");
    //        let theme_path = Self::local_path(&theme_filename);
    //        if theme_path.exists() {
    //            let theme = ThemeSet::get_theme(&theme_path)
    //                .with_context(|| format!("Invalid theme at '{}'", theme_path.display()))?;
    //            Some(theme)
    //        } else {
    //            let theme = if self.light_theme() {
    //                decode_bin(LIGHT_THEME).context("Invalid builtin light theme")?
    //            } else {
    //                decode_bin(DARK_THEME).context("Invalid builtin dark theme")?
    //            };
    //            Some(theme)
    //        }
    //    } else {
    //        None
    //    };
    //    let wrap = if *IS_STDOUT_TERMINAL {
    //        self.wrap.clone()
    //    } else {
    //        None
    //    };
    //    let truecolor = matches!(
    //        env::var("COLORTERM").as_ref().map(|v| v.as_str()),
    //        Ok("truecolor")
    //    );
    //    Ok(RenderOptions::new(theme, wrap, self.wrap_code, truecolor))
    // }
    public RenderOptions renderOptions() {
        return null;
    }

    private void loadEnvs(){ }
    private void loadFunctions() { }

    // pub fn select_functions(&self, role: &Role) -> Option<Vec<FunctionDeclaration>> {
    //    let mut functions = vec![];
    //    if self.function_calling {
    //        if let Some(use_tools) = role.use_tools() {
    //            let mut tool_names: HashSet<String> = Default::default();
    //            let declaration_names: HashSet<String> = self
    //                .functions
    //                .declarations()
    //                .iter()
    //                .map(|v| v.name.to_string())
    //                .collect();
    //            if use_tools == "all" {
    //                tool_names.extend(declaration_names);
    //            } else {
    //                for item in use_tools.split(',') {
    //                    let item = item.trim();
    //                    if let Some(values) = self.mapping_tools.get(item) {
    //                        tool_names.extend(
    //                            values
    //                                .split(',')
    //                                .map(|v| v.to_string())
    //                                .filter(|v| declaration_names.contains(v)),
    //                        )
    //                    } else if declaration_names.contains(item) {
    //                        tool_names.insert(item.to_string());
    //                    }
    //                }
    //            }
    //            functions = self
    //                .functions
    //                .declarations()
    //                .iter()
    //                .filter_map(|v| {
    //                    if tool_names.contains(&v.name) {
    //                        Some(v.clone())
    //                    } else {
    //                        None
    //                    }
    //                })
    //                .collect();
    //        }
    //
    //        if let Some(agent) = &self.agent {
    //            let mut agent_functions = agent.functions().declarations().to_vec();
    //            let tool_names: HashSet<String> = agent_functions
    //                .iter()
    //                .filter_map(|v| {
    //                    if v.agent {
    //                        None
    //                    } else {
    //                        Some(v.name.to_string())
    //                    }
    //                })
    //                .collect();
    //            agent_functions.extend(
    //                functions
    //                    .into_iter()
    //                    .filter(|v| !tool_names.contains(&v.name)),
    //            );
    //            functions = agent_functions;
    //        }
    //    };
    //    if functions.is_empty() {
    //        None
    //    } else {
    //        Some(functions)
    //    }
    // }
    public List<FunctionDeclaration> selectFunctions(Role role) {
        List<FunctionDeclaration> functions = new ArrayList<>();
        if (functionCalling) {
            String useTools = role.getUseTools();
            if (!isBlank(useTools)) {
                Set<String> toolNames = new HashSet<>();
                Set<String> declarationNames = this.functions.getDeclarations().stream()
                    .map(FunctionDeclaration::getName)
                    .collect(Collectors.toSet());
                if ("all".equals(useTools)) {
                    toolNames.addAll(declarationNames);
                } else {
                    String[] items = useTools.split(",");
                    for (String item : items) {
                        item = item.trim();
                        String values = (mappingTools == null) ? null : mappingTools.get(item);
                        if (!isBlank(values)) {
                            String[] parts = values.split(",");
                            for (String part : parts) {
                                part = part.trim();
                                if (!declarationNames.contains(part)) {
                                    toolNames.add(part);
                                }
                            }
                        } else if (declarationNames.contains(item)) {
                            toolNames.add(item);
                        }
                    }
                }
                functions = this.functions.getDeclarations().stream()
                    .filter(e -> toolNames.contains(e.getName()))
                    .toList();
            }

            if (agent != null) {
                List<FunctionDeclaration> agentFunctions = agent.getFunctions().getDeclarations();
                Set<String> toolNames = agentFunctions.stream()
                    .filter(e -> !e.isAgent())
                    .map(FunctionDeclaration::getName)
                    .collect(Collectors.toSet());
                agentFunctions.addAll(
                    functions.stream()
                        .filter(e -> !toolNames.contains(e.getName()))
                        .toList());
                functions = agentFunctions;
            }
        }

        if (isEmpty(functions)) {
            return null;
        } else {
            return functions;
        }
    }

    // fn setup_model(&mut self) -> Result<()> {
    //    let mut model_id = self.model_id.clone();
    //    if model_id.is_empty() {
    //        let models = list_models(self, ModelType::Chat);
    //        if models.is_empty() {
    //            bail!("No available model");
    //        }
    //        model_id = models[0].id()
    //    };
    //    self.set_model(&model_id)?;
    //    self.model_id = model_id;
    //    Ok(())
    // }
    private Result<?> setupModel() {
        String modelId = this.modelId;
        if (isEmpty(modelId)) {
            List<Model> models = listModels(this, ModelType.Chat);
            if (isEmpty(models)) {
                return bail("No available model");
            }
            modelId = models.getFirst().id();
        }
        setModel(modelId);
        this.modelId = modelId;
        return Ok();
    }

    private void setupDocumentLoaders() { }
    private void setupUserAgent() { }

    // pub fn sysinfo(&self) -> Result<String> {
    //    let display_path = |path: &Path| path.display().to_string();
    //    let wrap = self
    //        .wrap
    //        .clone()
    //        .map_or_else(|| String::from("no"), |v| v.to_string());
    //    let (rag_reranker_model, rag_top_k) = match &self.rag {
    //        Some(rag) => rag.get_config(),
    //        None => (self.rag_reranker_model.clone(), self.rag_top_k),
    //    };
    //    let role = self.extract_role();
    //    let mut items = vec![
    //        ("model", role.model().id()),
    //        ("temperature", format_option_value(&role.temperature())),
    //        ("top_p", format_option_value(&role.top_p())),
    //        ("use_tools", format_option_value(&role.use_tools())),
    //        (
    //            "max_output_tokens",
    //            role.model()
    //                .max_tokens_param()
    //                .map(|v| format!("{v} (current model)"))
    //                .unwrap_or_else(|| "null".into()),
    //        ),
    //        ("save_session", format_option_value(&self.save_session)),
    //        ("compress_threshold", self.compress_threshold.to_string()),
    //        (
    //            "rag_reranker_model",
    //            format_option_value(&rag_reranker_model),
    //        ),
    //        ("rag_top_k", rag_top_k.to_string()),
    //        ("dry_run", self.dry_run.to_string()),
    //        ("function_calling", self.function_calling.to_string()),
    //        ("stream", self.stream.to_string()),
    //        ("save", self.save.to_string()),
    //        ("keybindings", self.keybindings.clone()),
    //        ("wrap", wrap),
    //        ("wrap_code", self.wrap_code.to_string()),
    //        ("highlight", self.highlight.to_string()),
    //        ("theme", format_option_value(&self.theme)),
    //        ("config_file", display_path(&Self::config_file())),
    //        ("env_file", display_path(&Self::env_file())),
    //        ("roles_dir", display_path(&Self::roles_dir())),
    //        ("sessions_dir", display_path(&self.sessions_dir())),
    //        ("rags_dir", display_path(&Self::rags_dir())),
    //        ("macros_dir", display_path(&Self::macros_dir())),
    //        ("functions_dir", display_path(&Self::functions_dir())),
    //        ("messages_file", display_path(&self.messages_file())),
    //    ];
    //    if let Ok((_, Some(log_path))) = Self::log_config(self.working_mode.is_serve()) {
    //        items.push(("log_path", display_path(&log_path)));
    //    }
    //    let output = items
    //        .iter()
    //        .map(|(name, value)| format!("{name:<24}{value}\n"))
    //        .collect::<Vec<String>>()
    //        .join("");
    //    Ok(output)
    // }
    public Result<String> sysinfo() {
        String wrap = isBlank(this.wrap) ? "no" : this.wrap;

        String ragRerankerModel = this.ragRerankerModel;
        Integer ragTopK = this.ragTopK;
        if (rag != null) {
            Tuple<String, Integer> tuple = rag.getConfig();
            ragRerankerModel = tuple.first();
            ragTopK = tuple.second();
        }

        Role role = extractRole();
        List<Tuple<String, Object>> items = List.of(
            new Tuple<>("model", role.getModel().id()),
            new Tuple<>("temperature", formatOptionValue(role.getTemperature())),
            new Tuple<>("top_p", formatOptionValue(role.getTopP())),
            new Tuple<>("use_tools", formatOptionValue(role.getUseTools())),
            new Tuple<>("max_output_tokens", formatNonNullValue(role.getModel().getMaxTokensParam(), "{} (current model)")),
            new Tuple<>("save_session", formatOptionValue(isSaveSession())),
            new Tuple<>("compress_threshold", compressThreshold),
            new Tuple<>("rag_reranker_model", formatOptionValue(ragRerankerModel)),
            new Tuple<>("rag_top_k", ragTopK),
            new Tuple<>("dry_run", isDryRun()),
            new Tuple<>("function_calling", functionCalling),
            new Tuple<>("stream", isStream()),
            new Tuple<>("save", save),
            new Tuple<>("keybindings", keyBindings),
            new Tuple<>("wrap", wrap),
            new Tuple<>("wrap_code", wrapCode),
            new Tuple<>("highlight", highlight),
            new Tuple<>("theme", formatOptionValue(theme)),
            new Tuple<>("config_file", configFile()),
            new Tuple<>("env_file", envFile()),
            new Tuple<>("roles_dir", rolesDir()),
            new Tuple<>("sessions_dir", sessionsDir()),
            new Tuple<>("rags_dir", ragsDir()),
            new Tuple<>("macros_dir", macrosDir()),
            new Tuple<>("functions_dir", functionsDir()),
            new Tuple<>("messages_file", messagesFile())
            //new Tuple<>("logs_path", logsPath)
        );
        String output = String.join("\n", items.stream()
            .map(e -> format("{}{}", padRight(e.first(), 24), e.second()))
            .toList());
        return Ok(output);
    }

    // fn format_option_value<T>(value: &Option<T>) -> String
    // where
    //    T: std::fmt::Display,
    // {
    //    match value {
    //        Some(value) => value.to_string(),
    //        None => "null".to_string(),
    //    }
    // }
    private String formatOptionValue(Object value) {
        return (value == null) ? "null" : value.toString();
    }

    private String formatNonNullValue(Object value, String formatMsg) {
        return (value == null) ? "null" : format(formatMsg, value);
    }

    // fn discontinuous_last_message(&mut self) {
    //    if let Some(last_message) = self.last_message.as_mut() {
    //        last_message.continuous = false;
    //    }
    // }
    private void discontinuousLastMessage() {
        if (lastMessage != null) {
            lastMessage.setContinuous(false);
        }
    }

    // impl Default for Config {
    //    fn default() -> Self {
    //        Self {
    //            model_id: Default::default(),
    //            temperature: None,
    //            top_p: None,
    //
    //            dry_run: false,
    //            stream: true,
    //            save: false,
    //            keybindings: "emacs".into(),
    //            editor: None,
    //            wrap: None,
    //            wrap_code: false,
    //
    //            function_calling: true,
    //            mapping_tools: Default::default(),
    //            use_tools: None,
    //
    //            repl_prelude: None,
    //            cmd_prelude: None,
    //            agent_prelude: None,
    //
    //            save_session: None,
    //            compress_threshold: 4000,
    //            summarize_prompt: None,
    //            summary_prompt: None,
    //
    //            rag_embedding_model: None,
    //            rag_reranker_model: None,
    //            rag_top_k: 5,
    //            rag_chunk_size: None,
    //            rag_chunk_overlap: None,
    //            rag_template: None,
    //
    //            document_loaders: Default::default(),
    //
    //            highlight: true,
    //            theme: None,
    //            left_prompt: None,
    //            right_prompt: None,
    //
    //            serve_addr: None,
    //            user_agent: None,
    //            save_shell_history: true,
    //            sync_models_url: None,
    //
    //            clients: vec![],
    //
    //            macro_flag: false,
    //            info_flag: false,
    //            agent_variables: None,
    //
    //            model: Default::default(),
    //            functions: Default::default(),
    //            working_mode: WorkingMode::Cmd,
    //            last_message: None,
    //
    //            role: None,
    //            session: None,
    //            rag: None,
    //            agent: None,
    //        }
    //    }
    // }
    private static Config defaultConfig() {
        return new Config()
            .setDryRun(false)
            .setStream(true)
            .setSave(false)
            .setKeyBindings("emacs")
            .setWrapCode(false)

            .setFunctionCalling(true)

            .setCompressThreshold(4000)

            .setRagTopK(5)

            .setHighlight(true)

            .setClients(new ArrayList<>())

            .setMacroFlag(false)
            .setInfoFlag(false)

            .setWorkingMode(WorkingMode.Cmd);
    }

    // async fn create_config_file(config_path: &Path) -> Result<()> {
    //    let ans = Confirm::new("No config file, create a new one?")
    //        .with_default(true)
    //        .prompt()?;
    //    if !ans {
    //        process::exit(0);
    //    }
    //
    //    let client = Select::new("API Provider (required):", list_client_types()).prompt()?;
    //
    //    let mut config = serde_json::json!({});
    //    let (model, clients_config) = create_client_config(client).await?;
    //    config["model"] = model.into();
    //    config[CLIENTS_FIELD] = clients_config;
    //
    //    let config_data = serde_yaml::to_string(&config).with_context(|| "Failed to create config")?;
    //    let config_data = format!(
    //        "# see https://github.com/sigoden/aichat/blob/main/config.example.yaml\n\n{config_data}"
    //    );
    //
    //    ensure_parent_exists(config_path)?;
    //    std::fs::write(config_path, config_data)
    //        .with_context(|| format!("Failed to write to '{}'", config_path.display()))?;
    //    #[cfg(unix)]
    //    {
    //        use std::os::unix::prelude::PermissionsExt;
    //        let perms = std::fs::Permissions::from_mode(0o600);
    //        std::fs::set_permissions(config_path, perms)?;
    //    }
    //
    //    println!("✓ Saved the config file to '{}'.\n", config_path.display());
    //
    //    Ok(())
    // }
    private static Result<?> createConfigFile(Path configPath) {
        Result<Boolean> res = new Confirm("No config file, create a new one?")
            .setDefaultValue(true)
            .prompt();
        if (isErr(res)) {
            return Err(res);
        }
        boolean ans = res.getValue();
        if (!ans) {
            System.exit(0);
        }

        Result<String> ret = new Select("API Provider (required):", listClientTypes()).prompt();
        if (isErr(ret)) {
            return Err(ret);
        }
        String client = ret.getValue();

        Value config = new Value();
        Result<Tuple<String, Value>> rtt = createClientConfig(client);
        if (isErr(rtt)) {
            return Err(rtt);
        }
        Tuple<String, Value> tuple = rtt.getValue();
        String model = tuple.first();
        Value clientsConfig = tuple.second();
        config.put("model", model);
        config.put(CLIENTS_FIELD, clientsConfig);

        Result<String> rec = SerdeYaml.toString(config).withContext(() -> "Failed to create config");
        if (isErr(rec)) {
            return Err(rec);
        }

        String configData = rec.getValue();
        configData = format("# see https://github.com/knivit/jai for examples\n\n{}", configData);

        Result<?> rep = ensureParentExists(configPath);
        if (isErr(rep)) {
            return Err(rep);
        }
        Result<?> rew = Fs.write(configPath, configData);
        if (isErr(rew)) {
            return Err(rew);
        }

        println("✓ Saved the config file to '{}'.\n", configPath);

        return Ok();
    }

    // fn load_from_file(config_path: &Path) -> Result<Self> {
    //    let err = || format!("Failed to load config at '{}'", config_path.display());
    //    let content = read_to_string(config_path).with_context(err)?;
    //    let config: Self = serde_yaml::from_str(&content)
    //        .map_err(|err| {
    //            let err_msg = err.to_string();
    //            let err_msg = if err_msg.starts_with(&format!("{CLIENTS_FIELD}: ")) {
    //                // location is incorrect, get rid of it
    //                err_msg
    //                    .split_once(" at line")
    //                    .map(|(v, _)| {
    //                        format!("{v} (Sorry for being unable to provide an exact location)")
    //                    })
    //                    .unwrap_or_else(|| "clients: invalid value".into())
    //            } else {
    //                err_msg
    //            };
    //            anyhow!("{err_msg}")
    //        })
    //        .with_context(err)?;
    //
    //    Ok(config)
    // }
    private static Result<Config> loadFromFile(Path configPath) {
        Supplier<String> err = () -> format("Failed to load config at '{}'", configPath);
        Result<String> res = readToString(configPath).withContext(err);
        if (isErr(res)) {
            return Err(res);
        }
        String content = res.getValue();
        Result<Config> ret = SerdeYaml.fromStr(content, Config.class).withContext(err);
        Config config = ret.getValue();

        return Ok(config);
    }

    // fn load_dynamic(model_id: &str) -> Result<Self> {
    //    let provider = match model_id.split_once(':') {
    //        Some((v, _)) => v,
    //        _ => model_id,
    //    };
    //    let is_openai_compatible = OPENAI_COMPATIBLE_PROVIDERS
    //        .into_iter()
    //        .any(|(name, _)| provider == name);
    //    let client = if is_openai_compatible {
    //        json!({ "type": "openai-compatible", "name": provider })
    //    } else {
    //        json!({ "type": provider })
    //    };
    //    let config = json!({
    //        "model": model_id.to_string(),
    //        "save": false,
    //        "clients": vec![client],
    //    });
    //    let config =
    //        serde_json::from_value(config).with_context(|| "Failed to load config from env")?;
    //    Ok(config)
    // }
    private static Result<Config> loadDynamic(String modelId) {
        Tuple<String, String> tuple = splitOnce(modelId, ':');
        String provider = !isEmpty(tuple.first()) ? tuple.first() : modelId;
        boolean isOpenaiCompatible = OPENAI_COMPATIBLE_PROVIDERS.entrySet()
            .stream()
            .anyMatch(e -> Objects.equals(provider, e.getKey()));
        Value client = isOpenaiCompatible ?
            json("type", "openai-compatible", "name", provider) : json("type", provider);
        Value config = json(
            "model", modelId,
            "save", false,
            "clients", List.of(client)
        );
        Result<Config> res = SerdeJson.fromValue(config, Config.class).withContext(() -> "Failed to load config from env");
        if (isErr(res)) {
            return Err(res);
        }
        return Ok(res.getValue());
    }

    // pub fn loal_models_override() -> Result<Vec<ProviderModels>> {
    //    let model_override_path = Self::models_override_file();
    //    let err = || {
    //        format!(
    //            "Failed to load models at '{}'",
    //            model_override_path.display()
    //        )
    //    };
    //    let content = read_to_string(&model_override_path).with_context(err)?;
    //    let models_override: ModelsOverride = serde_yaml::from_str(&content).with_context(err)?;
    //    if models_override.version != env!("CARGO_PKG_VERSION") {
    //        bail!("Incompatible version")
    //    }
    //    Ok(models_override.list)
    // }
    public static Result<List<ProviderModels>> loadModelsOverride() {
        Path modelOverridePath = modelsOverrideFile();
        Supplier<String> err = () -> format("Failed to load models at '{}'", modelOverridePath);
        Result<String> res = readToString(modelOverridePath).withContext(err);
        if (isErr(res)) {
            return Err(res);
        }
        String content = res.getValue();
        Result<ModelsOverride> ret = SerdeYaml.fromStr(content, ModelsOverride.class).withContext(err);
        if (isErr(ret)) {
            return Err(ret);
        }
        ModelsOverride modelsOverride = ret.getValue();
        if (!Objects.equals(modelsOverride.getVersion(), CARGO_PKG_VERSION)) {
            return bail("Incompatible version");
        }
        return Ok(modelsOverride.getList());
    }

    private static List<String> listClientTypes() {
        return Arrays.asList(
            "openai",
            "openai-compatible",
            "gemini",
            "claude",
            "cohere",
            "azure-openai",
            "vertexai"
        );
    }

    // pub(crate) fn ensure_parent_exists(path: &Path) -> Result<()> {
    //    if path.exists() {
    //        return Ok(());
    //    }
    //    let parent = path
    //        .parent()
    //        .ok_or_else(|| anyhow!("Failed to write to '{}', No parent path", path.display()))?;
    //    if !parent.exists() {
    //        create_dir_all(parent).with_context(|| {
    //            format!(
    //                "Failed to write to '{}', Cannot create parent directory",
    //                path.display()
    //            )
    //        })?;
    //    }
    //    Ok(())
    // }
    public static Result<?> ensureParentExists(Path path) {
        if (Files.exists(path)) {
            return Ok();
        }
        try {
            Files.createDirectories(path.getParent());
            return Ok();
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    // pub fn apply_prelude(&mut self) -> Result<()> {
    //    if self.macro_flag || !self.state().is_empty() {
    //        return Ok(());
    //    }
    //    let prelude = match self.working_mode {
    //        WorkingMode::Repl => self.repl_prelude.as_ref(),
    //        WorkingMode::Cmd => self.cmd_prelude.as_ref(),
    //        WorkingMode::Serve => return Ok(()),
    //    };
    //    let prelude = match prelude {
    //        Some(v) => {
    //            if v.is_empty() {
    //                return Ok(());
    //            }
    //            v.to_string()
    //        }
    //        None => return Ok(()),
    //    };
    //
    //    let err_msg = || format!("Invalid prelude '{prelude}");
    //    match prelude.split_once(':') {
    //        Some(("role", name)) => {
    //            self.use_role(name).with_context(err_msg)?;
    //        }
    //        Some(("session", name)) => {
    //            self.use_session(Some(name)).with_context(err_msg)?;
    //        }
    //        Some((session_name, role_name)) => {
    //            self.use_session(Some(session_name)).with_context(err_msg)?;
    //            if let Some(true) = self.session.as_ref().map(|v| v.is_empty()) {
    //                self.use_role(role_name).with_context(err_msg)?;
    //            }
    //        }
    //        _ => {
    //            bail!("{}", err_msg())
    //        }
    //    }
    //    Ok(())
    // }
    public Result<?> applyPrelude() {
        if (macroFlag || StateFlags.EMPTY == state()) {
            return Ok();
        }
        String prelude = switch (workingMode) {
            case Repl -> replPrelude;
            case Cmd -> cmdPrelude;
            case Serve -> null;
        };
        if (isBlank(prelude)) {
            return Ok();
        }
        Tuple<String, String> tuple = splitOnce(prelude, ':');
        if ("role".equals(tuple.first()) && !isBlank(tuple.second())) {
            useRole(tuple.second());
        } else if ("session".equals(tuple.first()) && !isBlank(tuple.second())) {
            useSession(tuple.second());
        } else if (!isBlank(tuple.first()) && !isBlank(tuple.second())) {
            String sessionName = tuple.first();
            String roleName = tuple.second();
            useSession(sessionName);
            if (session == null) {
                useRole(roleName);
            }
        } else {
            return bail("Invalid prelude '{}'", prelude);
        }
        return Ok();
    }

    // pub fn print_markdown(&self, text: &str) -> Result<()> {
    //    if *IS_STDOUT_TERMINAL {
    //        let render_options = self.render_options()?;
    //        let mut markdown_render = MarkdownRender::init(render_options)?;
    //        println!("{}", markdown_render.render(text));
    //    } else {
    //        println!("{text}");
    //    }
    //    Ok(())
    // }
    public void printMarkdown(String text) {
        println(text);
    }
}
