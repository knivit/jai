package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.anyhow.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import static com.tsoft.jai.anyhow.Result.Ok;

@RequiredArgsConstructor
public class UnboundedSender<T> {

    private final UnboundedChannel<T> channel;

    public Result<?> send(T value) {
        channel.send(value);
        return Ok();
    }
}
