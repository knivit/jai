package com.tsoft.jai.config.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Config {

    private String model;
    private List<ClientConfig> clients;
}
