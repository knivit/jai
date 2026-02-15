package com.tsoft.jai.tokio;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Spawn {

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
    public static <T> CompletableFuture<T> spawn(Supplier<T> taskSupplier) {
        return supplyAsync(
            () -> {
                try {
                    return taskSupplier.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
