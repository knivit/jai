package com.tsoft.jai.config;

import com.tsoft.jai.client.model.ProviderModels;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ModelsOverride {

    private String version;
    private List<ProviderModels> list;
}
