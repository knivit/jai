package com.tsoft.jai.serde.serdejson;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serde.Value;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static com.tsoft.jai.anyhow.Result.*;

public final class SerdeJson {

    private static final ObjectMapper jsonMapper = new JsonMapper();

    public static Result<String> toString(Object obj) {
        try {
            return Ok(jsonMapper.writeValueAsString(obj));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<String> toString(Value value) {
        try {
            return Ok(jsonMapper.writeValueAsString(value.getData()));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<Value> json(Object obj) {
        return Value.json(obj, jsonMapper);
    }

    public static <T> Result<T> fromStr(String content, TypeReference<T> ref) {
        try {
            return Ok(jsonMapper.readValue(content, ref));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<Value> fromStr(String content) {
        return Value.fromStr(content, jsonMapper);
    }

    public static <T> Result<T> fromValue(Value value, Class<T> clazz) {
        try {
            Result<String> res = Value.json(value);
            if (isErr(res)) {
                return Err(res);
            }
            String json = res.getValue();
            return Ok(jsonMapper.readValue(json, clazz));
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    private SerdeJson() { }
}
