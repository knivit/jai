package com.tsoft.jai.core.macros;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.tsoft.jai.core.Panic.panic;

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

    public static String includeStr(String fileName) {
        try {
            return Files.readString(Paths.get(BuiltIn.class.getResource(fileName).toURI()));
        } catch (Exception ex) {
            panic(ex);
            return null;
        }
    }

    private BuiltIn() { }
}
