package com.tsoft.jai.provider;

import com.tsoft.jai.provider.openai.api.OpenAiApi;
import com.tsoft.jai.std.Result;

import java.util.List;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class Provider {

    public static Result<List<String>> getModels(String providerName, String apiBase) {
        if (isBlank(providerName)) {
            return Err("Empty provider name");
        }

        providerName = providerName.toLowerCase();
        return switch (providerName) {
            case "openapi-compatible" -> OpenAiApi.getModels(apiBase);
            default -> Err("Unknown provider '{}'", providerName);
        };
    }

    private Provider() { }
}
