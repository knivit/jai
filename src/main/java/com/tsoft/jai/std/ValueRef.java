package com.tsoft.jai.std;

import java.util.concurrent.atomic.AtomicReference;

import static com.tsoft.jai.std.Panic.panic;
import static com.tsoft.jai.std.Result.Ok;

public class ValueRef<T> {

    private final AtomicReference<T> ref = new AtomicReference<>();

    public ValueRef(T val) {
        if (val == null) {
            panic("val must be not null");
        }
        set(val);
    }

    public T get() {
        return ref.get();
    }

    public Result<ValueRef<T>> set(T value) {
        ref.set(value);
        return Ok(this);
    }
}
