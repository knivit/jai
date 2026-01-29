package com.tsoft.jai.reqwest;

import lombok.Getter;

@Getter
public class Event {

    public enum EventEnum {
        Open,
        Message
    }

    private EventEnum type;
    private MessageEvent message;
}
