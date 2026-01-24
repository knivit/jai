package com.tsoft.jai.anyhow;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Data
@Accessors(chain = true)
public class Result<T> {

    public enum ResultType {
        Ok,
        Err
    }

    private ResultType type;
    private T value;
    private List<String> errors;

    public static ResultType getType(Result<?> result) {
        return (result == null) ? ResultType.Err : result.type;
    }

    public static <T> Result<T> Ok() {
        return new Result<T>().setType(ResultType.Ok);
    }

    public static <T> Result<T> Ok(T value) {
        return new Result<T>().setType(ResultType.Ok).setValue(value);
    }

    public static <T> Result<T> Err() {
        return new Result<T>().setType(ResultType.Err);
    }

    public static <T> Result<T> Err(Exception exception) {
        return Err((exception == null) ? null : exception.getMessage());
    }

    public static <T> Result<T> Err(String error) {
        Result<T> result = new Result<T>().setType(ResultType.Err);
        if (!isBlank(error)) {
            List<String> errors = new ArrayList<>();
            errors.add(error);
            result.setErrors(errors);
        }
        return result;
    }

    public static boolean isOk(Result<?> result) {
        return (result != null) && (ResultType.Ok.equals(result.type));
    }

    public static boolean isErr(Result<?> result) {
        return (result != null) && (ResultType.Err.equals(result.type));
    }

    // Wrap the error value with additional context that is evaluated lazily only once an error does occur
    public Result<T> withContext(Supplier<String> supplier) {
        if (ResultType.Err.equals(type)) {
            String error = supplier.get();
            if (!isBlank(error)) {
                if (isEmpty(errors)) {
                    errors = new ArrayList<>();
                }
                errors.addFirst(error);
            }
        }
        return this;
    }
}
