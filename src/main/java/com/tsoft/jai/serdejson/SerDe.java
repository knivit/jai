package com.tsoft.jai.serdejson;

import com.tsoft.jai.anyhow.Result;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.utils.base.StringUtils.*;

@Slf4j
public class SerDe {

    private static final JsonMapper jsonMapper = new JsonMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static Result<Value> parseJson(String text) {
        return parse(text, jsonMapper);
    }

    public static Result<Value> parseYaml(String text) {
        return parse(text, yamlMapper);
    }

    private static Result<Value> parse(String text, ObjectMapper mapper) {
        if (isBlank(text)) {
            return Ok(new Value());
        }

        try {
            char ch = getFirstChar(text);

            if (ch == '{') {
                LinkedHashMap<String, Object> map = mapper.readValue(text, new TypeReference<>() {});
                return Ok(new Value(map));
            }

            if (ch == '[') {
                List<LinkedHashMap<String, Object>> map = mapper.readValue(text, new TypeReference<>() {});
                return Ok(new Value(map));
            }

            return Err("Not a JSON.");
        } catch (JacksonException ex) {
            return Err(ex);
        }
    }

    public static Value json(Object ... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        Value value = new Value();
        for (int i = 0; i < values.length; i += 2) {
            value.put((String)values[i], values[i + 1]);
        }

        return value;
    }

    public static Result<Value> toJson(Object value) {
        if (value == null) {
            return Ok(new Value());
        }
        String json = toJsonString(value);
        return parseJson(json);
    }

    public static String toJsonString(Object value) {
        return jsonMapper.writeValueAsString(value);
    }

    public static String toYamlString(Value value) {
        try {
            return yamlMapper.writeValueAsString(value.getData());
        } catch (Exception ex) {
            return null;
        }
    }

    public static String toYamlString(Object value) {
        try {
            return yamlMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T readFromYamlFile(File file, Class<T> clazz) {
        return yamlMapper.readValue(file, clazz);
    }

    public static <T> T readFromYamlFile(File file, TypeReference<T> ref) {
        return yamlMapper.readValue(file, ref);
    }
}
