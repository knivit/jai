package com.tsoft.jai.utils;

import com.tsoft.jai.std.Fs;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class Asset {

    private Asset() { }

    public static List<File> files(String assetName) {
        try {
            return Fs.readDir(Paths.get(Asset.class.getResource("/" + assetName).toURI()));
        } catch (Exception ex) {
            log.warn("Error listing asset files '{}'", assetName, ex);
            return Collections.emptyList();
        }
    }

    public static File file(String assetName) {
        try {
            return Paths.get(Asset.class.getResource("/" + assetName).toURI()).toFile();
        } catch (Exception ex) {
            log.warn("Error getting asset file '{}'", assetName, ex);
            return null;
        }
    }
}
