package com.tsoft.jai.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Panic {

    public static <T> T panic() {
        log.error("panic");
        System.exit(-1);
        return null;
    }

    public static <T> T panic(String message) {
        log.error("panic: {}", message);
        System.exit(-1);
        return null;
    }

    public static <T> T panic(Exception ex) {
        log.error("panic", ex);
        System.exit(-1);
        return null;
    }
}
