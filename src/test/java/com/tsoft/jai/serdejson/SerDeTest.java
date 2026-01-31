package com.tsoft.jai.serdejson;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.reqwest.Response;
import com.tsoft.jai.reqwest.StatusCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SerDeTest {

    @Test
    void parse() {
        Response res = new Response(StatusCode.HTTP_OK).setValue("""
            {
              "int": 1,
              "char": "a",
              "double": 0.11,
              "listInts": [ 1, 2, 3 ],
              "listChars": [ "a", "b", "c" ],
              "listDoubles": [ 1.1, 2.22, 3.333 ],
              "object": {
                "f1": "v1",
                "f2": "v2"
              },
              "listObjects": [
                { "f1": 1, "f2": "1" },
                { "f1": 2, "f2": "2" }
              ]
            }
            """);

        Result<Value> resValue = res.getJson();

        Value value = resValue.getValue();
        assertEquals(1, value.get("int").asInt());
        assertEquals("a", value.get("char").asStr());
        assertEquals("v1", value.get("object", "f1").asStr());
        assertEquals("v2", value.get("object", "f2").asStr());
        assertEquals(1, value.get("listObjects", 0, "f1").asInt());
        assertEquals("1", value.get("listObjects", 0, "f2").asStr());
        assertEquals(2, value.get("listObjects", 1, "f1").asInt());
        assertEquals("2", value.get("listObjects", 1, "f2").asStr());
    }
}