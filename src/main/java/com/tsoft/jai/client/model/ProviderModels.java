package com.tsoft.jai.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProviderModels {

    private String provider;
    private List<ModelData> models;
}
