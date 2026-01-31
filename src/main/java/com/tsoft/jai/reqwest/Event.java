package com.tsoft.jai.reqwest;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class Event {

    public enum EventEnum {
        Open,
        Message
    }

    private final EventEnum type;
    private MessageEvent message;

    public static Event Open() {
        return new Event(EventEnum.Open);
    }

    public static Event Message(MessageEvent message) {
        return new Event(EventEnum.Message).setMessage(message);
    }
}
