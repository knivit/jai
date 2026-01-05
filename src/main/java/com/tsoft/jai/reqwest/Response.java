package com.tsoft.jai.reqwest;

import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tsoft.jai.serdejson.SerDe.jsonMapper;

@Slf4j
@Data
@Accessors(chain = true)
public class Response {

    private StatusCode status;
    private Object value;

    private final AtomicBoolean jsonValueInitialized = new AtomicBoolean();
    private volatile Value jsonValue;

    public Value getJson() {
        if (jsonValueInitialized.get()) {
            return jsonValue;
        }

        synchronized (jsonValueInitialized) {
            if (jsonValueInitialized.get()) {
                return jsonValue;
            }

            jsonValueInitialized.set(true);

            try {
                if (value != null) {
                    String str = value.toString();

                    char ch = '\0';
                    for (int i = 0; i < str.length(); i ++) {
                        ch = str.charAt(i);
                        if (ch > ' ') {
                            break;
                        }
                    }

                    if (ch == '{') {
                        LinkedHashMap<String, Object> map = jsonMapper.readValue(str, new TypeReference<>() {});
                        return new Value(map);
                    }

                    if (ch == '[') {
                        List<LinkedHashMap<String, Object>> map = jsonMapper.readValue(str, new TypeReference<>() {});
                        return new Value(map);
                    }
                }

                throw new IllegalStateException("Not a JSON: " + value);
            } catch (JacksonException ex) {
                log.error("Fail to parse as JSON: {}", value);
                throw ex;
            }
        }
    }
}
