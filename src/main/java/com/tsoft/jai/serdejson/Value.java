package com.tsoft.jai.serdejson;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Data
@Accessors(chain = true)
public class Value {

    private final Object data;

    public Value() {
        data = new LinkedHashMap<>();
    }

    public Value(Object data) {
        this.data = data;
    }

    public Value get(Object ... path) {
        Object ptr = data;

        for (Object elem : path) {
            if (elem instanceof String name) {
                ptr = ((Map<String, ?>)ptr).get(name);
                continue;
            }

            if (elem instanceof Integer index) {
                ptr = ((List<?>)ptr).get(index);
                continue;
            }

            log.error("Failed to fetch an element using path: {}\nfrom data: {}", path, data);
            throw new IllegalStateException("Execution terminated");
        }

        return new Value(ptr);
    }

    public <T> Value put(String key, T value) {
        ((Map<String, T>)data).put(key, value);
        return this;
    }

    public String asStr() {
        return (data == null) ? null : data.toString();
    }

    public Integer asInt() {
        return (data == null) ? null : Integer.valueOf(data.toString());
    }

    public static boolean isList(Value value) {
        return (value != null) && (value.data instanceof List);
    }

    public List<Value> asList() {
        if (data == null) {
            return Collections.emptyList();
        }

        if (data instanceof List<?> list) {
            List<Value> result = new ArrayList<>();
            for (Object val : list) {
                result.add(new Value(val));
            }
            return result;
        }

        throw new IllegalStateException("Not a list: " + data);
    }

    public static boolean isMap(Value value) {
        return (value != null) && (value.data instanceof Map);
    }

    public Map<String, String> asMap() {
        if (data == null) {
            return Collections.emptyMap();
        }

        if (data instanceof Map map) {
            return map;
        }

        throw new IllegalStateException("Not a map: " + data);
    }

    public static Value jsonPatch(Value value, Value patch) {
        if (value == null || value.data == null) {
            return patch;
        }
        if (patch == null || patch.data == null) {
            return value;
        }
        if (isMap(value)) {
            if (isMap(patch)) {
                Map valueMap = (Map)value.data;
                Map patchMap = (Map)patch.data;
                valueMap.putAll(patchMap);
                return value;
            }
            throw new IllegalStateException("The patch is not a map: " + patch);
        }
        if (isList(value)) {
            if (isList(patch)) {
                List valueList = (List)value.data;
                List patchList = (List)patch.data;
                valueList.addAll(patchList);
                return value;
            }
            throw new IllegalStateException("The patch is not a list: " + patch);
        }
        throw new IllegalStateException("Unsupported operation");
    }
}
