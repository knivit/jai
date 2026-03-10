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

    final List<String> types = List.of(
            "openai-compatible",
            "openai",
            "gemini",
            "claude",
            "cohere",
            "azure-openai",
            "vertexai");

    final List<String> allModels = new ArrayList<>();

    private String type;
    private String provider;
    private String apiBase;
    private String apiKey;
    private List<String> models;
    private String model;
    private Boolean stream;
    private Float temperature;

    public static Result<Config> fromSer(ConfigSer ser) {
        Config cfg = new Config();

        return Ok()
            .then(_ -> isEmpty(ser.getType()) ? Err("Invalid config, type can't be empty") : Ok(ser.getType()))
            .then(type -> Ok().then(_ -> cfg.setType(type)))
            .then(_ -> isEmpty(ser.getProvider()) ? Err("Invalid config, provider can't be empty") : Ok(ser.getProvider()))
            .then(provider -> Ok().then(_ -> cfg.setProvider(provider)))
            .then(_ -> isEmpty(ser.getModel()) ? Err("Invalid config, model can't be empty") : Ok(ser.getModel()))
            .then(model -> Ok().then(_ -> cfg.setModel(model)))
            .then(_ -> isEmpty(ser.getClients()) ? Err("Invalid config, clients can't be empty") : Ok(ser.getClients()))
            .then(clients -> Ok(clients.getFirst()))
            .then(client -> Ok()
                .then(_ -> cfg.setType(client.getType()))
                .then(_ -> cfg.setProvider(client.getProvider()))
                .then(_ -> cfg.setApiBase(client.getApiBase()))
                .then(_ -> cfg.setModels(client.getModels())))
            .then(_ -> cfg.setStream(ser.getStream()))
            .then(_ -> cfg.setTemperature(ser.getTemperature()));
    }

    public Result<ConfigSer> toSer() {
        ClientConfigSer clientConfig = new ClientConfigSer();
        clientConfig.setType(type);
        clientConfig.setProvider(provider);
        clientConfig.setApiBase(apiBase);
        clientConfig.setModels(models);

        ConfigSer cfg = new ConfigSer();
        cfg.setType(type);
        cfg.setProvider(provider);
        cfg.setModel(model);
        cfg.setStream(stream);
        cfg.setTemperature(temperature);
        cfg.setClients(List.of(clientConfig));

        return Ok(cfg);
    }

    public Result<Config> setType(String type) {
        this.type = types.contains(type) ? type : types.getFirst();
        return Ok(this);
    }

    public Result<Config> setProvider(String provider) {
        this.provider = provider;
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
        this.model = model;
        return Ok(this);
    }

    public Result<Config> setStream(Boolean stream) {
        this.stream = stream;
        return Ok(this);
    }

    public Result<Config> setTemperature(Float temperature) {
        this.temperature = temperature;
        return Ok(this);
    }
}
