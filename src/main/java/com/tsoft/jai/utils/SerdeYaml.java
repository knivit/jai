package com.tsoft.jai.utils;

import com.tsoft.jai.std.Result;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.nio.file.Path;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;

public final class SerdeYaml {

    private static final ObjectMapper yamlMapper = new YAMLMapper();

    public static Result<String> toString(Object obj) {
        try {
            return Ok(yamlMapper.writeValueAsString(obj));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> toFile(Path file, Object obj) {
        try {
            yamlMapper.writeValue(file, obj);
            return Ok();
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromFile(Path file, Class<T> clazz) {
        try {
            return Ok(yamlMapper.readValue(file, clazz));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromStr(String content, Class<T> clazz) {
        try {
            return Ok(yamlMapper.readValue(content, clazz));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromStr(String content, TypeReference<T> ref) {
        try {
            return Ok(yamlMapper.readValue(content, ref));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private SerdeYaml() { }
}
