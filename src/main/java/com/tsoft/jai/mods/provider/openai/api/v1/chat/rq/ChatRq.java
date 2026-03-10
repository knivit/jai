package com.tsoft.jai.mods.provider.openai.api.v1.chat.rq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tsoft.jai.std.Result;
import lombok.Getter;

import java.util.List;

import static com.tsoft.jai.std.Result.Ok;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRq {

    private String model;
    private List<ChatMessageRq> messages;
    private Float temperature;
    private Boolean stream;

    public Result<ChatRq> setModel(String model) {
        this.model = model;
        return Ok(this);
    }

    public Result<ChatRq> setMessages(List<ChatMessageRq> messages) {
        this.messages = messages;
        return Ok(this);
    }

    public Result<ChatRq> setTemperature(Float temperature) {
        this.temperature = temperature;
        return Ok(this);
    }

    public Result<ChatRq> setStream(Boolean stream) {
        this.stream = stream;
        return Ok(this);
    }
}
