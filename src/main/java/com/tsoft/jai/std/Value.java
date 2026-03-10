package com.tsoft.jai.std;

import java.util.HashMap;
import java.util.Map;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;
import static com.tsoft.jai.utils.CollectionsUtils.isEmpty;

public class Value {

    private final Map<String, Object> map = new HashMap<>();

    public <T> T get(String name) {
        return isEmpty(name) ? null : (T)map.get(name);
    }

    public Result<Value> set(String name, Object value) {
        if (isEmpty(name)) {
            return Err("name is required");
        } else {
            map.put(name, value);
            return Ok(this);
        }
    }
}
