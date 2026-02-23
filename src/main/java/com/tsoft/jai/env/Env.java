package com.tsoft.jai.env;

import com.tsoft.jai.anyhow.Result;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public final class Env {

    public static Result<String> var(String name) {
        try {
            String value = System.getProperty(name);
            if (value == null) {
                return Err();
            } else {
                return Ok(value);
            }
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private Env() { }
}
