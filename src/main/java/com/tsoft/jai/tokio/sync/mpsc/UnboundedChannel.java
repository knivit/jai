package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.tokio.sync.TryRecvError;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public class UnboundedChannel<T> {

    private final ConcurrentLinkedQueue<Result<T>> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Send a message (multiple producers allowed)
    public Result<?> send(T item) {
        if (closed.get()) {
            return Err(TryRecvError.Closed());
        }

        queue.offer(Ok(item)); // unbounded; never blocks
        return Ok();
    }

    // Try to receive a message (single consumer loop)
    // Returns null if channel is closed and queue is empty
    public Result<T> tryRecv() {
        if (isClosed()) {
            return Err(TryRecvError.Closed());
        } else {
            Result<T> msg = queue.poll();
            return (msg == null) ? Err(TryRecvError.Empty()) : msg;
        }
    }

    // Optionally: block-waiting for item (with optional timeout)
    public Result<T> receiveBlocking() {
        while (true) {
            Result<T> item = queue.poll();
            if (item != null) {
                return item;
            }

            if (closed.get()) {
                return Err(TryRecvError.Closed());
            }

            // JVM hint for busy-wait
            Thread.onSpinWait();
        }
    }

    public void close() {
        closed.set(true);
    }

    public boolean isClosed() {
        return closed.get();
    }
}
