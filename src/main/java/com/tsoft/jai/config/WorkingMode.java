package com.tsoft.jai.config;

public enum WorkingMode {

    Cmd,
    Repl,
    Serve;

    public static boolean isCmd(WorkingMode mode) {
        return Cmd.equals(mode);
    }
}
