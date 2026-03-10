package com.tsoft.jai.mods.provider;

import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.mods.provider.openai.OpenAiMod;
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
            case "openai-compatible" -> OpenAiMod.getModels(apiBase);
            default -> Err("Unknown provider '{}'", provider);
        };
    }

    public static Result<Session> chat(Session session, String message) {
        return OpenAiMod.chat(session, message);
    }

    private ProviderMod() { }
}
