package com.tsoft.jai.mods.config.ser;

import lombok.Data;

import java.util.List;

@Data
public class ClientConfigSer {

    private String type;
    private String name;
    private String apiBase;
    private List<String> models;
}
