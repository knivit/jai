package com.tsoft.jai.anyhow;

import com.tsoft.jai.core.Option;
import lombok.*;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.tsoft.jai.core.Option.None;
import static com.tsoft.jai.core.Option.Some;
import static com.tsoft.jai.core.Panic.panic;
import static com.tsoft.jai.utils.base.StringUtils.format;

@RequiredArgsConstructor
public class Result<T> {

    public enum ResultEnum {
        Ok,
        Err
    }

    @Getter
    private final ResultEnum type;
    private T value;
    private Error<?> err;

    public static ResultEnum getType(Result<?> result) {
        return (result == null) ? ResultEnum.Err : result.type;
    }

    public static <T> Result<T> Ok() {
        return new Result<>(ResultEnum.Ok);
    }

    public static <T> Result<T> Ok(T value) {
        Result<T> result = new Result<>(ResultEnum.Ok);
        result.value = value;
        return result;
    }

    public static <T> Result<T> Err() {
        return new Result<>(ResultEnum.Err);
    }

    public static <T, E> Result<T> Err(E errValue) {
        Result<T> result = new Result<>(ResultEnum.Err);
        result.err = new Error<>(errValue);
        return result;
    }

    public static <T> Result<T> Err(Exception exception) {
        Result<T> result = new Result<>(ResultEnum.Err);
        result.err = new Error<>(exception);
        return result;
    }

    public static <T> Result<T> Err(String error) {
        Result<T> result = new Result<>(ResultEnum.Err);
        result.err = new Error<>(error);
        return result;
    }

    public static <T> Result<T> Err(String error, Object ... args) {
        Result<T> result = new Result<>(ResultEnum.Err);
        result.err = new Error<>(format(error, args));
        return result;
    }

    public static <T> Result<T> Err(Result<?> err) {
        if (err != null && ResultEnum.Err.equals(err.type)) {
            Result<T> result = new Result<>(ResultEnum.Err);
            result.err = err.err;
            return result;
        }
        return Err();
    }

    public static boolean isOk(Result<?> result) {
        return (result != null) && (ResultEnum.Ok.equals(result.type));
    }

    public static boolean isErr(Result<?> result) {
        return (result != null) && (ResultEnum.Err.equals(result.type));
    }

    public T unwrap() {
        return getValue();
    }

    public T unwrapOrElse(Function<String, T> fun) {
        return switch (type) {
            case Ok -> value;
            case Err -> fun.apply(err.toString());
        };
    }

    public T unwrapOrDefault(T def) {
        return switch (type) {
            case Ok -> value;
            case Err -> def;
        };
    }

    public T getValue() {
        return switch (type) {
            case Ok -> value;
            case Err -> panic();
        };
    }

    public Option<T> ok() {
        return switch (type) {
            case Ok -> Some(value);
            case Err -> None();
        };
    }

    public Error<?> getErr() {
        if (isErr(this)) {
            return err;
        }
        throw new IllegalStateException();
    }

    // Wrap the error value with additional context
    public Result<T> context(String context) {
        if (ResultEnum.Err.equals(type)) {
            if (err == null) {
                err = new Error<>(context);
            } else {
                err.add(context);
            }
        }
        return this;
    }

    // Wrap the error value with additional context that is evaluated lazily only once an error does occur
    public Result<T> withContext(Supplier<String> supplier) {
        if (ResultEnum.Err.equals(type)) {
            String context = supplier.get();
            return context(context);
        }
        return this;
    }

    @Override
    public String toString() {
        return switch (type) {
            case Ok -> format("{} (value={})", ResultEnum.Ok, value);
            case Err -> format("{} (err={})", ResultEnum.Err, err);
        };
    }
}
