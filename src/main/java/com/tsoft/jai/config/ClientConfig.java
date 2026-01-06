package com.tsoft.jai.config;

import com.tsoft.jai.client.common.ExtraConfig;
import com.tsoft.jai.client.common.RequestPatch;
import com.tsoft.jai.client.model.ModelData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClientConfig {

    private String name;
    private String apiKey;
    private String apiBase;
    private String organizationId;
    //#[serde(default)]
    private List<ModelData> models;
    private RequestPatch patch;
    private ExtraConfig extra;
}
