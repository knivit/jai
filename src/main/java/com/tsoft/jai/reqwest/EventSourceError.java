package com.tsoft.jai.reqwest;

import lombok.*;
import lombok.experimental.Accessors;

import static com.tsoft.jai.utils.base.StringUtils.format;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class EventSourceError {

    public enum EventSourceErrorEnum {
        StreamEnded,
        InvalidStatusCode,
        InvalidContentType
    }

    private final EventSourceErrorEnum type;
    private int status;
    private String text;
    private String headerValue;

    public static EventSourceError StreamEnded() {
        return new EventSourceError(EventSourceErrorEnum.StreamEnded);
    }

    public static EventSourceError InvalidStatusCode(int status, String text) {
        return new EventSourceError(EventSourceErrorEnum.InvalidStatusCode).setStatus(status).setText(text);
    }

    public static EventSourceError InvalidContentType(String headerValue, String text) {
        return new EventSourceError(EventSourceErrorEnum.InvalidContentType).setHeaderValue(headerValue).setText(text);
    }

    @Override
    public String toString() {
        return switch (type) {
            case StreamEnded -> format("{}", EventSourceErrorEnum.StreamEnded);
            case InvalidStatusCode -> format("{} (status={}, text={})", EventSourceErrorEnum.InvalidStatusCode, status, text);
            case InvalidContentType -> format("{} (headerValue={}, text={})", EventSourceErrorEnum.InvalidContentType, headerValue, text);
        };
    }
}
