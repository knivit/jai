package com.tsoft.jai.tokio;

import com.tsoft.jai.tokio.sync.oneshot.Oneshot;
import com.tsoft.jai.tokio.sync.oneshot.Receiver;
import com.tsoft.jai.tokio.sync.oneshot.Sender;
import com.tsoft.jai.utils.base.Tuple;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Tokio {

    public static <T> CompletableFuture<T> spawn(Supplier<T> taskSupplier) {
        return Spawn.spawn(taskSupplier);
    }

    @SafeVarargs
    public static <T> T select(Select.Branch<T>... branches) {
        return Select.select(branches);
    }

    public static <T> Tuple<Sender<T>, Receiver<T>> channel() {
        return Oneshot.channel();
    }

    private Tokio() { }
}
