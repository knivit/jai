package com.tsoft.jai.tokio.sync.oneshot;

import com.tsoft.jai.anyhow.Result;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Receiver<T> {

    private final OneshotChannel<T> channel;

    public Result<T> tryRecv() {
        return channel.tryRecv();
    }
}
