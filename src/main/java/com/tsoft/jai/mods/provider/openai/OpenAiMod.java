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
    // Response:
    // {
    //  "id": "chatcmpl-qb9qd6rjrcwas4nsb9gf",
    //  "object": "chat.completion",
    //  "created": 1773164882,
    //  "model": "google/gemma-3-4b",
    //  "choices": [
    //    {
    //      "index": 0,
    //      "message": {
    //        "role": "assistant",
    //        "content": "Hello there! How’s your day going so far? 😊 \n\nIs there anything I can help you with today, or were you just saying hello?",
    //        "tool_calls": []
    //      },
    //      "logprobs": null,
    //      "finish_reason": "stop"
    //    }
    //  ],
    //  "usage": {
    //    "prompt_tokens": 11,
    //    "completion_tokens": 33,
    //    "total_tokens": 44
    //  },
    //  "stats": {},
    //  "system_fingerprint": "google/gemma-3-4b"
    // }
    public static Result<Session> chat(Session ses, String msg) {
        ValueRef<String> body = new ValueRef<>("");

        return Ok()
            .then(_ -> toChat(ses, msg))
            .then(chat -> chat.setStream(false))
            .then(SerdeJson::toString)
            .then(body::set)
            .then(_ -> buildHttpRequestContext())
            .then(ctx -> ctx.setMethod(HttpMethod.POST))
            .then(ctx -> ctx.setUrl(ses.getApiBase() + "/v1/chat/completions"))
            .then(ctx -> ctx.setHeader("accept", "application/json"))
            .then(ctx -> ctx.setHeader("content-type", "application/json"))
            .then(ctx -> ctx.setBody(body.get()))
            .then(HttpUtils::buildHttpRequest)
            .then(HttpUtils::sendHttpRequest)
            .then(ctx -> ctx.getHttpCode() == 200 ? Ok(ctx.getBody()) : Err("Request for chat failed."))
            .then(json -> updateSession(ses, json));
    }

    private static Result<Session> updateSession(Session ses, String json) {
        return Ok()
            .then(_ -> SerdeJson.fromStr(json, ChatRs.class))
            .then(chat -> Ok(chat.getChoices().getFirst().getMessage()))
            .then(msg -> ses.addMessage(msg.getRole(), msg.getContent()));
    }

    private static Result<List<String>> toModels(String json) {
        return Ok()
            .then(_ -> SerdeJson.fromStr(json, ModelsRs.class))
            .then(models -> Ok(models.getData().stream()
                .map(ModelRs::getId)
                .toList()));
    }

    private static Result<ChatRq> toChat(Session ses, String msg) {
        ChatRq chat = new ChatRq();

        return Ok()
            .then(_ -> isEmpty(ses.getModel()) ? Err("model can't be empty") : chat.setModel(ses.getModel()))
            .then(_ -> ses.addMessage("user", msg))
            .then(_ -> chat.setMessages(toChatMessages(ses)))
            .then(_ -> chat.setTemperature(ses.getTemperature()));
    }

    private static List<ChatMessageRq> toChatMessages(Session ses) {
        List<ChatMessageRq> historyMessages = isEmpty(ses.getMessages()) ?
            Collections.emptyList() : ses.getMessages().stream()
                .map(e -> new ChatMessageRq()
                    .setRole(e.getRole())
                    .setContent(e.getContent()))
                    .toList();

        return new ArrayList<>(historyMessages);
    }

    private OpenAiMod() { }
}
