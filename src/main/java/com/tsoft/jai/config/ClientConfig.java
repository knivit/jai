package com.tsoft.jai.config;

import com.tsoft.jai.client.common.ExtraConfig;
import com.tsoft.jai.client.common.RequestPatch;
import com.tsoft.jai.client.model.ModelData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ClientConfig implements Cloneable {

    private String name;
    private String apiKey;
    private String apiBase;
    private String organizationId;
    //#[serde(default)]
    private List<ModelData> models;
    private RequestPatch patch;
    private ExtraConfig extra;

    @Override
    public ClientConfig clone() {
        try {
            ClientConfig clone = (ClientConfig) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
