package com.tsoft.jai.serdeyaml;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serdejson.Value;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public final class SerDeYaml {

    private static final ObjectMapper yamlMapper = new YAMLMapper();

    public static Result<String> toString(Value value) {
        try {
            return Ok(yamlMapper.writeValueAsString(value.getData()));
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

    private SerDeYaml() { }
}
