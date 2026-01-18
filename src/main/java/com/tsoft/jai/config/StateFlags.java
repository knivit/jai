package com.tsoft.jai.config;

public class StateFlags {

    public static int ROLE = 1 << 0;
    public static int SESSION_EMPTY = 1 << 1;
    public static int SESSION = 1 << 2;
    public static int RAG = 1 << 3;
    public static int AGENT = 1 << 4;

    public int empty() {
        return 0;
    }
}
