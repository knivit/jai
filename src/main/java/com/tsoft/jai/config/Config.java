package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.inquire.Confirm;
import com.tsoft.jai.inquire.Inquire;
import com.tsoft.jai.inquire.Select;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.std.Fs;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.file.*;
import java.util.*;

@Data
@Accessors(chain = true)
public class Config {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    //#[serde(default)]
    @JsonProperty("model")
    private String modelId;
    private Double temperature;
    private Double topP;

    private boolean dryRun;
    private boolean stream;
    private boolean save;
    private String keyBindings;
    private String editor;
    private String wrap;
    private boolean wrapCode;

    private boolean functionCalling;
    private Map<String, String> mappingTools;
    private String useTools;

    private String replPrelude;
    private String cmdPrelude;
    private String agentPrelude;

    private boolean saveSession;
    private Integer compressThreshold;
    private String summarizePrompt;
    private String summaryPrompt;

    private String ragEmbeddingModel;
    private String ragRerankerModel;
    private Integer ragTopK;
    private Integer ragChunkSize;
    private Integer ragChunkOverlap;
    private String ragTemplate;

    //#[serde(default)]
    private Map<String, String> documentLoaders;

    private boolean highlight;
    private String theme;
    private String leftPrompt;
    private String rightPrompt;

    private String serveAddr;
    private String userAgent;
    private boolean saveShellHistory;
    private String syncModelsUrl;

    private List<ClientConfig> clients;

    //#[serde(skip)]
    @JsonIgnore
    private boolean macroFlag;
    //#[serde(skip)]
    @JsonIgnore
    private boolean infoFlag;
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
    private WorkingMode workingMode;
    //#[serde(skip)]
    @JsonIgnore
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

    @JsonIgnore
    private Path configPath;

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
    public static Config init(WorkingMode workingMode, boolean infoFlag, String configFileName) {
        File configFile = configFile(configFileName);

        Config config;
        if (!configFile.exists()) {
            if (Inquire.terminal() != null) {
                createConfigFile(configFile);
                config = loadFromFile(configFile);
            } else {
                config = createDefaultConfig();
            }
        } else {
            config = loadFromFile(configFile);
        }

        config.setWorkingMode(workingMode);
        config.setInfoFlag(infoFlag);

        config.loadEnvs();
        config.loadFunctions();

        config.setupModel();
        config.setupDocumentLoaders();
        config.setupUserAgent();

        config.setConfigPath(configFile.toPath().getParent());

        return config;
    }

    private static File configFile(String configFile) {
        if (configFile != null && !configFile.isBlank()) {
            return Paths.get(configFile).toAbsolutePath().toFile();
        }
        return Paths.get(CONFIG_FILE_NAME).toAbsolutePath().toFile();
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

        Fs.readDir(rolesDir()).stream()
            .map(File::getName)
            .filter(e -> e.endsWith(".md"))
            .map(e -> e.substring(0, e.length() - 3))
            .forEach(names::add);

        if (withBuiltin) {
            names.addAll(Role.listBuiltinRoleNames());
        }
        List<String> roles = new ArrayList<>(names);
        roles.sort(String::compareToIgnoreCase);
        return roles;
    }

    // pub fn roles_dir() -> PathBuf {
    //    match env::var(get_env_name("roles_dir")) {
    //        Ok(value) => PathBuf::from(value),
    //        Err(_) => Self::local_path(ROLES_DIR_NAME),
    //    }
    // }
    public Path rolesDir() {
        return configPath.resolve(ROLES_DIR_NAME);
    }

    private void loadEnvs(){ }
    private void loadFunctions() { }
    private void setupModel() { }
    private void setupDocumentLoaders() { }
    private void setupUserAgent() { }

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
    private static Config createDefaultConfig() {
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
    private static void createConfigFile(File configFile) {
        boolean ans = new Confirm("No config file, create a new one?")
            .setDefaultValue(true)
            .prompt();
        if (!ans) {
            System.exit(0);
        }

        String client = new Select("API Provider (required):", listClientTypes()).prompt();

        Value config = new Value();
        createClientConfig(config, client);

        String configData = SerDe.toYamlString(config);
        configData = "# see https://github.com/knivit/jai for examples\n\n%s".formatted(configData);

        ensureParentExists(configFile);
        try {
            Files.writeString(configFile.toPath(), configData, StandardOpenOption.CREATE_NEW);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        System.out.printf("✓ Saved the config file to '%s'.%n", configFile.getAbsolutePath());
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
    private static Config loadFromFile(File configPath) {
        return SerDe.readFromYamlFile(configPath, Config.class);
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

    private static void createClientConfig(Value config, String client) {
        // to do
    }

    private static void ensureParentExists(File configFile) {
        try {
            Files.createDirectories(Paths.get(configFile.getParent()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
