package com.tsoft.jai.reqwest;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.serde.serdejson.SerdeJson;
import com.tsoft.jai.serde.Value;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
@RequiredArgsConstructor
public class Response {

    private final StatusCode status;

    private String value;
    private Result<Value> json;

    public Response setValue(String value) {
        this.value = value;
        json = SerdeJson.fromStr(value);
        return this;
    }
}
