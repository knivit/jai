package com.tsoft.jai.serde.serdeyaml;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.client.openaicompatible.OpenAICompatibleClient;
import com.tsoft.jai.inquire.TestTerminal;
import com.tsoft.jai.reqwest.TestHttpClient;
import com.tsoft.jai.serde.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tsoft.jai.anyhow.Result.isOk;
import static com.tsoft.jai.client.common.Common.setClientModelsConfig;
import static com.tsoft.jai.serde.Value.json;
import static org.assertj.core.api.Assertions.assertThat;

class SerdeYamlTest {

    @Test
    void value_to_string() {
        TestTerminal.init();
        TestTerminal.setInput("lfm2.5-thinking");

        Value clientsConfig = json(
            "type", OpenAICompatibleClient.NAME,
            "name", "ollama"
        );
        clientsConfig.put("api_base", "http://localhost:11434/v1");

        Result<String> res = setClientModelsConfig(clientsConfig, "ollama");
        assertThat(isOk(res)).isTrue();

        Value clients = new Value(List.of(clientsConfig));

        Value config = new Value();
        config.put("model", "ollama:lfm2.5-thinking");
        config.put("clients", clients);

        res = SerdeYaml.toString(config).withContext(() -> "Failed to create config");
        assertThat(isOk(res)).isTrue();

        assertThat(res.getValue()).isEqualTo("""
            model: ollama:lfm2.5-thinking
            clients:
              - type: openai-compatible
                name: ollama
                api_base: http://localhost:11434/v1
                models:
                  - name: lfm2.5-thinking
            """);
    }
}