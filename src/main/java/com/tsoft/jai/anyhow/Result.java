package com.tsoft.jai.anyhow;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {

    public enum ResultType {
        Ok,
        Err
    }

    private ResultType type;
    private T value;

    public static ResultType getType(Result<?> result) {
        return (result == null) ? ResultType.Err : result.type;
    }

    public static <T> Result<T> Ok() {
        return new Result<T>().setType(ResultType.Ok);
    }

    public static <T> Result<T> Ok(T value) {
        return new Result<T>().setType(ResultType.Ok).setValue(value);
    }

    public static boolean isOk(Result<?> result) {
        return (result != null) && (ResultType.Ok.equals(result.type));
    }

    public static <T> Result<T> Err() {
        return new Result<T>().setType(ResultType.Err);
    }

    public static <T> Result<T> Err(T value) {
        return new Result<T>().setType(ResultType.Err).setValue(value);
    }

    public static boolean isErr(Result<?> result) {
        return (result != null) && (ResultType.Err.equals(result.type));
    }
}
