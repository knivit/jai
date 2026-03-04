package com.tsoft.jai.serde.serdeyaml;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serde.Value;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.time.Instant;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public final class SerdeYaml {

    private static final ObjectMapper yamlMapper = new YAMLMapper();

    public static Result<String> toString(Object value) {
        try {
            return Ok(yamlMapper.writeValueAsString(value));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<String> toString(Value value) {
        try {
            return Value.yaml(value);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<String> asStr(Object value) {
        if (value == null) {
            return Ok("null");
        }

        if (value instanceof String str) {
            return Ok(str);
        }

        if (value instanceof Number num) {
            return Ok(num.toString());
        }

        if (value instanceof Instant ins) {
            return Ok(ins.toString());
        }

        return Ok(value.toString());
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

    public static Result<Value> fromStr(String content) {
        return Value.fromStr(content, yamlMapper);
    }

    private SerdeYaml() { }
}
