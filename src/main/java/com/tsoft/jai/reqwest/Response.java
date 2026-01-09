package com.tsoft.jai.reqwest;

import com.tsoft.jai.serdejson.SerDe;
import com.tsoft.jai.serdejson.Value;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

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

            if (value != null) {
                Value val = SerDe.parseJson(value.toString());
                if (val != null) {
                    return val;
                }
            }

            return null;
        }
    }
}
