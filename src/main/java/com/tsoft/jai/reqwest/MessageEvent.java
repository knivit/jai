package com.tsoft.jai.reqwest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;

@Data
@Accessors(chain = true)
public class MessageEvent {

    /// The event name if given
    private String event;
    /// The event data
    private String data;
    /// The event id if given
    private String id;
    /// Retry duration if given
    private Duration retry;
}
