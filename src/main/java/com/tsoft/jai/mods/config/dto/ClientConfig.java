package com.tsoft.jai.mods.config.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClientConfig {

    private String type;
    private String name;
    private String apiBase;
    private List<String> models;
}
