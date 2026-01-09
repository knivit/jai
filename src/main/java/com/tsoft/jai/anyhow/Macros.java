package com.tsoft.jai.anyhow;

import static com.tsoft.jai.utils.StringUtils.format;

public final class Macros {

    public static void bail(String msg, Object ... args) {
        msg = format(msg, args);
        System.err.println((msg == null) ? "" : msg);
        System.exit(1);
    }
}
