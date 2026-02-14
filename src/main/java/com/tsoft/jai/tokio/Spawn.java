package com.tsoft.jai.tokio;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Spawn {

    private static final Executor SPAWN_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    // Spawn function (analogous to tokio::spawn)
    //
    // #[tokio::main]
    // async fn main() {
    //    let handle = tokio::spawn(async {
    //        // Simulate async I/O
    //        tokio::time::sleep(std::time::Duration::from_secs(1)).await;
    //        42
    //    });
    //
    //    let result = handle.await.unwrap();
    //    println!("Result: {}", result);
    // }
    public static <T> CompletableFuture<T> spawn(Supplier<CompletableFuture<T>> taskSupplier) {
        return CompletableFuture.supplyAsync(
            () -> {
                try {
                    return taskSupplier.get().join();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            SPAWN_EXECUTOR
        );
    }
}
