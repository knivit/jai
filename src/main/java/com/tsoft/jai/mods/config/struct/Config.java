package com.tsoft.jai.mods.config.struct;

import com.tsoft.jai.mods.config.ser.ClientConfigSer;
import com.tsoft.jai.mods.config.ser.ConfigSer;
import com.tsoft.jai.std.Result;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;

@Getter
public class Config {

    final List<String> providers = List.of(
            "openai-compatible",
            "openai",
            "gemini",
            "claude",
            "cohere",
            "azure-openai",
            "vertexai");

    final List<String> allModels = new ArrayList<>();

    private String provider;
    private String providerName;
    private String apiBase;
    private String apiKey;
    private List<String> models;
    private String model;

    public static Result<Config> fromSer(ConfigSer ser) {
        Config cfg = new Config();

        return Ok()
            .then(_ -> isEmpty(ser.getModel()) ? Err("Invalid config, model can't be empty") : Ok(ser.getModel()))
            .then(model -> {
                String[] parts = model.split(":");
                if (parts.length == 2) {
                    return Ok()
                        .then(_ -> cfg.setProvider(parts[0]))
                        .then(_ -> cfg.setModel(parts[1]));
                } else {
                    return Err("Invalid config, model must be in <provider>:<model> format");
                }})
            .then(_ -> isEmpty(ser.getClients()) ? Err("Invalid config, clients can't be empty") : Ok(ser.getClients()))
            .then(clients -> Ok(clients.getFirst()))
            .then(client -> Ok()
                .then(_ -> cfg.setProviderName(client.getType()))
                .then(_ -> cfg.setApiBase(client.getApiBase()))
                .then(_ -> cfg.setModels(client.getModels()))
            );
    }

    public Result<ConfigSer> toSer() {
        ClientConfigSer clientConfig = new ClientConfigSer();
        clientConfig.setType(provider);
        clientConfig.setName(providerName);
        clientConfig.setApiBase(apiBase);
        clientConfig.setModels(models);

        ConfigSer cfg = new ConfigSer();
        cfg.setModel(model);
        cfg.setClients(List.of(clientConfig));

        return Ok(cfg);
    }

    public Result<Config> setProvider(String provider) {
        this.provider = providers.contains(provider) ? provider : providers.getFirst();
        return Ok(this);
    }

    public Result<Config> setProviderName(String providerName) {
        this.providerName = providerName;
        return Ok(this);
    }

    public Result<Config> setApiBase(String apiBase) {
        this.apiBase = apiBase;
        return Ok(this);
    }

    public Result<Config> setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return Ok(this);
    }

    public Result<Config> setAllModels(List<String> models) {
        allModels.addAll(models);
        return Ok(this);
    }

    public Result<Config> setModels(Collection<String> list) {
        models = new ArrayList<>();
        if (!isEmpty(list)) {
            models.addAll(list);
        }

        models = Collections.unmodifiableList(models);
        return Ok(this);
    }

    public Result<Config> setModel(String model) {
        this.model = providerName + ":" + model;
        return Ok(this);
    }
}
