package com.tsoft.jai.core;

public class Panic {

    public static void panic() {
        System.exit(-1);

        throw new IllegalStateException("panic");
    }
}
