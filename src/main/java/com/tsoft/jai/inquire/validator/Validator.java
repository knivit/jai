package com.tsoft.jai.inquire.validator;

import java.util.function.Function;

import static com.tsoft.jai.utils.base.StringUtils.isBlank;

public class Validator {

    public static Function<String, Boolean> required(String message) {
        return val -> !isBlank(val);
    }
}
