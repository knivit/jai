package com.tsoft.jai.serde;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serde.serdejson.SerdeJson;
import com.tsoft.jai.serde.serdeyaml.SerdeYaml;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.*;

import static com.tsoft.jai.anyhow.Result.*;
import static com.tsoft.jai.utils.base.StringUtils.*;

@Slf4j
@Data
@Accessors(chain = true)
public class Value {

    private static final int MAX_RECURSION_LEVEL = 32;

    private final Object data;

    public Value() {
        data = new LinkedHashMap<>();
    }

    public Value(Object data) {
        this.data = data;
    }

    public static Result<String> json(List<Value> list) {
        return json(list, 0);
    }

    private static Result<String> json(List<Value> list, int level) {
        if (level > MAX_RECURSION_LEVEL) {
            return Err("Serialization recursion level is more than {}", MAX_RECURSION_LEVEL);
        }

        if (list == null) {
            return Ok("null");
        }

        StringBuilder buf = new StringBuilder();

        int i = 0;
        buf.append('[');
        for (Value entry : list) {
            if (i > 0) {
                buf.append(',');
            }
            Result<String> res = json(entry, level + 1);
            if (isErr(res)) {
                return Err(res);
            }
            buf.append(res.getValue());
            i ++;
        }
        buf.append(']');

        return Ok(buf.toString());
    }

    public static Result<String> json(Value value) {
        return json(value, 0);
    }

    private static Result<String> json(Value value, int level) {
        if (level > MAX_RECURSION_LEVEL) {
            return Err("Serialization recursion level is more than {}", MAX_RECURSION_LEVEL);
        }

        if (value == null) {
            return Ok("null");
        }

        if (value.data == null) {
            return Ok("{ }");
        }

        StringBuilder buf = new StringBuilder();

        if (isMap(value)) {
            Map<String, Object> map = asMap(value);

            int i = 0;
            buf.append('{');
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (i > 0) {
                    buf.append(',');
                }
                Object item = entry.getValue();
                String text;
                if (item instanceof List list) {
                    Result<String> res = json(list, level + 1);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    text = res.getValue();
                } else {
                    Result<String> res = SerdeJson.toString(item);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    text = res.getValue();
                }
                buf.append('"').append(entry.getKey()).append('"').append(':').append(text);
                i ++;
            }
            buf.append('}');
        } else if (isList(value)) {
            List<Value> list = asList(value);
            Result<String> res = json(list, level + 1);
            if (isErr(res)) {
                return Err(res);
            }
            buf.append(res.getValue());
        } else {
            return Err("Unknown data type (must be a Map or a List): {}", value.data.getClass().getName());
        }

        return Ok(buf.toString());
    }

    public static Result<String> yaml(List<Value> list) {
        return yaml(list, 0);
    }

    private static Result<String> yaml(List<Value> list, int level) {
        if (level > MAX_RECURSION_LEVEL) {
            return Err("Serialization recursion level is more than {}", MAX_RECURSION_LEVEL);
        }

        if (list == null) {
            return Ok("null");
        }

        StringBuilder buf = new StringBuilder();

        buf.append(repeat("  ", level - 1));
        for (Value entry : list) {
            Result<String> res = yaml(entry, level + 1);
            if (isErr(res)) {
                return Err(res);
            }
            buf.append(res.getValue());
        }

        return Ok(buf.toString());
    }

    public static Result<String> yaml(Value value) {
        return yaml(value, 0);
    }

    private static Result<String> yaml(Value value, int level) {
        if (level > MAX_RECURSION_LEVEL) {
            return Err("Serialization recursion level is more than {}", MAX_RECURSION_LEVEL);
        }

        if (value == null) {
            return Ok("null");
        }

        if (value.data == null) {
            return Ok("");
        }

        StringBuilder buf = new StringBuilder();

        if (isMap(value)) {
            Map<String, Object> map = asMap(value);

            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object item = entry.getValue();
                String text;
                if (item instanceof List list) {
                    Result<String> res = yaml(list, level);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    text = res.getValue();
                    buf.append(repeat("  ", level)).append(entry.getKey()).append(":\n").append(text);
                } else if (item instanceof Value val) {
                    Result<String> res = yaml(val, level);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    text = res.getValue();
                    buf.append(repeat("  ", level)).append(entry.getKey()).append(":\n").append(text);
                } else {
                    Result<String> res = SerdeYaml.asStr(item);
                    if (isErr(res)) {
                        return Err(res);
                    }
                    text = res.getValue();
                    if (first && level > 1) {
                        buf.append(repeat("  ", level - 1)).append("- ");
                    } else {
                        buf.append(repeat("  ", level));
                    }
                    buf.append(entry.getKey()).append(": ").append(text).append('\n');
                }

                first = false;
            }
        } else if (isList(value)) {
            List<Value> list = asList(value);
            Result<String> res = yaml(list, level + 1);
            if (isErr(res)) {
                return Err(res);
            }
            buf.append(res.getValue());
        } else {
            return Err("Unknown data type (must be a Map or a List): {}", value.data.getClass().getName());
        }

        return Ok(buf.toString());
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

    public static Result<Value> json(Object obj, ObjectMapper mapper) {
        try {
            if (obj == null) {
                return Ok(new Value());
            }
            String json = mapper.writeValueAsString(obj);
            return fromStr(json, mapper);
        } catch (Exception ex) {
            return Err(ex);
        }
    }

    public static Result<Value> fromStr(String str, ObjectMapper mapper) {
        if (isBlank(str)) {
            return Ok(new Value());
        }

        try {
            char ch = getFirstChar(str);

            if (ch == '{') {
                LinkedHashMap<String, Object> map = mapper.readValue(str, new TypeReference<>() {});
                return Ok(new Value(map));
            }

            if (ch == '[') {
                List<LinkedHashMap<String, Object>> map = mapper.readValue(str, new TypeReference<>() {});
                return Ok(new Value(map));
            }

            return Err("Not a Value.");
        } catch (JacksonException ex) {
            return Err(ex);
        }
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

    public static Map<String, Object> asMap(Value value) {
        if (!isMap(value)) {
            throw new IllegalStateException("Not a map: " + value);
        }

        return (Map<String, Object>)value.data;
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
