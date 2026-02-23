package com.tsoft.jai.serdejson;

import com.tsoft.jai.anyhow.Result;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

public final class SerdeJson {

    private static final ObjectMapper jsonMapper = new JsonMapper();

    public static <T> Result<T> fromStr(String content, TypeReference<T> ref) {
        try {
            return Ok(jsonMapper.readValue(content, ref));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromValue(Value value, Class<T> clazz) {
        try {
            String json = Value.json(value);
            return Ok(jsonMapper.readValue(json, clazz));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private SerdeJson() { }
}
