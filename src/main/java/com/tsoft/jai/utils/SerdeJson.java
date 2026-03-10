package com.tsoft.jai.utils;

import com.tsoft.jai.std.Result;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;

public final class SerdeJson {

    private static final ObjectMapper MAPPER = new JsonMapper();

    public static Result<String> toString(Object obj) {
        try {
            return Ok(MAPPER.writeValueAsString(obj));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromStr(String content, Class<T> clazz) {
        try {
            return Ok(MAPPER.readValue(content, clazz));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static <T> Result<T> fromStr(String content, TypeReference<T> ref) {
        try {
            return Ok(MAPPER.readValue(content, ref));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private SerdeJson() { }
}
