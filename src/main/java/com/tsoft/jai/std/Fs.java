package com.tsoft.jai.std;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public final class Fs {

    public static List<File> readDir(Path path) {
        if (Files.exists(path)) {
            try (Stream<Path> files = Files.list(path)) {
                return files.map(Path::toFile).toList();
            } catch (Exception ex) {
                log.warn("Error reading dir {}: {}", path, ex.getMessage());
            }
        }

        return Collections.emptyList();
    }

    private Fs() { }
}
