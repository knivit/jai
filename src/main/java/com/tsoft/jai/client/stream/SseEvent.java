package com.tsoft.jai.client.stream;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SseEvent {

    public enum SseEventEnum {
        Text,
        Done
    }

    private SseEventEnum type;

    private String text;

    public static SseEvent Text(String text) {
        return new SseEvent().setType(SseEventEnum.Text).setText(text);
    }

    public static SseEvent Done() {
        return new SseEvent().setType(SseEventEnum.Done);
    }
}
