package com.tsoft.jai.mods.provider;

import com.tsoft.jai.mods.config.struct.Config;
import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.mods.provider.openai.OpenAiMod;
import com.tsoft.jai.std.Result;

import java.util.List;
import java.util.function.Consumer;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.user.terminal.TerminalUtils.println;
import static com.tsoft.jai.utils.StringUtils.isBlank;

public final class ProviderMod {

    public static Result<List<String>> getModels(String type, String apiBase) {
        if (isBlank(type)) {
            return Err("Empty provider type");
        }

        return switch (type.toLowerCase()) {
            case "openai-compatible" -> OpenAiMod.getModels(apiBase);
            default -> Err("Unknown provider type '{}'", type);
        };
    }

    public static Result<?> chat(Config cfg, Session ses, String msg) {
        return Ok()
            .then(_ -> OpenAiMod.chat(cfg, ses, msg))
            .then(_ -> Ok(ses.getMessages().getLast().getContent()))
            .then(_ -> println());
    }

    public static Result<?> chatStream(Config cfg, Session ses, String msg, Consumer<String> onChunk) {
        return Ok()
            .then(_ -> OpenAiMod.chatStream(cfg, ses, msg, onChunk))
            .then(_ -> println());
    }

    private ProviderMod() { }
}
