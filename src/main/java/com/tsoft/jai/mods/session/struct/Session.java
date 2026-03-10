package com.tsoft.jai.mods.session.struct;

import com.tsoft.jai.std.Result;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.tsoft.jai.std.Result.Ok;

@Getter
public class Session {

    private String apiBase;
    private String model;
    private List<Message> messages;
    private Float temperature;
    private ChatStatistics stats;

    public Result<Session> setApiBase(String apiBase) {
        this.apiBase = apiBase;
        return Ok(this);
    }

    public Result<Session> setModel(String model) {
        this.model = model;
        return Ok(this);
    }

    public Result<Session> addMessage(String role, String message) {
        messages = (messages == null) ? new ArrayList<>() : messages;

        messages.add(new Message()
            .setRole(role)
            .setContent(message));

        return Ok(this);
    }
}
