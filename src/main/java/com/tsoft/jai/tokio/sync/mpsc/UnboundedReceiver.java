package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.anyhow.Result;
import lombok.RequiredArgsConstructor;

import static com.tsoft.jai.anyhow.Result.Err;

@RequiredArgsConstructor
public class UnboundedReceiver<T> {

    private final UnboundedChannel<T> channel;

    public Result<T> tryRecv() {
        return channel.tryRecv();
    }

    public Result<T> recv() {
        try {
            return channel.receiveBlocking();
        } catch (Exception ie) {
            return Err(ie);
        }
    }
}
