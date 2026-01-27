package com.tsoft.jai.anyhow;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.tsoft.jai.utils.base.StringUtils.format;

@Data
@Accessors(chain = true)
public class Result<T> {

    public enum ResultType {
        Ok,
        Err
    }

    private final ResultType type;
    private T value;
    private Error err;

    public static ResultType getType(Result<?> result) {
        return (result == null) ? ResultType.Err : result.type;
    }

    public static <T> Result<T> Ok() {
        return new Result<T>(ResultType.Ok);
    }

    public static <T> Result<T> Ok(T value) {
        return new Result<T>(ResultType.Ok).setValue(value);
    }

    public static <T> Result<T> Err() {
        return new Result<T>(ResultType.Err);
    }

    public static <T> Result<T> Err(Exception exception) {
        return Err((exception == null) ? null : exception.getMessage());
    }

    public static <T> Result<T> Err(String error) {
        return new Result<T>(ResultType.Err).setErr(new Error(error));
    }

    public static <T> Result<T> Err(String error, Object ... args) {
        return new Result<T>(ResultType.Err).setErr(new Error(format(error, args)));
    }

    public static <T> Result<T> Err(Result<?> err) {
        if (err != null && ResultType.Err.equals(err.type)) {
            return new Result<T>(ResultType.Err).setErr(err.err);
        }
        return Err();
    }

    public static boolean isOk(Result<?> result) {
        return (result != null) && (ResultType.Ok.equals(result.type));
    }

    public static boolean isErr(Result<?> result) {
        return (result != null) && (ResultType.Err.equals(result.type));
    }

    // Wrap the error value with additional context
    public Result<T> context(String context) {
        if (ResultType.Err.equals(type)) {
            if (err == null) {
                err = new Error(context);
            } else {
                err.add(context);
            }
        }
        return this;
    }

    // Wrap the error value with additional context that is evaluated lazily only once an error does occur
    public Result<T> withContext(Supplier<String> supplier) {
        if (ResultType.Err.equals(type)) {
            String context = supplier.get();
            return context(context);
        }
        return this;
    }
}
