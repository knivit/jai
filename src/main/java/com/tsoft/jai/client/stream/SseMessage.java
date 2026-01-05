package com.tsoft.jai.client.stream;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SseMessage {

    private String event;
    private String data;
}
