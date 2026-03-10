package com.tsoft.jai.std;

import java.util.concurrent.atomic.AtomicReference;

import static com.tsoft.jai.std.Result.Ok;

public class ValueRef<T> {

    private final AtomicReference<T> ref = new AtomicReference<>();

    public T get() {
        return ref.get();
    }

    public Result<ValueRef<T>> set(T value) {
        ref.set(value);
        return Ok(this);
    }
}
