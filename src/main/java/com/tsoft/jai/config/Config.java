package com.tsoft.jai.config;

import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.agent.Agent;
import com.tsoft.jai.function.Functions;
import com.tsoft.jai.inquire.Confirm;
import com.tsoft.jai.inquire.Inquire;
import com.tsoft.jai.inquire.Select;
import com.tsoft.jai.rag.Rag;
import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Config {

    //#[serde(rename(serialize = "model", deserialize = "model"))]
    //#[serde(default)]
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
    private boolean macroFlag;
    //#[serde(skip)]
    private boolean infoFlag;
    //#[serde(skip)]
    private Map<String, String> agentVariables;

    //#[serde(skip)]
    private Model model;
    //#[serde(skip)]
    private Functions functions;
    //#[serde(skip)]
    private WorkingMode workingMode;
    //#[serde(skip)]
    private LastMessage lastMessage;

    //#[serde(skip)]
    private Role role;
    //#[serde(skip)]
    private Session session;
    //#[serde(skip)]
    private Rag rag;
    //#[serde(skip)]
    private Agent agent;

    private static final String CONFIG_FILE_NAME = "config.yaml";

    //     pub async fn init(working_mode: WorkingMode, info_flag: bool) -> Result<Self> {
    //        let config_path = Self::config_file();
    //        let mut config = if !config_path.exists() {
    //            match env::var(get_env_name("provider"))
    //                .ok()
    //                .or_else(|| env::var(get_env_name("platform")).ok())
    //            {
    //                Some(v) => Self::load_dynamic(&v)?,
    //                None => {
    //                    if *IS_STDOUT_TERMINAL {
    //                        create_config_file(&config_path).await?;
    //                    }
    //                    Self::load_from_file(&config_path)?
    //                }
    //            }
    //        } else {
    //            Self::load_from_file(&config_path)?
    //        };
    //
    //        config.working_mode = working_mode;
    //        config.info_flag = info_flag;
    //
    //        let setup = |config: &mut Self| -> Result<()> {
    //            config.load_envs();
    //
    //            if let Some(wrap) = config.wrap.clone() {
    //                config.set_wrap(&wrap)?;
    //            }
    //
    //            config.load_functions()?;
    //
    //            config.setup_model()?;
    //            config.setup_document_loaders();
    //            config.setup_user_agent();
    //            Ok(())
    //        };
    //        let ret = setup(&mut config);
    //        if !info_flag {
    //            ret?;
    //        }
    //        Ok(config)
    //    }
    public static Config init(WorkingMode workingMode, boolean infoFlag, String configFile) {
        File configPath = Paths.get(CONFIG_FILE_NAME).toAbsolutePath().toFile();
        if (configFile != null && !configFile.isBlank()) {
            configPath = Paths.get(configFile).toFile();
        }

        Config config;
        if (!configPath.exists()) {
            if (Inquire.terminal() != null) {
                createConfigFile(configPath);
                config = loadFromFile(configPath);
            } else {
                config = createDefaultConfig();
            }
        } else {
            config = loadFromFile(configPath);
        }

        config.setWorkingMode(workingMode);
        config.setInfoFlag(infoFlag);

        config.loadEnvs();
        config.loadFunctions();

        config.setupModel();
        config.setupDocumentLoaders();
        config.setupUserAgent();

        return config;
    }

    private void loadEnvs(){ }
    private void loadFunctions() { }
    private void setupModel() { }
    private void setupDocumentLoaders() { }
    private void setupUserAgent() { }
    private static Config createDefaultConfig() { return new Config(); }

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
    private static void createConfigFile(File configPath) {
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

        ensureParentExists(configPath);
        try {
            Files.writeString(configPath.toPath(), configData, StandardOpenOption.CREATE_NEW);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        System.out.printf("✓ Saved the config file to '%s'.%n", configPath.getAbsolutePath());
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

    private static void ensureParentExists(File configPath) {
        try {
            Files.createDirectories(Paths.get(configPath.getParent()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
