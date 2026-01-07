package com.tsoft.jai.utils;

import com.tsoft.jai.std.Fs;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public final class Asset {

    private Asset() { }

    public static List<File> get(String assetName) {
        try {
            return Fs.readDir(Paths.get(Asset.class.getResource("/" + assetName).toURI()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
