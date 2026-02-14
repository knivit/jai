package com.tsoft.jai.tokio.sync.oneshot;

import com.tsoft.jai.anyhow.Result;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Sender<T> {

    private final OneshotChannel<T> channel;

    public Result<T> send(Result<T> msg) {
        return channel.send(msg);
    }
}
