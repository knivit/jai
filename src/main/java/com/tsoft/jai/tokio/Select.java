package com.tsoft.jai.tokio;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class Select {

    public static final class Branch<T> {
        final CompletableFuture<? extends T> future;
        final Function<? super T, ? extends T> handler;

        private Branch(CompletableFuture<? extends T> f, Function<? super T, ? extends T> h) {
            this.future = f;
            this.handler = h;
        }
    }

    public static <T> Branch<T> branch(CompletableFuture<? extends T> f, Function<? super T, ? extends T> h) {
        return new Branch<>(f, h);
    }

    /**
     * Analogue of
     *     tokio::select! { v = future1 => body1, v = future2 => body2 }
     *
     * @param branches  pairs (future, handler). Handlers must be non-blocking.
     * @return whatever the *winning* handler returned.
     */
    @SafeVarargs
    public static <T> T select(Branch<T>... branches) {
        if (branches == null) {
            throw new IllegalArgumentException("select() needs at least one branch");
        }

        AtomicBoolean done = new AtomicBoolean(false);
        CompletableFuture<T> result = new CompletableFuture<>();

        for (Branch<T> c : branches) {
            c.future.whenComplete((value, throwable) -> {
                if (done.compareAndSet(false, true)) {          // first one wins
                    // cancel the remaining futures
                    for (Branch<T> cc : branches) {
                        cc.future.cancel(true);
                    }

                    if (throwable != null)
                        result.completeExceptionally(throwable);
                    else {
                        try {
                            result.complete(c.handler.apply(value));
                        } catch (Exception ex) {
                            result.completeExceptionally(ex);
                        }
                    }
                }
            });
        }

        try {
            return result.join();          // virtual thread parks, no OS thread blocked
        } catch (CompletionException ce) {
            throw new RuntimeException(ce.getCause());
        }
    }

    private Select() { }
}
