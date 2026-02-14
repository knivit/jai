package com.tsoft.jai.tokio;

import com.tsoft.jai.tokio.sync.oneshot.Oneshot;
import com.tsoft.jai.utils.base.Tuple;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class Tokio {

    public static <T> CompletableFuture<T> spawn(Supplier<CompletableFuture<T>> taskSupplier) {
        return Spawn.spawn(taskSupplier);
    }

    public static <T> T select(Select.Branch<T>... branches) {
        return Select.select(branches);
    }

    public static <T> Tuple<CompletableFuture<T>, Supplier<T>> channel() {
        return Oneshot.channel();
    }

    private Tokio() { }
}
