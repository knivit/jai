package com.tsoft.jai.dirs;

import com.tsoft.jai.core.Option;

import java.nio.file.Path;

public final class Dirs {

    /// Returns the path to the user's config directory.
    /// The returned value depends on the operating system and is either a Some, containing a value from the following table, or a None.
    /// Platform            Value                               Example
    /// Linux               $XDG_CONFIG_HOME or $HOME/.config   /home/alice/.config
    /// macOS               $HOME/Library/Application Support   /Users/Alice/Library/Application Support
    /// Windows             {FOLDERID_RoamingAppData}           C:\Users\Alice\AppData\Roaming
    public static Option<Path> configDir() {
        return new Option<>(homeDir().expect().resolve(".config"));
    }

    /// Returns the path to the user's home directory.
    /// The returned value depends on the operating system and is either a Some, containing a value from the following table, or a None.
    /// Platform            Value                               Example
    /// Linux               $HOME                               /home/alice
    /// macOS               $HOME                               /Users/Alice
    /// Windows             {FOLDERID_Profile}                  C:\Users\Alice
    public static Option<Path> homeDir() {
        String path = System.getProperty("user.home");
        if (path == null) {
            return new Option<>();
        } else {
            return new Option<>(Path.of(path));
        }
    }

    private Dirs() { }
}
