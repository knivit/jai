package com.tsoft.jai.anyhow;

import lombok.Getter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Getter
public class Error<T> {

    private final List<String> errors = new ArrayList<>();
    private final Exception backtrace;
    private final T errValue;

    public Error(String error) {
        this(error, null, null);
    }

    public Error(Exception exception) {
        this(null, exception, null);
    }

    public Error(T errValue) {
        this(null, null, errValue);
    }

    public Error(String error, Exception exception, T errValue) {
        if (exception != null) {
            errors.add(exception.getMessage());
        }
        if (!isBlank(error)) {
            errors.add(error);
        }
        backtrace = exception;
        this.errValue = errValue;
    }

    public Error<?> add(String error) {
        if (!isBlank(error)) {
            errors.add(error);
        }
        return this;
    }

    @Override
    public String toString() {
        if (isEmpty(errors)) {
            return "Not specified.";
        }

        StringBuilder buf = new StringBuilder();
        for (String error : errors) {
            buf.append(error).append('\n');
        }

        if (backtrace != null) {
            StringWriter sw = new StringWriter();
            backtrace.printStackTrace(new PrintWriter(sw));
            buf.append('\n').append(sw);
        }

        return buf.toString();
    }
}
