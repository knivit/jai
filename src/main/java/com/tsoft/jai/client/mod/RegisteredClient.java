package com.tsoft.jai.client.mod;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.ClientConfig;
import com.tsoft.jai.config.Config;
import com.tsoft.jai.utils.base.Triple;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.Function;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class RegisteredClient {

    private final String module;
    private final String type;
    private final Supplier<ClientConfig> configSupplier;
    private final Function<Triple<ClientConfig, Config, Model>, Client> clientSupplier;
}
