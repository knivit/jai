package com.tsoft.jai.anyhow;

import lombok.extern.slf4j.Slf4j;

import static com.tsoft.jai.utils.base.StringUtils.format;

@Slf4j
public final class Macros {

    public static void anyhow(String msg) {
        bail(msg);
    }

    public static void bail(String msg, Object ... args) {
        msg = format(msg, args);
        log.error((msg == null) ? "" : msg);
        System.exit(1);
    }
}
