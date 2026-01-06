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

    public String asStr() {
        return (data == null) ? null : data.toString();
    }

    public Integer asInt() {
        return (data == null) ? null : Integer.valueOf(data.toString());
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
}
