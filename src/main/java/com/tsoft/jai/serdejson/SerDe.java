package com.tsoft.jai.serdejson;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import static com.tsoft.jai.utils.StringUtils.*;

@Slf4j
public class SerDe {

    private static final JsonMapper jsonMapper = new JsonMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static Value parseJson(String text) {
        return parse(text, jsonMapper);
    }

    public static Value parseYaml(String text) {
        return parse(text, yamlMapper);
    }

    private static Value parse(String text, ObjectMapper mapper) {
        if (isBlank(text)) {
            return new Value();
        }

        try {
            char ch = getFirstChar(text);

            if (ch == '{') {
                LinkedHashMap<String, Object> map = mapper.readValue(text, new TypeReference<>() {});
                return new Value(map);
            }

            if (ch == '[') {
                List<LinkedHashMap<String, Object>> map = mapper.readValue(text, new TypeReference<>() {});
                return new Value(map);
            }
        } catch (JacksonException ex) {
            log.warn("Error parsing text: {}", text, ex);
        }

        return null;
    }

    public static Value toJson(Object value) {
        if (value == null) {
            return new Value();
        }
        String json = toJsonString(value);
        return parseJson(json);
    }

    public static String toJsonString(Object value) {
        return jsonMapper.writeValueAsString(value);
    }

    public static String toYamlString(Value value) {
        return yamlMapper.writeValueAsString(value.getData());
    }

    public static <T> T readFromYamlFile(File file, Class<T> clazz) {
        return yamlMapper.readValue(file, clazz);
    }

    public static <T> T readFromYamlFile(File file, TypeReference<T> ref) {
        return yamlMapper.readValue(file, ref);
    }
}
