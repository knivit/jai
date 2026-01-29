package com.tsoft.jai.client.stream;

import com.tsoft.jai.anyhow.Result;

public interface StreamHandler {

    Result<Boolean> handle(SseMessage message);
}
