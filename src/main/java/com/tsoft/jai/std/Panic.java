package com.tsoft.jai.std;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Panic {

    public static void panic() {
        log.error("panic");
        System.exit(-1);
    }

    public static void panic(String message) {
        log.error("panic: {}", message);
        System.exit(-1);
    }

    public static void panic(Exception ex) {
        log.error("panic", ex);
        System.exit(-1);
    }
}
