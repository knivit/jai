package com.tsoft.jai.tokio.sync.oneshot;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.tokio.sync.TryRecvError;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public class OneshotChannel<T> {

    private final BlockingQueue<Result<T>> channel = new LinkedBlockingQueue<>(1);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public Result<T> send(Result<T> msg) {
        if (isClosed()) {
            return Result.Err(TryRecvError.Closed());
        } else {
            try {
                channel.add(msg);
                return Ok();
            } catch (Exception ex) {
                return Err(ex);
            }
        }
    }

    public Result<T> tryRecv() {
        if (isClosed()) {
            return Err(TryRecvError.Closed());
        } else {
            Result<T> msg = channel.poll();
            return (msg == null) ? Err(TryRecvError.Empty()) : msg;
        }
    }

    public void close() {
        closed.set(true);
    }

    public boolean isClosed() {
        return closed.get();
    }
}
