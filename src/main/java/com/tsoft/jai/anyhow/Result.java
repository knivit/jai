package com.tsoft.jai.anyhow;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.tsoft.jai.utils.base.StringUtils.format;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class Result<T> {

    public enum ResultEnum {
        Ok,
        Err
    }

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
        return new Result<T>(ResultEnum.Ok).setValue(value);
    }

    public static <T> Result<T> Err() {
        return new Result<>(ResultEnum.Err);
    }

    public static <T, E> Result<T> Err(E errValue) {
        return new Result<T>(ResultEnum.Err).setErr(new Error<E>(errValue));
    }

    public static <T> Result<T> Err(Exception exception) {
        return new Result<T>(ResultEnum.Err).setErr(new Error<>(exception));
    }

    public static <T> Result<T> Err(String error) {
        return new Result<T>(ResultEnum.Err).setErr(new Error<>(error));
    }

    public static <T> Result<T> Err(String error, Object ... args) {
        return new Result<T>(ResultEnum.Err).setErr(new Error<>(format(error, args)));
    }

    public static <T> Result<T> Err(Result<?> err) {
        if (err != null && ResultEnum.Err.equals(err.type)) {
            return new Result<T>(ResultEnum.Err).setErr(err.err);
        }
        return Err();
    }

    public static boolean isOk(Result<?> result) {
        return (result != null) && (ResultEnum.Ok.equals(result.type));
    }

    public static boolean isErr(Result<?> result) {
        return (result != null) && (ResultEnum.Err.equals(result.type));
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
