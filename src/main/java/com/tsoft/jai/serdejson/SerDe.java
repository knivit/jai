package com.tsoft.jai.serdejson;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class SerDe {

    public static final JsonMapper jsonMapper = new JsonMapper();

    public static Value parse(String value) {
        return parse(value, (data, ex) -> {
            log.error("Fail to parse as JSON: {}", value, ex);
        });
    }

    public static Value parse(String value, BiConsumer<String, Exception> errHandler) {
        if (value == null) {
            return null;
        }

        try {
            char ch = '\0';
            for (int i = 0; i < value.length(); i ++) {
                ch = value.charAt(i);
                if (ch > ' ') {
                    break;
                }
            }

            if (ch == '{') {
                LinkedHashMap<String, Object> map = jsonMapper.readValue(value, new TypeReference<>() {});
                return new Value(map);
            }

            if (ch == '[') {
                List<LinkedHashMap<String, Object>> map = jsonMapper.readValue(value, new TypeReference<>() {});
                return new Value(map);
            }

            errHandler.accept(value, new IllegalStateException("Not a JSON"));
        } catch (JacksonException ex) {
            errHandler.accept(value, ex);
        }

        return null;
    }
}
