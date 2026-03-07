package com.tsoft.jai.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HttpResponseContext {

    private final int httpCode;
    private final String body;
}
