package com.tsoft.jai.client.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ExtraConfig {

    private String proxy;
    private Integer connectTimeout;
}
