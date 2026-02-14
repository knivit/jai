package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.anyhow.Result;
import lombok.RequiredArgsConstructor;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

@RequiredArgsConstructor
public class UnboundedReceiver<T> {

    private final UnboundedChannel<T> channel;

    public Result<T> tryRecv() {
        return channel.tryRecv();
    }

    public Result<T> recv() {
        try {
            return Ok(channel.receiveBlocking());
        } catch (InterruptedException ie) {
            return Err(ie);
        }
    }
}
