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

    private final StatusCode status;
    private final String value;

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

            if (value != null) {
                jsonValue = SerDe.parseJson(value);
            }

            jsonValueInitialized.set(true);
            return jsonValue;
        }
    }
}
