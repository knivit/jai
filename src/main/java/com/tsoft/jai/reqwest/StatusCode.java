package com.tsoft.jai.reqwest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class StatusCode {

    public static StatusCode HTTP_OK = StatusCode.of(200);

    private final int value;

    public static StatusCode of(int value) {
        return new StatusCode(value);
    }

    public boolean isSuccess() {
        return (value >= 200) && (value < 400);
    }

    public int asInt() {
        return value;
    }
}
