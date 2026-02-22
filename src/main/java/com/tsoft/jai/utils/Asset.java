package com.tsoft.jai.utils;

import com.tsoft.jai.anyhow.Result;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.std.Fs.readDir;
import static com.tsoft.jai.utils.base.StringUtils.format;

public final class Asset {

    private Asset() { }

    public static List<File> files(String assetName) {
        try {
            Result<List<File>> res = readDir(Paths.get(Asset.class.getResource("/" + assetName).toURI()));
            return switch (res.getType()) {
                case Ok -> res.getValue();
                case Err -> throw new IllegalStateException(res.getErr().toString());
            };
        } catch (Exception ex) {
            throw new IllegalArgumentException(format("Error listing asset files '{}': {}", assetName, ex.getMessage()));
        }
    }

    public static Result<File> file(String assetName) {
        try {
            File file = Paths.get(Asset.class.getResource("/" + assetName).toURI()).toFile();
            return Ok(file);
        } catch (Exception ex) {
            return Err(format("Error getting asset file '{}': {}", assetName, ex.getMessage()));
        }
    }
}
