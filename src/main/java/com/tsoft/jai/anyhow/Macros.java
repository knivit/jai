package com.tsoft.jai.anyhow;

import lombok.extern.slf4j.Slf4j;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.utils.base.StringUtils.format;

@Slf4j
public final class Macros {

    public static void anyhow(String msg) {
        bail(msg);
    }

    public static <T> Result<T> bail(String msg, Object ... args) {
        return Err(format(msg, args));
    }
}
