package com.tsoft.jai.mods.config.ser;

import lombok.Data;

import java.util.List;

@Data
public class ConfigSer {

    private String type;
    private String provider;
    private String model;
    private Boolean stream;
    private Float temperature;
    private List<ClientConfigSer> clients;
}