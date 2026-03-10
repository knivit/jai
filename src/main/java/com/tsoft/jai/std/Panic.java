package com.tsoft.jai.std;

import lombok.extern.slf4j.Slf4j;

import static com.tsoft.jai.utils.StringUtils.format;

@Slf4j
public class Panic {

    public static void panic() {
        panic("Fatal error occurred");
    }

    public static void panic(String message) {
        panic(message, Thread.currentThread().getStackTrace());
    }

    public static void panic(Exception ex) {
        panic(ex.getMessage(), ex.getStackTrace());
    }

    public static void panic(String message, StackTraceElement[] trace) {
        System.err.println(format("panic: {}", message));

        for (StackTraceElement traceElement : trace)
            System.err.println("  at " + traceElement);

        System.exit(-1);
    }
}
