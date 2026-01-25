package com.tsoft.jai.anyhow;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.tsoft.jai.utils.base.CollectionsUtils.isEmpty;
import static com.tsoft.jai.utils.base.StringUtils.isBlank;

@Getter
public class Error {

    private final List<String> errors = new ArrayList<>();
    private final Exception backtrace;

    public Error(String error) {
        this(error, null);
    }

    public Error(Exception exception) {
        this(exception.getMessage(), exception);
    }

    public Error(String error, Exception exception) {
        if (exception != null) {
            errors.add(exception.getMessage());
        }
        if (!isBlank(error)) {
            errors.add(error);
        }
        backtrace = exception;
    }

    public Error add(String error) {
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
        return buf.toString();
    }
}
