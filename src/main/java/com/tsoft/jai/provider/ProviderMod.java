package com.tsoft.jai.provider;

import com.tsoft.jai.mods.session.dto.Session;
import com.tsoft.jai.provider.openai.api.OpenAiApi;
import com.tsoft.jai.std.Result;

import java.util.List;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class ProviderMod {

    public static Result<List<String>> getModels(String provider, String apiBase) {
        if (isBlank(provider)) {
            return Err("Empty provider name");
        }

        return switch (provider.toLowerCase()) {
            case "openai-compatible" -> OpenAiApi.getModels(apiBase);
            default -> Err("Unknown provider '{}'", provider);
        };
    }

    public static Result<Session> chat(Session session, String message) {
        return OpenAiApi.chat(session, message);
    }

    private ProviderMod() { }
}
