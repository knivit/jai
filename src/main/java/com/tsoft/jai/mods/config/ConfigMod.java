package com.tsoft.jai.mods.config;

import com.tsoft.jai.mods.cli.struct.Cli;
import com.tsoft.jai.mods.config.ser.ConfigSer;
import com.tsoft.jai.mods.config.struct.Config;
import com.tsoft.jai.mods.provider.ProviderMod;
import com.tsoft.jai.std.Result;
import com.tsoft.jai.user.UserInput;
import com.tsoft.jai.utils.SerdeYaml;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.tsoft.jai.std.Result.*;
import static com.tsoft.jai.user.terminal.TerminalUtils.*;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public class ConfigMod {

    public static Result<Config> loadOrCreate(Cli cli) {
        return Ok()
            .then(_ -> configFile())
            .then(path -> Files.exists(path) ? load(path) : create(path));
    }

    private static Result<Config> load(Path file) {
        return Ok()
            .then(_ -> SerdeYaml.fromFile(file, ConfigSer.class))
            .then(Config::fromSer);
    }

    private static Result<?> save(Path file, Config cfg) {
        return Ok()
            .then(_ -> cfg.toSer())
            .then(ser -> SerdeYaml.toFile(file, ser))
            .then(_ -> println("✓ Saved the config file to '{}'.", file));
    }

    private static Result<Config> create(Path file) {
        Config cfg = new Config();

        return Ok()
            .then(_ -> readLine("No config file, create a new one? (Y/n)"))
            .then(UserInput::getMessage)
            .then(msg -> (isBlank(msg) || "Y".equalsIgnoreCase(msg)) ? Ok() : Err("Operation aborted."))
            .then(_ -> select("API Provider (required):", cfg.getProviders()))
            .then(UserInput::getMessage)
            .then(cfg::setProvider)
            .then(_ -> readLine("Provider Name (required):"))
            .then(UserInput::getMessage)
            .then(cfg::setProviderName)
            .then(_ -> readLine("API Base (required):"))
            .then(UserInput::getMessage)
            .then(cfg::setApiBase)
            .then(_ -> readLine("API Key (optional):"))
            .then(UserInput::getMessage)
            .then(cfg::setApiKey)
            .then(_ -> ProviderMod.getModels(cfg.getProvider(), cfg.getApiBase()))
            .then(cfg::setAllModels)
            .then(_ -> multiSelect("LLMs to include (required):", cfg.getAllModels()))
            .then(UserInput::getList)
            .then(cfg::setModels)
            .then(_ -> select("Default Model (required):", cfg.getModels()))
            .then(UserInput::getMessage)
            .then(cfg::setModel)
            .then(_ -> save(file, cfg))
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
