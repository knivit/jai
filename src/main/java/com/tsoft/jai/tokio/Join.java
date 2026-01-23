package com.tsoft.jai.tokio;

import com.tsoft.jai.utils.TupleN;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

public final class Join {

    /**
     * Analogue of
     *     let (a, b, c) = tokio::join!(fut1, fut2, fut3);
     *
     * Returns a record-like holder whose fields match the futures in order.
     * If *any* future fails the whole call throws (unchecked) immediately
     * after the first failure, but all futures are cancelled so no work leaks.
     */
    @SafeVarargs
    public static <T> TupleN join(CompletableFuture<? extends T>... futures) {
        if (futures == null)
            throw new IllegalArgumentException("join() needs at least one future");

        // Combine all into one future that finishes when the last one does
        CompletableFuture<Void> all = CompletableFuture.allOf(futures);

        try {
            all.join();          // virtual thread parks, no OS thread blocked
        } catch (CompletionException ce) {
            // cancel everything and re-throw the *first* failed exception
            for (CompletableFuture<? extends T> f : futures) {
                f.cancel(true);
            }
            throw new RuntimeException(ce.getCause());
        }

        // Materialise the results
        List<? extends T> results = Stream.of(futures)
            .map(CompletableFuture::join) // will *not* block
            .toList();

        return TupleN.asList(results);
    }
}
