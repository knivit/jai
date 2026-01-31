package com.tsoft.jai.client.stream;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class SseEvent {

    public enum SseEventEnum {
        Text,
        Done
    }

    private final SseEventEnum type;
    private String text;

    public static SseEvent Text(String text) {
        return new SseEvent(SseEventEnum.Text).setText(text);
    }

    public static SseEvent Done() {
        return new SseEvent(SseEventEnum.Done);
    }
}
