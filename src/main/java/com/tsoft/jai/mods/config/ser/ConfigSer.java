package com.tsoft.jai.mods.config.ser;

import lombok.Data;

import java.util.List;

@Data
public class ConfigSer {

    private String model;
    private List<ClientConfigSer> clients;
}