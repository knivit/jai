package com.tsoft.jai.mods.session.dto;

import com.tsoft.jai.std.Result;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static com.tsoft.jai.std.Result.Ok;

@Data
public class Session {

    private String apiBase;
    private String model;
    private List<Message> messages;
    private Float temperature;
    private ChatStatistics stats;

    public Result<Session> addMessage(String role, String message) {
        messages = (messages == null) ? new ArrayList<>() : messages;

        messages.add(new Message()
            .setRole(role)
            .setContent(message));

        return Ok(this);
    }
}
