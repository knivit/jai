package com.tsoft.jai.config;

import com.tsoft.jai.config.dto.ClientConfig;
import com.tsoft.jai.config.dto.Config;
import com.tsoft.jai.http.HttpMethod;
import com.tsoft.jai.http.HttpUtils;
import com.tsoft.jai.provider.Provider;
import com.tsoft.jai.provider.openai.api.v1.Model;
import com.tsoft.jai.provider.openai.api.v1.Models;
import com.tsoft.jai.std.Result;
import com.tsoft.jai.user.UserInput;
import com.tsoft.jai.utils.SerdeJson;
import com.tsoft.jai.utils.SerdeYaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.http.HttpUtils.*;
import static com.tsoft.jai.std.Result.*;
import static com.tsoft.jai.user.terminal.TerminalUtils.*;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public class ConfigService {

    public static Result<Config> loadOrCreate() {
        return configFile()
            .then(path -> Files.exists(path) ? load(path) : create(path));
    }

    private static Result<Config> load(Path file) {
        return SerdeYaml.fromFile(file, Config.class);
    }

    private static Result<?> save(Path file, Config cfg) {
        return SerdeYaml.toFile(file, cfg)
            .then(_ -> println("✓ Saved the config file to '{}'.", file));
    }

    private static Result<Config> create(Path file) {
        class ConfigContext {
            final List<String> providers = List.of(
                "openai-compatible",
                "openai",
                "gemini",
                "claude",
                "cohere",
                "azure-openai",
                "vertexai");

            final List<String> allModels = new ArrayList<>();

            String provider;
            String providerName;
            String apiBase;
            String apiKey;
            List<String> models;
            String model;

            public Result<ConfigContext> setProvider(String provider) {
                this.provider = providers.contains(provider) ? provider : providers.getFirst();
                return Ok(this);
            }

            public Result<ConfigContext> setProviderName(String providerName) {
                this.providerName = providerName;
                return Ok(this);
            }

            public Result<ConfigContext> setApiBase(String apiBase) {
                this.apiBase = apiBase;
                return Ok(this);
            }

            public Result<ConfigContext> setApiKey(String apiKey) {
                this.apiKey = apiKey;
                return Ok(this);
            }

            // HTTP GET http://localhost:11434/v1/models
            // {
            //  "object": "list",
            //  "data": [
            //    {
            //      "id": "rnj-1:latest",
            //      "object": "model",
            //      "created": 1771694631,
            //      "owned_by": "library"
            //    },
            //    ...
            // }
            public Result<ConfigContext> setModels(List<String> models) {
                allModels.addAll(models);
                return Ok(this);
            }

            public Result<ConfigContext> setSelectedModels(Collection<String> list) {
                models = new ArrayList<>();
                if (!isEmpty(list)) {
                    models.addAll(list);
                }

                models = Collections.unmodifiableList(models);
                return Ok(this);
            }

            public Result<ConfigContext> setModel(String model) {
                this.model = model;
                return Ok(this);
            }

            public Result<Config> createConfig() {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setType(provider);
                clientConfig.setName(providerName);
                clientConfig.setApiBase(apiBase);
                clientConfig.setModels(models);

                Config cfg = new Config();
                cfg.setModel(model);
                cfg.setClients(List.of(clientConfig));

                return Ok(cfg);
            }
        }

        ConfigContext ctx = new ConfigContext();

        return Ok()
            .then(_ -> readLine("No config file, create a new one? (Y/n)"))
            .then(UserInput::getMessage)
            .then(msg -> (isBlank(msg) || "Y".equalsIgnoreCase(msg)) ? Ok() : Err("Operation aborted."))
            .then(_ -> select("API Provider (required):", ctx.providers))
            .then(UserInput::getMessage)
            .then(ctx::setProvider)
            .then(_ -> readLine("Provider Name (required):"))
            .then(UserInput::getMessage)
            .then(ctx::setProviderName)
            .then(_ -> readLine("API Base (required):"))
            .then(UserInput::getMessage)
            .then(ctx::setApiBase)
            .then(_ -> readLine("API Key (optional):"))
            .then(UserInput::getMessage)
            .then(ctx::setApiKey)
            .then(_ -> Provider.getModels(ctx.providerName, ctx.apiBase))
            .then(ctx::setModels)
            .then(_ -> multiSelect("LLMs to include (required):", ctx.allModels))
            .then(UserInput::getList)
            .then(ctx::setSelectedModels)
            .then(_ -> select("Default Model (required):", ctx.models))
            .then(UserInput::getMessage)
            .then(ctx::setModel)
            .then(ConfigContext::createConfig)
            .then(cfg -> save(file, cfg))
            .then(_ -> load(file));
    }

    // Returns the path to the JAI config file.
    private static Result<Path> configFile() {
        return configDir()
            .then(path -> Ok(path.resolve("config.yaml")));
    }

    // Returns the path to the JAI config directory.
    // The returned value depends on the operating system and is either a Some, containing a value from the following table, or a None.
    // Platform            Value                                    Example
    // Linux               $HOME/.config/jai                        /home/alice/.config/jai
    // macOS               $HOME/Library/Application Support\jai    /Users/Alice/Library/Application Support/jai
    // Windows             {FOLDERID_RoamingAppData}/jai            C:\Users\Alice\AppData\Roaming\jai
    private static Result<Path> configDir() {
        return userConfigDir()
            .then(path -> {
                path = path.resolve("jai");
                return createDirIfNotExists(path);
            });
    }

    // Returns the path to the user's config directory.
    // The returned value depends on the operating system and is either a Some, containing a value from the following table, or a None.
    // Platform            Value                                    Example
    // Linux               $HOME/.config                            /home/alice/.config
    // macOS               $HOME/Library/Application Support        /Users/Alice/Library/Application Support
    // Windows             {FOLDERID_RoamingAppData}                C:\Users\Alice\AppData\Roaming
    private static Result<Path> userConfigDir() {
        return userHomeDir()
            .then(path -> {
                path = path.resolve(".config");
                return createDirIfNotExists(path);
            });
    }

    // Returns the path to the user's home directory.
    // The returned value depends on the operating system and is either a Some, containing a value from the following table, or a None.
    // Platform            Value                                    Example
    // Linux               $HOME                                    /home/alice
    // macOS               $HOME                                    /Users/Alice
    // Windows             {FOLDERID_Profile}                       C:\Users\Alice
    private static Result<Path> userHomeDir() {
        String path = System.getProperty("user.home");
        if (path == null) {
            return Err("User's home dir is undefined");
        } else {
            return Ok(Path.of(path));
        }
    }

    private static Result<Path> createDirIfNotExists(Path path) {
        try {
            if (!Files.isDirectory(path)) {
                Files.createDirectories(path);
            }
            return Ok(path);
        } catch (Exception ex) {
            return Err(ex);
        }
    }
}
