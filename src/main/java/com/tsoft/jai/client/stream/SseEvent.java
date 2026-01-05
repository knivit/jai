package com.tsoft.jai.client.stream;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SseEvent {

    private Object value;

    public static SseEvent Text(String text) {
        return new SseEvent().setValue(text);
    }

    public static SseEvent Done() {
        return new SseEvent().setValue("Done");
    }
}
