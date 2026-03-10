package com.tsoft.jai.mods.provider.openai;

import com.tsoft.jai.http.HttpMethod;
import com.tsoft.jai.http.HttpUtils;
import com.tsoft.jai.mods.session.struct.Session;
import com.tsoft.jai.mods.provider.openai.api.v1.chat.rq.ChatMessageRq;
import com.tsoft.jai.mods.provider.openai.api.v1.chat.rq.ChatRq;
import com.tsoft.jai.mods.provider.openai.api.v1.chat.rs.ChatRs;
import com.tsoft.jai.mods.provider.openai.api.v1.model.rs.ModelRs;
import com.tsoft.jai.mods.provider.openai.api.v1.model.rs.ModelsRs;
import com.tsoft.jai.std.Result;
import com.tsoft.jai.std.ValueRef;
import com.tsoft.jai.utils.SerdeJson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tsoft.jai.http.HttpUtils.buildHttpRequestContext;
import static com.tsoft.jai.std.Result.*;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;

public final class OpenAiMod {

    // HTTP GET http://localhost:11434/v1/models
    // {
    //  "object": "list",
    //  "data": [
    //    {
    //      "id": "model-identifier",
    //      "object": "model",
    //      "created": 1771694631,
    //      "owned_by": "library"
    //    },
    //    ...
    // }
    public static Result<List<String>> getModels(String apiBase) {
        return Ok()
            .then(_ -> buildHttpRequestContext())
            .then(ctx -> ctx.setMethod(HttpMethod.GET))
            .then(ctx -> ctx.setUrl(apiBase + "/v1/models"))
            .then(ctx -> ctx.setHeader("accept", "application/json"))
            .then(HttpUtils::buildHttpRequest)
            .then(HttpUtils::sendHttpRequest)
            .then(ctx -> ctx.getHttpCode() == 200 ? Ok(ctx.getBody()) : Err("Request for models failed."))
            .then(OpenAiMod::toModels);
    }

    // HTTP POST http://localhost:11434/v1/chat/completions
    // {
    //   "model": "model-identifier",
    //   "messages": [
    //      {"role": "system", "content": "Always answer in rhymes."},
    //      {"role": "user", "content": "Introduce yourself."}
    //    ],
    //    "temperature": 0.7
    // }
    public static Result<Session> chat(Session session, String message) {
        ValueRef<String> body = new ValueRef<>();

        return Ok()
            .then(_ -> toChat(session, message))
            .then(chat -> chat.setStream(false))
            .then(SerdeJson::toString)
            .then(body::set)
            .then(_ -> buildHttpRequestContext())
            .then(ctx -> ctx.setMethod(HttpMethod.POST))
            .then(ctx -> ctx.setUrl(session.getApiBase() + "/v1/chat/completions"))
            .then(ctx -> ctx.setHeader("accept", "application/json"))
            .then(ctx -> ctx.setHeader("content-type", "application/json"))
            .then(ctx -> ctx.setBody(body.get()))
            .then(HttpUtils::buildHttpRequest)
            .then(HttpUtils::sendHttpRequest)
            .then(ctx -> ctx.getHttpCode() == 200 ? Ok(ctx.getBody()) : Err("Request for chat failed."))
            .then(json -> updateSession(session, json));
    }

    private static Result<Session> updateSession(Session session, String json) {
        return Ok()
            .then(_ -> SerdeJson.fromStr(json, ChatRs.class))
            .then(chat -> session.addMessage("system", chat.getChoices().getFirst().getMessage()));
    }

    private static Result<List<String>> toModels(String json) {
        return Ok()
            .then(_ -> SerdeJson.fromStr(json, ModelsRs.class))
            .then(models -> Ok(models.getData().stream()
                .map(ModelRs::getId)
                .toList()));
    }

    private static Result<ChatRq> toChat(Session session, String message) {
        return Ok(new ChatRq())
            .then(chat -> isEmpty(session.getModel()) ? Err("model can't be empty") : chat.setModel(session.getModel()))
            .then(chat -> chat.setMessages(toChatMessages(session, message)))
            .then(chat -> chat.setTemperature(session.getTemperature()));
    }

    private static List<ChatMessageRq> toChatMessages(Session session, String message) {
        List<ChatMessageRq> historyMessages = isEmpty(session.getMessages()) ?
            Collections.emptyList() : session.getMessages().stream()
                .map(e -> new ChatMessageRq()
                    .setRole(e.getRole())
                    .setContent(e.getContent()))
                    .toList();

        List<ChatMessageRq> chatMessages = new ArrayList<>(historyMessages);

        chatMessages.add(new ChatMessageRq()
                .setRole("user")
                .setContent(message));

        return chatMessages;
    }

    private OpenAiMod() { }
}
