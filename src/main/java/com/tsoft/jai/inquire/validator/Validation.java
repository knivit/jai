package com.tsoft.jai.inquire.validator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Validation {

    public enum ValidationEnum {
        Invalid,
        Valid
    }

    private final ValidationEnum type;
    private String message;

    public static Validation Invalid(String message) {
        Validation validation = new Validation(ValidationEnum.Invalid);
        validation.message = message;
        return validation;
    }

    public static Validation Valid() {
        return new Validation(ValidationEnum.Valid);
    }
}
