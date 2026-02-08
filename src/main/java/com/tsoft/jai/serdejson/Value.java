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

    public static String json(Value value) {
        if (value == null) {
            return null;
        }

        if (value.data == null) {
            return "{ }";
        }

        StringBuilder buf = new StringBuilder();

        if (isMap(value)) {
            Map<String, String> map = asMap(value);

            int i = 0;
            buf.append('{');
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append('"').append(entry.getKey()).append('"').append(':').append(SerDe.toJsonString(entry.getValue()));
                i ++;
            }
            buf.append('}');
        } else if (isList(value)) {
            List<Value> list = asList(value);

            int i = 0;
            buf.append('[');
            for (Value entry : list) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append('{').append(json(entry)).append('}');
                i ++;
            }
            buf.append(']');
        } else {
            throw new IllegalStateException("Unknown data type (must be a Map or a List): " + value.data.getClass().getName());
        }

        return buf.toString();
    }

    public Value get(Object ... path) {
        Object ptr = data;

        for (Object elem : path) {
            if (elem instanceof String name) {
                ptr = ((Map<String, ?>)ptr).get(name);
                if (ptr == null) {
                    return null;
                }
                continue;
            }

            if (elem instanceof Integer index) {
                ptr = ((List<?>)ptr).get(index);
                if (ptr == null) {
                    return null;
                }
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

    public String asStr(Object ... path) {
        Value val = get(path);
        return (val == null) ? null : val.toString();
    }

    public Integer asInt(Object ... path) {
        Value val = get(path);
        return (val == null) ? null : Integer.valueOf(val.toString());
    }

    public static boolean isList(Value value) {
        return (value != null) && (value.data instanceof List);
    }

    public static List<Value> asList(Value value) {
        if (!isList(value)) {
            throw new IllegalStateException("Not a list: " + value);
        }

        return (List<Value>)value.data;
    }

    public List<Value> asList(Object ... path) {
        Value val = get(path);
        if (val == null) {
            return Collections.emptyList();
        }

        if (isList(val)) {
            List<Value> result = new ArrayList<>();
            for (Object item : (List<?>)data) {
                result.add(new Value(item));
            }
            return result;
        }

        throw new IllegalStateException("Not a list: " + data);
    }

    public static boolean isMap(Value value) {
        return (value != null) && (value.data instanceof Map);
    }

    public static Map<String, String> asMap(Value value) {
        if (!isMap(value)) {
            throw new IllegalStateException("Not a map: " + value);
        }

        return (Map<String, String>)value.data;
    }

    public Map<String, String> asMap(Object ... path) {
        Value val = get(path);
        if (val == null) {
            return Collections.emptyMap();
        }

        if (isMap(val)) {
            return (Map<String, String>)val.data;
        }

        throw new IllegalStateException("Not a map: " + val);
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

    @Override
    public String toString() {
        return (data == null) ? null : data.toString();
    }
}
