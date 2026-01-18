package com.tsoft.jai.core.macros;

public final class BuiltIn {

    public enum Platform {
        WINDOWS,
        LINUX,
        MAC,
        OTHER
    }
    public static boolean cfg(Platform platform) {
        return getPlatform().equals(platform);
    }

    public static Platform getPlatform() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return Platform.WINDOWS;
        } else if (os.contains("mac") || os.contains("darwin")) {
            return Platform.MAC;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return Platform.LINUX;
        } else {
            return Platform.OTHER;
        }
    }

    private BuiltIn() { }
}
