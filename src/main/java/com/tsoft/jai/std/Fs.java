package com.tsoft.jai.std;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.core.macros.BuiltIn;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

@Slf4j
public final class Fs {

    public static Result<String> readToString(Path path) {
        try {
            String content = Files.readString(Paths.get(BuiltIn.class.getResource(path.toString()).toURI()));
            return Ok(content);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<List<File>> readDir(Path path) {
        if (Files.exists(path)) {
            try (Stream<Path> files = Files.list(path)) {
                List<File> list = files.map(Path::toFile).toList();
                return Ok(list);
            } catch (Exception ex) {
                log.warn("Error reading dir {}: {}", path, ex.getMessage());
                return Err(ex);
            }
        }

        return Ok(Collections.emptyList());
    }

    public static Result<Void> write(Path path, String content) {
        try {
            Files.writeString(path, content);
            return Ok();
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private Fs() { }
}
