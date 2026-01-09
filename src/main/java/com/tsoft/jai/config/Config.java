package com.tsoft.jai.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.client.model.ModelType;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.config.role.RoleLike;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.inquire.Confirm;
import com.tsoft.jai.inquire.Inquire;
import com.tsoft.jai.inquire.Select;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import com.tsoft.jai.std.Fs;
import com.tsoft.jai.utils.AbortSignal;
import com.tsoft.jai.utils.Tuple;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.file.*;
import java.util.*;

import static com.tsoft.jai.anyhow.Macros.bail;
import static com.tsoft.jai.inquire.Inquire.println;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.*;

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
    @JsonIgnore
    private File configFile;

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
    public static Config init(WorkingMode workingMode, boolean infoFlag, String configFileName) {
        File configFile = configFile(configFileName);

        Config config;
        if (!configFile.exists()) {
            if (Inquire.terminal() != null) {
                createConfigFile(configFile);
                config = loadFromFile(configFile);
            } else {
                configFile = configFile(null);
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
        config.setConfigFile(configFile);

        return config;
    }

    private static File configFile(String configFile) {
        if (!isBlank(configFile)) {
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

    public Path functionsDir() {
        return configPath.resolve(FUNCTIONS_DIR_NAME);
    }

    public Path agentsDataDir() {
        return configPath.resolve(AGENTS_DIR_NAME);
    }

    public Path agentDataDir(String agentName) {
        return agentsDataDir().resolve(agentName);
    }

    public Path agentFunctionsDir(String agentName) {
        return configPath.resolve(FUNCTIONS_DIR_NAME).resolve(agentName);
    }

    // pub fn agent_rag_file(agent_name: &str, rag_name: &str) -> PathBuf {
    //    Self::agent_data_dir(agent_name).join(format!("{rag_name}.yaml"))
    // }
    public File agentRagFile(String agentName, String ragName) {
        return agentDataDir(agentName).resolve(format("{}.yaml", ragName)).toFile();
    }

    //  pub fn agent_config_file(name: &str) -> PathBuf {
    //     match env::var(format!("{}_CONFIG_FILE", normalize_env_name(name))) {
    //         Ok(value) => PathBuf::from(value),
    //         Err(_) => Self::agent_data_dir(name).join(CONFIG_FILE_NAME),
    //     }
    // }
    public Path agentConfigFile(String agentName) {
        return agentDataDir(agentName).resolve(CONFIG_FILE_NAME);
    }

    public Path ragsDir() {
        return configPath.resolve(RAGS_DIR_NAME);
    }

    public Path macrosDir() {
        return configPath.resolve(MACROS_DIR_NAME);
    }

    public Path sessionsDir() {
        return configPath.resolve(SESSIONS_DIR_NAME);
    }

    // pub fn session_file(&self, name: &str) -> PathBuf {
    //    match name.split_once("/") {
    //        Some((dir, name)) => self.sessions_dir().join(dir).join(format!("{name}.yaml")),
    //        None => self.sessions_dir().join(format!("{name}.yaml")),
    //    }
    // }
    public File sessionFile(String sessionName) {
        Tuple<String, String> tuple = splitOnce(sessionName, '/');
        String dir = tuple.first();
        String name = tuple.second();
        if (!isBlank(dir) && !isBlank(name)) {
            return sessionsDir().resolve(dir).resolve(format("{}.yaml", name)).toFile();
        }
        return sessionsDir().resolve(format("{}.yaml", name)).toFile();
    }

    // pub fn role_file(name: &str) -> PathBuf {
    //    Self::roles_dir().join(format!("{name}.md"))
    // }
    public File roleFile(String name) {
        return rolesDir().resolve(format("{}.md", name)).toFile();
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
    public File messagesFile() {
        if (agent == null) {
            return configPath.resolve(MESSAGES_FILE_NAME).toFile();
        }
        return agentDataDir(agent.getName()).resolve(MESSAGES_FILE_NAME).toFile();
    }

    // pub fn rag_file(&self, name: &str) -> PathBuf {
    //    match &self.agent {
    //        Some(agent) => Self::agent_rag_file(agent.name(), name),
    //        None => Self::rags_dir().join(format!("{name}.yaml")),
    //    }
    // }
    public File ragFile(String name) {
        if (agent != null) {
            return agentRagFile(agent.getName(), name);
        }
        return ragsDir().resolve(format("{}.yaml", name)).toFile();
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
        List<String> names = new ArrayList<>(
            Fs.readDir(ragsDir()).stream()
                .map(File::getName)
                .filter(e -> e.endsWith(".yaml"))
                .map(e -> e.substring(0, e.length() - 5))
                .toList());
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    // pub fn list_macros() -> Vec<String> {
    //    list_file_names(Self::macros_dir(), ".yaml")
    // }
    public List<String> listMacros() {
        List<String> names = new ArrayList<>(
            Fs.readDir(macrosDir()).stream()
                .map(File::getName)
                .filter(e -> e.endsWith(".yaml"))
                .map(e -> e.substring(0, e.length() - 5))
                .toList());
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    // pub fn list_sessions(&self) -> Vec<String> {
    //    list_file_names(self.sessions_dir(), ".yaml")
    // }
    public List<String> listSessions() {
        List<String> names = new ArrayList<>(
            Fs.readDir(sessionsDir()).stream()
                .map(File::getName)
                .filter(e -> e.endsWith(".yaml"))
                .map(e -> e.substring(0, e.length() - 5))
                .toList());
        names.sort(String::compareToIgnoreCase);
        return names;
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
    public void useAgent(String agentName, String sessionName, AbortSignal abortSignal) {
        if (!isFunctionCalling()) {
            bail("Please enable function calling before using the agent.");
        }
        if (agent != null) {
            bail("Already in a agent, please run '.exit agent' first to exit the current agent.");
        }

        Agent agent = Agent.init(this, agentName, abortSignal);

        String session = sessionName;
        if (isBlank(session)) {
            if (!isMacroFlag()) {
                session = agent.agentPrelude();
            }
        }

        rag = agent.getRag();
        this.agent = agent;
        if (!isBlank(session)) {
            useSession(session);
        } else {
            initAgentSharedVariables();
        }
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
    public void usePrompt(String prompt) {
        Role role = Role.create(TEMP_ROLE_NAME, prompt);
        role.setModel(currentModel());
        useRoleObj(role);
    }

    // pub fn use_role(&mut self, name: &str) -> Result<()> {
    //    let role = self.retrieve_role(name)?;
    //    self.use_role_obj(role)
    // }
    public void useRole(String name) {
        Role role = retrieveRole(name);
        useRoleObj(role);
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
    private void useRoleObj(Role role) {
        if (agent != null) {
            bail("Cannot perform this operation because you are using a agent");
            return;
        }
        if (session != null) {
            session.guardEmpty();
            session.setRole(role);
        } else {
            this.role = role;
        }
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
    public void useSession(String sessionName) {
        if (session != null) {
            bail("Already in a session, please run '.exit session' first to exit the current session.");
            return;
        }

        if (isBlank(sessionName) || TEMP_SESSION_NAME.equals(sessionName)) {
            File sessionFile = sessionFile(TEMP_SESSION_NAME);
            if (sessionFile.exists()) {
                if (!sessionFile.delete()) {
                    bail(format("Failed to cleanup previous '{}' session", TEMP_SESSION_NAME));
                    return;
                }
            }
            session = Session.create(this, TEMP_SESSION_NAME);
        } else {
            File sessionFile = sessionFile(sessionName);
            if (!sessionFile.exists()) {
                session = Session.create(this, sessionName);
            } else {
                session = Session.load(this, sessionName, sessionFile);
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
                    boolean ans = new Confirm("Start a session that incorporates the last question and answer?")
                        .setDefaultValue(false)
                        .prompt();
                    if (ans) {
                        session.addMessage(input, output);
                    }
                }
            }
        }

        initAgentSessionVariables(newSession);
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
    public void useRag(String name, AbortSignal abortSignal) {
        if (agent != null) {
            bail("Cannot perform this operation because you are using a agent");
            return;
        }

        Rag rag;
        if (isBlank(name)) {
            File ragPath = ragFile(TEMP_RAG_NAME);
            if (ragPath.exists()) {
                if (!ragPath.delete()) {
                    println("Failed to cleanup previous '{}' rag", TEMP_RAG_NAME);
                }
            }
            rag = Rag.init(this, TEMP_RAG_NAME, ragPath, Collections.emptyList(), abortSignal);
        } else {
            File ragPath = ragFile(name);
            if (!ragPath.exists()) {
                if (WorkingMode.isCmd(workingMode)) {
                    bail("Unknown RAG '{}'", name);
                    return;
                }
                rag = Rag.init(this, name, ragPath, Collections.emptyList(), abortSignal);
            } else {
                rag = Rag.load(this, name, ragPath);
            }
        }

        this.rag = rag;
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
    private void initAgentSharedVariables() {
        if (agent == null) {
            return;
        }

        if (!isEmpty(agent.definedVariables()) && isEmpty(agent.getSharedVariables())) {
            Map<String, String> configVariables = new HashMap<>(agent.configVariables());
            if (!isEmpty(agentVariables)) {
                configVariables.putAll(agentVariables);
            }

            Map<String, String> newVariables = Agent.initAgentVariables(agent.definedVariables(), configVariables, infoFlag);
            agent.setSharedVariables(newVariables);
        }

        if (!infoFlag) {
            agent.updateSharedDynamicInstructions(false);
        }
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
    public void setModel(String modelId) {
        Model model = Model.retrieveModel(this, modelId, ModelType.Chat);
        RoleLike roleLike = roleLikeMut();
        if (roleLike != null) {
            roleLike.setModel(model);
        } else{
            this.model = model;
        }
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
    private void initAgentSessionVariables(boolean newSession) {

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
    public Role retrieveRole(String name) {
        List<String> names = listRoles(false);

        Role role;
        if (names.contains(name)) {
            File path = roleFile(name);
            String content = readFile(path);
            role = Role.create(name, content);
        } else {
            role = Role.builtin(name);
        }
        Model currentModel = currentModel();
        if (!isBlank(role.getModelId())) {
            String modelId = role.getModelId();
            if (!Objects.equals(currentModel.id(), modelId)) {
                Model model = Model.retrieveModel(this, modelId, ModelType.Chat);
                role.setModel(model);
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
        return role;
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
    public void emptySession() {
        if (session != null) {
            if (agent != null) {
                session.syncAgent(agent);
            }
            session.clearMessages();
        } else {
            bail("No session");
        }
        discontinuousLastMessage();
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
    public String info() {
        if (agent != null) {
            String output = agent.export(this);
            if (session != null) {
                String info = String.join("\n", Arrays.stream(session.export().split("\n"))
                    .map(e -> format("  {}", e))
                    .toList());
                return format("{}session:\n{}", output, info);
            } else {
                return output;
            }
        } else if (session != null) {
            return session.export();
        } else if (role != null) {
            return role.export();
        } else if (rag != null) {
            return rag.export();
        } else {
            return sysinfo();
        }
    }

    private void loadEnvs(){ }
    private void loadFunctions() { }
    private void setupModel() { }
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
    private String sysinfo() {
        String displayPath = configPath.toString();
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
            new Tuple<>("config_file", configFile),
            //new Tuple<>("env_file", envFile()),
            new Tuple<>("roles_dir", rolesDir()),
            new Tuple<>("sessions_dir", sessionsDir()),
            new Tuple<>("rags_dir", ragsDir()),
            new Tuple<>("macros_dir", macrosDir()),
            new Tuple<>("functions_dir", functionsDir()),
            new Tuple<>("messages_file", messagesFile())
            //new Tuple<>("logs_path", logsPath)
        );
        String output = String.join("\n", items.stream()
            .map(e -> format("{}{}", e.first(), e.second()))
            .toList());
        return output;
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

        println("✓ Saved the config file to '{}'.\n", configFile.getAbsolutePath());
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
