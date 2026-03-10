package com.tsoft.jai.std;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.tsoft.jai.std.Panic.panic;
import static com.tsoft.jai.utils.StringUtils.format;

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
        Result<T> result = Ok();
        result.value = value;
        return result;
    }

    public static <T> Result<T> Err() {
        return new Result<>(ResultEnum.Err);
    }

    public static <T, E> Result<T> Err(E errValue) {
        Result<T> result = Err();
        result.err = new Error<>(errValue);
        return result;
    }

    public static <T> Result<T> Err(Exception exception) {
        Result<T> result = Err();
        result.err = new Error<>(exception);
        return result;
    }

    public static <T> Result<T> Err(String error) {
        Result<T> result = Err();
        result.err = new Error<>(error);
        return result;
    }

    public static <T> Result<T> Err(String error, Object ... args) {
        Result<T> result = Err();
        result.err = new Error<>(format(error, args));
        return result;
    }

    public static <T> Result<T> Err(Result<?> err) {
        if (err != null && ResultEnum.Err.equals(err.type)) {
            Result<T> result = Err();
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

    public <R> Result<R> then(Function<T, Result<R>> fun) {
        return switch (type) {
            case Ok -> {
                try {
                    yield fun.apply(value);
                } catch (Exception ex) {
                    yield Err(ex);
                }
            }
            case Err -> (Result<R>)this;
        };
    }

    public Result<?> thenOrElse(Consumer<T> then, Consumer<Error<?>> orElse) {
        return switch (type) {
            case Ok -> {
                try {
                    then.accept(value);
                    yield Ok();
                } catch (Exception ex) {
                    yield Err(ex);
                }
            }
            case Err -> {
                try {
                    orElse.accept(err);
                    yield Ok();
                } catch (Exception ex) {
                    yield Err(ex);
                }
            }
        };
    }

    public T unwrap() {
        return switch (type) {
            case Ok -> value;
            case Err -> {
                panic();
                throw new IllegalStateException();
            }
        };
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
