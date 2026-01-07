package com.tsoft.jai.utils;

import com.tsoft.jai.std.Fs;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public final class Asset {

    private Asset() { }

    public static List<File> files(String assetName) {
        try {
            return Fs.readDir(Paths.get(Asset.class.getResource("/" + assetName).toURI()));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static File file(String assetName) {
        try {
            return Paths.get(Asset.class.getResource("/" + assetName).toURI()).toFile();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
