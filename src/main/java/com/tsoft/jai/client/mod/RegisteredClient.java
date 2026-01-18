package com.tsoft.jai.client.mod;

import com.tsoft.jai.client.Client;
import com.tsoft.jai.client.model.Model;
import com.tsoft.jai.config.ClientConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class RegisteredClient {

    private final String module;
    private final String name;
    private final Supplier<ClientConfig> configSupplier;
    private final BiFunction<ClientConfig, Model, Client> clientSupplier;
}
